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

import LukioOppiaine = Lukio.LukioOppiaine;
'use strict';

interface LukioOpetussuunnitelmaServiceI {
    getAihekokonaisuudet(id?: number): IPromise<Lukio.AihekokonaisuudetPerusteenOsa>
    getOpetuksenYleisetTavoitteet(id?: number) : IPromise<Lukio.OpetuksenYleisetTavoitteetPerusteenOsa>
    getRakenne(id?: number): IPromise<Lukio.LukioOpetussuunnitelmaRakenneOps>
    getOppiaine(id: number, opsId?: number): IPromise<Lukio.LukioOppiaine>
    getKurssi(oppiaineId: number, kurssiId: number, opsId?: number): IPromise<Lukio.LukiokurssiOps>
}

ylopsApp
    .service('LukioOpetussuunnitelmaService', function(OpetusuunnitelmaLukio, $q:IQService,
                                                       Notifikaatiot, $stateParams) {
        var doGetAihekokonaisuudet =
            (id: number, d: IDeferred<Lukio.AihekokonaisuudetPerusteenOsa>) =>
                OpetusuunnitelmaLukio.aihekokonaisuudet({opsId: id})
                    .$promise.then((aihekok: Lukio.AihekokonaisuudetPerusteenOsa) => d.resolve(aihekok),
                            Notifikaatiot.serverCb);
        var aiheKokCache = new Cache.Cached<Lukio.AihekokonaisuudetPerusteenOsa>($q, doGetAihekokonaisuudet);
        var getAihekokonaisuudet = (id?: number) => aiheKokCache.get(id || $stateParams.id);

        var doGetOpetuksenYleisetTavoitteet =
            (id: number, d: IDeferred<Lukio.OpetuksenYleisetTavoitteetPerusteenOsa>) =>
                OpetusuunnitelmaLukio.opetuksenYleisetTavoitteet({opsId: id})
                    .$promise.then((yleisetTavoitteet: Lukio.OpetuksenYleisetTavoitteetPerusteenOsa) =>
                        d.resolve(yleisetTavoitteet), Notifikaatiot.serverCb);
        var yleisetTavoitteetCache = new Cache.Cached<Lukio.OpetuksenYleisetTavoitteetPerusteenOsa>($q,
            doGetOpetuksenYleisetTavoitteet);
        var getOpetuksenYleisetTavoitteet = (id?: number) => yleisetTavoitteetCache.get(id || $stateParams.id);

        var doGetRakenne = (id: number, d: IDeferred<Lukio.LukioOpetussuunnitelmaRakenneOps>) =>
            OpetusuunnitelmaLukio.rakenne({opsId: id})
                .$promise.then((rakenne: Lukio.LukioOpetussuunnitelmaRakenneOps) => d.resolve(rakenne), Notifikaatiot.serverCb);
        var rakenneCache = new Cache.Cached<Lukio.LukioOpetussuunnitelmaRakenneOps>($q, doGetRakenne);
        var getRakenne = (id?: number) => rakenneCache.get(id || $stateParams.id);

        var oppiaineCache = rakenneCache.related((from: Lukio.LukioOpetussuunnitelmaRakenneOps) : {[key:number]: Lukio.LukioOppiaine} =>
            _(from.oppiaineet).flattenTree((oa: Lukio.LukioOppiaine) => oa.oppimaarat || [])
                .indexBy(_.property('id')).value());
        var getOppiaine = (id: number, opsId?: number) => oppiaineCache.get(opsId || $stateParams.id, id);
        var kurssiCache = oppiaineCache.related((from: Lukio.LukioOppiaine) : {[key:number]: Lukio.LukiokurssiOps} =>
            _(from.kurssit).indexBy(_.property('id')).value());
        var getKurssi = (oppiaineId: number, kurssiId: number, opsId?: number) =>
            kurssiCache.get(opsId || $stateParams.id, oppiaineId, kurssiId);

        return <LukioOpetussuunnitelmaServiceI>{
            getAihekokonaisuudet: getAihekokonaisuudet,
            getOpetuksenYleisetTavoitteet: getOpetuksenYleisetTavoitteet,
            getRakenne: getRakenne,
            getOppiaine: getOppiaine,
            getKurssi: getKurssi
        }
    });
