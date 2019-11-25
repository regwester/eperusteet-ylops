package fi.vm.sade.eperusteet.ylops.service.ops.impl;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.utils.dto.peruste.lops2019.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.ylops.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019OpintojaksoDto;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019OpintojaksonOppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019PaikallinenOppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.navigation.NavigationNodeDto;
import fi.vm.sade.eperusteet.ylops.dto.navigation.NavigationType;
import fi.vm.sade.eperusteet.ylops.dto.peruste.lops2019.oppiaineet.Lops2019OppiaineKaikkiDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.lops2019.oppiaineet.moduuli.Lops2019ModuuliDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.repository.lops2019.Lops2019SisaltoRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.lops2019.Lops2019OpintojaksoService;
import fi.vm.sade.eperusteet.ylops.service.lops2019.Lops2019OppiaineService;
import fi.vm.sade.eperusteet.ylops.service.lops2019.Lops2019Service;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.NavigationBuilder;
import fi.vm.sade.eperusteet.ylops.service.ops.OpsDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Transactional
public class NavigationBuilderLops2019Impl implements NavigationBuilder {

    @Autowired
    private OpsDispatcher dispatcher;

    @Autowired
    private EperusteetService eperusteetService;

    @Autowired
    private Lops2019OpintojaksoService opintojaksoService;

    @Autowired
    private Lops2019SisaltoRepository lops2019SisaltoRepository;

    @Autowired
    private OpetussuunnitelmaRepository opetussuunnitelmaRepository;

    @Autowired
    private Lops2019Service lopsService;

    @Autowired
    private Lops2019OppiaineService oppiaineService;

    @Autowired
    private DtoMapper mapper;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.LOPS2019);
    }

    @Override
    public NavigationNodeDto buildNavigation(Long opsId) {
        return NavigationNodeDto.of(NavigationType.root)
                .addAll(dispatcher.get(NavigationBuilder.class).buildNavigation(opsId).getChildren())
                .add(oppiaineet(opsId));
    }

    private NavigationNodeDto oppiaineet(Long opsId) {
        List<Lops2019OppiaineKaikkiDto> oppiaineet = lopsService.getPerusteOppiaineet(opsId);
        List<Lops2019PaikallinenOppiaineDto> paikallisetOppiaineet = oppiaineService.getAll(opsId);

        Map<String, Set<Lops2019OpintojaksoDto>> opintojaksotMap = new HashMap<>();
        List<Lops2019OpintojaksoDto> opsOpintojaksot = opintojaksoService.getAll(opsId);
        if (!ObjectUtils.isEmpty(opsOpintojaksot)) {
            opsOpintojaksot.forEach(oj -> {
                Set<Lops2019OpintojaksonOppiaineDto> ojOppiaineet = oj.getOppiaineet();
                if (!ObjectUtils.isEmpty(ojOppiaineet)) {
                    ojOppiaineet.forEach(oa -> {
                        String key = oa.getKoodi();
                        if (!ObjectUtils.isEmpty(key)) {
                            if (!opintojaksotMap.containsKey(key)) {
                                opintojaksotMap.put(key, new HashSet<>());
                            }
                            opintojaksotMap.get(key).add(oj);
                        }
                    });
                }
            });
        }

        return NavigationNodeDto.of(NavigationType.oppiaineet)
                .addAll(oppiaineet.stream()
                        .map(oa -> mapOppiaine(oa, opintojaksotMap))
                        .collect(Collectors.toList()))
                .addAll(paikallisetOppiaineet.stream()
                        .map(this::mapPaikallinenOppiaine)
                        .collect(Collectors.toList()));
    }

    private NavigationNodeDto mapOppiaine(
            Lops2019OppiaineKaikkiDto oa,
            Map<String, Set<Lops2019OpintojaksoDto>> opintojaksotMap
    ) {
        NavigationNodeDto result = NavigationNodeDto
                .of(NavigationType.oppiaine, mapper.map(oa.getNimi(), LokalisoituTekstiDto.class), oa.getId())
                .meta("koodi", mapper.map(oa.getKoodi(), KoodiDto.class));

        if (!ObjectUtils.isEmpty(oa.getOppimaarat())) {
            result.add(NavigationNodeDto.of(NavigationType.oppimaarat)
                    .addAll(oa.getOppimaarat().stream().map(om -> mapOppiaine(om, opintojaksotMap))));
        }

        if (oa.getKoodi() != null && oa.getKoodi().getUri() != null
                && opintojaksotMap.containsKey(oa.getKoodi().getUri())
                && !ObjectUtils.isEmpty(opintojaksotMap.get(oa.getKoodi().getUri()))) {
            Set<Lops2019OpintojaksoDto> oaOpintojaksot = opintojaksotMap.get(oa.getKoodi().getUri());
            result.add(NavigationNodeDto.of(NavigationType.opintojaksot)
                    .addAll(oaOpintojaksot.stream()
                            .map(ojOa -> NavigationNodeDto.of(
                                    NavigationType.opintojakso,
                                    mapper.map(ojOa.getNimi(), LokalisoituTekstiDto.class),
                                    ojOa.getId())
                                    .meta("koodi", ojOa.getKoodi()))));
        }

        List<Lops2019ModuuliDto> moduulit = oa.getModuulit();
        if (!ObjectUtils.isEmpty(moduulit)) {
            result.add(NavigationNodeDto.of(NavigationType.moduulit)
                    .addAll(moduulit.stream()
                            .map(m -> NavigationNodeDto.of(
                                    NavigationType.moduuli,
                                    mapper.map(m.getNimi(), LokalisoituTekstiDto.class),
                                    m.getId())
                                    .meta("koodi", mapper.map(m.getKoodi(), KoodiDto.class))
                                    .meta("pakollinen", m.isPakollinen()))));
        }

        return result;
    }

    private NavigationNodeDto mapPaikallinenOppiaine(
            Lops2019PaikallinenOppiaineDto poa
    ) {
        return NavigationNodeDto
                .of(NavigationType.oppiaine, mapper.map(poa.getNimi(), LokalisoituTekstiDto.class), poa.getId())
                .meta("koodi", mapper.map(poa.getKoodi(), KoodiDto.class));
    }
}
