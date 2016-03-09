CREATE TABLE poistettu_oppiaine (
  id                   BIGINT NOT NULL,
  oppiaine_id     BIGINT NOT NULL,
  opetussuunnitelma_id BIGINT NOT NULL,
  palautettu           BOOLEAN DEFAULT FALSE,
  luoja varchar(255),
  luotu timestamp,
  muokattu timestamp,
  muokkaaja varchar(255),
  PRIMARY KEY (id)
);

CREATE TABLE poistettu_oppiaine_aud(
  id                   BIGINT NOT NULL,
  oppiaine_id     BIGINT,
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


-- insert into poistettu_oppiaine(id, oppiaine_id, opetussuunnitelma_id, palautettu)
--  select ROW_NUMBER() over() as row, oppiaine_id, opetussuunnitelma_id, FALSE
--  FROM (
--         SELECT
--           ROW_NUMBER()
--           OVER (PARTITION BY ops_oppiaine_aud.oppiaine_id
--             ORDER BY ops_oppiaine_aud.oppiaine_id ASC) AS rn,
--           tyyppi, opetussuunnitelma_id, ops_oppiaine_aud.oppiaine_id
--         FROM ops_oppiaine_aud, oppiaine_aud
--         WHERE ops_oppiaine_aud.oppiaine_id = oppiaine_aud.id
--               and ops_oppiaine_aud.revtype in (0,1)
--               AND ops_oppiaine_aud.oppiaine_id IN (
--
--           SELECT DISTINCT ops_oppiaine_aud.oppiaine_id
--           FROM ops_oppiaine_aud
--           WHERE ops_oppiaine_aud.revtype = 2
--            and oppiaine_aud.id= ops_oppiaine_aud.oppiaine_id
--         )
--       ) a
--  where rn =1
--  and opetussuunnitelma_id not in(
--    select id from opetussuunnitelma_aud where koulutustyyppi = 'LUKIOKOULUTUS'
--  )
--  and tyyppi not in('LUKIO','MUU_VALINNAINEN', 'TAIDE_TAITOAINE');

