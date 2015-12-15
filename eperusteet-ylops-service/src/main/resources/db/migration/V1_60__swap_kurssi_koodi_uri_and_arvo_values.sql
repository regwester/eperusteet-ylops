-- incorrectly swapped in public interface of ePerusteet
ALTER TABLE kurssi ADD COLUMN _tmp VARCHAR(255);
ALTER TABLE kurssi_aud ADD COLUMN _tmp VARCHAR(255);
UPDATE kurssi SET _tmp = koodi_arvo;
UPDATE kurssi SET koodi_arvo = koodi_uri;
UPDATE kurssi SET koodi_uri = _tmp;
UPDATE kurssi_aud SET _tmp = koodi_arvo;
UPDATE kurssi_aud SET koodi_arvo = koodi_uri;
UPDATE kurssi_aud SET koodi_uri = _tmp;
ALTER TABLE kurssi DROP COLUMN _tmp;
ALTER TABLE kurssi_aud DROP COLUMN _tmp;

UPDATE opetussuunnitelma SET cached_peruste = null WHERE koulutustyyppi = 'LUKIOKOULUTUS';
DELETE FROM peruste_cache WHERE koulutustyyppi = 'LUKIOKOULUTUS';
