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

import org.eclipse.che.core.db.jpa.ConflictCascadeOperationException;

/**
 * Publisher should expect {@link ConflictCascadeOperationException} while publishing event.
 *
 * @author Sergii Leschenko
 */
public abstract class PersistedEvent extends CascadeEvent {
}
