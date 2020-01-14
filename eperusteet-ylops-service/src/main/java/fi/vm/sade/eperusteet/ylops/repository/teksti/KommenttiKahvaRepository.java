package fi.vm.sade.eperusteet.ylops.repository.teksti;

import fi.vm.sade.eperusteet.ylops.domain.ops.KommenttiKahva;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;

public interface KommenttiKahvaRepository extends JpaRepository<KommenttiKahva, Long> {

    Set<KommenttiKahva> findAllByTeksti(LokalisoituTeksti lt);
}
