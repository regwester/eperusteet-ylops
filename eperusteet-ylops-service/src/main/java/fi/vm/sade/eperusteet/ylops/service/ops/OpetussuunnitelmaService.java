/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.ylops.service.ops;

import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.Tyyppi;
import fi.vm.sade.eperusteet.ylops.domain.peruste.Peruste;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteLaajaalainenosaaminen;
import fi.vm.sade.eperusteet.ylops.dto.JarjestysDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaInfoDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaLuontiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleViiteDto;
import java.util.List;
import java.util.Set;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author mikkom
 */
public interface OpetussuunnitelmaService {

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'LUKU')")
    Set<PerusteLaajaalainenosaaminen> getLaajaalaisetosaamiset(@P("opsId") Long id);

    @PreAuthorize("hasPermission(null, 'tarkastelu', 'HALLINTA') ||" +
        "(#tyyppi == T(fi.vm.sade.eperusteet.ylops.domain.Tyyppi).OPS and (hasPermission(null, 'opetussuunnitelma', 'LUKU'))) || " +
        "(#tyyppi == T(fi.vm.sade.eperusteet.ylops.domain.Tyyppi).POHJA and hasPermission(null, 'pohja', 'LUKU'))")
    List<OpetussuunnitelmaInfoDto> getAll(Tyyppi tyyppi);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'LUKU')")
    OpetussuunnitelmaDto getOpetussuunnitelma(@P("opsId") Long id);

    @PreAuthorize("hasPermission(null, 'opetussuunnitelma', 'LUONTI')")
    OpetussuunnitelmaDto addOpetussuunnitelma(OpetussuunnitelmaLuontiDto opetussuunnitelmaDto);

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    OpetussuunnitelmaDto addPohja(OpetussuunnitelmaLuontiDto opetussuunnitelmaDto);

    @PreAuthorize("hasPermission(#ops.id, 'opetussuunnitelma', 'MUOKKAUS')")
    OpetussuunnitelmaDto updateOpetussuunnitelma(@P("ops") OpetussuunnitelmaDto opetussuunnitelmaDto);

    @PreAuthorize("hasPermission(#id, 'opetussuunnitelma', 'TILANVAIHTO')")
    OpetussuunnitelmaDto updateTila(@P("id") Long id, Tila tila);

    @PreAuthorize("hasPermission(#id, 'opetussuunnitelma', 'HALLINTA')")
    OpetussuunnitelmaDto restore(@P("id") Long id);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'POISTO')")
    void removeOpetussuunnitelma(@P("opsId") Long id);

    @PreAuthorize("hasPermission(#pohjaId, 'opetussuunnitelma', 'MUOKKAUS')")
    void syncPohja(Long pohjaId);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    void updateOppiainejarjestys(Long opsId, List<JarjestysDto> oppiainejarjestys);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'LUKU')")
    TekstiKappaleViiteDto.Puu getTekstit(@P("opsId") final Long opsId);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    TekstiKappaleViiteDto.Matala addTekstiKappale(@P("opsId") final Long opsId, TekstiKappaleViiteDto.Matala viite);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    TekstiKappaleViiteDto.Matala addTekstiKappaleLapsi(@P("opsId") final Long opsId, final Long parentId,
                                                       TekstiKappaleViiteDto.Matala viite);
    /**
     * Hakee opetussuunnitelmaan liittyvän opetussuunnitelman perusteen
     *
     * @param opsId
     * @return Peruste
     */
    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'LUKU')")
    public Peruste getPeruste(@P("opsId") Long opsId);
}
