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
package fi.vm.sade.eperusteet.ylops.repository.liite;

import fi.vm.sade.eperusteet.ylops.domain.liite.Liite;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author jhyoty
 */
@Repository
public interface LiiteRepository extends JpaRepository<Liite, UUID>, LiiteRepositoryCustom {

    @Query("SELECT l FROM Opetussuunnitelma o JOIN o.liitteet l WHERE o.id = ?1")
    List<Liite> findByOpsId(Long opsId);

    @Query("SELECT l FROM Opetussuunnitelma o JOIN o.liitteet l WHERE o.id = ?1 AND l.id = ?2")
    Liite findOne(Long opsId, UUID id);
}
