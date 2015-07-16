CREATE TABLE termi(
    id int8 NOT NULL,
    ops_id int8 NOT NULL REFERENCES opetussuunnitelma(id),
    avain TEXT NOT NULL,
    termi_id int8 REFERENCES lokalisoituteksti(id),
    selitys_id int8 REFERENCES lokalisoituteksti(id),
    PRIMARY KEY(id)
);
    
CREATE TABLE termi_aud(
    id int8,
    avain TEXT,
    ops_id int8 REFERENCES opetussuunnitelma(id),
    termi_id int8 REFERENCES lokalisoituteksti(id),
    selitys_id int8 REFERENCES lokalisoituteksti(id),
    REV int4,
    REVTYPE int2,
    REVEND int4
);
    
