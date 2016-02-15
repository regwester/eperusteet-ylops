package fi.vm.sade.eperusteet.ylops.domain.teksti;

import fi.vm.sade.eperusteet.ylops.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.ylops.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by autio on 9.2.2016.
 */
@Entity
@Table(name = "poistettu_tekstikappale")
@Audited
@Getter
@Setter
public class PoistettuTekstiKappale extends AbstractAuditedEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opetussuunnitelma_id", nullable = false)
    private Opetussuunnitelma opetussuunnitelma;

    @ManyToOne
    private TekstiKappale tekstiKappale;

    private Boolean palautettu;

}

