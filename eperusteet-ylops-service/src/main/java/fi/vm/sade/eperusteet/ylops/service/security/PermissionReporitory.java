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
package fi.vm.sade.eperusteet.ylops.service.security;

import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Omistussuhde;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Tietokantaoperaatiot oikeuksien hallintaan
 *
 * @author jhyoty
 */
@Repository
public class PermissionReporitory {

    @Autowired
    private EntityManager em;

    @Autowired
    private OpetussuunnitelmaRepository suunnitelmat;

    public Set<Opetussuunnitelma> findOpsByTekstikappaleId(long tekstikappaleId, Set<Omistussuhde> omistus) {
        assert Omistussuhde.values().length == 2 : "Kysely olettaa että omistussuhteessa on 2 arvoa";
        assert omistus != null;

        List<Omistussuhde> tmp = new ArrayList<>(omistus.isEmpty() ? EnumSet.allOf(Omistussuhde.class) : omistus);
        if (tmp.size() < Omistussuhde.values().length) {
            tmp.addAll(omistus);
        }
        Query q = em.createNamedQuery("TekstiKappaleViite.findRootByTekstikappaleId");
        List<Long> result = ((List<?>) q
            .setParameter(1, tekstikappaleId)
            .setParameter(2, tmp.get(0).name())
            .setParameter(3, tmp.get(1).name())
            .getResultList())
            .stream()
            .map(o -> ((Number) o).longValue())
            .collect(Collectors.toList());

        if (result.isEmpty()) {
            return Collections.emptySet();
        }

        return suunnitelmat.findByTekstiRoot(result);
    }

    public Set<Opetussuunnitelma> findOpsByOppiaineId(long oppiaineId) {
        return suunnitelmat.findByOppiaineId(oppiaineId);
    }

    public Set<Opetussuunnitelma> findOpsByVuosiluokkakokonaisuusId(long vuosiluokkakokonaisuusId) {
        return suunnitelmat.findByVuosiluokkakokonaisuusId(vuosiluokkakokonaisuusId);
    }
}
