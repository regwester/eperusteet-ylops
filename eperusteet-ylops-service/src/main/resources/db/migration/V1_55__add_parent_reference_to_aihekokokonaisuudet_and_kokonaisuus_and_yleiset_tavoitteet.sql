ALTER TABLE aihekokonaisuudet ADD COLUMN parent_id INT8 REFERENCES aihekokonaisuudet(id);
ALTER TABLE aihekokonaisuudet_aud ADD COLUMN parent_id INT8;
ALTER TABLE aihekokonaisuus ADD COLUMN parent_id INT8 REFERENCES aihekokonaisuus(id);
ALTER TABLE aihekokonaisuus_aud ADD COLUMN parent_id INT8;

ALTER TABLE lukiokoulutuksen_opetuksen_yleiset_tavoitteet ADD COLUMN parent_id INT8 REFERENCES lukiokoulutuksen_opetuksen_yleiset_tavoitteet(id);
ALTER TABLE lukiokoulutuksen_opetuksen_yleiset_tavoitteet_aud ADD COLUMN parent_id INT8;
