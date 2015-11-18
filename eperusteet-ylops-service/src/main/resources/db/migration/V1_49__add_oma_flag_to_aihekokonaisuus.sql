ALTER TABLE aihekokonaisuus ADD COLUMN oma BOOLEAN DEFAULT FALSE;
UPDATE aihekokonaisuus SET oma = FALSE;
ALTER TABLE aihekokonaisuus ALTER COLUMN oma SET NOT NULL;
ALTER TABLE aihekokonaisuus_aud ADD COLUMN oma BOOLEAN;
