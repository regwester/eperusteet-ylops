create table opetuksentavoite_laajattavoitteet (
    opetuksentavoite_id int8 not null,
    laajaalainenosaaminen_viite varchar(255)
);

create table opetuksentavoite_laajattavoitteet_AUD (
    REV int4 not null,
    REVTYPE int2 not null,
    opetuksentavoite_id int8 not null,
    SETORDINAL int4 not null,
    REVEND int4,
    laajaalainenosaaminen_viite varchar(255),
    primary key (REV, REVTYPE, opetuksentavoite_id, SETORDINAL)
);

create table keskeinen_sisaltoalue (
    id int8 not null,
    tunniste uuid,
    kuvaus_id int8,
    nimi_id int8,
    primary key (id)
);

create table keskeinen_sisaltoalue_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    tunniste uuid,
    kuvaus_id int8,
    nimi_id int8,
    primary key (id, REV)
);

create table opetuksen_tavoite (
    id int8 not null,
    tunniste uuid,
    vuosiluokka varchar(255),
    kuvaus_id int8,
    tavoite_id int8,
    primary key (id)
);

create table opetuksen_tavoite_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    tunniste uuid,
    vuosiluokka varchar(255),
    kuvaus_id int8,
    tavoite_id int8,
    primary key (id, REV)
);

create table opetuksen_tavoite_keskeinen_sisaltoalue (
    opetuksen_tavoite_id int8 not null,
    sisaltoalueet_id int8 not null,
    primary key (opetuksen_tavoite_id, sisaltoalueet_id)
);

create table opetuksen_tavoite_keskeinen_sisaltoalue_AUD (
    REV int4 not null,
    opetuksen_tavoite_id int8 not null,
    sisaltoalueet_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, opetuksen_tavoite_id, sisaltoalueet_id)
);

create table opetuksen_tavoite_oppiaine_kohdealue (
    opetuksen_tavoite_id int8 not null,
    kohdealueet_id int8 not null,
    primary key (opetuksen_tavoite_id, kohdealueet_id)
);

create table opetuksen_tavoite_oppiaine_kohdealue_AUD (
    REV int4 not null,
    opetuksen_tavoite_id int8 not null,
    kohdealueet_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, opetuksen_tavoite_id, kohdealueet_id)
);

create table opetuksen_tavoite_tavoitteen_arviointi (
    opetuksen_tavoite_id int8 not null,
    arvioinninkohteet_id int8 not null,
    primary key (opetuksen_tavoite_id, arvioinninkohteet_id)
);

create table opetuksen_tavoite_tavoitteen_arviointi_AUD (
    REV int4 not null,
    opetuksen_tavoite_id int8 not null,
    arvioinninkohteet_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, opetuksen_tavoite_id, arvioinninkohteet_id)
);

create table oppiaine (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    koodi varchar(255),
    koosteinen boolean not null,
    nimi_id int8,
    oppiaine_id int8,
    tehtava_id int8,
    primary key (id)
);

create table oppiaine_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    koodi varchar(255),
    koosteinen boolean,
    nimi_id int8,
    oppiaine_id int8,
    tehtava_id int8,
    primary key (id, REV)
);

create table oppiaine_kohdealue (
    id int8 not null,
    nimi_id int8,
    primary key (id)
);

create table oppiaine_kohdealue_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    nimi_id int8,
    primary key (id, REV)
);

create table oppiaine_oppiaine_kohdealue (
    oppiaine_id int8 not null,
    kohdealueet_id int8 not null,
    primary key (oppiaine_id, kohdealueet_id)
);

create table oppiaine_oppiaine_kohdealue_AUD (
    REV int4 not null,
    oppiaine_id int8 not null,
    kohdealueet_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, oppiaine_id, kohdealueet_id)
);

create table oppiaineen_vlkok (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    arviointi_id int8,
    ohjaus_id int8,
    oppiaine_id int8 not null,
    tehtava_id int8,
    tyotavat_id int8,
    vuosiluokkakokonaisuus_id int8 not null,
    primary key (id)
);

create table oppiaineen_vlkok_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    arviointi_id int8,
    ohjaus_id int8,
    oppiaine_id int8,
    tehtava_id int8,
    tyotavat_id int8,
    vuosiluokkakokonaisuus_id int8,
    primary key (id, REV)
);

create table oppiaineen_vlkok_oppiaineenvuosiluokka (
    oppiaineen_vlkok_id int8 not null,
    vuosiluokat_id int8 not null,
    primary key (oppiaineen_vlkok_id, vuosiluokat_id)
);

create table oppiaineen_vlkok_oppiaineenvuosiluokka_AUD (
    REV int4 not null,
    oppiaineen_vlkok_id int8 not null,
    vuosiluokat_id int8 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, oppiaineen_vlkok_id, vuosiluokat_id)
);

create table oppiaineenvuosiluokka (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    vuosiluokka varchar(255),
    kokonaisuus_id int8 not null,
    primary key (id)
);

create table oppiaineenvuosiluokka_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    vuosiluokka varchar(255),
    kokonaisuus_id int8,
    primary key (id, REV)
);

create table oppiaineenvuosiluokka_keskeinen_sisaltoalue (
    oppiaineenvuosiluokka_id int8 not null,
    sisaltoalueet_id int8 not null,
    sisaltoalueet_ORDER int4 not null,
    primary key (oppiaineenvuosiluokka_id, sisaltoalueet_ORDER)
);

create table oppiaineenvuosiluokka_keskeinen_sisaltoalue_AUD (
    REV int4 not null,
    oppiaineenvuosiluokka_id int8 not null,
    sisaltoalueet_id int8 not null,
    sisaltoalueet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, oppiaineenvuosiluokka_id, sisaltoalueet_id, sisaltoalueet_ORDER)
);

create table oppiaineenvuosiluokka_opetuksen_tavoite (
    oppiaineenvuosiluokka_id int8 not null,
    tavoitteet_id int8 not null,
    tavoitteet_ORDER int4 not null,
    primary key (oppiaineenvuosiluokka_id, tavoitteet_ORDER)
);

create table oppiaineenvuosiluokka_opetuksen_tavoite_AUD (
    REV int4 not null,
    oppiaineenvuosiluokka_id int8 not null,
    tavoitteet_id int8 not null,
    tavoitteet_ORDER int4 not null,
    REVTYPE int2,
    REVEND int4,
    primary key (REV, oppiaineenvuosiluokka_id, tavoitteet_id, tavoitteet_ORDER)
);


create table tavoitteen_arviointi (
    id int8 not null,
    arvioinninKohde_id int8,
    hyvanOsaamisenKuvaus_id int8,
    primary key (id)
);

create table tavoitteen_arviointi_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    arvioinninKohde_id int8,
    hyvanOsaamisenKuvaus_id int8,
    primary key (id, REV)
);

create table tekstiosa (
    id int8 not null,
    otsikko_id int8,
    teksti_id int8,
    primary key (id)
);

create table tekstiosa_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    otsikko_id int8,
    teksti_id int8,
    primary key (id, REV)
);

create table vlkok_laaja_osaaminen (
    id int8 not null,
    laajaalainenosaaminen_viite varchar(255),
    kuvaus_id int8,
    vuosiluokkakokonaisuus_id int8 not null,
    primary key (id)
);

create table vlkok_laaja_osaaminen_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    laajaalainenosaaminen_viite varchar(255),
    kuvaus_id int8,
    vuosiluokkakokonaisuus_id int8,
    primary key (id, REV)
);

create table vlkok_viite (
    id int8 not null,
    vuosiluokkakokonaisuus_viite varchar(255),
    primary key (id)
);

create table vlkok_vuosiluokat (
    Vuosiluokkakokonaisuusviite_id int8 not null,
    vuosiluokka varchar(255)
);

create table vlkokonaisuus (
    id int8 not null,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    laajaalainenOsaaminen_id int8,
    nimi_id int8,
    siirtymaEdellisesta_id int8,
    siirtymaSeuraavaan_id int8,
    tehtava_id int8,
    tunniste_id int8,
    primary key (id)
);

create table vlkokonaisuus_AUD (
    id int8 not null,
    REV int4 not null,
    REVTYPE int2,
    REVEND int4,
    luoja varchar(255),
    luotu timestamp,
    muokattu timestamp,
    muokkaaja varchar(255),
    laajaalainenOsaaminen_id int8,
    nimi_id int8,
    siirtymaEdellisesta_id int8,
    siirtymaSeuraavaan_id int8,
    tehtava_id int8,
    tunniste_id int8,
    primary key (id, REV)
);

alter table opetuksen_tavoite_tavoitteen_arviointi
    add constraint UK_50a0hf2lywpy0cunsy5sjgbh5  unique (arvioinninkohteet_id);

alter table oppiaine_oppiaine_kohdealue
    add constraint UK_lgwv4djq3te7nmso4kuuthknn  unique (kohdealueet_id);

alter table oppiaineen_vlkok_oppiaineenvuosiluokka
    add constraint UK_rxonkp7n36f5j14f1kxvesysh  unique (vuosiluokat_id);

alter table oppiaineenvuosiluokka_keskeinen_sisaltoalue
    add constraint UK_snqw1k9xf9twxwohwexi4f3c6  unique (sisaltoalueet_id);

alter table oppiaineenvuosiluokka_opetuksen_tavoite
    add constraint UK_1v99vt41hguwgrhg56nl3ydn9  unique (tavoitteet_id);

alter table vlkok_viite
    add constraint UK_77db1yw5yjlk21go45ghbvuip  unique (vuosiluokkakokonaisuus_viite);

alter table Opetuksentavoite_laajattavoitteet
    add constraint FK_gku0wf1dfe9eyqpg1gfhlqsta
    foreign key (Opetuksentavoite_id)
    references opetuksen_tavoite;

alter table Opetuksentavoite_laajattavoitteet_AUD
    add constraint FK_fahee66gayrgkl8mfulyv2dam
    foreign key (REV)
    references revinfo;

alter table Opetuksentavoite_laajattavoitteet_AUD
    add constraint FK_fg76ulaovxrol7pt156ddg8t4
    foreign key (REVEND)
    references revinfo;

alter table keskeinen_sisaltoalue
    add constraint FK_fhv8k9qey5o0tr9m2aauuphm9
    foreign key (kuvaus_id)
    references lokalisoituteksti;

alter table keskeinen_sisaltoalue
    add constraint FK_1bpmwavntd2f292nr881wckdb
    foreign key (nimi_id)
    references lokalisoituteksti;

alter table keskeinen_sisaltoalue_AUD
    add constraint FK_6m521w9cisf0c9bocn1rp4a5w
    foreign key (REV)
    references revinfo;

alter table keskeinen_sisaltoalue_AUD
    add constraint FK_lcxrqceyk1gp7r3w97l2ja7cs
    foreign key (REVEND)
    references revinfo;

alter table opetuksen_tavoite
    add constraint FK_50crarscvfbxkcgd22696xx0s
    foreign key (kuvaus_id)
    references lokalisoituteksti;

alter table opetuksen_tavoite
    add constraint FK_hv0yohbx3p407s11gm602qp7n
    foreign key (tavoite_id)
    references lokalisoituteksti;

alter table opetuksen_tavoite_AUD
    add constraint FK_38yc839b5sm8si125wibpjqem
    foreign key (REV)
    references revinfo;

alter table opetuksen_tavoite_AUD
    add constraint FK_sltj700b5grud2cgtghecuesa
    foreign key (REVEND)
    references revinfo;

alter table opetuksen_tavoite_keskeinen_sisaltoalue
    add constraint FK_ix1mrxbho30e76ah258ijunxy
    foreign key (sisaltoalueet_id)
    references keskeinen_sisaltoalue;

alter table opetuksen_tavoite_keskeinen_sisaltoalue
    add constraint FK_45sqm1c70viyacx0d2gornh0c
    foreign key (opetuksen_tavoite_id)
    references opetuksen_tavoite;

alter table opetuksen_tavoite_keskeinen_sisaltoalue_AUD
    add constraint FK_5985j7x1j1twtj2t16a91vy2v
    foreign key (REV)
    references revinfo;

alter table opetuksen_tavoite_keskeinen_sisaltoalue_AUD
    add constraint FK_s56r18616y31xl1yddedyyos7
    foreign key (REVEND)
    references revinfo;

alter table opetuksen_tavoite_oppiaine_kohdealue
    add constraint FK_2etca25yjmva4ch5agudhci4s
    foreign key (kohdealueet_id)
    references oppiaine_kohdealue;

alter table opetuksen_tavoite_oppiaine_kohdealue
    add constraint FK_qcinsw63tvj2mghb5hi15etj7
    foreign key (opetuksen_tavoite_id)
    references opetuksen_tavoite;

alter table opetuksen_tavoite_oppiaine_kohdealue_AUD
    add constraint FK_jrfspokoep3wf8iyh463qon2t
    foreign key (REV)
    references revinfo;

alter table opetuksen_tavoite_oppiaine_kohdealue_AUD
    add constraint FK_l97jpr61hx2x2ddmmb2wqs30m
    foreign key (REVEND)
    references revinfo;

alter table opetuksen_tavoite_tavoitteen_arviointi
    add constraint FK_50a0hf2lywpy0cunsy5sjgbh5
    foreign key (arvioinninkohteet_id)
    references tavoitteen_arviointi;

alter table opetuksen_tavoite_tavoitteen_arviointi
    add constraint FK_t1e4g2ytgil6a9exi6583j7wd
    foreign key (opetuksen_tavoite_id)
    references opetuksen_tavoite;

alter table opetuksen_tavoite_tavoitteen_arviointi_AUD
    add constraint FK_8wesqqo71v9kq99lsy2pgpi6r
    foreign key (REV)
    references revinfo;

alter table opetuksen_tavoite_tavoitteen_arviointi_AUD
    add constraint FK_dyqkknye3stve5nau2yrxv6ty
    foreign key (REVEND)
    references revinfo;

alter table oppiaine
    add constraint FK_k3v1cav1ao08do04wd5fi40qi
    foreign key (nimi_id)
    references lokalisoituteksti;

alter table oppiaine
    add constraint FK_j82hgaoufo9fbus9maqp9gqyy
    foreign key (oppiaine_id)
    references oppiaine;

alter table oppiaine
    add constraint FK_3q6rq6ncoq14j3w2j3skkeod2
    foreign key (tehtava_id)
    references tekstiosa;

alter table oppiaine_AUD
    add constraint FK_b1i7t1girpmouit2c23rnxeni
    foreign key (REV)
    references revinfo;

alter table oppiaine_AUD
    add constraint FK_qi4ngb2ksf4kbdf4nlcnashc3
    foreign key (REVEND)
    references revinfo;

alter table oppiaine_kohdealue
    add constraint FK_cpqjxhxlwnxucoyjjg7iy04cw
    foreign key (nimi_id)
    references lokalisoituteksti;

alter table oppiaine_kohdealue_AUD
    add constraint FK_8lu4kwp1dehp13o5n3rl3ligd
    foreign key (REV)
    references revinfo;

alter table oppiaine_kohdealue_AUD
    add constraint FK_fhek7ntbagw5xk4hj04jaax3g
    foreign key (REVEND)
    references revinfo;

alter table oppiaine_oppiaine_kohdealue
    add constraint FK_lgwv4djq3te7nmso4kuuthknn
    foreign key (kohdealueet_id)
    references oppiaine_kohdealue;

alter table oppiaine_oppiaine_kohdealue
    add constraint FK_3pros762tj9815bas2pjn7q46
    foreign key (oppiaine_id)
    references oppiaine;

alter table oppiaine_oppiaine_kohdealue_AUD
    add constraint FK_1u3lsj0ryd9bfxt0r6knjfogv
    foreign key (REV)
    references revinfo;

alter table oppiaine_oppiaine_kohdealue_AUD
    add constraint FK_no9mjb6elod9gbxshjyjc52xy
    foreign key (REVEND)
    references revinfo;

alter table oppiaineen_vlkok
    add constraint FK_jt3guq5oxby8h1rmtsb0su6mr
    foreign key (arviointi_id)
    references tekstiosa;

alter table oppiaineen_vlkok
    add constraint FK_r8arrij7y7m75xwxv2vjwpm7g
    foreign key (ohjaus_id)
    references tekstiosa;

alter table oppiaineen_vlkok
    add constraint FK_a844oio5dm15xh9pbkweenpu
    foreign key (oppiaine_id)
    references oppiaine;

alter table oppiaineen_vlkok
    add constraint FK_efcy8h1gjysmupbrus1iv9om2
    foreign key (tehtava_id)
    references tekstiosa;

alter table oppiaineen_vlkok
    add constraint FK_aint8a2o1ya26ecevy34q4i1a
    foreign key (tyotavat_id)
    references tekstiosa;

alter table oppiaineen_vlkok
    add constraint FK_jyqhvmpupr4wqn15fshedh154
    foreign key (vuosiluokkakokonaisuus_id)
    references vlkok_viite;

alter table oppiaineen_vlkok_AUD
    add constraint FK_275q970jhc0i4cb2sfoy750o1
    foreign key (REV)
    references revinfo;

alter table oppiaineen_vlkok_AUD
    add constraint FK_nwpujk7oao84d6bfhcfs17enn
    foreign key (REVEND)
    references revinfo;

alter table oppiaineen_vlkok_oppiaineenvuosiluokka
    add constraint FK_rxonkp7n36f5j14f1kxvesysh
    foreign key (vuosiluokat_id)
    references oppiaineenvuosiluokka;

alter table oppiaineen_vlkok_oppiaineenvuosiluokka
    add constraint FK_2qjsg13ivvev09o2iu7y67abu
    foreign key (oppiaineen_vlkok_id)
    references oppiaineen_vlkok;

alter table oppiaineen_vlkok_oppiaineenvuosiluokka_AUD
    add constraint FK_jfo2m8fcb962sjbvt34lhpf2l
    foreign key (REV)
    references revinfo;

alter table oppiaineen_vlkok_oppiaineenvuosiluokka_AUD
    add constraint FK_3phxvo8j7tks2y14uwsfwcwk2
    foreign key (REVEND)
    references revinfo;

alter table oppiaineenvuosiluokka
    add constraint FK_l2w5ueonohdt87l4bv90ik7t1
    foreign key (kokonaisuus_id)
    references oppiaineen_vlkok;

alter table oppiaineenvuosiluokka_AUD
    add constraint FK_2bwmtivua83of0m3cd384yldq
    foreign key (REV)
    references revinfo;

alter table oppiaineenvuosiluokka_AUD
    add constraint FK_9xdo4uktmg84xm93legk9p30h
    foreign key (REVEND)
    references revinfo;

alter table oppiaineenvuosiluokka_keskeinen_sisaltoalue
    add constraint FK_snqw1k9xf9twxwohwexi4f3c6
    foreign key (sisaltoalueet_id)
    references keskeinen_sisaltoalue;

alter table oppiaineenvuosiluokka_keskeinen_sisaltoalue
    add constraint FK_asog8sog3j18bpxpkbuxd5yk3
    foreign key (oppiaineenvuosiluokka_id)
    references oppiaineenvuosiluokka;

alter table oppiaineenvuosiluokka_keskeinen_sisaltoalue_AUD
    add constraint FK_34e23hs01ej056bitei968947
    foreign key (REV)
    references revinfo;

alter table oppiaineenvuosiluokka_keskeinen_sisaltoalue_AUD
    add constraint FK_qe6oqk4u1obj7j3fcw131yyj6
    foreign key (REVEND)
    references revinfo;

alter table oppiaineenvuosiluokka_opetuksen_tavoite
    add constraint FK_1v99vt41hguwgrhg56nl3ydn9
    foreign key (tavoitteet_id)
    references opetuksen_tavoite;

alter table oppiaineenvuosiluokka_opetuksen_tavoite
    add constraint FK_g6sy4pwucr3bg47y9ht55w3c8
    foreign key (oppiaineenvuosiluokka_id)
    references oppiaineenvuosiluokka;

alter table oppiaineenvuosiluokka_opetuksen_tavoite_AUD
    add constraint FK_jstw6m1jprw4m8a2pknajwmsn
    foreign key (REV)
    references revinfo;

alter table oppiaineenvuosiluokka_opetuksen_tavoite_AUD
    add constraint FK_go1vt1w4qjr6y17orsbvdrudx
    foreign key (REVEND)
    references revinfo;

alter table tavoitteen_arviointi
    add constraint FK_j6lkgq154pfmvcsuwb1k01qci
    foreign key (arvioinninKohde_id)
    references lokalisoituteksti;

alter table tavoitteen_arviointi
    add constraint FK_eeut9iudxka2v8qvsl1p5cqwg
    foreign key (hyvanOsaamisenKuvaus_id)
    references lokalisoituteksti;

alter table tavoitteen_arviointi_AUD
    add constraint FK_iwm5vaje8qltq2hp8d7uf1pvl
    foreign key (REV)
    references revinfo;

alter table tavoitteen_arviointi_AUD
    add constraint FK_t0c5kylryseacsyvhwmk77qx6
    foreign key (REVEND)
    references revinfo;

alter table tekstiosa
    add constraint FK_spsm22lrit456xp9k0yc1m9kj
    foreign key (otsikko_id)
    references lokalisoituteksti;

alter table tekstiosa
    add constraint FK_oa7juph0c39rc1mu1y0l0xdeb
    foreign key (teksti_id)
    references lokalisoituteksti;

alter table tekstiosa_AUD
    add constraint FK_j0crlc392kycsuget13pkggtx
    foreign key (REV)
    references revinfo;

alter table tekstiosa_AUD
    add constraint FK_ok5uqqli0gjnktsx8e7lxy3a7
    foreign key (REVEND)
    references revinfo;

alter table vlkok_laaja_osaaminen
    add constraint FK_3cwpnpl17eknv6pnen4iiqhui
    foreign key (kuvaus_id)
    references lokalisoituteksti;

alter table vlkok_laaja_osaaminen
    add constraint FK_p4lcno9ree01d59hfy4v6l3qj
    foreign key (vuosiluokkakokonaisuus_id)
    references vlkokonaisuus;

alter table vlkok_laaja_osaaminen_AUD
    add constraint FK_bjrf5rt3794uuwknntjtvdh9h
    foreign key (REV)
    references revinfo;

alter table vlkok_laaja_osaaminen_AUD
    add constraint FK_ix1fbfix4dq3fn880qa98m52r
    foreign key (REVEND)
    references revinfo;

alter table vlkok_vuosiluokat
    add constraint FK_y8pao0251hvxjs8obklqp8c3
    foreign key (Vuosiluokkakokonaisuusviite_id)
    references vlkok_viite;

alter table vlkokonaisuus
    add constraint FK_hfcgd4d25uq8c8hp1ji5mhvrx
    foreign key (laajaalainenOsaaminen_id)
    references tekstiosa;

alter table vlkokonaisuus
    add constraint FK_rsyxsu3vkdr15mq87tkq03pt1
    foreign key (nimi_id)
    references lokalisoituteksti;

alter table vlkokonaisuus
    add constraint FK_odo125srbhysbnpir09jb49fl
    foreign key (siirtymaEdellisesta_id)
    references tekstiosa;

alter table vlkokonaisuus
    add constraint FK_fiheyw5us7hvrsa0furj1mo54
    foreign key (siirtymaSeuraavaan_id)
    references tekstiosa;

alter table vlkokonaisuus
    add constraint FK_j61lhbt7dakoeawav4dlkc1sc
    foreign key (tehtava_id)
    references tekstiosa;

alter table vlkokonaisuus
    add constraint FK_76i2qwjdimrv87wft1iruo585
    foreign key (tunniste_id)
    references vlkok_viite;

alter table vlkokonaisuus_AUD
    add constraint FK_pml54okwry9fycsjapbrd4hv1
    foreign key (REV)
    references revinfo;

alter table vlkokonaisuus_AUD
    add constraint FK_gqp70cd38ti7e8d2d0risx7g8
    foreign key (REVEND)
    references revinfo;
