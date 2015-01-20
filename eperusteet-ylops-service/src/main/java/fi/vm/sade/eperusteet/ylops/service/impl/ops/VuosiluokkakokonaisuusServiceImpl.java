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
import fi.vm.sade.eperusteet.ylops.domain.ops.OpsVuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.domain.vuosiluokkakokonaisuus.Vuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.dto.ops.VuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.repository.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.VuosiluokkakokonaisuusRepository;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.VuosiluokkakokonaisuusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author jhyoty
 */
@Service
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
        Vuosiluokkakokonaisuus vk;
        if (dto.getId() != null) {
            //TODO: implement
            throw new UnsupportedOperationException("vuosiluokkakokonaisuuden liittäminen ei ole tuettu");
        } else {
            vk = mapper.map(dto, Vuosiluokkakokonaisuus.class);
        }
        OpsVuosiluokkakokonaisuus ovk = new OpsVuosiluokkakokonaisuus(ops, vk, true);
        ovk = kokonaisuudet.save(ovk);
        return mapper.map(ovk.getVuosiluokkakokonaisuus(), VuosiluokkakokonaisuusDto.class);
    }

    @Override
    public VuosiluokkakokonaisuusDto get(Long opsId, Long kokonaisuusId) {
        final OpsVuosiluokkakokonaisuus ovk = kokonaisuudet.findBy(opsId, kokonaisuusId);
        return mapper.map(ovk.getVuosiluokkakokonaisuus(), VuosiluokkakokonaisuusDto.class);
    }

    @Override
    public void delete(Long opsId, Long kokonaisuusId) {
        OpsVuosiluokkakokonaisuus ovk = kokonaisuudet.findBy(opsId, kokonaisuusId);
        if (ovk != null) {
            kokonaisuudet.delete(ovk);
        }
    }

    @Override
    public VuosiluokkakokonaisuusDto update(Long opsId, VuosiluokkakokonaisuusDto dto) {
        final OpsVuosiluokkakokonaisuus ovk = kokonaisuudet.findBy(opsId, dto.getId());
        if (ovk == null) {
            throw new BusinessRuleViolationException("Päivitettävää vuosiluokkakokonaisuutta ei ole olemassa");
        }
        mapper.map(dto, ovk.getVuosiluokkakokonaisuus());
        return mapper.map(ovk.getVuosiluokkakokonaisuus(), VuosiluokkakokonaisuusDto.class);
    }

}
