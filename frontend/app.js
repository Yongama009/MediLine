const API_BASE = 'http://localhost:8080/api';
/** @type {string | null} */
let authHeader = null;
/** @type {string | null} */
let currentUsername = null;
/** @type {string} */
let currentRole = 'nurse';
/** @type {any[]} */
let patients = [];
/** @type {any[]} */
let appointments = [];
/** @type {string} */
let patientSearch = '';
/** @type {number | null} */
let editingPatientId = null;
/** @type {HTMLElement | null} */
let appRoot = null;

/**
 * Initializes and renders the application into the root element.
 * @param {HTMLElement} root 
 */
export function renderApp(root) {
    appRoot = root;
    renderLanding();
}

/**
 * Builds the headers for API requests.
 * @param {RequestInit} [options] 
 * @returns {Record<string, string>}
 */
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

/**
 * Helper to fetch data from the API.
 * @param {string} path 
 * @param {RequestInit} [options] 
 * @returns {Promise<any>}
 */
async function fetchApi(path, options = {}) {
    const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers: buildHeaders(options)
    });

    if (response.status === 204 || response.status === 205) {
        return null;
    }

    const text = await response.text();
    if (!response.ok) {
        let errorMessage = text || response.statusText;
        try {
            const json = JSON.parse(text);
            if (typeof json === 'object' && json !== null) {
                // Extract the specific validation messages (e.g., "Reason for visit is required")
                errorMessage = Object.values(json).join(' ');
            }
        } catch (e) { /* ignore parse errors */ }
        throw new Error(errorMessage);
    }

    return text ? JSON.parse(text) : null;
}

/**
 * Sets the status message on the dashboard.
 * @param {string} text - The message to display.
 * @param {string} [type] - The type of message (e.g., 'error', 'success').
 */
function setStatus(text, type = '') {
    const status = appRoot.querySelector('#status') || appRoot.querySelector('#dashboard-status');
    if (!status) {
        return;
    }
    status.textContent = text;
    status.className = type ? `message ${type}` : 'message';
}

/**
 * Escapes HTML characters to prevent XSS.
 * @param {any} value 
 * @returns {string}
 */
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
                    Email
                    <input type="email" id="email" required placeholder="john@example.com" />
                </label>
                <label>
                    Phone number
                    <input type="text" id="phone" required placeholder="(555) 123-4567" />
                </label>
                <label>
                    Password
                    <input type="password" id="reg-password" required placeholder="••••••••" />
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

/**
 * Handles new patient registration.
 * @param {SubmitEvent} event 
 */
async function handlePatientRegistration(event) {
    event.preventDefault();
    const payload = {
        name: appRoot.querySelector('#name').value.trim(),
        email: appRoot.querySelector('#email').value.trim(),
        phone: appRoot.querySelector('#phone').value.trim(),
        password: appRoot.querySelector('#reg-password').value,
        notes: appRoot.querySelector('#notes').value.trim()
    };

    const button = appRoot.querySelector('#patient-submit');
    button.disabled = true;
    button.textContent = 'Registering...';
    setStatus('Saving patient record...', '');

    try {
        const patient = await fetchApi('/patients', {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        authHeader = `Basic ${btoa(`${payload.email}:${payload.password}`)}`;
        currentUsername = payload.email;
        renderPatientDashboard(patient);
    } catch (error) {
        setStatus(error.message, 'error');
    } finally {
        button.disabled = false;
        button.textContent = 'Register Patient';
    }
}

/**
 * Handles admin login validation.
 * @param {SubmitEvent} event 
 */
async function handleAdminLogin(event) {
    event.preventDefault();
    const username = appRoot.querySelector('#username').value.trim();
    const password = appRoot.querySelector('#password').value;
    currentRole = appRoot.querySelector('#admin-role').value;
    authHeader = `Basic ${btoa(`${username}:${password}`)}`;
    currentUsername = username;
    setStatus('Validating credentials...', '');

    try {
        await Promise.all([loadAppointments(), refreshPatients()]);
        renderAdminDashboard();
        setStatus(`Signed in as ${currentRole}.`, 'success');
    } catch (error) {
        authHeader = null;
        currentUsername = null;
        setStatus(error.message || 'Login failed', 'error');
    }
}

/**
 * Renders the Admin Dashboard UI.
 */
function renderAdminDashboard() {
    appRoot.innerHTML = `
        <div class="section-title">Admin Dashboard</div>
        <div class="switch-row">
            <div>Welcome back, <strong>${escapeHtml(currentUsername)}</strong>. Manage clinic operations below.</div>
            <button id="logout-button">Sign out</button>
        </div>

        <div id="dashboard-status"></div>

        <section class="card-grid">
            <div class="card-panel">
                <div class="section-title">Search patients</div>
                <div class="helper-text">Search by name, phone, or email</div>
                <div class="search-box">
                    <input type="text" id="patient-search-input" placeholder="Search patients..." value="${escapeHtml(patientSearch)}" />
                    <button id="search-button">Search</button>
                </div>
            </div>

            <div class="card-panel">
                <div class="section-title">Schedule Appointment</div>
                <form id="admin-appointment-form">
                    <label>
                        Patient
                        <select id="admin-patient-select" required>
                            <option value="">Choose a patient...</option>
                            ${patients.map(p => `<option value="${p.id}">${escapeHtml(p.name)} (${escapeHtml(p.email)})</option>`).join('')}
                        </select>
                    </label>
                    <label>
                        Date & time
                        <input type="datetime-local" id="admin-appointment-time" required />
                    </label>
                    <label>
                        Reason
                        <textarea id="admin-appointment-reason" placeholder="Brief appointment details..."></textarea>
                    </label>
                    <button type="submit">Create Appointment</button>
                </form>
            </div>
        </section>

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
        appointments = [];
        renderLanding();
    });

    appRoot.querySelector('#search-button').addEventListener('click', async () => {
        patientSearch = appRoot.querySelector('#patient-search-input').value.trim();
        await refreshPatients();
        renderAdminDashboard();
    });

    appRoot.querySelector('#admin-appointment-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const patientId = appRoot.querySelector('#admin-patient-select').value;
        const scheduledAt = appRoot.querySelector('#admin-appointment-time').value;
        const reason = appRoot.querySelector('#admin-appointment-reason').value.trim();
        await handleAdminScheduleAppointment(patientId, scheduledAt, reason);
    });

    renderAppointmentTable();
    renderPatientTable();
}

/**
 * Renders the Patient Dashboard UI.
 * @param {any} patient 
 */
function renderPatientDashboard(patient) {
    appRoot.innerHTML = `
        <div class="section-title">Patient dashboard</div>
        <div class="switch-row">
            <div>
                Welcome, <strong>${escapeHtml(patient.name)}</strong>. You can book your appointments here.
            </div>
            <button id="logout-button">Sign out</button>
        </div>

        <div id="dashboard-status"></div>

        <section class="card-grid">
            <div class="card-panel">
                <div class="section-title">Schedule an appointment</div>
                <form id="appointment-form">
                    <label>
                        Date & time
                        <input type="datetime-local" id="appointment-time" required />
                    </label>
                    <label>
                        Reason for visit
                        <textarea id="appointment-reason" required placeholder="Briefly describe why you are visiting..."></textarea>
                    </label>
                    <button type="submit">Book Appointment</button>
                </form>
            </div>

            <div class="card-panel">
                <div class="section-title">Your Appointments</div>
                <div id="patient-appointments-list">Loading your appointments...</div>
            </div>
        </section>
    `;

    appRoot.querySelector('#logout-button').addEventListener('click', () => {
        authHeader = null;
        currentUsername = null;
        renderLanding();
    });

    appRoot.querySelector('#appointment-form').addEventListener('submit', (e) => handleCreateAppointment(e, patient.id));
    refreshPatientAppointments(patient.id);
}

/**
 * Handles appointment creation for a specific patient.
 * @param {SubmitEvent} event 
 * @param {number} patientId 
 */
async function handleCreateAppointment(event, patientId) {
    event.preventDefault();
    const scheduledAt = appRoot.querySelector('#appointment-time').value;
    const reason = appRoot.querySelector('#appointment-reason').value.trim();

    if (!reason) {
        setStatus('Please provide a reason for your visit.', 'error');
        return;
    }

    if (!scheduledAt) {
        setStatus('Please select a date and time for your appointment.', 'error');
        return;
    }

    setStatus('Scheduling appointment...', '');

    try {
        await fetchApi('/appointments', {
            method: 'POST',
            body: JSON.stringify({ patientId: Number(patientId), scheduledAt, reason })
        });
        setStatus('Appointment scheduled successfully!', 'success');
        appRoot.querySelector('#appointment-form').reset();
        refreshPatientAppointments(patientId);
    } catch (error) {
        setStatus(error.message, 'error');
    }
}

/**
 * Handles appointment creation by an admin.
 */
async function handleAdminScheduleAppointment(patientId, scheduledAt, reason) {
    setStatus('Scheduling appointment...', '');

    try {
        await fetchApi('/appointments', {
            method: 'POST',
            body: JSON.stringify({ patientId: Number(patientId), scheduledAt, reason })
        });
        setStatus('Appointment scheduled successfully!', 'success');
        await loadAppointments();
        renderAppointmentTable();
        appRoot.querySelector('#admin-appointment-form').reset();
    } catch (error) {
        setStatus(error.message, 'error');
    }
}

/**
 * Fetches and renders appointments for a specific patient.
 * @param {number} patientId 
 */
async function refreshPatientAppointments(patientId) {
    const container = appRoot.querySelector('#patient-appointments-list');
    if (!container) return;

    try {
        // Note: Ensure the backend allows PATIENT role to GET /api/appointments 
        // or implement a specific /api/patients/{id}/appointments endpoint.
        await loadAppointments();
        const myAppointments = appointments.filter(a => Number(a.patientId) === Number(patientId));

        if (myAppointments.length === 0) {
            container.innerHTML = '<p class="helper-text">You have no appointments scheduled yet.</p>';
            return;
        }

        container.innerHTML = `
            <div class="table-wrapper">
                <table>
                    <thead>
                        <tr>
                            <th>Date & Time</th>
                            <th>Reason</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${myAppointments.map(app => `
                            <tr>
                                <td>${formatDateTime(app.scheduledAt)}</td>
                                <td>${escapeHtml(app.reason || '')}</td>
                                <td><span class="status-text">${app.status || 'PENDING'}</span></td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    } catch (error) {
        container.innerHTML = `<p class="message error">Could not load appointments: ${error.message}</p>`;
    }
}

/**
 * Refreshes the patient list from the backend.
 */
async function refreshPatients() {
    const query = patientSearch ? `?q=${encodeURIComponent(patientSearch)}` : '';
    patients = await fetchApi(`/patients${query}`) || [];
}

/**
 * Loads the appointment list from the backend.
 */
async function loadAppointments() {
    appointments = await fetchApi('/appointments') || [];
}

/**
 * Renders the patient management table.
 */
function renderPatientTable() {
    const container = appRoot.querySelector('#patient-table-container');
    if (!container) {
        return;
    }

    const rows = patients.map(patient => {
        const isEditing = editingPatientId === patient.id;
        if (isEditing) {
            return `
                <tr>
                    <td>${patient.id}</td>
                    <td><input type="text" id="edit-name-${patient.id}" value="${escapeHtml(patient.name)}" /></td>
                    <td>${escapeHtml(patient.email)}</td>
                    <td><input type="text" id="edit-phone-${patient.id}" value="${escapeHtml(patient.phone)}" /></td>
                    <td><input type="text" id="edit-notes-${patient.id}" value="${escapeHtml(patient.notes || '')}" /></td>
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
                <td>${escapeHtml(patient.email)}</td>
                <td>${escapeHtml(patient.phone)}</td>
                <td>${escapeHtml(patient.notes || '')}</td>
                <td class="actions">
                    <button class="edit-button" data-id="${patient.id}">Edit</button>
                    <button class="delete-button" data-id="${patient.id}">Delete</button>
                </td>
            </tr>
        `;
    }).join('');

    let html = `
        <div class="table-wrapper">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Phone</th>
                        <th>Notes</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    ${rows}
                </tbody>
            </table>
        </div>
    `;

    container.innerHTML = html;

    container.querySelectorAll('.edit-button').forEach((button) => {
        button.addEventListener('click', (event) => {
            editingPatientId = Number(event.currentTarget.dataset.id);
            renderPatientTable();
        });
    });

    container.querySelectorAll('.delete-button').forEach((button) => {
        button.addEventListener('click', async (event) => {
            const id = Number(event.currentTarget.dataset.id);
            if (window.confirm('Delete this patient record?')) {
                await handleDeletePatient(id);
            }
        });
    });

    container.querySelectorAll('.save-button').forEach((button) => {
        button.addEventListener('click', async (event) => {
            const id = Number(event.currentTarget.dataset.id);
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

/**
 * Renders the appointment management table.
 */
function renderAppointmentTable() {
    const container = appRoot.querySelector('#appointment-table-container');
    if (!container) return;

    if (!appointments || !appointments.length) {
        container.innerHTML = `
            <div class="table-wrapper">
                <table>
                    <thead><tr><th>ID</th><th>Patient</th><th>Reason</th><th>Scheduled At</th><th>Status</th><th>Actions</th></tr></thead>
                    <tbody><tr><td colspan="6" style="text-align:center;">No appointments scheduled yet.</td></tr></tbody>
                </table>
            </div>`;
        return;
    }

    const rows = appointments.map((appointment) => {
        const status = (appointment.status || 'PENDING').toUpperCase();
        const canConfirm = status === 'PENDING';
        const canStart = status === 'CONFIRMED';
        const canComplete = status === 'IN_PROGRESS';

        return `
            <tr>
                <td>${appointment.id}</td>
                <td>${escapeHtml(appointment.patientName)}</td>
                <td>${escapeHtml(appointment.reason || '—')}</td>
                <td>${formatDateTime(appointment.scheduledAt)}</td>
                <td><span class="status-badge ${status.toLowerCase()}">${status}</span></td>
                <td class="actions">
                    ${canConfirm ? `<button class="confirm-button" data-id="${appointment.id}">Confirm</button>` : ''}
                    ${canStart ? `<button class="progress-button" data-id="${appointment.id}">Start</button>` : ''}
                    ${canComplete ? `<button class="complete-button" data-id="${appointment.id}">Complete</button>` : ''}
                    <button class="delete-app-button danger" data-id="${appointment.id}">Delete</button>
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
                        <th>Patient</th>
                        <th>Reason</th>
                        <th>Scheduled At</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    ${rows}
                </tbody>
            </table>
        </div>
    `;

    container.querySelectorAll('.confirm-button').forEach((button) => {
        button.addEventListener('click', async (event) => {
            const id = Number(event.currentTarget.dataset.id);
            await handleConfirmAppointment(id);
        });
    });

    container.querySelectorAll('.progress-button').forEach((button) => {
        button.addEventListener('click', async (event) => {
            const id = Number(event.currentTarget.dataset.id);
            await handleInProgressAppointment(id);
        });
    });

    container.querySelectorAll('.complete-button').forEach((button) => {
        button.addEventListener('click', async (event) => {
            const id = Number(event.currentTarget.dataset.id);
            await handleCompleteAppointment(id);
        });
    });

    container.querySelectorAll('.delete-app-button').forEach((button) => {
        button.addEventListener('click', async (event) => {
            const id = Number(event.currentTarget.dataset.id);
            if (window.confirm('Are you sure you want to delete this appointment?')) {
                await handleDeleteAppointment(id);
            }
        });
    });
}

/**
 * Confirms a pending appointment.
 * @param {number} id 
 */
async function handleConfirmAppointment(id) {
    setStatus('Confirming appointment...', '');

    try {
        await fetchApi(`/appointments/${id}/confirm`, {
            method: 'PUT'
        });

        await loadAppointments();
        renderAppointmentTable();
        setStatus('Appointment confirmed successfully.', 'success');
    } catch (error) {
        setStatus(error.message, 'error');
    }
}

/**
 * Sets an appointment status to In Progress.
 * @param {number} id 
 */
async function handleInProgressAppointment(id) {
    setStatus('Updating status to In Progress...', '');

    try {
        await fetchApi(`/appointments/${id}/in-progress`, {
            method: 'PUT'
        });

        await loadAppointments();
        renderAppointmentTable();
        setStatus('Appointment is now in progress.', 'success');
    } catch (error) {
        setStatus(error.message, 'error');
    }
}

/**
 * Sets an appointment status to Completed.
 * @param {number} id 
 */
async function handleCompleteAppointment(id) {
    setStatus('Completing appointment...', '');

    try {
        await fetchApi(`/appointments/${id}/complete`, {
            method: 'PUT'
        });

        await loadAppointments();
        renderAppointmentTable();
        setStatus('Appointment marked as completed.', 'success');
    } catch (error) {
        setStatus(error.message, 'error');
    }
}

/**
 * Deletes an appointment.
 * @param {number} id 
 */
async function handleDeleteAppointment(id) {
    setStatus('Deleting appointment...', '');

    try {
        await fetchApi(`/appointments/${id}`, {
            method: 'DELETE'
        });

        await loadAppointments();
        renderAppointmentTable();
        setStatus('Appointment deleted successfully.', 'success');
    } catch (error) {
        setStatus(error.message, 'error');
    }
}

/**
 * Updates an existing patient record.
 * @param {number} id 
 */
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

/**
 * Deletes a patient record.
 * @param {number} id 
 */
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

/**
 * Formats a date string for display.
 * @param {string | number | Date} value 
 * @returns {string}
 */
function formatDateTime(value) {
    if (!value) {
        return '';
    }

    const date = new Date(value);
    return date.toLocaleString([], {
        weekday: 'short',
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}
