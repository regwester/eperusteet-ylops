package fi.vm.sade.eperusteet.ylops.service.ukk;

import fi.vm.sade.eperusteet.ylops.dto.ukk.KysymysDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;


public interface KysymysService {

    @PreAuthorize("isAuthenticated()")
    List<KysymysDto> getKysymykset();

    // TODO: toteuta oikeustarkastelu
    @PreAuthorize("isAuthenticated()")
    KysymysDto createKysymys(KysymysDto dto);

    // TODO: toteuta oikeustarkastelu
    @PreAuthorize("isAuthenticated()")
    KysymysDto updateKysymys(KysymysDto dto);

    // TODO: toteuta oikeustarkastelu
    @PreAuthorize("isAuthenticated()")
    void deleteKysymys(Long id);

}
