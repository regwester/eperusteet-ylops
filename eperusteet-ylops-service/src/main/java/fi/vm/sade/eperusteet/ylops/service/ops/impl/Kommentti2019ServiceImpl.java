package fi.vm.sade.eperusteet.ylops.service.ops.impl;

import fi.vm.sade.eperusteet.ylops.domain.ops.Kommentti2019;
import fi.vm.sade.eperusteet.ylops.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.Kommentti2019Dto;
import fi.vm.sade.eperusteet.ylops.dto.ops.Kommentti2019LuontiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.KommenttiDto;
import fi.vm.sade.eperusteet.ylops.repository.ops.Kommentti2019Repository;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.external.KayttajanTietoService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.Kommentti2019Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class Kommentti2019ServiceImpl implements Kommentti2019Service {

    @Autowired
    private DtoMapper mapper;

    @Autowired
    private KayttajanTietoService kayttajanTietoService;

    @Autowired
    private Kommentti2019Repository kommenttiRepository;

    private String getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getName();
        }
        else {
            throw new BusinessRuleViolationException("kayttaja-ei-kirjautunut");
        }
    }

    @Override
    public Kommentti2019Dto get(UUID uuid) {
        return mapper.map(getImpl(uuid), Kommentti2019Dto.class);
    }

    @Override
    public Kommentti2019Dto reply(UUID uuid, Kommentti2019LuontiDto kommenttiDto) {
        Kommentti2019 parent = getImpl(uuid);
        return null;
    }

    @Override
    public void asetaOikeatNimet(Kommentti2019Dto kommentti) {
        kommentti.getKommentit().stream()
                .flatMap(k -> k.getKommentit().stream())
                .forEach(k -> k.setLuoja(haeNimi(k.getLuoja())));
    }

    private String haeNimi(String oid) {
        if (!StringUtils.isEmpty(oid)) {
            try {
                KayttajanTietoDto kayttaja = kayttajanTietoService.haeAsync(oid).get();
                String kutsumanimi = kayttaja.getKutsumanimi();
                String etunimet = kayttaja.getEtunimet();
                String etunimi = kutsumanimi != null ? kutsumanimi : etunimet;
                return etunimi + " " + kayttaja.getSukunimi();
            } catch (ExecutionException | InterruptedException e) {
                return oid;
            }
        }
        return "Tuntematon käyttäjä";
    }

    @Override
    public Kommentti2019Dto add(Kommentti2019LuontiDto kommenttiLuontiDto) {
        Kommentti2019Dto kommenttiDto = mapper.map(kommenttiLuontiDto, Kommentti2019Dto.class);
        kommenttiDto.setLuoja(getUser());
        Date luotu = new Date();
        kommenttiDto.setLuotu(luotu);
        kommenttiDto.setMuokattu(luotu);
        kommenttiDto.setTunniste(UUID.randomUUID());
        Kommentti2019 kommentti = mapper.map(kommenttiDto, Kommentti2019.class);
        kommentti = kommenttiRepository.save(kommentti);

        if (kommenttiDto.getParent() != null) {
            Kommentti2019 parent = getImpl(kommenttiDto.getParent());
            parent.getKommentit().add(kommentti);
        }

        return mapper.map(kommentti, Kommentti2019Dto.class);
    }


    @Override
    public Kommentti2019Dto update(UUID uuid, Kommentti2019LuontiDto kommenttiUpdateDto) {
        Kommentti2019 kommentti = getImpl(uuid);
        if (!kommentti.getLuoja().equals(getUser())) {
            throw new BusinessRuleViolationException("vain-omaa-kommenttia-voi-muokata");
        }
        kommentti.setSisalto(kommenttiUpdateDto.getSisalto());
        kommentti.setMuokattu(new Date());
        kommenttiRepository.save(kommentti);
        return mapper.map(kommentti, Kommentti2019Dto.class);
    }

    private Kommentti2019 getImpl(UUID uuid) {
        Kommentti2019 kommentti = kommenttiRepository.getOne(uuid);
        if (kommentti == null) {
            throw new BusinessRuleViolationException("virheellinen-kiinnitys");
        }
        return kommentti;
    }

    @Override
    public void remove(UUID uuid) {
        Kommentti2019 kommentti = getImpl(uuid);
        if (!kommentti.getLuoja().equals(getUser())) {
            throw new BusinessRuleViolationException("vain-omaa-kommenttia-voi-muokata");
        }

        if (kommentti.getParent() != null) {
            Kommentti2019 parent = getImpl(kommentti.getParent());
            parent.getKommentit().remove(kommentti);
        }

        kommenttiRepository.delete(kommentti);
    }
}
