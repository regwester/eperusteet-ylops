package fi.vm.sade.eperusteet.ylops.service.ops;

import fi.vm.sade.eperusteet.ylops.dto.ops.Kommentti2019Dto;
import fi.vm.sade.eperusteet.ylops.dto.ops.Kommentti2019LuontiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.KommenttiDto;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

public interface Kommentti2019Service {

//    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'KOMMENTOINTI')")
    @PreAuthorize("permitAll()")
    Kommentti2019Dto get(@P("uuid") final UUID uuid);

    @PreAuthorize("permitAll()")
    void asetaOikeatNimet(Kommentti2019Dto kommentti);

    //    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'KOMMENTOINTI')")
    @PreAuthorize("permitAll()")
    Kommentti2019Dto add(final Kommentti2019LuontiDto kommenttiDto);

    //    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'KOMMENTOINTI')")
    @PreAuthorize("permitAll()")
    Kommentti2019Dto reply(UUID uuid, final Kommentti2019LuontiDto kommenttiDto);

//    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'KOMMENTOINTI')")
    @PreAuthorize("permitAll()")
    Kommentti2019Dto update(final UUID uuid, Kommentti2019LuontiDto kommenttiUpdateDto);

//    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'KOMMENTOINTI')")
    @PreAuthorize("permitAll()")
    void remove(@P("uuid") final UUID uuid);

}
