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
package org.eclipse.che.core.db.event;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;

import javax.inject.Singleton;

/**
 * Allows to throw exception during cascade event
 * publishing to cancel event.
 *
 * <p>Usage example:
 * <pre>
 *     EventService bus = new CascadeEventService();
 *     bus.subscribe(new CascadeEventSubscriber&lt;MyEvent&gt;() {
 *         &#64;Override
 *         public void onCascadeEvent(MyEvent event) {
 *             if (event.getUsername().startsWith("reserved")) {
 *                 throw new CascadeOperationException("Username can't start with `reserved`.");
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
     *         when some error occurs during operation publishing
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
     *         409
     * @throws ServerException
     *         500
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
     *         409
     * @throws ServerException
     *         500
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
     * @throws ConflictException
     *         409
     * @throws ServerException
     *         500
     */
    public void publish(UpdateEvent event) throws ConflictException,
                                                  ServerException,
                                                  NotFoundException {
        try {
            publish((CascadeEvent)event);
        } catch (NotFoundException | ConflictException | ServerException e) {
            throw e;
        } catch (ApiException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }
}
