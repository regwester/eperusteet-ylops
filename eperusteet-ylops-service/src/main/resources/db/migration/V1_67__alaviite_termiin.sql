ALTER TABLE termi ADD COLUMN alaviite boolean;
UPDATE termi set alaviite=false;

ALTER TABLE termi_aud ADD COLUMN alaviite boolean;
UPDATE termi_aud set alaviite=false;