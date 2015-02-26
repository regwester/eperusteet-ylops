create table lukko (
     id int8 not null,
     haltija_oid varchar(255),
     luotu timestamp,
     vanhenemisAika int4 not null,
     primary key (id)
 );
