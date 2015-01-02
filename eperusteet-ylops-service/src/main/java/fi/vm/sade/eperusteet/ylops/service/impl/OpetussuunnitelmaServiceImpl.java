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

import fi.vm.sade.eperusteet.ylops.domain.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Omistussuhde;
import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappaleViite;
import fi.vm.sade.eperusteet.ylops.dto.OpetussuunnitelmaDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleViiteDto;
import fi.vm.sade.eperusteet.ylops.repository.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.repository.teksti.TekstiKappaleViiteRepository;
import fi.vm.sade.eperusteet.ylops.service.OpetussuunnitelmaService;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.teksti.TekstiKappaleViiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author mikkom
 */
@Service
@Transactional
public class OpetussuunnitelmaServiceImpl implements OpetussuunnitelmaService {

    @Autowired
    private DtoMapper mapper;

    @Autowired
    private OpetussuunnitelmaRepository repository;

    @Autowired
    private TekstiKappaleViiteRepository viiteRepository;

    @Autowired
    private TekstiKappaleViiteService tekstiKappaleViiteService;

    @Override
    @Transactional(readOnly = true)
    public List<OpetussuunnitelmaDto> getAll() {
        List<Opetussuunnitelma> opetussuunnitelmat = repository.findAll();
        return mapper.mapAsList(opetussuunnitelmat, OpetussuunnitelmaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public OpetussuunnitelmaDto getOpetussuunnitelma(@P("id") Long id) {
        Opetussuunnitelma ops = repository.findOne(id);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        return mapper.map(ops, OpetussuunnitelmaDto.class);
    }

    @Override
    public OpetussuunnitelmaDto addOpetussuunnitelma(OpetussuunnitelmaDto opetussuunnitelmaDto) {
        Opetussuunnitelma ops = mapper.map(opetussuunnitelmaDto, Opetussuunnitelma.class);
        lisaaTekstipuunJuuri(ops);
        ops = repository.save(ops);
        return mapper.map(ops, OpetussuunnitelmaDto.class);
    }

    private void lisaaTekstipuunJuuri(Opetussuunnitelma ops) {
        TekstiKappaleViite juuri = new TekstiKappaleViite(Omistussuhde.OMA);
        juuri = viiteRepository.save(juuri);
        ops.setTekstit(juuri);
    }

    @Override
    public OpetussuunnitelmaDto updateOpetussuunnitelma(OpetussuunnitelmaDto opetussuunnitelmaDto) {
        Opetussuunnitelma ops = repository.findOne(opetussuunnitelmaDto.getId());
        assertExists(ops, "Päivitettävää tietoa ei ole olemassa");
        mapper.map(opetussuunnitelmaDto, ops);
        ops = repository.save(ops);
        return mapper.map(ops, OpetussuunnitelmaDto.class);
    }

    @Override
    public void removeOpetussuunnitelma(@P("id") Long id) {
        Opetussuunnitelma ops = repository.findOne(id);
        repository.delete(ops);
    }

    @Override
    public TekstiKappaleViiteDto.Puu getTekstit(@P("opsId") Long opsId) {
        Opetussuunnitelma ops = repository.findOne(opsId);
        assertExists(ops, "Opetussuunnitelmaa ei ole olemassa");
        return mapper.map(ops.getTekstit(), TekstiKappaleViiteDto.Puu.class);
    }

    @Override
    public TekstiKappaleViiteDto.Matala addTekstiKappale(@P("opsId") Long opsId, TekstiKappaleViiteDto.Matala viite) {
        Opetussuunnitelma ops = repository.findOne(opsId);
        assertExists(ops, "Opetussuunnitelmaa ei ole olemassa");

        // Lisätään viite juurinoden alle
        return tekstiKappaleViiteService.addTekstiKappaleViite(opsId, ops.getTekstit().getId(), viite);
    }

    @Override
    public TekstiKappaleViiteDto.Matala addTekstiKappaleLapsi(@P("opsId") Long opsId, Long parentId,
                                                              TekstiKappaleViiteDto.Matala viite) {
        // Lisätään viite parent-noden alle
        return tekstiKappaleViiteService.addTekstiKappaleViite(opsId, parentId, viite);
    }

    private static void assertExists(Object o, String msg) {
        if (o == null) {
            throw new BusinessRuleViolationException(msg);
        }
    }
}