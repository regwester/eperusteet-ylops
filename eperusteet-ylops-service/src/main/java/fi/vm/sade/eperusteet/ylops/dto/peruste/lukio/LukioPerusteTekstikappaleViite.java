package fi.vm.sade.eperusteet.ylops.dto.peruste.lukio;

import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteTekstiKappale;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by jsikio on 14.11.2015.
 */
@Getter
@Setter
public class LukioPerusteTekstikappaleViite {
    private Long id;
    private LukioPerusteenOsa perusteenOsa;
    private List<LukioPerusteTekstikappaleViite> lapset;
}
