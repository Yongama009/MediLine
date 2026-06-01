const API_BASE = 'http://localhost:8080/api';
let authHeader = null;
let currentUsername = null;
let currentRole = 'nurse';
let patients = [];
let appointments = [];
let patientSearch = '';
let editingPatientId = null;
let appRoot = null;

export function renderApp(root) {
    appRoot = root;
    renderLanding();
}

function buildHeaders(options = {}) {
    const headers = {
        ...(options.headers || {})
    };

    if (authHeader) {
        headers.Authorization = authHeader;
    }

    if (options.body && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
    }

    return headers;
}

async function fetchApi(path, options = {}) {
    const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers: buildHeaders(options)
    });

    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || response.statusText);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

function setStatus(text, type = '') {
    const status = appRoot.querySelector('#status') || appRoot.querySelector('#dashboard-status');
    if (!status) {
        return;
    }
    status.textContent = text;
    status.className = type ? `message ${type}` : 'message';
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function renderLanding() {
    appRoot.innerHTML = `
        <div class="section-title">Choose your access</div>
        <div class="switch-row">
            <label>
                Access type
                <select id="access-type" aria-label="Select patient or admin access">
                    <option value="patient">Patient registration</option>
                    <option value="admin">Admin login</option>
                </select>
            </label>
        </div>

        <div id="patient-section">
            <div class="section-title">Register a patient</div>
            <form id="patient-form">
                <label>
                    Full name
                    <input type="text" id="name" required placeholder="John Doe" />
                </label>
                <label>
                    Phone number
                    <input type="text" id="phone" required placeholder="(555) 123-4567" />
                </label>
                <label>
                    Notes
                    <textarea id="notes" placeholder="Any patient notes..."></textarea>
                </label>
                <button type="submit" id="patient-submit">Register Patient</button>
            </form>
        </div>

        <div id="admin-section" class="hidden">
            <div class="section-title">Admin sign in</div>
            <form id="admin-form">
                <label>
                    Your role
                    <select id="admin-role" aria-label="Select admin role">
                        <option value="nurse">Nurse</option>
                        <option value="doctor">Doctor</option>
                    </select>
                </label>
                <label>
                    Username
                    <input type="text" id="username" required placeholder="admin" />
                </label>
                <label>
                    Password
                    <input type="password" id="password" required placeholder="••••••••" />
                </label>
                <button type="submit">Sign In</button>
            </form>
            <p class="helper-text">Use <strong>admin</strong> / <strong>password</strong> for demo access.</p>
        </div>

        <div id="status" class="message"></div>
    `;

    attachLandingListeners();
}

function attachLandingListeners() {
    const accessType = appRoot.querySelector('#access-type');
    const adminSection = appRoot.querySelector('#admin-section');
    const patientSection = appRoot.querySelector('#patient-section');

    accessType.addEventListener('change', () => {
        const mode = accessType.value;
        if (mode === 'admin') {
            adminSection.classList.remove('hidden');
            patientSection.classList.add('hidden');
        } else {
            patientSection.classList.remove('hidden');
            adminSection.classList.add('hidden');
        }
        setStatus('', '');
    });

    appRoot.querySelector('#patient-form').addEventListener('submit', handlePatientRegistration);
    appRoot.querySelector('#admin-form').addEventListener('submit', handleAdminLogin);
}

async function handlePatientRegistration(event) {
    event.preventDefault();
    const payload = {
        name: appRoot.querySelector('#name').value.trim(),
        phone: appRoot.querySelector('#phone').value.trim(),
        notes: appRoot.querySelector('#notes').value.trim()
    };

    const button = appRoot.querySelector('#patient-submit');
    button.disabled = true;
    button.textContent = 'Registering...';
    setStatus('Saving patient record...', '');

    try {
        const response = await fetch(`${API_BASE}/patients`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Unable to register patient');
        }

        const patient = await response.json();
        setStatus(`Patient registered successfully. ID #${patient.id}`, 'success');
        appRoot.querySelector('#patient-form').reset();
    } catch (error) {
        setStatus(error.message, 'error');
    } finally {
        button.disabled = false;
        button.textContent = 'Register Patient';
    }
}

async function handleAdminLogin(event) {
    event.preventDefault();
    const username = appRoot.querySelector('#username').value.trim();
    const password = appRoot.querySelector('#password').value;
    currentRole = appRoot.querySelector('#admin-role').value;
    authHeader = `Basic ${btoa(`${username}:${password}`)}`;
    currentUsername = username;
    setStatus('Validating credentials...', '');

    try {
        await refreshPatients();
        await loadAppointments();
        renderAdminDashboard();
        setStatus(`Signed in as ${currentRole}.`, 'success');
    } catch (error) {
        authHeader = null;
        currentUsername = null;
        setStatus(error.message || 'Login failed', 'error');
    }
}

function renderAdminDashboard() {
    appRoot.innerHTML = `
        <div class="section-title">Admin dashboard</div>
        <div class="switch-row">
            <div>
                <strong>Signed in as:</strong> ${escapeHtml(currentUsername)}
            </div>
            <button id="logout-button">Sign out</button>
        </div>

        <section class="card-grid">
            <div class="card-panel">
                <div class="section-title">Search patients</div>
                <form id="search-form">
                    <label>
                        Search by name or phone
                        <input id="patient-search" type="search" placeholder="Type a name or phone" value="${escapeHtml(patientSearch)}" />
                    </label>
                    <button type="submit">Search</button>
                </form>
            </div>
            <div class="card-panel">
                <div class="section-title">Schedule appointment</div>
                <form id="appointment-form">
                    <label>
                        Patient
                        <select id="appointment-patient" required></select>
                    </label>
                    <label>
                        Date & time
                        <input type="datetime-local" id="appointment-time" required />
                    </label>
                    <label>
                        Reason
                        <textarea id="appointment-reason" placeholder="Brief appointment details..."></textarea>
                    </label>
                    <button type="submit">Create Appointment</button>
                </form>
            </div>
        </section>

        <div id="dashboard-status"></div>

        <section class="table-section">
            <div class="section-title">Patients</div>
            <div id="patient-table-container"></div>
        </section>

        <section class="table-section">
            <div class="section-title">Appointments</div>
            <div id="appointment-table-container"></div>
        </section>
    `;

    appRoot.querySelector('#logout-button').addEventListener('click', () => {
        authHeader = null;
        currentUsername = null;
        patients = [];
        appointments = [];
        patientSearch = '';
        renderLanding();
    });

    appRoot.querySelector('#search-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        patientSearch = appRoot.querySelector('#patient-search').value.trim();
        await refreshPatients();
        renderPatientTable();
        populatePatientSelect();
    });

    appRoot.querySelector('#appointment-form').addEventListener('submit', handleCreateAppointment);
    renderPatientTable();
    renderAppointmentTable();
    populatePatientSelect();
}

async function refreshPatients() {
    const query = patientSearch ? `?q=${encodeURIComponent(patientSearch)}` : '';
    patients = await fetchApi(`/patients${query}`);
}

async function loadAppointments() {
    appointments = await fetchApi('/appointments');
}

function populatePatientSelect() {
    const select = appRoot.querySelector('#appointment-patient');
    if (!select) {
        return;
    }

    if (!patients.length) {
        select.innerHTML = '<option value="">No patients available</option>';
        return;
    }

    select.innerHTML = patients
        .map((patient) => `<option value="${patient.id}">${escapeHtml(patient.name)} (#${patient.id})</option>`)
        .join('');
}

function renderPatientTable() {
    const container = appRoot.querySelector('#patient-table-container');
    if (!container) {
        return;
    }

    if (!patients.length) {
        container.innerHTML = '<p>No patients found.</p>';
        return;
    }

    const rows = patients
        .map((patient) => {
            if (editingPatientId === patient.id) {
                return `
                    <tr>
                        <td>${patient.id}</td>
                        <td><input id="edit-name-${patient.id}" value="${escapeHtml(patient.name)}" /></td>
                        <td><input id="edit-phone-${patient.id}" value="${escapeHtml(patient.phone)}" /></td>
                        <td><textarea id="edit-notes-${patient.id}">${escapeHtml(patient.notes || '')}</textarea></td>
                        <td>${formatDateTime(patient.createdAt)}</td>
                        <td class="actions">
                            <button class="save-button" data-id="${patient.id}">Save</button>
                            <button class="cancel-button" data-id="${patient.id}">Cancel</button>
                        </td>
                    </tr>
                `;
            }

            return `
                <tr>
                    <td>${patient.id}</td>
                    <td>${escapeHtml(patient.name)}</td>
                    <td>${escapeHtml(patient.phone)}</td>
                    <td>${escapeHtml(patient.notes || '')}</td>
                    <td>${formatDateTime(patient.createdAt)}</td>
                    <td class="actions">
                        <button class="edit-button" data-id="${patient.id}">Edit</button>
                        <button class="delete-button" data-id="${patient.id}">Delete</button>
                    </td>
                </tr>
            `;
        })
        .join('');

    container.innerHTML = `
        <div class="table-wrapper">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Phone</th>
                        <th>Notes</th>
                        <th>Created</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    ${rows}
                </tbody>
            </table>
        </div>
    `;

    container.querySelectorAll('.edit-button').forEach((button) => {
        button.addEventListener('click', (event) => {
            editingPatientId = Number(event.target.dataset.id);
            renderPatientTable();
        });
    });

    container.querySelectorAll('.delete-button').forEach((button) => {
        button.addEventListener('click', async (event) => {
            const id = Number(event.target.dataset.id);
            if (window.confirm('Delete this patient record?')) {
                await handleDeletePatient(id);
            }
        });
    });

    container.querySelectorAll('.save-button').forEach((button) => {
        button.addEventListener('click', async (event) => {
            const id = Number(event.target.dataset.id);
            await handleUpdatePatient(id);
        });
    });

    container.querySelectorAll('.cancel-button').forEach((button) => {
        button.addEventListener('click', () => {
            editingPatientId = null;
            renderPatientTable();
        });
    });
}

function renderAppointmentTable() {
    const container = appRoot.querySelector('#appointment-table-container');
    if (!container) {
        return;
    }

    if (!appointments.length) {
        container.innerHTML = '<p>No appointments scheduled yet.</p>';
        return;
    }

    const rows = appointments
        .map((appointment) => `
            <tr>
                <td>${appointment.id}</td>
                <td>${escapeHtml(appointment.patientName)}</td>
                <td>${formatDateTime(appointment.scheduledAt)}</td>
                <td>${escapeHtml(appointment.reason || '')}</td>
                <td>${formatDateTime(appointment.createdAt)}</td>
            </tr>
        `)
        .join('');

    container.innerHTML = `
        <div class="table-wrapper">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Patient</th>
                        <th>Appointment</th>
                        <th>Reason</th>
                        <th>Created</th>
                    </tr>
                </thead>
                <tbody>
                    ${rows}
                </tbody>
            </table>
        </div>
    `;
}

async function handleCreateAppointment(event) {
    event.preventDefault();
    const patientId = Number(appRoot.querySelector('#appointment-patient').value);
    const scheduledAt = appRoot.querySelector('#appointment-time').value;
    const reason = appRoot.querySelector('#appointment-reason').value.trim();

    setStatus('Scheduling appointment...', '');

    try {
        await fetchApi('/appointments', {
            method: 'POST',
            body: JSON.stringify({
                patientId,
                scheduledAt,
                reason
            })
        });

        await loadAppointments();
        renderAppointmentTable();
        setStatus('Appointment created successfully.', 'success');
    } catch (error) {
        setStatus(error.message, 'error');
    }
}

async function handleUpdatePatient(id) {
    const name = appRoot.querySelector(`#edit-name-${id}`).value.trim();
    const phone = appRoot.querySelector(`#edit-phone-${id}`).value.trim();
    const notes = appRoot.querySelector(`#edit-notes-${id}`).value.trim();

    setStatus('Updating patient...', '');

    try {
        await fetchApi(`/patients/${id}`, {
            method: 'PUT',
            body: JSON.stringify({ name, phone, notes })
        });

        editingPatientId = null;
        await refreshPatients();
        renderPatientTable();
        setStatus('Patient updated successfully.', 'success');
    } catch (error) {
        setStatus(error.message, 'error');
    }
}

async function handleDeletePatient(id) {
    setStatus('Deleting patient...', '');

    try {
        await fetchApi(`/patients/${id}`, {
            method: 'DELETE'
        });

        await refreshPatients();
        await loadAppointments();
        renderPatientTable();
        renderAppointmentTable();
        setStatus('Patient deleted successfully.', 'success');
    } catch (error) {
        setStatus(error.message, 'error');
    }
}

function formatDateTime(value) {
    if (!value) {
        return '';
    }

    const date = new Date(value);
    return date.toLocaleString([], {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}
