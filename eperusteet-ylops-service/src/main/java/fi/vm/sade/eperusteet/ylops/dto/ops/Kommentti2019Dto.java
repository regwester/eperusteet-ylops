package fi.vm.sade.eperusteet.ylops.dto.ops;

import fi.vm.sade.eperusteet.ylops.dto.dokumentti.LokalisointiDto;
import fi.vm.sade.eperusteet.ylops.service.external.impl.perustedto.TekstiOsaDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Kommentti2019Dto {
    private UUID thread;
    private UUID tunniste;
    private UUID reply;
    private String sisalto;
    private Long opsId;
    private Date luotu;
    private Date muokattu;
    private String luoja;
    private String nimi;

    public static Kommentti2019Dto of(String kommentti) {
        Kommentti2019Dto result = new Kommentti2019Dto();
        result.sisalto = kommentti;
        return result;
    }

}
