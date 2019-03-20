drop table if exists lops2019_opintojakso cascade;
drop table if exists lops2019_opintojakso_aud cascade;
drop table if exists lops2019_opintojakso_moduuli cascade;
drop table if exists lops2019_opintojakso_moduuli_aud cascade;
drop table if exists lops2019_opintojakson_moduuli cascade;
drop table if exists lops2019_opintojakson_moduuli_aud cascade;
drop table if exists lops2019_sisalto cascade;
drop table if exists lops2019_sisalto_aud cascade;
drop table if exists lops2019_sisalto_opintojakso cascade;
drop table if exists lops2019_sisalto_opintojakso_aud cascade;

create table lops2019_opintojakso (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    koodi varchar(255),
    kuvaus_id int8,
    nimi_id int8,
    sisalto_id int8,
    primary key (id)
);

create table lops2019_opintojakso_aud (
    id int8 not null,
    rev int4 not null,
    revtype int2,
    revend int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    koodi varchar(255),
    kuvaus_id int8,
    nimi_id int8,
    sisalto_id int8,
    primary key (id, rev)
);

create table lops2019_opintojakso_moduuli (
    opintojakso_id int8,
    moduuli_id int8 not null,
    primary key (moduuli_id)
);

create table lops2019_opintojakso_moduuli_aud (
    moduuli_id int8 not null,
    rev int4 not null,
    opintojakso_id int8,
    primary key (moduuli_id, rev)
);

create table lops2019_opintojakson_moduuli (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    koodiuri varchar(255) not null,
    kuvaus_id int8,
    primary key (id)
);

create table lops2019_opintojakson_moduuli_aud (
    id int8 not null,
    rev int4 not null,
    revtype int2,
    revend int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    koodiuri varchar(255),
    kuvaus_id int8,
    primary key (id, rev)
);

create table lops2019_sisalto (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    opetussuunnitelma_id int8 not null,
    primary key (id)
);

create table lops2019_sisalto_aud (
    id int8 not null,
    rev int4 not null,
    revtype int2,
    revend int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    opetussuunnitelma_id int8,
    primary key (id, rev)
);

create table lops2019_sisalto_opintojakso (
    sisalto_id int8 not null,
    opintojakso_id int8 not null,
    primary key (sisalto_id, opintojakso_id)
);

create table lops2019_sisalto_opintojakso_aud (
    rev int4 not null,
    sisalto_id int8 not null,
    opintojakso_id int8 not null,
    revtype int2,
    revend int4,
    primary key (rev, sisalto_id, opintojakso_id)
);