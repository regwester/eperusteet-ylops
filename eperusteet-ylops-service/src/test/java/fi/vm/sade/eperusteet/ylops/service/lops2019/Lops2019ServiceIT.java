package fi.vm.sade.eperusteet.ylops.service.lops2019;

import fi.vm.sade.eperusteet.ylops.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.ylops.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.ylops.domain.Tyyppi;
import fi.vm.sade.eperusteet.ylops.domain.lops2019.Lops2019Opintojakso;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.dto.Reference;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.OrganisaatioDto;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019ModuuliDto;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019OpintojaksoDto;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019OpintojaksonModuuliDto;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaBaseDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaLuontiDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteInfoDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteTekstiKappaleDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteTekstiKappaleViiteDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleViiteDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleViiteKevytDto;
import fi.vm.sade.eperusteet.ylops.repository.lops2019.Lops2019OpintojaksoRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.ops.OpetussuunnitelmaService;
import fi.vm.sade.eperusteet.ylops.test.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class Lops2019ServiceIT extends AbstractIntegrationTest {

    @Autowired
    private EperusteetService eperusteetService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Lops2019OpintojaksoService opintojaksoService;

    @Autowired
    private Lops2019Service lopsService;

    @Autowired
    private OpetussuunnitelmaService opetussuunnitelmaService;

    @Autowired
    private OpetussuunnitelmaRepository opetussuunnitelmaRepository;

    @Autowired
    private Lops2019OpintojaksoRepository opintojaksoRepository;

    private OpetussuunnitelmaDto createLukioOpetussuunnitelma() {
        OpetussuunnitelmaLuontiDto pohjaLuontiDto = new OpetussuunnitelmaLuontiDto();
        pohjaLuontiDto.setTyyppi(Tyyppi.POHJA);
        pohjaLuontiDto.setPerusteenDiaarinumero("1/2/3");
        OpetussuunnitelmaDto pohjaDto = opetussuunnitelmaService.addPohja(pohjaLuontiDto);

        OpetussuunnitelmaLuontiDto opsLuontiDto = new OpetussuunnitelmaLuontiDto();
        opsLuontiDto.setTyyppi(Tyyppi.OPS);
        opsLuontiDto.setOrganisaatiot(Stream.of("1.2.246.562.10.83037752777")
                .map(oid -> {
                    OrganisaatioDto result = new OrganisaatioDto();
                    result.setOid(oid);
                    return result;
                })
                .collect(Collectors.toSet()));
        opsLuontiDto.setPohja(Reference.of(pohjaDto.getId()));

        return opetussuunnitelmaService.addOpetussuunnitelma(opsLuontiDto);
    }

    @Test
    public void convertTestJsonToDto() {
        List<PerusteInfoDto> perusteet = eperusteetService.findPerusteet();
        assertThat(perusteet.size()).isGreaterThan(0);
        PerusteDto peruste = eperusteetService.getPeruste("1/2/3");
        assertThat(peruste.getLops2019()).isNotNull();
        List<Lops2019ModuuliDto> moduulit = peruste.getLops2019().getOppiaineet().get(1).getOppimaarat().get(0).getModuulit();
        assertThat(moduulit.size()).isNotEqualTo(0);
        PerusteTekstiKappaleDto viite = peruste.getLops2019().getSisalto().getLapset().get(1).getPerusteenOsa();
        assertThat(viite.getNimi().get(Kieli.FI)).isEqualTo("Arvoperusta");
        assertThat(viite.getTeksti().get(Kieli.FI)).isNotBlank();
        assertThat(viite.getOsanTyyppi()).isEqualTo("tekstikappale");
    }

    @Test
    public void testUudenLukiopohjanLuonti() {
        OpetussuunnitelmaLuontiDto pohjaLuontiDto = new OpetussuunnitelmaLuontiDto();
        pohjaLuontiDto.setTyyppi(Tyyppi.POHJA);
        pohjaLuontiDto.setPerusteenDiaarinumero("1/2/3");
        OpetussuunnitelmaDto pohjaDto = opetussuunnitelmaService.addPohja(pohjaLuontiDto);
        assertThat(pohjaDto)
                .extracting("toteutus", "koulutustyyppi", "perusteenDiaarinumero", "tyyppi")
                .containsExactly(KoulutustyyppiToteutus.LOPS2019, KoulutusTyyppi.LUKIOKOULUTUS, "1/2/3", Tyyppi.POHJA);

        TekstiKappaleViiteDto.Puu tekstit = opetussuunnitelmaService.getTekstit(pohjaDto.getId(), TekstiKappaleViiteDto.Puu.class);
        assertThat(tekstit.getLapset().size()).isEqualTo(6);
    }

    @Test
    public void testUudenLukionOpsinLuonti() {
        OpetussuunnitelmaDto opsDto = createLukioOpetussuunnitelma();
        assertThat(opsDto)
                .extracting("toteutus", "koulutustyyppi", "perusteenDiaarinumero", "tyyppi")
                .containsExactly(KoulutustyyppiToteutus.LOPS2019, KoulutusTyyppi.LUKIOKOULUTUS, "1/2/3", Tyyppi.OPS);
        assertThat(opsDto.getPohja()).isNotNull();

        List<Lops2019OppiaineDto> oppiaineet = lopsService.getPerusteOppiaineet(opsDto.getId());
        assertThat(oppiaineet).isNotNull();
        assertThat(oppiaineet.size()).isNotEqualTo(0);

        PerusteTekstiKappaleViiteDto tekstikappaleet = lopsService.getPerusteTekstikappaleet(opsDto.getId());
        assertThat(tekstikappaleet).isNotNull();

        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsDto.getId());
        assertThat(ops.getLops2019()).isNotNull();
        assertThat(ops.getLops2019().getOpetussuunnitelma()).isNotNull();

    }

    @Test
    public void testOpintojaksojenHallinta() {
        OpetussuunnitelmaDto ops = createLukioOpetussuunnitelma();

        Lops2019OpintojaksoDto opintojaksoDto = Lops2019OpintojaksoDto.builder()
                .nimi(LokalisoituTekstiDto.of("Geometriat"))
                .kuvaus(LokalisoituTekstiDto.of("Geometriaan liittyvät moduulit toteutetaan yhtenä opintojaksona"))
                .koodi("1234")
                .oppiaineet(Collections.singleton("oppiaineet_maa"))
                .moduuli(Lops2019OpintojaksonModuuliDto.builder()
                        .koodiUri("moduuli_maa3")
                        .kuvaus(LokalisoituTekstiDto.of("X"))
                        .build())
                .moduuli(Lops2019OpintojaksonModuuliDto.builder()
                        .koodiUri("moduuli_maa4")
                        .kuvaus(LokalisoituTekstiDto.of("Y"))
                        .build())
                .build();

        opintojaksoDto = opintojaksoService.addOpintojakso(ops.getId(), opintojaksoDto);
        List<Lops2019OpintojaksoDto> opintojaksot = opintojaksoService.getAll(ops.getId());
        assertThat(opintojaksot.size()).isEqualTo(1);
        assertThat(opintojaksot.get(0).getId()).isEqualTo(opintojaksoDto.getId());
    }

}
