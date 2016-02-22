CREATE INDEX oppiaine_oppiaine_index on oppiaine (oppiaine_id);
CREATE INDEX ops_pohja_index ON opetussuunnitelma (pohja_id);
CREATE INDEX ops_kuvaus_index ON opetussuunnitelma (kuvaus_id);
CREATE INDEX ops_nimi_index ON opetussuunnitelma (nimi_id);
CREATE INDEX ops_tekstit_index ON opetussuunnitelma (tekstit_id);
CREATE INDEX lukiokurssi_kurssi_index on oppiaine_lukiokurssi (kurssi_id);
CREATE INDEX lukiokurssi_ops_index on oppiaine_lukiokurssi (opetussuunnitelma_id);
CREATE INDEX lukiokurssi_oppiaine_index on oppiaine_lukiokurssi (oppiaine_id);
CREATE INDEX tekstikappale_teksti_index on tekstikappale (teksti_id);

