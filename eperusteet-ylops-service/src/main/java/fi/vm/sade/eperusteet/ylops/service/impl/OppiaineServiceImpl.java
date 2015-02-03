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
package fi.vm.sade.eperusteet.ylops.service.impl;

import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaine;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.OppiaineRepository;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.OppiaineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * @author mikkom
 */
@Service
@Transactional
public class OppiaineServiceImpl implements OppiaineService {

    @Autowired
    private DtoMapper mapper;

    @Autowired
    private OpetussuunnitelmaRepository opetussuunnitelmaRepository;

    @Autowired
    private OppiaineRepository oppiaineRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OppiaineDto> getAll(@P("opsId") Long opsId) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        return mapper.mapAsList(oppiaineRepository.findByOpsId(opsId), OppiaineDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public OppiaineDto get(@P("opsId") Long opsId, Long id) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        Oppiaine oppiaine = oppiaineRepository.findOne(id);
        assertExists(ops, "Pyydettyä oppiainetta ei ole olemassa");
        if (!ops.containsOppiaine(oppiaine)) {
            throw new BusinessRuleViolationException("Pyydettyä oppiainetta ei ole opetussuunnitelmassa");
        }

        return mapper.map(oppiaine, OppiaineDto.class);
    }

    @Override
    public OppiaineDto add(@P("opsId") Long opsId, OppiaineDto oppiaineDto) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        opetussuunnitelmaRepository.lock(ops);
        Oppiaine oppiaine = saveOppiaine(oppiaineDto);
        ops.addOppiaine(oppiaine);
        return mapper.map(oppiaine, OppiaineDto.class);
    }

    private Oppiaine saveOppiaine(OppiaineDto oppiaineDto) {
        Oppiaine oppiaine = fromDto(oppiaineDto);
        return oppiaineRepository.save(oppiaine);

    }

    private Oppiaine fromDto(OppiaineDto dto) {
        Oppiaine mappedOppiaine = mapper.map(dto, Oppiaine.class);
        Oppiaine oppiaine = new Oppiaine();
        oppiaine.setId(mappedOppiaine.getId());
        oppiaine.setNimi(mappedOppiaine.getNimi());
        oppiaine.setTehtava(mappedOppiaine.getTehtava());
        oppiaine.setKoodi(mappedOppiaine.getKoodi());
        oppiaine.setKoosteinen(mappedOppiaine.isKoosteinen());
        oppiaine.setKohdealueet(mappedOppiaine.getKohdealueet());

        if (dto.getOppimaarat() != null) {
            for (OppiaineSuppeaDto oppimaaraDto : dto.getOppimaarat()) {
                Oppiaine oppimaara = fromDto(oppimaaraDto);
                oppiaine.addOppimaara(oppimaara);
            }
        }

        return oppiaine;
    }

    private Oppiaine fromDto(OppiaineSuppeaDto dto) {
        Oppiaine mappedOppiaine = mapper.map(dto, Oppiaine.class);
        Oppiaine oppiaine = new Oppiaine();
        oppiaine.setId(mappedOppiaine.getId());
        oppiaine.setNimi(mappedOppiaine.getNimi());
        oppiaine.setTehtava(mappedOppiaine.getTehtava());
        oppiaine.setKoodi(mappedOppiaine.getKoodi());
        oppiaine.setKoosteinen(mappedOppiaine.isKoosteinen());
        oppiaine.setKohdealueet(mappedOppiaine.getKohdealueet());

        return oppiaine;
    }

    @Override
    public OppiaineDto update(@P("opsId") Long opsId, OppiaineDto oppiaineDto) {
        Oppiaine oppiaine = oppiaineRepository.findOne(oppiaineDto.getId());
        assertExists(oppiaine, "Pyydettyä oppiainetta ei ole olemassa");

        // lockService.assertLock ( opsId ) ... ?
        oppiaineRepository.lock(oppiaine);

        mapper.map(oppiaineDto, oppiaine);

        oppiaine = oppiaineRepository.save(oppiaine);
        return mapper.map(oppiaine, OppiaineDto.class);
    }

    @Override
    public void delete(@P("opsId") Long opsId, Long id) {
        Oppiaine oppiaine = oppiaineRepository.findOne(id);
        assertExists(oppiaine, "Pyydettyä oppiainetta ei ole olemassa");
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        if (!ops.containsOppiaine(oppiaine)) {
            throw new BusinessRuleViolationException("Pyydettyä oppiainetta ei ole opetussuunnitelmassa");
        }

        oppiaineRepository.lock(oppiaine);

        if (oppiaine.isKoosteinen()) {
            // Lukitukset...
            oppiaine.getOppimaarat().forEach(oppimaara -> delete(opsId, oppimaara.getId()));
        }

        if (oppiaine.getOppiaine() != null) {
            oppiaine.getOppiaine().removeOppimaara(oppiaine);
        } else {
            ops.removeOppiaine(oppiaine);
        }

        oppiaineRepository.delete(oppiaine);
    }

    private static void assertExists(Object o, String msg) {
        if (o == null) {
            throw new BusinessRuleViolationException(msg);
        }
    }
}
