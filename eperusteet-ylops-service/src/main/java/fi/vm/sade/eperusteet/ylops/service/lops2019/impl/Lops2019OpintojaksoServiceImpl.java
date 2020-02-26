package fi.vm.sade.eperusteet.ylops.service.lops2019.impl;

import fi.vm.sade.eperusteet.ylops.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.ylops.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.ylops.domain.lops2019.Lops2019Opintojakso;
import fi.vm.sade.eperusteet.ylops.domain.lops2019.Lops2019OpintojaksonModuuli;
import fi.vm.sade.eperusteet.ylops.domain.lops2019.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.ylops.domain.lops2019.Lops2019Poistettu;
import fi.vm.sade.eperusteet.ylops.domain.lops2019.PoistetunTyyppi;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.dto.KoodiDto;
import fi.vm.sade.eperusteet.ylops.dto.RevisionDto;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019OpintojaksoBaseDto;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019OpintojaksoDto;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019OpintojaksoPerusteDto;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019OpintojaksonModuuliDto;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019OpintojaksonOppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019PaikallinenOppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.lops2019.oppiaineet.Lops2019OppiaineKaikkiDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.lops2019.oppiaineet.moduuli.Lops2019ModuuliBaseDto;
import fi.vm.sade.eperusteet.ylops.repository.lops2019.Lops2019OpintojaksoRepository;
import fi.vm.sade.eperusteet.ylops.repository.lops2019.Lops2019OppiaineRepository;
import fi.vm.sade.eperusteet.ylops.repository.lops2019.Lops2019PoistetutRepository;
import fi.vm.sade.eperusteet.ylops.repository.lops2019.Lops2019SisaltoRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.external.KayttajanTietoService;
import fi.vm.sade.eperusteet.ylops.service.lops2019.Lops2019OpintojaksoService;
import fi.vm.sade.eperusteet.ylops.service.lops2019.Lops2019OppiaineService;
import fi.vm.sade.eperusteet.ylops.service.lops2019.Lops2019Service;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.OpetussuunnitelmaService;
import fi.vm.sade.eperusteet.ylops.service.ops.OpetussuunnitelmanMuokkaustietoService;
import fi.vm.sade.eperusteet.ylops.service.util.UpdateWrapperDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class Lops2019OpintojaksoServiceImpl implements Lops2019OpintojaksoService {

    @Autowired
    private Lops2019OppiaineRepository oppiaineRepository;

    @Autowired
    private Lops2019PoistetutRepository poistetutRepository;

    @Autowired
    private Lops2019OpintojaksoRepository opintojaksoRepository;

    @Autowired
    private Lops2019SisaltoRepository sisaltoRepository;

    @Autowired
    private OpetussuunnitelmaService opetussuunnitelmaService;

    @Autowired
    private OpetussuunnitelmaRepository opetussuunnitelmaRepository;

    @Autowired
    private Lops2019Service lopsService;

    @Autowired
    private KayttajanTietoService kayttajanTietoService;

    @Autowired
    private DtoMapper mapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private OpetussuunnitelmanMuokkaustietoService muokkaustietoService;

    @Autowired
    private Lops2019OppiaineService lops2019OppiaineService;

    private Opetussuunnitelma getOpetussuunnitelma(Long opsId) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        if (ops == null) {
            throw new BusinessRuleViolationException("opetussuunnitelmaa-ei-loytynyt");
        }
        else if (!KoulutustyyppiToteutus.LOPS2019.equals(ops.getToteutus())) {
            throw new BusinessRuleViolationException("opetussuunnitelma-vaaran-tyyppinen");
        }
        else {
            return ops;
        }
    }

    @Override
    public long getOpintojaksonLaajuus(Long opsId, Lops2019OpintojaksoDto opintojakso) {

        Long laajuudet = 0l;

        if (!CollectionUtils.isEmpty(opintojakso.getPaikallisetOpintojaksot())) {
            laajuudet += opintojakso.getPaikallisetOpintojaksot().stream()
                    .map(paikallinenOpintojakso -> getOpintojaksonLaajuus(opsId, paikallinenOpintojakso))
                    .mapToLong(Long::longValue)
                    .sum();
        }

        if (opintojakso.getModuulit().isEmpty()) {
            laajuudet +=  opintojakso.getOppiaineet().stream()
                .filter(Objects::nonNull)
                .map(Lops2019OpintojaksonOppiaineDto::getLaajuus)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
        }
        else {
            Set<String> moduulikoodit = opintojakso.getModuulit().stream()
                    .map(Lops2019OpintojaksonModuuliDto::getKoodiUri)
                    .collect(Collectors.toSet());
            Set<fi.vm.sade.eperusteet.ylops.dto.peruste.lops2019.oppiaineet.moduuli.Lops2019ModuuliDto> perusteModuulit
                    = lopsService.getPerusteModuulit(opsId, moduulikoodit);
            laajuudet +=  perusteModuulit.stream()
                    .map(fi.vm.sade.eperusteet.ylops.dto.peruste.lops2019.oppiaineet.moduuli.Lops2019ModuuliDto::getLaajuus)
                    .mapToLong(BigDecimal::longValue)
                    .sum();
        }

        return laajuudet;
    }

    private Lops2019Opintojakso getOpintojakso(Long opsId, Long opintojaksoId) {
        Opetussuunnitelma ops = getOpetussuunnitelma(opsId);
        Lops2019Opintojakso opintojakso = ops.getLops2019().getOpintojakso(opintojaksoId);
        if (opintojakso == null) {
            throw new BusinessRuleViolationException("opintojaksoa-ei-ole");
        }
        return opintojakso;
    }

    @Override
    public List<Lops2019OpintojaksoDto> getAll(Long opsId) {
        Opetussuunnitelma opetussuunnitelma = getOpetussuunnitelma(opsId);
        return opetussuunnitelma.getLops2019().getOpintojaksot().stream()
                .map(oj -> mapper.map(oj, Lops2019OpintojaksoDto.class))
                .map(oj -> {
                    oj.setLaajuus(getOpintojaksonLaajuus(opsId, oj));
                    if(oj.getPaikallisetOpintojaksot() != null) {
                        oj.getPaikallisetOpintojaksot().forEach(paikallinenOpintojakso -> {
                            paikallinenOpintojakso.setLaajuus(getOpintojaksonLaajuus(opsId, paikallinenOpintojakso));
                        });
                    }
                    return oj;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Lops2019OpintojaksoDto getOne(Long opsId, Long opintojaksoId) {
        Lops2019Opintojakso opintojakso = getOpintojakso(opsId, opintojaksoId);
        Lops2019OpintojaksoDto result = mapper.map(opintojakso, Lops2019OpintojaksoDto.class);
        result.setLaajuus(getOpintojaksonLaajuus(opsId, result));
        if(result.getPaikallisetOpintojaksot() != null) {
            result.getPaikallisetOpintojaksot().forEach(paikallinenOpintojakso -> {
                paikallinenOpintojakso.setLaajuus(getOpintojaksonLaajuus(opsId, paikallinenOpintojakso));
            });
        }
        return result;
    }

    @Override
    public Lops2019OpintojaksoDto addOpintojakso(
            Long opsId,
            Lops2019OpintojaksoDto opintojaksoDto
    ) {
        return this.addOpintojakso(opsId, opintojaksoDto, null);
    }

    @Override
    public Lops2019OpintojaksoDto addOpintojakso(
            Long opsId,
            Lops2019OpintojaksoDto opintojaksoDto,
            MuokkausTapahtuma tapahtuma
    ) {
        opintojaksoDto.setId(null);
        Opetussuunnitelma ops = getOpetussuunnitelma(opsId);
        Set<fi.vm.sade.eperusteet.ylops.dto.peruste.lops2019.oppiaineet.moduuli.Lops2019ModuuliDto> loydetytModuulit
                = lopsService.getPerusteModuulit(opsId, opintojaksoDto.getModuulit().stream()
                .map(Lops2019OpintojaksonModuuliDto::getKoodiUri)
                .collect(Collectors.toSet()));

        if (opintojaksoDto.getModuulit() != null && opintojaksoDto.getModuulit().size() != loydetytModuulit.size()) {
            throw new BusinessRuleViolationException("perusteen-moduulia-ei-olemassa");
        }

        if (opintojaksoDto.getOppiaineet() == null) {
            throw new BusinessRuleViolationException("perusteen-oppiainetta-ei-olemassa");
        }

        tarkistaOpintojakso(ops, opintojaksoDto);

        Set<String> opintojaksonOppiaineKoodit = opintojaksoDto.getOppiaineet().stream()
                .map(Lops2019OpintojaksonOppiaineDto::getKoodi)
                .collect(Collectors.toSet());

        opintojaksonOppiaineKoodit.addAll(
                lops2019OppiaineService.getAll(opsId).stream()
                        .filter(paikallinen -> opintojaksonOppiaineKoodit.contains(paikallinen.getKoodi()))
                        .map(Lops2019PaikallinenOppiaineDto::getPerusteenOppiaineUri)
                        .collect(Collectors.toSet()));

        Set<Lops2019OppiaineKaikkiDto> perusteenOppiaineet
                = lopsService.getPerusteenOppiaineet(opsId, opintojaksonOppiaineKoodit);

        Set<String> oppiaineKoodit = Stream.concat(
                oppiaineRepository.findAllBySisalto(ops.getLops2019()).stream()
                    .map(Lops2019Oppiaine::getKoodi),
                perusteenOppiaineet.stream()
                        .flatMap(x -> Stream.concat(Stream.of(x), x.getOppimaarat().stream()))
                        .map(oa -> oa.getKoodi().getUri()))
                .collect(Collectors.toSet());

        if (!oppiaineKoodit.containsAll(opintojaksoDto.getOppiaineet().stream()
                .map(Lops2019OpintojaksonOppiaineDto::getKoodi)
                .collect(Collectors.toSet()))) {
            throw new BusinessRuleViolationException("oppiainetta-ei-ole");
        }

        if (perusteenOppiaineet.stream().anyMatch(oa -> !oa.getOppimaarat().isEmpty())) {
            throw new BusinessRuleViolationException("opintojaksoon-ei-voi-liittaa-abstraktia-oppiainetta");
        }

        { // Tarkistetaan että moduulit ovat oikeista oppiaineista
            Set<String> moduulikoodit = loydetytModuulit.stream()
                    .map(Lops2019ModuuliBaseDto::getKoodi)
                    .map(KoodiDto::getUri)
                    .collect(Collectors.toSet());
            perusteenOppiaineet.forEach(oa -> {
                moduulikoodit.removeAll(oa.getModuulit().stream()
                        .map(Lops2019ModuuliBaseDto::getKoodi)
                        .map(KoodiDto::getUri)
                        .collect(Collectors.toSet()));
            });

            if (!moduulikoodit.isEmpty()) {
                throw new BusinessRuleViolationException("liitetyt-moduulit-tulee-loytya-opintojakson-oppiaineilta");
            }
        }

        Lops2019Opintojakso opintojakso = mapper.map(opintojaksoDto, Lops2019Opintojakso.class);
        opintojakso = opintojaksoRepository.save(opintojakso);
        ops.getLops2019().addOpintojakso(opintojakso);
        Lops2019OpintojaksoDto result = mapper.map(opintojakso, Lops2019OpintojaksoDto.class);
        result.setLaajuus(getOpintojaksonLaajuus(opsId, result));

        muokkaustietoService.addOpsMuokkausTieto(opsId,
                opintojakso,
                tapahtuma != null ? tapahtuma : MuokkausTapahtuma.LUONTI);
        return result;
    }

    @Override
    public Lops2019OpintojaksoDto updateOpintojakso(
            Long opsId,
            Long opintojaksoId,
            UpdateWrapperDto<Lops2019OpintojaksoDto> opintojaksoDto
    ) {
        return this.updateOpintojakso(opsId, opintojaksoId, opintojaksoDto, null);
    }

    @Override
    public Lops2019OpintojaksoDto updateOpintojakso(
            Long opsId,
            Long opintojaksoId,
            UpdateWrapperDto<Lops2019OpintojaksoDto> opintojaksoDto,
            MuokkausTapahtuma tapahtuma
    ) {
        Opetussuunnitelma ops = getOpetussuunnitelma(opsId);
        tarkistaOpintojakso(ops, opintojaksoDto.getData());

        opintojaksoDto.getData().setId(opintojaksoId);
        Lops2019Opintojakso opintojakso = getOpintojakso(opsId, opintojaksoId);
        opintojakso = mapper.map(opintojaksoDto.getData(), opintojakso);
        opintojakso.updateMuokkaustiedot();
        opintojakso = opintojaksoRepository.save(opintojakso);
        Lops2019OpintojaksoDto result = mapper.map(opintojakso, Lops2019OpintojaksoDto.class);
        result.setLaajuus(getOpintojaksonLaajuus(opsId, result));

        muokkaustietoService.addOpsMuokkausTieto(opsId,
                opintojakso,
                tapahtuma != null ? tapahtuma : MuokkausTapahtuma.PAIVITYS);
        return result;
    }

    @Override
    public void removeOne(Long opsId, Long opintojaksoId) {
        Lops2019Opintojakso opintojakso = getOpintojakso(opsId, opintojaksoId);
        Opetussuunnitelma ops = getOpetussuunnitelma(opsId);
        Lops2019Poistettu poistettu = new Lops2019Poistettu();
        poistettu.setNimi(opintojakso.getNimi());
        poistettu.setOpetussuunnitelma(ops);
        poistettu.setPoistettu_id(opintojaksoId);
        poistettu.setPalautettu(false);
        poistettu.setTyyppi(PoistetunTyyppi.OPINTOJAKSO);
        opintojakso.updateMuokkaustiedot();
        poistetutRepository.save(poistettu);
        ops.getLops2019().getOpintojaksot().remove(opintojakso);

        muokkaustietoService.addOpsMuokkausTieto(opsId, opintojakso, MuokkausTapahtuma.POISTO);
    }

    @Override
    public List<RevisionDto> getVersions(Long opsId, Long opintojaksoId) {
        getOpintojakso(opsId, opintojaksoId);
        return mapper.mapAsList(opintojaksoRepository.getRevisions(opintojaksoId), RevisionDto.class).stream()
                .peek(rev -> {
                    String nimi = kayttajanTietoService.haeKayttajanimi(rev.getMuokkaajaOid());
                    rev.setNimi(nimi);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Lops2019OpintojaksoDto getVersion(Long opsId, Long opintojaksoId, Integer versio) {
        getOpintojakso(opsId, opintojaksoId);
        Lops2019Opintojakso revision = opintojaksoRepository.findRevision(opintojaksoId, versio);
        return mapper.map(revision, Lops2019OpintojaksoDto.class);
    }

    @Override
    public Lops2019OpintojaksoDto revertTo(Long opsId, Long opintojaksoId, Integer versio) {
        getOpintojakso(opsId, opintojaksoId);
        Lops2019Opintojakso revision = opintojaksoRepository.findRevision(opintojaksoId, versio);
        Lops2019OpintojaksoDto dto = mapper.map(revision, Lops2019OpintojaksoDto.class);
        UpdateWrapperDto<Lops2019OpintojaksoDto> wrapperDto = new UpdateWrapperDto<>();
        wrapperDto.setData(dto);
        return updateOpintojakso(opsId, opintojaksoId, wrapperDto, MuokkausTapahtuma.PALAUTUS);
    }

    @Override
    public Lops2019OpintojaksoPerusteDto getOpintojaksonPeruste(Long opsId, Long opintojaksoId) {
        Lops2019Opintojakso opintojakso = getOpintojakso(opsId, opintojaksoId);
        Lops2019OpintojaksoPerusteDto opintojaksoPerusteDto = new Lops2019OpintojaksoPerusteDto();
        opintojaksoPerusteDto.setModuulit(opintojakso.getModuulit().stream()
                .map(Lops2019OpintojaksonModuuli::getKoodiUri)
                .map(koodi -> lopsService.getPerusteModuuli(opsId, koodi))
                .collect(Collectors.toList()));
        return opintojaksoPerusteDto;
    }

    private void tarkistaOpintojakso(Opetussuunnitelma ops, Lops2019OpintojaksoDto opintojaksoDto) {
        List<Lops2019OpintojaksoDto> opsOpintojaksot = mapper.mapAsList(ops.getLops2019().getOpintojaksot(), Lops2019OpintojaksoDto.class);

        Set<Long> opintojaksotPaikallisillaopintojaksoilla = opsOpintojaksot.stream()
                .filter(opintojakso -> !CollectionUtils.isEmpty(opintojakso.getPaikallisetOpintojaksot()))
                .map(Lops2019OpintojaksoBaseDto::getId)
                .collect(Collectors.toSet());

        if (!CollectionUtils.isEmpty(opintojaksoDto.getPaikallisetOpintojaksot())) {
            opintojaksoDto.getPaikallisetOpintojaksot().forEach(paikallinenOpintojakso -> {
                if (opintojaksotPaikallisillaopintojaksoilla.contains(paikallinenOpintojakso.getId())) {
                    throw new BusinessRuleViolationException("paikalliseen-opintojaksoon-on-jo-lisatty-opintojaksoja");
                }
            });
        }
    }

    @Override
    public boolean tarkistaOpintojaksot(@P("opsId") Long opsId) {

        Opetussuunnitelma ops = getOpetussuunnitelma(opsId);
        List<Lops2019OpintojaksoDto> opsOpintojaksot = mapper.mapAsList(ops.getLops2019().getOpintojaksot(), Lops2019OpintojaksoDto.class);

        try {
            opsOpintojaksot.forEach(opintojakso -> tarkistaOpintojakso(ops, opintojakso));
            return true;
        } catch (BusinessRuleViolationException ex) {
            return false;
        }
    }
}
