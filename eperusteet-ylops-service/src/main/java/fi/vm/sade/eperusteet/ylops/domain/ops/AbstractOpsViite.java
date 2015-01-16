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
package fi.vm.sade.eperusteet.ylops.domain.ops;

import fi.vm.sade.eperusteet.ylops.domain.AbstractReferenceableEntity;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author jhyoty
 */
@MappedSuperclass
public abstract class AbstractOpsViite extends AbstractReferenceableEntity {

    @Getter
    @Setter
    @Column(updatable = false)
    private boolean oma;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    private Opetussuunnitelma opetussuunnitelma;

    public AbstractOpsViite() {
    }

    public AbstractOpsViite(Opetussuunnitelma opetussuunnitelma, boolean oma) {
        this.oma = oma;
        this.opetussuunnitelma = opetussuunnitelma;
    }

    void setOpetussuunnitelma(Opetussuunnitelma opetussuunnitelma) {
        if (this.opetussuunnitelma == null || this.opetussuunnitelma.equals(opetussuunnitelma)) {
            this.opetussuunnitelma = opetussuunnitelma;
        } else {
            throw new IllegalStateException("viitettä ei voi vaihtaa");
        }
    }

}
