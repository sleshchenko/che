/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.core.db.cascade;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.core.db.cascade.event.CascadeEvent;
import org.eclipse.che.core.db.cascade.event.PersistEvent;
import org.eclipse.che.core.db.cascade.event.RemoveEvent;
import org.eclipse.che.core.db.cascade.event.UpdateEvent;

import javax.inject.Singleton;

/**
 * Extends {@link EventService} and allows throwing
 * exception during cascade event publishing.
 *
 * <p>Usage example:
 * <pre>
 *     CascadeEventService bus = new CascadeEventService();
 *     bus.subscribe(new CascadeEventSubscriber&lt;MyEvent&gt;() {
 *         &#64;Override
 *         public void onCascadeEvent(MyEvent event) throws ApiException {
 *             if (event.getUsername().startsWith("reserved")) {
 *                 throw new ConflictException("Username can't start with `reserved`.");
 *             }
 *         }
 *     });
 *     bus.publish(new MyEvent(...));
 * </pre>
 *
 * @author Sergii Leschenko
 */
@Singleton
public class CascadeEventService extends EventService {

    /**
     * Publish specified {@code event}.
     *
     * @param event
     *         event to publish
     * @throws ApiException
     *         when any subscriber throws {@link ApiException}
     */
    public void publish(CascadeEvent event) throws ApiException {
        super.publish(event);
        if (event.getContext().isFailed()) {
            throw event.getContext().getCause();
        }
    }

    /**
     * Publish specified {@code event}.
     *
     * @param event
     *         event to publish
     * @throws ConflictException
     *         when any subscriber throws {@link ConflictException}
     * @throws ServerException
     *         when any subscriber throws {@link ServerException}
     * @throws ServerException
     *         when any subscriber throws other kind of {@link ApiException}
     */
    public void publish(PersistEvent event) throws ConflictException,
                                                   ServerException {
        try {
            publish((CascadeEvent)event);
        } catch (ConflictException | ServerException e) {
            throw e;
        } catch (ApiException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Publish specified {@code event}.
     *
     * @param event
     *         event to publish
     * @throws ConflictException
     *         when any subscriber throws {@link ConflictException}
     * @throws ServerException
     *         when any subscriber throws {@link ServerException}
     * @throws ServerException
     *         when any subscriber throws other kind of {@link ApiException}
     */
    public void publish(RemoveEvent event) throws ConflictException,
                                                  ServerException {
        try {
            publish((CascadeEvent)event);
        } catch (ConflictException | ServerException e) {
            throw e;
        } catch (ApiException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Publish specified {@code event}.
     *
     * @param event
     *         event to publish
     * @throws NotFoundException
     *         when any subscriber throws {@link NotFoundException}
     * @throws ConflictException
     *         when any subscriber throws {@link ConflictException}
     * @throws ServerException
     *         when any subscriber throws {@link ServerException}
     * @throws ServerException
     *         when any subscriber throws other kind of {@link ApiException}
     */
    public void publish(UpdateEvent event) throws NotFoundException,
                                                  ConflictException,
                                                  ServerException {
        try {
            publish((CascadeEvent)event);
        } catch (NotFoundException | ConflictException | ServerException e) {
            throw e;
        } catch (ApiException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }
}
