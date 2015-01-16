
alter table opetussuunnitelma
add column luoja varchar(255),
add column luotu timestamp,
add column muokattu timestamp,
add column muokkaaja varchar(255),
add column tila varchar(255);

alter table opetussuunnitelma
alter column tila set not null;

alter table opetussuunnitelma_aud
add column luoja varchar(255),
add column luotu timestamp,
add column muokattu timestamp,
add column muokkaaja varchar(255),
add column tila varchar(255);
