alter table opetussuunnitelma
add column yhteystiedot_id int8;

alter table opetussuunnitelma_AUD
add column yhteystiedot_id int8;

alter table opetussuunnitelma
add constraint FK_asdbxdtxyqi82kl5spp17qfbd
foreign key (yhteystiedot_id)
references lokalisoituteksti;
