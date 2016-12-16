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
package org.eclipse.che.api.user.server.event;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.core.db.cascade.event.UpdateEvent;

/**
 * Published after {@link UserImpl user} updated.
 *
 * @author Sergii Leschenko
 */
@EventOrigin("user")
public class PostUserUpdatedEvent extends UpdateEvent {
    private final UserImpl original;
    private final UserImpl updated;

    public PostUserUpdatedEvent(UserImpl original, UserImpl updated) {
        this.original = original;
        this.updated = updated;
    }

    /** Returns user which was before update. */
    public UserImpl getOriginal() {
        return original;
    }

    /** Returns user which is updated. */
    public UserImpl getUpdated() {
        return updated;
    }
}
