ALTER TABLE tekstikappaleviite ADD COLUMN valmis boolean;
UPDATE tekstikappaleviite SET valmis = 'false';
ALTER TABLE tekstikappaleviite_aud ADD COLUMN valmis boolean;
