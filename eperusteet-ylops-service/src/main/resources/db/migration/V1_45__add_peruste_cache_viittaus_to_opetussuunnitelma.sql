
ALTER TABLE opetussuunnitelma ADD COLUMN cached_peruste INT8 REFERENCES peruste_cache(id);
ALTER TABLE opetussuunnitelma_aud ADD COLUMN cached_peruste INT8;
