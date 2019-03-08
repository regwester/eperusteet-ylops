package fi.vm.sade.eperusteet.ylops.dto.lops2019;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Lops2019OppiaineDto extends Lops2019OppimaaraDto {
    private List<Lops2019OppimaaraDto> oppimaarat = new ArrayList<>();
}
