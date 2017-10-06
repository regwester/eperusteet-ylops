package fi.vm.sade.eperusteet.ylops.dto.teksti;

import fi.vm.sade.eperusteet.ylops.dto.PoistettuDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by autio on 15.2.2016.
 */
@Getter
@Setter
public class PoistettuTekstiKappaleDto extends PoistettuDto {
    private Long tekstiKappale;
}
