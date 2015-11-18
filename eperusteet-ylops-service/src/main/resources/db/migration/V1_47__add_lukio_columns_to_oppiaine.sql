ALTER TABLE oppiaine ADD COLUMN jarjestys INT;
ALTER TABLE oppiaine_aud ADD COLUMN jarjestys INT;

ALTER TABLE oppiaine ADD COLUMN tavoitteet_id INT8 REFERENCES tekstiosa(id);
ALTER TABLE oppiaine_aud ADD COLUMN tavoitteet_id INT8;
ALTER TABLE oppiaine ADD COLUMN arvioinnit_id INT8 REFERENCES tekstiosa(id);
ALTER TABLE oppiaine_aud ADD COLUMN arvioinnit_id INT8;

ALTER TABLE oppiaine ADD COLUMN valtakunnallinen_pakollinen_kuvaus_id INT8 REFERENCES lokalisoituteksti(id);
ALTER TABLE oppiaine_aud ADD COLUMN valtakunnallinen_pakollinen_kuvaus_id INT8;
ALTER TABLE oppiaine ADD COLUMN valtakunnallinen_syventava_kuvaus_id INT8 REFERENCES lokalisoituteksti(id);
ALTER TABLE oppiaine_aud ADD COLUMN valtakunnallinen_syventava_kuvaus_id INT8;
ALTER TABLE oppiaine ADD COLUMN valtakunnallinen_soveltava_kuvaus_id INT8 REFERENCES lokalisoituteksti(id);
ALTER TABLE oppiaine_aud ADD COLUMN valtakunnallinen_soveltava_kuvaus_id INT8;

ALTER TABLE oppiaine ADD COLUMN paikallinen_pakollinen_kuvaus_id INT8 REFERENCES lokalisoituteksti(id);
ALTER TABLE oppiaine_aud ADD COLUMN paikallinen_pakollinen_kuvaus_id INT8;
ALTER TABLE oppiaine ADD COLUMN paikallinen_syventava_kuvaus_id INT8 REFERENCES lokalisoituteksti(id);
ALTER TABLE oppiaine_aud ADD COLUMN paikallinen_syventava_kuvaus_id INT8;
ALTER TABLE oppiaine ADD COLUMN paikallinen_soveltava_kuvaus_id INT8 REFERENCES lokalisoituteksti(id);
ALTER TABLE oppiaine_aud ADD COLUMN paikallinen_soveltava_kuvaus_id INT8;
