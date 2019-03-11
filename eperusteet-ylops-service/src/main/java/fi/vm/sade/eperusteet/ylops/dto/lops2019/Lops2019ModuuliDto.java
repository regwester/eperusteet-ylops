package fi.vm.sade.eperusteet.ylops.dto.lops2019;

import fi.vm.sade.eperusteet.ylops.dto.EtuliitelistaDto;
import fi.vm.sade.eperusteet.ylops.dto.KoodiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Lops2019ModuuliDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private KoodiDto koodi;
    private Integer laajuus;
    private EtuliitelistaDto tavoitteet;
    private EtuliitelistaDto sisallot;
}
