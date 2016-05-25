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

import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappaleViite;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.LocalizedMessagesService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.YleisetOsuudetService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiUtils.addHeader;
import static fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiUtils.addLokalisoituteksti;
import static fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiUtils.getTextString;

/**
 * @author isaul
 */
@Service
@Transactional
public class YleisetOsuudetServiceImpl implements YleisetOsuudetService {
    @Autowired
    private LocalizedMessagesService messages;

    public void addYleisetOsuudet(DokumenttiBase docBase)
            throws IOException, SAXException, ParserConfigurationException {

        if (docBase.getOps().getTekstit() != null) {
            addTekstiKappale(docBase, docBase.getOps().getTekstit(), true);
        }
    }

    private void addTekstiKappale(DokumenttiBase docBase, TekstiKappaleViite viite, boolean paataso)
            throws ParserConfigurationException, IOException, SAXException {

        for (TekstiKappaleViite lapsi : viite.getLapset()) {
            if (lapsi != null && lapsi.getTekstiKappale() != null) {

                if (paataso && lapsi.getTekstiKappale() != null
                        && lapsi.getTekstiKappale().getNimi() != null
                        && lapsi.getTekstiKappale().getNimi().getTeksti() != null
                        && lapsi.getTekstiKappale().getNimi().getTeksti().get(docBase.getKieli()) != null
                        && lapsi.getTekstiKappale().getNimi().getTeksti().get(docBase.getKieli())
                        .equals(messages.translate("liitteet", docBase.getKieli()))) {
                    // Jos on liitteet päätasolla niin siirrytään seuraavaan tekstiin
                    continue;
                }

                // Ei näytetä yhteisen osien Pääkappaleiden otsikoita
                // Opetuksen järjestäminen ja Opetuksen toteuttamisen lähtökohdat
                if (paataso) {
                    addTekstiKappale(docBase, lapsi, false);
                } else {

                    if (lapsi.getTekstiKappale().getNimi() != null) {
                        addHeader(docBase, getTextString(docBase, lapsi.getTekstiKappale().getNimi()));
                    }

                    // Opsin teksti luvulle
                    if (lapsi.getTekstiKappale().getTeksti() != null) {
                        addLokalisoituteksti(docBase, lapsi.getTekstiKappale().getTeksti(), "div");
                    }

                    if (lapsi.getTekstiKappale().getNimi() != null) {
                        docBase.getGenerator().increaseDepth();
                    }

                    // Rekursiivisesti
                    addTekstiKappale(docBase, lapsi, false);

                    if (lapsi.getTekstiKappale().getNimi() != null) {
                        docBase.getGenerator().decreaseDepth();
                        docBase.getGenerator().increaseNumber();
                    }
                }
            }
        }
    }

    public void addLiitteet(DokumenttiBase docBase) throws IOException, SAXException, ParserConfigurationException {
        if (docBase.getOps().getTekstit() != null) {
            for (TekstiKappaleViite liiteViite : docBase.getOps().getTekstit().getLapset()) {
                if (liiteViite != null
                        && liiteViite.getTekstiKappale() != null
                        && liiteViite.getTekstiKappale().getNimi() != null
                        && liiteViite.getTekstiKappale().getNimi().getTeksti() != null
                        && liiteViite.getTekstiKappale().getNimi().getTeksti().get(docBase.getKieli()) != null
                        && liiteViite.getTekstiKappale().getNimi().getTeksti().get(docBase.getKieli())
                        .equals(messages.translate("liitteet", docBase.getKieli()))) {
                    addTekstiKappale(docBase, liiteViite, false);
                }
            }
        }
    }
}
