package fi.vm.sade.eperusteet.ylops.service.dokumentti;

import fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util.DokumenttiBase;

/**
 * @author isaul
 */
public interface LukioService {
    void addOpetuksenYleisetTavoitteet(DokumenttiBase docBase);
    void addAihekokonaisuudet(DokumenttiBase docBase);
    void addOppiaineet(DokumenttiBase docBase);
}
