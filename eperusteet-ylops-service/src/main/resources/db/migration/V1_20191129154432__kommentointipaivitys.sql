drop table if exists kommentti_2019;
drop table if exists lokalisoituteksti_kommentti_kahva_2019;
drop table if exists kommentti_kahva_2019;
drop table if exists kommentti_2019_AUD;


create table kommentti_2019 (
    id int8 not null,
    tunniste uuid not null,
    luoja varchar(255),
    muokkaaja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    reply uuid,
    sisalto varchar(1024),
    thread uuid not null,
    primary key (tunniste)
);

create table kommentti_2019_AUD (
    id int8 not null,
    tunniste uuid,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    muokkaaja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    reply uuid,
    sisalto varchar(1024),
    thread uuid,
    primary key (id, REV)
);

create table kommentti_kahva_2019 (
    id int8 not null,
    kieli varchar(255) not null,
    opsId int8 not null,
    start int4 not null,
    stop int4 not null,
    thread uuid not null,
    teksti_id int8 not null,
    primary key (id)
);

create table lokalisoituteksti_kommentti_kahva_2019 (
    lokalisoituteksti_id int8 not null,
    ketjut_id int8 not null,
    primary key (lokalisoituteksti_id, ketjut_id)
);

alter table kommentti_2019_AUD
    add constraint FK_gpdhh625ji2rl74tmb9ckri0i
    foreign key (REV)
    references revinfo;

alter table kommentti_2019_AUD
    add constraint FK_5sd35b90e419em96h5dgwafjy
    foreign key (REVEND)
    references revinfo;

alter table kommentti_kahva_2019
    add constraint FK_7jbe3gohxuv445l43srswqmoh
    foreign key (teksti_id)
    references lokalisoituteksti;

alter table lokalisoituteksti_kommentti_kahva_2019
    add constraint FK_r4801g1vx2c1xk0a9hlp1a4ta
    foreign key (ketjut_id)
    references kommentti_kahva_2019;

alter table lokalisoituteksti_kommentti_kahva_2019
    add constraint FK_t0bqelwdbo0y584mjqk0fq5ys
    foreign key (lokalisoituteksti_id)
    references lokalisoituteksti;
