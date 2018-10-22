package fi.vm.sade.eperusteet.ylops.dto.ops;

import fi.vm.sade.eperusteet.ylops.domain.Tyyppi;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpetussuunnitelmaQuery implements Serializable {
    private String koulutustyyppi;
    private Tyyppi tyyppi;
    private String organisaatio;
    private Long perusteenId;
    private String perusteenDiaarinumero;
}
