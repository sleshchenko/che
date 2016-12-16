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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.core.db.jpa.CascadeOperationException;
import org.eclipse.che.core.db.jpa.ConflictCascadeOperationException;

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
     * @throws CascadeOperationException
     *         when some error occurs during operation publishing
     */
    public void publish(CascadeEvent event) throws CascadeOperationException {
        super.publish(event);
        if (event.getContext().isFailed()) {
            Exception cause = event.getContext().getCause();
            if (cause instanceof ConflictException) {
                throw new ConflictCascadeOperationException(cause.getMessage(), cause);
            } else {
                throw new CascadeOperationException(cause.getMessage(), cause);
            }
        }
    }

    /**
     * Publish specified {@code event}.
     *
     * @param event
     *         event to publish
     * @throws ConflictCascadeOperationException
     *         409
     * @throws CascadeOperationException
     *         500
     */
    public void publish(PersistedEvent event) throws ConflictCascadeOperationException,
                                                     CascadeOperationException {
        publish((CascadeEvent)event);
    }
}
