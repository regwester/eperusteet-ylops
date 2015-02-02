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

import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.vuosiluokkakokonaisuus.Vuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.dto.ops.VuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.VuosiluokkakokonaisuusRepository;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.VuosiluokkakokonaisuusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jhyoty
 */
@Service
@Transactional(readOnly = true)
public class VuosiluokkakokonaisuusServiceImpl implements VuosiluokkakokonaisuusService {

    @Autowired
    private OpetussuunnitelmaRepository suunnitelmat;

    @Autowired
    private VuosiluokkakokonaisuusRepository kokonaisuudet;

    @Autowired
    private DtoMapper mapper;

    @Override
    public VuosiluokkakokonaisuusDto add(Long opsId, VuosiluokkakokonaisuusDto dto) {
        Opetussuunnitelma ops = suunnitelmat.findOne(opsId);
        if (ops == null) {
            throw new BusinessRuleViolationException("Opetussuunnitelmaa ei löydy");
        }
        Vuosiluokkakokonaisuus vk;
        if (dto.getId() != null) {
            vk = kokonaisuudet.findOne(opsId);
            ops.attachVuosiluokkaKokonaisuus(vk);
            //TODO: tarkista lukuoikeus
        } else {
            vk = mapper.map(dto, Vuosiluokkakokonaisuus.class);
            vk = kokonaisuudet.save(vk);
            ops.addVuosiluokkaKokonaisuus(vk);
        }
        return mapper.map(vk, VuosiluokkakokonaisuusDto.class);
    }

    @Override
    public VuosiluokkakokonaisuusDto get(Long opsId, Long kokonaisuusId) {
        final Vuosiluokkakokonaisuus vk = kokonaisuudet.findBy(opsId, kokonaisuusId);
        return vk == null ? null : mapper.map(vk, VuosiluokkakokonaisuusDto.class);
    }

    @Override
    public void delete(Long opsId, Long kokonaisuusId) {
        Vuosiluokkakokonaisuus vk = kokonaisuudet.findBy(opsId, kokonaisuusId);
        if (vk != null) {
            Opetussuunnitelma ops = suunnitelmat.findOne(opsId);
            ops.removeVuosiluokkakokonaisuus(vk);
            if (!kokonaisuudet.isInUse(kokonaisuusId)) {
                kokonaisuudet.delete(vk);
            }
        }
    }

    @Override
    public VuosiluokkakokonaisuusDto update(Long opsId, VuosiluokkakokonaisuusDto dto) {
        final Vuosiluokkakokonaisuus vk = kokonaisuudet.findBy(opsId, dto.getId());
        if (vk == null) {
            throw new BusinessRuleViolationException("Päivitettävää vuosiluokkakokonaisuutta ei ole olemassa");
        }
        mapper.map(dto, vk);
        return mapper.map(vk, VuosiluokkakokonaisuusDto.class);
    }

}
