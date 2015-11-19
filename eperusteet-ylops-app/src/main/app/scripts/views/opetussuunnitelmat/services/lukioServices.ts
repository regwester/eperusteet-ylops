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

'use strict';

interface Lokalisoitu {
    fi?: string;
    sv?: string;
    en?: string;
}

interface PerusteOpsOsa<Perusteen,Opsin> {
    perusteen?: Perusteen;
    paikallinen: Opsin;
    tunniste?: string;
    kommentti?: string;
}

interface Aihekokonaisuus {
    tunniste?: string;
    id?: number;
    jnro?: number;
    otsikko?: Lokalisoitu;
    yleiskuvaus?: Lokalisoitu;
}

interface Aihekokonaisuudet {
    uuidTunniste?: string;
    id? : number;
    otsikko?: Lokalisoitu;
    yleiskuvaus?: Lokalisoitu;
    aihekokonaisuudet: Aihekokonaisuus[];
}

interface OpsAihekokonaisuus extends Aihekokonaisuus {
    perusteen?: Aihekokonaisuus;
}

interface OpsAihekokonaisuudet extends Aihekokonaisuudet {
    aihekokonaisuudet: OpsAihekokonaisuus[];
}

interface OpetuksenYleisetTavoitteet {
    uuidTunniste?: string;
    id?: number;
    otsikko?: Lokalisoitu;
    kuvaus?: Lokalisoitu;
}

interface OpsOpetuksenYleisetTavoitteet extends OpetuksenYleisetTavoitteet {
    perusteen?: OpetuksenYleisetTavoitteet
}

interface AihekokonaisuudetPerusteenOsa extends PerusteOpsOsa<Aihekokonaisuudet,
    OpsAihekokonaisuudet> {}
interface OpetuksenYleisetTavoitteetPerusteenOsa extends PerusteOpsOsa<OpetuksenYleisetTavoitteet,
    OpsOpetuksenYleisetTavoitteet> {}

ylopsApp
    .service('LukioOpetussuunnitelmaService', function(OpetusuunnitelmaLukio, $q:IQService,
                                                       Notifikaatiot, $stateParams) {
        var aiheKokCache:AihekokonaisuudetPerusteenOsa = null;
        var yleisetTavoitteetCache:OpetuksenYleisetTavoitteetPerusteenOsa = null;

        var getAihekokonaisuudet = function(): IPromise<AihekokonaisuudetPerusteenOsa> {
            if (aiheKokCache) {
                return $q.when<AihekokonaisuudetPerusteenOsa>(aiheKokCache);
            }
            var d = $q.defer<AihekokonaisuudetPerusteenOsa>();
            OpetusuunnitelmaLukio.aihekokonaisuudet({opsId: $stateParams.id})
                    .$promise.then(function(aihekok: AihekokonaisuudetPerusteenOsa) {
                aiheKokCache = aihekok;
                d.resolve(aihekok);
            }, Notifikaatiot.serverCb);
            return d.promise;
        };

        var getOpetuksenYleisetTavoitteet = function(): IPromise<OpetuksenYleisetTavoitteetPerusteenOsa> {
            if (yleisetTavoitteetCache) {
                return $q.when<OpetuksenYleisetTavoitteetPerusteenOsa>(yleisetTavoitteetCache);
            }
            var d = $q.defer<OpetuksenYleisetTavoitteetPerusteenOsa>();
            OpetusuunnitelmaLukio.opetuksenYleisetTavoitteet({opsId: $stateParams.id})
                    .$promise.then(function(yleisetTavoitteet: OpetuksenYleisetTavoitteetPerusteenOsa) {
                yleisetTavoitteetCache = yleisetTavoitteet;
                d.resolve(yleisetTavoitteet);
            }, Notifikaatiot.serverCb);
            return d.promise;
        };

        return {
            getAihekokonaisuudet: getAihekokonaisuudet,
            getOpetuksenYleisetTavoitteet: getOpetuksenYleisetTavoitteet
        }
    });
