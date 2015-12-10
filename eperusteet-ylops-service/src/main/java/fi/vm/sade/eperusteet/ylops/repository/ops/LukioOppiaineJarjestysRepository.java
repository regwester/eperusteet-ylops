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

package fi.vm.sade.eperusteet.ylops.repository.ops;

import fi.vm.sade.eperusteet.ylops.domain.lukio.LukioOppiaineId;
import fi.vm.sade.eperusteet.ylops.domain.lukio.LukioOppiaineJarjestys;
import fi.vm.sade.eperusteet.ylops.dto.lukio.OppiaineJarjestysDto;
import fi.vm.sade.eperusteet.ylops.repository.version.JpaWithVersioningRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * User: tommiratamaa
 * Date: 10.12.2015
 * Time: 13.56
 */
@Repository
public interface LukioOppiaineJarjestysRepository extends JpaWithVersioningRepository<LukioOppiaineJarjestys, LukioOppiaineId> {

    @Query(value = "delete from LukioOppiaineJarjestys j where j.oppiaine.id = ?1")
    void deleteByOppiaineId(long oppiaineId);

    @Query(value = "select j from LukioOppiaineJarjestys j where j.opetussuunnitelma.id = ?1 " +
            " order by j.jarjestys, j.oppiaine.id")
    List<LukioOppiaineJarjestys> findByOpetussuunnitelmaId(long opsId);

    @Query(value = "select new fi.vm.sade.eperusteet.ylops.dto.lukio.OppiaineJarjestysDto(oa.id, j.jarjestys)" +
            " from LukioOppiaineJarjestys j inner join j.oppiaine oa where j.opetussuunnitelma.id = ?1" +
            " order by j.jarjestys, oa.id")
    List<OppiaineJarjestysDto> findJarjestysDtosByOpetussuunnitelmaId(long opsId);
}
