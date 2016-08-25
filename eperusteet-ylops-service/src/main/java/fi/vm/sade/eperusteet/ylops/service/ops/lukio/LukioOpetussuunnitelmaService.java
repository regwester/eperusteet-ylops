/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.ylops.service.ops.lukio;

import fi.vm.sade.eperusteet.ylops.dto.lukio.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * User: tommiratamaa
 * Date: 19.11.2015
 * Time: 15.28
 */
public interface LukioOpetussuunnitelmaService {

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'LUKU')")
    LukioOpetussuunnitelmaRakenneOpsDto getRakenne(long opsId);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'LUKU')")
    LukioOppiaineTiedotDto getOppiaineTiedot(long opsId, long oppiaineId);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'LUKU')")
    AihekokonaisuudetPerusteOpsDto getAihekokonaisuudet(long opsId);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'LUKU')")
    OpetuksenYleisetTavoitteetPerusteOpsDto getOpetuksenYleisetTavoitteet(long opsId);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    void updateOpetuksenYleisetTavoitteet(long opsId, OpetuksenYleisetTavoitteetUpdateDto tavoitteet);
    
    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    long saveOppiaine(long opsId, LukioOppiaineSaveDto oppiaine);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    void updateTreeStructure(Long opsId, OppaineKurssiTreeStructureDto structureDto);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    void updateOppiaine(long opsId, LukioOppiaineSaveDto oppiaine);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    long addOppimaara(long opsId, long oppiaineId, LukioKopioiOppimaaraDto kt);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    long addAbstraktiOppiaine(long opsId, LukioAbstraktiOppiaineTuontiDto tuonti);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    long saveKurssi(long opsId, LukiokurssiSaveDto kurssi);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    void updateKurssi(long opsId, long kurssiId, LukiokurssiUpdateDto kurssi);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    long disconnectKurssi(Long kurssiId, Long oppiaineId, Long opsId);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    long reconnectKurssi(Long kurssiId, Long oppiaineId, Long opsId);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    long saveAihekokonaisuus(long opsId, AihekokonaisuusSaveDto kokonaisuus);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    void reArrangeAihekokonaisuudet(long opsId, AihekokonaisuudetJarjestaDto jarjestys);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    void updateAihekokonaisuusYleiskuvaus(long opsId, AihekokonaisuusSaveDto yleiskuvaus);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    void updateAihekokonaisuus(long opsId, long id, AihekokonaisuusSaveDto kokonaisuus);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    void deleteAihekokonaisuus(long opsId, long id);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    void removeKurssi(long opsId, long kurssiId);
}
