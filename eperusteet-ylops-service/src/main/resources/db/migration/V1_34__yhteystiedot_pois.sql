alter table opetussuunnitelma
        drop constraint FK_asdbxdtxyqi82kl5spp17qfbd;

alter table opetussuunnitelma
drop column if exists yhteystiedot_id cascade;

alter table opetussuunnitelma_AUD
drop column if exists yhteystiedot_id cascade;

alter table opetussuunnitelma
add column hyvaksymis_pvm timestamp;

alter table opetussuunnitelma_AUD
add column hyvaksymis_pvm timestamp;