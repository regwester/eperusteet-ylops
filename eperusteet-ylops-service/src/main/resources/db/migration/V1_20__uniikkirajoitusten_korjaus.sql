alter table oppiaineenvuosiluokka_keskeinen_sisaltoalue
    drop constraint UK_snqw1k9xf9twxwohwexi4f3c6;

alter table oppiaineenvuosiluokka_opetuksen_tavoite
    drop constraint UK_1v99vt41hguwgrhg56nl3ydn9;

alter table oppiaineenvuosiluokka_keskeinen_sisaltoalue
    add constraint UK_snqw1k9xf9twxwohwexi4f3c6  unique (sisaltoalueet_id) deferrable initially deferred;

alter table oppiaineenvuosiluokka_opetuksen_tavoite
    add constraint UK_1v99vt41hguwgrhg56nl3ydn9  unique (tavoitteet_id) deferrable initially deferred;
