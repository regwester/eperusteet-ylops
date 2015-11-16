/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.ylops.service.util;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * User: tommiratamaa
 * Date: 16.11.2015
 * Time: 15.53
 */
public class ExceptionUtil {
    private ExceptionUtil() {
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T, Ex extends Exception> {
        T get() throws Ex;
    }

    @FunctionalInterface
    public interface ThrowingFunction<F, T, Ex extends Exception> {
        T apply(F f) throws Ex;
    }

    private static Function<Exception, IllegalStateException> DEFAULT_RUNTIME_EX = IllegalStateException::new;

    public static <F, T, Ex extends Exception> Function<F, T> wrapRuntime(ThrowingFunction<F, T, Ex> target)
            throws IllegalStateException {
        return wrapRuntime(target, DEFAULT_RUNTIME_EX);
    }

    public static <F, T, Ex extends Exception, RtEx extends RuntimeException> Function<F, T>
            wrapRuntime(ThrowingFunction<F, T, Ex> target, Function<? super Ex, ? extends RtEx> wrapper)
                throws RtEx {
        return (F f) -> {
            try {
                return target.apply(f);
            } catch (Exception e) {
                if (RuntimeException.class.isAssignableFrom(e.getClass())) {
                    throw (RuntimeException) e;
                }
                throw wrapper.apply((Ex) e);
            }
        };
    }

    public static <T, Ex extends Exception> Supplier<T> wrapRuntime(ThrowingSupplier<T, Ex> target)
            throws IllegalStateException {
        return wrapRuntime(target, DEFAULT_RUNTIME_EX);
    }

    public static <T,Ex extends Exception, RtEx extends RuntimeException> Supplier<T>
            wrapRuntime(ThrowingSupplier<T, Ex> target, Function<? super Ex, ? extends RtEx> wrapper)
                throws RtEx {
        return () -> {
            try {
                return target.get();
            } catch (Exception e) {
                if (RuntimeException.class.isAssignableFrom(e.getClass())) {
                    throw (RuntimeException) e;
                }
                throw wrapper.apply((Ex)e);
            }
        };
    }
}
