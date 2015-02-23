create table kommentti (
  id int8 not null,
  luoja varchar(255),
  luotu timestamp,
  muokattu timestamp,
  muokkaaja varchar(255),
  nimi varchar(255),
  opetussuunnitelmaId int8,
  parentId int8,
  poistettu boolean,
  sisalto varchar(255),
  tekstiKappaleViiteId int8,
  ylinId int8,
  primary key (id)
);
