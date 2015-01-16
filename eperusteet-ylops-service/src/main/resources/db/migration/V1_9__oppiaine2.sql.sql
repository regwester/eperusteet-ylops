create table ops_oppiaine (
    id int8 not null,
    oma boolean,
    opetussuunnitelma_id int8 not null,
    oppiaine_id int8 not null,
    primary key (id)
);

create table ops_oppiaine_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    oppiaine_id int8,
    primary key (id, REV)
);

create table ops_vuosiluokkakokonaisuus (
    id int8 not null,
    oma boolean,
    opetussuunnitelma_id int8 not null,
    vuosiluokkakokonaisuus_id int8 not null,
    primary key (id)
);

create table ops_vuosiluokkakokonaisuus_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    vuosiluokkakokonaisuus_id int8,
    primary key (id, REV)
);

alter table oppiaine
    add column tila varchar(255);

alter table oppiaine_AUD
    add column tila varchar(255);

update oppiaine set tila = 'LUONNOS';

alter table oppiaine
    alter column tila set not null;

alter table vlkokonaisuus
    add column tila varchar(255);

alter table vlkokonaisuus_AUD
    add column tila varchar(255);

update vlkokonaisuus set tila = 'LUONNOS';

alter table vlkokonaisuus
    alter column tila set not null;

alter table ops_oppiaine
    add constraint FK_ehi17d04axkhmo5ifhyxl362x
    foreign key (opetussuunnitelma_id)
    references opetussuunnitelma;

alter table ops_oppiaine
    add constraint FK_45992gg7yaveqgj5o2k2nksls
    foreign key (oppiaine_id)
    references oppiaine;

alter table ops_oppiaine_AUD
    add constraint FK_h35asifnxxixwwnr4b207cdlq
    foreign key (REV)
    references revinfo;

alter table ops_oppiaine_AUD
    add constraint FK_jmbewdjr4tvn7044v3b585b0c
    foreign key (REVEND)
    references revinfo;

alter table ops_vuosiluokkakokonaisuus
    add constraint FK_dv0m6lfhngbiaoad85wmnjtwy
    foreign key (opetussuunnitelma_id)
    references opetussuunnitelma;

alter table ops_vuosiluokkakokonaisuus
    add constraint FK_5kxym5rbpey8j5l9x7o00n8fg
    foreign key (vuosiluokkakokonaisuus_id)
    references vlkokonaisuus;

alter table ops_vuosiluokkakokonaisuus_AUD
    add constraint FK_64rseg7mu71cm9y8xnsxgjpgo
    foreign key (REV)
    references revinfo;

alter table ops_vuosiluokkakokonaisuus_AUD
    add constraint FK_l28od75ufy46bpyppf3fcipb8
    foreign key (REVEND)
    references revinfo;

