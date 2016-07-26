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
package org.eclipse.che.api.workspace.server.spi.tck;

import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;

import javax.persistence.Entity;

/**
 * Simple implementation of {@link AccountImpl} for testing {@link WorkspaceDao} interface
 *
 * @author Sergii Leschenko.
 */
@Entity(name = "TestAccount")
public class TestAccountImpl extends AccountImpl {
    public TestAccountImpl() {
    }

    public TestAccountImpl(String id, String name) {
        super(id, name);
    }

    @Override
    public String getType() {
        return "test";
    }
}
