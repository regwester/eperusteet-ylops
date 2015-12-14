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
import com.github.jknack.handlebars.Template;
import com.lowagie.text.DocumentException;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.DokumenttiBuilderService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author iSaul
 */
@Service
public class DokumenttiBuilderServiceImpl implements DokumenttiBuilderService {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiBuilderServiceImpl.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public byte[] generatePdf() throws IOException, DocumentException {
        // Täällä tehdään kaikki taika

        StringBuffer htmlDokumentti = new StringBuffer();
        htmlDokumentti.append("<html style='color: red'>");
        htmlDokumentti.append("moi");
        htmlDokumentti.append("</html>");

        LOG.info(applicationContext.getResource("/").getURL().toString());
        //LOG.info(applicationContext.getResource("docgen/ops_template.hbs").getURL().toString());

        Handlebars handlebars = new Handlebars();
        Template template = handlebars.compile("docgen/ops-template");

        final File outputFile = File.createTempFile("FlyingSacuer.test", ".pdf");
        OutputStream os = new FileOutputStream(outputFile);

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(template.apply("Handlebars.java"));
        renderer.layout();
        renderer.createPDF(os, false);

        // kansilehti
        addCoverPage();

        // infosivu
        addInfoPage();

        // pudotellaan tutkinnonosat paikalleen
        addTutkinnonosat();

        // sisältöelementit (proosa)
        addSisaltoElement();

        // käsitteet
        addGlossary();

        // Lopetetaan leipominen ja siivotaan jäljet
        renderer.finishPDF();
        os.close();

        LOG.info("Test pdf generated to " + outputFile);

        InputStream io = new FileInputStream(outputFile);
        return IOUtils.toByteArray(io);
    }

    private void addCoverPage() {

    }

    private void addInfoPage() {

    }

    private void addTutkinnonosat() {

    }

    private void addSisaltoElement() {

    }

    private void addGlossary() {

    }
}
