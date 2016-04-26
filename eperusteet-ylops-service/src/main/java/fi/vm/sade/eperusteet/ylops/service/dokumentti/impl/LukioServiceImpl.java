package fi.vm.sade.eperusteet.ylops.service.dokumentti.impl;

import fi.vm.sade.eperusteet.ylops.domain.lukio.*;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaine;
import fi.vm.sade.eperusteet.ylops.domain.ops.OpsOppiaine;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Tekstiosa;
import fi.vm.sade.eperusteet.ylops.dto.lukio.LukioOpetussuunnitelmaRakenneOpsDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteOppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteTekstiOsaDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.lukio.*;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.LocalizedMessagesService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.LukioService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiBase;
import fi.vm.sade.eperusteet.ylops.service.ops.lukio.LukioOpetussuunnitelmaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiUtils.*;

/**
 * @author isaul
 */
@Service
public class LukioServiceImpl implements LukioService {
    @Autowired
    private LocalizedMessagesService messages;

    @Autowired
    private LukioOpetussuunnitelmaService lukioOpetussuunnitelmaService;

    public void addOppimistavoitteetJaOpetuksenKeskeisetSisallot(DokumenttiBase docBase) {
        addHeader(docBase, messages.translate("oppimistavoitteet-ja-opetuksen-keskeiset-sisallot", docBase.getKieli()));
        docBase.getGenerator().increaseDepth();

        addOpetuksenYleisetTavoitteet(docBase);
        addAihekokonaisuudet(docBase);
        addOppiaineet(docBase);

        docBase.getGenerator().decreaseDepth();
    }

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

    public void addAihekokonaisuudet(DokumenttiBase docBase) {
        Aihekokonaisuudet aihekokonaisuudet = docBase.getOps().getAihekokonaisuudet();
        AihekokonaisuudetDto perusteAihekokonaisuudet = docBase.getPerusteDto().getLukiokoulutus().getAihekokonaisuudet();
        if (aihekokonaisuudet == null || perusteAihekokonaisuudet == null) {
            return;
        }

        addHeader(docBase, getTextString(docBase, perusteAihekokonaisuudet.getOtsikko()));
        addLokalisoituteksti(docBase, perusteAihekokonaisuudet.getYleiskuvaus(), "cite");
        addLokalisoituteksti(docBase, aihekokonaisuudet.getYleiskuvaus(), "div");

        Set<Aihekokonaisuus> aihekokonaisuudetSet = docBase.getOps().getAihekokonaisuudet().getAihekokonaisuudet();
        if (aihekokonaisuudetSet != null) {
            docBase.getGenerator().increaseDepth();
            docBase.getGenerator().increaseDepth();
            docBase.getGenerator().increaseDepth();
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
            docBase.getGenerator().decreaseDepth();
            docBase.getGenerator().decreaseDepth();
            docBase.getGenerator().decreaseDepth();
        }

        docBase.getGenerator().increaseNumber();
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
            addLokalisoituteksti(docBase, aihekokonaisuus.getOtsikko(), "h6");
        } else if (perusteAihekokonaisuusDto != null) {
            addLokalisoituteksti(docBase, perusteAihekokonaisuusDto.getOtsikko(), "h6");
        } else {
            addTeksti(docBase, "Aihekokonaisuuden otsikko puuttuu", "h6");
        }

        if (perusteAihekokonaisuusDto != null) {
            addLokalisoituteksti(docBase, perusteAihekokonaisuusDto.getYleiskuvaus(), "cite");
        }

        addLokalisoituteksti(docBase, aihekokonaisuus.getYleiskuvaus(), "div");

        docBase.getGenerator().increaseNumber();
    }

    public void addOppiaineet(DokumenttiBase docBase) {

        LukioOpetussuunnitelmaRakenneOpsDto lukioOpetussuunnitelmaRakenneOpsDto
                = lukioOpetussuunnitelmaService.getRakenne(docBase.getOps().getId());

        // Rakenteen mukaisesti oppiaineet
        lukioOpetussuunnitelmaRakenneOpsDto.getOppiaineet().stream()
                .forEach(oaRakenne ->  {
                    Optional<Oppiaine> optOppiaine = docBase.getOps().getOppiaineet().stream()
                            .map(OpsOppiaine::getOppiaine)
                            .filter(oa -> oa.getTunniste().equals(oaRakenne.getTunniste()))
                            .findFirst();

                    if (optOppiaine.isPresent()) {
                        addOppiaine(docBase, optOppiaine.get());
                    }
                });
    }

    private void addOppiaine(DokumenttiBase docBase, Oppiaine oppiaine) {
        LukioOpetussuunnitelmaRakenneDto perusteRakenne = docBase.getPerusteDto().getLukiokoulutus().getRakenne();
        if (perusteRakenne == null || oppiaine == null) {
            return;
        }

        Optional<LukioPerusteOppiaineDto> optPerusteOppiaine = perusteRakenne.getOppiaineet().stream()
                .filter(oa -> oa.getTunniste().equals(oppiaine.getTunniste()))
                .findFirst();

        // Oppiainetta vastaava perusteen osa
        LukioPerusteOppiaineDto perusteOppiaine = null;

        if (optPerusteOppiaine.isPresent()) {
            perusteOppiaine = optPerusteOppiaine.get();
        }

        // Oppiaineen nimi
        addHeader(docBase, getTextString(docBase, oppiaine.getNimi()));


        if (perusteOppiaine != null && perusteOppiaine.getTehtava() != null) {
            addLokalisoituteksti(docBase, perusteOppiaine.getTehtava().getTeksti(), "cite");
        }
        if (oppiaine.getTehtava() != null) {
            addLokalisoituteksti(docBase, oppiaine.getTehtava().getTeksti(), "div");
        }

        if (perusteOppiaine != null) {
            addYleinenKuvaus(docBase, oppiaine.getTavoitteet(), perusteOppiaine.getTavoitteet());
            addYleinenKuvaus(docBase, oppiaine.getArviointi(), perusteOppiaine.getArviointi());
        } else {
            addYleinenKuvaus(docBase, oppiaine.getTavoitteet(), null);
            addYleinenKuvaus(docBase, oppiaine.getArviointi(), null);
        }

        docBase.getGenerator().increaseDepth();

        // Oppimäärät
        Set<Oppiaine> oppimaarat = oppiaine.getOppimaarat();
        if (oppimaarat != null) {
            oppimaarat.stream()
                    .forEach(om -> addOppiaine(docBase, om));
        }

        // Valtakunnallinen pakolliset
        addTeksti(docBase, messages.translate("pakolliset-kurssit", docBase.getKieli()), "h6");
        if (perusteOppiaine != null) {
            addLokalisoituteksti(docBase, perusteOppiaine.getPakollinenKurssiKuvaus(), "cite");
        }
        addLokalisoituteksti(docBase, oppiaine.getValtakunnallinenPakollinenKuvaus(), "div");
        addKurssiByTyyppi(docBase, oppiaine, perusteOppiaine, LukiokurssiTyyppi.VALTAKUNNALLINEN_PAKOLLINEN);

        // Valtakunnalliset syventävät
        addTeksti(docBase, messages.translate("valtakunnalliset-syventavat-kurssit", docBase.getKieli()), "h6");
        addKurssiByTyyppi(docBase, oppiaine, perusteOppiaine, LukiokurssiTyyppi.VALTAKUNNALLINEN_SYVENTAVA);

        // Paikalliset syventävät
        addTeksti(docBase, messages.translate("paikalliset-syventavat-kurssit", docBase.getKieli()), "h6");
        addKurssiByTyyppi(docBase, oppiaine, perusteOppiaine, LukiokurssiTyyppi.PAIKALLINEN_SYVENTAVA);

        // Paikalliset soveltavat
        addTeksti(docBase, messages.translate("paikalliset-soveltavat-kurssit", docBase.getKieli()), "h6");
        addKurssiByTyyppi(docBase, oppiaine, perusteOppiaine, LukiokurssiTyyppi.PAIKALLINEN_SOVELTAVA);

        docBase.getGenerator().decreaseDepth();

        docBase.getGenerator().increaseNumber();
    }

    private void addKurssiByTyyppi(DokumenttiBase docBase, Oppiaine oppiaine, LukioPerusteOppiaineDto perusteOppiaine, LukiokurssiTyyppi tyyppi) {
        Set<LukiokurssiPerusteDto> perusteKurssit = perusteOppiaine != null ? perusteOppiaine.getKurssit() : null;
        docBase.getOps().lukiokurssitByOppiaine().apply(oppiaine.getId()).stream()
                .filter(kurssi -> kurssi.getKurssi().getTyyppi().equals(tyyppi))
                .forEach(kurssi -> {
                    LukiokurssiPerusteDto perusteKurssi = null;
                    if (perusteKurssit != null) {
                        Optional<LukiokurssiPerusteDto> optPerusteKurssi = perusteKurssit.stream()
                                .filter(pKurssi -> pKurssi.getTunniste().equals(kurssi.getKurssi().getTunniste()))
                                .findFirst();
                        if (optPerusteKurssi.isPresent()) {
                            perusteKurssi = optPerusteKurssi.get();
                        }
                    }
                    addKurssi(docBase, kurssi, perusteKurssi);
                });
    }

    private void addKurssi(DokumenttiBase docBase, OppiaineLukiokurssi oppiaineLukiokurssi, LukiokurssiPerusteDto perusteKurssi) {
        if (oppiaineLukiokurssi.getKurssi() == null) {
            return;
        }

        Lukiokurssi lukiokurssi = oppiaineLukiokurssi.getKurssi();

        // Kurssin otsikko
        StringBuilder otsikko = new StringBuilder();
        if (oppiaineLukiokurssi.getJarjestys() != null) {
            otsikko.append(oppiaineLukiokurssi.getJarjestys());
            otsikko.append(". ");
        }
        otsikko.append(getTextString(docBase, lukiokurssi.getNimi()));
        if (lukiokurssi.getLokalisoituKoodi() != null) {
            otsikko.append(" (");
            otsikko.append(getTextString(docBase, lukiokurssi.getLokalisoituKoodi()));
            otsikko.append(")");
        }
        addTeksti(docBase, otsikko.toString(), "h5");

        // Kuvaus
        if (perusteKurssi != null) {
            addLokalisoituteksti(docBase, perusteKurssi.getKuvaus(), "cite");
        }
        addLokalisoituteksti(docBase, lukiokurssi.getKuvaus(), "div");

        if (perusteKurssi != null) {
            addYleinenKuvaus(docBase, lukiokurssi.getTavoitteet(), perusteKurssi.getTavoitteet());
            addYleinenKuvaus(docBase, lukiokurssi.getKeskeinenSisalto(), perusteKurssi.getKeskeisetSisallot());
        } else {
            addYleinenKuvaus(docBase, lukiokurssi.getTavoitteet(), null);
            addYleinenKuvaus(docBase, lukiokurssi.getKeskeinenSisalto(), null);
        }

        docBase.getGenerator().increaseNumber();
    }

    private void addYleinenKuvaus(DokumenttiBase docBase, Tekstiosa tekstiosa, PerusteTekstiOsaDto perusteTekstiosa) {
        if (tekstiosa != null && tekstiosa.getOtsikko() != null) {
            addLokalisoituteksti(docBase, tekstiosa.getOtsikko(), "h6");
        } else if (perusteTekstiosa != null && perusteTekstiosa.getOtsikko() != null) {
            addLokalisoituteksti(docBase, perusteTekstiosa.getOtsikko(), "h6");
        } else {
            return;
        }

        if (perusteTekstiosa != null) {
            addLokalisoituteksti(docBase, perusteTekstiosa.getTeksti(), "cite");
        }
        if (tekstiosa != null) {
            addLokalisoituteksti(docBase, tekstiosa.getTeksti(), "div");
        }
    }
}
