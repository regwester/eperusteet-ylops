package fi.vm.sade.eperusteet.ylops.service.lops2019;

import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019OpintojaksoDto;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019OppiaineDto;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface Lops2019Service {
    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'LUKU')")
    List<Lops2019OpintojaksoDto> getOpintojaksot(@P("opsId") Long opsId);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'LUKU')")
    List<Lops2019OppiaineDto> getPerusteOppiaineet(@P("opsId") Long opsId);
}
