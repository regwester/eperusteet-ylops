/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.ylops.repository.ops;

import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaine;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.OppiaineTyyppi;
import fi.vm.sade.eperusteet.ylops.repository.version.JpaWithVersioningRepository;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author mikkom
 */
@Repository
public interface OppiaineRepository extends JpaWithVersioningRepository<Oppiaine, Long> {
    @Query(value = "SELECT a FROM Opetussuunnitelma o JOIN o.oppiaineet oa JOIN oa.oppiaine a WHERE o.id = ?1")
    Set<Oppiaine> findByOpsId(long opsId);

    @Query(value = "SELECT a FROM Opetussuunnitelma o JOIN o.oppiaineet oa JOIN oa.oppiaine a WHERE o.id = ?1 AND a.tyyppi = 'YHTEINEN'")
    Set<Oppiaine> findYhteisetByOpsId(long opsId);

    @Query(value = "SELECT a FROM Opetussuunnitelma o JOIN o.oppiaineet oa JOIN oa.oppiaine a WHERE o.id = ?1 AND a.tyyppi <> 'YHTEINEN'")
    Set<Oppiaine> findValinnaisetByOpsId(long opsId);

    @Query(value = "SELECT a FROM Opetussuunnitelma o JOIN o.oppiaineet oa JOIN oa.oppiaine a WHERE o.id = ?1 AND a.tyyppi = ?2")
    Set<Oppiaine> findByOpsIdAndTyyppi(long opsId, OppiaineTyyppi tyyppi);

    @Query(value = "SELECT a FROM Opetussuunnitelma o JOIN o.oppiaineet oa JOIN oa.oppiaine a WHERE o.id = ?1 AND a.tunniste = ?2")
    Oppiaine findOneByOpsIdAndTunniste(long opsId, UUID tunniste);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM Opetussuunnitelma o JOIN o.oppiaineet oa JOIN oa.oppiaine a LEFT JOIN a.oppimaarat m WHERE o.id = ?1 AND (a.id = ?2 OR m.id = ?2)")
    boolean exists(long opsId, long oppiaineId);
}
