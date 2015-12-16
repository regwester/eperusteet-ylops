
CREATE OR REPLACE FUNCTION findParentKurssi(opsId bigint, kurssiId bigint) RETURNS BIGINT AS $$
DECLARE
  _opsId bigint := opsId;
  _kurssiId bigint := kurssiId;
  _parentKurssiId bigint := null;
BEGIN

  select kurssi.id from kurssi, oppiaine_lukiokurssi WHERE opetussuunnitelma_id = (
    select id from opetussuunnitelma WHERE id = (
      select pohja_id from opetussuunnitelma where id = _opsId
    )
  ) and oppiaine_id in (
    SELECT oppiaine_id FROM oppiaine_lukiokurssi WHERE opetussuunnitelma_id = _opsId and kurssi_id = _kurssiId
  ) and kurssi_id = kurssi.id and kurssi.tunniste = (
    select tunniste from kurssi where id = _kurssiId
  ) into _parentKurssiId;

  IF _parentKurssiId IS NOT NULL THEN
    RETURN _parentKurssiId;
  END IF;

  RETURN NULL;
END;
$$ LANGUAGE plpgsql;
