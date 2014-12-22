create table opetussuunnitelma (
  id int8 not null,
  kuvaus_id int8,
  nimi_id int8,
  tekstit_id int8,
  primary key (id)
);

create table opetussuunnitelma_AUD (
  id int8 not null,
  REV int4 not null,
  REVTYPE int2,
  REVEND int4,
  kuvaus_id int8,
  nimi_id int8,
  tekstit_id int8,
  primary key (id, REV)
);

alter table opetussuunnitelma
add constraint FK_opetussuunnitelma_lokalisoituteksti_kuvaus
foreign key (kuvaus_id)
references lokalisoituteksti;

alter table opetussuunnitelma
add constraint FK_opetussuunnitelma_lokalisoituteksti_nimi
foreign key (nimi_id)
references lokalisoituteksti;

alter table opetussuunnitelma
add constraint FK_opetussuunnitelma_tekstikappaleviite
foreign key (tekstit_id)
references tekstikappaleviite;

alter table opetussuunnitelma_AUD
add constraint FK_opetussuunnitelma_aud_revinfo_rev
foreign key (REV)
references revinfo;

alter table opetussuunnitelma_AUD
add constraint FK_opetussuunnitelma_aud_revinfo_revend
foreign key (REVEND)
references revinfo;
