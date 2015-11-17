package fi.vm.sade.eperusteet.ylops.resource.ops;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import fi.vm.sade.eperusteet.ylops.domain.Tyyppi;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaKevytDto;
import fi.vm.sade.eperusteet.ylops.service.ops.OpetussuunnitelmaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by isaul on 11/17/15.
 */
@RestController
@RequestMapping("/opetussuunnitelmat/julkiset")
@Api(value = "Opetussuunnitelmat julkiset")
public class OpetussuunnitelmaJulkisetController {
    @Autowired
    private OpetussuunnitelmaService opetussuunnitelmaService;


    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @Timed
    public List<OpetussuunnitelmaDto> getAll(@RequestParam(value="tyyppi", required=false) Tyyppi tyyppi) {
        return opetussuunnitelmaService.getAllJulkiset(tyyppi == null ? Tyyppi.OPS : tyyppi);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    @Timed
    public ResponseEntity<OpetussuunnitelmaKevytDto> get(@PathVariable("id") final Long id) {
        return new ResponseEntity<>(opetussuunnitelmaService.getOpetussuunnitelmaJulkaistu(id), HttpStatus.OK);
    }
}
