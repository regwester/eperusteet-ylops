package fi.vm.sade.eperusteet.ylops.domain.lops2019;

import fi.vm.sade.eperusteet.ylops.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Audited
@Table(name = "lops2019_oppiaine_laajaalainenosaaminen")
public class Lops2019LaajaAlainenOsaaminen extends AbstractAuditedReferenceableEntity {

    @Getter
    @Setter
    @NotNull
    private String koodi;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    private LokalisoituTeksti kuvaus;

    public static Lops2019LaajaAlainenOsaaminen copy(Lops2019LaajaAlainenOsaaminen original) {
        if (original == null) {
            return null;
        }
        Lops2019LaajaAlainenOsaaminen result = new Lops2019LaajaAlainenOsaaminen();
        result.setKuvaus(original.getKuvaus());
        result.setKoodi(original.getKoodi());
        return result;
    }
}
