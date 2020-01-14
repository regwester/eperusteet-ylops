package fi.vm.sade.eperusteet.ylops.service.ops.impl;

import fi.vm.sade.eperusteet.ylops.domain.ops.Kommentti2019;
import fi.vm.sade.eperusteet.ylops.domain.ops.KommenttiKahva;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.Kommentti2019Dto;
import fi.vm.sade.eperusteet.ylops.dto.ops.KommenttiKahvaDto;
import fi.vm.sade.eperusteet.ylops.repository.ops.Kommentti2019Repository;
import fi.vm.sade.eperusteet.ylops.repository.teksti.KommenttiKahvaRepository;
import fi.vm.sade.eperusteet.ylops.repository.teksti.LokalisoituTekstiRepository;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.external.KayttajanTietoService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.Kommentti2019Service;
import fi.vm.sade.eperusteet.ylops.service.security.PermissionManager;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class Kommentti2019ServiceImpl implements Kommentti2019Service {

    private final int CommentTagSize = 62;

    @Autowired
    private DtoMapper mapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private KayttajanTietoService kayttajanTietoService;

    @Autowired
    private Kommentti2019Repository kommenttiRepository;

    @Autowired
    private KommenttiKahvaRepository kahvaRepository;

    @Autowired
    private PermissionManager permissionManager;

    @Autowired
    private KayttajanTietoService kayttajat;

    @Autowired
    private LokalisoituTekstiRepository lokalisoituTekstiRepository;

    public boolean overlaps(int a1, int a2, int b1, int b2) {
        return (a1 <= b1 && b1 <= a2)
                || (a1 <= b2 && b2 <= a2)
                || (b1 <= a1 && a1 <= b2)
                || (b1 <= a2 && a2 <= b2);
    }

    /**
     * Tarkistaa että kommenttiketjujen ankkurit eivät mene päällekkäin.
     *
     * @param ketjut Kaikki kommenttiketjut.
     */
    public void validateCommentOverlaps(List<KommenttiKahva> ketjut) {
        for (KommenttiKahva a : ketjut) {
            if (a.getStart() >= a.getStop()) {
                throw new BusinessRuleViolationException("virheellinen-kommentin-sijainti");
            }
            if (a.getStart() < 0) {
                throw new BusinessRuleViolationException("virheellinen-kommentin-sijainti");
            }

            for (KommenttiKahva b : ketjut) {
                if (a != b && overlaps(a.getStart(), a.getStop(), b.getStart(), b.getStop())) {
                    throw new BusinessRuleViolationException("kommentit-eivat-saa-olla-paallekkain");
                }
            }
        }
    }

    /**
     * Vetää kahvan oikeaan kohtaan huomioiden sen vasemmalle puolelle lisätyt kommentit. Mahdollistaa yksittäisen
     * kommentin liittämisen sisältöön siten että se säilyy validina.
     *
     * @param kahvaDto Lisättävä kommenttikahva
     * @param ketjut Lokalisoidussa tekstissä löytyvät tekstiversiot
     */
    void pullToPlace(KommenttiKahvaDto kahvaDto, List<KommenttiKahva> ketjut) {
        LokalisoituTeksti teksti = lokalisoituTekstiRepository.getOne(kahvaDto.getTekstiId());
        int commentsBefore = (int)ketjut.stream()
                .filter(k -> k.getStart() < kahvaDto.getStart())
                .count();
        int pushAmount = CommentTagSize * commentsBefore;
        kahvaDto.setStart(kahvaDto.getStart() - pushAmount);
        kahvaDto.setStop(kahvaDto.getStop() - pushAmount);
    }

    /**
     * Lisää kommenttikahvan ja ensimmäisen kommentin osaksi tekstielementtiä. Jokaisen kommentin sijainti ilmoitetaan
     * suhteessa alkuperäiseen tekstiin ja muita kommentteja ei huomioida. Kommentit injektoidaan sisältöön lopusta
     * alkuun mikä ei sekoita kahvojen sijainteja.
     *
     * @param kahvaDto Lisättävä kommentti ja sen sijainti tekstisisällön kieliversiossa opetussuunnitelmittain.
     * @return Palauttaa lisätyn kahvan.
     */
    @Override
    public KommenttiKahvaDto addKommenttiKahva(KommenttiKahvaDto kahvaDto) {
        LokalisoituTeksti teksti = lokalisoituTekstiRepository.getOne(kahvaDto.getTekstiId());
        List<KommenttiKahva> ketjut = teksti.getKetjut().stream()
                .filter(kommenttiKahva -> kommenttiKahva.getKieli().equals(kahvaDto.getKieli()))
                .sorted(Comparator.comparingInt(KommenttiKahva::getStart).reversed())
                .collect(Collectors.toList());
        pullToPlace(kahvaDto, ketjut);

        KommenttiKahva kahva = mapper.map(kahvaDto, KommenttiKahva.class);
        kahva.setThread(UUID.randomUUID());
        kahva.setTeksti(teksti);
        validateCommentOverlaps(Stream.concat(ketjut.stream(), Stream.of(kahva)).collect(Collectors.toList()));
        kahva = kahvaRepository.save(kahva);
        teksti.getKetjut().add(kahva);
        em.merge(teksti);
        em.flush();

        {// Assert that html is valid
            LokalisoituTeksti updated = lokalisoituTekstiRepository.getOne(kahvaDto.getTekstiId());
            String html = updated.getTeksti().get(kahvaDto.getKieli());
            Jsoup.isValid(html, Whitelist.relaxed());
        }

        Kommentti2019Dto aloituskommentti = kahvaDto.getAloituskommentti();
        if (aloituskommentti != null) {
            aloituskommentti.setThread(kahva.getThread());
            aloituskommentti = add(kahva.getThread(), aloituskommentti);
        }

        KommenttiKahvaDto result = mapper.map(kahva, KommenttiKahvaDto.class);
        if (aloituskommentti != null) {
            result.setAloituskommentti(aloituskommentti);
        }
        result.setTekstiId(teksti.getId());
        return result;
    }

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
    public List<Kommentti2019Dto> get(UUID uuid) {
        return mapper.mapAsList(getThread(uuid), Kommentti2019Dto.class);
    }

    @Override
    public void asetaOikeatNimet(Kommentti2019Dto kommentti) {
//        kommentti.getKommentit().stream()
//                .flatMap(k -> k.getKommentit().stream())
//                .forEach(k -> k.setLuoja(haeNimi(k.getLuoja())));
    }

//    private String haeNimi(String oid) {
//        if (!StringUtils.isEmpty(oid)) {
//            try {
//                KayttajanTietoDto kayttaja = kayttajanTietoService.haeAsync(oid).get();
//                String kutsumanimi = kayttaja.getKutsumanimi();
//                String etunimet = kayttaja.getEtunimet();
//                String etunimi = kutsumanimi != null ? kutsumanimi : etunimet;
//                return etunimi + " " + kayttaja.getSukunimi();
//            } catch (ExecutionException | InterruptedException e) {
//                return oid;
//            }
//        }
//        return "Tuntematon käyttäjä";
//    }

    private void validateParentChain(Kommentti2019 kommentti) {
//        if (kommentti.getOpsId() == null) {
//            throw new BusinessRuleViolationException("virheellinen-kommenttiketju");
//        }
//
//        UUID threadUuid = kommentti.getThread();
//        if (threadUuid != null) {
//            Kommentti2019 parent = kommenttiRepository.getOne(threadUuid);
//            if (!Objects.equals(parent.getOpsId(), kommentti.getOpsId())) {
//                throw new BusinessRuleViolationException("virheellinen-kommenttiketju");
//            }
//            validateParentChain(parent);
//        }
    }

    private void hasOpsPermissions(Long opsId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        KayttajanTietoDto kirjautunut = kayttajat.haeKirjautaunutKayttaja();
//        if (kirjautunut.getOidHenkilo().equals(kommentti.getLuoja())) {
//            return;
//        }
        if (!permissionManager.hasPermission(authentication, opsId, PermissionManager.TargetType.OPETUSSUUNNITELMA, PermissionManager.Permission.LUKU)) {
            throw new BusinessRuleViolationException("ei-oikeutta");
        }
    }

    private void validateComment(Kommentti2019 kommentti, PermissionManager.Permission p) {
        validateParentChain(kommentti);
//        assertRights(kommentti, p);
    }

    private void validateComment(Kommentti2019 kommentti) {
        validateParentChain(kommentti);
//        assertRights(kommentti, PermissionManager.Permission.HALLINTA);
    }

    @Override
    public Kommentti2019Dto add(UUID uuid, Kommentti2019Dto kommenttiDto) {
        kommenttiDto.setLuoja(getUser());
        Date luotu = new Date();
        kommenttiDto.setLuotu(luotu);
        kommenttiDto.setMuokattu(luotu);
        kommenttiDto.setTunniste(UUID.randomUUID());
        hasOpsPermissions(kommenttiDto.getOpsId());
        Kommentti2019 kommentti = mapper.map(kommenttiDto, Kommentti2019.class);
//        validateComment(kommentti, PermissionManager.Permission.MUOKKAUS);
        kommentti = kommenttiRepository.save(kommentti);
        return mapper.map(kommentti, Kommentti2019Dto.class);
    }

    @Override
    public Kommentti2019Dto update(Kommentti2019Dto kommenttiDto) {
        Kommentti2019 kommentti = getOne(kommenttiDto.getTunniste());
        if (!kommentti.getLuoja().equals(getUser())) {
            throw new BusinessRuleViolationException("vain-omaa-kommenttia-voi-muokata");
        }

        kommentti.setSisalto(kommenttiDto.getSisalto());
        kommentti.setMuokattu(new Date());
        validateComment(kommentti);
        kommenttiRepository.save(kommentti);
        return mapper.map(kommentti, Kommentti2019Dto.class);
    }

    private Kommentti2019 getOne(UUID uuid) {
        Kommentti2019 kommentti = kommenttiRepository.getOne(uuid);
        if (kommentti == null) {
            throw new BusinessRuleViolationException("virheellinen-kiinnitys");
        }
        return kommentti;
    }

    private List<Kommentti2019> getThread(UUID uuid) {
        List<Kommentti2019> kommentti = kommenttiRepository.findAllByThread(uuid);
        if (kommentti == null) {
            throw new BusinessRuleViolationException("virheellinen-kiinnitys");
        }
        return kommentti;
    }

    @Override
    public void remove(UUID uuid) {
        Kommentti2019 kommentti = getOne(uuid);
        validateComment(kommentti);
        if (!kommentti.getLuoja().equals(getUser())) {
            throw new BusinessRuleViolationException("vain-omaa-kommenttia-voi-muokata");
        }

        kommenttiRepository.delete(kommentti);
    }
}
