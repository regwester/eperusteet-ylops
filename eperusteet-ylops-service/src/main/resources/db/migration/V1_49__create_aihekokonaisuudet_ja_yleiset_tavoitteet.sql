
CREATE TABLE aihekokonaisuudet (
  id BIGINT PRIMARY KEY NOT NULL,
  luoja CHARACTER VARYING(255),
  luotu TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
  muokattu TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja CHARACTER VARYING(255),
  tunniste UUID,
  otsikko_id BIGINT REFERENCES lokalisoituteksti(id),
  yleiskuvaus_id BIGINT REFERENCES lokalisoituteksti(id),
  opetussuunnitelma_id BIGINT REFERENCES opetussuunnitelma(id) NOT NULL
);
CREATE TABLE aihekokonaisuudet_aud (
  id BIGINT NOT NULL,
  luoja CHARACTER VARYING(255),
  luotu TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  muokattu TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja CHARACTER VARYING(255),
  tunniste UUID,
  otsikko_id BIGINT,
  yleiskuvaus_id BIGINT,
  opetussuunnitelma_id BIGINT,
  rev INTEGER NOT NULL,
  revtype SMALLINT,
  revend INTEGER,
  PRIMARY KEY (id, rev),
  FOREIGN KEY (rev) REFERENCES revinfo (rev)
  MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
  MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
CREATE TABLE aihekokonaisuus (
  id BIGINT PRIMARY KEY NOT NULL,
  luoja CHARACTER VARYING(255),
  luotu TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
  muokattu TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja CHARACTER VARYING(255),
  tunniste UUID,
  otsikko_id BIGINT REFERENCES lokalisoituteksti(id) NOT NULL,
  yleiskuvaus_id BIGINT REFERENCES lokalisoituteksti(id),
  jnro BIGINT DEFAULT 0,
  aihekokonaisuudet_id BIGINT NOT NULL REFERENCES aihekokonaisuudet (id)
);
CREATE TABLE aihekokonaisuus_aud (
  id BIGINT NOT NULL,
  luoja CHARACTER VARYING(255),
  luotu TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  muokattu TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja CHARACTER VARYING(255),
  tunniste UUID,
  otsikko_id BIGINT,
  yleiskuvaus_id BIGINT,
  rev INTEGER NOT NULL,
  revtype SMALLINT,
  revend INTEGER,
  jnro BIGINT,
  aihekokonaisuudet_id BIGINT,
  PRIMARY KEY (id, rev),
  FOREIGN KEY (rev) REFERENCES revinfo (rev)
  MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
  MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);


CREATE TABLE lukiokoulutuksen_opetuksen_yleiset_tavoitteet (
  id BIGINT PRIMARY KEY NOT NULL,
  luoja CHARACTER VARYING(255),
  luotu TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
  muokattu TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja CHARACTER VARYING(255),
  tunniste UUID,
  otsikko_id BIGINT REFERENCES lokalisoituteksti(id),
  kuvaus_id BIGINT REFERENCES lokalisoituteksti(id),
  opetussuunnitelma_id BIGINT REFERENCES opetussuunnitelma(id)
);
CREATE TABLE lukiokoulutuksen_opetuksen_yleiset_tavoitteet_aud (
  id BIGINT NOT NULL,
  luoja CHARACTER VARYING(255),
  luotu TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
  muokattu TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja CHARACTER VARYING(255),
  tunniste UUID,
  otsikko_id BIGINT,
  kuvaus_id BIGINT,
  opetussuunnitelma_id BIGINT,
  rev INTEGER NOT NULL,
  revtype SMALLINT,
  revend INTEGER,
  PRIMARY KEY (id, rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
  MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  FOREIGN KEY (rev) REFERENCES revinfo (rev)
  MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);

