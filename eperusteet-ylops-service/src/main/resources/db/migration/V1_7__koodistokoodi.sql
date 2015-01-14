create table koodistokoodi (
  id int8 not null,
  koodiArvo varchar(255),
  koodiUri varchar(255),
  primary key (id)
);

create table opetussuunnitelma_koodistokoodi (
  opetussuunnitelma_id int8 not null,
  kunnat_id int8 not null,
  primary key (opetussuunnitelma_id, kunnat_id)
);

create table opetussuunnitelma_koodistokoodi_AUD (
  REV int4 not null,
  opetussuunnitelma_id int8 not null,
  kunnat_id int8 not null,
  REVTYPE int2,
  REVEND int4,
  primary key (REV, opetussuunnitelma_id, kunnat_id)
);

create table Opetussuunnitelma_koulut (
  Opetussuunnitelma_id int8 not null,
  koulut varchar(255)
);

create table Opetussuunnitelma_koulut_AUD (
  REV int4 not null,
  Opetussuunnitelma_id int8 not null,
  koulut varchar(255) not null,
  REVTYPE int2,
  REVEND int4,
  primary key (REV, Opetussuunnitelma_id, koulut)
);

alter table opetussuunnitelma_koodistokoodi
add constraint FK_dsfpjvbl9wo0c0sjnp1bec4t3
foreign key (kunnat_id)
references koodistokoodi;

alter table opetussuunnitelma_koodistokoodi
add constraint FK_kb775ur3ap0e6vw4bvch0thyh
foreign key (opetussuunnitelma_id)
references opetussuunnitelma;

alter table opetussuunnitelma_koodistokoodi_AUD
add constraint FK_33wo3fu3s0jl5allvpsqafk0x
foreign key (REV)
references revinfo;

alter table opetussuunnitelma_koodistokoodi_AUD
add constraint FK_2xtr6786qv934w1j1iarttq49
foreign key (REVEND)
references revinfo;

alter table Opetussuunnitelma_koulut
add constraint FK_prljq4o9chadb5p4fb4kaoer0
foreign key (Opetussuunnitelma_id)
references opetussuunnitelma;

alter table Opetussuunnitelma_koulut_AUD
add constraint FK_ppo45oanktb47ds837291hw1h
foreign key (REV)
references revinfo;

alter table Opetussuunnitelma_koulut_AUD
add constraint FK_6lcqv3d2kx97yv1m1otfkjd73
foreign key (REVEND)
references revinfo;
