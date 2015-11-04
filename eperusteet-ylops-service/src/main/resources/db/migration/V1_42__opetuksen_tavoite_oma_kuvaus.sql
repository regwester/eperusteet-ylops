alter table opetuksen_tavoite_keskeinen_sisaltoalue add column id bigserial not null;
alter table opetuksen_tavoite_keskeinen_sisaltoalue_aud add column id bigint;

alter table opetuksen_tavoite_keskeinen_sisaltoalue add column omakuvaus_id int8;
alter table opetuksen_tavoite_keskeinen_sisaltoalue_aud add column omakuvaus_id int8;

