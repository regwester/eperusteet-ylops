-- multiple inherited Opetussuunnitelma may share the same Lukiokurssi

-- fix typ: in the table name
ALTER TABLE oppaine_lukiokurssi RENAME TO oppiaine_lukiokurssi;
ALTER TABLE oppaine_lukiokurssi_aud RENAME TO oppiaine_lukiokurssi_aud;

-- move the opetussuunnitelma-relation to oppiaine_lukiokurssi
ALTER TABLE oppiaine_lukiokurssi ADD COLUMN opetussuunnitelma_id INT8 REFERENCES opetussuunnitelma(id);
UPDATE oppiaine_lukiokurssi SET opetussuunnitelma_id = (SELECT k.opetussuunnitelma_id
  FROM kurssi k WHERE k.id = oppiaine_lukiokurssi.kurssi_id);
ALTER TABLE oppiaine_lukiokurssi ALTER COLUMN opetussuunnitelma_id SET NOT NULL;
ALTER TABLE oppiaine_lukiokurssi_aud ADD COLUMN opetussuunnitelma_id INT8;

-- mark oma=true when related kurssi is owned by the related opetussuunnitelma
ALTER TABLE oppiaine_lukiokurssi ADD COLUMN oma BOOLEAN DEFAULT FALSE;
UPDATE oppiaine_lukiokurssi SET oma = TRUE;
ALTER TABLE oppiaine_lukiokurssi ALTER COLUMN oma SET NOT NULL;
ALTER TABLE oppiaine_lukiokurssi_aud ADD COLUMN oma BOOLEAN;

-- remove the relation from kurssi:
ALTER TABLE kurssi DROP COLUMN opetussuunnitelma_id;
ALTER TABLE kurssi_aud DROP COLUMN opetussuunnitelma_id;

-- does not work the same way for Aihekokonaisuus, oma first understood as peruste/OPS-speficied but this
-- discrimination can be done based on existence of tunniste
ALTER TABLE aihekokonaisuus DROP COLUMN oma;
ALTER TABLE aihekokonaisuus_aud DROP COLUMN oma;
