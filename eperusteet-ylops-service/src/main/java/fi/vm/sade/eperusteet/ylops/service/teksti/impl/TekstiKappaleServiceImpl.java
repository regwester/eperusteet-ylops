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
package fi.vm.sade.eperusteet.ylops.service.teksti.impl;

import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Omistussuhde;
import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappale;
import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappaleViite;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleDto;
import fi.vm.sade.eperusteet.ylops.repository.teksti.TekstiKappaleRepository;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.teksti.TekstiKappaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author mikkom
 */
@Service
@Transactional
public class TekstiKappaleServiceImpl implements TekstiKappaleService {

    @Autowired
    private DtoMapper mapper;

    @Autowired
    private TekstiKappaleRepository repository;

    @Override
    @Transactional(readOnly = true)
    public TekstiKappaleDto get(Long id) {
        TekstiKappale tekstiKappale = repository.getOne(id);
        assertExists(tekstiKappale, "Pyydettyä tekstikappaletta ei ole olemassa");
        return mapper.map(tekstiKappale, TekstiKappaleDto.class);
    }

    @Override
    public TekstiKappaleDto add(TekstiKappaleViite viite, TekstiKappaleDto tekstiKappaleDto) {
        TekstiKappale tekstiKappale = mapper.map(tekstiKappaleDto, TekstiKappale.class);
        tekstiKappale.setTila(Tila.LUONNOS);
        viite.setTekstiKappale(tekstiKappale);
        tekstiKappale = repository.saveAndFlush(tekstiKappale);
        mapper.map(tekstiKappale, tekstiKappaleDto);
        return tekstiKappaleDto;
    }

    @Override
    public TekstiKappaleDto update(TekstiKappaleDto tekstiKappaleDto) {
        Long id = tekstiKappaleDto.getId();
        assertExists(id);
        TekstiKappale current = repository.findOne(id);
        repository.lock(current);
        mapper.map(tekstiKappaleDto, current);
        current = repository.save(current);
        mapper.map(current, tekstiKappaleDto);
        return tekstiKappaleDto;
    }

    @Override
    public TekstiKappaleDto mergeNew(TekstiKappaleViite viite, TekstiKappaleDto tekstiKappaleDto) {
        assertExists(viite.getTekstiKappale(), "Viitteellä ei ole tekstikappaletta");
        Long id = viite.getTekstiKappale().getId();
        assertExists(id);

        TekstiKappale clone = repository.findOne(id).copy();
        mapper.map(tekstiKappaleDto, clone);
        clone = repository.save(clone);

        viite.setTekstiKappale(clone);
        viite.setOmistussuhde(Omistussuhde.OMA);

        mapper.map(clone, tekstiKappaleDto);
        return tekstiKappaleDto;
    }

    @Override
    public void delete(Long id) {
        repository.delete(id);
    }

    private void assertExists(Long id) {
        if (!repository.exists(id)) {
            throw new BusinessRuleViolationException("Pyydettyä tekstikappaletta ei ole olemassa");
        }
    }

    private static void assertExists(Object o, String msg) {
        if (o == null) {
            throw new BusinessRuleViolationException(msg);
        }
    }
}
