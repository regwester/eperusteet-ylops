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

package fi.vm.sade.eperusteet.ylops.service.mapping;

import java.util.Collection;
import java.util.List;

/**
 * @author jhyoty
 */
public interface DtoMapper {

    /**
     * Muuntaa lähdeobjektin kohdeluokaksi
     *
     * @param <S>              lähdeobjectin tyyppi
     * @param <D>              kohdeobjectin tyyppi
     * @param sourceObject     lähdeobjecti
     * @param destinationClass type token
     * @return uusi objekti tyyppiä S
     */
    <S, D> D map(S sourceObject, Class<D> destinationClass);

    /**
     * Muuntaa lähdeobjektin kohdeobjektiin
     *
     * @param <S>               lähdeobjectin tyyppi
     * @param <D>               kohdeobjectin tyyppi
     * @param sourceObject      lähdeobjecti
     * @param destinationObject kohdeobjekti
     */
    <S, D> D map(S sourceObject, D destinationObject);

    /**
     * muuntaa Iterablen listaksi jonka elementin tyyppi on D
     *
     * @param <S>              lähdekokoelman tyyppi
     * @param <D>              kohdelistan elementin tyyppi
     * @param source           lähdekokoelma
     * @param destinationClass kohdelistan elementin "type token"
     * @return uusi lista jonka elementit ovat tyyppiä D
     */
    <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass);

    /**
     * muutaa iterablen kohdekokoelmaan,
     *
     * @param <S>      lähdekokoelman tyyppi
     * @param <D>      kohdelistan elementin tyyppi
     * @param source   lähdekokoelma
     * @param dest     kohdekokoelma
     * @param elemType kohdelistan elementin "type token"
     */
    <S, D, T extends Collection<D>> T mapToCollection(Iterable<S> source, T dest, Class<D> elemType);

    /**
     * Palauttaa mapper-toteutuksen
     *
     * @param <M>
     * @param mapperClass
     * @return mapper implementation.
     */
    <M> M unwrap(Class<M> mapperClass);

}
