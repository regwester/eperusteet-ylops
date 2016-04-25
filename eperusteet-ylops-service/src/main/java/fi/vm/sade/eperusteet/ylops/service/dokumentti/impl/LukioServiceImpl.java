package fi.vm.sade.eperusteet.ylops.service.dokumentti.impl;

import fi.vm.sade.eperusteet.ylops.domain.lukio.Aihekokonaisuudet;
import fi.vm.sade.eperusteet.ylops.domain.lukio.Aihekokonaisuus;
import fi.vm.sade.eperusteet.ylops.domain.lukio.OpetuksenYleisetTavoitteet;
import fi.vm.sade.eperusteet.ylops.domain.lukio.OppiaineLukiokurssi;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaine;
import fi.vm.sade.eperusteet.ylops.dto.peruste.lukio.AihekokonaisuudetDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.lukio.AihekokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.lukio.OpetuksenYleisetTavoitteetDto;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.LukioService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiUtils.addHeader;
import static fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiUtils.addLokalisoituteksti;
import static fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiUtils.getTextString;

/**
 * @author isaul
 */
@Service
public class LukioServiceImpl implements LukioService {
    private static final Logger LOG = LoggerFactory.getLogger(LukioServiceImpl.class);

    @Override
    public void addOpetuksenYleisetTavoitteet(DokumenttiBase docBase) {
        OpetuksenYleisetTavoitteet yleisetTavoitteet = docBase.getOps().getOpetuksenYleisetTavoitteet();
        OpetuksenYleisetTavoitteetDto perusteYleisetTavoitteet = docBase.getPerusteDto().getLukiokoulutus().getOpetuksenYleisetTavoitteet();

        if (perusteYleisetTavoitteet == null) {
            return;
        }

        addHeader(docBase, getTextString(docBase, perusteYleisetTavoitteet.getOtsikko()));
        addLokalisoituteksti(docBase, perusteYleisetTavoitteet.getKuvaus(), "cite");
        if (yleisetTavoitteet != null) {
            addLokalisoituteksti(docBase, yleisetTavoitteet.getKuvaus(), "div");
        }

        docBase.getGenerator().increaseNumber();
    }

    @Override
    public void addAihekokonaisuudet(DokumenttiBase docBase) {
        Aihekokonaisuudet aihekokonaisuudet = docBase.getOps().getAihekokonaisuudet();
        AihekokonaisuudetDto perusteAihekokonaisuudet = docBase.getPerusteDto().getLukiokoulutus().getAihekokonaisuudet();
        if (aihekokonaisuudet == null || perusteAihekokonaisuudet == null) {
            return;
        }

        addHeader(docBase, getTextString(docBase, perusteAihekokonaisuudet.getOtsikko()));
        addLokalisoituteksti(docBase, perusteAihekokonaisuudet.getYleiskuvaus(), "cite");
        addLokalisoituteksti(docBase, aihekokonaisuudet.getYleiskuvaus(), "div");

        docBase.getGenerator().increaseNumber();

        Set<Aihekokonaisuus> aihekokonaisuudetSet = docBase.getOps().getAihekokonaisuudet().getAihekokonaisuudet();
        if (aihekokonaisuudetSet != null) {
            docBase.getGenerator().increaseDepth();

            aihekokonaisuudetSet.stream()
                    .map(o -> {
                        if (o.getJnro() == null) {
                            o.setJnro(Long.MAX_VALUE);
                        }
                        return o;
                    })
                    .sorted((o1, o2) -> Long.compare(o1.getJnro(), o2.getJnro()))
                    .forEach(o -> addAihekokonaisuus(docBase, o));

            docBase.getGenerator().decreaseDepth();

            docBase.getGenerator().increaseNumber();
        }
    }

    private void addAihekokonaisuus(DokumenttiBase docBase, Aihekokonaisuus aihekokonaisuus) {
        if (aihekokonaisuus.getTunniste() == null) {
            return;
        }

        // Opsin aihekokonaisuutta vastaava peruste
        Optional<AihekokonaisuusDto> optAihekokonaisuusDto = docBase.getPerusteDto().getLukiokoulutus()
                .getAihekokonaisuudet().getAihekokonaisuudet().stream()
                .filter(o -> o.getTunniste() != null)
                .filter(o -> o.getTunniste().equals(aihekokonaisuus.getTunniste()))
                .findFirst();

        AihekokonaisuusDto perusteAihekokonaisuusDto = null;
        if (optAihekokonaisuusDto.isPresent()) {
            perusteAihekokonaisuusDto = optAihekokonaisuusDto.get();
        }

        // Näytetään opsin otsikko jos saatavilla, muuten käytetään perusteen otsikkoa.
        // Jos kumpaakaan ei ole, näytetään tieto puuttuvasta otsikosta.
        if (aihekokonaisuus.getOtsikko() != null) {
            addHeader(docBase, getTextString(docBase, aihekokonaisuus.getOtsikko()));
        } else if (perusteAihekokonaisuusDto != null) {
            addHeader(docBase, getTextString(docBase, perusteAihekokonaisuusDto.getOtsikko()));
        } else {
            addHeader(docBase, "Aihekokonaisuuden otsikko puuttuu");
        }

        if (perusteAihekokonaisuusDto != null) {
            addLokalisoituteksti(docBase, perusteAihekokonaisuusDto.getYleiskuvaus(), "cite");
        }

        addLokalisoituteksti(docBase, aihekokonaisuus.getYleiskuvaus(), "div");

        docBase.getGenerator().increaseNumber();
    }

    @Override
    public void addOppiaineet(DokumenttiBase docBase) {
        Set<OppiaineLukiokurssi> lukiokurssit = docBase.getOps().getLukiokurssit();

        docBase.getOps().getOppiaineet().stream()
                .forEach(oa -> addOppiaine(docBase, oa.getOppiaine()));
    }

    private void addOppiaine(DokumenttiBase docBase, Oppiaine oppiaine) {
        docBase.getOps().lukiokurssitByOppiaine().apply(oppiaine.getId()).stream()
                .forEach(kurssi -> addKurssi(docBase, kurssi));
    }

    private void addKurssi(DokumenttiBase docBase, OppiaineLukiokurssi kurssi) {
        OppiaineLukiokurssi oppiaineLukiokurssi = kurssi;
    }
}
