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
package fi.vm.sade.eperusteet.ylops.service.impl.teksti;

import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Omistussuhde;
import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappale;
import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappaleViite;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleViiteDto;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.repository.teksti.TekstiKappaleRepository;
import fi.vm.sade.eperusteet.ylops.repository.teksti.TekstiKappaleViiteRepository;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.teksti.TekstiKappaleService;
import fi.vm.sade.eperusteet.ylops.service.teksti.TekstiKappaleViiteService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mikkom
 */
@Service
@Transactional(readOnly = true)
public class TekstiKappaleViiteServiceImpl implements TekstiKappaleViiteService {

    @Autowired
    private DtoMapper mapper;

    @Autowired
    private OpetussuunnitelmaRepository opetussuunnitelmaRepository;

    @Autowired
    private TekstiKappaleViiteRepository repository;

    @Autowired
    private TekstiKappaleRepository tekstiKappaleRepository;

    @Autowired
    private TekstiKappaleService tekstiKappaleService;

    @Override
    public TekstiKappaleViiteDto.Matala getTekstiKappaleViite(@P("opsId") Long opsId, Long viiteId) {
        TekstiKappaleViite viite = findViite(opsId, viiteId);
        return mapper.map(viite, TekstiKappaleViiteDto.Matala.class);
    }

    @Override
    @Transactional(readOnly = false)
    public TekstiKappaleViiteDto.Matala addTekstiKappaleViite(@P("opsId") Long opsId, Long parentViiteId,
        TekstiKappaleViiteDto.Matala viiteDto) {
        TekstiKappaleViite parentViite = findViite(opsId, parentViiteId);

        TekstiKappaleViite uusiViite = new TekstiKappaleViite(Omistussuhde.OMA);
        repository.lock(parentViite.getRoot());

        List<TekstiKappaleViite> lapset = parentViite.getLapset();
        if (lapset == null) {
            lapset = new ArrayList<>();
            parentViite.setLapset(lapset);
        }
        lapset.add(uusiViite);
        uusiViite.setVanhempi(parentViite);
        uusiViite = repository.save(uusiViite);

        if (viiteDto == null || (viiteDto.getTekstiKappaleRef() == null && viiteDto.getTekstiKappale() == null)) {
            // Luodaan kokonaan uusi tekstikappale
            TekstiKappale uusiKappale = new TekstiKappale();
            uusiKappale = tekstiKappaleRepository.save(uusiKappale);
            uusiViite.setTekstiKappale(uusiKappale);
        } else {
            // Viittessä on mukana tekstikappale ja/tai lapsiviitteet
            TekstiKappaleViite viiteEntity = mapper.map(viiteDto, TekstiKappaleViite.class);
            uusiViite.setLapset(viiteEntity.getLapset());

            if (viiteDto.getTekstiKappaleRef() != null) {
                // TODO: Lisää tähän tekstikappaleiden lukuoikeuden tarkistelu
                uusiViite.setTekstiKappale(viiteEntity.getTekstiKappale());
            } else if (viiteDto.getTekstiKappale() != null) {
                tekstiKappaleService.add(uusiViite, viiteDto.getTekstiKappale());
            }
        }

        repository.flush();
        return mapper.map(uusiViite, TekstiKappaleViiteDto.Matala.class);
    }

    @Override
    @Transactional(readOnly = false)
    public void updateTekstiKappaleViite(@P("opsId") Long opsId, Long rootViiteId, TekstiKappaleViiteDto.Puu uusi) {
        TekstiKappaleViite viite = findViite(opsId, rootViiteId);
        repository.lock(viite.getRoot());
        updateTekstiKappale(viite, uusi);
    }

    @Override
    @Transactional(readOnly = false)
    public void reorderSubTree(@P("opsId") Long opsId, Long rootViiteId, TekstiKappaleViiteDto.Puu uusi) {
        TekstiKappaleViite viite = findViite(opsId, rootViiteId);
        repository.lock(viite.getRoot());
        Set<TekstiKappaleViite> refs = Collections.newSetFromMap(new IdentityHashMap<TekstiKappaleViite, Boolean>());
        refs.add(viite);
        TekstiKappaleViite parent = viite.getVanhempi();
        clearChildren(viite, refs);
        updateTraverse(parent, uusi, refs);
    }

    @Override
    @Transactional(readOnly = false)
    public void removeTekstiKappaleViite(@P("opsId") Long opsId, Long viiteId) {
        TekstiKappaleViite viite = findViite(opsId, viiteId);

        if (viite.getVanhempi() == null) {
            throw new BusinessRuleViolationException("Sisällön juurielementtiä ei voi poistaa");
        }

        if (viite.getLapset() != null && !viite.getLapset().isEmpty()) {
            throw new BusinessRuleViolationException("Sisällöllä on lapsia, ei voida poistaa");
        }

        if (viite.getTekstiKappale() != null && viite.getTekstiKappale().getTila().equals(Tila.LUONNOS) && findViitteet(opsId, viiteId).size() == 1) {
            TekstiKappale tekstiKappale = viite.getTekstiKappale();
            tekstiKappaleService.delete(tekstiKappale.getId());
        }
        viite.setTekstiKappale(null);
        viite.getVanhempi().getLapset().remove(viite);
        viite.setVanhempi(null);
        repository.delete(viite);
    }

    @Override
    @Transactional(readOnly = false)
    public TekstiKappaleViiteDto.Puu kloonaaTekstiKappale(@P("opsId") Long opsId, Long viiteId) {
        TekstiKappaleViite viite = findViite(opsId, viiteId);
        TekstiKappale originaali = viite.getTekstiKappale();
        // TODO: Tarkista onko tekstikappaleeseen lukuoikeutta
        TekstiKappale klooni = originaali.copy();
        klooni.setTila(Tila.LUONNOS);
        viite.setTekstiKappale(tekstiKappaleRepository.save(klooni));
        viite.setOmistussuhde(Omistussuhde.OMA);
        viite = repository.save(viite);
        return mapper.map(viite, TekstiKappaleViiteDto.Puu.class);
    }

    private List<TekstiKappaleViite> findViitteet(Long opsId, Long viiteId) {
        TekstiKappaleViite viite = findViite(opsId, viiteId);
        return repository.findAllByTekstiKappale(viite.getTekstiKappale());
    }

    private TekstiKappaleViite findViite(Long opsId, Long viiteId) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Opetussuunnitelmaa ei ole olemassa");
        TekstiKappaleViite viite = repository.findOne(viiteId);
        assertExists(viite, "Tekstikappaleviitettä ei ole olemassa");
        if (!ops.containsViite(viite)) {
            throw new BusinessRuleViolationException("Annettu tekstikappaleviite ei kuulu tähän opetussuunnitelmaan");
        }
        return viite;
    }

    private void clearChildren(TekstiKappaleViite viite, Set<TekstiKappaleViite> refs) {
        for (TekstiKappaleViite lapsi : viite.getLapset()) {
            refs.add(lapsi);
            clearChildren(lapsi, refs);
        }
        viite.setVanhempi(null);
        viite.getLapset().clear();
    }

    private void updateTekstiKappale(TekstiKappaleViite viite, TekstiKappaleViiteDto uusi) {
        if (uusi.getTekstiKappale() != null) {
            if (viite.getOmistussuhde() == Omistussuhde.OMA) {
                tekstiKappaleService.update(uusi.getTekstiKappale());
            } else {
                TekstiKappale vanha = viite.getTekstiKappale();
                tekstiKappaleService.mergeNew(viite, uusi.getTekstiKappale());

                // Poista vanha tekstikappale jos siihen ei tämän jälkeen enää löydy viittauksia
                if (repository.findAllByTekstiKappale(vanha).size() == 0) {
                    tekstiKappaleService.delete(vanha.getId());
                }
            }
        }
    }

    private TekstiKappaleViite updateTraverse(TekstiKappaleViite parent, TekstiKappaleViiteDto.Puu uusi,
        Set<TekstiKappaleViite> refs) {
        TekstiKappaleViite viite = repository.getOne(uusi.getId());
        if (!refs.remove(viite)) {
            throw new BusinessRuleViolationException("Viitepuun päivitysvirhe, annettua alipuun juuren viitettä ei löydy");
        }
        viite.setVanhempi(parent);

        List<TekstiKappaleViite> lapset = viite.getLapset();
        lapset.clear();

        // Päivitä myös tekstikappale jos DTO sen sisältää
        updateTekstiKappale(viite, uusi);

        if (uusi.getLapset() != null) {
            lapset.addAll(uusi.getLapset()
                .stream()
                .map(elem -> updateTraverse(viite, elem, refs))
                .collect(Collectors.toList()));
        }
        return repository.save(viite);
    }

    private static void assertExists(Object o, String msg) {
        if (o == null) {
            throw new BusinessRuleViolationException(msg);
        }
    }
}
