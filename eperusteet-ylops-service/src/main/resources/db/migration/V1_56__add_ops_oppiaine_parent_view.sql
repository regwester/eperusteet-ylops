
CREATE OR REPLACE FUNCTION firstParentOppiaine(opeId bigint, oppiaineId bigint) RETURNS BIGINT AS $$
DECLARE
  _opsId bigint := opeId;
  _oppiaineId bigint := oppiaineId;
  _parentOppiaineId bigint := null;
BEGIN
  WHILE (_opsId IS NOT NULL) LOOP
    -- select parent ops:
    SELECT ops.pohja_id INTO _opsId FROM opetussuunnitelma ops WHERE ops.id = _opsId;
    IF _opsId IS NOT NULL THEN
      -- find oppiaine from parent
      SELECT ooa.oppiaine_id INTO _parentOppiaineId FROM ops_oppiaine ooa
        INNER JOIN oppiaine oa ON oa.id = ooa.oppiaine_id WHERE ooa.opetussuunnitelma_id = _opsId
                                                                AND oa.tunniste = (SELECT currentOppiaine.tunniste FROM oppiaine currentOppiaine WHERE currentOppiaine.id = _oppiaineId);
      IF _parentOppiaineId IS NOT NULL THEN
        RETURN _parentOppiaineId;
      END IF;
    END IF;
  END LOOP;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE VIEW ops_oppiaine_parent AS
  SELECT
    ooa.opetussuunnitelma_id                                       AS opetussuunnitelma_id,
    ooa.oppiaine_id                                                AS oppiaine_id,
    ooa.oma                                                        AS oppiaine_oma,
    oa.tunniste                                                    AS oppiaine_tunniste,
    firstParentOppiaine(ooa.opetussuunnitelma_id, ooa.oppiaine_id) AS ensimmaisen_pohjan_oppiaine_id
  FROM ops_oppiaine ooa
    INNER JOIN opetussuunnitelma ops ON ooa.opetussuunnitelma_id = ops.id
    INNER JOIN oppiaine oa ON oa.id = ooa.oppiaine_id;
