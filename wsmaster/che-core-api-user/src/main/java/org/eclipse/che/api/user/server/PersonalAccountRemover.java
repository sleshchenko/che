/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.user.server;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * //TODO Move to onpremises
 *
 * @author Sergii Leschenko
 */
@Singleton
public class PersonalAccountRemover extends CascadeEventSubscriber<BeforeUserRemovedEvent> {
    private final AccountManager accountManager;
    private final EventService   eventService;

    @Inject
    public PersonalAccountRemover(AccountManager accountManager,
                                  EventService eventService) {
        this.accountManager = accountManager;
        this.eventService = eventService;
    }

    @PostConstruct
    public void subscribe() {
        eventService.subscribe(this, BeforeUserRemovedEvent.class);
    }

    @PreDestroy
    public void unsubscribe() {
        eventService.unsubscribe(this, BeforeUserRemovedEvent.class);
    }

    @Override
    public void onCascadeEvent(BeforeUserRemovedEvent event) throws ApiException {
        accountManager.remove(event.getUser().getId());
    }
}
