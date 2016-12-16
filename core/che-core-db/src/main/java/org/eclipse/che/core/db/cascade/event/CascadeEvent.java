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
package org.eclipse.che.core.db.cascade.event;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.core.db.cascade.CascadeContext;

/**
 * Special event type which is needed only for notification
 * in the process which can require cascade operation.
 *
 * <p>Rollback of operation must be performed when subscriber
 * throws {@link ApiException} during event processing.
 *
 * @author Anton Korneta
 * @author Sergii Leschenko
 */
public abstract class CascadeEvent {
    protected final CascadeContext context = new CascadeContext();

    public CascadeContext getContext() {
        return context;
    }
}
