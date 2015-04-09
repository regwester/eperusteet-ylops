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

import java.security.Principal;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static fi.vm.sade.eperusteet.ylops.service.security.PermissionEvaluator.RolePermission;
import static fi.vm.sade.eperusteet.ylops.service.security.PermissionEvaluator.RolePrefix;


/**
 *
 * @author jhyoty
 */
public final class SecurityUtil {

    public static final String OPH_OID = "1.2.246.562.10.00000000001";

    private SecurityUtil() {
        //helper class
    }

    public static Principal getAuthenticatedPrincipal() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static void allow(String principalName) {
        Principal p = getAuthenticatedPrincipal();
        if ( p == null || !p.getName().equals(principalName)) {
            throw new AccessDeniedException("Pääsy evätty");
        }
    }

    public static Set<String> getOrganizations(Set<RolePermission> permissions) {
        return getOrganizations(SecurityContextHolder.getContext().getAuthentication(), permissions);
    }

    public static Set<String> getOrganizations(Authentication authentication, Set<RolePermission> permissions) {
        return authentication.getAuthorities().stream()
                             .map(grantedAuthority -> parseOid(grantedAuthority.getAuthority(),
                                                               RolePrefix.ROLE_APP_EPERUSTEET_YLOPS,
                                                               permissions))
                             .filter(Optional::isPresent)
                             .map(Optional::get)
                             .collect(Collectors.toSet());
    }

    private static Optional<String> parseOid(String authority, RolePrefix prefix, Set<RolePermission> permissions) {
        return permissions.stream()
                          .map(p -> {
                              String authPrefix = prefix.name() + "_" + p.name() + "_";
                              return authority.startsWith(authPrefix) ?
                                     Optional.of(authority.substring(authPrefix.length())) : Optional.<String>empty();
                          })
                          .filter(Optional::isPresent)
                          .map(Optional::get)
                          .findAny();
    }
}
