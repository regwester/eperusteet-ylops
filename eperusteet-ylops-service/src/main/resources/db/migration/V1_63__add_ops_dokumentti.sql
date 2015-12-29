create table dokumentti (
    id              bigint not null primary key,
    ops_id          bigint,
    kieli           character varying not null,
    luoja           character varying not null,
    aloitusaika     timestamp without time zone not null,
    valmistumisaika timestamp without time zone,
    tila            character varying not null,
    dokumenttidata  oid
);

ALTER TABLE ONLY dokumentti
    ADD CONSTRAINT fk_dokumentti_ops FOREIGN KEY (ops_id) REFERENCES opetussuunnitelma(id);
