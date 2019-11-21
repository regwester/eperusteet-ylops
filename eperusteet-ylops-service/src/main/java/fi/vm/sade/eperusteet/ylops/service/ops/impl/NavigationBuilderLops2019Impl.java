package fi.vm.sade.eperusteet.ylops.service.ops.impl;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.utils.dto.peruste.lops2019.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.ylops.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.ylops.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019PaikallinenOppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.navigation.NavigationNodeDto;
import fi.vm.sade.eperusteet.ylops.dto.navigation.NavigationType;
import fi.vm.sade.eperusteet.ylops.dto.peruste.lops2019.oppiaineet.Lops2019OppiaineKaikkiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.repository.lops2019.Lops2019SisaltoRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
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
        final Lops2019Sisalto sisalto = lops2019SisaltoRepository.findOne(opsId);

        List<Lops2019OppiaineKaikkiDto> oppiaineet = lopsService.getPerusteOppiaineet(opsId);
        List<Lops2019PaikallinenOppiaineDto> paikallisetOppiaineet = oppiaineService.getAll(opsId);

        List<Lops2019OppiaineKaikkiDto> kaikki = new ArrayList<>();
        kaikki.addAll(oppiaineet);
        //kaikki.addAll(oppimaarat);


        return NavigationNodeDto.of(NavigationType.oppiaineet)
                .addAll(oppiaineet.stream()
                        .map(oa -> mapOppiaine(oa))
                        .collect(Collectors.toList()))
                .addAll(paikallisetOppiaineet.stream()
                        .map(poa -> mapPaikallinenOppiaine(poa))
                        .collect(Collectors.toList()));

        /*
        List<Lops2019Oppiaine> oppiaineet = sisalto.getOppiaineet();
        List<Lops2019Oppiaine> oppimaarat = new ArrayList<>();
        if (!ObjectUtils.isEmpty(oppiaineet)) {
            oppimaarat = lops2019OppiaineRepository.getOppimaaratByParents(oppiaineet);
        }

        List<Lops2019Oppiaine> kaikki = new ArrayList<>();
        kaikki.addAll(oppiaineet);
        kaikki.addAll(oppimaarat);

        Map<Lops2019Oppiaine, List<Lops2019Oppiaine>> oppimaaratMap = new HashMap<>();
        oppimaarat.forEach(om -> {
            if (!oppimaaratMap.containsKey(om.getOppiaine())) {
                oppimaaratMap.put(om.getOppiaine(), new ArrayList<>());
            }
            oppimaaratMap.get(om.getOppiaine()).add(om);
        });

        Map<Lops2019Oppiaine, List<Lops2019Moduuli>> moduulitMap = new HashMap<>();
        List<Lops2019Moduuli> moduulit = new ArrayList<>();
        if (!ObjectUtils.isEmpty(kaikki)) {
            moduulit = lops2019ModuuliRepository.getModuulitByParents(kaikki);
        }
        moduulit.forEach(m -> {
            if (!moduulitMap.containsKey(m.getOppiaine())) {
                moduulitMap.put(m.getOppiaine(), new ArrayList<>());
            }
            moduulitMap.get(m.getOppiaine()).add(m);
        });

        return NavigationNodeDto.of(NavigationType.oppiaineet)
                .addAll(oppiaineet.stream()
                        .map(oa -> mapOppiaine(oa, oppimaaratMap, moduulitMap))
                        .collect(Collectors.toList()));
        */
    }

    private NavigationNodeDto mapOppiaine(
            Lops2019OppiaineKaikkiDto oa
    ) {
        NavigationNodeDto result = NavigationNodeDto
                .of(NavigationType.oppiaine, mapper.map(oa.getNimi(), LokalisoituTekstiDto.class), oa.getId())
                .meta("koodi", mapper.map(oa.getKoodi(), KoodiDto.class));

        if (!ObjectUtils.isEmpty(oa.getOppimaarat())) {
            result.add(NavigationNodeDto.of(NavigationType.oppimaarat)
                    .addAll(oa.getOppimaarat().stream().map(this::mapOppiaine)));
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
