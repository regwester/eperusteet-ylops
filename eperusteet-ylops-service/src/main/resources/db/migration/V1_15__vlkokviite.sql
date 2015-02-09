UPDATE vlkokonaisuus set tunniste_id = null;
UPDATE oppiaineen_vlkok set vuosiluokkakokonaisuus_id = null;
UPDATE vlkokonaisuus_AUD set tunniste_id = null;
UPDATE oppiaineen_vlkok_AUD set vuosiluokkakokonaisuus_id = null;

DROP table vlkok_vuosiluokat;
DROP table vlkok_viite CASCADE;

ALTER table vlkokonaisuus
    alter column tunniste_id type uuid using null;

ALTER table oppiaineen_vlkok
    alter column vuosiluokkakokonaisuus_id type uuid using null;

ALTER table vlkokonaisuus_AUD
    alter column tunniste_id type uuid using null;

ALTER table oppiaineen_vlkok_AUD
    alter column vuosiluokkakokonaisuus_id type uuid using null;

create table vlkokviite (
    id uuid not null,
    primary key (id)
);

create table vlkokviite_vuosiluokat (
    vlkokviite_id uuid not null,
    vuosiluokka varchar(255)
);

alter table vlkokonaisuus
     add constraint FK_76i2qwjdimrv87wft1iruo585
     foreign key (tunniste_id)
     references vlkokviite;

alter table vlkokviite_vuosiluokat
     add constraint FK_o56lqfp4o2lyosj2f25wwqv7n
     foreign key (vlkokviite_id)
     references vlkokviite;

alter table oppiaineen_vlkok
    add constraint FK_jyqhvmpupr4wqn15fshedh154
    foreign key (vuosiluokkakokonaisuus_id)
    references vlkokviite;
