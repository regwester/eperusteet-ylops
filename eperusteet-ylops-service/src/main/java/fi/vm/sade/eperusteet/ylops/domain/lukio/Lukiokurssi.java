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

package fi.vm.sade.eperusteet.ylops.domain.lukio;

import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Tekstiosa;
import fi.vm.sade.eperusteet.ylops.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.ylops.domain.validation.ValidHtml.WhitelistType;
import fi.vm.sade.eperusteet.ylops.service.util.LambdaUtil.Copier;
import fi.vm.sade.eperusteet.ylops.service.util.LambdaUtil.Copyable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User: tommiratamaa
 * Date: 17.11.2015
 * Time: 14.02
 */
@Entity
@Audited
@Table(name = "lukiokurssi", schema = "public")
public class Lukiokurssi extends Kurssi implements Copyable<Lukiokurssi> {

    @Getter
    @Setter
    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LukiokurssiTyyppi tyyppi;

    @Getter
    @Setter
    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @JoinColumn(name = "lokalisoitava_koodi_id", nullable = true)
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private LokalisoituTeksti lokalisoituKoodi;

    @Getter
    @Setter
    @Valid
    @JoinColumn(name = "tavoitteet_id", nullable = true)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Tekstiosa tavoitteet;

    @Getter
    @Setter
    @Valid
    @JoinColumn(name = "keskeinen_sisalto_id", nullable = true)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Tekstiosa keskeinenSisalto;

    @Getter
    @Setter
    @Valid
    @JoinColumn(name = "tavoitteet_ja_keskeinen_sisalto_id", nullable = true)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Tekstiosa tavoitteetJaKeskeinenSisalto;

    @Getter
    @Audited
    @OneToMany(mappedBy = "kurssi", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OppiaineLukiokurssi> oppiaineet = new HashSet<>(0);

    public Lukiokurssi() {
        super(UUID.randomUUID());
    }

    public Lukiokurssi(UUID tunniste) {
        super(tunniste);
    }

    public Lukiokurssi copy() {
        return copyInto(new Lukiokurssi(this.getTunniste()));
    }

    public Lukiokurssi copyInto(Lukiokurssi lukiokurssi) {
        super.copyInto(lukiokurssi);
        lukiokurssi.setTyyppi(this.tyyppi);
        lukiokurssi.setLokalisoituKoodi(this.lokalisoituKoodi);
        lukiokurssi.setTavoitteet(Tekstiosa.copyOf(this.tavoitteet));
        lukiokurssi.setKeskeinenSisalto(Tekstiosa.copyOf(this.keskeinenSisalto));
        lukiokurssi.setTavoitteetJaKeskeinenSisalto(Tekstiosa.copyOf(this.tavoitteetJaKeskeinenSisalto));
        return lukiokurssi;
    }
}
