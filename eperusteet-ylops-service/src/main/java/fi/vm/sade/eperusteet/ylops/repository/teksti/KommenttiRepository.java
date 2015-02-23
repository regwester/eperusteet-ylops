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
package fi.vm.sade.eperusteet.ylops.repository.teksti;

import fi.vm.sade.eperusteet.ylops.domain.teksti.Kommentti;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author mikkom
 */
@Repository
public interface KommenttiRepository extends JpaRepository<Kommentti, Long> {
    List<Kommentti> findByOpetussuunnitelmaId(Long opetussuunnitelmaId);

    @Query("SELECT k FROM Kommentti k WHERE k.opetussuunnitelmaId = ?1 AND k.tekstiKappaleViiteId = ?2")
    List<Kommentti> findByTekstiKappaleViiteId(Long opsId, Long tekstiKappaleViiteId);

    List<Kommentti> findByParentId(Long parentId);

    List<Kommentti> findByYlinId(Long ylinId);
}
