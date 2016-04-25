package fi.vm.sade.eperusteet.ylops.service.dokumentti.impl;

import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappaleViite;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.YleisetOsuudetService;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiBase;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiUtils.addHeader;
import static fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiUtils.getTextString;

/**
 * @author isaul
 */
@Service
public class YleisetOsuudetServiceImpl implements YleisetOsuudetService {

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
                        String teskti = "<div>" + getTextString(docBase, lapsi.getTekstiKappale().getTeksti()) + "</div>";

                        Document tempDoc = new W3CDom().fromJsoup(Jsoup.parseBodyFragment(teskti));
                        Node node = tempDoc.getDocumentElement().getChildNodes().item(1).getFirstChild();

                        docBase.getBodyElement().appendChild(docBase.getDocument().importNode(node, true));

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
}
