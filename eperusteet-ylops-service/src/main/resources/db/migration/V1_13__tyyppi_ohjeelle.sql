alter table ohje
    add column tyyppi varchar(255);

update ohje
    set tyyppi = 'perusteteksti';

alter table ohje
    alter column tyyppi set not null;

alter table ohje_aud
    add column tyyppi varchar(255);
