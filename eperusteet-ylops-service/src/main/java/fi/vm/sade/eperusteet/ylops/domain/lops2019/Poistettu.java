package fi.vm.sade.eperusteet.ylops.domain.lops2019;

import fi.vm.sade.eperusteet.ylops.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Audited
@Table(name = "lops2019_poistettu_sisalto")
@Getter
@Setter
public class Poistettu extends AbstractAuditedEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @NotNull
    private Opetussuunnitelma opetussuunnitelma;

    @Column(name = "poistettu_id")
    private Long poistettuId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PoistetunTyyppi tyyppi;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    private LokalisoituTeksti nimi;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    private LokalisoituTeksti parent;

    private Boolean palautettu = false;
}
