alter table oppiaine
add column laajuus int4,
add column tyyppi varchar(255),
add column liittyvaOppiaine_id int8;

alter table oppiaine_aud
add column laajuus int4,
add column tyyppi varchar(255),
add column liittyvaOppiaine_id int8;

update oppiaine set tyyppi='YHTEINEN';

alter table oppiaine
alter column tyyppi set not null;

alter table oppiaine
add constraint FK_8oc74t5cl9bji44w598r0gyhk
foreign key (liittyvaOppiaine_id)
references oppiaine;
