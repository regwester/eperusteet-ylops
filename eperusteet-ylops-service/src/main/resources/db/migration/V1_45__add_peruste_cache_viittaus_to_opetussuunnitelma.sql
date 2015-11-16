
ALTER TABLE opetussuunnitelma ADD COLUMN cached_peruste REFERENCES peruste_cache(id);
ALTER TABLE opetussuunnitelma_aud ADD COLUMN cached_peruste REFERENCES peruste_cache(id);
