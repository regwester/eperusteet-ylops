ALTER TABLE kurssi ALTER COLUMN kuvaus_id DROP NOT NULL;
DROP INDEX oppaine_lukiokurssi_single; -- voi olla välikaisesti
