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
package fi.vm.sade.eperusteet.ylops.service.util;

import fi.vm.sade.eperusteet.ylops.service.exception.NotExistsException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

/**
 * Apumetodeja null-arvojen kanssa temppuiluun
 *
 * @author jhyoty
 */
public final class Nulls {

    private Nulls() {
        //Apuluokka
    }

    public static <T> Collection<T> nullToEmpty(Collection<T> s) {
        if (s == null) {
            return Collections.emptySet();
        }
        return s;
    }

    public static String nullToEmpty(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

    public static <T> T assertExists(T o, String msg) {
        if (o == null) {
            throw new NotExistsException(msg);
        }
        return o;
    }

    public static<F,T> Function<F,Optional<T>> ofNullable(Function<F,T> target) {
        return f -> {
            T value = target.apply(f);
            return Optional.ofNullable(value);
        };
    }
}
