drop table if exists kommentti_2019;
drop table if exists kommentti_2019_AUD;

create extension if not exists "uuid-ossp";

create table kommentti_2019 (
    tunniste uuid not null,
    kommentti varchar(255),
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    opsId int8,
    parent uuid,
    sisalto varchar(1024),
    primary key (tunniste)
);

create table kommentti_2019_AUD (
    tunniste uuid not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    kommentti varchar(255),
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    opsId int8,
    parent uuid,
    sisalto varchar(1024),
    primary key (tunniste, REV)
);

alter table kommentti_2019_AUD 
    add constraint FK_gpdhh625ji2rl74tmb9ckri0i 
    foreign key (REV) 
    references revinfo;

alter table kommentti_2019_AUD 
    add constraint FK_5sd35b90e419em96h5dgwafjy 
    foreign key (REVEND) 
    references revinfo;
