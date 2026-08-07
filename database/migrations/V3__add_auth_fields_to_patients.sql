ALTER TABLE patients
ADD COLUMN email VARCHAR(255) NOT NULL UNIQUE DEFAULT 'temp@example.com';

ALTER TABLE patients
ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT 'temp';

-- Update the default values for existing rows
UPDATE patients
SET
    email = CONCAT(
        'patient',
        id,
        '@mediline.clinic'
    )
WHERE
    email = 'temp@example.com';

UPDATE patients SET password = 'password123' WHERE password = 'temp';