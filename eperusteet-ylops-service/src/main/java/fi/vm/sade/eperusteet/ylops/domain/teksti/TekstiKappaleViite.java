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
package fi.vm.sade.eperusteet.ylops.domain.teksti;

import fi.vm.sade.eperusteet.ylops.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.ylops.dto.EntityReference;
import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 *
 * @author mikkom
 */
@Entity
@Audited
@Table(name = "tekstikappaleviite")
@NamedNativeQuery(
    name = "TekstiKappaleViite.findRootByTekstikappaleId",
    query
    = "with recursive vanhemmat(id,vanhempi_id,tekstikappale_id) as "
    + "(select tv.id, tv.vanhempi_id, tv.tekstikappale_id from tekstikappaleviite tv "
    + "where tv.tekstikappale_id = ?1 and tv.omistussuhde in (?2,?3) "
    + "union all "
    + "select tv.id, tv.vanhempi_id, v.tekstikappale_id "
    + "from tekstikappaleviite tv, vanhemmat v where tv.id = v.vanhempi_id) "
    + "select id from vanhemmat where vanhempi_id is null"
)
public class TekstiKappaleViite implements ReferenceableEntity, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ManyToOne
    @Getter
    @Setter
    private TekstiKappaleViite vanhempi;

    @ManyToOne
    @Getter
    @Setter
    private TekstiKappale tekstiKappale;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    @Getter
    @Setter
    private Omistussuhde omistussuhde = Omistussuhde.OMA;

    @OneToMany(mappedBy = "vanhempi", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderColumn
    @Getter
    @Setter
    private List<TekstiKappaleViite> lapset;

    public TekstiKappaleViite() {
    }

    public TekstiKappaleViite(Omistussuhde omistussuhde) {
        this.omistussuhde = omistussuhde;
    }

    @Override
    public EntityReference getReference() {
        return new EntityReference(id);
    }

    public TekstiKappaleViite getRoot() {
        TekstiKappaleViite root = this;
        while (root.getVanhempi() != null) {
            root = root.getVanhempi();
        }
        return root;
    }
}
