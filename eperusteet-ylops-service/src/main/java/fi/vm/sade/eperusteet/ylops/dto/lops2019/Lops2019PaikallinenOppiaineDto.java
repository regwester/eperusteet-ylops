package fi.vm.sade.eperusteet.ylops.dto.lops2019;

import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lops2019PaikallinenOppiaineDto {
    private Long id;
    private String koodi;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kuvaus;
    private List<Lops2019PaikallinenLaajaAlainenDto> laajaAlainenOsaaminen = new ArrayList<>();
    private Lops2019TehtavaDto tehtava;
    private Lops2019ArviointiDto arviointi;
    private Lops2019OppiaineenTavoitteetDto tavoitteet;
    private String perusteenOppiaineUri;
}
