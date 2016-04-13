package fi.vm.sade.eperusteet.ylops.repository.ops;

import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Opetuksenkohdealue;
import fi.vm.sade.eperusteet.ylops.repository.version.JpaWithVersioningRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by autio on 12.4.2016.
 */
@Repository
public interface OpetuksenkohdealueRepository extends JpaWithVersioningRepository<Opetuksenkohdealue, Long> {
}
