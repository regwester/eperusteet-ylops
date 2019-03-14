package fi.vm.sade.eperusteet.ylops.repository.ukk;

import fi.vm.sade.eperusteet.ylops.domain.ukk.Kysymys;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KysymysRepository extends JpaRepository<Kysymys, Long> {
    
}
