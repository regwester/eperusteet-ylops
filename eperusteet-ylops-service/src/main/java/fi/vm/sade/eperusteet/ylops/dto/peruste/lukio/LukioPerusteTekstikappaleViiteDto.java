package fi.vm.sade.eperusteet.ylops.dto.peruste.lukio;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by jsikio on 14.11.2015.
 */
@Getter
@Setter
public class LukioPerusteTekstikappaleViiteDto {
    private Long id;
    private LukioPerusteenOsaDto perusteenOsa;
    private List<LukioPerusteTekstikappaleViiteDto> lapset;
}
