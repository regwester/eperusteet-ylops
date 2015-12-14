CREATE TABLE lukio_oppiaine_jarjestys (
  opetussuunnitelma_id BIGINT REFERENCES opetussuunnitelma (id) NOT NULL,
  oppiaine_id          BIGINT REFERENCES oppiaine (id)          NOT NULL,
  jarjestys            INT,
  luoja                VARCHAR(255),
  luotu                TIMESTAMP DEFAULT now(),
  muokattu             TIMESTAMP,
  muokkaaja            VARCHAR(255),
  PRIMARY KEY (opetussuunnitelma_id, oppiaine_id)
);

CREATE TABLE lukio_oppiaine_jarjestys_aud (
  opetussuunnitelma_id BIGINT NOT NULL,
  oppiaine_id          BIGINT NOT NULL,
  jarjestys            INT,
  luoja                VARCHAR(255),
  luotu                TIMESTAMP,
  muokattu             TIMESTAMP,
  muokkaaja            VARCHAR(255),
  rev                  INT4   NOT NULL,
  revtype              INT2,
  revend               INT4,
  PRIMARY KEY (opetussuunnitelma_id, oppiaine_id, rev)
);

INSERT INTO lukio_oppiaine_jarjestys(opetussuunnitelma_id, oppiaine_id, jarjestys)
WITH RECURSIVE oppiaineet_opetusuunnitelmat AS (
  SELECT
      ops.id as opetusuunnitelma_id,
      oa.id as oppiaine_id,
      oa.jarjestys as jarjestys
    FROM opetussuunnitelma ops
        INNER JOIN ops_oppiaine ooa ON ooa.opetussuunnitelma_id = ops.id
        INNER JOIN oppiaine oa ON ooa.oppiaine_id = oa.id
  UNION
    SELECT
        oppiaineet_opetusuunnitelmat.opetusuunnitelma_id as opetusuunnitelma_id,
        child.id as oppiaine_id,
        child.jarjestys as jarjestys
      FROM oppiaineet_opetusuunnitelmat
        INNER JOIN oppiaine child ON child.oppiaine_id = oppiaineet_opetusuunnitelmat.oppiaine_id
) SELECT oo.* FROM oppiaineet_opetusuunnitelmat oo
    INNER JOIN opetussuunnitelma ops ON oo.opetusuunnitelma_id = ops.id
    WHERE ops.koulutustyyppi = 'LUKIOKOULUTUS';

-- jarjestys-column on ollut väärässä paikassa alun perinkin (ja lisätty Lukiota varten):
ALTER TABLE oppiaine DROP COLUMN jarjestys;
ALTER TABLE oppiaine_aud DROP COLUMN jarjestys;
