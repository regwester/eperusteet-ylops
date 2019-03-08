package fi.vm.sade.eperusteet.ylops.dto.lops2019;

import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Lops2019OppimaaraDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kuvaus;
    private Lops2019OppiaineenArviointi arviointi;
    private Lops2019OppiaineenTehtava tehtava;
    private Lops2019OppimaaranLaajaAlaisetOsaamisetDto laajaAlaisetOsaamiset;
//    private List<Lops2019ModuuliDto> moduulit = new ArrayList<>();
}
