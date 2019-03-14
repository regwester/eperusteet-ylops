package fi.vm.sade.eperusteet.ylops.service.lops2019.impl;

import fi.vm.sade.eperusteet.ylops.domain.cache.PerusteCache;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019ModuuliDto;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019OpintojaksoDto;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteTekstiKappaleViiteDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteTekstiKappaleViiteMatalaDto;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.lops2019.Lops2019Service;
import fi.vm.sade.eperusteet.ylops.service.ops.OpetussuunnitelmaService;
import fi.vm.sade.eperusteet.ylops.service.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@Slf4j
public class Lops2019ServiceImpl implements Lops2019Service {

    @Autowired
    private EperusteetService eperusteetService;

    @Autowired
    private OpetussuunnitelmaService opetussuunnitelmaService;

    @Autowired
    private OpetussuunnitelmaRepository opetussuunnitelmaRepository;

    private Opetussuunnitelma getOpetussuunnitelma(Long opsId) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        if (ops == null) {
            throw new BusinessRuleViolationException("ops-ei-loydy");
        }
        return ops;
    }

    private PerusteDto getPeruste(Long opsId) {
        Opetussuunnitelma ops = getOpetussuunnitelma(opsId);
        PerusteCache perusteCached = ops.getCachedPeruste();
        return eperusteetService.getPerusteById(perusteCached.getId());
    }

    @Override
    public List<Lops2019OpintojaksoDto> getOpintojaksot(Long opsId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Lops2019OppiaineDto> getPerusteOppiaineet(Long opsId) {
        PerusteDto perusteDto = getPeruste(opsId);
        return perusteDto.getLops2019()
                .getOppiaineet();
    }

    @Override
    public List<Lops2019OppiaineDto> getPerusteOppiaine(Long opsId, Long oppiaineId) {
        return getPeruste(opsId).getLops2019().getOppiaineet().stream()
                .filter(oa -> Objects.equals(oppiaineId, oa.getId()))
                .map(Lops2019OppiaineDto::getOppimaarat)
                .findFirst()
                    .orElse(null);
    }

    @Override
    public Lops2019ModuuliDto getPerusteModuuli(Long opsId, Long oppiaineId, Long moduuliId) {
        return CollectionUtil.treeToStream(
                getPeruste(opsId).getLops2019().getOppiaineet(),
                Lops2019OppiaineDto::getOppimaarat)
                .filter((oa) -> Objects.equals(oppiaineId, oa.getId()))
                .findFirst()
                    .map(Lops2019OppiaineDto::getModuulit)
                    .orElse(new ArrayList<>())
                .stream()
                    .filter((moduuli) -> Objects.equals(oppiaineId, moduuli.getId()))
                    .findFirst().orElse(null);
    }

    @Override
    public PerusteTekstiKappaleViiteDto getPerusteTekstikappaleet(Long opsId) {
        return getPeruste(opsId).getLops2019().getSisalto();
    }

    @Override
    public PerusteTekstiKappaleViiteMatalaDto getPerusteTekstikappale(Long opsId, Long tekstikappaleId) {
        return CollectionUtil.treeToStream(
                getPeruste(opsId).getLops2019().getSisalto(),
                PerusteTekstiKappaleViiteDto::getLapset)
                    .filter(viiteDto -> Objects.equals(tekstikappaleId, viiteDto.getId()))
                    .findFirst()
                    .orElse(null);
    }

}
