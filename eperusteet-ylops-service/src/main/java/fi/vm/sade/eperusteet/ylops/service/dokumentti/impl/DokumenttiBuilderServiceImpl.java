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

import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappaleViite;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.DokumenttiBuilderService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.LocalizedMessagesService;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.fop.apps.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author iSaul
 */
@Service
public class DokumenttiBuilderServiceImpl implements DokumenttiBuilderService {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiBuilderServiceImpl.class);

    @Autowired
    private LocalizedMessagesService messages;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public byte[] generatePdf(Opetussuunnitelma ops, Kieli kieli)
            throws TransformerException, IOException, SAXException, ParserConfigurationException {
        // Täällä tehdään kaikki taika

        // Säilytetään luonnin aikana pdf dataa muistissa
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("html");
        doc.appendChild(rootElement);

        Element headElement = doc.createElement("head");

        // Delete annoying <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
        if (headElement.hasChildNodes())
            headElement.removeChild(headElement.getFirstChild());

        Element bodyElement = doc.createElement("body");

        rootElement.appendChild(headElement);
        rootElement.appendChild(bodyElement);

        // Kansilehti & Infosivu
        addMetaPages(doc, headElement, ops, kieli);

        // Sisältöelementit
        addSisaltoElement(doc, bodyElement, ops, kieli);

        // käsitteet
        addGlossary();


        // Testaukseen
        /*Element fakeChapter = doc.createElement("chapter");
        Element fakeTitle = doc.createElement("title");
        fakeTitle.appendChild(doc.createTextNode("Luku otsikko"));

        fakeChapter.appendChild(fakeTitle);
        rootElement.appendChild(fakeChapter);*/

        LOG.info("XML model  :");
        LOG.info(StringEscapeUtils.unescapeHtml4(getStringFromDoc(doc)));


        Resource resource = applicationContext.getResource("classpath:docgen/xhtml-to-xslfo.xsl");
        pdf.write(convertOps2PDF(doc, resource.getFile()));

        // Lopetetaan leipominen ja siivotaan jäljet

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
        LOG.info("Generted XML  :");
        printStream(xmlStream);

        // Muunntetaan saatu xml malli fo:ksi
        InputStream xmlInputStream = new ByteArrayInputStream(xmlStream.toByteArray());
        convertXML2FO(xmlInputStream, xslt, foStream);
        LOG.info("Generated XSL-FO:");
        printStream(foStream);

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

        String tyyppi = messages.translate(ops.getTyyppi().toString(), kieli);
        Element type = doc.createElement("meta");
        type.setAttribute("name", "type");
        type.setAttribute("content", tyyppi);
        headElement.appendChild(type);

        String kuvaus = getTextString(ops.getKuvaus(), kieli);
        Element description = doc.createElement("meta");
        description.setAttribute("name", "description");
        description.setAttribute("content", kuvaus);
        headElement.appendChild(description);
    }

    private void addSisaltoElement(Document doc, Element rootElement, Opetussuunnitelma ops, Kieli kieli)
            throws IOException, SAXException, ParserConfigurationException {

        for (TekstiKappaleViite viite : ops.getTekstit().getLapset()) {
            // Tedään luvut
            Element header = doc.createElement("h1");
            header.setAttribute("number", "1.");
            header.appendChild(doc.createTextNode(getTextString(viite.getTekstiKappale().getNimi(), kieli)));
            rootElement.appendChild(header);

            addTekstiKappale(doc, rootElement, viite, kieli, 2);
        }
    }

    private void addTekstiKappale(Document doc, Element element, TekstiKappaleViite viite , Kieli kieli, int depth)
            throws ParserConfigurationException, IOException, SAXException {

        for (TekstiKappaleViite lapsi : viite.getLapset()) {
            // Tedään luvut
            String nimi = getTextString(lapsi.getTekstiKappale().getNimi(), kieli);
            Element header = doc.createElement("h" + depth);
            header.appendChild(doc.createTextNode(nimi));
            element.appendChild(header);

            // Luodaan sisältö
            String teskti = "<root>" + getTextString(lapsi.getTekstiKappale().getTeksti(), kieli) + "</root>";

            Node tempNode = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(teskti.getBytes()))
                    .getDocumentElement();

            LOG.info("sisalto: " + tempNode.toString());
            LOG.info("teksti: " + teskti);

            Node node = doc.importNode(tempNode, true);

            element.appendChild(node);

            addTekstiKappale(doc, element, lapsi, kieli, depth + 1);
        }
    }

    private void addGlossary() {

    }

    private void printStream(ByteArrayOutputStream stream) {
        LOG.info(StringEscapeUtils.unescapeHtml4(new String(stream.toByteArray(), StandardCharsets.UTF_8)));
    }

    private String getTextString(LokalisoituTeksti teksti, Kieli kieli) {
        if (teksti == null || teksti.getTeksti() == null || teksti.getTeksti().get(kieli) == null) {
            return "";
        } else {
            return teksti.getTeksti().get(kieli);
        }
    }

    private String getStringFromDoc(Document doc)    {
        DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
        LSSerializer lsSerializer = domImplementation.createLSSerializer();
        return lsSerializer.writeToString(doc);
    }
}
