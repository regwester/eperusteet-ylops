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

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.ylops.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.ylops.domain.LaajaalainenosaaminenViite;
import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.koodisto.KoodistoKoodi;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.*;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.ops.OpsOppiaine;
import fi.vm.sade.eperusteet.ylops.domain.ops.OpsVuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappaleViite;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Tekstiosa;
import fi.vm.sade.eperusteet.ylops.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.ylops.domain.vuosiluokkakokonaisuus.Laajaalainenosaaminen;
import fi.vm.sade.eperusteet.ylops.domain.vuosiluokkakokonaisuus.Vuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.TermiDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.*;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.DokumenttiBuilderService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.LocalizedMessagesService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.PdfService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.CharapterNumberGenerator;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiBase;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiRivi;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiTaulukko;
import fi.vm.sade.eperusteet.ylops.service.exception.DokumenttiException;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.external.KoodistoService;
import fi.vm.sade.eperusteet.ylops.service.external.OrganisaatioService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.LiiteService;
import fi.vm.sade.eperusteet.ylops.service.ops.TermistoService;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.*;
import org.apache.xml.security.utils.Base64;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 *
 * @author iSaul
 */
@Service
public class DokumenttiBuilderServiceImpl implements DokumenttiBuilderService {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiBuilderServiceImpl.class);

    private static final float COMPRESSION_LEVEL = 0.9f;

    @Autowired
    private LocalizedMessagesService messages;

    @Autowired
    private TermistoService termistoService;

    @Autowired
    private LiiteService liiteService;

    @Autowired
    private KoodistoService koodistoService;

    @Autowired
    private OrganisaatioService organisaatioService;

    @Autowired
    private EperusteetService eperusteetService;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private DtoMapper mapper;

    @Override
    public byte[] generatePdf(Opetussuunnitelma ops, Kieli kieli)
            throws TransformerException, IOException, SAXException,
            ParserConfigurationException, NullPointerException, DokumenttiException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        // Luodaan XHTML pohja
        Element rootElement = doc.createElement("html");
        rootElement.setAttribute("lang", kieli.toString());
        doc.appendChild(rootElement);

        Element headElement = doc.createElement("head");

        // Poistetaan HEAD:in <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
        if (headElement.hasChildNodes())
            headElement.removeChild(headElement.getFirstChild());

        Element bodyElement = doc.createElement("body");

        rootElement.appendChild(headElement);
        rootElement.appendChild(bodyElement);

        // Apuluokka datan säilömiseen generoinin ajaksi
        DokumenttiBase docBase = new DokumenttiBase();
        docBase.setDocument(doc);
        docBase.setHeadElement(headElement);
        docBase.setBodyElement(bodyElement);
        docBase.setOps(ops);
        docBase.setGenerator(new CharapterNumberGenerator());
        docBase.setKieli(kieli);


        // Kansilehti & Infosivu
        addMetaPages(docBase);

        // Sisältöelementit
        addYhteisetOsuudet(docBase);

        // Perusopetus
        if (ops.getKoulutustyyppi() != null && ops.getKoulutustyyppi() == KoulutusTyyppi.PERUSOPETUS) {
            PerusteDto perusteDto = eperusteetService.getPeruste(ops.getPerusteenDiaarinumero());
            if (perusteDto == null) {
                throw new DokumenttiException("Peruste puuttuu", new Throwable());
            }
            docBase.setPerusteDto(perusteDto);
            addVuosiluokkakokonaisuudet(docBase);
        }

        // Alaviitteet
        buildFootnotes(docBase);

        // Kuvat
        buildImages(docBase);

        // PDF luonti XHTML dokumentista
        return pdfService.xhtml2pdf(doc);
    }

    private void addMetaPages(DokumenttiBase docBase) {
        Element title = docBase.getDocument().createElement("title");
        String nimi = getTextString(docBase.getOps().getNimi(), docBase.getKieli());
        if (!docBase.getOps().getTila().equals(Tila.JULKAISTU)) {
            nimi += " (" + docBase.getOps().getTila() + ")";
        }
        title.appendChild(docBase.getDocument().createTextNode(nimi));
        docBase.getHeadElement().appendChild(title);

        String kuvaus = getTextString(docBase.getOps().getKuvaus(), docBase.getKieli());
        if (kuvaus != null && kuvaus.length() != 0) {
            Element description = docBase.getDocument().createElement("meta");
            description.setAttribute("name", "description");
            description.setAttribute("content", kuvaus);
            docBase.getHeadElement().appendChild(description);
        }

        Set<KoodistoKoodi> koodistoKoodit = docBase.getOps().getKunnat();
        if (koodistoKoodit != null) {
            Element municipalities = docBase.getDocument().createElement("kunnat");
            for (KoodistoKoodi koodistoKoodi : koodistoKoodit) {
                Element kuntaEl = docBase.getDocument().createElement("kunta");
                KoodistoKoodiDto koodistoKoodiDto = koodistoService.get("kunta", koodistoKoodi.getKoodiUri());
                if (koodistoKoodiDto != null && koodistoKoodiDto.getMetadata() != null) {
                    for (KoodistoMetadataDto metadata : koodistoKoodiDto.getMetadata()) {
                        if (metadata.getNimi() != null && metadata.getKieli().toLowerCase()
                                .equals(docBase.getKieli().toString().toLowerCase())) {
                            kuntaEl.setTextContent(metadata.getNimi());
                        }
                    }
                }
                municipalities.appendChild(kuntaEl);
            }
            docBase.getHeadElement().appendChild(municipalities);
        }

        // Organisaatiot
        Element organisaatiot = docBase.getDocument().createElement("organisaatiot");

        docBase.getOps().getOrganisaatiot().stream()
                .map(org -> organisaatioService.getOrganisaatio(org))
                .filter(node -> {
                    JsonNode tyypit = node.get("tyypit");
                    if (tyypit.isArray()) {
                        for (JsonNode asd : tyypit) {
                            if (asd.textValue().equals("Koulutustoimija")) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .map(node -> node.get("nimi"))
                .filter(Objects::nonNull)
                .map(x -> x.get(docBase.getKieli().toString()))
                .filter(Objects::nonNull)
                .map(JsonNode::asText)
                .forEach(koulu -> {
                    Element orgEl = docBase.getDocument().createElement("koulu");
                    orgEl.setTextContent(koulu);
                    organisaatiot.appendChild(orgEl);
                });

        docBase.getHeadElement().appendChild(organisaatiot);


        // Päätöspäivämäärä
        Date paatospaivamaara = docBase.getOps().getPaatospaivamaara();
        Element dateEl = docBase.getDocument().createElement("meta");
        dateEl.setAttribute("name", "date");
        if (paatospaivamaara != null) {
            String paatospaivamaaraText = new SimpleDateFormat("d.M.yyyy").format(paatospaivamaara);
            dateEl.setAttribute("content", paatospaivamaaraText);
        } else {
            dateEl.setAttribute("content", "");
        }
        docBase.getHeadElement().appendChild(dateEl);


        // Koulun nimi
        Element koulutEl = docBase.getDocument().createElement("koulut");

        docBase.getOps().getOrganisaatiot().stream()
                .map(org -> organisaatioService.getOrganisaatio(org))
                .filter(node -> {
                    JsonNode tyypit = node.get("tyypit");
                    if (tyypit.isArray()) {
                        for (JsonNode asd : tyypit) {
                            if (asd.textValue().equals("Oppilaitos")) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .map(node -> node.get("nimi"))
                .filter(Objects::nonNull)
                .map(x -> x.get(docBase.getKieli().toString()))
                .filter(Objects::nonNull)
                .map(JsonNode::asText)
                .forEach(koulu -> {
                    Element kouluEl = docBase.getDocument().createElement("koulu");
                    kouluEl.setTextContent(koulu);
                    koulutEl.appendChild(kouluEl);
                });

        docBase.getHeadElement().appendChild(koulutEl);
    }

    private void addYhteisetOsuudet(DokumenttiBase docBase)
            throws IOException, SAXException, ParserConfigurationException {

        if (docBase.getOps().getTekstit() != null) {
            addTekstiKappale(docBase, docBase.getOps().getTekstit(), true);
        }
    }

    private void addTekstiKappale(DokumenttiBase docBase, TekstiKappaleViite viite, boolean paataso)
            throws ParserConfigurationException, IOException, SAXException {

        for (TekstiKappaleViite lapsi : viite.getLapset()) {
            if (lapsi.getTekstiKappale() != null && lapsi.getTekstiKappale().getNimi() != null) {

                // Ei näytetä yhteisen osien Pääkappaleiden otsikoita
                // Opetuksen järjestäminen ja Opetuksen toteuttamisen lähtökohdat
                if (paataso) {
                    addTekstiKappale(docBase, lapsi, false);
                } else {

                    addHeader(docBase, getTextString(lapsi.getTekstiKappale().getNimi(), docBase.getKieli()));

                    // Opsin teksti luvulle
                    if (lapsi.getTekstiKappale().getTeksti() != null) {
                        String teskti = "<div>" + getTextString(lapsi.getTekstiKappale().getTeksti(), docBase.getKieli()) + "</div>";

                        Document tempDoc = new W3CDom().fromJsoup(Jsoup.parseBodyFragment(teskti));
                        Node node = tempDoc.getDocumentElement().getChildNodes().item(1).getFirstChild();

                        docBase.getBodyElement().appendChild(docBase.getDocument().importNode(node, true));

                    }

                    docBase.getGenerator().increaseDepth();

                    // Rekursiivisesti
                    addTekstiKappale(docBase, lapsi, false);

                    docBase.getGenerator().decreaseDepth();
                    docBase.getGenerator().increaseNumber();

                }
            }
        }
    }

    private void addVuosiluokkakokonaisuudet(DokumenttiBase docBase) {
        Set<OpsVuosiluokkakokonaisuus> opsVlkset = docBase.getOps().getVuosiluokkakokonaisuudet();

        // Haetaan vuosiluokkkakokonaisuudet
        ArrayList<Vuosiluokkakokonaisuus> vlkset = new ArrayList<>();
        for (OpsVuosiluokkakokonaisuus opsVlk : opsVlkset) {
            vlkset.add(opsVlk.getVuosiluokkakokonaisuus());
        }

        // Järjestetään aakkosjärjestykseen
        vlkset = vlkset.stream()
                .sorted((vlk1, vlk2) -> vlk1.getNimi().getTeksti().get(docBase.getKieli())
                        .compareTo(vlk2.getNimi().getTeksti().get(docBase.getKieli())))
                .collect(Collectors.toCollection(ArrayList::new));

        vlkset.stream()
                .forEach(vlk -> {
                    String teksti = getTextString(vlk.getNimi(), docBase.getKieli());
                    addHeader(docBase, !teksti.isEmpty() ? teksti : "Vuosiluokkakokonaisuuden otsikko puuttuu");

                    PerusopetuksenPerusteenSisaltoDto poPerusteenSisaltoDto = docBase.getPerusteDto().getPerusopetus();
                    if (poPerusteenSisaltoDto != null && vlk.getTunniste().getId() != null) {
                        Optional<PerusteVuosiluokkakokonaisuusDto> optPerusteVlkDto =
                                poPerusteenSisaltoDto.getVuosiluokkakokonaisuudet(vlk.getTunniste().getId());

                        if (optPerusteVlkDto.isPresent()) {

                            PerusteVuosiluokkakokonaisuusDto perusteVlk = optPerusteVlkDto.get();

                            // Vuosiluokkan sisältö
                            docBase.getGenerator().increaseDepth();


                            // Vuosiluokkakokonaisuuden kohdat

                            addVlkYleisetOsiot(docBase, perusteVlk.getSiirtymaEdellisesta(), vlk.getSiirtymaEdellisesta());

                            addVlkYleisetOsiot(docBase, perusteVlk.getTehtava(), vlk.getTehtava());

                            addVlkYleisetOsiot(docBase, perusteVlk.getSiirtymaSeuraavaan(), vlk.getSiirtymaSeuraavaan());

                            addVlkYleisetOsiot(docBase, perusteVlk.getLaajaalainenOsaaminen(), vlk.getLaajaalainenOsaaminen());

                            addLaajaalaisetOsaamisenAlueet(docBase, perusteVlk, vlk);

                            addOppiaineet(docBase, vlk);

                            docBase.getGenerator().decreaseDepth();

                            docBase.getGenerator().increaseNumber();
                        }
                    }
                });
    }

    private void addVlkYleisetOsiot(DokumenttiBase docBase,
                                    PerusteTekstiOsaDto perusteTekstiOsaDto,
                                    Tekstiosa tekstiosa) {
        // Otsikko
        if (perusteTekstiOsaDto.getOtsikko() != null) {
            addHeader(docBase, getTextString(perusteTekstiOsaDto.getOtsikko(), docBase.getKieli()));

            // Perusteen teksi
            if (perusteTekstiOsaDto.getTeksti() != null) {
                addLokalisoituteksti(docBase, perusteTekstiOsaDto.getTeksti(), "cite");
            }

            // Opsin teksti
            if (tekstiosa != null && tekstiosa.getTeksti() != null) {
                addLokalisoituteksti(docBase, tekstiosa.getTeksti(), "div");
            }

            docBase.getGenerator().increaseNumber();
        }
    }
    private void addLaajaalaisetOsaamisenAlueet(DokumenttiBase docBase,
                                                PerusteVuosiluokkakokonaisuusDto perusteVlk,
                                                Vuosiluokkakokonaisuus vlk) {
        if (perusteVlk.getLaajaalaisetOsaamiset() != null ) {

            addHeader(docBase, messages.translate("laaja-alaisen-osaamisen-alueet", docBase.getKieli()));

            List<PerusteVuosiluokkakokonaisuudenLaajaalainenosaaminenDto> perusteLaajaalaisetOsaamiset = perusteVlk.getLaajaalaisetOsaamiset().stream()
                    .filter((lao -> lao.getLaajaalainenOsaaminen() != null))
                    .sorted((lao1, lao2) -> lao1.getLaajaalainenOsaaminen().getNimi().getTekstit().get(docBase.getKieli())
                            .compareTo(lao2.getLaajaalainenOsaaminen().getNimi().getTekstit().get(docBase.getKieli())))
                    .collect(Collectors.toCollection(ArrayList::new));

            for (PerusteVuosiluokkakokonaisuudenLaajaalainenosaaminenDto perusteLaajaalainenosaaminen : perusteLaajaalaisetOsaamiset) {
                PerusteLaajaalainenosaaminenDto perusteLaajaalainenosaaminenDto = perusteLaajaalainenosaaminen.getLaajaalainenOsaaminen();

                if (perusteLaajaalainenosaaminenDto != null) {
                    docBase.getGenerator().increaseDepth();
                    docBase.getGenerator().increaseDepth();
                    docBase.getGenerator().increaseDepth();
                    docBase.getGenerator().increaseDepth();

                    // otsikko
                    addHeader(docBase, getTextString(perusteLaajaalainenosaaminenDto.getNimi(), docBase.getKieli()));

                    // Perusteen osa
                    addLokalisoituteksti(docBase, perusteLaajaalainenosaaminenDto.getKuvaus(), "cite");

                    // Opsin osa
                    if (perusteLaajaalainenosaaminen.getLaajaalainenOsaaminen() != null
                            && perusteLaajaalainenosaaminen.getLaajaalainenOsaaminen().getTunniste() != null) {
                        Optional<Laajaalainenosaaminen> optLaajaalainenosaaminen = vlk.getLaajaalaisetosaamiset().stream()
                                .filter((l -> l.getLaajaalainenosaaminen().getViite().equals(
                                        perusteLaajaalainenosaaminenDto.getTunniste().toString())))
                                .findFirst();

                        if (optLaajaalainenosaaminen.isPresent()) {
                            addLokalisoituteksti(docBase, optLaajaalainenosaaminen.get().getKuvaus(), "div");
                        }
                    }

                    docBase.getGenerator().decreaseDepth();
                    docBase.getGenerator().decreaseDepth();
                    docBase.getGenerator().decreaseDepth();
                    docBase.getGenerator().decreaseDepth();
                }
            }

            docBase.getGenerator().increaseNumber();
        }
    }

    private void addOppiaineet(DokumenttiBase docBase, Vuosiluokkakokonaisuus vlk) {
        if (docBase.getOps() != null && docBase.getOps().getOppiaineet() != null) {
            Set<OpsOppiaine> oppiaineet = docBase.getOps().getOppiaineet();

            List<OpsOppiaine> oppiaineetAsc = oppiaineet.stream()
                    .filter(oa -> oa.getOppiaine() != null
                            && oa.getOppiaine().getNimi() != null
                            && oa.getOppiaine().getNimi().getTeksti() != null
                            && oa.getOppiaine().getNimi().getTeksti().get(docBase.getKieli()) != null)
                    .sorted((oa1, oa2) -> oa1.getOppiaine().getNimi().getTeksti().get(docBase.getKieli())
                            .compareTo(oa2.getOppiaine().getNimi().getTeksti().get(docBase.getKieli())))
                    .collect(Collectors.toCollection(ArrayList::new));

            addHeader(docBase, messages.translate("oppiaineet", docBase.getKieli()));

            docBase.getGenerator().increaseDepth();

            // Oppiaineet akkosjärjestyksessä
            for (OpsOppiaine opsOppiaine : oppiaineetAsc) {
                Oppiaine oppiaine = opsOppiaine.getOppiaine();

                Set<Oppiaineenvuosiluokkakokonaisuus> oaVlkset = oppiaine.getVuosiluokkakokonaisuudet();


                UUID tunniste = oppiaine.getTunniste();

                // Oppiaine nimi
                addHeader(docBase, getTextString(oppiaine.getNimi(), docBase.getKieli()));

                docBase.getGenerator().increaseDepth();
                docBase.getGenerator().increaseDepth();

                PerusteOppiaineDto perusteOppiaineDto = null;
                PerusteOppiaineenVuosiluokkakokonaisuusDto perusteOaVlkDto = null;
                Oppiaineenvuosiluokkakokonaisuus oaVlk = null;

                Optional<Oppiaineenvuosiluokkakokonaisuus> optOaVlk = oaVlkset.stream()
                        .filter(o -> o.getVuosiluokkakokonaisuus().getId() == vlk.getTunniste().getId())
                        .findFirst();

                if (optOaVlk.isPresent()) {
                    oaVlk = optOaVlk.get();
                    Optional<PerusteOppiaineDto> optPerusteOppiaineDto = docBase.getPerusteDto().getPerusopetus().getOppiaine(tunniste);
                    if (optPerusteOppiaineDto.isPresent()) {
                        perusteOppiaineDto = optPerusteOppiaineDto.get();
                        Optional<PerusteOppiaineenVuosiluokkakokonaisuusDto> optPerusteOaVlkDto =
                                perusteOppiaineDto.getVuosiluokkakokonaisuus(oaVlk.getVuosiluokkakokonaisuus().getId());
                        if (optPerusteOaVlkDto.isPresent()) {
                            perusteOaVlkDto = optPerusteOaVlkDto.get();
                        }
                    }
                }

                // Tehtävä
                addOppiaineTehtava(docBase, oppiaine, perusteOppiaineDto);

                // Oppiaineen vuosiluokkakokonaiuuden kohtaiset
                addOppiaineVuosiluokkkakokonaisuus(docBase, perusteOaVlkDto, oaVlk);

                docBase.getGenerator().decreaseDepth();

                // Oppimäärät
                Set<Oppiaine> oppimaarat = oppiaine.getOppimaarat();
                if (oppimaarat != null) {

                    Set<PerusteOppiaineDto> perusteOppimaarat = null;
                    if (perusteOppiaineDto != null) {
                        perusteOppimaarat = perusteOppiaineDto.getOppimaarat();
                    }

                    addOppimaarat(docBase, perusteOppimaarat, oppimaarat, vlk);
                }

                docBase.getGenerator().decreaseDepth();

                docBase.getGenerator().increaseNumber();
            }
            docBase.getGenerator().decreaseDepth();

            docBase.getGenerator().increaseNumber();
        }
    }

    private void addOppiaineTehtava(DokumenttiBase docBase, Oppiaine oppiaine, PerusteOppiaineDto perusteOppiaineDto) {
        if (perusteOppiaineDto != null) {
            PerusteTekstiOsaDto tehtava = perusteOppiaineDto.getTehtava();
            if (tehtava != null) {
                addHeader(docBase, getTextString(tehtava.getOtsikko(), docBase.getKieli()));
                addLokalisoituteksti(docBase, tehtava.getTeksti(), "cite");
            }
        }
        addTekstiosa(docBase, oppiaine.getTehtava(), "div");
    }

    private void addOppiaineVuosiluokkkakokonaisuus(DokumenttiBase docBase,
                                                    PerusteOppiaineenVuosiluokkakokonaisuusDto perusteOaVlkDto,
                                                    Oppiaineenvuosiluokkakokonaisuus oaVlkDto) {

        if (oaVlkDto == null) {
            return;
        }

        if (perusteOaVlkDto != null) {
            addOppiaineYleisetOsiot(docBase, oaVlkDto.getTehtava(), perusteOaVlkDto.getTehtava());
            addOppiaineYleisetOsiot(docBase, oaVlkDto.getTyotavat(), perusteOaVlkDto.getTyotavat());
            addOppiaineYleisetOsiot(docBase, oaVlkDto.getOhjaus(), perusteOaVlkDto.getOhjaus());
            addOppiaineYleisetOsiot(docBase, oaVlkDto.getArviointi(), perusteOaVlkDto.getArviointi());
            addTavoitteetJaSisaltoalueet(docBase, perusteOaVlkDto, oaVlkDto);
        } else {
            addOppiaineYleisetOsiot(docBase, oaVlkDto.getTehtava(), null);
            addOppiaineYleisetOsiot(docBase, oaVlkDto.getTyotavat(), null);
            addOppiaineYleisetOsiot(docBase, oaVlkDto.getOhjaus(), null);
            addOppiaineYleisetOsiot(docBase, oaVlkDto.getArviointi(), null);
            addTavoitteetJaSisaltoalueet(docBase, null, oaVlkDto);
        }

    }

    private void addTavoitteetJaSisaltoalueet(DokumenttiBase docBase,
                                              PerusteOppiaineenVuosiluokkakokonaisuusDto perusteOaVlkDto,
                                              Oppiaineenvuosiluokkakokonaisuus oaVlkDto) {

        // Tavoitteet vuosiluokittain
        if (oaVlkDto.getVuosiluokat() != null) {
            ArrayList<Oppiaineenvuosiluokka> vuosiluokat = oaVlkDto.getVuosiluokat().stream()
                    .sorted((el1, el2) -> el1.getVuosiluokka().toString().compareTo(el2.getVuosiluokka().toString()))
                    .collect(Collectors.toCollection(ArrayList::new));
            // Vuosiluokka otsikko
            vuosiluokat.stream()
                    .filter(oaVuosiluokka -> oaVuosiluokka.getVuosiluokka() != null)
                    .forEach(oaVuosiluokka -> {
                // Vuosiluokka otsikko
                addHeader(docBase, messages.translate(oaVuosiluokka.getVuosiluokka().toString(), docBase.getKieli()));

                addVuosiluokkaSisaltoalueet(docBase, oaVuosiluokka, perusteOaVlkDto);

                addVuosiluokkaTaulukko(docBase, oaVuosiluokka, perusteOaVlkDto);

                addVuosiluokkaTavoitteet(docBase, oaVuosiluokka, perusteOaVlkDto);
            });
        }
    }

    private void addVuosiluokkaSisaltoalueet(DokumenttiBase docBase,
                                             Oppiaineenvuosiluokka oaVuosiluokka,
                                             PerusteOppiaineenVuosiluokkakokonaisuusDto perusteOaVlkDto) {
        if (oaVuosiluokka.getSisaltoalueet() != null) {
            addHeader(docBase, messages.translate("vuosiluokan-keskeiset-sisaltoalueet", docBase.getKieli()));

            oaVuosiluokka.getSisaltoalueet().stream()
                    .filter(s -> s.getPiilotettu() == null || !s.getPiilotettu())
                    .forEach(ksa -> {
                        docBase.getGenerator().increaseDepth();

                        // Sisältöalue otsikko
                        addHeader(docBase, getTextString(ksa.getNimi(), docBase.getKieli()));

                        // Sisältöalue peruste
                        if (perusteOaVlkDto != null && perusteOaVlkDto.getSisaltoalueet() != null) {
                            Optional<PerusteKeskeinensisaltoalueDto> optPerusteKsa = perusteOaVlkDto.getSisaltoalueet().stream()
                                    .filter(pKsa -> pKsa.getTunniste().equals(ksa.getTunniste()))
                                    .findFirst();
                            if (optPerusteKsa.isPresent()) {
                                addLokalisoituteksti(docBase, optPerusteKsa.get().getKuvaus(), "cite");
                            }
                        }

                        // Sisältöalue ops
                        if (ksa.getKuvaus() != null) {
                            addLokalisoituteksti(docBase, ksa.getKuvaus(), "div");
                        }

                        docBase.getGenerator().decreaseDepth();
            });
        }
    }

    private void addVuosiluokkaTaulukko(DokumenttiBase docBase,
                                        Oppiaineenvuosiluokka oaVuosiluokka,
                                        PerusteOppiaineenVuosiluokkakokonaisuusDto perusteOaVlkDto) {
        // Opetuksen tavoitteet taulukko
        if (oaVuosiluokka.getTavoitteet() != null) {
            DokumenttiTaulukko taulukko = new DokumenttiTaulukko();

            taulukko.addOtsikkoSarake(messages.translate("opetuksen-tavoitteet", docBase.getKieli()));
            taulukko.addOtsikkoSarake(messages.translate("tavoitteisiin-liittyvat-sisaltoalueet", docBase.getKieli()));
            taulukko.addOtsikkoSarake(messages.translate("laaja-alainen-osaaminen", docBase.getKieli()));

            for (Opetuksentavoite opetuksentavoite : oaVuosiluokka.getTavoitteet()) {
                DokumenttiRivi rivi = new DokumenttiRivi();

                // Opsin tavoitetta vastaava perusteen tavoite
                if (perusteOaVlkDto != null) {
                    List<PerusteOpetuksentavoiteDto> perusteTavoitteet = perusteOaVlkDto.getTavoitteet();
                    Optional<PerusteOpetuksentavoiteDto> optPerusteOpetuksentavoiteDto = perusteTavoitteet.stream()
                            .filter((o) -> o.getTunniste().equals(opetuksentavoite.getTunniste()))
                            .findFirst();
                    if (optPerusteOpetuksentavoiteDto.isPresent()) {
                        PerusteOpetuksentavoiteDto perusteOpetuksentavoiteDto = optPerusteOpetuksentavoiteDto.get();

                        if (perusteOpetuksentavoiteDto.getTavoite() != null
                                && perusteOpetuksentavoiteDto.getTavoite().get(docBase.getKieli()) != null) {
                            String tavoite = perusteOpetuksentavoiteDto.getTavoite().get(docBase.getKieli());
                            rivi.addSarake(tavoite.substring(0, tavoite.indexOf(" ")));
                        }



                        // Tavoitteisiin liittyvät sisltöalueet
                        Set<OpetuksenKeskeinensisaltoalue> sisaltoalueet = opetuksentavoite.getSisaltoalueet();

                        if (sisaltoalueet != null) {
                            List<OpetuksenKeskeinensisaltoalue> sisaltoalueetAsc = sisaltoalueet.stream()
                                    .filter(s -> s.getSisaltoalueet() != null
                                            && (s.getSisaltoalueet().getPiilotettu() == null
                                            || !s.getSisaltoalueet().getPiilotettu()))
                                    .sorted((s1, s2) -> s1.getSisaltoalueet().getNimi().getTeksti().get(docBase.getKieli()).compareTo(
                                            s2.getSisaltoalueet().getNimi().getTeksti().get(docBase.getKieli())))
                                    .collect(Collectors.toCollection(ArrayList::new));

                            StringBuilder sisaltoalueetBuilder = new StringBuilder();

                            for (OpetuksenKeskeinensisaltoalue opetuksenKeskeinensisaltoalue : sisaltoalueetAsc) {
                                Keskeinensisaltoalue keskeinensisaltoalue
                                        = opetuksenKeskeinensisaltoalue.getSisaltoalueet();
                                if (keskeinensisaltoalue != null) {
                                    if (keskeinensisaltoalue.getNimi() != null
                                            && keskeinensisaltoalue.getNimi().getTeksti() != null
                                            && keskeinensisaltoalue.getNimi().getTeksti().containsKey(docBase.getKieli())) {
                                        String nimi = keskeinensisaltoalue.getNimi().getTeksti().get(docBase.getKieli());
                                        if(nimi.contains(" ")){
                                            sisaltoalueetBuilder.append(nimi.substring(0, nimi.indexOf(" ")));
                                            sisaltoalueetBuilder.append(", ");
                                        }
                                    }
                                }
                            }
                            String sisaltoalueetString = sisaltoalueetBuilder.toString().replaceAll(", $", "");
                            rivi.addSarake(sisaltoalueetString);
                        }

                        // Laaja-alainen osaaminen
                        Set<LaajaalainenosaaminenViite> laajaalainenosaamisenViitteet = opetuksentavoite.getLaajattavoitteet();
                        if (laajaalainenosaamisenViitteet != null) {
                            StringBuilder laajaalainenOsaaminenBuilder = new StringBuilder();
                            List<String> laajaalaisetLista = new ArrayList<>();

                            for (LaajaalainenosaaminenViite laajaalainenosaaminenViite : laajaalainenosaamisenViitteet) {
                                String viite = laajaalainenosaaminenViite.getViite();
                                if (viite != null) {
                                    // Haetaan laaja-alainen
                                    PerusopetuksenPerusteenSisaltoDto perusopetusPerusteSisaltoDto
                                            = docBase.getPerusteDto().getPerusopetus();
                                    Optional<PerusteVuosiluokkakokonaisuusDto> perusteVlk = perusopetusPerusteSisaltoDto
                                            .getVuosiluokkakokonaisuudet(
                                                    oaVuosiluokka.getKokonaisuus().getVuosiluokkakokonaisuus().getId());

                                    if (perusteVlk.isPresent()) {
                                        if (perusteVlk.get().getLaajaalaisetOsaamiset() != null) {
                                            Optional<PerusteVuosiluokkakokonaisuudenLaajaalainenosaaminenDto> optPerusteLao
                                                    = perusteVlk.get().getLaajaalaisetOsaamiset().stream()
                                                    .filter(Objects::nonNull)
                                                    .filter(lao -> lao.getLaajaalainenOsaaminen() != null
                                                            || lao.getLaajaalainenOsaaminen().getTunniste() != null)
                                                    .filter(lao -> lao.getLaajaalainenOsaaminen().getTunniste().toString()
                                                            .equals(laajaalainenosaaminenViite.getViite()))
                                                    .findFirst();

                                            if (optPerusteLao.isPresent()) {
                                                PerusteVuosiluokkakokonaisuudenLaajaalainenosaaminenDto perusteLao = optPerusteLao.get();
                                                if (perusteLao.getLaajaalainenOsaaminen() != null
                                                        || perusteLao.getLaajaalainenOsaaminen().getNimi() != null
                                                        || perusteLao.getLaajaalainenOsaaminen()
                                                        .getNimi().get(docBase.getKieli()) != null) {

                                                    String nimi = perusteLao.getLaajaalainenOsaaminen()
                                                            .getNimi().get(docBase.getKieli());

                                                    if(nimi.contains(" ")){
                                                        laajaalaisetLista.add(nimi.substring(0, nimi.indexOf(" ")));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            // Laaja-alaiset aakkosjärjestykseen
                            laajaalaisetLista.stream()
                                    .sorted(String::compareTo)
                                    .forEach(el ->  {
                                        laajaalainenOsaaminenBuilder.append(el);
                                        laajaalainenOsaaminenBuilder.append(", ");
                                    });
                            rivi.addSarake(laajaalainenOsaaminenBuilder.toString().replaceAll(", $", ""));
                        }

                        taulukko.addRivi(rivi);
                    }
                }
            }

            taulukko.addToDokumentti(docBase);
        }
    }

    private void addVuosiluokkaTavoitteet(DokumenttiBase docBase,
                                          Oppiaineenvuosiluokka oaVuosiluokka,
                                          PerusteOppiaineenVuosiluokkakokonaisuusDto perusteOaVlkDto) {
        if (oaVuosiluokka.getTavoitteet() != null) {

            addHeader(docBase, messages.translate("vuosiluokan-tavoitteet", docBase.getKieli()));

            for (Opetuksentavoite opetuksentavoite : oaVuosiluokka.getTavoitteet()) {

                // Opsin tavoitetta vastaava perusteen tavoite ja perusteen arviointi tavoitteelle
                PerusteOpetuksentavoiteDto perusteOpetuksentavoiteDto = null;
                if (perusteOaVlkDto != null) {
                    List<PerusteOpetuksentavoiteDto> perusteTavoitteet = perusteOaVlkDto.getTavoitteet();
                    Optional<PerusteOpetuksentavoiteDto> optPerusteOpetuksentavoiteDto = perusteTavoitteet.stream()
                            .filter((o) -> o.getTunniste().equals(opetuksentavoite.getTunniste()))
                            .findFirst();
                    if (optPerusteOpetuksentavoiteDto.isPresent()) {
                        perusteOpetuksentavoiteDto = optPerusteOpetuksentavoiteDto.get();
                    }
                }

                if (perusteOpetuksentavoiteDto != null
                        && perusteOpetuksentavoiteDto.getTavoite() != null) {

                    // Tavoitteen otsikko
                    addLokalisoituteksti(docBase, perusteOpetuksentavoiteDto.getTavoite(), "h5");

                    // Ops tavoite
                    addLokalisoituteksti(docBase, opetuksentavoite.getTavoite(), "div");

                    // Tavoitteen arviointi
                    DokumenttiTaulukko taulukko = new DokumenttiTaulukko();
                    taulukko.addOtsikko(messages.translate("arviointi-vuosiluokan-paatteeksi", docBase.getKieli()));
                    taulukko.addOtsikkoSarake(messages.translate("arvioinnin-kohde", docBase.getKieli()));
                    taulukko.addOtsikkoSarake(messages.translate("arvion-hyva-osaaminen", docBase.getKieli()));

                    perusteOpetuksentavoiteDto.getArvioinninkohteet().stream()
                    .forEach(perusteenTavoitteenArviointi -> {
                        DokumenttiRivi rivi = new DokumenttiRivi();
                        String kohde = "";
                        if (perusteenTavoitteenArviointi.getArvioinninKohde() != null
                                && perusteenTavoitteenArviointi.getArvioinninKohde().get(docBase.getKieli()) != null) {
                            kohde = unescapeHtml5(perusteenTavoitteenArviointi.getArvioinninKohde().get(docBase.getKieli()));
                        }
                        rivi.addSarake(kohde);
                        String kuvaus = "";
                        if (perusteenTavoitteenArviointi.getHyvanOsaamisenKuvaus() != null
                                && perusteenTavoitteenArviointi.getHyvanOsaamisenKuvaus().get(docBase.getKieli()) != null) {
                            kuvaus = unescapeHtml5(perusteenTavoitteenArviointi.getHyvanOsaamisenKuvaus().get(docBase.getKieli()));
                        }
                        rivi.addSarake(kuvaus);
                        taulukko.addRivi(rivi);
                    });

                    taulukko.addToDokumentti(docBase);

                    // Tavoitteen sisaltoalueet
                    addVuosiluokkaTavoitteenSisaltoalueet(docBase, opetuksentavoite);
                }
            }
        }
    }

    private void addVuosiluokkaTavoitteenSisaltoalueet(DokumenttiBase docBase,
                                                       Opetuksentavoite opetuksentavoite) {

        Set<OpetuksenKeskeinensisaltoalue> sisaltoalueet = opetuksentavoite.getSisaltoalueet();
        List<OpetuksenKeskeinensisaltoalue> sisaltoalueetAsc = sisaltoalueet.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getSisaltoalueet() != null
                        && (s.getSisaltoalueet().getPiilotettu() == null
                        || !s.getSisaltoalueet().getPiilotettu()))
                .sorted((s1, s2) -> s1.getSisaltoalueet().getNimi().getTeksti().get(docBase.getKieli()).compareTo(
                        s2.getSisaltoalueet().getNimi().getTeksti().get(docBase.getKieli())))
                .collect(Collectors.toCollection(ArrayList::new));

        if (sisaltoalueetAsc.size() > 0) {

            sisaltoalueetAsc.stream()
                    .forEach(s -> {
                        if (s.getSisaltoalueet() != null) {

                            // Tavoitteen sisältöalue
                            if (s.getOmaKuvaus() != null) {
                                addLokalisoituteksti(docBase, s.getSisaltoalueet().getNimi(), "h6");
                                addLokalisoituteksti(docBase, s.getOmaKuvaus(), "div");
                            }
                        }
                    });
        }
    }

    private void addOppiaineYleisetOsiot(DokumenttiBase docBase, Tekstiosa tekstiosa, PerusteTekstiOsaDto perusteTekstiOsaDto) {
        if (tekstiosa != null) {
            LokalisoituTeksti otsikko = tekstiosa.getOtsikko();
            if (otsikko != null) {
                addHeader(docBase, getTextString(otsikko, docBase.getKieli()));
            } else if (perusteTekstiOsaDto != null) {
                addHeader(docBase, getTextString(perusteTekstiOsaDto.getOtsikko(), docBase.getKieli()));
                addLokalisoituteksti(docBase, perusteTekstiOsaDto.getTeksti(), "cite");
            }

            addLokalisoituteksti(docBase, tekstiosa.getTeksti(), "div");
        }
    }

    private void addOppimaarat(DokumenttiBase docBase, Set<PerusteOppiaineDto> perusteOppimaarat,
                               Set<Oppiaine> oppimaarat, Vuosiluokkakokonaisuus vlk) {
        if (oppimaarat != null) {
            for (Oppiaine oppimaara : oppimaarat) {
                PerusteOppiaineDto perusteOppiaineDto = null;
                if (perusteOppimaarat != null) {
                    Optional<PerusteOppiaineDto> optPerusteOppimaara = perusteOppimaarat.stream()
                            .filter(perusteOppiaine -> perusteOppiaine.getTunniste().equals(oppimaara.getTunniste()))
                            .findFirst();

                    if (optPerusteOppimaara.isPresent()) {
                        perusteOppiaineDto = optPerusteOppimaara.get();
                    }
                }

                // Jos on koosteinen oppimäärä ja oppimäärälle ei löydy perustetta
                // perusteen oppiaineesta, käytetään opsin perusteen oppiainetta
                if (oppimaara.getOppiaine().isKoosteinen() && perusteOppiaineDto == null) {
                    Optional<PerusteOppiaineDto> optPerusteOppiaineDto = docBase.getPerusteDto().getPerusopetus()
                            .getOppiaine(oppimaara.getTunniste());
                    if (optPerusteOppiaineDto.isPresent()) {
                        perusteOppiaineDto = optPerusteOppiaineDto.get();
                    }
                }

                addOppimaara(docBase, perusteOppiaineDto, oppimaara, vlk);
            }
        }
    }

    private void addOppimaara(DokumenttiBase docBase, PerusteOppiaineDto perusteOppiaineDto,
                              Oppiaine oppiaine, Vuosiluokkakokonaisuus vlk) {
        Optional<Oppiaineenvuosiluokkakokonaisuus> optOaVlk
                = oppiaine.getVuosiluokkakokonaisuus(vlk.getTunniste().getId());

        if (!optOaVlk.isPresent()) {
            return;
        }

        // Oppimäärä otsikko
        addHeader(docBase, getTextString(oppiaine.getNimi(), docBase.getKieli()));

        docBase.getGenerator().increaseDepth();

        PerusteTekstiOsaDto perusteTekstiOsaDto = null;
        if (perusteOppiaineDto != null) {
            perusteTekstiOsaDto = perusteOppiaineDto.getTehtava();
        }

        // Tehtävä
        addOppiaineYleisetOsiot(docBase, oppiaine.getTehtava(), perusteTekstiOsaDto);

        // Peruste
        PerusteOppiaineenVuosiluokkakokonaisuusDto perusteOaVlkDto = null;

        if (perusteOppiaineDto != null) {
            Optional<PerusteOppiaineenVuosiluokkakokonaisuusDto> optPerusteOaVlkDto
                    = perusteOppiaineDto.getVuosiluokkakokonaisuus(vlk.getTunniste().getId());
            if (optPerusteOaVlkDto.isPresent()) {
                perusteOaVlkDto = optPerusteOaVlkDto.get();
            }
        }

        // Oppimäärän vuosiluokkakokonaiuuden kohtaiset
        addOppiaineVuosiluokkkakokonaisuus(docBase, perusteOaVlkDto, optOaVlk.get());

        docBase.getGenerator().decreaseDepth();
        docBase.getGenerator().increaseNumber();
    }

    private void buildFootnotes(DokumenttiBase docBase) {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        try {
            XPathExpression expression = xpath.compile("//abbr");
            NodeList list = (NodeList) expression.evaluate(docBase.getDocument(), XPathConstants.NODESET);

            int noteNumber = 1;
            for (int i = 0; i < list.getLength(); i++) {
                Element element = (Element) list.item(i);
                Node node = list.item(i);
                if (node.getAttributes() != null & node.getAttributes().getNamedItem("data-viite") != null) {
                    String avain = node.getAttributes().getNamedItem("data-viite").getNodeValue();

                    if (docBase.getOps() != null && docBase.getOps().getId() != null) {
                        TermiDto termiDto = termistoService.getTermi(docBase.getOps().getId(), avain);

                        // todo: perusteen viite
                        //if (termiDto == null) {}
                        if (termiDto != null && termiDto.isAlaviite() && termiDto.getSelitys() != null) {
                            element.setAttribute("number", String.valueOf(noteNumber));

                            LokalisoituTekstiDto tekstiDto = termiDto.getSelitys();
                            String selitys = getTextString(
                                    mapper.map(tekstiDto, LokalisoituTeksti.class), docBase.getKieli())
                                    .replaceAll("<[^>]+>", ""); // Tällä hetkellä tuetaan vain tekstiä
                            element.setAttribute("text", selitys);
                            noteNumber++;
                        }
                    }
                }
            }

        } catch (XPathExpressionException e) {
            LOG.error(e.getLocalizedMessage());
        }
    }

    private void buildImages(DokumenttiBase docBase) {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        try {
            XPathExpression expression = xpath.compile("//img");
            NodeList list = (NodeList) expression.evaluate(docBase.getDocument(), XPathConstants.NODESET);

            for (int i = 0; i < list.getLength(); i++) {
                Element element = (Element) list.item(i);
                String id = element.getAttribute("data-uid");

                UUID uuid = UUID.fromString(id);

                // Ladataan kuvan data muistiin
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                liiteService.export(docBase.getOps().getId(), uuid, byteArrayOutputStream);

                // Tehdään muistissa olevasta datasta kuva
                InputStream in = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                BufferedImage bufferedImage = ImageIO.read(in);

                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();

                // Muutetaan kaikkien kuvien väriavaruus RGB:ksi jotta PDF/A validointi menee läpi
                // Asetetaan lisäksi läpinäkyvien kuvien taustaksi valkoinen väri
                BufferedImage tempImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
                        BufferedImage.TYPE_3BYTE_BGR);
                tempImage.getGraphics().setColor(new Color(255, 255, 255, 0));
                tempImage.getGraphics().fillRect (0, 0, width, height);
                tempImage.getGraphics().drawImage(bufferedImage, 0, 0, null);
                bufferedImage = tempImage;

                ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
                ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
                jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                jpgWriteParam.setCompressionQuality(COMPRESSION_LEVEL);

                // Muunnetaan kuva base64 enkoodatuksi
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                MemoryCacheImageOutputStream imageStream = new MemoryCacheImageOutputStream(out);
                jpgWriter.setOutput(imageStream);
                IIOImage outputImage = new IIOImage(bufferedImage, null, null);
                jpgWriter.write(null, outputImage, jpgWriteParam);
                jpgWriter.dispose();
                String base64 = Base64.encode(out.toByteArray());

                // Lisätään bas64 kuva img elementtiin
                element.setAttribute("width", String.valueOf(width));
                element.setAttribute("height", String.valueOf(height));
                element.setAttribute("src", "data:image/jpg;base64," + base64);
            }

        } catch (XPathExpressionException | IOException e) {
            LOG.error(e.getLocalizedMessage());
        }
    }

    private void addLokalisoituteksti(DokumenttiBase docBase, LokalisoituTekstiDto lTekstiDto, String tagi) {
        if (lTekstiDto != null) {
            addLokalisoituteksti(docBase, mapper.map(lTekstiDto, LokalisoituTeksti.class), tagi);
        }
    }

    private void addLokalisoituteksti(DokumenttiBase docBase, LokalisoituTeksti lTeksti, String tagi) {
        if (lTeksti != null && lTeksti.getTeksti() != null && lTeksti.getTeksti().get(docBase.getKieli()) != null) {
            String teksti = lTeksti.getTeksti().get(docBase.getKieli());
            teksti = "<" + tagi + ">" + unescapeHtml5(teksti) + "</" + tagi + ">";

            Document tempDoc = new W3CDom().fromJsoup(Jsoup.parseBodyFragment(teksti));
            Node node = tempDoc.getDocumentElement().getChildNodes().item(1).getFirstChild();

            docBase.getBodyElement().appendChild(docBase.getDocument().importNode(node, true));
        }
    }

    private void addTekstiosa(DokumenttiBase docBase,  Tekstiosa tekstiosa, String tagi) {
        if (tekstiosa != null) {
            LokalisoituTeksti otsikko = tekstiosa.getOtsikko();
            LokalisoituTeksti teksti = tekstiosa.getTeksti();
            if (otsikko != null) {
                addLokalisoituteksti(docBase, otsikko, tagi);
            }
            if (teksti != null) {
                addLokalisoituteksti(docBase, teksti, tagi);
            }
        }
    }

    private void addHeader(DokumenttiBase docBase, String text) {
        if (text != null) {
            Element header = docBase.getDocument().createElement("h" + docBase.getGenerator().getDepth());
            header.setAttribute("number", docBase.getGenerator().generateNumber());
            header.appendChild(docBase.getDocument().createTextNode(unescapeHtml5(text)));
            docBase.getBodyElement().appendChild(header);
        }
    }

    private String getTextString(LokalisoituTekstiDto lokalisoituTekstiDto, Kieli kieli) {
        LokalisoituTeksti lokalisoituTeksti = mapper.map(lokalisoituTekstiDto, LokalisoituTeksti.class);
        return getTextString(lokalisoituTeksti, kieli);
    }

    private static String getTextString(LokalisoituTeksti lokalisoituTeksti, Kieli kieli) {
        if (lokalisoituTeksti == null || lokalisoituTeksti.getTeksti() == null
                || lokalisoituTeksti.getTeksti().get(kieli) == null) {
            return "";
        } else {
            return unescapeHtml5(lokalisoituTeksti.getTeksti().get(kieli));
        }
    }

    private static String unescapeHtml5(String string) {
        return Jsoup.clean(string, ValidHtml.WhitelistType.NORMAL.getWhitelist());
    }
}
