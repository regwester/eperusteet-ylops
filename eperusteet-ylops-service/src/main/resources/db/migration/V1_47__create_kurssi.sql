CREATE TABLE kurssi (
  id         BIGINT PRIMARY KEY                        NOT NULL, -- hibernate_sequence used by application
  luoja      CHARACTER VARYING(255),
  luotu      TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
  muokattu   TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja  CHARACTER VARYING(255),
  tunniste   UUID,
  opetussuunnitelma_id BIGINT REFERENCES opetussuunnitelma(id) NOT NULL,
  nimi_id    BIGINT REFERENCES lokalisoituteksti (id)      NOT NULL,
  kuvaus_id  BIGINT REFERENCES lokalisoituteksti (id)      NOT NULL,
  koodi_uri  VARCHAR(255),
  koodi_arvo VARCHAR(255)
);
CREATE TABLE kurssi_aud (
  id         BIGINT                                    NOT NULL,
  luoja      CHARACTER VARYING(255),
  luotu      TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  muokattu   TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja  CHARACTER VARYING(255),
  tunniste   UUID,
  opetussuunnitelma_id BIGINT,
  nimi_id    BIGINT,
  kuvaus_id  BIGINT,
  koodi_uri  VARCHAR(255),
  koodi_arvo VARCHAR(255),
  rev        INTEGER,
  revtype    SMALLINT,
  revend     INTEGER,
  PRIMARY KEY (rev, id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);

CREATE TABLE lukiokurssityyppi (
  tyyppi VARCHAR(64) PRIMARY KEY,
  paikallinen BOOL NOT NULL DEFAULT FALSE
);
INSERT INTO lukiokurssityyppi (tyyppi, paikallinen)
VALUES ('VALTAKUNNALLINEN_PAKOLLINEN', FALSE),
  ('VALTAKUNNALLINEN_SYVENTAVA', FALSE),
  ('VALTAKUNNALLINEN_SOVELTAVA', FALSE),
  ('PAIKALLINEN_PAKOLLINEN', TRUE),
  ('PAIKALLINEN_SYVENTAVA', TRUE),
  ('PAIKALLINEN_SOVELTAVA', TRUE);


CREATE TABLE lukiokurssi (
  id                     BIGINT PRIMARY KEY REFERENCES kurssi (id)               NOT NULL,
  tyyppi                 VARCHAR(64) REFERENCES lukiokurssityyppi (tyyppi)       NOT NULL,
  lokalisoitava_koodi_id BIGINT REFERENCES lokalisoituteksti (id),
  kurssityypin_kuvaus_id BIGINT REFERENCES lokalisoituteksti (id),
  tavoitteet_id          BIGINT REFERENCES tekstiosa (id),
  keskeinen_sisalto_id   BIGINT REFERENCES tekstiosa (id),
  tavoitteet_ja_keskeinen_sisalto_id BIGINT REFERENCES tekstiosa (id)
);
CREATE TABLE lukiokurssi_aud (
  id                     BIGINT      NOT NULL,
  tyyppi                 VARCHAR(64),
  lokalisoitava_koodi_id BIGINT,
  kurssityypin_kuvaus_id BIGINT,
  tavoitteet_id          BIGINT,
  keskeinen_sisalto_id   BIGINT,
  tavoitteet_ja_keskeinen_sisalto_id BIGINT,
  rev                    INTEGER     NOT NULL,
  revtype                SMALLINT,
  revend                 INTEGER,
  PRIMARY KEY (rev, id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);

CREATE TABLE oppaine_lukiokurssi (
  id          BIGINT PRIMARY KEY                        NOT NULL, -- hibernate_sequence used by application
  luoja       CHARACTER VARYING(255),
  luotu       TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
  muokattu    TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja   CHARACTER VARYING(255),
  oppiaine_id BIGINT REFERENCES oppiaine (id)        NOT NULL,
  kurssi_id   BIGINT REFERENCES lukiokurssi (id)     NOT NULL,
  jarjestys   INT
);
CREATE UNIQUE INDEX oppaine_lukiokurssi_single ON oppaine_lukiokurssi (oppiaine_id, kurssi_id);
CREATE TABLE oppaine_lukiokurssi_aud (
  id          BIGINT                                    NOT NULL,
  luoja       CHARACTER VARYING(255),
  luotu       TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  muokattu    TIMESTAMP WITHOUT TIME ZONE,
  muokkaaja   CHARACTER VARYING(255),
  oppiaine_id BIGINT,
  kurssi_id   BIGINT,
  jarjestys   INT,
  rev         INTEGER,
  revtype     SMALLINT,
  revend      INTEGER,
  PRIMARY KEY (rev, id),
  FOREIGN KEY (rev) REFERENCES revinfo (rev),
  FOREIGN KEY (revend) REFERENCES revinfo (rev)
);
