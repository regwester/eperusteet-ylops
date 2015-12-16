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
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.lowagie.text.DocumentException;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.DokumenttiBuilderService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.LocalizedMessagesService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
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
    public byte[] generatePdf() throws IOException, DocumentException {
        // Täällä tehdään kaikki taika


        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();

        //final File outputFile = File.createTempFile("FlyingSacuer.test", ".pdf");
        //OutputStream os = new FileOutputStream(outputFile);

        Handlebars hb = new Handlebars();
        ITextRenderer renderer = new ITextRenderer();
        Map<String, String> model = new HashMap<>();

        Resource resource = applicationContext.getResource("classpath:" + "docgen/ops-style.css");
        if (resource.exists()) {
            String styles = FileUtils.readFileToString(resource.getFile(), "UTF-8");
            model.put("styles", styles);
        } else {
            LOG.warn("Cannot found styles for pdf");
        }

        // Kansilehti
        addCoverPage(renderer, hb, model, pdfStream);

        // Infosivu
        addInfoPage(renderer, hb);

        // Sisällysluettelo
        addTocPage(renderer, hb);

        // Tutkinnonosat
        addTutkinnonosat();

        // Sisältöelementit
        addSisaltoElement();

        // käsitteet
        addGlossary();

        // Lopetetaan leipominen ja siivotaan jäljet
        renderer.finishPDF();
        //io.close();

        //LOG.info("Test pdf generated to " + outputFile);

        //InputStream io = new FileInputStream(outputFile);
        //return IOUtils.toByteArray(io);

        return pdfStream.toByteArray();
    }

    private void addCoverPage(ITextRenderer renderer, Handlebars hb, Map<String, String> model, OutputStream os)
            throws IOException, DocumentException {
        Template template = hb.compile("docgen/ops-cover-page");

        Map<String, String> coverModel = new HashMap<>();
        coverModel.put("otsikko", "Otsikko");
        coverModel.put("aliotsikko", "Aliotsikko");

        Context context = Context
                .newBuilder(model)
                .combine(coverModel)
                .resolver(MapValueResolver.INSTANCE)
                .build();

        renderer.setDocumentFromString(template.apply(context));
        renderer.layout();
        renderer.createPDF(os, false);
    }

    private void addInfoPage(ITextRenderer renderer, Handlebars hb) throws IOException, DocumentException {
        Template template = hb.compile("docgen/ops-template");

        renderer.setDocumentFromString(template.apply("Handlebars info"));
        renderer.layout();
        renderer.writeNextDocument();
    }

    private void addTocPage(ITextRenderer renderer, Handlebars hb) throws IOException, DocumentException {
        Template template = hb.compile("docgen/ops-toc-page");

        renderer.setDocumentFromString(template.apply("Handlebars toc"));
        renderer.layout();
        renderer.writeNextDocument();
    }

    private void addTutkinnonosat() {

    }

    private void addSisaltoElement() {

    }

    private void addGlossary() {

    }
}
