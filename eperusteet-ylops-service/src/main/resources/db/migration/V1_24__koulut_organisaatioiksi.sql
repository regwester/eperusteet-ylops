alter table opetussuunnitelma_koulut
rename column koulut to organisaatiot;

alter table opetussuunnitelma_koulut
rename to opetussuunnitelma_organisaatiot;

alter table opetussuunnitelma_koulut_aud
rename column koulut to organisaatiot;

alter table opetussuunnitelma_koulut_aud
rename to opetussuunnitelma_organisaatiot_aud;
