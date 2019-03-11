package fi.vm.sade.eperusteet.ylops.dto;

import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EtuliitelistaDto {
    private LokalisoituTekstiDto etuliite;
    private List<LokalisoituTekstiDto> arvot;
}
