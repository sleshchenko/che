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
package org.eclipse.che.api.workspace.server.model;

import java.util.List;

/**
 * @author Sergii Leschenko
 */
public class AclImpl implements Acl {
    private final String user;
    private final List<String>     actions;

    public AclImpl(String user, List<String> actions) {
        this.user = user;
        this.actions = actions;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public List<String> getActions() {
        return actions;
    }
}
