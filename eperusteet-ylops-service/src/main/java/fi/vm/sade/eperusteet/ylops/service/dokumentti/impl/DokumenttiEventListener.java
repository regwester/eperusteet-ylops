/*
 *
 *  * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *  *
 *  * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  * soon as they will be approved by the European Commission - subsequent versions
 *  * of the EUPL (the "Licence");
 *  *
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  * European Union Public Licence for more details.
 *
 */

package fi.vm.sade.eperusteet.ylops.service.dokumentti.impl;

import org.apache.fop.events.Event;
import org.apache.fop.events.EventFormatter;
import org.apache.fop.events.EventListener;
import org.apache.fop.events.model.EventSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author isaul
 */
public class DokumenttiEventListener implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiEventListener.class);

    @Override
    public void processEvent(Event event) {
        String msg = EventFormatter.format(event);
        EventSeverity severity = event.getSeverity();
        if (severity == EventSeverity.INFO) {
            LOG.info(msg);
        } else if (severity == EventSeverity.WARN) {
            LOG.warn(msg);
        } else if (severity == EventSeverity.ERROR) {
            LOG.error(msg);
        } else if (severity == EventSeverity.FATAL) {
            LOG.error(msg);
        } else {
            assert false;
        }
    }
}
