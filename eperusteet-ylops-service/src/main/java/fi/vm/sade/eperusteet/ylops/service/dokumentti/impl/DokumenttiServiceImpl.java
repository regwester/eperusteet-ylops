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

package fi.vm.sade.eperusteet.ylops.service.dokumentti.impl;

import fi.vm.sade.eperusteet.ylops.domain.dokumentti.Dokumentti;
import fi.vm.sade.eperusteet.ylops.domain.dokumentti.DokumenttiTila;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.dto.dokumentti.DokumenttiDto;
import fi.vm.sade.eperusteet.ylops.repository.dokumentti.DokumenttiRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.DokumenttiBuilderService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.DokumenttiService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

/**
 *
 * @author iSaul
 */
@Service
public class DokumenttiServiceImpl implements DokumenttiService {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiServiceImpl.class);

    @Autowired
    private DokumenttiRepository dokumenttiRepository;

    @Autowired
    private DtoMapper mapper;

    @Autowired
    private OpetussuunnitelmaRepository opetussuunnitelmaRepository;

    @Autowired
    private DokumenttiBuilderService builder;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public DokumenttiDto getDto(@P("id") long opsId, Kieli kieli) {
        List<Dokumentti> dokumentit = dokumenttiRepository.findByOpsIdAndKieli(opsId, kieli);

        // Jos löytyy
        if (!dokumentit.isEmpty()) {
            return mapper.map(dokumentit.get(0), DokumenttiDto.class);
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public DokumenttiDto createDtoFor(@P("id") long id, Kieli kieli) {
        String name = SecurityUtil.getAuthenticatedPrincipal().getName();
        Dokumentti dokumentti = new Dokumentti();
        dokumentti.setTila(DokumenttiTila.EI_OLE);
        dokumentti.setAloitusaika(new Date());
        dokumentti.setLuoja(name);
        dokumentti.setKieli(kieli);
        dokumentti.setOpsId(id);

        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(id);
        if (ops != null) {
            Dokumentti saved = dokumenttiRepository.save(dokumentti);
            return mapper.map(saved, DokumenttiDto.class);
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    public void setStarted(@P("dto") DokumenttiDto dto) {
        // Asetetaan dokumentti luonti tilaan
        String name = SecurityUtil.getAuthenticatedPrincipal().getName();
        Dokumentti dokumentti = dokumenttiRepository.findById(dto.getId());
        dokumentti.setAloitusaika(new Date());
        dokumentti.setLuoja(name);
        dokumentti.setTila(DokumenttiTila.LUODAAN);
        dokumenttiRepository.save(dokumentti);
    }

    @Override
    @Transactional
    public void generateWithDto(@P("dto") DokumenttiDto dto) {
        Dokumentti dokumentti = dokumenttiRepository.findById(dto.getId());
        Opetussuunnitelma opetussuunnitelma = opetussuunnitelmaRepository.findOne(dokumentti.getOpsId());
        Kieli kieli = dokumentti.getKieli();

        try {
            // Luodaan pdf
            byte[] data = builder.generatePdf(opetussuunnitelma, kieli);

            dokumentti.setData(data);
            dokumentti.setTila(DokumenttiTila.VALMIS);
            dokumentti.setValmistumisaika(new Date());
            dokumentti.setVirhekoodi("");

            // Tallennetaan valmis dokumentti
            dokumenttiRepository.save(dokumentti);
        } catch (Exception ex) {
            dokumentti.setTila(DokumenttiTila.EPAONNISTUI);

            String virhekoodiBuilder = "internal error - " + new Date().toString();

            dokumentti.setVirhekoodi(virhekoodiBuilder);
            dokumenttiRepository.save(dokumentti);

            LOG.error(ex.getLocalizedMessage(), ex.getCause());
            ex.printStackTrace();

            //throw new DokumenttiException(ex.getLocalizedMessage(), ex.getCause());
        }
    }

    @Override
    @Transactional
    public DokumenttiDto getDto(@P("id") long id) {
        Dokumentti dokumentti = dokumenttiRepository.findById(id);
        return mapper.map(dokumentti, DokumenttiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] get(Long id) {
        Dokumentti dokumentti = dokumenttiRepository.findById(id);
        if (dokumentti != null) {
            return dokumentti.getData();
        } else {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DokumenttiDto query(Long id) {
        Dokumentti findById = dokumenttiRepository.findById(id);
        return mapper.map(findById, DokumenttiDto.class);
    }
}
