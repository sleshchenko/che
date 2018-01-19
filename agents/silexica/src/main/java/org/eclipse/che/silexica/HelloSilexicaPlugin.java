/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.silexica;

import javax.inject.Inject;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.shared.dto.event.InstallerLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloSilexicaPlugin {
  private static final Logger LOG = LoggerFactory.getLogger(HelloSilexicaPlugin.class);

  @Inject
  public void subscribe(EventService eventService) {
    eventService.subscribe(new InstallerOutputListener());
  }

  private static class InstallerOutputListener implements EventSubscriber<InstallerLogEvent> {
    @Override
    public void onEvent(InstallerLogEvent event) {
      if ("org.eclipse.silexica.hello".equals(event.getInstaller())) {
        LOG.info(
            "Silexica | "
                + event.getRuntimeId()
                + ":"
                + event.getMachineName()
                + " > "
                + event.getText());
      }
    }
  }
}
