package fi.vm.sade.eperusteet.ylops.dto.lops2019;

import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Lops2019LaajaAlainenDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kuvaus;
    private LokalisoituTekstiDto opinnot;
    private List<Lops2019LaajaAlaisenOsaamisenTavoitteetDto> tavoitteet = new ArrayList<>();
    private List<Lops2019LaajaAlaisenOsaamisenPainopisteDto> painopisteet = new ArrayList<>();
}
