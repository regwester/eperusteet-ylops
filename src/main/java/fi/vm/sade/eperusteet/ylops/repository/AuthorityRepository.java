package fi.vm.sade.eperusteet.ylops.repository;

import fi.vm.sade.eperusteet.ylops.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Authority entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
