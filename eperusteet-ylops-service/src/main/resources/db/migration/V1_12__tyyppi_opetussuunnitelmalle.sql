alter table opetussuunnitelma
    add column tyyppi varchar(255);

update opetussuunnitelma
    set tyyppi = 'OPS';

alter table opetussuunnitelma
    alter column tyyppi set not null;

alter table opetussuunnitelma_aud
    add column tyyppi varchar(255);
