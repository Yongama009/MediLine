-- Add SMS opt-in flag to patients
ALTER TABLE patients
ADD COLUMN sms_opt_in boolean DEFAULT false NOT NULL;

