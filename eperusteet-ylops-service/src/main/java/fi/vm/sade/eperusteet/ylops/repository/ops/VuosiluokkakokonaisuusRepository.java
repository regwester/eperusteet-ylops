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

import fi.vm.sade.eperusteet.ylops.domain.vuosiluokkakokonaisuus.Vuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.repository.version.JpaWithVersioningRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author jhyoty
 */
@Repository
public interface VuosiluokkakokonaisuusRepository extends JpaWithVersioningRepository<Vuosiluokkakokonaisuus, Long> {

    @Query("SELECT v FROM Opetussuunnitelma o JOIN o.vuosiluokkakokonaisuudet ov JOIN ov.vuosiluokkakokonaisuus v WHERE o.id = ?1 AND v.id = ?2")
    Vuosiluokkakokonaisuus findBy(Long opsId, Long id);

    @Query("SELECT CASE COUNT(o) WHEN 0 THEN false ELSE true END FROM Opetussuunnitelma o JOIN o.vuosiluokkakokonaisuudet ov JOIN ov.vuosiluokkakokonaisuus v WHERE v.id = ?1")
    boolean isInUse(Long kokonaisuusId);

}
