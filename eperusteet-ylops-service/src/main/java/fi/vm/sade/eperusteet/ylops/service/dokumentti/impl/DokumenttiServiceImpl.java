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

import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.dokumentti.Dokumentti;
import fi.vm.sade.eperusteet.ylops.domain.dokumentti.DokumenttiTila;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.dto.dokumentti.DokumenttiDto;
import fi.vm.sade.eperusteet.ylops.repository.dokumentti.DokumenttiRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.DokumenttiBuilderService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.DokumenttiService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.DokumenttiStateService;
import fi.vm.sade.eperusteet.ylops.service.exception.DokumenttiException;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.util.SecurityUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
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

    @Autowired
    private DokumenttiStateService dokumenttiStateService;

    @Override
    @Transactional(readOnly = true)
    public DokumenttiDto getDto(Long opsId, Kieli kieli) {
        List<Dokumentti> dokumentit = dokumenttiRepository.findByOpsIdAndKieli(opsId, kieli);

        // Jos löytyy
        if (!dokumentit.isEmpty()) {
            dokumentit.sort((a, b) -> new Long(a.getId()).compareTo(b.getId()));
            return mapper.map(dokumentit.get(0), DokumenttiDto.class);
        }

        return null;
    }

    @Override
    @Transactional
    public DokumenttiDto createDtoFor(Long id, Kieli kieli) {
        Dokumentti dokumentti = new Dokumentti();
        dokumentti.setTila(DokumenttiTila.EI_OLE);
        dokumentti.setAloitusaika(new Date());
        dokumentti.setLuoja(SecurityUtil.getAuthenticatedPrincipal().getName());
        dokumentti.setKieli(kieli);
        dokumentti.setOpsId(id);

        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(id);
        if (ops != null) {
            Dokumentti saved = dokumenttiRepository.save(dokumentti);

            return mapper.map(saved, DokumenttiDto.class);
        }

        return null;
    }

    @Override
    @Transactional(noRollbackFor = DokumenttiException.class)
    @Async(value = "docTaskExecutor")
    public void autogenerate(Long id, Kieli kieli) throws DokumenttiException {
        Dokumentti dokumentti;
        List<Dokumentti> dokumentit = dokumenttiRepository.findByOpsIdAndKieli(id, kieli);
        if (!dokumentit.isEmpty()) {
            dokumentit.sort((a, b) -> new Long(a.getId()).compareTo(b.getId()));
            dokumentti = dokumentit.get(0);
        } else {
            dokumentti = new Dokumentti();
        }

        dokumentti.setTila(DokumenttiTila.LUODAAN);
        dokumentti.setAloitusaika(new Date());
        dokumentti.setLuoja(SecurityUtil.getAuthenticatedPrincipal().getName());
        dokumentti.setKieli(kieli);
        dokumentti.setOpsId(id);

        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(id);
        if (ops != null) {
            try {
                dokumentti.setData(builder.generatePdf(ops, kieli));
                dokumentti.setTila(DokumenttiTila.VALMIS);
                dokumentti.setValmistumisaika(new Date());
                dokumentti.setVirhekoodi("");
                dokumenttiRepository.save(dokumentti);
            } catch (Exception ex) {
                dokumentti.setTila(DokumenttiTila.EPAONNISTUI);
                dokumentti.setVirhekoodi(ExceptionUtils.getStackTrace(ex));
                dokumenttiRepository.save(dokumentti);

                throw new DokumenttiException(ex.getMessage(), ex);
            }
        } else {
            dokumentti.setTila(DokumenttiTila.EPAONNISTUI);
            dokumenttiRepository.save(dokumentti);
        }
    }

    @Override
    @Transactional
    public void setStarted(DokumenttiDto dto) {
        dto.setAloitusaika(new Date());
        dto.setLuoja(SecurityUtil.getAuthenticatedPrincipal().getName());
        dto.setTila(DokumenttiTila.JONOSSA);
        dokumenttiStateService.save(dto);
    }

    @Override
    @Transactional(noRollbackFor = DokumenttiException.class)
    @Async(value = "docTaskExecutor")
    public void generateWithDto(DokumenttiDto dto) throws DokumenttiException {
        dto.setTila(DokumenttiTila.LUODAAN);
        dokumenttiStateService.save(dto);

        try {
            Dokumentti dokumentti = mapper.map(dto, Dokumentti.class);
            Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(dokumentti.getOpsId());
            dokumentti.setTila(DokumenttiTila.VALMIS);
            dokumentti.setValmistumisaika(new Date());
            dokumentti.setVirhekoodi(null);
            dokumentti.setData(builder.generatePdf(ops, dokumentti.getKieli()));

            dokumenttiRepository.save(dokumentti);
        } catch (Exception ex) {
            dto.setTila(DokumenttiTila.EPAONNISTUI);
            dto.setVirhekoodi(ex.getLocalizedMessage());

            dokumenttiStateService.save(dto);

            throw new DokumenttiException(ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DokumenttiDto getDto(Long id) {
        Dokumentti dokumentti = dokumenttiRepository.findOne(id);
        return mapper.map(dokumentti, DokumenttiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] get(Long id) {
        Dokumentti dokumentti = dokumenttiRepository.findOne(id);
        if (dokumentti == null) {
            return null;
        }

        return dokumentti.getData();
    }

    @Override
    @Transactional
    public Long getDokumenttiId(Long opsId, Kieli kieli) {
        Sort sort = new Sort(Sort.Direction.DESC, "valmistumisaika");
        List<Dokumentti> documents = dokumenttiRepository
                .findByOpsIdAndKieliAndTila(opsId, kieli, DokumenttiTila.VALMIS, sort);

        if (!documents.isEmpty()) {
            return documents.get(0).getId();
        } else {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long id) {
        Dokumentti dokumentti = dokumenttiRepository.findOne(id);
        if (dokumentti == null) {
            return false;
        }

        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(dokumentti.getOpsId());
        String name = SecurityUtil.getAuthenticatedPrincipal().getName();

        return ops.getTila().equals(Tila.JULKAISTU) || !name.equals("anonymousUser");
    }

    @Override
    @Transactional(readOnly = true)
    public DokumenttiDto query(Long id) {
        Dokumentti dokumentti = dokumenttiRepository.findOne(id);
        return mapper.map(dokumentti, DokumenttiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public DokumenttiTila getTila(Long opsId, Kieli kieli) {
        DokumenttiDto dokumentti = getDto(opsId, kieli);
        if (dokumentti != null) {
            return dokumentti.getTila();
        }

        return null;
    }
}
