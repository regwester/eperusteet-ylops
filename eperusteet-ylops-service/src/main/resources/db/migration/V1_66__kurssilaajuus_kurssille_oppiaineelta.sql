
ALTER TABLE oppiaine DROP COLUMN lukio_laajuus;
ALTER TABLE oppiaine_aud DROP COLUMN lukio_laajuus;

ALTER TABLE lukiokurssi ADD COLUMN laajuus DECIMAL(4,2);
ALTER TABLE lukiokurssi_aud ADD COLUMN laajuus DECIMAL(4,2);
UPDATE lukiokurssi SET laajuus = 1;