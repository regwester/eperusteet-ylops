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
import fi.vm.sade.eperusteet.ylops.domain.koodisto.KoodistoKoodi;
import fi.vm.sade.eperusteet.ylops.domain.ohje.OhjeTyyppi;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Keskeinensisaltoalue;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaine;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaineenvuosiluokka;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaineenvuosiluokkakokonaisuus;
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
import fi.vm.sade.eperusteet.ylops.dto.ohje.OhjeDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.TermiDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.*;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.DokumenttiBuilderService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.LocalizedMessagesService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.PdfService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.CharapterNumberGenerator;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.external.KoodistoService;
import fi.vm.sade.eperusteet.ylops.service.external.OrganisaatioService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ohje.OhjeService;
import fi.vm.sade.eperusteet.ylops.service.ops.LiiteService;
import fi.vm.sade.eperusteet.ylops.service.ops.TermistoService;
import org.apache.xml.security.utils.Base64;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
    private OhjeService ohjeService;

    @Autowired
    @Qualifier("koodistoServiceImpl")
    private KoodistoService koodistoService;

    @Autowired
    @Qualifier("organisaatioServiceImpl")
    private OrganisaatioService organisaatioService;

    @Autowired
    private EperusteetService eperusteetService;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private DtoMapper mapper;

    @Override
    public byte[] generatePdf(Opetussuunnitelma ops, Kieli kieli)
            throws TransformerException, IOException, SAXException, ParserConfigurationException, NullPointerException {

        // Säilytetään luonnin aikana pdf data muistissa
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // Luodaan XHTML pohja
        Document doc = docBuilder.newDocument();
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

        // Aloitetaan varsinaisen dokumentin luonti
        CharapterNumberGenerator generator = new CharapterNumberGenerator();


        // Kansilehti & Infosivu
        addMetaPages(doc, headElement, ops, kieli);

        // Sisältöelementit
        addSisaltoElement(doc, bodyElement, ops, generator, kieli);



        // Vuosiluokkakokonaisuudet ja oppiaineet
        if (ops.getKoulutustyyppi() == KoulutusTyyppi.PERUSOPETUS) {

            PerusteDto perusteDto;
            if (ops.getCachedPeruste() != null && ops.getCachedPeruste().getPerusteId() != null) {
                perusteDto = eperusteetService.getEperusteetPeruste(ops.getCachedPeruste().getPerusteId());
            } else {
                perusteDto = eperusteetService.getPeruste(ops.getPerusteenDiaarinumero());
            }

            addVuosiluokkakokonaisuudet(doc, bodyElement, ops, perusteDto, generator, kieli);
        }

        // Alaviitteet
        buildFootnotes(doc, ops, kieli);

        // Kuvat
        buildImages(doc, ops);

        // PDF luonti dokumentista
        pdf.write(pdfService.xhtml2pdf(doc));

        return pdf.toByteArray();
    }

    private void addMetaPages(Document doc, Element headElement, Opetussuunnitelma ops, Kieli kieli) {
        Element title = doc.createElement("title");
        String nimi = getTextString(ops.getNimi(), kieli);
        title.appendChild(doc.createTextNode(nimi));
        headElement.appendChild(title);

        String tyyppi = messages.translate(ops.getKoulutustyyppi().toString(), kieli);
        Element type = doc.createElement("meta");
        type.setAttribute("name", "type");
        type.setAttribute("content", tyyppi);
        headElement.appendChild(type);

        String kuvaus = getTextString(ops.getKuvaus(), kieli);
        if (kuvaus != null && kuvaus.length() != 0) {
            Element description = doc.createElement("meta");
            description.setAttribute("name", "description");
            description.setAttribute("content", kuvaus);
            headElement.appendChild(description);
        }

        String diaarinumero = ops.getPerusteenDiaarinumero();
        if (diaarinumero != null  && diaarinumero.length() != 0) {
            Element diary = doc.createElement("meta");
            diary.setAttribute("name", "diary");
            diary.setAttribute("content", diaarinumero);
            headElement.appendChild(diary);
        }

        Set<KoodistoKoodi> koodistoKoodit = ops.getKunnat();
        Element municipalities = doc.createElement("municipalities");
        for (KoodistoKoodi koodistoKoodi : koodistoKoodit) {
            Element kuntaEl = doc.createElement("municipality");
            KoodistoKoodiDto koodistoKoodiDto = koodistoService.get("kunta", koodistoKoodi.getKoodiUri());
            if (koodistoKoodiDto != null) {
                for (KoodistoMetadataDto metadata : koodistoKoodiDto.getMetadata()) {
                    if (metadata.getKieli().toLowerCase()
                            .equals(kieli.toString().toLowerCase()))
                        kuntaEl.setTextContent(metadata.getNimi());
                }
            }

            municipalities.appendChild(kuntaEl);
        }
        headElement.appendChild(municipalities);

        Set<String> organisaatiot = ops.getOrganisaatiot();
        Element organizations = doc.createElement("organizations");
        for (String org : organisaatiot) {
            JsonNode orgNode = organisaatioService.getOrganisaatio(org);
            JsonNode nimiNode = orgNode.get("nimi");
            Element orgEl = doc.createElement("organization");
            orgEl.setTextContent(nimiNode.get(kieli.toString()).asText());
            organizations.appendChild(orgEl);
        }
        headElement.appendChild(organizations);

        Date paatospaivamaara = ops.getPaatospaivamaara();
        if (paatospaivamaara != null) {
            String paatospaivamaaraText = new SimpleDateFormat("d.m.yyyy").format(paatospaivamaara);
            Element dateEl = doc.createElement("meta");
            dateEl.setAttribute("name", "date");
            dateEl.setAttribute("content", paatospaivamaaraText);
            headElement.appendChild(dateEl);
        }
    }

    private void addSisaltoElement(Document doc, Element rootElement, Opetussuunnitelma ops,
                                   CharapterNumberGenerator generator, Kieli kieli)
            throws IOException, SAXException, ParserConfigurationException {

        generator.increaseDepth();

        for (TekstiKappaleViite viite : ops.getTekstit().getLapset()) {
            // Tedään luvut
            Element header = doc.createElement("h" + generator.getDepth());
            header.setAttribute("number", generator.generateNumber());
            header.appendChild(doc.createTextNode(getTextString(viite.getTekstiKappale().getNimi(), kieli)));
            rootElement.appendChild(header);

            // Perusteen teksti luvulle
            addPerusteTeksti(doc, rootElement, viite, kieli);

            // Luodaan pohjan sisältö kappaleelle
            String teskti = "<div>" + getTextString(viite.getTekstiKappale().getTeksti(), kieli) + "</div>";

            Document tempDoc = new W3CDom().fromJsoup(Jsoup.parseBodyFragment(teskti));
            Node node = tempDoc.getDocumentElement().getChildNodes().item(1).getFirstChild();

            rootElement.appendChild(doc.importNode(node, true));

            addTekstiKappale(doc, rootElement, viite, kieli, generator);

            generator.increaseNumber();
        }
    }

    private void addTekstiKappale(Document doc, Element element, TekstiKappaleViite viite,
                                  Kieli kieli, CharapterNumberGenerator generator)
            throws ParserConfigurationException, IOException, SAXException {

        generator.increaseDepth();
        for (TekstiKappaleViite lapsi : viite.getLapset()) {
            // Tedään luvut
            String nimi = getTextString(lapsi.getTekstiKappale().getNimi(), kieli);
            Element header = doc.createElement("h" + generator.getDepth());
            header.setAttribute("number", generator.generateNumber());
            header.appendChild(doc.createTextNode(nimi));
            element.appendChild(header);

            // Perusteen teksti luvulle
            addPerusteTeksti(doc, element, lapsi, kieli);

            // Luodaan sisältö
            String teskti = "<div>" + getTextString(lapsi.getTekstiKappale().getTeksti(), kieli) + "</div>";

            Document tempDoc = new W3CDom().fromJsoup(Jsoup.parseBodyFragment(teskti));
            Node node = tempDoc.getDocumentElement().getChildNodes().item(1).getFirstChild();

            element.appendChild(doc.importNode(node, true));

            // Rekursiivisesti
            addTekstiKappale(doc, element, lapsi, kieli, generator);

            generator.increaseNumber();
        }
        generator.decreaseDepth();
    }

    private void addPerusteTeksti(Document doc, Element rootElement, TekstiKappaleViite viite, Kieli kieli)
            throws ParserConfigurationException, IOException, SAXException {

        // Perusteen teksti luvulle
        List<OhjeDto> ohjeDto = ohjeService.getTekstiKappaleOhjeet(viite.getTekstiKappale().getTunniste());

        // Luodaan sisältö
        ohjeDto.stream()
                .filter(ohje -> ohje.getTyyppi() == OhjeTyyppi.PERUSTETEKSTI)
                .forEach(ohje -> {
            LokalisoituTekstiDto tekstiDto = ohje.getTeksti();
            LokalisoituTeksti teksti = mapper.map(tekstiDto, LokalisoituTeksti.class);

            // Luodaan sisältö
            String teskti = "<cite>" + getTextString(teksti, kieli) + "</cite>";

            Document tempDoc = new W3CDom().fromJsoup(Jsoup.parseBodyFragment(teskti));
            Node node = tempDoc.getDocumentElement().getChildNodes().item(1).getFirstChild();

            rootElement.appendChild(doc.importNode(node, true));
        });
    }

    private void buildFootnotes(Document doc, Opetussuunnitelma ops, Kieli kieli) {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        try {
            XPathExpression expression = xpath.compile("//abbr");
            NodeList list = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);

            int noteNumber = 1;
            for (int i = 0; i < list.getLength(); i++) {
                Element element = (Element) list.item(i);
                TermiDto termiDto = termistoService.getTermi(ops.getId(), element.getAttribute("data-viite"));
                if (termiDto != null && termiDto.isAlaviite()) {
                    element.setAttribute("number", String.valueOf(noteNumber));
                    element.setAttribute("text", getFootnoteText(termiDto, kieli));
                    noteNumber++;
                }
            }

        } catch (XPathExpressionException e) {
            LOG.error(e.getLocalizedMessage());
        }
    }

    private void addVuosiluokkakokonaisuudet(Document doc, Element bodyElement, Opetussuunnitelma ops,
                                             PerusteDto perusteDto, CharapterNumberGenerator generator, Kieli kieli) {
        Set<OpsVuosiluokkakokonaisuus> opsVlkset = ops.getVuosiluokkakokonaisuudet();

        // Haetaan vuosiluokkkakokonaisuudet
        ArrayList<Vuosiluokkakokonaisuus> vlkset = new ArrayList<>();
        for (OpsVuosiluokkakokonaisuus opsVlk : opsVlkset) {
            if (opsVlk.isOma())
                vlkset.add(opsVlk.getVuosiluokkakokonaisuus());
        }

        // Järjestetään aakkosjärjestykseen
        vlkset = vlkset.stream()
                .sorted((vlk1, vlk2) -> vlk1.getNimi().getTeksti().get(kieli)
                        .compareTo(vlk2.getNimi().getTeksti().get(kieli)))
                .collect(Collectors.toCollection(ArrayList::new));

        for (Vuosiluokkakokonaisuus vlk : vlkset) {

            addHeader(doc, bodyElement, getTextString(vlk.getNimi(), kieli), generator);

            // Vuosiluokkan sisältö
            generator.increaseDepth();

            // Perusteen osa
            PerusteVuosiluokkakokonaisuusDto perusteVlk = perusteDto.getPerusopetus()
                    .getVuosiluokkakokonaisuudet(vlk.getTunniste().getId()).get();

            addSiirtyminenEdellisesta(doc, bodyElement, vlk, perusteVlk, generator, kieli);

            addTehtava(doc, bodyElement, vlk, perusteVlk, generator, kieli);

            addSiirtyminenSeuraavaan(doc, bodyElement, vlk, perusteVlk, generator, kieli);

            addLaajaalainenOsaaminen(doc, bodyElement, vlk, perusteVlk, generator, kieli);

            addPaikallisestiPaatettavat(doc, bodyElement, perusteVlk, generator, kieli);

            addOppiaineet(doc, bodyElement, ops, vlk, perusteDto, generator, kieli);

            generator.decreaseDepth();

            generator.increaseNumber();
        }

        generator.decreaseDepth();
    }

    private void addSiirtyminenEdellisesta(Document doc, Element bodyElement, Vuosiluokkakokonaisuus vlk,
                                           PerusteVuosiluokkakokonaisuusDto perusteVlk,
                                           CharapterNumberGenerator generator, Kieli kieli) {

        PerusteTekstiOsaDto perusteTekstiOsaDto = perusteVlk.getSiirtymaEdellisesta();
        LokalisoituTeksti perusteOtsikko = mapper.map(perusteTekstiOsaDto.getOtsikko(), LokalisoituTeksti.class);

        // Otsikko
        addHeader(doc, bodyElement, getTextString(perusteOtsikko, kieli), generator);

        // Perusteen teksi
        LokalisoituTeksti perusteTeksti = mapper.map(perusteTekstiOsaDto.getTeksti(), LokalisoituTeksti.class);
        addLokalisoituteksti(doc, bodyElement, perusteTeksti, "cite", kieli);

        // Opsin teksti
        Tekstiosa siirtymaEdellisesta = vlk.getSiirtymaEdellisesta();
        if (siirtymaEdellisesta != null)
            addLokalisoituteksti(doc, bodyElement, siirtymaEdellisesta.getTeksti(), "div", kieli);

        generator.increaseNumber();
    }

    private void addTehtava(Document doc, Element bodyElement, Vuosiluokkakokonaisuus vlk,
                            PerusteVuosiluokkakokonaisuusDto perusteVlk,
                            CharapterNumberGenerator generator, Kieli kieli) {

        // Perusteen osa
        PerusteTekstiOsaDto perusteTekstiOsaDto = perusteVlk.getTehtava();
        LokalisoituTeksti perusteOtsikko = mapper.map(perusteTekstiOsaDto.getOtsikko(), LokalisoituTeksti.class);

        // Otsikko
        addHeader(doc, bodyElement, getTextString(perusteOtsikko, kieli), generator);

        // Perusteen teksi
        LokalisoituTeksti perusteTeksti = mapper.map(perusteTekstiOsaDto.getTeksti(), LokalisoituTeksti.class);
        addLokalisoituteksti(doc, bodyElement, perusteTeksti, "cite", kieli);

        // Opsin teksti
        Tekstiosa tekstiosa = vlk.getTehtava();
        if (tekstiosa != null)
            addLokalisoituteksti(doc, bodyElement, tekstiosa.getTeksti(), "div", kieli);

        generator.increaseNumber();

    }

    private void addSiirtyminenSeuraavaan(Document doc, Element bodyElement, Vuosiluokkakokonaisuus vlk,
                                          PerusteVuosiluokkakokonaisuusDto perusteVlk,
                                          CharapterNumberGenerator generator, Kieli kieli) {

        // Perusteen osa
        PerusteTekstiOsaDto perusteTekstiOsaDto = perusteVlk.getSiirtymaSeuraavaan();
        LokalisoituTeksti perusteOtsikko = mapper.map(perusteTekstiOsaDto.getOtsikko(), LokalisoituTeksti.class);

        // Otsikko
        addHeader(doc, bodyElement, getTextString(perusteOtsikko, kieli), generator);

        // Perusteen teksi
        LokalisoituTeksti perusteTeksti = mapper.map(perusteTekstiOsaDto.getTeksti(), LokalisoituTeksti.class);
        addLokalisoituteksti(doc, bodyElement, perusteTeksti, "cite", kieli);

        // Opsin teksti
        Tekstiosa tekstiosa = vlk.getSiirtymaSeuraavaan();
        if (tekstiosa != null)
            addLokalisoituteksti(doc, bodyElement, tekstiosa.getTeksti(), "div", kieli);

        generator.increaseNumber();

    }

    private void addLaajaalainenOsaaminen(Document doc, Element bodyElement, Vuosiluokkakokonaisuus vlk,
                                          PerusteVuosiluokkakokonaisuusDto perusteVlk,
                                          CharapterNumberGenerator generator, Kieli kieli) {

        PerusteTekstiOsaDto perusteTekstiOsaDto = perusteVlk.getLaajaalainenOsaaminen();
        LokalisoituTeksti perusteOtsikko = mapper.map(perusteTekstiOsaDto.getOtsikko(), LokalisoituTeksti.class);

        // Otsikko
        addHeader(doc, bodyElement, getTextString(perusteOtsikko, kieli), generator);

        // Perusteen teksi
        LokalisoituTeksti perusteTeksti = mapper.map(perusteTekstiOsaDto.getTeksti(), LokalisoituTeksti.class);
        addLokalisoituteksti(doc, bodyElement, perusteTeksti, "cite", kieli);

        // Opsin teksti
        Tekstiosa laajaalainenOsaaminen = vlk.getLaajaalainenOsaaminen();
        if (laajaalainenOsaaminen != null)
            addLokalisoituteksti(doc, bodyElement, laajaalainenOsaaminen.getTeksti(), "div", kieli);

        // Laajaalaisen osaamisen osat
        addLaajaalainenOsaamisenOsat(doc, bodyElement, vlk, perusteVlk, generator, kieli);

        generator.increaseNumber();
    }

    private void addLaajaalainenOsaamisenOsat(Document doc, Element bodyElement, Vuosiluokkakokonaisuus vlk,
                                             PerusteVuosiluokkakokonaisuusDto perusteVlk,
                                              CharapterNumberGenerator generator, Kieli kieli) {

        Set<PerusteVuosiluokkakokonaisuudenLaajaalainenosaaminenDto> perusteOsaamiset = perusteVlk.getLaajaalaisetOsaamiset();
        Set<Laajaalainenosaaminen> osaamiset = vlk.getLaajaalaisetosaamiset();

        List<PerusteVuosiluokkakokonaisuudenLaajaalainenosaaminenDto> perusteOsaamisetLista = perusteOsaamiset.stream()
                .sorted((pvl1, pvl2) -> pvl1.getLaajaalainenOsaaminen().getNimi().get(kieli)
                        .compareTo(pvl2.getLaajaalainenOsaaminen().getNimi().get(kieli)))
                .collect(Collectors.toCollection(ArrayList::new));

        generator.increaseDepth();
        generator.increaseDepth();
        generator.increaseDepth();

        perusteOsaamisetLista.stream()
                .filter(osat -> osat.getLaajaalainenOsaaminen() != null)
                .forEach(osat -> {
            LokalisoituTekstiDto nimi = osat.getLaajaalainenOsaaminen().getNimi();
            LokalisoituTekstiDto kuvaus = osat.getLaajaalainenOsaaminen().getKuvaus();

            // Perusteen teksti
            addHeader(doc, bodyElement, getTextString(nimi, kieli), generator);
            addLokalisoituteksti(doc, bodyElement, mapper.map(kuvaus, LokalisoituTeksti.class), "cite", kieli);

            // Opsin teksti
            Optional<Laajaalainenosaaminen> vastaavaOpsissa = osaamiset.stream()
                    .filter(osaaminen -> osaaminen.getLaajaalainenosaaminen().getViite()
                            .equals(osat.getLaajaalainenOsaaminen().getTunniste().toString()))
                    .findFirst();
            addLokalisoituteksti(doc, bodyElement, vastaavaOpsissa.get().getKuvaus(), "div", kieli);

            generator.increaseNumber();
        });

        generator.decreaseDepth();
        generator.decreaseDepth();
        generator.decreaseDepth();
    }

    private void addPaikallisestiPaatettavat(Document doc, Element bodyElement,
                                             PerusteVuosiluokkakokonaisuusDto perusteVlk,
                                             CharapterNumberGenerator generator, Kieli kieli) {
        PerusteTekstiOsaDto perusteTekstiOsaDtoDto = perusteVlk.getPaikallisestiPaatettavatAsiat();
        if (perusteTekstiOsaDtoDto == null)
            return;

        LokalisoituTekstiDto otsikkoDto = perusteTekstiOsaDtoDto.getOtsikko();
        LokalisoituTekstiDto tekstiDto = perusteTekstiOsaDtoDto.getTeksti();

        if (otsikkoDto == null || tekstiDto == null)
            return;

        LokalisoituTeksti otsikko = mapper.map(otsikkoDto, LokalisoituTeksti.class);
        LokalisoituTeksti teksti = mapper.map(tekstiDto, LokalisoituTeksti.class);

        if (otsikko == null || teksti == null)
            return;

        addHeader(doc, bodyElement, getTextString(otsikko, kieli), generator);
        addLokalisoituteksti(doc, bodyElement, teksti, "cite", kieli);

        generator.increaseNumber();
    }

    private void addOppiaineet(Document doc, Element bodyElement, Opetussuunnitelma ops,
                               Vuosiluokkakokonaisuus vlk, PerusteDto perusteDto,
                               CharapterNumberGenerator generator, Kieli kieli) {
        Set<OpsOppiaine> oppiaineet = ops.getOppiaineet();
        if (oppiaineet == null)
            return;

        List<OpsOppiaine> oppiaineetAsc = oppiaineet.stream()
                .filter(oa -> oa.getOppiaine() != null)
                .sorted((oa1, oa2) -> oa1.getOppiaine().getNimi().getTeksti().get(kieli)
                        .compareTo(oa2.getOppiaine().getNimi().getTeksti().get(kieli)))
                .collect(Collectors.toCollection(ArrayList::new));

        addHeader(doc, bodyElement, messages.translate("oppiaineet", kieli), generator);

        generator.increaseDepth();

        // Oppiaineet
        for (OpsOppiaine opsOppiaine : oppiaineetAsc) {
            Oppiaine oppiaine = opsOppiaine.getOppiaine();

            Set<Oppiaineenvuosiluokkakokonaisuus> oaVlkset = oppiaine.getVuosiluokkakokonaisuudet();

            Optional<Oppiaineenvuosiluokkakokonaisuus> optOaVlk = oaVlkset.stream()
                    .filter(o -> o.getVuosiluokkakokonaisuus().getId() == vlk.getTunniste().getId())
                    .findFirst();

            if (optOaVlk.isPresent()) {
                UUID tunniste = oppiaine.getTunniste();

                addHeader(doc, bodyElement, getTextString(oppiaine.getNimi(), kieli), generator);
                addOppiaineTehtava(doc, bodyElement, tunniste, oppiaine, perusteDto, kieli);

                generator.increaseDepth();
                generator.increaseDepth();

                Oppiaineenvuosiluokkakokonaisuus oaVlk = optOaVlk.get();

                // Perusteen osa
                Optional<PerusteOppiaineDto> optPerusteOppiaineDto = perusteDto.getPerusopetus().getOppiaine(tunniste);

                PerusteOppiaineDto perusteOppiaineDto = null;
                if (optPerusteOppiaineDto.isPresent())
                    perusteOppiaineDto = optPerusteOppiaineDto.get();

                if (perusteOppiaineDto != null) {
                    Optional<PerusteOppiaineenVuosiluokkakokonaisuusDto> optPerusteOaVlkDto =
                            perusteOppiaineDto.getVuosiluokkakokonaisuus(oaVlk.getVuosiluokkakokonaisuus().getId());
                    if (optPerusteOaVlkDto.isPresent()) {
                        // Peruste & ops
                        addOppiaineVuosiluokkkakokonaisuus(doc, bodyElement, optPerusteOaVlkDto.get(), oaVlk,
                                vlk, perusteDto, generator, kieli);
                    }
                }

                generator.decreaseDepth();
                generator.decreaseDepth();
            }



            generator.increaseNumber();


        }

        generator.decreaseDepth();
    }

    private void addOppiaineVuosiluokkkakokonaisuus(Document doc, Element bodyElement,
                                                    PerusteOppiaineenVuosiluokkakokonaisuusDto perusteOaVlkDto,
                                                    Oppiaineenvuosiluokkakokonaisuus oaVlkDto,
                                                    Vuosiluokkakokonaisuus vlk, PerusteDto perusteDto,
                                                    CharapterNumberGenerator generator, Kieli kieli) {


        // Tehtävä
        PerusteTekstiOsaDto perusteTekstiOsaDto = perusteOaVlkDto.getTehtava();
        if (perusteTekstiOsaDto != null) {
            LokalisoituTekstiDto otsikko = perusteTekstiOsaDto.getOtsikko();
            LokalisoituTekstiDto teksti = perusteTekstiOsaDto.getTeksti();
            if (otsikko != null) {
                addHeader(doc, bodyElement, getTextString(otsikko, kieli), generator);
            }
            if (teksti != null) {
                addOppiainePerusteTeksti(doc, bodyElement, perusteTekstiOsaDto, kieli);
            }
        }

        Tekstiosa tekstiOsa = oaVlkDto.getTehtava();
        if (tekstiOsa != null) {
            LokalisoituTeksti teksti = tekstiOsa.getTeksti();
            if (teksti != null) {
                addLokalisoituteksti(doc, bodyElement, teksti, "div", kieli);
            }
        }

        // Tavoitteet
        List<PerusteOpetuksentavoiteDto> tavoitteet = perusteOaVlkDto.getTavoitteet();

        if (tavoitteet != null) {
            addHeader(doc, bodyElement, messages.translate("tavoitteet-ja-sisallot-vuosiluokittain", kieli), generator);
        }

        /*for (PerusteOpetuksentavoiteDto perusteTavoiteDto : tavoitteet) {

            LokalisoituTekstiDto tavoiteDto = perusteTavoiteDto.getTavoite();
            LokalisoituTeksti tavoite = mapper.map(tavoiteDto, LokalisoituTeksti.class);
            addLokalisoituteksti(doc, bodyElement, tavoite, "cite", kieli);
        }*/

        ArrayList<Oppiaineenvuosiluokka> vuosiluokat = oaVlkDto.getVuosiluokat().stream()
                .sorted((el1, el2) -> el1.getVuosiluokka().toString().compareTo(el2.getVuosiluokka().toString()))
                .collect(Collectors.toCollection(ArrayList::new));

        // Oppiaine vuosiluokka
        for (Oppiaineenvuosiluokka oaVuosiluokka : vuosiluokat) {
            // Vuosiluokka otsikko
            try {
                addHeader(doc, bodyElement,
                        messages.translate(oaVuosiluokka.getVuosiluokka().toString(), kieli), generator);
            } catch (NoSuchMessageException ex) {
                LOG.warn(ex.getLocalizedMessage());
                addHeader(doc, bodyElement, oaVuosiluokka.getVuosiluokka().toString(), generator);
            }

            for (Keskeinensisaltoalue ksa : oaVuosiluokka.getSisaltoalueet()) {
                generator.increaseDepth();

                addHeader(doc, bodyElement, getTextString(ksa.getNimi(), kieli), generator);

                // Peruste
                Optional<PerusteKeskeinensisaltoalueDto> optPerusteKsa = perusteOaVlkDto.getSisaltoalueet().stream()
                        .filter(pKsa -> pKsa.getTunniste().equals(ksa.getTunniste()))
                        .findFirst();


                if (optPerusteKsa.isPresent()) {
                    PerusteKeskeinensisaltoalueDto perusteKsa = optPerusteKsa.get();
                    LokalisoituTekstiDto tekstiDto = perusteKsa.getKuvaus();
                    LokalisoituTeksti teksti = mapper.map(tekstiDto, LokalisoituTeksti.class);
                    addLokalisoituteksti(doc, bodyElement, teksti, "cite", kieli);
                }

                // Ops
                addLokalisoituteksti(doc, bodyElement, ksa.getKuvaus(), "div", kieli);
                generator.decreaseDepth();
            }
        }

        // Työtavat
        PerusteTekstiOsaDto perusteTyotavat = perusteOaVlkDto.getTyotavat();
        if (perusteTyotavat != null) {
            addHeader(doc, bodyElement, getTextString(perusteTyotavat.getOtsikko(), kieli), generator);
            addOppiainePerusteTeksti(doc, bodyElement, perusteTyotavat, kieli);
        }

        Tekstiosa tyotavat = oaVlkDto.getTyotavat();
        LokalisoituTeksti tyotavatTeksti = tyotavat.getTeksti();
        addLokalisoituteksti(doc, bodyElement, tyotavatTeksti, "div", kieli);

        // Ohjaus
        PerusteTekstiOsaDto perusteOhjaus = perusteOaVlkDto.getOhjaus();
        if (perusteOhjaus != null) {
            addHeader(doc, bodyElement, getTextString(perusteOhjaus.getOtsikko(), kieli), generator);
            addOppiainePerusteTeksti(doc, bodyElement, perusteOhjaus, kieli);
        }

        Tekstiosa ohjaus = oaVlkDto.getOhjaus();
        LokalisoituTeksti ohjausTeksti = ohjaus.getTeksti();
        addLokalisoituteksti(doc, bodyElement, ohjausTeksti, "div", kieli);

        // Arviointi
        PerusteTekstiOsaDto perusteArviointi = perusteOaVlkDto.getArviointi();
        if (perusteArviointi != null) {
            addHeader(doc, bodyElement, getTextString(perusteArviointi.getOtsikko(), kieli), generator);
            addOppiainePerusteTeksti(doc, bodyElement, perusteArviointi, kieli);
        }

        Tekstiosa arviointi = oaVlkDto.getArviointi();
        LokalisoituTeksti arviointiTeksti = arviointi.getTeksti();
        addLokalisoituteksti(doc, bodyElement, arviointiTeksti, "div", kieli);

        // Oppimäärät
        Oppiaine oppiaine = oaVlkDto.getOppiaine();
        if (oppiaine != null) {
            Optional<PerusteOppiaineDto> optPerusteOppiaineDto = perusteDto
                    .getPerusopetus().getOppiaine(oppiaine.getTunniste());
            PerusteOppiaineDto perusteOppiaineDto;
            if (optPerusteOppiaineDto.isPresent()) {
                perusteOppiaineDto = optPerusteOppiaineDto.get();
                Set<PerusteOppiaineDto> perusteOppimaarat = perusteOppiaineDto.getOppimaarat();
                Set<Oppiaine> oppimaarat = oppiaine.getOppimaarat();

                addOppimaarat(doc, bodyElement, perusteOppimaarat, oppimaarat, vlk, generator, kieli);
            }
        }
    }

    private void addOppimaarat(Document doc, Element bodyElement,
                               Set<PerusteOppiaineDto> perusteOppimaarat, Set<Oppiaine> oppimaarat,
                               Vuosiluokkakokonaisuus vlk, CharapterNumberGenerator generator, Kieli kieli) {
        if (perusteOppimaarat != null) {
            for (PerusteOppiaineDto perusteOppiaineDto : perusteOppimaarat) {

                Optional<Oppiaine> optOppimaara = oppimaarat.stream()
                        .filter(oppiaine -> oppiaine.getTunniste().equals(perusteOppiaineDto.getTunniste()))
                        .findFirst();
                Oppiaine oppiaine = null;
                if (optOppimaara.isPresent()) {
                    oppiaine = optOppimaara.get();
                }

                addOppimaara(doc, bodyElement, perusteOppiaineDto, oppiaine, vlk, generator, kieli);
            }
        }
    }

    private void addOppimaara(Document doc, Element bodyElement,
                              PerusteOppiaineDto perusteOppiaineDto, Oppiaine oppiaine,
                              Vuosiluokkakokonaisuus vlk, CharapterNumberGenerator generator, Kieli kieli) {

        if (perusteOppiaineDto.getNimi().get(kieli) != null) {
            // Otsikko
            addHeader(doc, bodyElement, getTextString(perusteOppiaineDto.getNimi(), kieli), generator);

            generator.increaseDepth();
            // Tehtävä
            addOppimaaraTehtava(doc, bodyElement, perusteOppiaineDto, oppiaine, generator, kieli);

            Optional<PerusteOppiaineenVuosiluokkakokonaisuusDto> optPerusteOaVlkDto
                    = perusteOppiaineDto.getVuosiluokkakokonaisuus(vlk.getTunniste().getId());


            PerusteOppiaineenVuosiluokkakokonaisuusDto perusteOaVlkDto;
            if (optPerusteOaVlkDto.isPresent()) {
                perusteOaVlkDto = optPerusteOaVlkDto.get();

                Oppiaineenvuosiluokkakokonaisuus oaVlk = null;
                if (oppiaine != null) {
                    Optional<Oppiaineenvuosiluokkakokonaisuus> optOaVlk
                            = oppiaine.getVuosiluokkakokonaisuus(vlk.getTunniste().getId());

                    if (optOaVlk.isPresent()) {
                        oaVlk = optOaVlk.get();
                    }
                }

                // Vuosiluokkakokonaisuudella
                addOppimaaraTehtavaVuosiluokalla(doc, bodyElement, perusteOaVlkDto, oaVlk, generator, kieli);

                // todo: Oppimäärän tavoitteet
                // Oppimäärän tavoitteet vuosiluokkakokonaisuudella
                //addOppimaaraTavoitteetVuosiluokkakokonaisuudella(doc, bodyElement, perusteOaVlkDto, oaVlk, kieli);

                // Työtavat
                PerusteTekstiOsaDto perusteTyotavat = perusteOaVlkDto.getTyotavat();
                if (perusteTyotavat != null) {
                    addHeader(doc, bodyElement, getTextString(perusteTyotavat.getOtsikko(), kieli), generator);
                    addOppiainePerusteTeksti(doc, bodyElement, perusteTyotavat, kieli);
                }
                if (oaVlk != null) {
                    Tekstiosa tyotavat = oaVlk.getTyotavat();
                    LokalisoituTeksti ohjausTeksti = tyotavat.getTeksti();
                    addLokalisoituteksti(doc, bodyElement, ohjausTeksti, "div", kieli);
                }

                // Ohjaus
                PerusteTekstiOsaDto perusteOhjaus = perusteOaVlkDto.getOhjaus();
                if (perusteOhjaus != null) {
                    addHeader(doc, bodyElement, getTextString(perusteOhjaus.getOtsikko(), kieli), generator);
                    addOppiainePerusteTeksti(doc, bodyElement, perusteOhjaus, kieli);
                }

                if (oaVlk != null) {
                    Tekstiosa ohjaus = oaVlk.getOhjaus();
                    LokalisoituTeksti ohjausTeksti = ohjaus.getTeksti();
                    addLokalisoituteksti(doc, bodyElement, ohjausTeksti, "div", kieli);
                }

                // Arviointi
                PerusteTekstiOsaDto perusteArviointi = perusteOaVlkDto.getArviointi();
                if (perusteArviointi != null) {
                    addHeader(doc, bodyElement, getTextString(perusteArviointi.getOtsikko(), kieli), generator);
                    addOppiainePerusteTeksti(doc, bodyElement, perusteArviointi, kieli);
                }

                if (oaVlk != null) {
                    Tekstiosa arviointi = oaVlk.getArviointi();
                    LokalisoituTeksti arviointiTeksti = arviointi.getTeksti();
                    addLokalisoituteksti(doc, bodyElement, arviointiTeksti, "div", kieli);
                }
            }

            generator.decreaseDepth();
        }

    }

    private void addOppimaaraTehtava(Document doc, Element bodyElement,
                                     PerusteOppiaineDto perusteOppiaineDto, Oppiaine oppiaine,
                                     CharapterNumberGenerator generator, Kieli kieli) {
        // Erityinen tehtävä
        PerusteTekstiOsaDto perusteTekstiOsaDto = perusteOppiaineDto.getTehtava();
        if (perusteTekstiOsaDto != null) {
            LokalisoituTekstiDto otsikkoDto = perusteTekstiOsaDto.getOtsikko();
            LokalisoituTekstiDto tekstiDto = perusteTekstiOsaDto.getTeksti();

            if (otsikkoDto != null && tekstiDto != null) {
                LokalisoituTeksti otsikko = mapper.map(otsikkoDto, LokalisoituTeksti.class);
                LokalisoituTeksti teksti = mapper.map(tekstiDto, LokalisoituTeksti.class);

                addHeader(doc, bodyElement, getTextString(otsikko, kieli), generator);
                addLokalisoituteksti(doc, bodyElement, teksti, "cite", kieli);

                if (oppiaine != null) {
                    Tekstiosa tekstiosa = oppiaine.getTehtava();
                    if (tekstiosa != null) {
                        addLokalisoituteksti(doc, bodyElement, tekstiosa.getTeksti(), "div", kieli);
                    }
                }
            }
        }
    }

    private void addOppimaaraTehtavaVuosiluokalla(Document doc, Element bodyElement,
                                                  PerusteOppiaineenVuosiluokkakokonaisuusDto perusteOaVlkDto,
                                                  Oppiaineenvuosiluokkakokonaisuus oaVlk,
                                                  CharapterNumberGenerator generator, Kieli kieli) {

        PerusteTekstiOsaDto perusteTekstiOsaDto = perusteOaVlkDto.getTehtava();
        if (perusteTekstiOsaDto != null) {
            LokalisoituTekstiDto tekstiDto = perusteTekstiOsaDto.getTeksti();
            LokalisoituTeksti teksti = mapper.map(tekstiDto, LokalisoituTeksti.class);

            // Tehtävä ei löydy valitulla kielellä
            if (teksti.getTeksti().get(kieli) != null) {
                addHeader(doc, bodyElement, getTextString(perusteTekstiOsaDto.getOtsikko(), kieli), generator);
                addLokalisoituteksti(doc, bodyElement, teksti, "cite", kieli);

                if (oaVlk != null) {
                    Tekstiosa tekstiosa = oaVlk.getTehtava();
                    addTekstiosa(doc, bodyElement, tekstiosa, "div", kieli);
                }
            }
        }
    }

    private void addOppimaaraTavoitteetVuosiluokkakokonaisuudella(Document doc, Element bodyElement,
                                                                  PerusteOppiaineenVuosiluokkakokonaisuusDto perusteOaVlkDto,
                                                                  Oppiaineenvuosiluokkakokonaisuus oaVlk,
                                                                  CharapterNumberGenerator generator, Kieli kieli) {

        addHeader(doc, bodyElement, "Tavoitteet!", generator);

        // Tavoitteet
        List<PerusteOpetuksentavoiteDto> perusteOpetuksentavoiteDtos = perusteOaVlkDto.getTavoitteet();
        for (PerusteOpetuksentavoiteDto perusteOpetuksentavoiteDto : perusteOpetuksentavoiteDtos) {
            LokalisoituTekstiDto lokalisoituTekstiDto = perusteOpetuksentavoiteDto.getTavoite();
            LokalisoituTeksti lokalisoituTeksti = mapper.map(lokalisoituTekstiDto, LokalisoituTeksti.class);

            addLokalisoituteksti(doc, bodyElement, lokalisoituTeksti, "cite", kieli);
        }
    }

    private void addOppiaineTehtava(Document doc, Element bodyElement, UUID tunniste,
                                    Oppiaine oppiaine, PerusteDto perusteDto, Kieli kieli) {

        if (tunniste != null && perusteDto.getPerusopetus().getOppiaine(tunniste) != null) {
            Optional<PerusteOppiaineDto> optPerusteenOppiaineet = perusteDto.getPerusopetus().getOppiaine(tunniste);
            PerusteOppiaineDto perusteenOppiaineet = null;

            if (optPerusteenOppiaineet.isPresent())
                perusteenOppiaineet = optPerusteenOppiaineet.get();

            if (perusteenOppiaineet != null) {

                PerusteTekstiOsaDto tehtava = perusteenOppiaineet.getTehtava();
                if (tehtava != null)
                    addOppiainePerusteTeksti(doc, bodyElement, tehtava, kieli);
            }
        }

        addTekstiosa(doc, bodyElement, oppiaine.getTehtava(), "div", kieli);
    }

    private void buildImages(Document doc, Opetussuunnitelma ops) {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        try {
            XPathExpression expression = xpath.compile("//img");
            NodeList list = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < list.getLength(); i++) {
                Element element = (Element) list.item(i);
                String id = element.getAttribute("data-uid");

                UUID uuid = UUID.fromString(id);

                // Ladataan kuvan data muistiin
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                liiteService.export(ops.getId(), uuid, byteArrayOutputStream);

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

        } catch (XPathExpressionException | IOException | NullPointerException e) {
            LOG.error(e.getLocalizedMessage());
        }
    }

    private void addOppiainePerusteTeksti(Document doc, Element rootElement, PerusteTekstiOsaDto tekstiOsaDto, Kieli kieli) {

        LokalisoituTekstiDto tekstiDto = tekstiOsaDto.getTeksti();
        if (tekstiDto == null)
            return;

        // Luodaan sisältö
        String teskti = "<cite>" + getTextString(mapper.map(tekstiDto, LokalisoituTeksti.class), kieli) + "</cite>";

        Document tempDoc = new W3CDom().fromJsoup(Jsoup.parseBodyFragment(teskti));
        Node node = tempDoc.getDocumentElement().getChildNodes().item(1).getFirstChild();

        rootElement.appendChild(doc.importNode(node, true));
    }

    private void addLokalisoituteksti(Document doc, Element parent, LokalisoituTeksti lTeksti, String tagi, Kieli kieli) {
        if (lTeksti == null || lTeksti.getTeksti() == null || lTeksti.getTeksti().get(kieli) == null)
            return;

        String teksti = lTeksti.getTeksti().get(kieli);
        if (teksti != null) {
            teksti = "<" + tagi + ">" + unescapeHtml5(teksti) + "</" + tagi + ">";

            Document tempDoc = new W3CDom().fromJsoup(Jsoup.parseBodyFragment(teksti));
            Node node = tempDoc.getDocumentElement().getChildNodes().item(1).getFirstChild();

            parent.appendChild(doc.importNode(node, true));
        }
    }

    private void addTekstiosa(Document doc, Element bodyElement,  Tekstiosa tekstiosa, String tagi, Kieli kieli) {
        if (tekstiosa != null) {
            LokalisoituTeksti otsikko = tekstiosa.getOtsikko();
            LokalisoituTeksti teksti = tekstiosa.getTeksti();
            if (otsikko != null) {
                addLokalisoituteksti(doc, bodyElement, otsikko, tagi, kieli);
            }
            if (teksti != null) {
                addLokalisoituteksti(doc, bodyElement, teksti, tagi, kieli);
            }
        }
    }

    private void addHeader(Document doc, Element bodyElement, String text, CharapterNumberGenerator generator) {
        if (text != null) {
            Element header = doc.createElement("h" + generator.getDepth());
            header.setAttribute("number", generator.generateNumber());
            header.appendChild(doc.createTextNode(unescapeHtml5(text)));
            bodyElement.appendChild(header);
        }
    }

    private String getFootnoteText(TermiDto termiDto, Kieli kieli) {
        LokalisoituTekstiDto tekstiDto = termiDto.getSelitys();
        String selitys = getTextString(mapper.map(tekstiDto, LokalisoituTeksti.class), kieli);
        selitys = selitys.replaceAll("<[^>]+>", "");

        return selitys;
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
