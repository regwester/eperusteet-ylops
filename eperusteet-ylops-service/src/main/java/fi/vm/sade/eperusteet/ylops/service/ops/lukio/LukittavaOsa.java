/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.ylops.service.ops.lukio;

import fi.vm.sade.eperusteet.ylops.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.ylops.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.repository.ops.*;
import fi.vm.sade.eperusteet.ylops.repository.version.JpaWithVersioningRepository;

import java.util.Optional;
import java.util.function.Function;

/**
 * User: tommiratamaa
 * Date: 16.12.2015
 * Time: 14.07
 */
public enum LukittavaOsa {
    OPS(OpetussuunnitelmaRepository.class),
    OPPIAINE(OppiaineRepository.class),
    LUKIOKURSSI(LukiokurssiRepository.class),
    YLEISET_TAVOITTEET(OpetuksenYleisetTavoitteetRepository.class, Opetussuunnitelma::getOpetuksenYleisetTavoitteet),
    AIHEKOKONAISUUDET(AihekokonaisuudetRepository.class, Opetussuunnitelma::getAihekokonaisuudet),
    AIHEKOKONAISUUS(AihekokonaisuusRepository.class);

    private Optional<Function<Opetussuunnitelma, ? extends AbstractAuditedReferenceableEntity>> fromOps = Optional.empty();

    private Class<? extends JpaWithVersioningRepository<?, ?>> repository;

    <T> LukittavaOsa(Class<? extends JpaWithVersioningRepository<T, ?>> repository) {
        this.repository = repository;
    }

    <T extends AbstractAuditedReferenceableEntity> LukittavaOsa(Class<? extends JpaWithVersioningRepository<T, ?>> repository,
                                                                Function<Opetussuunnitelma, T> fromOps) {
        this.repository = repository;
        this.fromOps = Optional.of(fromOps);
    }

    public Class<? extends JpaWithVersioningRepository<?, ?>> getRepository() {
        return repository;
    }

    public Optional<Function<Opetussuunnitelma, ? extends AbstractAuditedReferenceableEntity>> getFromOps() {
        return fromOps;
    }

    public boolean isFromOps() {
        return fromOps.isPresent();
    }
}
