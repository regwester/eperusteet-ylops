CREATE TABLE poistettu_tekstikappale (
  id                   BIGINT NOT NULL,
  tekstikappale_id     BIGINT NOT NULL,
  opetussuunnitelma_id BIGINT NOT NULL,
  palautettu           BOOLEAN DEFAULT FALSE,
  luoja varchar(255),
  luotu timestamp,
  muokattu timestamp,
  muokkaaja varchar(255),
  PRIMARY KEY (id)
);

CREATE TABLE poistettu_tekstikappale_aud(
  id                   BIGINT NOT NULL,
  tekstikappale_id     BIGINT,
  opetussuunnitelma_id BIGINT,
  palautettu           BOOLEAN,
  luoja varchar(255),
  luotu timestamp,
  muokattu timestamp,
  muokkaaja varchar(255),
  rev                  INT4   NOT NULL,
  revtype              INT2,
  revend               INT4,
  PRIMARY KEY (id, rev)
);



create or replace function findOpsForTekstikappaleAud(parm1 bigint)
  returns bigint
as
$body$
select id from opetussuunnitelma_aud where tekstit_id =
 (
  with recursive vanhemmat(id,vanhempi_id,tekstikappale_id) as
  (select tv.id, tv.vanhempi_id, tv.tekstikappale_id from tekstikappaleviite_aud tv
    where tv.tekstikappale_id = $1
   union all
   select tv.id, tv.vanhempi_id, v.tekstikappale_id
   from tekstikappaleviite_aud tv, vanhemmat v where tv.id = v.vanhempi_id)
  select id from vanhemmat where vanhempi_id is null
   limit 1
) LIMIT 1;
$body$
language sql;




insert into poistettu_tekstikappale(id, tekstikappale_id, opetussuunnitelma_id, palautettu, luoja, luotu, muokattu, muokkaaja)
select ROW_NUMBER() over() as row, id, findOpsForTekstikappaleAud(id) as ops, FALSE, muokkaaja, muokattu, muokattu, muokkaaja
FROM (
       SELECT ROW_NUMBER() OVER (PARTITION BY id ORDER BY ID ASC) AS rn, *
       FROM tekstikappale_aud
       WHERE revtype = 1 AND id IN (
         SELECT id
         FROM tekstikappale_aud
         WHERE revtype = 2
       ) GROUP BY id, rev
     ) a
where rn =1;

