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
package fi.vm.sade.eperusteet.ylops.service.ops.impl;

import fi.vm.sade.eperusteet.ylops.domain.liite.Liite;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.dto.liite.LiiteDto;
import fi.vm.sade.eperusteet.ylops.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.ylops.service.exception.ServiceException;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.LiiteService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import org.apache.commons.io.IOUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jhyoty
 */
@Service
public class LiiteServiceImpl implements LiiteService {

    @Autowired
    EntityManager em;

    @Autowired
    DtoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public void export(Long opsId, UUID id, OutputStream os) {
        Liite liite = em.find(Liite.class, id);
        if ( liite == null ) {
            throw new NotExistsException("ei ole");
        }
        try ( InputStream is = liite.getData().getBinaryStream() ) {
            IOUtils.copy(is, os);
        } catch (SQLException | IOException ex) {
            throw new ServiceException("Liiteen lataaminen ei onnistu", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LiiteDto get(Long opsId, UUID id) {
        Liite liite = em.find(Liite.class, id);
        return mapper.map(liite, LiiteDto.class);
    }

    @Override
    @Transactional
    public UUID add(Long opsId, String tyyppi, String nimi, long length, InputStream is) {
        Session session = em.unwrap(Session.class);
        Blob blob = Hibernate.getLobCreator(session).createBlob(is, length);
        Liite liite = new Liite(tyyppi, nimi, blob);
        em.persist(liite);
        Opetussuunnitelma ops = em.find(Opetussuunnitelma.class, opsId);
        ops.attachLiite(liite);
        return liite.getId();
    }

    @Override
    public List<LiiteDto> getAll(Long opsId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(Long opsId, UUID id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
