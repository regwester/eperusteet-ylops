package fi.vm.sade.eperusteet.ylops.service.external.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import fi.vm.sade.eperusteet.ylops.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteInfoDto;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import lombok.Value;
import org.eclipse.core.internal.jobs.ObjectMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Profile("e2e")
public class EperusteetServiceE2EMock implements EperusteetService {

    static private JsonNode openFakeData(String file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Resource resource = new ClassPathResource("fakedata/" + file);
            InputStream resourceInputStream = resource.getInputStream();
            return objectMapper.readTree(resourceInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static private List<JsonNode> perusteet = new ArrayList<>();

    static {
        perusteet.add(openFakeData("varhaiskasvatus.json"));
    }

    @Override
    public PerusteDto getPeruste(String diaariNumero) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        for (JsonNode peruste : perusteet) {
            if (peruste.get("diaarinumero") != null && Objects.equals(diaariNumero, peruste.get("diaarinumero").asText())) {
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
    @Deprecated
    public List<PerusteInfoDto> findPerusteet(Set<KoulutusTyyppi> tyypit) {
        throw new UnsupportedOperationException("ei-toteutettu");
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
    public PerusteDto getEperusteetPeruste(Long id) {
        throw new UnsupportedOperationException("ei-toteutettu");
    }

    @Override
    public JsonNode getTiedotteet(Long jalkeen) {
        JsonNodeFactory foo = new JsonNodeFactory(false);
        return foo.arrayNode();
    }
}
