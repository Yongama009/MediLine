ALTER TABLE patients ADD COLUMN IF NOT EXISTS email VARCHAR(255);

ALTER TABLE patients
ADD COLUMN IF NOT EXISTS password VARCHAR(255) NOT NULL DEFAULT 'temp';

UPDATE patients
SET
    email = 'patient' || id || '@mediline.clinic'
WHERE
    email IS NULL;

UPDATE patients SET password = 'password123' WHERE password = 'temp';

ALTER TABLE patients ALTER COLUMN email SET NOT NULL;

DROP INDEX IF EXISTS patients_email_key;

CREATE UNIQUE INDEX IF NOT EXISTS patients_email_key ON patients (email);