package fi.vm.sade.eperusteet.ylops.resource.kommentointi;

import fi.vm.sade.eperusteet.ylops.dto.ops.Kommentti2019Dto;
import fi.vm.sade.eperusteet.ylops.dto.ops.Kommentti2019LuontiDto;
import fi.vm.sade.eperusteet.ylops.service.ops.Kommentti2019Service;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/kommentointi")
@Api("Kommentointi")
public class KommentointiController {

    @Autowired
    private Kommentti2019Service kommentti2019Service;

    @RequestMapping(value = "/{ketjuUuid}", method = GET)
    public Kommentti2019Dto getKommenttiByKetjuUuid(
            @PathVariable("ketjuUuid") final UUID uuid
    ) {
        Kommentti2019Dto kommentti2019Dto = kommentti2019Service.get(uuid);
        return kommentti2019Dto;
    }

    @RequestMapping(method = POST)
    public Kommentti2019Dto addKommenttiKetju(
            @RequestBody final Kommentti2019LuontiDto kommenttiDto
    ) {
        return kommentti2019Service.add(kommenttiDto);
    }

    @RequestMapping(value = "/{ketjuUuid}/reply", method = POST)
    public Kommentti2019Dto replyToKommenttiKetju(
            @PathVariable("ketjuUuid") final UUID uuid,
            @RequestBody final Kommentti2019LuontiDto kommenttiDto
    ) {
        Kommentti2019Dto result = kommentti2019Service.reply(uuid, kommenttiDto);
        return result;
    }

    @RequestMapping(value = "/{ketjuUuid}", method = PUT)
    public Kommentti2019Dto updateKommentti(
            @PathVariable("ketjuUuid") final UUID uuid,
            @RequestBody final Kommentti2019LuontiDto kommenttiDto
    ) {
        Kommentti2019Dto result = kommentti2019Service.update(uuid, kommenttiDto);
        return result;
    }

    @RequestMapping(value = "/{ketjuUuid}", method = DELETE)
    public void poistaKommenttiKetju(
        @PathVariable("ketjuUuid") final UUID uuid
    ) {
        kommentti2019Service.remove(uuid);
    }

}
