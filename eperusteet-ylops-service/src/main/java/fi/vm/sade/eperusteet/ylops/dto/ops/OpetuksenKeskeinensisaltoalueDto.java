package fi.vm.sade.eperusteet.ylops.dto.ops;

import fi.vm.sade.eperusteet.ylops.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by autio on 28.10.2015.
 */
@Getter
@Setter
public class OpetuksenKeskeinensisaltoalueDto implements ReferenceableDto {
    private Long id;
    private KeskeinenSisaltoalueDto sisaltoalueet;
    private LokalisoituTekstiDto omaKuvaus;
}
