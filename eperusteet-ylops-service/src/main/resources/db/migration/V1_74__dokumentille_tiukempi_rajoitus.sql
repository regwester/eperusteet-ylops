DELETE FROM dokumentti WHERE exists (
    select 1
    from dokumentti d
    where d.ops_id = dokumentti.ops_id and
          d.kieli = dokumentti.kieli and
          d.valmistumisaika > dokumentti.valmistumisaika
);
ALTER TABLE dokumentti ADD CONSTRAINT ops_id_kieli_key UNIQUE (ops_id, kieli);
