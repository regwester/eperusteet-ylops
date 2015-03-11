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

import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.Tyyppi;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.util.CollectionUtil;
import fi.vm.sade.eperusteet.ylops.service.util.SecurityUtil;
import org.jgroups.util.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.ylops.service.security.PermissionEvaluator.RolePermission;
import static fi.vm.sade.eperusteet.ylops.service.security.PermissionEvaluator.RolePrefix;
import static fi.vm.sade.eperusteet.ylops.service.security.PermissionEvaluator.Organization;


/**
 *
 * @author mikkom
 */
@Service
public class PermissionManager {

    public enum TargetType {

        POHJA("pohja"),
        OPETUSSUUNNITELMA("opetussuunnitelma");

        private final String target;

        private TargetType(String target) {
            this.target = target;
        }

        @Override
        public String toString() {
            return target;
        }
    }

    public enum Permission {

        LUKU("luku"),
        MUOKKAUS("muokkaus"),
        KOMMENTOINTI("kommentointi"),
        LUONTI("luonti"),
        POISTO("poisto");

        private final String permission;

        private Permission(String permission) {
            this.permission = permission;
        }

        @Override
        public String toString() {
            return permission;
        }
    }

    @Autowired
    private OpetussuunnitelmaRepository opetussuunnitelmaRepository;

    @Transactional(readOnly = true)
    public boolean hasPermission(Authentication authentication, Serializable targetId, TargetType target,
                                 Permission perm) {

        // Salli valmiiden pohjien lukeminen kaikilta joilla on CRUD-oikeus
        if (perm == Permission.LUKU && targetId != null &&
            hasRole(authentication, RolePrefix.ROLE_APP_EPERUSTEET_YLOPS, RolePermission.CRUD, Organization.ANY)) {
            Object[] tyyppiJaTila = (Object[])opetussuunnitelmaRepository.findTyyppiAndTila((long)targetId);
            Tyyppi tyyppi = (Tyyppi)tyyppiJaTila[0];
            Tila tila = (Tila)tyyppiJaTila[1];
            if (tyyppi == Tyyppi.POHJA && tila == Tila.VALMIS) {
                return true;
            }
        }

        Set<RolePermission> permissions;
        switch (perm) {
            case LUKU:
            case KOMMENTOINTI:
                permissions = EnumSet.allOf(RolePermission.class);
                break;
            case LUONTI:
            case POISTO:
                permissions = EnumSet.of(RolePermission.CRUD);
                break;
            case MUOKKAUS:
                permissions = EnumSet.of(RolePermission.CRUD, RolePermission.READ_UPDATE);
                break;
            default:
                permissions = EnumSet.noneOf(RolePermission.class);
                break;
        }

        switch (target) {
            case POHJA:
            case OPETUSSUUNNITELMA:
                if (targetId != null) {
                    List<String> opsOrganisaatiot = opetussuunnitelmaRepository.findOrganisaatiot((Long) targetId);
                    Set<String> kayttajaOrganisaatiot = SecurityUtil.getOrganizations(authentication, permissions);
                    return !CollectionUtil.intersect(opsOrganisaatiot, kayttajaOrganisaatiot).isEmpty();
                } else {
                    return hasAnyRole(authentication, RolePrefix.ROLE_APP_EPERUSTEET_YLOPS,
                                      permissions, Organization.ANY);
                }
            default:
                return hasAnyRole(authentication, RolePrefix.ROLE_APP_EPERUSTEET_YLOPS,
                                  permissions, Organization.ANY);
        }
    }

    private static boolean hasRole(Authentication authentication, RolePrefix prefix,
                                   RolePermission permission, Organization org) {
        return hasAnyRole(authentication, prefix, Collections.singleton(permission), org);
    }

    private static boolean hasAnyRole(Authentication authentication, RolePrefix prefix,
                                      Set<RolePermission> permission, Organization org) {
        return authentication.getAuthorities().stream()
                             .anyMatch(a -> permission.stream().anyMatch(p -> roleEquals(a.getAuthority(), prefix, p, org)));
    }

    private static boolean roleEquals(String authority, RolePrefix prefix,
                                      RolePermission permission, Organization org) {
        if (Organization.ANY.equals(org)) {
            return authority.equals(prefix.name() + "_" + permission.name());
        }
        return authority.equals(prefix.name() + "_" + permission.name() + "_" + org.getOrganization().get());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Map<TargetType, Set<Permission>> getOpsPermissions() {

        Map<TargetType, Set<Permission>> permissionMap = new HashMap<>();
        Set<Permission> permissions =
            EnumSet.allOf(RolePermission.class).stream()
                   .map(p -> new Tuple<>(p, SecurityUtil.getOrganizations(Collections.singleton(p))))
                   .flatMap(pair -> fromRolePermission(pair.getVal1()).stream())
                   .collect(Collectors.toSet());

        permissionMap.put(TargetType.OPETUSSUUNNITELMA, permissions);

        return permissionMap;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Map<TargetType, Set<Permission>> getOpsPermissions(Long id) {

        Map<TargetType, Set<Permission>> permissionMap = new HashMap<>();
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(id);
        if (ops == null) {
            throw new BusinessRuleViolationException("Opetussuunnitelmaa ei ole olemassa");
        }
        Set<String> organisaatiot = ops.getOrganisaatiot();
        Set<Permission> permissions =
            EnumSet.allOf(RolePermission.class).stream()
                   .map(p -> new Tuple<>(p, SecurityUtil.getOrganizations(Collections.singleton(p))))
                   .filter(pair -> !CollectionUtil.intersect(pair.getVal2(), organisaatiot).isEmpty())
                   .flatMap(pair -> fromRolePermission(pair.getVal1()).stream())
                   .collect(Collectors.toSet());

        permissionMap.put(TargetType.OPETUSSUUNNITELMA, permissions);

        return permissionMap;
    }

    private static Set<Permission> fromRolePermission(RolePermission rolePermission) {
        Set<Permission> permissions = new HashSet<>();
        switch (rolePermission) {
            case CRUD:
                permissions.add(Permission.LUONTI);
                permissions.add(Permission.POISTO);
            case READ_UPDATE:
                permissions.add(Permission.LUKU);
                permissions.add(Permission.MUOKKAUS);
            case READ:
                permissions.add(Permission.LUKU);
                permissions.add(Permission.KOMMENTOINTI);
                break;
        }
        return permissions;
    }
}
