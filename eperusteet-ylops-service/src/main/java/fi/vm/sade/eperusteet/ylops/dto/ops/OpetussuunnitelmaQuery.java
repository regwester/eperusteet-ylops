package fi.vm.sade.eperusteet.ylops.dto.ops;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OpetussuunnitelmaQuery {
    private int sivu = 0;
    private int sivukoko = 25;
    private String nimi;
    private String tila = "valmis";
    private List<String> koulutustyyppi;

    public void setTyyppi(List<String> tyyppi) {
        this.koulutustyyppi = tyyppi;
    }

    public List<String> getTyyppi() {
        return this.koulutustyyppi;
    }
}
