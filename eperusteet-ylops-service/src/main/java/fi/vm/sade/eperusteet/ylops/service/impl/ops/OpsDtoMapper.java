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
package fi.vm.sade.eperusteet.ylops.service.impl.ops;

import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaine;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaineenvuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.dto.EntityReference;
import fi.vm.sade.eperusteet.ylops.dto.eperusteet.OppiaineenVuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetuksenKohdealueDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineLaajaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineenVuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.VuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiosaDto;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 * @author mikkom
 */
@Component
public class OpsDtoMapper {

    @Autowired
    private DtoMapper mapper;

    public Oppiaine fromDto(OppiaineLaajaDto dto) {
        Oppiaine oppiaine = mapper.map(dto, Oppiaine.class);
        if (dto.getOppimaarat() != null) {
            dto.getOppimaarat().forEach(o -> oppiaine.addOppimaara(fromDto(o)));
        }

        if (dto.getVuosiluokkakokonaisuudet() != null) {
            dto.getVuosiluokkakokonaisuudet()
               .forEach(ovk -> oppiaine.addVuosiluokkaKokonaisuus(
                   mapper.map(ovk, Oppiaineenvuosiluokkakokonaisuus.class)));
        }

        return oppiaine;
    }

    public Oppiaine fromDto(OppiaineDto dto) {
        Oppiaine oppiaine = mapper.map(dto, Oppiaine.class);
        if (dto.getOppimaarat() != null) {
            dto.getOppimaarat().forEach(o -> oppiaine.addOppimaara(mapper.map(o, Oppiaine.class)));
        }

        if (dto.getVuosiluokkakokonaisuudet() != null) {
            dto.getVuosiluokkakokonaisuudet()
               .forEach(ovk -> oppiaine.addVuosiluokkaKokonaisuus(
                   mapper.map(ovk, Oppiaineenvuosiluokkakokonaisuus.class)));
        }

        return oppiaine;
    }

    public static VuosiluokkakokonaisuusDto fromEperusteet(
        fi.vm.sade.eperusteet.ylops.dto.eperusteet.VuosiluokkakokonaisuusDto dto) {
        VuosiluokkakokonaisuusDto vk = new VuosiluokkakokonaisuusDto(new EntityReference(dto.getTunniste().toString()));
        vk.setNimi(Optional.ofNullable(dto.getNimi()));
        //TODO. laaja-alaiset osaamiset
        return vk;
    }

    public static OppiaineDto fromEperusteet(
        fi.vm.sade.eperusteet.ylops.dto.eperusteet.OppiaineDto oa,
        Map<EntityReference, UUID> vuosiluokkaMap) {
        OppiaineDto dto = new OppiaineDto();
        dto.setTila(Tila.LUONNOS);

        dto.setNimi(oa.getNimi());
        dto.setKoosteinen(oa.getKoosteinen());
        dto.setTehtava(fromEperusteet(oa.getTehtava()));
        dto.setKoodiArvo(oa.getKoodiArvo());
        dto.setKoodiUri(oa.getKoodiUri());

        Set<OpetuksenKohdealueDto> kohdealueet =
            oa.getKohdealueet().stream()
              .map(OpsDtoMapper::fromEperusteet)
              .collect(Collectors.toSet());
        dto.setKohdealueet(kohdealueet);

        // Ollaan oppimäärässä, täällä ei pitäisi enää olla alioppimääriä
        assert(!oa.getKoosteinen());

        if (oa.getVuosiluokkakokonaisuudet() != null) {
            dto.setVuosiluokkakokonaisuudet(
                oa.getVuosiluokkakokonaisuudet().stream()
                  .map(oaVlk -> fromEperusteet(oaVlk, vuosiluokkaMap))
                  .collect(Collectors.toSet()));
        }

        return dto;
    }

    public static OppiaineLaajaDto fromEperusteet(
        fi.vm.sade.eperusteet.ylops.dto.eperusteet.OppiaineLaajaDto oa,
        Map<EntityReference, UUID> vuosiluokkaMap) {
        OppiaineLaajaDto dto = new OppiaineLaajaDto();
        dto.setTila(Tila.LUONNOS);

        dto.setNimi(oa.getNimi());
        dto.setKoosteinen(oa.getKoosteinen());
        dto.setTehtava(fromEperusteet(oa.getTehtava()));
        dto.setKoodiArvo(oa.getKoodiArvo());
        dto.setKoodiUri(oa.getKoodiUri());

        dto.setKohdealueet(
            oa.getKohdealueet().stream()
              .map(OpsDtoMapper::fromEperusteet)
              .collect(Collectors.toSet()));

        if (oa.getOppimaarat() != null) {
            dto.setOppimaarat(
                oa.getOppimaarat().stream()
                  .map(om -> fromEperusteet(om, vuosiluokkaMap))
                  .collect(Collectors.toSet()));
        }

        if (oa.getVuosiluokkakokonaisuudet() != null) {
            dto.setVuosiluokkakokonaisuudet(
                oa.getVuosiluokkakokonaisuudet().stream()
                  .map(oaVlk -> fromEperusteet(oaVlk, vuosiluokkaMap))
                  .collect(Collectors.toSet()));
        }

        return dto;
    }

    public static OppiaineenVuosiluokkakokonaisuusDto fromEperusteet(OppiaineenVuosiluokkaKokonaisuusDto ovk,
                                                                     Map<EntityReference, UUID> vuosiluokkaMap) {
        OppiaineenVuosiluokkakokonaisuusDto dto = new OppiaineenVuosiluokkakokonaisuusDto();
        dto.setTehtava(fromEperusteet(ovk.getTehtava()));
        dto.setTyotavat(fromEperusteet(ovk.getTyotavat()));
        dto.setOhjaus(fromEperusteet(ovk.getOhjaus()));
        dto.setArviointi(fromEperusteet(ovk.getArviointi()));

        EntityReference vlkRef = ovk.getVuosiluokkaKokonaisuus();
        UUID vlkTunniste = vuosiluokkaMap.get(vlkRef);
        dto.setVuosiluokkakokonaisuus(new EntityReference(vlkTunniste));

        return dto;
    }

    public static TekstiosaDto fromEperusteet(fi.vm.sade.eperusteet.ylops.dto.eperusteet.TekstiOsaDto dto) {
        if (dto == null) {
            return null;
        }
        LokalisoituTekstiDto otsikko = dto.getOtsikko();
        LokalisoituTekstiDto teksti = dto.getTeksti();
        return new TekstiosaDto(Optional.ofNullable(otsikko),
                                Optional.ofNullable(teksti));
    }

    public static OpetuksenKohdealueDto fromEperusteet(
        fi.vm.sade.eperusteet.ylops.dto.eperusteet.OpetuksenKohdealueDto dto) {
        return new OpetuksenKohdealueDto(dto.getNimi());
    }
}
