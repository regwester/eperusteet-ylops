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
import fi.vm.sade.eperusteet.ylops.service.dokumentti.DokumenttiBuilderService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.LocalizedMessagesService;
import org.apache.fop.apps.*;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.w3c.dom.Element;
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
import java.util.*;

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
        Element rootElement = doc.createElement("book");
        //rootElement.setAttribute("xmlns", "http://docbook.org/ns/docbook");
        doc.appendChild(rootElement);

        // Kansilehti
        addCoverPage(doc, rootElement, ops, kieli);

        // Infosivu
        addInfoPage(doc, ops, kieli);

        // Sisällysluettelo
        addTocPage();

        // Tutkinnonosat
        addTutkinnonosat();

        // Sisältöelementit
        addSisaltoElement();

        // käsitteet
        addGlossary();


        if (rootElement.getChildNodes().getLength() <= 1) {
            Element fakeChapter = doc.createElement("chapter");
            fakeChapter.appendChild(doc.createElement("title"));
            rootElement.appendChild(fakeChapter);
        }

        //printDocument(doc, System.out);

        Resource resource = applicationContext.getResource("classpath:docgen/test.xsl");
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
        LOG.info("Generted xml:");
        printStream(xmlStream);

        // Muunntetaan saatu xml malli fo:ksi
        InputStream xmlInputStream = new ByteArrayInputStream(xmlStream.toByteArray());
        convertXML2FO(xmlInputStream, xslt, foStream);
        LOG.info("Generated fo:");
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

            Source src = new StreamSource(fo);
            Result res = new SAXResult(fop.getDefaultHandler());

            transformer.transform(src, res);

            FormattingResults foResults = fop.getResults();
            java.util.List pageSequences = foResults.getPageSequences();
            for (Object pageSequence : pageSequences) {
                PageSequenceResults pageSequenceResults = (PageSequenceResults) pageSequence;
                System.out.println("PageSequence "
                        + (String.valueOf(pageSequenceResults.getID()).length() > 0
                        ? pageSequenceResults.getID() : "<no id>")
                        + " generated " + pageSequenceResults.getPageCount() + " pages.");
            }
            System.out.println("Generated " + foResults.getPageCount() + " pages in total.");
        } finally {
            pdf.close();
        }
    }

    private void addCoverPage(Document doc, Element rootElement, Opetussuunnitelma ops, Kieli kieli) {
        Element info = doc.createElement("info");
        rootElement.appendChild(info);

        String nimi = getTextString(ops.getNimi(), kieli);
        Element titleElement = doc.createElement("title");
        titleElement.appendChild(doc.createTextNode(nimi));
        info.appendChild(titleElement);

        String subtitleText = messages.translate("docgen.subtitle.ops", kieli);

        Element subtitle = doc.createElement("subtitle");
        subtitle.appendChild(doc.createTextNode(subtitleText));
        info.appendChild(subtitle);
    }

    private void addInfoPage(Document doc, Opetussuunnitelma ops, Kieli kieli) {
        Element rootElement = doc.getDocumentElement();
        Element info = doc.createElement("bookinfo");
        rootElement.appendChild(info);


        // Laitetaan alkuun kuvaus (tiivistelmä)
        String kuvaus = getTextString(ops.getKuvaus(), kieli);
        Element abstractPara = doc.createElement("para");
        addMarkupToElement(doc, abstractPara, kuvaus);
        info.appendChild(abstractPara);

        // Taulukossa loput tiedot
        Element it = doc.createElement("informaltable");
        it.setAttribute("frame", "none");
        it.setAttribute("colsep", "0");
        it.setAttribute("rowsep", "0");
        info.appendChild(it);
        Element tgroup = doc.createElement("tgroup");
        tgroup.setAttribute("cols", "2");
        it.appendChild(tgroup);
        Element colspec1 = doc.createElement("colspec");
        Element colspec2 = doc.createElement("colspec");
        colspec1.setAttribute("colwidth", "1*");
        colspec2.setAttribute("colwidth", "2*");
        tgroup.appendChild(colspec1);
        tgroup.appendChild(colspec2);
        Element tbody = doc.createElement("tbody");
        tgroup.appendChild(tbody);

        /*
        Element itrow = addTableRow(doc, tbody);
        addTableCell(doc, itrow, newBoldElement(doc, messages.translate("docgen.info.perusteen-nimi", kieli)));
        addTableCell(doc, itrow, getTextString(peruste.getNimi(), kieli));

        Element itrow2 = addTableRow(doc, tbody);
        addTableCell(doc, itrow2, newBoldElement(doc, messages.translate("docgen.info.maarayksen-diaarinumero", kieli)));
        if (peruste.getDiaarinumero() != null) {
            addTableCell(doc, itrow2, peruste.getDiaarinumero().toString());
        } else {
            addTableCell(doc, itrow2, messages.translate("docgen.info.ei-asetettu", kieli));
        }

        Element itrow8 = addTableRow(doc, tbody);
        addTableCell(doc, itrow8, newBoldElement(doc, messages.translate("docgen.info.korvaa-perusteet", kieli)));
        if (peruste.getKorvattavatDiaarinumerot() != null && !peruste.getKorvattavatDiaarinumerot().isEmpty())
        {
            Set<String> numeroStringit = new HashSet<>();
            for (Diaarinumero nro : peruste.getKorvattavatDiaarinumerot()) {
                numeroStringit.add(nro.getDiaarinumero());
            }
            addTableCell(doc, itrow8, StringUtils.join(numeroStringit, ", "));
        } else {
            addTableCell(doc, itrow8, messages.translate("docgen.info.ei-asetettu", kieli));
        }

        Set<Koulutus> koulutukset = peruste.getKoulutukset();
        Element koulutuslist = doc.createElement("simplelist");
        for (Koulutus koulutus : koulutukset) {
            String koulutusNimi = getTextString(koulutus.getNimi(), kieli);
            if (StringUtils.isNotEmpty(koulutus.getKoulutuskoodiArvo())) {
                koulutusNimi += " (" + koulutus.getKoulutuskoodiArvo() + ")";
            }
            Element member = doc.createElement("member");
            member.appendChild(doc.createTextNode(koulutusNimi));
            koulutuslist.appendChild(member);
        }

        Element itrow3 = addTableRow(doc, tbody);
        addTableCell(doc, itrow3, newBoldElement(doc, messages.translate("docgen.info.koulutuskoodit", kieli)));
        if (koulutuslist.hasChildNodes()) {
            addTableCell(doc, itrow3, koulutuslist);
        } else {
            addTableCell(doc, itrow3, messages.translate("docgen.info.ei-asetettu", kieli));
        }

        Set<Koodi> osaamisalat = peruste.getOsaamisalat();
        Element osaamisalalist = doc.createElement("simplelist");
        for (Koodi osaamisala : osaamisalat) {
            String osaamisalaNimi = getTextString(osaamisala.getNimi(), kieli);
            if (StringUtils.isNotEmpty(osaamisala.getArvo())) {
                osaamisalaNimi += " (" + osaamisala.getArvo() + ")";
            }
            Element member = doc.createElement("member");
            member.appendChild(doc.createTextNode(osaamisalaNimi));
            osaamisalalist.appendChild(member);
        }

        Element itrow4 = addTableRow(doc, tbody);
        addTableCell(doc, itrow4, newBoldElement(doc, messages.translate("docgen.info.osaamisalat", kieli)));
        if (osaamisalalist.hasChildNodes()) {
            addTableCell(doc, itrow4, osaamisalalist);
        } else {
            addTableCell(doc, itrow4, messages.translate("docgen.info.ei-asetettu", kieli));
        }

        List<TutkintonimikeKoodi> nimikeKoodit = tutkintonimikeKoodiRepository.findByPerusteId(peruste.getId());
        Element nimikelist = doc.createElement("simplelist");
        for (TutkintonimikeKoodi tnkoodi : nimikeKoodit) {
            KoodistoKoodiDto koodiDto = koodistoService.get("tutkintonimikkeet", tnkoodi.getTutkintonimikeUri());

            for (KoodistoMetadataDto meta : koodiDto.getMetadata()) {
                if (meta.getKieli().toLowerCase().equals(kieli.toString().toLowerCase())) {
                    Element member = doc.createElement("member");
                    member.appendChild(doc.createTextNode(meta.getNimi() + " (" + tnkoodi.getTutkintonimikeArvo() + ")"));
                    nimikelist.appendChild(member);
                } else {
                    LOG.debug("{} was no match", meta.getKieli() );
                }
            }
        }


        Element itrow5 = addTableRow(doc, tbody);
        addTableCell(doc, itrow5, newBoldElement(doc, messages.translate("docgen.info.tutkintonimikkeet", kieli)));
        if (nimikelist.hasChildNodes()) {
            addTableCell(doc, itrow5, nimikelist);
        } else {
            addTableCell(doc, itrow5, messages.translate("docgen.info.ei-asetettu", kieli));
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        Element itrow6 = addTableRow(doc, tbody);
        addTableCell(doc, itrow6, newBoldElement(doc, messages.translate("docgen.info.voimaantulo", kieli)));
        if (peruste.getVoimassaoloAlkaa() != null) {
            addTableCell(doc, itrow6, dateFormat.format(peruste.getVoimassaoloAlkaa()));
        } else {
            addTableCell(doc, itrow6, messages.translate("docgen.info.ei-asetettu", kieli));
        }

        Element itrow7 = addTableRow(doc, tbody);
        addTableCell(doc, itrow7, newBoldElement(doc, messages.translate("docgen.info.voimassaolon-paattyminen", kieli)));
        if (peruste.getVoimassaoloLoppuu() != null) {
            addTableCell(doc, itrow7, dateFormat.format(peruste.getVoimassaoloLoppuu()));
        } else {
            addTableCell(doc, itrow7, messages.translate("docgen.info.ei-asetettu", kieli));
        }
        */
    }

    private void addTocPage() {

    }

    private void addTutkinnonosat() {

    }

    private void addSisaltoElement() {

    }

    private void addGlossary() {

    }

    private void printStream(ByteArrayOutputStream stream) {
        LOG.info(new String(stream.toByteArray(), StandardCharsets.UTF_8));
    }

    private void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc),
                new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }

    private String getTextString(LokalisoituTeksti teksti, Kieli kieli) {
        if (teksti == null || teksti.getTeksti() == null || teksti.getTeksti().get(kieli) == null) {
            return "";
        } else {
            return teksti.getTeksti().get(kieli);
        }
    }

    private void addMarkupToElement(Document doc, Element element, String markup) {
        org.jsoup.nodes.Document fragment = Jsoup.parseBodyFragment(markup);
        jsoupIntoDOMNode(doc, element, fragment.body());
    }

    private void jsoupIntoDOMNode(Document rootDoc, Node parentNode, org.jsoup.nodes.Node jsoupNode) {
        for (org.jsoup.nodes.Node child : jsoupNode.childNodes()) {
            createDOM(child, parentNode, rootDoc, new HashMap<String, String>());
        }
    }

    private void createDOM(org.jsoup.nodes.Node node, Node out, Document doc, Map<String, String> ns) {

        if (node instanceof org.jsoup.nodes.Document) {

            org.jsoup.nodes.Document d = ((org.jsoup.nodes.Document) node);
            for (org.jsoup.nodes.Node n : d.childNodes()) {
                createDOM(n, out, doc, ns);
            }

        } else if (node instanceof org.jsoup.nodes.Element) {

            org.jsoup.nodes.Element e = ((org.jsoup.nodes.Element) node);
            // create all new elements into xhtml namespace
            org.w3c.dom.Element _e = doc.createElementNS("http://www.w3.org/1999/xhtml", e.tagName());
            out.appendChild(_e);
            org.jsoup.nodes.Attributes atts = e.attributes();

            for (org.jsoup.nodes.Attribute a : atts) {
                String attName = a.getKey();
                //omit xhtml namespace
                if (attName.equals("xmlns")) {
                    continue;
                }
                String attPrefix = getNSPrefix(attName);
                if (attPrefix != null) {
                    if (attPrefix.equals("xmlns")) {
                        ns.put(getLocalName(attName), a.getValue());
                    } else if (!attPrefix.equals("xml")) {
                        String namespace = ns.get(attPrefix);
                        if (namespace == null) {
                            //fix attribute names looking like qnames
                            attName = attName.replace(':', '_');
                        }
                    }
                }
                _e.setAttribute(attName, a.getValue());
            }

            for (org.jsoup.nodes.Node n : e.childNodes()) {
                createDOM(n, _e, doc, ns);
            }

        } else if (node instanceof org.jsoup.nodes.TextNode) {

            org.jsoup.nodes.TextNode t = ((org.jsoup.nodes.TextNode) node);
            if (!(out instanceof Document)) {
                out.appendChild(doc.createTextNode(t.getWholeText()));
            }
        }
    }

    private String getNSPrefix(String name) {
        if (name != null) {
            int pos = name.indexOf(':');
            if (pos > 0) {
                return name.substring(0, pos);
            }
        }
        return null;
    }

    private String getLocalName(String name) {
        if (name != null) {
            int pos = name.lastIndexOf(':');
            if (pos > 0) {
                return name.substring(pos + 1);
            }
        }
        return name;
    }
}
