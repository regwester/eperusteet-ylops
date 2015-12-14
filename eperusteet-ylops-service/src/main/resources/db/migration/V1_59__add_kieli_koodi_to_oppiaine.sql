ALTER TABLE oppiaine ADD COLUMN kieli_koodi_uri VARCHAR(255);
ALTER TABLE oppiaine_aud ADD COLUMN kieli_koodi_uri VARCHAR(255);
ALTER TABLE oppiaine ADD COLUMN kieli_koodi_arvo VARCHAR(255);
ALTER TABLE oppiaine_aud ADD COLUMN kieli_koodi_arvo VARCHAR(255);
ALTER TABLE oppiaine ADD COLUMN kieli_id BIGINT REFERENCES lokalisoituteksti(id);
ALTER TABLE oppiaine_aud ADD COLUMN kieli_id BIGINT;
