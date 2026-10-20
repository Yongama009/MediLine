SET
    @email_column_exists := (
        SELECT COUNT(*)
        FROM information_schema.columns
        WHERE
            table_schema = DATABASE()
            AND table_name = 'patients'
            AND column_name = 'email'
    );

SET
    @sql := IF(
        @email_column_exists = 0,
        'ALTER TABLE patients ADD COLUMN email VARCHAR(255) NULL',
        'SELECT 1'
    );

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

SET
    @password_column_exists := (
        SELECT COUNT(*)
        FROM information_schema.columns
        WHERE
            table_schema = DATABASE()
            AND table_name = 'patients'
            AND column_name = 'password'
    );

SET
    @sql := IF(
        @password_column_exists = 0,
        'ALTER TABLE patients ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT ''temp''',
        'SELECT 1'
    );

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

UPDATE patients
SET
    email = CONCAT(
        'patient',
        id,
        '@mediline.clinic'
    )
WHERE
    email IS NULL
    OR email = 'temp@example.com';

UPDATE patients SET password = 'password123' WHERE password = 'temp';

ALTER TABLE patients MODIFY COLUMN email VARCHAR(255) NOT NULL;

SET
    @email_unique_index_exists := (
        SELECT COUNT(*)
        FROM information_schema.statistics
        WHERE
            table_schema = DATABASE()
            AND table_name = 'patients'
            AND column_name = 'email'
            AND non_unique = 0
    );

SET
    @sql := IF(
        @email_unique_index_exists = 0,
        'CREATE UNIQUE INDEX patients_email_key ON patients (email)',
        'SELECT 1'
    );

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;