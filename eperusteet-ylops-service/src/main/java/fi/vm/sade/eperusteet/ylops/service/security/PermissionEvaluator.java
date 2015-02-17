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
package fi.vm.sade.eperusteet.ylops.service.security;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

/**
 * Oikeuksien tarkistelu.
 *
 * @author jhyoty
 */
public class PermissionEvaluator implements org.springframework.security.access.PermissionEvaluator {

    static class Organization {

        private final String organization;

        private Organization() {
            this.organization = null;
        }

        Organization(String organization) {
            this.organization = organization;
        }

        public Optional<String> getOrganization() {
            return Optional.ofNullable(organization);
        }

        private static final Organization OPH = new Organization("1.2.246.562.10.00000000001");
        private static final Organization ANY = new Organization();

    }

    enum RolePrefix {

        ROLE_APP_EPERUSTEET_YLOPS,
        ROLE_VIRKAILIJA
    }

    enum RolePermission {

        CRUD,
        READ_UPDATE,
        READ
    }

    enum TargetType {

        POHJA,
        OPETUSSUUNNITELMA
    }

    enum Permission {

        LUKU,
        MUOKKAUS,
        LUONTI,
        POISTO
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        throw new UnsupportedOperationException("EI TOTEUTETTU");
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        //TODO. toteutus on kesken

        if (authentication == null || authentication.getAuthorities() == null) {
            LOG.error("Virheellinen autentikaatioparametri");
            return false;
        }

        TargetType target = TargetType.valueOf(targetType.toUpperCase());
        Permission perm = Permission.valueOf(permission.toString().toUpperCase());

        return hasPermission(authentication, targetId, target, perm);

    }

    private boolean hasPermission(Authentication authentication, Serializable targetId, TargetType target, Permission perm) {

        switch (target) {

            case POHJA:
                switch (perm) {
                    case LUKU:
                        return hasAnyRole(authentication, RolePrefix.ROLE_APP_EPERUSTEET_YLOPS, EnumSet.allOf(RolePermission.class), Organization.OPH) ||
                            hasRole(authentication, RolePrefix.ROLE_APP_EPERUSTEET_YLOPS, RolePermission.CRUD, Organization.ANY);

                    case LUONTI:
                    case POISTO:
                        return hasRole(authentication, RolePrefix.ROLE_APP_EPERUSTEET_YLOPS, RolePermission.CRUD, Organization.OPH);

                    case MUOKKAUS:
                        return hasAnyRole(authentication, RolePrefix.ROLE_APP_EPERUSTEET_YLOPS, EnumSet.of(RolePermission.READ_UPDATE, RolePermission.CRUD), Organization.OPH);

                    default:
                        return false;
                }

            case OPETUSSUUNNITELMA:
                return hasAnyRole(authentication, RolePrefix.ROLE_APP_EPERUSTEET_YLOPS, EnumSet.allOf(RolePermission.class), Organization.ANY);

            default:
                return false;
        }
    }

    private boolean hasRole(Authentication authentication, RolePrefix prefix, RolePermission permission, Organization org) {
        return authentication.getAuthorities().stream()
            .anyMatch(a -> roleEquals(a.getAuthority(), prefix, permission, org));
    }

    private boolean hasAnyRole(Authentication authentication, RolePrefix prefix, Set<RolePermission> permission, Organization org) {
        return authentication.getAuthorities().stream()
            .anyMatch(a -> permission.stream().anyMatch(p -> roleEquals(a.getAuthority(), prefix, p, org)));
    }

    private boolean roleEquals(String authority, RolePrefix prefix, RolePermission permission, Organization org) {
        if (Organization.ANY.equals(org)) {
            return authority.equals(prefix.name() + "_" + permission.name());
        }
        return authority.equals(prefix.name() + "_" + permission.name() + "_" + org.getOrganization().get());
    }

    private static final Logger LOG = LoggerFactory.getLogger(PermissionEvaluator.class);
}
