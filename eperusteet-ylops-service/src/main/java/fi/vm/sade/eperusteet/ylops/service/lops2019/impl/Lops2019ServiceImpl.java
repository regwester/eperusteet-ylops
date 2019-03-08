package fi.vm.sade.eperusteet.ylops.service.lops2019.impl;

import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019OpintojaksoDto;
import fi.vm.sade.eperusteet.ylops.dto.lops2019.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.ylops.service.lops2019.Lops2019Service;
import fi.vm.sade.eperusteet.ylops.service.ops.OpetussuunnitelmaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class Lops2019ServiceImpl implements Lops2019Service {

    @Autowired
    private OpetussuunnitelmaService opetussuunnitelmaService;

    @Override
    public List<Lops2019OpintojaksoDto> getOpintojaksot(Long opsId) {
        return null;
    }

    @Override
    public List<Lops2019OppiaineDto> getPerusteOppiaineet(Long opsId) {
        return null;
    }
}
