SET
    @role_column_exists := (
        SELECT COUNT(*)
        FROM information_schema.columns
        WHERE
            table_schema = DATABASE()
            AND table_name = 'patients'
            AND column_name = 'role'
    );

SET
    @sql := IF(
        @role_column_exists = 0,
        'ALTER TABLE patients ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT ''PATIENT''',
        'SELECT 1'
    );

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

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
FROM dual
WHERE
    NOT EXISTS (
        SELECT 1
        FROM patients
        WHERE
            email = 'admin@mediline.clinic'
    );