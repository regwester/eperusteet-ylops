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

ylopsApp
    .controller("VirheController", function($rootScope, $state, $scope, VirheService) {
        $scope.$watch(VirheService.getData, function(value) {
            $scope.data = value;
        });

        $scope.lisatiedot = $state.params.lisatiedot;
        $scope.lisatiedotCollapsed = true;
        var ls = $rootScope.lastState;

        // Tarkistetaan oliko edellinen tila jossain opintosuunnitelmassa.
        var opsname = "root.opetussuunnitelmat";
        if (ls.state.name.substr(0, opsname.length) === opsname) {
            $scope.opsId = _.get(ls, "params.id");
        }

        $scope.ylaTila = ls.state.name.replace(/(.[^.]*$)/, "");
        // Tarkistetaan onko ylempää tilaa olemassa ja että se ei ole abstrakti.
        var ylaTila = $state.get($scope.ylaTila);
        $scope.ylaTila = ylaTila && !ylaTila.abstract && $scope.ylaTila;

        // Välitetään mahdolliset id-parametrit sellaisenaan eteenpäin edellisestä tilasta.
        var params = {
            id: _.get(ls, "params.id"),
            pohjaId: _.get(ls, "params.pohjaId"),
            vlkId: _.get(ls, "params.vlkId")
        };
        $scope.palaaEdelliseen = function() {
            $state.go(ls.state.name, params);
        };
        $scope.palaaYlempaan = function() {
            $state.go($scope.ylaTila, params);
        };
    })
    .service("VirheService", function($state) {
        var data = {};

        this.setData = function(data) {
            data = data;
        };
        this.getData = function() {
            return data;
        };

        this.virhe = function(virhe, lisatiedot) {
            if (_.isObject(virhe)) {
                data = virhe;
            } else {
                data = { muu: virhe };
            }

            $state.go("root.virhe", { lisatiedot: lisatiedot });
        };
    });
