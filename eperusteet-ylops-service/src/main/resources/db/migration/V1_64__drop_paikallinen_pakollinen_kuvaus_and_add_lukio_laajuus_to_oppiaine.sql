
ALTER TABLE oppiaine DROP COLUMN paikallinen_pakollinen_kuvaus_id;
ALTER TABLE oppiaine_aud DROP COLUMN paikallinen_pakollinen_kuvaus_id;
ALTER TABLE oppiaine ADD COLUMN lukio_laajuus DECIMAL(4,2);
ALTER TABLE oppiaine_aud ADD COLUMN lukio_laajuus DECIMAL(4,2);

UPDATE oppiaine SET lukio_laajuus = 1 WHERE
  id IN (SELECT j.oppiaine_id FROM lukio_oppiaine_jarjestys j);
