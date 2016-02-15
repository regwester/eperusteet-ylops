package fi.vm.sade.eperusteet.ylops.dto.teksti;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by autio on 15.2.2016.
 */
@Getter
@Setter
public class PoistettuTekstiKappaleDto {
    private Long id;
    private TekstiKappaleDto tekstiKappale;
    private Boolean palautettu;
    private String luoja;
    private Date luotu;
    private String muokkaaja;
    private Date muokattu;
}
