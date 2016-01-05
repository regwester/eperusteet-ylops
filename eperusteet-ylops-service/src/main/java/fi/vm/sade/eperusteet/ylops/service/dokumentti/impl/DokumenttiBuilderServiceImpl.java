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
import fi.vm.sade.eperusteet.ylops.dto.ops.TermiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.DokumenttiBuilderService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.LocalizedMessagesService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.LiiteService;
import fi.vm.sade.eperusteet.ylops.service.ops.TermistoService;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.fop.apps.*;
import org.apache.xml.security.utils.Base64;
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
import java.awt.image.ColorModel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private DtoMapper mapper;

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

        CharapterNumberGenerator generator = new CharapterNumberGenerator();
        generator.increaseDepth();

        for (TekstiKappaleViite viite : ops.getTekstit().getLapset()) {
            // Tedään luvut
            Element header = doc.createElement("h" + generator.getDepth());
            header.setAttribute("number", generator.generateNumber());
            header.appendChild(doc.createTextNode(getTextString(viite.getTekstiKappale().getNimi(), kieli)));
            rootElement.appendChild(header);

            addTekstiKappale(doc, rootElement, viite, kieli, generator);

            generator.increaseNumber();
        }

        buildFootnotes(doc, ops, kieli);
        buildImages(doc, ops);
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

            // Luodaan sisältö
            String teskti = "<root>" + getTextString(lapsi.getTekstiKappale().getTeksti(), kieli) + "</root>";
            teskti = StringEscapeUtils.unescapeHtml4(teskti);

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

    private void buildFootnotes(Document doc, Opetussuunnitelma ops, Kieli kieli) {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        try {
            XPathExpression expression = xpath.compile("//abbr");
            NodeList list = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < list.getLength(); i++) {
                Element element = (Element) list.item(i);
                element.setAttribute("number", String.valueOf(i + 1));
                element.setAttribute("text", getFootnoteByKey(ops.getId(), element.getAttribute("data-viite"), kieli));
            }

        } catch (XPathExpressionException e) {
            LOG.error(e.getLocalizedMessage());
        }
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
                ColorModel model = bufferedImage.getColorModel();

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

                // Image writer
                ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
                ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
                jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                jpgWriteParam.setCompressionQuality(COMPRESSION_LEVEL);

                // Muunnetaan kuva base64 enkoodatuksi
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                //ImageIO.write(bufferedImage, "JPG", out); // ilman pakkaustason määrittämistä
                MemoryCacheImageOutputStream imageStream = new MemoryCacheImageOutputStream(out);
                jpgWriter.setOutput(imageStream);
                IIOImage outputImage = new IIOImage(bufferedImage, null, null);
                jpgWriter.write(null, outputImage, jpgWriteParam);
                jpgWriter.dispose();
                String base64 = Base64.encode(out.toByteArray());


                // Debug
                //LiiteDto liiteDto = liiteService.get(ops.getId(), uuid);
                //LOG.info(liiteDto.getNimi() + ", " + id + ", (" + width + ", " + height + ")");

                // Lisätään bas64 kuva img elementtiin
                element.setAttribute("width", String.valueOf(width));
                element.setAttribute("height", String.valueOf(height));
                element.setAttribute("src", "data:image/jpg;base64," + base64);
            }

        } catch (XPathExpressionException | IOException e) {
            LOG.error(e.getLocalizedMessage());
        }
    }

    private String getFootnoteByKey(Long opsId, String key, Kieli kieli) {
        TermiDto termiDto = termistoService.getTermi(opsId, key);

        if (termiDto == null)
            return "text missing";
        LokalisoituTekstiDto tekstiDto = termiDto.getSelitys();
        String selitys = getTextString(mapper.map(tekstiDto, LokalisoituTeksti.class), kieli);
        selitys = StringEscapeUtils.unescapeHtml4(selitys);
        selitys = selitys.replaceAll("\\<.*?>", "");

        return selitys;
    }
    private void addGlossary() {

    }

    private void printStream(ByteArrayOutputStream stream) {
        // Escapettaminen auttaa lukemista konsolista
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

    // Pieni apuluokka dokumentin lukujen generointiin
    private class CharapterNumberGenerator {
        private List<Integer> numbers = new ArrayList<>();
        private int startingValue = 1;

        public int getDepth() {
            return numbers.size();
        }

        public void increaseDepth() {
            numbers.add(startingValue);
        }

        public void decreaseDepth() {
            if (!numbers.isEmpty())
                numbers.remove(numbers.size() - 1);
        }

        public void increaseNumber() {
            if (!numbers.isEmpty()) {
                int last = numbers.size() - 1;
                numbers.set(last, numbers.get(last) + 1);
            }
        }

        public String generateNumber() {
            String numberString = new String();

            for (Integer number : numbers) {
                numberString += String.valueOf(number) + ".";
            }

            return numberString;
        }
    }
}
