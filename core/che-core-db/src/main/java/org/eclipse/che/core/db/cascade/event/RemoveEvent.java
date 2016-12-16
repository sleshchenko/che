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
package org.eclipse.che.core.db.cascade.event;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.core.db.cascade.CascadeEventService;

/**
 * Cascade event about an entity removing.
 *
 * <p>{@link ConflictException} or {@link ServerException} can be thrown
 * during event publishing.
 *
 * @see CascadeEventService#publish(RemoveEvent)
 *
 * @author Sergii Leschenko
 */
public abstract class RemoveEvent extends CascadeEvent {
}
