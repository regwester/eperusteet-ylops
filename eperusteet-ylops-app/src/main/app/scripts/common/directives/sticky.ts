namespace Sticky {
    export const directive = () => {
        return {
            restrict: "A",
            link: (scope, element, attrs) => {
                $(element).sticky({
                    topSpacing: attrs.topSpacing || 0,
                    className: attrs.classname
                });
                console.log(element);
            }
        };
    };
}

ylopsApp.directive("sticky", Sticky.directive);
