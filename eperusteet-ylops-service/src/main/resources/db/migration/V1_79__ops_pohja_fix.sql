CREATE OR REPLACE FUNCTION fix_pohja(ops_id BIGINT) RETURNS VOID
    AS $$
		UPDATE opetussuunnitelma
			SET pohja_id = oikea_pohja.pohja_id
			FROM (SELECT pohja_id
				  FROM opetussuunnitelma_aud
				  WHERE pohja_id IS NOT NULL AND id = ops_id
				  ORDER BY rev
				  DESC LIMIT 1) AS oikea_pohja
			WHERE id = ops_id;
        UPDATE opetussuunnitelma_aud
            SET pohja_id = oikea_pohja.pohja_id
            FROM (SELECT pohja_id
                  FROM opetussuunnitelma_aud
                  WHERE pohja_id IS NOT NULL AND id = ops_id
                  ORDER BY rev
                  DESC LIMIT 1) AS oikea_pohja
            WHERE id = ops_id;
    $$ LANGUAGE SQL;

SELECT fix_pohja(id) FROM opetussuunnitelma WHERE pohja_id IS NULL AND tyyppi = 'OPS';

ALTER TABLE opetussuunnitelma DROP CONSTRAINT IF EXISTS has_pohja;
ALTER TABLE opetussuunnitelma
    ADD CONSTRAINT has_pohja CHECK (
    (tyyppi = 'OPS' AND pohja_id IS NOT NULL)
    OR (tyyppi = 'POHJA' AND pohja_id IS NULL));