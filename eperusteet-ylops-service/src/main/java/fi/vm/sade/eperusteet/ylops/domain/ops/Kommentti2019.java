package fi.vm.sade.eperusteet.ylops.domain.ops;

import fi.vm.sade.eperusteet.ylops.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Kommenttikahvaan kiinnitetty kommentti. Kommentit haetaan tekstisisältöjen relaationa löytyvästä ketjusta.
 */
@Entity
@Table(name = "kommentti_2019")
@Audited
public class Kommentti2019 extends AbstractAuditedReferenceableEntity {

    @Getter
    @Setter
    @Column(updatable = false)
    @NotNull
    private UUID tunniste;

    @Getter
    @Setter
    @Column(updatable = false)
    @NotNull
    private UUID thread;

    @Getter
    @Setter
    @Column(updatable = false)
    private UUID reply;

    @Getter
    @Setter
    @Column(length = 1024)
    @Size(max = 1024, message = "Kommentin maksimipituus on {max} merkkiä")
    private String sisalto;

}
