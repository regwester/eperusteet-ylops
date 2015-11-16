CREATE TABLE peruste_cache(
  id SERIAL8 PRIMARY KEY,
  peruste_id INT8 NOT NULL,
  aikaleima TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  UNIQUE (peruste_id, aikaleima),
  diaarinumero VARCHAR(255) NOT NULL,
  koulutustyyppi VARCHAR(255) NOT NULL,
  voimassaolo_alkaa TIMESTAMP WITHOUT TIME ZONE,
  voimassaolo_loppuu TIMESTAMP WITHOUT TIME ZONE,
  nimi_id INT8 REFERENCES lokalisoituteksti(id) NOT NULL,
  peruste_json TEXT NOT NULL
);