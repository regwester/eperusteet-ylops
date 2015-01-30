alter table tekstikappaleviite
    add column pakollinen boolean;

update tekstikappaleviite
    set pakollinen = 'false';

alter table tekstikappaleviite_aud
    add column pakollinen boolean;
