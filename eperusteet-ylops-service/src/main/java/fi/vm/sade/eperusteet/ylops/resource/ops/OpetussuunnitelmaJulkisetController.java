package fi.vm.sade.eperusteet.ylops.resource.ops;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaJulkinenDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaQuery;
import fi.vm.sade.eperusteet.ylops.service.ops.OpetussuunnitelmaService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @ApiImplicitParams({
//        @ApiImplicitParam(name = "sivu", dataType = "integer", paramType = "query"),
//        @ApiImplicitParam(name = "sivukoko", dataType = "integer", paramType = "query"),
//        @ApiImplicitParam(name = "siirtyma", dataType = "boolean", paramType = "query", defaultValue = "false", value = "hae myös siirtymäajalla olevat perusteet"),
//        @ApiImplicitParam(name = "nimi", dataType = "string", paramType = "query"),
//        @ApiImplicitParam(name = "koulutusala", dataType = "string", paramType = "query", allowMultiple = true),
        @ApiImplicitParam(name = "koulutustyyppi", dataType = "string", paramType = "query", allowMultiple = false, value = "koulutustyyppi (koodistokoodi)"),
        @ApiImplicitParam(name = "organisaatio", dataType = "string", paramType = "query", allowMultiple = false, value = "organisaatio oid (organisaatiopalvelusta)"),
        @ApiImplicitParam(name = "tyyppi", dataType = "string", paramType = "query", allowMultiple = false, value = "ops | pohja")
//        @ApiImplicitParam(name = "kieli", dataType = "string", paramType = "query", defaultValue = "fi", value = "perusteen nimen kieli"),
//        @ApiImplicitParam(name = "opintoala", dataType = "string", paramType = "query", allowMultiple = true, value = "opintoalakoodi"),
//        @ApiImplicitParam(name = "muokattu", dataType = "integer", paramType = "query", value = "muokattu jälkeen (aikaleima; millisenkunteja alkaen 1970-01-01 00:00:00 UTC)")
    })
    public List<OpetussuunnitelmaJulkinenDto> getAll(OpetussuunnitelmaQuery query) {
        return opetussuunnitelmaService.getAllJulkiset(query);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    @Timed
    public ResponseEntity<OpetussuunnitelmaJulkinenDto> get(@PathVariable("id") final Long id) {
        return new ResponseEntity<>(opetussuunnitelmaService.getOpetussuunnitelmaJulkinen(id), HttpStatus.OK);
    }

}
