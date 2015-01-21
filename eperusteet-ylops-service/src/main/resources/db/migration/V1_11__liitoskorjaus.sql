
drop table ops_oppiaine;
drop table ops_oppiaine_AUD;
drop table ops_vuosiluokkakokonaisuus;
drop table ops_vuosiluokkakokonaisuus_AUD;

create table ops_oppiaine (
    opetussuunnitelma_id int8 not null,
    oma boolean not null,
    oppiaine_id int8 not null,
    primary key (opetussuunnitelma_id, oppiaine_id)
);

create table ops_oppiaine_AUD (
    REV int4 not null,
    REVTYPE int2 not null,
    opetussuunnitelma_id int8 not null,
    SETORDINAL int4 not null,
    REVEND int4,
    oma boolean,
    oppiaine_id int8,
    primary key (REV, REVTYPE, opetussuunnitelma_id, SETORDINAL)
);

create table ops_vuosiluokkakokonaisuus (
    opetussuunnitelma_id int8 not null,
    oma boolean not null,
    vuosiluokkakokonaisuus_id int8 not null,
    primary key (opetussuunnitelma_id, vuosiluokkakokonaisuus_id)
);

create table ops_vuosiluokkakokonaisuus_AUD (
    REV int4 not null,
    REVTYPE int2 not null,
    opetussuunnitelma_id int8 not null,
    SETORDINAL int4 not null,
    REVEND int4,
    oma boolean,
    vuosiluokkakokonaisuus_id int8,
    primary key (REV, REVTYPE, opetussuunnitelma_id, SETORDINAL)
);

 alter table ops_oppiaine
    add constraint FK_ald3cfo3e7166xxv7bwkfmhxl
    foreign key (oppiaine_id)
    references oppiaine;

alter table ops_oppiaine
    add constraint FK_4s776neaoe1wmp9uprmw1pdyf
    foreign key (opetussuunnitelma_id)
    references opetussuunnitelma;

alter table ops_oppiaine_AUD
    add constraint FK_ga6v9b0arigy3wsylhqt5xein
    foreign key (REV)
    references revinfo;

alter table ops_vuosiluokkakokonaisuus
    add constraint FK_5kxym5rbpey8j5l9x7o00n8fg
    foreign key (vuosiluokkakokonaisuus_id)
    references vlkokonaisuus;

alter table ops_vuosiluokkakokonaisuus
    add constraint FK_dv0m6lfhngbiaoad85wmnjtwy
    foreign key (opetussuunnitelma_id)
    references opetussuunnitelma;

alter table ops_vuosiluokkakokonaisuus_AUD
    add constraint FK_64rseg7mu71cm9y8xnsxgjpgo
    foreign key (REV)
    references revinfo;


