package fi.vm.sade.eperusteet.ylops.dto.peruste;

import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Field;
import java.util.*;

@Getter
@Setter
public class TiedoteQueryDto {
    private int sivu = 0;
    private int sivukoko = 25;
    private List<Kieli> kieli;
    private String nimi;
    private Long perusteId;
    private Boolean perusteeton;
    private Boolean julkinen; // Jos null, haetaan julkiset ja sisäiset
    private Boolean yleinen; // Jos halutaan esittää mm. etusivulla

    public String toRequestParams() {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();

        for (Field field : this.getClass().getDeclaredFields()) {
            String name = field.getName();
            try {
                Object value = field.get(this);
                if (value instanceof Collection<?>){
                    for (Object element : (Collection<?>) value) {
                        if (!ObjectUtils.isEmpty(value)) {
                            builder.queryParam(name, element);
                        }
                    }
                } else {
                    if (!ObjectUtils.isEmpty(value)) {
                        builder.queryParam(name, value);
                    }
                }
            } catch (IllegalAccessException e) {
                // Ignore
            }
        }

        return builder.build().encode().toUriString();
    }
}
