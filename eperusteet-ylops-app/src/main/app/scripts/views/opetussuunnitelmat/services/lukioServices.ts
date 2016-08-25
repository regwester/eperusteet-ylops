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
    getAihekokonaisuudet(opsId?: number): IPromise<Lukio.AihekokonaisuudetPerusteenOsa>
    lukitseAihekokonaisuudet(id:number, opsId?:number): IPromise<void>
    vapautaAihekokonaisuudet(id:number, opsId?:number): IPromise<void>
    updateAihekokonaisuudetYleiskuvaus(yleiskuvaus: Lukio.AihekokonaisuudetPaivitaYleiskuvaus, opsId?:number): IPromise<void>
    rearrangeAihekokonaisuudet(jarjestys: Lukio.JarjestaAihekokonaisuudet, opsId?:number): IPromise<void>

    getAihekokonaisuus(id: number, opsId?: number): IPromise<Lukio.OpsAihekokonaisuus>
    lukitseAihekokonaisuus(id:number, opsId?:number): IPromise<void>
    vapautaAihekokonaisuus(id:number, opsId?:number): IPromise<void>
    saveAihekokonaisuus(aihekok: Lukio.LuoAihekokonaisuus, opsId?:number): IPromise<IdHolder>
    updateAihekokonaisuus(id: number, aihekok: Lukio.PaivitaAihekokonaisuus, opsId?:number): IPromise<void>
    deleteAihekokonaisuus(aihekokId: number, opsId?:number): IPromise<void>

    getOpetuksenYleisetTavoitteet(opsId?: number) : IPromise<Lukio.OpetuksenYleisetTavoitteetPerusteenOsa>
    lukitseYleisetTavoitteet(opsId?:number): IPromise<void>
    vapautaYleisetTavoitteet(opsId?:number): IPromise<void>
    updateYleisetTavoitteet(tavoitteet: Lukio.OpetuksenYleisetTavoitteetUpdate, opsId?: any): IPromise<void>
    
    onAihekokonaisuudetUpdate(then: () => void ) : void
    onRakenneUpdate(then: () => void ) : void

    getRakenne(id?: number): IPromise<Lukio.LukioOpetussuunnitelmaRakenneOps>
    lukitseRakenne(opsId?:number): IPromise<void>
    vapautaRakenne(opsId?:number): IPromise<void>
    updateOppiaineKurssiStructure(treeRoot:LukioKurssiTreeNode, kommentti?: string, opsId?: number): IPromise<void>,

    getOppiaine(id: number, opsId?: number): IPromise<Lukio.LukioOppiaine>
    lukitseOppiaine(id:number, opsId?:number): IPromise<void>
    vapautaOppiaine(id:number, opsId?:number): IPromise<void>
    saveOppiaine(oppiaine: Lukio.LukioOppiaineTallennus, opsId?: number): IPromise<IdHolder>
    updateOppiaine(oppiaine: Lukio.LukioOppiaineTallennus, opsId?: number): IPromise<void>
    kloonaaOppiaineMuokattavaksi(oppiaineId:number, opsId?:number): IPromise<IdHolder>
    palautaYlempaan(oppiaineId:number, opsId?:number): IPromise<IdHolder>,
    addKielitarjonta(oppiaineId:number, tarjonta:Lukio.OppiaineKielitarjonta, opsId?: number): IPromise<Lukio.IdHolder>
    addAbstraktiOppiaine(tarjonta:Lukio.AbstraktiOppiaine, opsId?: number): IPromise<Lukio.IdHolder>
    deleteOppiaine(oppiaineId:number, opsId?: number): IPromise<void>

    getKurssi(oppiaineId: number, kurssiId: number, opsId?: number): IPromise<Lukio.LukiokurssiOps>
    lukitseKurssi(opsId?:number): IPromise<void>
    vapautaKurssi(opsId?:number): IPromise<void>
    saveKurssi(kurssi:Lukio.LuoLukiokurssi, opsId?: number): IPromise<IdHolder>
    updateKurssi(kurssiId:number, kurssi:Lukio.UpdateLukiokurssi, opsId?: number): IPromise<void>
    disconnectKurssi(kurssiId:number, oppiaineId: number, opsId:number): IPromise<IdHolder>
    reconnectKurssi(kurssiId:number, oppiaineId: number, opsId:number): IPromise<IdHolder>
    removeKurssi(kurssiId:number, opsId:number): IPromise<void>
}

ylopsApp
    .service('LukioOpetussuunnitelmaService', function(OpetusuunnitelmaLukio, $q:IQService, $log, Lukko,
                                                       Notifikaatiot, $stateParams, OppiaineCRUD) {
        const doGetAihekokonaisuudet =
            (opsId: any, d: IDeferred<Lukio.AihekokonaisuudetPerusteenOsa>) =>
                OpetusuunnitelmaLukio.aihekokonaisuudet({opsId: opsId || $stateParams.id})
                    .$promise.then((aihekok: Lukio.AihekokonaisuudetPerusteenOsa) => d.resolve(aihekok),
                            Notifikaatiot.serverCb);
        const aiheKokCache = cached<Lukio.AihekokonaisuudetPerusteenOsa>($q, doGetAihekokonaisuudet);
        const getAihekokonaisuudet = (opsId?: number) => aiheKokCache.get(opsId || $stateParams.id);
        const lukitseAihekokonaisuudet = (id:number, opsId?:number) => Lukko.lockLukio({
            opsId: opsId || $stateParams.id, id: id, lukittavaOsa: 'AIHEKOKONAISUUDET'});
        const vapautaAihekokonaisuudet = (id:number, opsId?:number) => Lukko.unlockLukio({
            opsId: opsId || $stateParams.id, id: id, lukittavaOsa: 'AIHEKOKONAISUUDET'});
        const rearrangeAihekokonaisuudet = (jarjestys: Lukio.JarjestaAihekokonaisuudet, opsId?:number) =>
            OpetusuunnitelmaLukio.rearrangeAihekokonaisuudet({opsId: opsId || $stateParams.id}, jarjestys)
                .$promise.then(r => { aiheKokCache.clear(); return r; }, Notifikaatiot.serverCb);
        const updateAihekokonaisuudetYleiskuvaus = (yleiskuvaus: Lukio.AihekokonaisuudetPaivitaYleiskuvaus, opsId?:number) =>
            OpetusuunnitelmaLukio.updateAihekokonaisuudetYleiskuvaus({opsId: opsId || $stateParams.id}, yleiskuvaus)
                .$promise.then(r => { aiheKokCache.clear(); return r; }, Notifikaatiot.serverCb);

        const kokonaisuusCache = aiheKokCache.related((from: Lukio.AihekokonaisuudetPerusteenOsa) : {[key:number]: Lukio.OpsAihekokonaisuus} =>
            _(from.paikallinen.aihekokonaisuudet).indexBy(_.property('id')).value());
        const getAihekokonaisuus = (id: number, opsId?: number) => kokonaisuusCache.get(opsId || $stateParams.id, id);
        const lukitseAihekokonaisuus = (id:number, opsId?:number) => Lukko.lockLukio({
            opsId: opsId || $stateParams.id, id: id, lukittavaOsa: 'AIHEKOKONAISUUS'});
        const vapautaAihekokonaisuus = (id:number, opsId?:number) => Lukko.unlockLukio({
            opsId: opsId || $stateParams.id, id: id, lukittavaOsa: 'AIHEKOKONAISUUS'});
        const saveAihekokonaisuus = (aihekok: Lukio.LuoAihekokonaisuus, opsId?:number) =>
            OpetusuunnitelmaLukio.saveAihekokonaisuus({opsId: opsId || $stateParams.id}, aihekok)
                .$promise.then(r => { kokonaisuusCache.clear(); return r; }, Notifikaatiot.serverCb);
        const updateAihekokonaisuus = (id: number, aihekok:Lukio.PaivitaAihekokonaisuus, opsId?:number) =>
            OpetusuunnitelmaLukio.updateAihekokonaisuus({opsId: opsId || $stateParams.id, aihekokonaisuusId: id}, aihekok)
                .$promise.then(r => { aiheKokCache.clear(); return r; }, Notifikaatiot.serverCb);
        const deleteAihekokonaisuus = (aihekokId:number, opsId?:number) =>
            OpetusuunnitelmaLukio.deleteAihekokonaisuus({opsId: opsId || $stateParams.id, aihekokonaisuusId: aihekokId})
                .$promise.then(r => { aiheKokCache.clear(); return r; }, Notifikaatiot.serverCb);

        const doGetOpetuksenYleisetTavoitteet =
            (opsId: any, d: IDeferred<Lukio.OpetuksenYleisetTavoitteetPerusteenOsa>) =>
                OpetusuunnitelmaLukio.opetuksenYleisetTavoitteet({opsId: opsId || $stateParams.id})
                    .$promise.then((yleisetTavoitteet: Lukio.OpetuksenYleisetTavoitteetPerusteenOsa) =>
                        d.resolve(yleisetTavoitteet), Notifikaatiot.serverCb);
        const yleisetTavoitteetCache = cached<Lukio.OpetuksenYleisetTavoitteetPerusteenOsa>($q,
            doGetOpetuksenYleisetTavoitteet);
        const getOpetuksenYleisetTavoitteet = (opsId?: number) => yleisetTavoitteetCache.get(opsId || $stateParams.id);
        const lukitseYleisetTavoitteet = (opsId?: any): IPromise<void> => Lukko.lockLukio({
            opsId: opsId || $stateParams.id, id: opsId || $stateParams.id, lukittavaOsa: 'YLEISET_TAVOITTEET'});
        const vapautaYleisetTavoitteet = (opsId?: any): IPromise<void> => Lukko.unlockLukio({
            opsId: opsId || $stateParams.id, id: opsId || $stateParams.id, lukittavaOsa: 'YLEISET_TAVOITTEET'});
        const updateYleisetTavoitteet = (tavoitteet: Lukio.OpetuksenYleisetTavoitteetUpdate, opsId?: any): IPromise<void> =>
            OpetusuunnitelmaLukio.updateOpetuksenYleisetTavoitteet({opsId: opsId || $stateParams.id}, tavoitteet)
                .$promise.then(r => { yleisetTavoitteetCache.clear(); return r; });

        const doGetRakenne = (id: number, d: IDeferred<Lukio.LukioOpetussuunnitelmaRakenneOps>) =>
            OpetusuunnitelmaLukio.rakenne({opsId: id})
                .$promise.then((rakenne: Lukio.LukioOpetussuunnitelmaRakenneOps) => d.resolve(rakenne), Notifikaatiot.serverCb);
        const rakenneCache = cached<Lukio.LukioOpetussuunnitelmaRakenneOps>($q, doGetRakenne);
        const lukitseRakenne = (opsId?:number) => Lukko.lockLukio({
            opsId: opsId || $stateParams.id, id: opsId || $stateParams.id, lukittavaOsa: 'OPS'});
        const vapautaRakenne = (opsId?:number) => Lukko.unlockLukio({
            opsId: opsId || $stateParams.id, id: opsId || $stateParams.id, lukittavaOsa: 'OPS'});
        const getRakenne = (id?: number) => rakenneCache.get(id || $stateParams.id);
        const updateOppiaineKurssiStructure = (treeRoot:LukioKurssiTreeNode,
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

        const combineWithOpsId = (opsId: number, id: number) => opsId ?  opsId + "/" + id : id;
        const doGetOppiaine = (oppiaineWithOpsId: number|string, d: IDeferred<Lukio.LukioOppiaine>) => {
            var prts = (""+oppiaineWithOpsId).split('/');
            var opsId = $stateParams.id,
                oppiaineId = null;
            if (prts.length > 1) {
                opsId = prts[0];
                oppiaineId = prts[1];
            } else {
                oppiaineId = prts[0];
            }
            return OpetusuunnitelmaLukio.oppiaine({opsId: opsId, oppiaineId: oppiaineId})
                .$promise.then((oa: Lukio.LukioOppiaine) => d.resolve(oa),
                    Notifikaatiot.serverCb);
        };
        const oppiaineCache = cached<Lukio.LukioOppiaine>($q, doGetOppiaine).alsoClear(rakenneCache);
        const getOppiaine = (id: number, opsId?: number) => oppiaineCache.get(combineWithOpsId(opsId, id));
        const lukitseOppiaine = (id:number, opsId?:number) => Lukko.lockLukio({
            opsId: opsId || $stateParams.id, id: id, lukittavaOsa: 'OPPIAINE'});
        const vapautaOppiaine = (id:number, opsId?:number) => Lukko.unlockLukio({
            opsId: opsId || $stateParams.id, id: id, lukittavaOsa: 'OPPIAINE'});
        const saveOppiaine = (oppiaine: Lukio.LukioOppiaineTallennus, opsId?: number) =>
            OpetusuunnitelmaLukio.saveOppiaine({opsId: opsId || $stateParams.id}, oppiaine)
                .$promise.then(r => { oppiaineCache.clear(); return r; }, Notifikaatiot.serverCb);
        const kloonaaOppiaineMuokattavaksi = (oppiaineId:number, opsId?:number) => OppiaineCRUD.kloonaaMuokattavaksi({
            opsId: opsId || $stateParams.id,
            oppiaineId: oppiaineId
        }, {}).$promise.then(res => { oppiaineCache.clear(); return res;}, Notifikaatiot.serverCb);
        const palautaYlempaan = (oppiaineId:number, opsId?:number) => OppiaineCRUD.palautaYlempaan({
            opsId: opsId || $stateParams.id,
            oppiaineId: oppiaineId
        }, {}).$promise.then(res => { oppiaineCache.clear(); return res;}, Notifikaatiot.serverCb);
        const updateOppiaine = (oppiaine: Lukio.LukioOppiaineTallennus, opsId?: number) =>
            OpetusuunnitelmaLukio.updateOppiaine({opsId: opsId || $stateParams.id}, oppiaine)
                .$promise.then(r => { oppiaineCache.clear(); return r; }, Notifikaatiot.serverCb);
        const addKielitarjonta = (oppiaineId:number, tarjonta:Lukio.OppiaineKielitarjonta, opsId?: number) =>
            OpetusuunnitelmaLukio.addKielitarjonta({oppiaineId: oppiaineId, opsId: opsId || $stateParams.id}, tarjonta)
                .$promise.then(r => { oppiaineCache.clear(); return r; }, Notifikaatiot.serverCb);
        const addAbstraktiOppiaine = (tarjonta:Lukio.AbstraktiOppiaine, opsId?: number) =>
            OpetusuunnitelmaLukio.addAbstraktiOppiaine({opsId: opsId || $stateParams.id}, tarjonta)
                .$promise.then(r => { oppiaineCache.clear(); return r; }, Notifikaatiot.serverCb);
        const deleteOppiaine = (oppiaineId:number, opsId?: number) =>
            OppiaineCRUD.delete({oppiaineId: oppiaineId, opsId: opsId || $stateParams.id})
                .$promise.then(() => { oppiaineCache.clear(); }, Notifikaatiot.serverCb);

        const kurssiCache = oppiaineCache.related((from: Lukio.LukioOppiaine) : {[key:number]: Lukio.LukiokurssiOps} =>
            _(from.kurssit).indexBy(_.property('id')).value());
        const getKurssi = (oppiaineId: number, kurssiId: number, opsId?: number) =>
            kurssiCache.get(combineWithOpsId(opsId, oppiaineId), kurssiId);
        const lukitseKurssi = (id:number, opsId?:number) => Lukko.lockLukio({
            opsId: opsId || $stateParams.id, id: id, lukittavaOsa: 'LUKIOKURSSI'});
        const vapautaKurssi = (id:number, opsId?:number) => Lukko.unlockLukio({
            opsId: opsId || $stateParams.id, id: id, lukittavaOsa: 'LUKIOKURSSI'});
        const saveKurssi = (kurssi:Lukio.LuoLukiokurssi, opsId?: number) =>
            OpetusuunnitelmaLukio.saveKurssi({opsId: opsId || $stateParams.id}, kurssi)
                .$promise.then(r => { kurssiCache.clear(); return r; }, Notifikaatiot.serverCb);
        const updateKurssi = (kurssiId:number, kurssi:Lukio.UpdateLukiokurssi, opsId?: number) =>
            OpetusuunnitelmaLukio.updateKurssi({kurssiId: kurssiId, opsId: opsId || $stateParams.id}, kurssi)
                .$promise.then(r => { kurssiCache.clear(); return r; }, Notifikaatiot.serverCb);
        const disconnectKurssi = (kurssiId:number, oppiaineId: number, opsId:number) =>
            OpetusuunnitelmaLukio.disconnectKurssi({ kurssiId: kurssiId, opsId: opsId || $stateParams.id, oppiaineId: $stateParams.oppiaineId }, {})
                .$promise.then(r => { kurssiCache.clear(); return r; }, Notifikaatiot.serverCb);
        const reconnectKurssi = (kurssiId:number, oppiaineId: number, opsId:number) =>
            OpetusuunnitelmaLukio.reconnectKurssi({ kurssiId: kurssiId, opsId: opsId || $stateParams.id, oppiaineId: $stateParams.oppiaineId }, {})
                .$promise.then(r => { kurssiCache.clear(); return r; }, Notifikaatiot.serverCb);

        const removeKurssi = (kurssiId:number, opsId:number) =>
            OpetusuunnitelmaLukio.removeKurssi({kurssiId: kurssiId, opsId: opsId || $stateParams.id}, {})
                .$promise.then(r => { kurssiCache.clear(); return r; }, Notifikaatiot.serverCb);

        return <LukioOpetussuunnitelmaServiceI>{
            getAihekokonaisuudet: getAihekokonaisuudet,
            lukitseAihekokonaisuudet: lukitseAihekokonaisuudet,
            vapautaAihekokonaisuudet: vapautaAihekokonaisuudet,
            rearrangeAihekokonaisuudet: rearrangeAihekokonaisuudet,
            updateAihekokonaisuudetYleiskuvaus: updateAihekokonaisuudetYleiskuvaus,

            getAihekokonaisuus: getAihekokonaisuus,
            lukitseAihekokonaisuus: lukitseAihekokonaisuus,
            vapautaAihekokonaisuus: vapautaAihekokonaisuus,
            saveAihekokonaisuus: saveAihekokonaisuus,
            updateAihekokonaisuus: updateAihekokonaisuus,
            deleteAihekokonaisuus: deleteAihekokonaisuus,

            getOpetuksenYleisetTavoitteet: getOpetuksenYleisetTavoitteet,
            lukitseYleisetTavoitteet: lukitseYleisetTavoitteet,
            vapautaYleisetTavoitteet: vapautaYleisetTavoitteet,
            updateYleisetTavoitteet: updateYleisetTavoitteet,
            
            onAihekokonaisuudetUpdate: (then) => aiheKokCache.onUpdate(then),
            onRakenneUpdate: (then) => rakenneCache.onUpdate(then),

            getRakenne: getRakenne,
            lukitseRakenne: lukitseRakenne,
            vapautaRakenne: vapautaRakenne,
            updateOppiaineKurssiStructure: updateOppiaineKurssiStructure,

            getOppiaine: getOppiaine,
            lukitseOppiaine: lukitseOppiaine,
            vapautaOppiaine: vapautaOppiaine,
            saveOppiaine: saveOppiaine,
            updateOppiaine: updateOppiaine,
            kloonaaOppiaineMuokattavaksi: kloonaaOppiaineMuokattavaksi,
            palautaYlempaan: palautaYlempaan,
            addKielitarjonta: addKielitarjonta,
            addAbstraktiOppiaine: addAbstraktiOppiaine,
            deleteOppiaine: deleteOppiaine,

            getKurssi: getKurssi,
            lukitseKurssi: lukitseKurssi,
            vapautaKurssi: vapautaKurssi,
            saveKurssi: saveKurssi,
            updateKurssi: updateKurssi,
            reconnectKurssi: reconnectKurssi,
            disconnectKurssi: disconnectKurssi,
            removeKurssi: removeKurssi
        }
    });
