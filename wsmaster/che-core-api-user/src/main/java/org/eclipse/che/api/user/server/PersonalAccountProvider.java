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
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.api.user.server.event.PostUserPersistedEvent;
import org.eclipse.che.api.user.server.event.PostUserUpdatedEvent;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * //TODO Revise this class
 *
 * @author Sergii Leschenko
 */
@Singleton
public class PersonalAccountProvider {
    public static final String PERSONAL_ACCOUNT = "personal";

    private final AccountManager             accountManager;
    private final EventService               eventService;
    private final PersonalAccountCreator     accountCreator;
    private final PersonalAccountNameUpdater accountNameUpdater;
    private final PersonalAccountRemover     accountRemover;

    @Inject
    public PersonalAccountProvider(AccountManager accountManager, EventService eventService) {
        this.accountManager = accountManager;
        this.eventService = eventService;
        this.accountCreator = new PersonalAccountCreator();
        this.accountNameUpdater = new PersonalAccountNameUpdater();
        this.accountRemover = new PersonalAccountRemover();
    }

    @PostConstruct
    public void subscribe() {
        eventService.subscribe(accountCreator, PostUserPersistedEvent.class);
        eventService.subscribe(accountNameUpdater, PostUserUpdatedEvent.class);
        eventService.subscribe(accountRemover, BeforeUserRemovedEvent.class);
    }

    public class PersonalAccountCreator extends CascadeEventSubscriber<PostUserPersistedEvent> {
        @Override
        public void onCascadeEvent(PostUserPersistedEvent event) throws ApiException {
            accountManager.create(new AccountImpl(event.getUser().getId(), event.getUser().getName(), PERSONAL_ACCOUNT));
        }
    }

    public class PersonalAccountNameUpdater extends CascadeEventSubscriber<PostUserUpdatedEvent> {
        @Override
        public void onCascadeEvent(PostUserUpdatedEvent event) throws ApiException {
            String oldUsername = event.getOriginalUser().getName();
            String newUserName = event.getUpdatedUser().getName();
            if (!oldUsername.equals(newUserName)) {
                accountManager.update(new AccountImpl(event.getOriginalUser().getId(), newUserName, PERSONAL_ACCOUNT));
            }
        }
    }

    public class PersonalAccountRemover extends CascadeEventSubscriber<BeforeUserRemovedEvent> {
        @Override
        public void onCascadeEvent(BeforeUserRemovedEvent event) throws ApiException {
            accountManager.remove(event.getUser().getId());
        }
    }
}
