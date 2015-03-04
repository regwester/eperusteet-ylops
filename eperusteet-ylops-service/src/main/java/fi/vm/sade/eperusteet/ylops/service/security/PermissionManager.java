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

import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.service.util.CollectionUtil;
import fi.vm.sade.eperusteet.ylops.service.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static fi.vm.sade.eperusteet.ylops.service.security.PermissionEvaluator.TargetType;
import static fi.vm.sade.eperusteet.ylops.service.security.PermissionEvaluator.Permission;
import static fi.vm.sade.eperusteet.ylops.service.security.PermissionEvaluator.RolePermission;
import static fi.vm.sade.eperusteet.ylops.service.security.PermissionEvaluator.RolePrefix;
import static fi.vm.sade.eperusteet.ylops.service.security.PermissionEvaluator.Organization;


/**
 *
 * @author mikkom
 */
@Service
public class PermissionManager {
    @Autowired
    private OpetussuunnitelmaRepository opetussuunnitelmaRepository;

    @Transactional(readOnly = true)
    public boolean hasPermission(Authentication authentication, Serializable targetId, TargetType target,
                                 Permission perm) {

        Set<RolePermission> permissions;
        switch (perm) {
            case LUKU:
                permissions = EnumSet.allOf(RolePermission.class);
                break;
            case LUONTI:
            case POISTO:
                permissions = EnumSet.of(RolePermission.CRUD);
                break;
            case MUOKKAUS:
            case KOMMENTOINTI:
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
                    Long opsId = (Long)targetId;
                    Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
                    Set<String> organisaatiot = SecurityUtil.getOrganizations(authentication, permissions);
                    return !CollectionUtil.intersect(ops.getOrganisaatiot(), organisaatiot).isEmpty();
                } else {
                    return hasAnyRole(authentication, RolePrefix.ROLE_APP_EPERUSTEET_YLOPS,
                                      permissions, Organization.ANY);
                }
            default:
                return hasAnyRole(authentication, RolePrefix.ROLE_APP_EPERUSTEET_YLOPS,
                                  permissions, Organization.ANY);
        }
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
}
