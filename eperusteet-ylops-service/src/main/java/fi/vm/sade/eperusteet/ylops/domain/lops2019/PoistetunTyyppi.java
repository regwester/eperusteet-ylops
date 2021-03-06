package fi.vm.sade.eperusteet.ylops.domain.lops2019;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PoistetunTyyppi {
    TUOTU_OPINTOJAKSO("tuotu_opintojakso"),
    LOPS2019OPPIAINE("lops2019oppiaine"),
    OPINTOJAKSO("opintojakso"),
    OPPIAINE("oppiaine");

    private final String tyyppi;

    PoistetunTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    @Override
    public String toString() {
        return tyyppi;
    }

    @JsonCreator
    public static PoistetunTyyppi of(String tila) {
        for (PoistetunTyyppi s : values()) {
            if (s.tyyppi.equalsIgnoreCase(tila)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tila + " ei ole kelvollinen tyyppi");
    }
}
