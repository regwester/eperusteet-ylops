INSERT INTO opetussuunnitelma_organisaatiot (opetussuunnitelma_id, organisaatiot)
  SELECT o.id, '1.2.246.562.10.00000000001'
  FROM opetussuunnitelma o WHERE tyyppi = 'POHJA' AND
                                 NOT EXISTS (SELECT 0
                                             FROM opetussuunnitelma_organisaatiot org
                                             WHERE org.opetussuunnitelma_id = o.id AND
                                                   org.organisaatiot = '1.2.246.562.10.00000000001');
