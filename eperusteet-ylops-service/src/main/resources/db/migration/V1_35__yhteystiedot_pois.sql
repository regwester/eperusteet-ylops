alter table opetussuunnitelma
    drop constraint FK_asdbxdtxyqi82kl5spp17qfbd;

alter table opetussuunnitelma
    drop column if exists yhteystiedot_id;

alter table opetussuunnitelma_AUD
    drop column if exists yhteystiedot_id;
