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

/* global _ */
'use strict';

ylopsApp
    .service('VersionHelper', function($modal, $state, $stateParams) {

        //this.lastModified = function (data) {
        //    //console.log('last', data);
        //    //if (data && data.chosen) {
        //    //    var found = _.find(data.list, {numero: data.chosen.numero});
        //    //    if (found) {
        //    //        return found.pvm;
        //    //    }
        //    //}
        //};

        //this.getOpsTekstiVersions = function(id, versiot){
        //    console.log('get vers', id, versiot);
        //    getVersions(data, tunniste, 'perusteenosa', force, cb);
            //versiot = {test: 1};
        //};
        //
        //this.select = function (data, index) {
        //    var found = _.find(data.list, {index: parseInt(index, 10)});
        //    if (found) {
        //        data.chosen = found;
        //        data.latest = data.chosen.numero === latest(data.list).numero;
        //        return found.numero;
        //    }
        //};
        //
        //this.currentIndex = function (data) {
        //    if (data && data.chosen) {
        //        return data.chosen.index;
        //    }
        //};
        //
        //this.latestIndex = function (data) {
        //    var latestItem = latest(data.list);
        //    if (latestItem) {
        //        return latestItem.index;
        //    }
        //};
        //
        //this.getPerusteenosaVersions = function (data, tunniste, force, cb) {
        //    getVersions(data, tunniste, 'perusteenosa', force, cb);
        //};
        //
        //this.getTutkinnonOsaViiteVersions = function (data, tunniste, force, cb) {
        //    getVersions(data, tunniste, 'tutkinnonOsaViite', force, cb);
        //};
        //
        //this.getPerusteenOsaVersionsByViite = function (data, tunniste, force, cb) {
        //    getVersions(data, tunniste, 'perusteenOsaViite', force, cb);
        //};
        //
        //this.getRakenneVersions = function (data, tunniste, force, cb) {
        //    getVersions(data, tunniste, 'rakenne', force, cb);
        //};
        //
        //this.getLukioYleisetTavoitteetVersions = function (data, tunniste, force, cb) {
        //    getVersions(data, tunniste, 'lukioyleisettavoitteet', force, cb);
        //};
        //
        //this.getLukioAihekokonaisuudetVersions = function (data, tunniste, force, cb) {
        //    getVersions(data, tunniste, 'lukioaihekokonaisuudet', force, cb);
        //};
        //
        //this.getLukioAihekokonaisuusVersions = function (data, tunniste, force, cb) {
        //    getVersions(data, tunniste, 'lukioaihekokonaisuus', force, cb);
        //};
        //
        //this.getLukiokurssiVersions = function (data, tunniste, force, cb) {
        //    getVersions(data, tunniste, 'lukiokurssi', force, cb);
        //};
        //
        //this.getLukioOppiaineVersions = function(data, tunniste, force, cb) {
        //    getVersions(data, tunniste, 'lukiooppiaine', force, cb);
        //};
        //
        //this.getLukioRakenneVersions = function(data, tunniste, force, cb) {
        //    getVersions(data, tunniste, 'lukiorakenne', force, cb);
        //};
        //
        //this.chooseLatest = function (data) {
        //    data.chosen = latest(data.list);
        //};
        //
        //this.changePerusteenosa = function(data, tunniste, cb) {
        //    change(data, tunniste, 'Perusteenosa', cb);
        //};
        //
        //this.changeRakenne = function(data, tunniste, cb) {
        //    change(data, tunniste, 'Rakenne', cb);
        //};
        //
        //this.revertTutkinnonOsaViite = function (data, object, cb) {
        //    revert(data, {id: object.id}, 'TutkinnonOsaViite', cb);
        //};
        //
        //this.revertPerusteenosa = function (data, object, cb) {
        //    var isTekstikappale = _.has(object, 'nimi') && _.has(object, 'teksti');
        //    var type = isTekstikappale ? 'Perusteenosa' : 'Tutkinnonosa';
        //    revert(data, {id: object.id}, type, cb);
        //};
        //
        //this.revertRakenne = function (data, tunniste, cb) {
        //    revert(data, tunniste, 'Rakenne', cb);
        //};
        //
        //this.revertLukioYleisetTavoitteet = function (data, tunniste, cb) {
        //    revert(data, tunniste, 'lukioyleisettavoitteet', cb);
        //};
        //
        //this.revertLukioAihekokonaisuudetYleiskuvaus = function (data, tunniste, cb) {
        //    revert(data, tunniste, 'lukioaihekokonaosuudetyleiskuvaus', cb);
        //};
        //
        //this.revertLukioAihekokonaisuus = function (data, tunniste, cb) {
        //    revert(data, tunniste, 'lukioaihekokonaisuus', cb);
        //};
        //
        //this.revertLukiokurssi = function (data, tunniste, cb) {
        //    revert(data, tunniste, 'lukiokurssi', cb);
        //};
        //
        //this.revertLukioOppiaine = function (data, tunniste, cb) {
        //    revert(data, tunniste, 'lukiooppiaine', cb);
        //};
        //
        //this.revertLukioRakenne = function(data, tunniste, cb) {
        //    revert(data, tunniste, 'lukiorakenne', cb);
        //};
        //
        //this.setUrl = function (data) {
        //    // Tricks for ui-router 0.2.*
        //    // We want to update the url only when user changes the version.
        //    // If we enter with versionless url don't rewrite it.
        //    // This function will currently navigate to a new state if version has changed.
        //    if (_.isEmpty(data)) {
        //        return;
        //    }
        //
        //    data.latest = data.chosen.index === latest(data.list).index;
        //    var versionlessUrl = $state.href($state.current.name, {versio: null}, {inherit:true}).replace(/#/g, '');
        //    var currentVersion = this.currentIndex(data);
        //    var isValid = _.isNumber(currentVersion);
        //    var urlHasVersion = $location.url() !== versionlessUrl;
        //    if ((urlHasVersion || data.hasChanged) && isValid && !data.latest) {
        //        data.hasChanged = false;
        //        var versionUrl = $state.href($state.current.name, {versio: '/' + currentVersion}, {inherit:true}).replace(/#/g, '').replace(/%252F/, '/');
        //        $location.url(versionUrl);
        //    } else {
        //        $location.url(versionlessUrl);
        //    }
        //};

        this.historyView = (data) => {
            console.log("history");
            $modal.open({

                    templateUrl: 'views/common/modals/versiohelper.html',
                    //templateUrl: 'views/partials/muokkaus/versiohelper.html',
                    //template: "<p>hello</p>",
                    controller: 'HistoryViewCtrl',
                    resolve: {
                        versions: function () {
                            return data;
                        }
                    }
                })
                .result.then(function (re) {
                console.log(re);
                var params = _.clone($stateParams);
                params.versio = '/' + re.numero;
                $state.go($state.current.name, params);
            });
        };

    })

    .controller('HistoryViewCtrl', function ($scope, versions, $modalInstance) {
        $scope.versions = versions;
        $scope.close = function(versio) {
            if (versio) {
                $modalInstance.close(versio);
            }
            else {
                $modalInstance.dismiss();
            }
        };
        $scope.paginate = {
            current: 1,
            perPage: 10
        };
    });
