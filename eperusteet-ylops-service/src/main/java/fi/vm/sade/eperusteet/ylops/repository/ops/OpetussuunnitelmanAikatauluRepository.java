package fi.vm.sade.eperusteet.ylops.repository.ops;

import fi.vm.sade.eperusteet.ylops.domain.ops.OpetussuunnitelmaAikataulu;
import fi.vm.sade.eperusteet.ylops.repository.version.JpaWithVersioningRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpetussuunnitelmanAikatauluRepository extends JpaWithVersioningRepository<OpetussuunnitelmaAikataulu, Long> {

    List<OpetussuunnitelmaAikataulu> findByOpetussuunnitelmaId(Long opsId);
}
