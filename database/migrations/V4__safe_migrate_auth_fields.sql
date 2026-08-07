ALTER TABLE patients ADD COLUMN IF NOT EXISTS email VARCHAR(255);

ALTER TABLE patients
ADD COLUMN IF NOT EXISTS password VARCHAR(255) NOT NULL DEFAULT 'temp';

UPDATE patients
SET
    email = CONCAT(
        'patient',
        id,
        '@mediline.clinic'
    )
WHERE
    email IS NULL;

UPDATE patients SET password = 'password123' WHERE password = 'temp';

ALTER TABLE patients MODIFY COLUMN email VARCHAR(255) NOT NULL;

SET
    @idx := (
        SELECT COUNT(*)
        FROM information_schema.statistics
        WHERE
            table_schema = DATABASE()
            AND table_name = 'patients'
            AND index_name = 'patients_email_key'
    );

SET
    @sql := IF(
        @idx > 0,
        'ALTER TABLE patients DROP INDEX patients_email_key',
        'SELECT 1'
    );

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

CREATE UNIQUE INDEX IF NOT EXISTS patients_email_key ON patients (email);