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

ylopsApp
    .directive('versiotiedot', (VersionHelper, $q) => {
        return {
            templateUrl: 'views/common/directives/versiotiedot.html',
            restrict: 'E',
            controller: ($scope, EperusteetKayttajatiedot) => {
              let reqs = [];
              _.forEach(_.uniq($scope.versiot.list, 'muokkaajaOid'), (i) => reqs.push(EperusteetKayttajatiedot.get({oid: i.muokkaajaOid}).$promise));

              $q.all(reqs).then((values) => {
                _.forEach($scope.versiot.list, (name) => {
                  const henkilo = _.find(values, (i) => i.oidHenkilo === name.muokkaajaOid);
                  const nimi = _.isEmpty(henkilo) ? ' ': (henkilo.kutsumanimi || '') + ' ' + (henkilo.sukunimi || '');
                  name.muokkaaja = nimi === ' ' ? name.muokkaajaOid : nimi;
                });
              });

              $scope.history = () => {
                  VersionHelper.historyView($scope.versiot);
              };
            }
        };
    });
