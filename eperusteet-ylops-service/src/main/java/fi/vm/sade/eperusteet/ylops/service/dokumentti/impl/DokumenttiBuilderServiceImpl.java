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

import com.github.jknack.handlebars.Handlebars;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.DokumenttiBuilderService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.LocalizedMessagesService;
import org.apache.commons.io.FileUtils;
import org.apache.fop.apps.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
            throws TransformerException, IOException, SAXException {
        // Täällä tehdään kaikki taika

        // Säilytetään luonnin aikana pdf dataa muistissa
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();

        /*Handlebars hb = new Handlebars();
        Map<String, String> model = new HashMap<>();

        model.put("globalStyles", getStyleShteet("docgen/ops-global-styles"));

        // Kansilehti
        addCoverPage();

        // Infosivu
        addInfoPage();

        // Sisällysluettelo
        addTocPage();

        // Tutkinnonosat
        addTutkinnonosat();

        // Sisältöelementit
        addSisaltoElement();

        // käsitteet
        addGlossary();*/

        Resource resource = applicationContext.getResource("classpath:docgen/test.xsl");
        pdf.write(convertOps2PDF(ops, resource.getFile()));

        // Lopetetaan leipominen ja siivotaan jäljet

        return pdf.toByteArray();
    }

    private byte[] convertOps2PDF(Opetussuunnitelma ops, File xslt)
            throws IOException, TransformerException, SAXException {
        // Streams
        ByteArrayOutputStream xmlStream = new ByteArrayOutputStream();
        ByteArrayOutputStream foStream = new ByteArrayOutputStream();
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();

        // Muunnetaan ops objekti xml muotoon
        convertOps2XML(ops, xmlStream);
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

    private void convertOps2XML(Opetussuunnitelma ops, OutputStream xml)
            throws IOException, TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();

        String test = "<test><asd>asd</asd></test>";

        InputStream inputStream = new ByteArrayInputStream(test.getBytes(StandardCharsets.UTF_8));

        Source src = new StreamSource(inputStream);
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
    public void convertFO2PDF(InputStream fo, OutputStream pdf) throws IOException, SAXException {

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

        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        } finally {
            pdf.close();
        }
    }

    private void addCoverPage() {

    }

    private void addInfoPage() {

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

    private String getStyleShteet(String path) {
        Resource resource = applicationContext.getResource("classpath:" + path + ".css");
        String styles = "";
        try {
            if (resource.exists()) {
                styles = FileUtils.readFileToString(resource.getFile(), "UTF-8");
            } else {
                LOG.warn("Cannot found styles for pdf");
            }
        } catch (IOException ex) {
            LOG.error("Cannot read exsisting style sheet: " + ex.getMessage());
        }
        return styles;
    }
}
