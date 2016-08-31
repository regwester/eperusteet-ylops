package fi.vm.sade.eperusteet.ylops.dto.ops;

import fi.vm.sade.eperusteet.ylops.domain.Tyyppi;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpetussuunnitelmaQuery implements Serializable {
//    private int sivu = 0;
//    private int sivukoko = 25;
//    private String nimi;
//    private String tila = "valmis";
    private String koulutustyyppi;
    private Tyyppi tyyppi;
    private String organisaatio;
}
