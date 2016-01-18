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
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.*;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.ops.OpsOppiaine;
import fi.vm.sade.eperusteet.ylops.domain.ops.OpsVuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappaleViite;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Tekstiosa;
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
import fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.CharapterNumberGenerator;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.external.KoodistoService;
import fi.vm.sade.eperusteet.ylops.service.external.OrganisaatioService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ohje.OhjeService;
import fi.vm.sade.eperusteet.ylops.service.ops.LiiteService;
import fi.vm.sade.eperusteet.ylops.service.ops.TermistoService;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.fop.apps.*;
import org.apache.xml.security.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
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
    private ApplicationContext applicationContext;

    @Autowired
    private TermistoService termistoService;

    @Autowired
    private LiiteService liiteService;

    @Autowired
    private OhjeService ohjeService;

    @Autowired
    private KoodistoService koodistoService;

    @Autowired
    private OrganisaatioService organisaatioService;

    @Autowired
    private EperusteetService eperusteetService;

    @Autowired
    private DtoMapper mapper;

    CharapterNumberGenerator generator;
    PerusteDto perusteDto;

    @Override
    public byte[] generatePdf(Opetussuunnitelma ops, Kieli kieli)
            throws TransformerException, IOException, SAXException, ParserConfigurationException {

        // Säilytetään luonnin aikana pdf dataa muistissa
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

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

        generator = new CharapterNumberGenerator();
        perusteDto = eperusteetService.getEperusteetPeruste(ops.getCachedPeruste().getPerusteId());

        // Kansilehti & Infosivu
        addMetaPages(doc, headElement, ops, kieli);

        // Sisältöelementit
        addSisaltoElement(doc, bodyElement, ops, kieli);

        // Vuosiluokkakokonaisuudet ja oppiaineet
        if (ops.getKoulutustyyppi() == KoulutusTyyppi.PERUSOPETUS)
            addVuosiluokkakokonaisuudet(doc, bodyElement, ops, kieli);

        // Alaviitteet
        buildFootnotes(doc, ops, kieli);

        // Kuvat
        buildImages(doc, ops);

        Resource resource = applicationContext.getResource("classpath:docgen/xhtml-to-xslfo.xsl");
        pdf.write(convertOps2PDF(doc, resource.getFile()));

        return pdf.toByteArray();
    }

    private byte[] convertOps2PDF(Document doc, File xslt)
            throws IOException, TransformerException, SAXException {
        // Streams
        ByteArrayOutputStream xmlStream = new ByteArrayOutputStream();
        ByteArrayOutputStream foStream = new ByteArrayOutputStream();
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();

        // Muunnetaan ops objekti xml muotoon
        convertOps2XML(doc, xmlStream);
        //LOG.info("Generted XML  :");
        //printStream(xmlStream);

        // Muunntetaan saatu xml malli fo:ksi
        InputStream xmlInputStream = new ByteArrayInputStream(xmlStream.toByteArray());
        convertXML2FO(xmlInputStream, xslt, foStream);
        //LOG.info("Generated XSL-FO:");
        //printStream(foStream);

        // Muunnetaan saatu fo malli pdf:ksi
        InputStream foInputStream = new ByteArrayInputStream(foStream.toByteArray());
        convertFO2PDF(foInputStream, pdfStream);

        return pdfStream.toByteArray();
    }

    private void convertOps2XML(Document doc, OutputStream xml)
            throws IOException, TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();

        // To avoid <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        Source src = new DOMSource(doc);
        Result res = new StreamResult(xml);

        transformer.transform(src, res);
    }

    public void convertXML2FO(InputStream xml, File xslt, OutputStream fo)
            throws IOException, TransformerException {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(new StreamSource(xslt));

            Source src = new StreamSource(xml);
            Result res = new StreamResult(fo);

            transformer.transform(src, res);
        } finally {
            fo.close();
        }
    }

    @SuppressWarnings("unchecked")
    public void convertFO2PDF(InputStream fo, OutputStream pdf)
            throws IOException, SAXException, TransformerException {

        Resource resource = applicationContext.getResource("classpath:docgen/fop.xconf");
        FopFactory fopFactory = FopFactory.newInstance(resource.getFile());

        try {
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            foUserAgent.getRendererOptions().put("pdf-a-mode", "PDF/A-1b");

            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdf);

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            // XSLT version
            transformer.setParameter("versionParam", "2.0");

            Source src = new StreamSource(fo);
            Result res = new SAXResult(fop.getDefaultHandler());

            transformer.transform(src, res);
        } finally {
            pdf.close();
        }
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

    private void addSisaltoElement(Document doc, Element rootElement, Opetussuunnitelma ops, Kieli kieli)
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
            String teskti = "<p>" + getTextString(viite.getTekstiKappale().getTeksti(), kieli) + "</p>";

            Node node = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(teskti.getBytes()))
                    .getDocumentElement();
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
            String teskti = "<p>" + getTextString(lapsi.getTekstiKappale().getTeksti(), kieli) + "</p>";
            teskti = teskti.replace("&shy;", "");

            Node node = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(teskti.getBytes()))
                    .getDocumentElement();
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

        for (OhjeDto ohje : ohjeDto) {

            if (ohje.getTyyppi() == OhjeTyyppi.PERUSTETEKSTI) {
                LokalisoituTekstiDto tekstiDto = ohje.getTeksti();
                LokalisoituTeksti teksti = mapper.map(tekstiDto, LokalisoituTeksti.class);

                // Luodaan sisältö
                String teskti = "<cite>" + getTextString(teksti, kieli) + "</cite>";
                teskti = teskti.replace("&shy;", "");

                Node node = DocumentBuilderFactory
                        .newInstance()
                        .newDocumentBuilder()
                        .parse(new ByteArrayInputStream(teskti.getBytes()))
                        .getDocumentElement();
                rootElement.appendChild(doc.importNode(node, true));
            }
        }
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

    private void addVuosiluokkakokonaisuudet(Document doc, Element bodyElement, Opetussuunnitelma ops, Kieli kieli) {
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

            addHeader(doc, bodyElement, getTextString(vlk.getNimi(), kieli));

            // Vuosiluokkan sisältö
            generator.increaseDepth();

            // Perusteen osa
            PerusteVuosiluokkakokonaisuusDto perusteVlk = perusteDto.getPerusopetus()
                    .getVuosiluokkakokonaisuudet(vlk.getTunniste().getId()).get();

            addSiirtyminenEdellisesta(doc, bodyElement, vlk, perusteVlk, kieli);

            addTehtava(doc, bodyElement, vlk, perusteVlk, kieli);

            addSiirtyminenSeuraavaan(doc, bodyElement, vlk, perusteVlk, kieli);

            addLaajaalainenOsaaminen(doc, bodyElement, vlk, perusteVlk, kieli);

            addPaikallisestiPaatettavat(doc, bodyElement, perusteVlk, kieli);

            addOppiaineet(doc, bodyElement, ops, vlk, kieli);

            generator.decreaseDepth();

            generator.increaseNumber();
        }

        generator.decreaseDepth();
    }

    private void addSiirtyminenEdellisesta(Document doc, Element bodyElement, Vuosiluokkakokonaisuus vlk,
                                           PerusteVuosiluokkakokonaisuusDto perusteVlk, Kieli kieli) {

        PerusteTekstiOsaDto perusteTekstiOsaDto = perusteVlk.getSiirtymaEdellisesta();
        LokalisoituTeksti perusteOtsikko = mapper.map(perusteTekstiOsaDto.getOtsikko(), LokalisoituTeksti.class);

        // Otsikko
        addHeader(doc, bodyElement, getTextString(perusteOtsikko, kieli));

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
                            PerusteVuosiluokkakokonaisuusDto perusteVlk, Kieli kieli) {

        // Perusteen osa
        PerusteTekstiOsaDto perusteTekstiOsaDto = perusteVlk.getTehtava();
        LokalisoituTeksti perusteOtsikko = mapper.map(perusteTekstiOsaDto.getOtsikko(), LokalisoituTeksti.class);

        // Otsikko
        addHeader(doc, bodyElement, getTextString(perusteOtsikko, kieli));

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
                                          PerusteVuosiluokkakokonaisuusDto perusteVlk, Kieli kieli) {

        // Perusteen osa
        PerusteTekstiOsaDto perusteTekstiOsaDto = perusteVlk.getSiirtymaSeuraavaan();
        LokalisoituTeksti perusteOtsikko = mapper.map(perusteTekstiOsaDto.getOtsikko(), LokalisoituTeksti.class);

        // Otsikko
        addHeader(doc, bodyElement, getTextString(perusteOtsikko, kieli));

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
                                          PerusteVuosiluokkakokonaisuusDto perusteVlk, Kieli kieli) {

        PerusteTekstiOsaDto perusteTekstiOsaDto = perusteVlk.getLaajaalainenOsaaminen();
        LokalisoituTeksti perusteOtsikko = mapper.map(perusteTekstiOsaDto.getOtsikko(), LokalisoituTeksti.class);

        // Otsikko
        addHeader(doc, bodyElement, getTextString(perusteOtsikko, kieli));

        // Perusteen teksi
        LokalisoituTeksti perusteTeksti = mapper.map(perusteTekstiOsaDto.getTeksti(), LokalisoituTeksti.class);
        addLokalisoituteksti(doc, bodyElement, perusteTeksti, "cite", kieli);

        // Opsin teksti
        Tekstiosa laajaalainenOsaaminen = vlk.getLaajaalainenOsaaminen();
        if (laajaalainenOsaaminen != null)
            addLokalisoituteksti(doc, bodyElement, laajaalainenOsaaminen.getTeksti(), "div", kieli);

        // Laajaalaisen osaamisen osat
        addLaajaalainenOsaamisenOsat(doc, bodyElement, vlk, perusteVlk, kieli);

        generator.increaseNumber();
    }

    private void addLaajaalainenOsaamisenOsat(Document doc, Element bodyElement, Vuosiluokkakokonaisuus vlk,
                                             PerusteVuosiluokkakokonaisuusDto perusteVlk, Kieli kieli) {

        Set<PerusteVuosiluokkakokonaisuudenLaajaalainenosaaminenDto> perusteOsaamiset = perusteVlk.getLaajaalaisetOsaamiset();
        Set<Laajaalainenosaaminen> osaamiset = vlk.getLaajaalaisetosaamiset();

        List<PerusteVuosiluokkakokonaisuudenLaajaalainenosaaminenDto> perusteOsaamisetLista = perusteOsaamiset.stream()
                .sorted((pvl1, pvl2) -> pvl1.getLaajaalainenOsaaminen().getNimi().get(kieli)
                        .compareTo(pvl2.getLaajaalainenOsaaminen().getNimi().get(kieli)))
                .collect(Collectors.toCollection(ArrayList::new));

        generator.increaseDepth();
        generator.increaseDepth();
        generator.increaseDepth();

        for (PerusteVuosiluokkakokonaisuudenLaajaalainenosaaminenDto osat : perusteOsaamisetLista) {
            if (osat.getLaajaalainenOsaaminen() != null) {
                LokalisoituTekstiDto nimi = osat.getLaajaalainenOsaaminen().getNimi();
                LokalisoituTekstiDto kuvaus = osat.getLaajaalainenOsaaminen().getKuvaus();

                // Perusteen teksti
                addHeader(doc, bodyElement, getTextString(nimi, kieli));
                addLokalisoituteksti(doc, bodyElement, mapper.map(kuvaus, LokalisoituTeksti.class), "cite", kieli);

                // Opsin teksti
                Optional<Laajaalainenosaaminen> vastaavaOpsissa = osaamiset.stream()
                        .filter(osaaminen -> osaaminen.getLaajaalainenosaaminen().getViite()
                                .equals(osat.getLaajaalainenOsaaminen().getTunniste().toString()))
                        .findFirst();
                addLokalisoituteksti(doc, bodyElement, vastaavaOpsissa.get().getKuvaus(), "div", kieli);

                generator.increaseNumber();
            }
        }

        generator.decreaseDepth();
        generator.decreaseDepth();
        generator.decreaseDepth();
    }

    private void addPaikallisestiPaatettavat(Document doc, Element bodyElement,
                                             PerusteVuosiluokkakokonaisuusDto perusteVlk, Kieli kieli) {
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

        addHeader(doc, bodyElement, getTextString(otsikko, kieli));
        addLokalisoituteksti(doc, bodyElement, teksti, "cite", kieli);

        generator.increaseNumber();
    }

    private void addOppiaineet(Document doc, Element bodyElement, Opetussuunnitelma ops,
                               Vuosiluokkakokonaisuus vlk, Kieli kieli) {
        Set<OpsOppiaine> oppiaineet = ops.getOppiaineet();
        if (oppiaineet == null)
            return;

        List<OpsOppiaine> oppiaineetAsc = oppiaineet.stream()
                .filter(oa -> oa.getOppiaine() != null)
                .sorted((oa1, oa2) -> oa1.getOppiaine().getNimi().getTeksti().get(kieli)
                        .compareTo(oa2.getOppiaine().getNimi().getTeksti().get(kieli)))
                .collect(Collectors.toCollection(ArrayList::new));

        addHeader(doc, bodyElement, messages.translate("oppiaineet", kieli));

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

                addHeader(doc, bodyElement, getTextString(oppiaine.getNimi(), kieli));
                addOppiaineTehtava(doc, bodyElement, tunniste, oppiaine, kieli);

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
                        addOppiaineVuosiluokkkakokonaisuus(doc, bodyElement, optPerusteOaVlkDto.get(), oaVlk, kieli);
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
                                                    Oppiaineenvuosiluokkakokonaisuus oaVlkDto, Kieli kieli) {

        // Tehtävä

        PerusteTekstiOsaDto perusteTekstiOsaDto = perusteOaVlkDto.getTehtava();
        if (perusteTekstiOsaDto != null) {
            LokalisoituTekstiDto otsikko = perusteTekstiOsaDto.getOtsikko();
            LokalisoituTekstiDto teksti = perusteTekstiOsaDto.getTeksti();
            if (otsikko != null) {
                addHeader(doc, bodyElement, getTextString(otsikko, kieli));
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
            addHeader(doc, bodyElement, messages.translate("tavoitteet-ja-sisallot-vuosiluokittain", kieli));
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
                addHeader(doc, bodyElement, messages.translate(oaVuosiluokka.getVuosiluokka().toString(), kieli));
            } catch (NoSuchMessageException ex) {
                LOG.warn(ex.getLocalizedMessage());
                addHeader(doc, bodyElement, oaVuosiluokka.getVuosiluokka().toString());
            }

            for (Keskeinensisaltoalue ksa : oaVuosiluokka.getSisaltoalueet()) {
                generator.increaseDepth();

                addHeader(doc, bodyElement, getTextString(ksa.getNimi(), kieli));

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
            addHeader(doc, bodyElement, getTextString(perusteTyotavat.getOtsikko(), kieli));
            addOppiainePerusteTeksti(doc, bodyElement, perusteTyotavat, kieli);
        }

        Tekstiosa tyotavat = oaVlkDto.getTyotavat();
        LokalisoituTeksti tyotavatTeksti = tyotavat.getTeksti();
        addLokalisoituteksti(doc, bodyElement, tyotavatTeksti, "div", kieli);

        // Ohjaus
        PerusteTekstiOsaDto perusteOhjaus = perusteOaVlkDto.getOhjaus();
        if (perusteOhjaus != null) {
            addHeader(doc, bodyElement, getTextString(perusteOhjaus.getOtsikko(), kieli));
            addOppiainePerusteTeksti(doc, bodyElement, perusteOhjaus, kieli);
        }

        Tekstiosa ohjaus = oaVlkDto.getOhjaus();
        LokalisoituTeksti ohjausTeksti = ohjaus.getTeksti();
        addLokalisoituteksti(doc, bodyElement, ohjausTeksti, "div", kieli);

        // Arviointi
        PerusteTekstiOsaDto perusteArviointi = perusteOaVlkDto.getArviointi();
        if (perusteArviointi != null) {
            addHeader(doc, bodyElement, getTextString(perusteArviointi.getOtsikko(), kieli));
            addOppiainePerusteTeksti(doc, bodyElement, perusteArviointi, kieli);
        }

        Tekstiosa arviointi = oaVlkDto.getArviointi();
        LokalisoituTeksti arviointiTeksti = arviointi.getTeksti();
        addLokalisoituteksti(doc, bodyElement, arviointiTeksti, "div", kieli);
    }



    private void addOppiaineTehtava(Document doc, Element bodyElement, UUID tunniste,
                                    Oppiaine oppiaine, Kieli kieli) {

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

    private void addOppiainePerusteTeksti(Document doc, Element rootElement, PerusteTekstiOsaDto tekstiOsaDto, Kieli kieli) {

        LokalisoituTekstiDto tekstiDto = tekstiOsaDto.getTeksti();
        if (tekstiDto == null)
            return;

        // Luodaan sisältö
        String teskti = "<cite>" + getTextString(mapper.map(tekstiDto, LokalisoituTeksti.class), kieli) + "</cite>";
        teskti = teskti.replace("&shy;", "");

        try {
            Node node = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(teskti.getBytes()))
                    .getDocumentElement();
            rootElement.appendChild(doc.importNode(node, true));
        } catch (ParserConfigurationException | IOException | SAXException ex) {
            LOG.error(ex.getLocalizedMessage());
        }
    }

    private void addLokalisoituteksti(Document doc, Element parent, LokalisoituTeksti lokalisoituTeksti, String tagi, Kieli kieli) {
        if (lokalisoituTeksti == null)
            return;

        try {
            String teksti = lokalisoituTeksti.getTeksti().get(kieli);
            teksti = "<" + tagi + ">" + teksti + "</" + tagi + ">";

            Node node = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(teksti.getBytes()))
                    .getDocumentElement();
            parent.appendChild(doc.importNode(node, true));
        } catch (ParserConfigurationException | IOException | SAXException ex) {
            LOG.error(ex.getLocalizedMessage());
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

    private void addHeader(Document doc, Element bodyElement, String text) {
        Element header = doc.createElement("h" + generator.getDepth());
        header.setAttribute("number", generator.generateNumber());
        header.appendChild(doc.createTextNode(text));
        bodyElement.appendChild(header);
    }

    private String getFootnoteText(TermiDto termiDto, Kieli kieli) {
        LokalisoituTekstiDto tekstiDto = termiDto.getSelitys();
        String selitys = getTextString(mapper.map(tekstiDto, LokalisoituTeksti.class), kieli);
        selitys = StringEscapeUtils.unescapeHtml(selitys);
        selitys = selitys.replaceAll("<[^>]+>", "");

        return selitys;
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

    private String getTextString(LokalisoituTekstiDto lokalisoituTekstiDto, Kieli kieli) {
        LokalisoituTeksti lokalisoituTeksti = mapper.map(lokalisoituTekstiDto, LokalisoituTeksti.class);
        return getTextString(lokalisoituTeksti, kieli);
    }

    private String getTextString(LokalisoituTeksti lokalisoituTeksti, Kieli kieli) {
        if (lokalisoituTeksti == null || lokalisoituTeksti.getTeksti() == null
                || lokalisoituTeksti.getTeksti().get(kieli) == null) {
            return "";
        } else {
            return unescapeHtml5(lokalisoituTeksti.getTeksti().get(kieli));
        }
    }

    private String unescapeHtml5(String string) {
        return string
                .replaceAll("&shy;", "")
                .replaceAll("&ndash;", "–")
                .replaceAll("&sect;", "§")
                .replaceAll("&rdquo;", "\"")
                .replaceAll("&minus;", "−");
    }

}
