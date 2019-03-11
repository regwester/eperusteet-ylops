package fi.vm.sade.eperusteet.ylops.dto.lops2019;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.ylops.dto.KoodiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Lops2019OppiaineDto {
    private Long id;
    private KoodiDto koodi;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kuvaus;
    private Lops2019OppiaineenArviointi arviointi;
    private Lops2019OppiaineenTehtava tehtava;
    private Lops2019OppimaaranLaajaAlaisetOsaamisetDto laajaAlaisetOsaamiset;
    private List<Lops2019OppiaineDto> oppimaarat = new ArrayList<>();
    private List<Lops2019ModuuliDto> moduulit = new ArrayList<>();
}
