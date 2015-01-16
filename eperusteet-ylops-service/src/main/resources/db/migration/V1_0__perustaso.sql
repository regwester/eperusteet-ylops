create table revinfo (
  rev int4 not null,
  revtstmp int8,
  kommentti varchar(1000),
  muokkaajaOid varchar(255),
  primary key (rev)
);

create sequence hibernate_sequence;

