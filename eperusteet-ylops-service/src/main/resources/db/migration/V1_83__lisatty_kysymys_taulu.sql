create table Kysymys (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kysymys_id int8,
    vastaus_id int8,
    primary key (id)
);

create table Kysymys_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kysymys_id int8,
    vastaus_id int8,
    primary key (id, REV)
);

create table Kysymys_organisaatiot (
    Kysymys_id int8 not null,
    organisaatiot varchar(255)
);