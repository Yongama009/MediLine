-- Add SMS opt-in flag to patients
ALTER TABLE patients
ADD COLUMN sms_opt_in BOOLEAN DEFAULT FALSE NOT NULL;