ALTER TABLE patients
ADD COLUMN IF NOT EXISTS role VARCHAR(50) NOT NULL DEFAULT 'PATIENT';

UPDATE patients SET role = 'PATIENT' WHERE role IS NULL;

INSERT INTO
    patients (
        name,
        phone,
        email,
        password,
        notes,
        role
    )
SELECT 'admin', '0000000000', 'admin@mediline.clinic', 'password', 'Seeded administrator account', 'ADMIN'
WHERE
    NOT EXISTS (
        SELECT 1
        FROM patients
        WHERE
            email = 'admin@mediline.clinic'
    );