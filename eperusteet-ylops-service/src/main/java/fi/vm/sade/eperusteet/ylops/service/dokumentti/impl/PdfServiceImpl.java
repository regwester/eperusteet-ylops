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

import fi.vm.sade.eperusteet.ylops.service.dokumentti.PdfService;
import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author isaul
 */
@Service
@Transactional
public class PdfServiceImpl implements PdfService {
    private static final Logger LOG = LoggerFactory.getLogger(PdfServiceImpl.class);

    @Value("classpath:docgen/xhtml-to-xslfo.xsl")
    Resource template;

    @Value("classpath:docgen/fop.xconf")
    Resource config;

    @Override
    public byte[] xhtml2pdf(Document document) throws IOException, TransformerException, SAXException {
        return convertOps2PDF(document, template.getFile());
    }


    private byte[] convertOps2PDF(Document doc, File xslt)
            throws IOException, TransformerException, SAXException {
        // Alustetaan Streamit
        ByteArrayOutputStream xmlStream = new ByteArrayOutputStream();
        ByteArrayOutputStream foStream = new ByteArrayOutputStream();
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();

        // Muunnetaan ops objekti xml muotoon
        convertOps2XML(doc, xmlStream);
        //LOG.info("Generted XML  :");
        printStream(xmlStream);

        // Muunntetaan saatu xml malli fo:ksi
        InputStream xmlInputStream = new ByteArrayInputStream(xmlStream.toByteArray());
        convertXML2FO(xmlInputStream, xslt, foStream);
        //LOG.info("Generated XSL-FO:");
//        printStream(foStream);

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

        FopFactory fopFactory = FopFactory.newInstance(config.getFile());

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

    private void printStream(ByteArrayOutputStream stream) {
        // Escapettaminen auttaa lukemista konsolista
        LOG.error(new String(stream.toByteArray(), StandardCharsets.UTF_8));
    }
}
