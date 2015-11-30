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


ylopsApp
    .service('LukioOpetussuunnitelmaService', function(OpetusuunnitelmaLukio, $q:IQService,
                                                       Notifikaatiot, $stateParams, $log) {
        var doGetAihekokonaisuudet = function(id: number, d: IDeferred<Lukio.AihekokonaisuudetPerusteenOsa>) {
            OpetusuunnitelmaLukio.aihekokonaisuudet({opsId: id})
                    .$promise.then(function(aihekok: Lukio.AihekokonaisuudetPerusteenOsa) {
                d.resolve(aihekok);
            }, Notifikaatiot.serverCb);
        };
        var aiheKokCache = new Cache.Cached<Lukio.AihekokonaisuudetPerusteenOsa>($q, doGetAihekokonaisuudet);
        var getAihekokonaisuudet = function(id?: number): IPromise<Lukio.AihekokonaisuudetPerusteenOsa> {
            return aiheKokCache.get(id || $stateParams.id);
        };

        var doGetOpetuksenYleisetTavoitteet = function(id: number, d: IDeferred<Lukio.OpetuksenYleisetTavoitteetPerusteenOsa>) {
            OpetusuunnitelmaLukio.opetuksenYleisetTavoitteet({opsId: id})
                    .$promise.then(function(yleisetTavoitteet: Lukio.OpetuksenYleisetTavoitteetPerusteenOsa) {
                d.resolve(yleisetTavoitteet);
            }, Notifikaatiot.serverCb);
        };
        var yleisetTavoitteetCache = new Cache.Cached<Lukio.OpetuksenYleisetTavoitteetPerusteenOsa>($q, doGetOpetuksenYleisetTavoitteet);
        var getOpetuksenYleisetTavoitteet = function(id?: number): IPromise<Lukio.OpetuksenYleisetTavoitteetPerusteenOsa> {
            return yleisetTavoitteetCache.get(id || $stateParams.id);
        };

        var doGetRakenne = function(id: number, d: IDeferred<Lukio.LukioOpetussuunnitelmaRakenneOps>) {
            OpetusuunnitelmaLukio.rakenne({opsId: id})
                    .$promise.then(function(rakenne: Lukio.LukioOpetussuunnitelmaRakenneOps) {
                d.resolve(rakenne);
            }, Notifikaatiot.serverCb);
        };
        var rakenneCache = new Cache.Cached<Lukio.LukioOpetussuunnitelmaRakenneOps>($q, doGetRakenne);
        var getRakenne = function(id?: number): IPromise<Lukio.LukioOpetussuunnitelmaRakenneOps> {
            return rakenneCache.get(id || $stateParams.id);
        };

        var oppiaineCache = rakenneCache.related(function(from: Lukio.LukioOpetussuunnitelmaRakenneOps)
                    : {[key:number]: Lukio.LukioOppiaine} {
            var oas = _(from.oppiaineet).flattenTree(function(oa: Lukio.LukioOppiaine) {
                return oa.oppimaarat || [];
            }).indexBy(_.property('id')).value();
            $log.info('Get oppiaineet from', from, ' got ', oas);
            return oas;
        });
        var getOppiaine = function(id: number, opsId?: number): IPromise<Lukio.LukioOppiaine> {
            return oppiaineCache.get(opsId || $stateParams.id, id);
        };
        var kurssiCache = oppiaineCache.related(function(from: Lukio.LukioOppiaine)
                    : {[key:number]: Lukio.LukiokurssiOps} {
            var kurssit = _(from.kurssit).indexBy(_.property('id')).value();
            $log.info('Kurssit from ', from, ' got ', kurssit);
            return kurssit;
        });
        var getKurssi = function(oppiaineId: number, kurssiId: number, opsId?: number): IPromise<Lukio.LukiokurssiOps> {
            return kurssiCache.get(opsId || $stateParams.id, oppiaineId, kurssiId);
        };

        return {
            getAihekokonaisuudet: getAihekokonaisuudet,
            getOpetuksenYleisetTavoitteet: getOpetuksenYleisetTavoitteet,
            getRakenne: getRakenne,
            getOppiaine: getOppiaine,
            getKurssi: getKurssi
        }
    });
