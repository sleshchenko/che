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
    private final UserImpl originalUser;
    private final UserImpl updatedUser;

    public PostUserUpdatedEvent(UserImpl originalUser, UserImpl updatedUser) {
        this.originalUser = originalUser;
        this.updatedUser = updatedUser;
    }

    /** Returns user which is updated. */
    public UserImpl getUpdatedUser() {
        return updatedUser;
    }

    /** Return user which is updating. */
    public UserImpl getOriginalUser() {
        return originalUser;
    }
}
