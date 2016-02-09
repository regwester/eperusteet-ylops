package fi.vm.sade.eperusteet.ylops.domain.teksti;

import fi.vm.sade.eperusteet.ylops.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.ylops.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by autio on 9.2.2016.
 */
@Entity
@Table(name = "poistettu_tekstikappale")
@Audited
public class PoistettuTekstiKappale extends AbstractAuditedEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opetussuunnitelma_id", nullable = false)
    private Opetussuunnitelma opetussuunnitelma;

    @ManyToOne
    @Getter
    @Setter
    private TekstiKappale tekstiKappale;

    @Getter
    @Setter
    private Boolean palautettu;

}

