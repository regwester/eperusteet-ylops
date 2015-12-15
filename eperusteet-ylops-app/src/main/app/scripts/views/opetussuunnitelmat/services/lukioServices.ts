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
import Oppiaine = Lukio.Oppiaine;
import IdHolder = Lukio.IdHolder;
'use strict';

interface LukioOpetussuunnitelmaServiceI {
    getAihekokonaisuudet(id?: number): IPromise<Lukio.AihekokonaisuudetPerusteenOsa>
    getOpetuksenYleisetTavoitteet(id?: number) : IPromise<Lukio.OpetuksenYleisetTavoitteetPerusteenOsa>
    onAihekokonaisuudetUpdate(then: () => void ) : void
    onRaknneUpdate(then: () => void ) : void
    getRakenne(id?: number): IPromise<Lukio.LukioOpetussuunnitelmaRakenneOps>
    getOppiaine(id: number, opsId?: number): IPromise<Lukio.LukioOppiaine>
    saveOppiaine(oppiaine: Lukio.LukioOppiaineTallennus, opsId?: number): IPromise<IdHolder>
    updateOppiaine(oppiaine: Lukio.LukioOppiaineTallennus, opsId?: number): IPromise<void>
    getKurssi(oppiaineId: number, kurssiId: number, opsId?: number): IPromise<Lukio.LukiokurssiOps>
    kloonaaOppiaineMuokattavaksi(oppiaineId:number, opsId?:number): IPromise<IdHolder>
    palautaYlempaan(oppiaineId:number, opsId?:number): IPromise<IdHolder>,
    updateOppiaineKurssiStructure(treeRoot:LukioKurssiTreeNode, kommentti?: string, opsId?: number): IPromise<void>,
    addKielitarjonta(oppiaineId:number, tarjonta:Lukio.OppiaineKielitarjonta, opsId?: number): IPromise<Lukio.IdHolder>
    deleteOppiaine(oppiaineId:number, opsId?: number): IPromise<void>
    saveKurssi(kurssi:Lukio.LuoLukiokurssi, opsId?: number): IPromise<IdHolder>
    updateKurssi(kurssiId:number, kurssi:Lukio.UpdateLukiokurssi, opsId?: number): IPromise<void>
    disconnectKurssi(kurssiId:number, opsId:number): IPromise<void>
}

ylopsApp
    .service('LukioOpetussuunnitelmaService', function(OpetusuunnitelmaLukio, $q:IQService, $log,
                                                       Notifikaatiot, $stateParams, OppiaineCRUD) {
        var doGetAihekokonaisuudet =
            (id: number, d: IDeferred<Lukio.AihekokonaisuudetPerusteenOsa>) =>
                OpetusuunnitelmaLukio.aihekokonaisuudet({opsId: id})
                    .$promise.then((aihekok: Lukio.AihekokonaisuudetPerusteenOsa) => d.resolve(aihekok),
                            Notifikaatiot.serverCb);
        var aiheKokCache = cached<Lukio.AihekokonaisuudetPerusteenOsa>($q, doGetAihekokonaisuudet);
        var getAihekokonaisuudet = (id?: number) => aiheKokCache.get(id || $stateParams.id);

        var doGetOpetuksenYleisetTavoitteet =
            (id: number, d: IDeferred<Lukio.OpetuksenYleisetTavoitteetPerusteenOsa>) =>
                OpetusuunnitelmaLukio.opetuksenYleisetTavoitteet({opsId: id})
                    .$promise.then((yleisetTavoitteet: Lukio.OpetuksenYleisetTavoitteetPerusteenOsa) =>
                        d.resolve(yleisetTavoitteet), Notifikaatiot.serverCb);
        var yleisetTavoitteetCache = cached<Lukio.OpetuksenYleisetTavoitteetPerusteenOsa>($q,
            doGetOpetuksenYleisetTavoitteet);
        var getOpetuksenYleisetTavoitteet = (id?: number) => yleisetTavoitteetCache.get(id || $stateParams.id);

        var doGetRakenne = (id: number, d: IDeferred<Lukio.LukioOpetussuunnitelmaRakenneOps>) =>
            OpetusuunnitelmaLukio.rakenne({opsId: id})
                .$promise.then((rakenne: Lukio.LukioOpetussuunnitelmaRakenneOps) => d.resolve(rakenne), Notifikaatiot.serverCb);
        var rakenneCache = cached<Lukio.LukioOpetussuunnitelmaRakenneOps>($q, doGetRakenne);
        var getRakenne = (id?: number) => rakenneCache.get(id || $stateParams.id);

        var oppiaineCache = rakenneCache.related((from: Lukio.LukioOpetussuunnitelmaRakenneOps) : {[key:number]: Lukio.LukioOppiaine} =>
            _(from.oppiaineet).flattenTree((oa: Lukio.LukioOppiaine) => oa.oppimaarat || [])
                .indexBy(_.property('id')).value());
        var getOppiaine = (id: number, opsId?: number) => oppiaineCache.get(opsId || $stateParams.id, id);
        var saveOppiaine = (oppiaine: Lukio.LukioOppiaineTallennus, opsId?: number) =>
            OpetusuunnitelmaLukio.saveOppiaine({opsId: opsId || $stateParams.id}, oppiaine)
                .$promise.then(r => { oppiaineCache.clear(); return r; }, Notifikaatiot.serverCb);

        var updateOppiaine = (oppiaine: Lukio.LukioOppiaineTallennus, opsId?: number) =>
            OpetusuunnitelmaLukio.updateOppiaine({opsId: opsId || $stateParams.id}, oppiaine)
                .$promise.then(r => { oppiaineCache.clear(); return r; }, Notifikaatiot.serverCb);

        var kurssiCache = oppiaineCache.related((from: Lukio.LukioOppiaine) : {[key:number]: Lukio.LukiokurssiOps} =>
            _(from.kurssit).indexBy(_.property('id')).value());
        var getKurssi = (oppiaineId: number, kurssiId: number, opsId?: number) =>
            kurssiCache.get(opsId || $stateParams.id, oppiaineId, kurssiId);

        var kloonaaOppiaineMuokattavaksi = (oppiaineId:number, opsId?:number) => OppiaineCRUD.kloonaaMuokattavaksi({
                opsId: opsId || $stateParams.id,
                oppiaineId: oppiaineId
            }, {}).$promise.then(res => { oppiaineCache.clear(); return res;}, Notifikaatiot.serverCb);

        var palautaYlempaan = (oppiaineId:number, opsId?:number) => OppiaineCRUD.palautaYlempaan({
            opsId: opsId || $stateParams.id,
            oppiaineId: oppiaineId
        }, {}).$promise.then(res => { oppiaineCache.clear(); return res;}, Notifikaatiot.serverCb);

        var updateOppiaineKurssiStructure = (treeRoot:LukioKurssiTreeNode,
                                             kommentti?: string, opsId?: number) => {
            var chain = _(treeRoot).flattenTree((node:LukioKurssiTreeNode) => {
                    var kurssiJarjestys = 1,
                        oppiaineJarjestys = 1;
                    return _(node.lapset).map((n:LukioKurssiTreeNode):LukioKurssiTreeNode => {
                        if (n.dtype == LukioKurssiTreeNodeType.kurssi) {
                            return {
                                id: n.id,
                                dtype: LukioKurssiTreeNodeType.kurssi,
                                oppiaineet: [{
                                    oppiaineId: node.id,
                                    jarjestys: kurssiJarjestys++
                                }]
                            };
                        } else {
                            return {
                                id: n.id,
                                dtype: LukioKurssiTreeNodeType.oppiaine,
                                jarjestys: oppiaineJarjestys++,
                                lapset: n.lapset
                            };
                        }
                    }).value();
                }),
                update = {
                    oppiaineet: chain.filter(n => n && n.dtype == LukioKurssiTreeNodeType.oppiaine).value(),
                    kurssit: chain.filter(n => n.dtype == LukioKurssiTreeNodeType.kurssi)
                        .reducedIndexOf(n => n.id, (a, b) => {
                            var c = _.clone(a);
                            c.oppiaineet = _.union(a.oppiaineet, b.oppiaineet);
                            return c;
                        }).values().value(),
                    kommentti: kommentti
                };
            return OpetusuunnitelmaLukio.updateStructure({
                    opsId: opsId || $stateParams.id}, update)
                .$promise.then((res) => {
                    rakenneCache.clear();
                    return res;
                }, Notifikaatiot.serverCb);
        };

        var addKielitarjonta = (oppiaineId:number, tarjonta:Lukio.OppiaineKielitarjonta, opsId?: number) =>
            OpetusuunnitelmaLukio.addKielitarjonta({oppiaineId: oppiaineId, opsId: opsId || $stateParams.id}, tarjonta)
                .$promise.then(r => { oppiaineCache.clear(); return r; }, Notifikaatiot.serverCb);

        var deleteOppiaine = (oppiaineId:number, opsId?: number) =>
            OppiaineCRUD.delete({oppiaineId: oppiaineId, opsId: opsId || $stateParams.id})
                .$promise.then(() => { oppiaineCache.clear(); }, Notifikaatiot.serverCb);

        var saveKurssi = (kurssi:Lukio.LuoLukiokurssi, opsId?: number) =>
            OpetusuunnitelmaLukio.saveKurssi({opsId: opsId || $stateParams.id}, kurssi)
                .$promise.then(r => { kurssiCache.clear(); return r; }, Notifikaatiot.serverCb);

        var updateKurssi = (kurssiId:number, kurssi:Lukio.UpdateLukiokurssi, opsId?: number) =>
            OpetusuunnitelmaLukio.updateKurssi({kurssiId: kurssiId, opsId: opsId || $stateParams.id}, kurssi)
                .$promise.then(r => { kurssiCache.clear(); return r; }, Notifikaatiot.serverCb);

        var disconnectKurssi = (kurssiI:number, opsI:number) =>
            OpetusuunnitelmaLukio.disconnectKurssi({kurssiId: kurssiI, opsId: opsI || $stateParams.id}, {})
                .$promise.then(r => { kurssiCache.clear(); return r; }, Notifikaatiot.serverCb);


        return <LukioOpetussuunnitelmaServiceI>{
            getAihekokonaisuudet: getAihekokonaisuudet,
            getOpetuksenYleisetTavoitteet: getOpetuksenYleisetTavoitteet,
            onAihekokonaisuudetUpdate: (then) => aiheKokCache.onUpdate(then),
            onRaknneUpdate: (then) => rakenneCache.onUpdate(then),
            getRakenne: getRakenne,
            getOppiaine: getOppiaine,
            getKurssi: getKurssi,
            saveOppiaine: saveOppiaine,
            updateOppiaine: updateOppiaine,
            kloonaaOppiaineMuokattavaksi: kloonaaOppiaineMuokattavaksi,
            palautaYlempaan: palautaYlempaan,
            updateOppiaineKurssiStructure: updateOppiaineKurssiStructure,
            addKielitarjonta: addKielitarjonta,
            deleteOppiaine: deleteOppiaine,
            saveKurssi: saveKurssi,
            updateKurssi: updateKurssi,
            disconnectKurssi: disconnectKurssi
        }
    });
