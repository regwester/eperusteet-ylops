package fi.vm.sade.eperusteet.ylops.resource.lops2019;

import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.lops2019.Lops2019Service;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/opetussuunnitelmat/{opsId}/peruste")
@Api("Lops2019PerusteController")
@ApiIgnore
public class Lops2019PerusteController {

    @Autowired
    private EperusteetService eperusteetService;

    @Autowired
    private Lops2019Service lopsService;

    @RequestMapping(method = RequestMethod.GET)
    public List<Lops2019OppiaineDto> getAllLops2019PerusteOppiaineet(
            @PathVariable final Long opsId) {
        return lopsService.getPerusteOppiaineet(opsId);
    }

}
