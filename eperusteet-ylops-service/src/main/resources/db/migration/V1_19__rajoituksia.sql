alter table oppiaineenvuosiluokka
    add constraint UK_olocgmwmni18yiwjxba5ifn9a  unique (kokonaisuus_id, vuosiluokka);

alter table oppiaineen_vlkok
    add constraint UK_i4les4nhl5uotjvn6jgwjj06w  unique (oppiaine_id, vuosiluokkakokonaisuus_id);
