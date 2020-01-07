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
public class Kommentti2019Dto extends Kommentti2019LuontiDto {
    private UUID tunniste;
    private UUID parent;
    private Long opsId;
    private Date luotu;
    private Date muokattu;
    private String luoja;
    private List<Kommentti2019Dto> kommentit = new ArrayList<>();
}
