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
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.external.KoodistoService;
import fi.vm.sade.eperusteet.ylops.service.external.OrganisaatioService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.LiiteService;
import fi.vm.sade.eperusteet.ylops.service.ops.TermistoService;
import org.apache.xml.security.utils.Base64;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
            throws TransformerException, IOException, SAXException, ParserConfigurationException, NullPointerException {

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
            PerusteDto perusteDto = null;
            if (ops.getCachedPeruste() != null && ops.getCachedPeruste().getPerusteId() != null) {
                // Käytetään ensisijaisesti cachetettua perustetta
                perusteDto = eperusteetService.getEperusteetPeruste(ops.getCachedPeruste().getPerusteId());
                if (perusteDto == null) {
                    perusteDto = eperusteetService.getPeruste(ops.getPerusteenDiaarinumero());
                }
            }
            if (perusteDto != null) {
                docBase.setPerusteDto(perusteDto);
                addVuosiluokkakokonaisuudet(docBase);
            }
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
        title.appendChild(docBase.getDocument().createTextNode(nimi));
        docBase.getHeadElement().appendChild(title);

        String tyyppi = messages.translate(docBase.getOps().getKoulutustyyppi().toString(), docBase.getKieli());
        Element type = docBase.getDocument().createElement("meta");
        type.setAttribute("name", "type");
        type.setAttribute("content", tyyppi);
        docBase.getHeadElement().appendChild(type);

        String kuvaus = getTextString(docBase.getOps().getKuvaus(), docBase.getKieli());
        if (kuvaus != null && kuvaus.length() != 0) {
            Element description = docBase.getDocument().createElement("meta");
            description.setAttribute("name", "description");
            description.setAttribute("content", kuvaus);
            docBase.getHeadElement().appendChild(description);
        }

        String diaarinumero = docBase.getOps().getPerusteenDiaarinumero();
        if (diaarinumero != null  && diaarinumero.length() != 0) {
            Element diary = docBase.getDocument().createElement("meta");
            diary.setAttribute("name", "diary");
            diary.setAttribute("content", diaarinumero);
            docBase.getHeadElement().appendChild(diary);
        }

        Set<KoodistoKoodi> koodistoKoodit = docBase.getOps().getKunnat();
        Element municipalities = docBase.getDocument().createElement("municipalities");
        for (KoodistoKoodi koodistoKoodi : koodistoKoodit) {
            Element kuntaEl = docBase.getDocument().createElement("municipality");
            KoodistoKoodiDto koodistoKoodiDto = koodistoService.get("kunta", koodistoKoodi.getKoodiUri());
            if (koodistoKoodiDto != null) {
                for (KoodistoMetadataDto metadata : koodistoKoodiDto.getMetadata()) {
                    if (metadata.getKieli().toLowerCase()
                            .equals(docBase.getKieli().toString().toLowerCase()))
                        kuntaEl.setTextContent(metadata.getNimi());
                }
            }

            municipalities.appendChild(kuntaEl);
        }
        docBase.getHeadElement().appendChild(municipalities);

        Set<String> organisaatiot = docBase.getOps().getOrganisaatiot();
        Element organizations = docBase.getDocument().createElement("organizations");
        for (String org : organisaatiot) {
            JsonNode orgNode = organisaatioService.getOrganisaatio(org);
            JsonNode nimiNode = orgNode.get("nimi");
            Element orgEl = docBase.getDocument().createElement("organization");
            orgEl.setTextContent(nimiNode.get(docBase.getKieli().toString()).asText());
            organizations.appendChild(orgEl);
        }
        docBase.getHeadElement().appendChild(organizations);

        Date paatospaivamaara = docBase.getOps().getPaatospaivamaara();
        if (paatospaivamaara != null) {
            String paatospaivamaaraText = new SimpleDateFormat("d.m.yyyy").format(paatospaivamaara);
            Element dateEl = docBase.getDocument().createElement("meta");
            dateEl.setAttribute("name", "date");
            dateEl.setAttribute("content", paatospaivamaaraText);
            docBase.getHeadElement().appendChild(dateEl);
        }
    }

    private void addYhteisetOsuudet(DokumenttiBase docBase)
            throws IOException, SAXException, ParserConfigurationException {

        if (docBase.getOps().getTekstit() != null) {
            addTekstiKappale(docBase, docBase.getOps().getTekstit());
        }
    }

    private void addTekstiKappale(DokumenttiBase docBase, TekstiKappaleViite viite)
            throws ParserConfigurationException, IOException, SAXException {
        docBase.getGenerator().increaseDepth();

        for (TekstiKappaleViite lapsi : viite.getLapset()) {
            if (lapsi.getTekstiKappale() != null && lapsi.getTekstiKappale().getNimi() != null) {
                addHeader(docBase, getTextString(lapsi.getTekstiKappale().getNimi(), docBase.getKieli()));

                // Opsin teksti luvulle
                if (lapsi.getTekstiKappale().getTeksti() != null) {
                    String teskti = "<div>" + getTextString(lapsi.getTekstiKappale().getTeksti(), docBase.getKieli()) + "</div>";

                    Document tempDoc = new W3CDom().fromJsoup(Jsoup.parseBodyFragment(teskti));
                    Node node = tempDoc.getDocumentElement().getChildNodes().item(1).getFirstChild();

                    docBase.getBodyElement().appendChild(docBase.getDocument().importNode(node, true));

                }
                // Rekursiivisesti
                addTekstiKappale(docBase, lapsi);

                docBase.getGenerator().increaseNumber();
            }
        }

        docBase.getGenerator().decreaseDepth();
    }

    private void addVuosiluokkakokonaisuudet(DokumenttiBase docBase) {
        Set<OpsVuosiluokkakokonaisuus> opsVlkset = docBase.getOps().getVuosiluokkakokonaisuudet();

        // Haetaan omat vuosiluokkkakokonaisuudet
        ArrayList<Vuosiluokkakokonaisuus> vlkset = new ArrayList<>();
        for (OpsVuosiluokkakokonaisuus opsVlk : opsVlkset) {
            if (opsVlk.isOma())
                vlkset.add(opsVlk.getVuosiluokkakokonaisuus());
        }

        // Järjestetään aakkosjärjestykseen
        vlkset = vlkset.stream()
                .sorted((vlk1, vlk2) -> vlk1.getNimi().getTeksti().get(docBase.getKieli())
                        .compareTo(vlk2.getNimi().getTeksti().get(docBase.getKieli())))
                .collect(Collectors.toCollection(ArrayList::new));

        for (Vuosiluokkakokonaisuus vlk : vlkset) {
            docBase.getGenerator().increaseDepth();

            if (vlk.getNimi() != null) {
                addHeader(docBase, getTextString(vlk.getNimi(), docBase.getKieli()));

                PerusopetuksenPerusteenSisaltoDto poPerusteenSisaltoDto = docBase.getPerusteDto().getPerusopetus();
                if (poPerusteenSisaltoDto != null && vlk.getTunniste().getId() != null) {
                    Optional<PerusteVuosiluokkakokonaisuusDto> optPerusteVlkDto =
                            poPerusteenSisaltoDto.getVuosiluokkakokonaisuudet(vlk.getTunniste().getId());
                    if (optPerusteVlkDto.isPresent()) {
                        PerusteVuosiluokkakokonaisuusDto perusteVlk = optPerusteVlkDto.get();

                        // Vuosiluokkan sisältö
                        docBase.getGenerator().increaseDepth();

                        if (perusteVlk.getSiirtymaEdellisesta() != null) {
                            addVlkYleisetOsiot(docBase, vlk, perusteVlk.getSiirtymaEdellisesta());
                        }

                        if (perusteVlk.getTehtava() != null) {
                            addVlkYleisetOsiot(docBase, vlk, perusteVlk.getTehtava());
                        }

                        if (perusteVlk.getSiirtymaSeuraavaan() != null) {
                            addVlkYleisetOsiot(docBase, vlk, perusteVlk.getSiirtymaSeuraavaan());
                        }

                        if (perusteVlk.getLaajaalainenOsaaminen() != null) {
                            addVlkYleisetOsiot(docBase, vlk, perusteVlk.getLaajaalainenOsaaminen());
                        }

                        addPaikallisestiPaatettavat(docBase, perusteVlk);

                        addOppiaineet(docBase, vlk);

                        docBase.getGenerator().decreaseDepth();
                    }
                }
                docBase.getGenerator().decreaseDepth();
            }
        }
    }

    private void addVlkYleisetOsiot(DokumenttiBase docBase, Vuosiluokkakokonaisuus vlk,
                                    PerusteTekstiOsaDto perusteTekstiOsaDto) {
        // Otsikko
        if (perusteTekstiOsaDto.getOtsikko() != null) {
            addHeader(docBase, getTextString(perusteTekstiOsaDto.getOtsikko(), docBase.getKieli()));

            // Perusteen teksi
            if (perusteTekstiOsaDto.getTeksti() != null) {
                addLokalisoituteksti(docBase, perusteTekstiOsaDto.getTeksti(), "cite");
            }

            // Opsin teksti
            Tekstiosa siirtymaEdellisesta = vlk.getSiirtymaEdellisesta();
            if (siirtymaEdellisesta != null && siirtymaEdellisesta.getTeksti() != null) {
                addLokalisoituteksti(docBase, siirtymaEdellisesta.getTeksti(), "div");
            }

            docBase.getGenerator().increaseNumber();
        }
    }

    private void addPaikallisestiPaatettavat(DokumenttiBase docBase, PerusteVuosiluokkakokonaisuusDto perusteVlk) {
        PerusteTekstiOsaDto perusteTekstiOsaDtoDto = perusteVlk.getPaikallisestiPaatettavatAsiat();
        if (perusteTekstiOsaDtoDto != null) {
            if (perusteTekstiOsaDtoDto.getOtsikko() != null) {
                LokalisoituTekstiDto otsikkoDto = perusteTekstiOsaDtoDto.getOtsikko();
                addHeader(docBase, getTextString(otsikkoDto, docBase.getKieli()));

                if (perusteTekstiOsaDtoDto.getTeksti() != null) {
                    LokalisoituTekstiDto tekstiDto = perusteTekstiOsaDtoDto.getTeksti();
                    addLokalisoituteksti(docBase, tekstiDto, "cite");
                }

                docBase.getGenerator().increaseNumber();
            }
        }
    }

    private void addOppiaineet(DokumenttiBase docBase, Vuosiluokkakokonaisuus vlk) {
        if (docBase.getOps() != null && docBase.getOps().getOppiaineet() != null) {
            Set<OpsOppiaine> oppiaineet = docBase.getOps().getOppiaineet();

            List<OpsOppiaine> oppiaineetAsc = oppiaineet.stream()
                    .filter(oa -> oa.getOppiaine() != null)
                    .sorted((oa1, oa2) -> oa1.getOppiaine().getNimi().getTeksti().get(docBase.getKieli())
                            .compareTo(oa2.getOppiaine().getNimi().getTeksti().get(docBase.getKieli())))
                    .collect(Collectors.toCollection(ArrayList::new));

            addHeader(docBase, messages.translate("oppiaineet", docBase.getKieli()));

            docBase.getGenerator().increaseDepth();

            // Oppiaineet akkosjärjestyksessä
            for (OpsOppiaine opsOppiaine : oppiaineetAsc) {
                Oppiaine oppiaine = opsOppiaine.getOppiaine();

                Set<Oppiaineenvuosiluokkakokonaisuus> oaVlkset = oppiaine.getVuosiluokkakokonaisuudet();

                Optional<Oppiaineenvuosiluokkakokonaisuus> optOaVlk = oaVlkset.stream()
                        .filter(o -> o.getVuosiluokkakokonaisuus().getId() == vlk.getTunniste().getId())
                        .findFirst();

                if (optOaVlk.isPresent()) {
                    UUID tunniste = oppiaine.getTunniste();

                    addHeader(docBase, getTextString(oppiaine.getNimi(), docBase.getKieli()));
                    addOppiaineTehtava(docBase, tunniste, oppiaine);

                    docBase.getGenerator().increaseDepth();
                    docBase.getGenerator().increaseDepth();



                    Oppiaineenvuosiluokkakokonaisuus oaVlk = optOaVlk.get();
                    Optional<PerusteOppiaineDto> optPerusteOppiaineDto = docBase.getPerusteDto().getPerusopetus().getOppiaine(tunniste);

                    PerusteOppiaineenVuosiluokkakokonaisuusDto perusteOaVlkDto = null;
                    if (optPerusteOppiaineDto.isPresent()) {
                        PerusteOppiaineDto perusteOppiaineDto = optPerusteOppiaineDto.get();

                        Optional<PerusteOppiaineenVuosiluokkakokonaisuusDto> optPerusteOaVlkDto =
                                perusteOppiaineDto.getVuosiluokkakokonaisuus(oaVlk.getVuosiluokkakokonaisuus().getId());
                        if (optPerusteOaVlkDto.isPresent()) {
                            perusteOaVlkDto = optPerusteOaVlkDto.get();
                        }
                    }

                    // Oppiaineen vuosiluokkakokonaiuuden generointi
                    addOppiaineVuosiluokkkakokonaisuus(docBase, perusteOaVlkDto, oaVlk, vlk);

                    docBase.getGenerator().decreaseDepth();
                    docBase.getGenerator().decreaseDepth();
                }
                docBase.getGenerator().increaseNumber();
            }
            docBase.getGenerator().decreaseDepth();
        }
    }

    private void addOppiaineVuosiluokkkakokonaisuus(DokumenttiBase docBase,
                                                    PerusteOppiaineenVuosiluokkakokonaisuusDto perusteOaVlkDto,
                                                    Oppiaineenvuosiluokkakokonaisuus oaVlkDto,
                                                    Vuosiluokkakokonaisuus vlk) {


        // Tehtävä
        if (perusteOaVlkDto != null) {
            addOppiaineYleisetOsiot(docBase, oaVlkDto.getTehtava(), perusteOaVlkDto.getTehtava());
        } else {
            addOppiaineYleisetOsiot(docBase, oaVlkDto.getTehtava());
        }

        // Tavoitteet
        if (perusteOaVlkDto != null && perusteOaVlkDto.getTavoitteet() != null) {
            List<PerusteOpetuksentavoiteDto> tavoitteet = perusteOaVlkDto.getTavoitteet();
            addHeader(docBase, messages.translate("tavoitteet-ja-sisallot-vuosiluokittain", docBase.getKieli()));

            for (PerusteOpetuksentavoiteDto perusteTavoiteDto : tavoitteet) {
                LokalisoituTekstiDto tavoiteDto = perusteTavoiteDto.getTavoite();
                addLokalisoituteksti(docBase, tavoiteDto, "cite");
            }
        }

        // Oppiaine vuosiluokka
        /*ArrayList<Oppiaineenvuosiluokka> vuosiluokat = oaVlkDto.getVuosiluokat().stream()
                .sorted((el1, el2) -> el1.getVuosiluokka().toString().compareTo(el2.getVuosiluokka().toString()))
                .collect(Collectors.toCollection(ArrayList::new));
        for (Oppiaineenvuosiluokka oaVuosiluokka : vuosiluokat) {
            // Vuosiluokka otsikko
            addHeader(docBase, messages.translate(oaVuosiluokka.getVuosiluokka().toString(), docBase.getKieli()));

            if (oaVuosiluokka.getSisaltoalueet() != null) {
                for (Keskeinensisaltoalue ksa : oaVuosiluokka.getSisaltoalueet()) {
                    docBase.getGenerator().increaseDepth();

                    addHeader(docBase, getTextString(ksa.getNimi(), docBase.getKieli()));

                    // Peruste
                    Optional<PerusteKeskeinensisaltoalueDto> optPerusteKsa = perusteOaVlkDto.getSisaltoalueet().stream()
                            .filter(pKsa -> pKsa.getTunniste().equals(ksa.getTunniste()))
                            .findFirst();
                    if (optPerusteKsa.isPresent()) {
                        PerusteKeskeinensisaltoalueDto perusteKsa = optPerusteKsa.get();
                        LokalisoituTekstiDto tekstiDto = perusteKsa.getKuvaus();
                        if (tekstiDto != null) {
                            addLokalisoituteksti(docBase, tekstiDto, "cite");
                        }
                    }

                    // Ops
                    if (ksa.getKuvaus() != null) {
                        addLokalisoituteksti(docBase, ksa.getKuvaus(), "div");
                        docBase.getGenerator().decreaseDepth();
                    }
                }
            }
        }*/

        // Työtavat
        if (perusteOaVlkDto != null) {
            addOppiaineYleisetOsiot(docBase, oaVlkDto.getTyotavat(), perusteOaVlkDto.getTyotavat());
        } else {
            addOppiaineYleisetOsiot(docBase, oaVlkDto.getTyotavat());
        }

        // Ohjaus
        if (perusteOaVlkDto != null) {
            addOppiaineYleisetOsiot(docBase, oaVlkDto.getOhjaus(), perusteOaVlkDto.getOhjaus());
        } else {
            addOppiaineYleisetOsiot(docBase, oaVlkDto.getOhjaus());
        }

        // Arviointi
        if (perusteOaVlkDto != null) {
            addOppiaineYleisetOsiot(docBase, oaVlkDto.getArviointi(), perusteOaVlkDto.getArviointi());
        } else {
            addOppiaineYleisetOsiot(docBase, oaVlkDto.getArviointi());
        }

        /*
        // Oppimäärät
        Oppiaine oppiaine = oaVlkDto.getOppiaine();
        if (oppiaine != null) {
            Optional<PerusteOppiaineDto> optPerusteOppiaineDto = docBase.getPerusteDto()
                    .getPerusopetus().getOppiaine(oppiaine.getTunniste());
            PerusteOppiaineDto perusteOppiaineDto;
            if (optPerusteOppiaineDto.isPresent()) {
                perusteOppiaineDto = optPerusteOppiaineDto.get();
                Set<PerusteOppiaineDto> perusteOppimaarat = perusteOppiaineDto.getOppimaarat();
                Set<Oppiaine> oppimaarat = oppiaine.getOppimaarat();

                addOppimaarat(docBase, perusteOppimaarat, oppimaarat, vlk);
            }
        }*/
    }

    private void addOppiaineYleisetOsiot(DokumenttiBase docBase, Tekstiosa tekstiosa) {
        if (tekstiosa != null) {
            addHeader(docBase, getTextString(tekstiosa.getOtsikko(), docBase.getKieli()));
            addLokalisoituteksti(docBase, tekstiosa.getTeksti(), "div");
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
        if (perusteOppimaarat != null) {
            for (PerusteOppiaineDto perusteOppiaineDto : perusteOppimaarat) {

                Optional<Oppiaine> optOppimaara = oppimaarat.stream()
                        .filter(oppiaine -> oppiaine.getTunniste().equals(perusteOppiaineDto.getTunniste()))
                        .findFirst();
                Oppiaine oppiaine = null;
                if (optOppimaara.isPresent()) {
                    oppiaine = optOppimaara.get();
                }

                addOppimaara(docBase, perusteOppiaineDto, oppiaine, vlk);
            }
        }
    }

    private void addOppimaara(DokumenttiBase docBase, PerusteOppiaineDto perusteOppiaineDto,
                              Oppiaine oppiaine, Vuosiluokkakokonaisuus vlk) {

        if (perusteOppiaineDto.getNimi().get(docBase.getKieli()) != null) {
            // Otsikko
            addHeader(docBase, getTextString(perusteOppiaineDto.getNimi(), docBase.getKieli()));

            docBase.getGenerator().increaseDepth();
            // Tehtävä
            addOppimaaraTehtava(docBase, perusteOppiaineDto, oppiaine);

            Optional<PerusteOppiaineenVuosiluokkakokonaisuusDto> optPerusteOaVlkDto
                    = perusteOppiaineDto.getVuosiluokkakokonaisuus(vlk.getTunniste().getId());


            PerusteOppiaineenVuosiluokkakokonaisuusDto perusteOaVlkDto;
            if (optPerusteOaVlkDto.isPresent()) {
                perusteOaVlkDto = optPerusteOaVlkDto.get();

                if (oppiaine != null) {
                    Optional<Oppiaineenvuosiluokkakokonaisuus> optOaVlk
                            = oppiaine.getVuosiluokkakokonaisuus(vlk.getTunniste().getId());

                    if (optOaVlk.isPresent()) {
                        Oppiaineenvuosiluokkakokonaisuus oaVlk = optOaVlk.get();

                        // Vuosiluokkakokonaisuudella
                        addOppimaaraTehtavaVuosiluokalla(docBase, perusteOaVlkDto, oaVlk);

                        // todo: Oppimäärän tavoitteet
                        // Oppimäärän tavoitteet vuosiluokkakokonaisuudella
                        //addOppimaaraTavoitteetVuosiluokkakokonaisuudella(docBase, perusteOaVlkDto, oaVlk);

                        // Työtavat
                        PerusteTekstiOsaDto perusteTyotavat = perusteOaVlkDto.getTyotavat();
                        if (perusteTyotavat != null) {
                            addHeader(docBase, getTextString(perusteTyotavat.getOtsikko(), docBase.getKieli()));
                            addLokalisoituteksti(docBase, perusteTyotavat.getTeksti(), "cite");
                        }
                        if (oaVlk != null) {
                            Tekstiosa tyotavat = oaVlk.getTyotavat();
                            LokalisoituTeksti ohjausTeksti = tyotavat.getTeksti();
                            addLokalisoituteksti(docBase, ohjausTeksti, "div");
                        }

                        // Ohjaus
                        PerusteTekstiOsaDto perusteOhjaus = perusteOaVlkDto.getOhjaus();
                        if (perusteOhjaus != null) {
                            addHeader(docBase, getTextString(perusteOhjaus.getOtsikko(), docBase.getKieli()));
                            addLokalisoituteksti(docBase, perusteOhjaus.getTeksti(), "cite");
                        }

                        if (oaVlk != null) {
                            Tekstiosa ohjaus = oaVlk.getOhjaus();
                            LokalisoituTeksti ohjausTeksti = ohjaus.getTeksti();
                            addLokalisoituteksti(docBase, ohjausTeksti, "div");
                        }

                        // Arviointi
                        PerusteTekstiOsaDto perusteArviointi = perusteOaVlkDto.getArviointi();
                        if (perusteArviointi != null) {
                            addHeader(docBase, getTextString(perusteArviointi.getOtsikko(), docBase.getKieli()));
                            addLokalisoituteksti(docBase, perusteArviointi.getTeksti(), "cite");
                        }

                        if (oaVlk != null) {
                            Tekstiosa arviointi = oaVlk.getArviointi();
                            LokalisoituTeksti arviointiTeksti = arviointi.getTeksti();
                            addLokalisoituteksti(docBase, arviointiTeksti, "div");
                        }
                    }
                }
            }

            docBase.getGenerator().decreaseDepth();
        }

    }

    private void addOppimaaraTehtava(DokumenttiBase docBase,
                                     PerusteOppiaineDto perusteOppiaineDto, Oppiaine oppiaine) {
        // Erityinen tehtävä
        PerusteTekstiOsaDto perusteTekstiOsaDto = perusteOppiaineDto.getTehtava();
        if (perusteTekstiOsaDto != null) {
            LokalisoituTekstiDto otsikkoDto = perusteTekstiOsaDto.getOtsikko();
            LokalisoituTekstiDto tekstiDto = perusteTekstiOsaDto.getTeksti();

            if (otsikkoDto != null && tekstiDto != null) {
                addHeader(docBase, getTextString(otsikkoDto, docBase.getKieli()));
                addLokalisoituteksti(docBase, tekstiDto, "cite");

                if (oppiaine != null) {
                    Tekstiosa tekstiosa = oppiaine.getTehtava();
                    if (tekstiosa != null) {
                        addLokalisoituteksti(docBase, tekstiosa.getTeksti(), "div");
                    }
                }
            }
        }
    }

    private void addOppimaaraTehtavaVuosiluokalla(DokumenttiBase docBase,
                                                  PerusteOppiaineenVuosiluokkakokonaisuusDto perusteOaVlkDto,
                                                  Oppiaineenvuosiluokkakokonaisuus oaVlk) {

        PerusteTekstiOsaDto perusteTekstiOsaDto = perusteOaVlkDto.getTehtava();
        if (perusteTekstiOsaDto != null) {
            LokalisoituTekstiDto tekstiDto = perusteTekstiOsaDto.getTeksti();

            // Tehtävä ei löydy valitulla kielellä
            if (tekstiDto != null) {
                addHeader(docBase, getTextString(perusteTekstiOsaDto.getOtsikko(), docBase.getKieli()));
                addLokalisoituteksti(docBase, tekstiDto, "cite");

                if (oaVlk != null) {
                    Tekstiosa tekstiosa = oaVlk.getTehtava();
                    addTekstiosa(docBase, tekstiosa, "div");
                }
            }
        }
    }

    /*private void addOppimaaraTavoitteetVuosiluokkakokonaisuudella(DokumenttiBase docBase,
                                                                  PerusteOppiaineenVuosiluokkakokonaisuusDto perusteOaVlkDto,
                                                                  Oppiaineenvuosiluokkakokonaisuus oaVlk) {

        addHeader(docBase, "Tavoitteet!");

        // Tavoitteet
        List<PerusteOpetuksentavoiteDto> perusteOpetuksentavoiteDtos = perusteOaVlkDto.getTavoitteet();
        for (PerusteOpetuksentavoiteDto perusteOpetuksentavoiteDto : perusteOpetuksentavoiteDtos) {
            LokalisoituTekstiDto lokalisoituTekstiDto = perusteOpetuksentavoiteDto.getTavoite();
            LokalisoituTeksti lokalisoituTeksti = mapper.map(lokalisoituTekstiDto, LokalisoituTeksti.class);

            addLokalisoituteksti(docBase, lokalisoituTeksti, "cite");
        }
    }*/

    private void addOppiaineTehtava(DokumenttiBase docBase, UUID tunniste,
                                    Oppiaine oppiaine) {

        if (docBase.getPerusteDto().getPerusopetus() != null) {
            Optional<PerusteOppiaineDto> optPerusteenOppiaineet
                    = docBase.getPerusteDto().getPerusopetus().getOppiaine(tunniste);
            PerusteOppiaineDto perusteenOppiaineet = null;

            if (optPerusteenOppiaineet.isPresent()) {
                perusteenOppiaineet = optPerusteenOppiaineet.get();
            }

            if (perusteenOppiaineet != null) {

                PerusteTekstiOsaDto tehtava = perusteenOppiaineet.getTehtava();
                if (tehtava != null)
                    addLokalisoituteksti(docBase, tehtava.getTeksti(), "cite");
            }
        }

        addTekstiosa(docBase, oppiaine.getTehtava(), "div");
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

        } catch (XPathExpressionException | IOException | NullPointerException e) {
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
