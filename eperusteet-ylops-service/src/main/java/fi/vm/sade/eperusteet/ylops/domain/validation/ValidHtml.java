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
package fi.vm.sade.eperusteet.ylops.domain.validation;

import org.jsoup.safety.Whitelist;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author mikkom
 */
@Target({FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = {ValidHtmlValidator.class, ValidHtmlCollectionValidator.class})
@Documented
public @interface ValidHtml {

    String message() default "Teksti saa sisältää vain ennaltamääriteltyjä html-elementtejä";

    WhitelistType whitelist() default WhitelistType.NORMAL;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    enum WhitelistType {
        MINIMAL(Whitelist.none()),
        SIMPLIFIED(Whitelist.none().addTags("p", "strong", "em", "i", "s", "ol", "li", "ul")),
        NORMAL(Whitelist.none()
                .addTags(
                        "p", "span", "strong", "em", "i", "s", "blockquote", "pre", "a", "abbr", // tekstit
                        "ol", "ul", "li", // listat
                        "table", "caption", "tbody", "tr", "td", "hr", "th", "thead", // taulukot
                        "figure", "img", "figcaption" // kuvat
                )
                .addAttributes("abbr", "data-viite")
                .addAttributes("span", "class", "uid")
                .addAttributes("a", "href", "target")
                .addAttributes("table", "align", "border", "cellpadding", "cellspacing", "style", "summary")
                .addAttributes("th", "scope", "colspan", "rowspan", "style")
                .addAttributes("td", "colspan", "rowspan", "style", "style")
                .addAttributes("img", "data-uid", "alt", "style", "src")
                .addAttributes("figure", "class"));

        private Whitelist whitelist;

        private WhitelistType(Whitelist whitelist) {
            this.whitelist = whitelist;
        }

        public Whitelist getWhitelist() {
            return whitelist;
        }
    }
}
