
CREATE OR REPLACE FUNCTION findParentKurssi(opsId bigint, kurssiId bigint) RETURNS BIGINT AS $$
BEGIN
  RETURN (WITH RECURSIVE opsit AS (
    SELECT opsId as id, 1 as dept
    UNION SELECT ops.pohja_id as id, opsit.dept+1 as dept
      FROM opsit INNER JOIN opetussuunnitelma ops ON ops.id = opsit.id AND ops.pohja_id IS NOT NULL
  ) SELECT oa_lk.id FROM opsit o
    INNER JOIN oppiaine_lukiokurssi oa_lk ON o.id = oa_lk.opetussuunnitelma_id
    INNER JOIN lukiokurssi lk ON oa_lk.kurssi_id = lk.id
    INNER JOIN kurssi k ON k.id = lk.id AND k.tunniste = (select tunniste from kurssi k2 WHERE k2.id = kurssiId)
  WHERE o.dept > 1 ORDER BY o.dept LIMIT 1);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- SELECT oa_lk.kurssi_id, findParentKurssi(:opsId, oa_lk.kurssi_id) from oppiaine_lukiokurssi oa_lk
-- WHERE oa_lk.opetussuunnitelma_id = :opsId GROUP BY oa_lk.kurssi_id;