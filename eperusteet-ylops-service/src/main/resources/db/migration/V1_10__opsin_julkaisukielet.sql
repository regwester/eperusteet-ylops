create table Opetussuunnitelma_julkaisukielet (
  Opetussuunnitelma_id int8 not null,
  julkaisukielet varchar(255)
);

create table Opetussuunnitelma_julkaisukielet_AUD (
  REV int4 not null,
  Opetussuunnitelma_id int8 not null,
  julkaisukielet varchar(255) not null,
  REVTYPE int2,
  REVEND int4,
  primary key (REV, Opetussuunnitelma_id, julkaisukielet)
);

alter table Opetussuunnitelma_julkaisukielet
add constraint FK_il7kmuh52hmlr3d2r04buywd0
foreign key (Opetussuunnitelma_id)
references opetussuunnitelma;

alter table Opetussuunnitelma_julkaisukielet_AUD
add constraint FK_9mh1nop9pvgrsrxy0ko6a7hjr
foreign key (REV)
references revinfo;

alter table Opetussuunnitelma_julkaisukielet_AUD
add constraint FK_8oegas5p3n5f6msqhmlgjxj8p
foreign key (REVEND)
references revinfo;
