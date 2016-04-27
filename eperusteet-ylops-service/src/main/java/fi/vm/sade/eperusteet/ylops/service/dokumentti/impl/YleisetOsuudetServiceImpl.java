package fi.vm.sade.eperusteet.ylops.service.dokumentti.impl;

import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappaleViite;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.LocalizedMessagesService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.YleisetOsuudetService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
            if (lapsi.getTekstiKappale() != null) {

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
                if (liiteViite.getTekstiKappale() != null
                        && liiteViite.getTekstiKappale().getNimi() != null
                        && liiteViite.getTekstiKappale().getNimi().getTeksti() != null
                        && liiteViite.getTekstiKappale().getNimi().getTeksti().get(docBase.getKieli()) != null
                        && liiteViite.getTekstiKappale().getNimi().getTeksti().get(docBase.getKieli())
                        .equals(messages.translate("liitteet", docBase.getKieli()))) {
                    addTekstiKappale(docBase, liiteViite, false);
                }
            }

            // todo: Miksi teksti on null streamissa?
            /*Optional<TekstiKappaleViite> optLiitteetViite = docBase.getOps().getTekstit().getLapset().stream()
                    .filter(teksti -> teksti.getTekstiKappale() != null
                            && teksti.getTekstiKappale().getNimi() != null
                            && teksti.getTekstiKappale().getNimi().getTeksti() != null
                            && teksti.getTekstiKappale().getNimi().getTeksti().get(docBase.getKieli()) != null)
                    .filter(teksti -> teksti.getTekstiKappale().getNimi().getTeksti().get(docBase.getKieli())
                            .equals(messages.translate("liitteet", docBase.getKieli())))
                    .findFirst();

            if (optLiitteetViite.isPresent()) {
                TekstiKappaleViite liiteViite = optLiitteetViite.get();
                yleisetOsuudetService.addTekstiKappale(docBase, liiteViite, true);
            }*/
        }
    }
}
