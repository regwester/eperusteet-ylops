create table liite (
    id uuid not null,
    data oid not null,
    luotu timestamp,
    nimi varchar(1024),
    tyyppi varchar(255) not null,
    primary key (id)
);

create table opetussuunnitelma_liite (
    opetussuunnitelma_id int8 not null,
    liite_id uuid not null,
    primary key (opetussuunnitelma_id, liite_id)
);

create table opetussuunnitelma_liite_AUD (
    REV int4 not null,
    opetussuunnitelma_id int8 not null,
    liite_id uuid not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, opetussuunnitelma_id, liite_id)
);

alter table opetussuunnitelma_liite
    add constraint FK_n5yqt8i556xum7c63wyd8l7se
    foreign key (liite_id)
    references liite;

alter table opetussuunnitelma_liite
    add constraint FK_k9qfrela3cfnnettr4aryvs0f
    foreign key (opetussuunnitelma_id)
    references opetussuunnitelma;

alter table opetussuunnitelma_liite_AUD
    add constraint FK_7ki7ulrar35ev4rpechrfpodx
    foreign key (REV)
    references revinfo;

alter table opetussuunnitelma_liite_AUD
    add constraint FK_1cf2y5my1vxf9gylil6vbtwjl
    foreign key (REVEND)
    references revinfo;
