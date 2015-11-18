ALTER TABLE opetussuunnitelma ADD COLUMN esikatseltavissa boolean;
UPDATE opetussuunnitelma set esikatseltavissa=false;

ALTER TABLE opetussuunnitelma_aud ADD COLUMN esikatseltavissa boolean;
UPDATE opetussuunnitelma_aud set esikatseltavissa=false;