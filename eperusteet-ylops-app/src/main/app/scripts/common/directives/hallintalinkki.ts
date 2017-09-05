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

ylopsApp.directive("hallintalinkki", [
    "OpetussuunnitelmaOikeudetService",
    "$window",
    function(OpetussuunnitelmaOikeudetService, $window) {
        return {
            template:
                '<a class="header-links" ng-cloak ui-sref="root.admin" icon-role="settings" kaanna="hallinta"></a>',
            restrict: "E",
            link: function postLink(scope, element) {
                element.hide();
                scope.$on("fetched:oikeusTiedot", function() {
                    if (
                        $window.location.host.indexOf("localhost") === 0 ||
                        OpetussuunnitelmaOikeudetService.onkoOikeudet("opetussuunnitelma", "hallinta", true) ||
                        OpetussuunnitelmaOikeudetService.onkoOikeudet("pohja", "hallinta", true)
                    ) {
                        element.show();
                    }
                });
            }
        };
    }
]);
