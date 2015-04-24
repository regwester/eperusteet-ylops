'use strict';

ylopsApp
  .directive('hallintalinkki',['OpetussuunnitelmaOikeudetService', '$window', function (OpetussuunnitelmaOikeudetService) {
    return {
      template: '<a class="header-links" ng-cloak ui-sref="root.admin" icon-role="settings" kaanna="hallinta"></a>',
      restrict: 'E',
      link: function postLink(scope, element) {

        element.hide();

        scope.$on('fetched:oikeusTiedot', function() {
          if (OpetussuunnitelmaOikeudetService.onkoOikeudet('opetussuunnitelma', 'hallinta', true) || OpetussuunnitelmaOikeudetService.onkoOikeudet('pohja', 'hallinta', true)) {
            element.show();
          }
        });
      }
    };
  }]);