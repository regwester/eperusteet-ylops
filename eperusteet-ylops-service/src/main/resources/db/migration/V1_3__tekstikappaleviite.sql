create table tekstikappaleviite (
  id int8 not null,
  omistussuhde varchar(255) not null,
  tekstiKappale_id int8,
  vanhempi_id int8,
  lapset_ORDER int4,
  primary key (id)
);

create table tekstikappaleviite_AUD (
  id int8 not null,
  REV int4 not null,
  REVTYPE int2,
  REVEND int4,
  omistussuhde varchar(255),
  tekstiKappale_id int8,
  vanhempi_id int8,
  primary key (id, REV)
);

alter table tekstikappaleviite
add constraint FK_tekstikappaleviite_tekstikappale
foreign key (tekstiKappale_id)
references tekstikappale;

alter table tekstikappaleviite
add constraint FK_tekstikappaleviite_vanhempi
foreign key (vanhempi_id)
references tekstikappaleviite;

alter table tekstikappaleviite_AUD
add constraint FK_tekstikappaleviite_aud_revinfo_rev
foreign key (REV)
references revinfo;

alter table tekstikappaleviite_AUD
add constraint FK_tekstikappaleviite_aud_revinfo_revend
foreign key (REVEND)
references revinfo;
