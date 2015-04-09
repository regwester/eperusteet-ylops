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

import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.Tyyppi;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.repository.version.JpaWithVersioningRepository;
import fi.vm.sade.eperusteet.ylops.service.util.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author mikkom
 */
@Repository
public interface OpetussuunnitelmaRepository extends JpaWithVersioningRepository<Opetussuunnitelma, Long> {

    @Query(value = "SELECT org from Opetussuunnitelma o join o.organisaatiot org where o.id = ?1")
    public List<String> findOrganisaatiot(long id);

    @Query(value = "SELECT NEW fi.vm.sade.eperusteet.ylops.service.util.Pair(o.tyyppi, o.tila) from Opetussuunnitelma o where o.id = ?1")
    public Pair<Tyyppi,Tila> findTyyppiAndTila(long id);

    public Opetussuunnitelma findOneByTyyppiAndTila(Tyyppi tyyppi, Tila tila);
    public Opetussuunnitelma findFirst1ByTyyppi(Tyyppi tyyppi);
    public List<Opetussuunnitelma> findAllByTyyppi(Tyyppi tyyppi);

    @Query(value = "SELECT DISTINCT o FROM Opetussuunnitelma o JOIN o.organisaatiot org " +
                   "WHERE org IN (:organisaatiot) AND o.tyyppi = :tyyppi")
    public List<Opetussuunnitelma> findAllByTyyppi(@Param("tyyppi") Tyyppi tyyppi,
                                                   @Param("organisaatiot") Collection<String> organisaatiot);

    @Query(value = "SELECT o FROM Opetussuunnitelma o WHERE o.tekstit.id in ?1")
    Set<Opetussuunnitelma> findByTekstiRoot(Iterable<Long> ids);

    @Query(value = "SELECT o FROM Opetussuunnitelma o JOIN o.oppiaineet oa JOIN oa.oppiaine a WHERE a.id = ?1")
    Set<Opetussuunnitelma> findByOppiaineId(long id);

    @Query(value = "SELECT o FROM Opetussuunnitelma o JOIN o.vuosiluokkakokonaisuudet ov JOIN ov.vuosiluokkakokonaisuus v WHERE v.id = ?1")
    Set<Opetussuunnitelma> findByVuosiluokkakokonaisuusId(long id);
}
