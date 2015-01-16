create table tekstikappale (
  id int8 not null,
  luoja varchar(255),
  luotu timestamp,
  muokattu timestamp,
  muokkaaja varchar(255),
  tila varchar(255) not null,
  nimi_id int8,
  teksti_id int8,
  primary key (id)
);

create table tekstikappale_AUD (
  id int8 not null,
  REV int4 not null,
  REVTYPE int2,
  REVEND int4,
  luoja varchar(255),
  luotu timestamp,
  muokattu timestamp,
  muokkaaja varchar(255),
  tila varchar(255),
  nimi_id int8,
  teksti_id int8,
  primary key (id, REV)
);

alter table tekstikappale
add constraint FK_tekstikappale_lokalisoituteksti_nimi
foreign key (nimi_id)
references lokalisoituteksti;

alter table tekstikappale
add constraint FK_tekstikappale_lokalisoituteksti_teksti
foreign key (teksti_id)
references lokalisoituteksti;

alter table tekstikappale_AUD
add constraint FK_tekstikappale_aud_revinfo_rev
foreign key (REV)
references revinfo;

alter table tekstikappale_AUD
add constraint FK_tekstikappale_aud_revinfo_revend
foreign key (REVEND)
references revinfo;
