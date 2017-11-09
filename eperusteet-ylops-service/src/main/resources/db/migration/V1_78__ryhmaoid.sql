ALTER TABLE opetussuunnitelma ADD COLUMN ryhmaoid VARCHAR(255);
ALTER TABLE opetussuunnitelma_aud ADD COLUMN ryhmaoid VARCHAR(255);

ALTER TABLE opetussuunnitelma ADD COLUMN ryhman_nimi VARCHAR(255);
ALTER TABLE opetussuunnitelma_aud ADD COLUMN ryhman_nimi VARCHAR(255);