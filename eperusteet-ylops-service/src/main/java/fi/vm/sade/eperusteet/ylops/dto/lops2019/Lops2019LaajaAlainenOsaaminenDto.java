package fi.vm.sade.eperusteet.ylops.dto.lops2019;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Lops2019LaajaAlainenOsaaminenDto {
    private List<Lops2019LaajaAlainenDto> laajaAlaisetOsaamiset = new ArrayList<>();
}
