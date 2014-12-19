create table lokalisoituteksti (
    id int8 not null,
    primary key (id)
);

create table lokalisoituteksti_teksti (
    lokalisoituteksti_id int8 not null,
    kieli varchar(255),
    teksti TEXT
);

alter table lokalisoituteksti_teksti
    add constraint FK_lokalisoituteksti_id
    foreign key (lokalisoituteksti_id)
    references lokalisoituteksti;
