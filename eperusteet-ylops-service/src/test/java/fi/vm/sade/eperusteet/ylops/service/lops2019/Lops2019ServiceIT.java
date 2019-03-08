package fi.vm.sade.eperusteet.ylops.service.lops2019;

import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteInfoDto;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.external.impl.EperusteetServiceE2EMock;
import fi.vm.sade.eperusteet.ylops.test.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class Lops2019ServiceIT extends AbstractIntegrationTest {

    private EperusteetService eperusteetService;

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void setup() {
        eperusteetService = new EperusteetServiceE2EMock();
        AutowireCapableBeanFactory factory = applicationContext.getAutowireCapableBeanFactory();
        factory.autowireBean(eperusteetService);
        factory.initializeBean(eperusteetService, "EperusteetServiceE2EMock");

    }

    @Test
    public void convertTestJsonToDto() {
        List<PerusteInfoDto> perusteet = eperusteetService.findPerusteet();
        assertThat(perusteet.size()).isEqualTo(2);
        PerusteDto peruste = eperusteetService.getPeruste("1/2/3");
    }

}
