create table ohje (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kohde UUID,
    teksti_id int8,
    primary key (id)
);

create table ohje_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    kohde UUID,
    teksti_id int8,
    primary key (id, REV)
);

alter table tekstikappale
    add column tunniste UUID;

alter table tekstikappale_AUD
    add column tunniste UUID;

create index UK_4lodr57s3wsfsqxxkut6opxrh on ohje (kohde);

 alter table ohje
    add constraint FK_hw19w1na8qehidjetsw9wobrx
    foreign key (teksti_id)
    references lokalisoituteksti;

alter table ohje_AUD
    add constraint FK_7yv0l2iob8mugl35rure6fos1
    foreign key (REV)
    references revinfo;

alter table ohje_AUD
    add constraint FK_f48ffqqfl2low007owdugtksf
    foreign key (REVEND)
    references revinfo;
