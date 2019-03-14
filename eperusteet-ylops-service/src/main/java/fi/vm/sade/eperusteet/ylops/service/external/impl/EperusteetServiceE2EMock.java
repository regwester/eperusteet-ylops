package fi.vm.sade.eperusteet.ylops.service.external.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import fi.vm.sade.eperusteet.ylops.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.ylops.domain.cache.PerusteCache;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteInfoDto;
import fi.vm.sade.eperusteet.ylops.repository.cache.PerusteCacheRepository;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.external.impl.perustedto.EperusteetPerusteDto;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.util.JsonMapper;
import lombok.Value;
import org.eclipse.core.internal.jobs.ObjectMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Service
@Profile("e2e")
public class EperusteetServiceE2EMock implements EperusteetService {

    @Autowired
    private PerusteCacheRepository perusteCacheRepository;

    @Autowired
    private JsonMapper jsonMapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        objectMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        perusteet.add(openFakeData("varhaiskasvatus.json"));
        perusteet.add(openFakeData("lops.json"));
    }

    private JsonNode openFakeData(String file) {
        try {
            Resource resource = new ClassPathResource("fakedata/" + file);
            InputStream resourceInputStream = resource.getInputStream();
            JsonNode result = objectMapper.readTree(resourceInputStream);
            PerusteCache peruste = perusteCacheRepository.findNewestEntryForPerusteByDiaarninumero(result.get("diaarinumero").asText());
            if (peruste == null) {
                savePerusteCahceEntry(objectMapper.treeToValue(result, EperusteetPerusteDto.class));
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<JsonNode> perusteet = new ArrayList<>();

    // FIXME pilko service kahteen osaan
    private void savePerusteCahceEntry(EperusteetPerusteDto peruste) {
        PerusteCache cache = new PerusteCache();
        cache.setAikaleima(peruste.getGlobalVersion().getAikaleima());
        cache.setPerusteId(peruste.getId());
        cache.setKoulutustyyppi(peruste.getKoulutustyyppi());
        cache.setDiaarinumero(peruste.getDiaarinumero());
        cache.setVoimassaoloAlkaa(peruste.getVoimassaoloAlkaa());
        cache.setVoimassaoloLoppuu(peruste.getVoimassaoloLoppuu());
        cache.setNimi(LokalisoituTeksti.of(peruste.getNimi().getTekstit()));
        try {
            cache.setPerusteJson(peruste, jsonMapper);
        } catch (IOException e) {
            // Should not happen (EperusteetPerusteDto parsed from JSON to begin with)
            throw new IllegalStateException("Could not serialize EperusteetPerusteDto for cache.", e);
        }
        perusteCacheRepository.saveAndFlush(cache);
    }

    public PerusteDto getPeruste(Predicate<JsonNode> cmp) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        for (JsonNode peruste : perusteet) {
            if (cmp.test(peruste)) {
                try {
                    return objectMapper.treeToValue(peruste, PerusteDto.class);
                } catch (JsonProcessingException e) {
                    throw new BusinessRuleViolationException("perusteen-parsinta-epaonnistui");
                }
            }
        }
        return null;
    }

    @Override
    public PerusteDto getPeruste(String diaariNumero) {
        return getPeruste((peruste) ->
                peruste.get("diaarinumero") != null
                        && Objects.equals(diaariNumero, peruste.get("diaarinumero").asText()));
    }

    @Override
    public PerusteDto getPerusteUpdateCache(String diaarinumero) {
        throw new UnsupportedOperationException("ei-toteutettu");
    }

    @Override
    public List<PerusteInfoDto> findPerusteet() {
        ObjectMapper objectMapper = new ObjectMapper();
        return perusteet.stream()
                .map(p -> {
                    try {
                        return objectMapper.treeToValue(p, PerusteInfoDto.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        throw new BusinessRuleViolationException("perusteen-parsinta-epaonnistui");
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PerusteInfoDto> findPerusteet(Set<KoulutusTyyppi> tyypit) {
        return findPerusteet().stream()
                .filter(peruste -> tyypit.contains(peruste.getKoulutustyyppi()))
                .collect(Collectors.toList());
    }

    @Override
    @Deprecated
    public List<PerusteInfoDto> findPerusopetuksenPerusteet() {
        throw new UnsupportedOperationException("ei-toteutettu");
    }

    @Override
    @Deprecated
    public List<PerusteInfoDto> findLukiokoulutusPerusteet() {
        throw new UnsupportedOperationException("ei-toteutettu");
    }

    @Override
    public PerusteDto getPerusteById(Long id) {
        return getPeruste((peruste) ->
                peruste.get("id") != null
                        && Objects.equals(id, peruste.get("id").asLong()));
    }

    @Override
    public JsonNode getTiedotteet(Long jalkeen) {
        JsonNodeFactory foo = new JsonNodeFactory(false);
        return foo.arrayNode();
    }
}
