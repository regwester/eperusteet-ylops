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

import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import java.util.Collection;

/**
 *
 * @author mikkom
 */
public class ValidHtmlCollectionValidator extends ValidHtmlValidatorBase implements
        ConstraintValidator<ValidHtml, Collection<LokalisoituTeksti>>{
    @Override
    public void initialize(ValidHtml constraintAnnotation) {
        setupValidator(constraintAnnotation);
    }

    @Override
    public boolean isValid(Collection<LokalisoituTeksti> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return value.stream().allMatch(this::isValid);
    }
}
