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
package fi.vm.sade.eperusteet.ylops.service.ops.impl;

import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaine;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaineenvuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteOpetuksenkohdealue;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteOppiaine;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteOppiaineenVuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteTekstiOsa;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteVuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.dto.Reference;
import fi.vm.sade.eperusteet.ylops.dto.ops.LaajaalainenosaaminenDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetuksenKohdealueDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineLaajaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineenVuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.VuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiosaDto;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author mikkom
 */
@Component
public class OpsDtoMapper {

    @Autowired
    private DtoMapper mapper;

    public Oppiaine fromDto(OppiaineLaajaDto dto) {
        Oppiaine oppiaine = new Oppiaine(dto.getTunniste());

        mapper.map(dto, oppiaine);
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

        Oppiaine oppiaine = new Oppiaine(dto.getTunniste());
        mapper.map(dto, oppiaine);
        if (dto.getOppimaarat() != null) {
            dto.getOppimaarat().forEach(o -> {
                Oppiaine om = new Oppiaine(o.getTunniste());
                mapper.map(o, om);
                oppiaine.addOppimaara(om);
            });
        }

        if (dto.getVuosiluokkakokonaisuudet() != null) {
            dto.getVuosiluokkakokonaisuudet()
                .forEach(ovk -> oppiaine.addVuosiluokkaKokonaisuus(
                    mapper.map(ovk, Oppiaineenvuosiluokkakokonaisuus.class)));
        }

        return oppiaine;
    }

    public static VuosiluokkakokonaisuusDto fromEperusteet(
        PerusteVuosiluokkakokonaisuus dto) {
        VuosiluokkakokonaisuusDto vk = new VuosiluokkakokonaisuusDto(new Reference(dto.getTunniste().toString()));
        vk.setNimi(Optional.ofNullable(dto.getNimi()));
        vk.setTunniste(Optional.ofNullable(Reference.of(dto.getTunniste())));
        if (dto.getLaajaalaisetOsaamiset() != null) {
            vk.setLaajaalaisetosaamiset(new HashSet<>());
            dto.getLaajaalaisetOsaamiset().forEach(lo -> {
                LaajaalainenosaaminenDto l = new LaajaalainenosaaminenDto();
                l.setLaajaalainenosaaminen(Reference.of(lo.getLaajaalainenOsaaminen().getTunniste()));
                vk.getLaajaalaisetosaamiset().add(l);
            });
        }
        return vk;
    }

    public static OppiaineDto oppimaaraFromEperusteet(
        PerusteOppiaine oa) {
        OppiaineDto dto = new OppiaineDto();
        dto.setTila(Tila.LUONNOS);

        dto.setNimi(oa.getNimi());
        dto.setTunniste(oa.getTunniste());
        dto.setKoosteinen(oa.getKoosteinen());
        dto.setKoodiArvo(oa.getKoodiArvo());
        dto.setKoodiUri(oa.getKoodiUri());

        Set<OpetuksenKohdealueDto> kohdealueet
            = oa.getKohdealueet().stream()
            .map(OpsDtoMapper::fromEperusteet)
            .collect(Collectors.toSet());
        dto.setKohdealueet(kohdealueet);

        // Ollaan oppimäärässä, täällä ei pitäisi enää olla alioppimääriä
        assert (!oa.getKoosteinen());

        if (oa.getVuosiluokkakokonaisuudet() != null) {
            dto.setVuosiluokkakokonaisuudet(
                oa.getVuosiluokkakokonaisuudet().stream()
                .map(OpsDtoMapper::fromEperusteet)
                .collect(Collectors.toSet()));
        }

        return dto;
    }

    public static OppiaineLaajaDto fromEperusteet(
        PerusteOppiaine oa) {
        OppiaineLaajaDto dto = new OppiaineLaajaDto();
        dto.setTila(Tila.LUONNOS);

        dto.setNimi(oa.getNimi());
        dto.setKoosteinen(oa.getKoosteinen());
        dto.setTunniste(oa.getTunniste());
        dto.setKoodiArvo(oa.getKoodiArvo());
        dto.setKoodiUri(oa.getKoodiUri());

        dto.setKohdealueet(
            oa.getKohdealueet().stream()
            .map(OpsDtoMapper::fromEperusteet)
            .collect(Collectors.toSet()));

        if (oa.getOppimaarat() != null) {
            dto.setOppimaarat(
                oa.getOppimaarat().stream()
                .map(OpsDtoMapper::oppimaaraFromEperusteet)
                .collect(Collectors.toSet()));
        }

        if (oa.getVuosiluokkakokonaisuudet() != null) {
            dto.setVuosiluokkakokonaisuudet(
                oa.getVuosiluokkakokonaisuudet().stream()
                .map(OpsDtoMapper::fromEperusteet)
                .collect(Collectors.toSet()));
        }

        return dto;
    }

    public static OppiaineenVuosiluokkakokonaisuusDto fromEperusteet(PerusteOppiaineenVuosiluokkakokonaisuus ovk) {
        OppiaineenVuosiluokkakokonaisuusDto dto = new OppiaineenVuosiluokkakokonaisuusDto();
        dto.setTehtava(new TekstiosaDto());
        dto.setTyotavat(new TekstiosaDto());
        dto.setOhjaus(new TekstiosaDto());
        dto.setArviointi(new TekstiosaDto());

        dto.setVuosiluokkakokonaisuus(Reference.of(ovk.getVuosiluokkaKokonaisuus().getTunniste()));

        return dto;
    }

    public static OpetuksenKohdealueDto fromEperusteet(
        PerusteOpetuksenkohdealue dto) {
        return new OpetuksenKohdealueDto(dto.getNimi());
    }
}
