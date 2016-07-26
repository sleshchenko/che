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
package org.eclipse.che.account.shared.model;

/**
 * TODO Add description
 *
 * @author Sergii Leschenko
 */
public interface Account {
    /**
     * Returns account id
     */
    String getId();

    /**
     * Returns name of account
     */
    String getName();

    /**
     * Returns type of account
     */
    String getType();
}
