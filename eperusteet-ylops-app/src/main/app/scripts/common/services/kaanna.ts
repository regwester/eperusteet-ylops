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
    .constant("DEBUG_KAANNA", false)
    .service("Kaanna", function($translate, Kieli, DEBUG_KAANNA) {
        function translate(obj, key, useFallback) {
            function getTranslation(input, lang) {
                return input[lang] || input[lang.toUpperCase()] || input["kieli_" + lang + "#1"];
            }
            var primary = getTranslation(obj, key);
            if (primary) {
                return primary;
            }
            var secondaryKey = key === "fi" || key === "FI" ? "sv" : "fi";
            var secondary = getTranslation(obj, secondaryKey);

            if (secondary) {
                return useFallback ? secondary : "[" + secondary + "]";
            } else if (useFallback) {
                return _(obj)
                    .values()
                    .first();
            }
            return secondary;
        }

        function kaannaSisalto(input, useFallback) {
            if (_.isEmpty(input)) {
                return "";
            }
            var sisaltokieli = Kieli.getSisaltokieli();
            return translate(input, sisaltokieli, useFallback);
        }

        return {
            kaanna: function(input, config, useFallback) {
                let result = "";
                if (_.isObject(input)) {
                    result = kaannaSisalto(input, useFallback);
                } else if (_.isString(input)) {
                    result = $translate.instant(input, config);
                    if (DEBUG_KAANNA) {
                        if (!_.isEmpty(input) && _.eq(result, input)) {
                            console.info("Käännös puuttuu:", input);
                        }
                    }
                }
                return result || "";
            },
            kaannaSisalto: kaannaSisalto
        };
    })
    .directive("kaanna", function(Kaanna, $compile, IconMapping) {
        function resolvePostfix(attrs) {
            var postfix = attrs.kaannaPostfix || "";
            if (postfix) {
                postfix = " " + postfix;
            }
            if (!postfix && attrs.vaaditaan !== undefined) {
                postfix = " *";
            }
            return postfix;
        }
        function getAttr(attr: any, scope: any) {
            if (!_.isString(attr) || _.size(attr) === 0) {
                return;
            }
            return scope.$eval(attr) || attr;
        }
        return {
            restrict: "A",
            link: function(scope: any, el: any, attrs: any) {
                function kaannaValue(value) {
                    return _.isObject(value) ? Kaanna.kaannaSisalto(value) : Kaanna.kaanna(value);
                }
                var original = getAttr(attrs.kaanna, scope) || el.text();
                var postfix = resolvePostfix(attrs);
                if (_.isObject(original)) {
                    el.text(Kaanna.kaannaSisalto(original));
                    if (attrs.iconRole) {
                        IconMapping.addIcon(attrs.iconRole, el);
                    }
                    scope.$watch(
                        function() {
                            return getAttr(attrs.kaanna, scope);
                        },
                        function(value) {
                            el.text(kaannaValue(value));
                        }
                    );
                    scope.$on("changed:sisaltokieli", function() {
                        el.text(kaannaValue(getAttr(attrs.kaanna, scope)));
                    });
                } else {
                    var textEl = angular.element("<span>").attr("translate", original);
                    if (attrs.kaannaValues) {
                        textEl.attr("translate-values", attrs.kaannaValues);
                    }
                    el
                        .html("")
                        .append(textEl)
                        .append(postfix);
                    if (attrs.iconRole) {
                        var iconEl = angular.element("<span>").attr("icon-role", attrs.iconRole);
                        el.removeAttr("icon-role");
                        el.prepend(iconEl);
                    }
                    el.removeAttr("kaanna");
                    el.removeAttr("kaanna-values");
                    $compile(el.contents())(scope);
                }
            }
        };
    })
    .filter("kaanna", function(Kaanna) {
        return Kaanna.kaanna;
    });
