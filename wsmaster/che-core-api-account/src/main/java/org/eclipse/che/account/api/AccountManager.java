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
package org.eclipse.che.account.api;

import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.account.spi.AccountDao;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;

/**
 * Facade for Account related operations.
 *
 * @author Sergii Leschenko
 */
public class AccountManager {

    private final AccountDao accountDao;

    @Inject
    public AccountManager(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    /**
     * Gets account by identifier.
     *
     * @param id
     *         id of account to fetch
     * @return account instance with given id
     * @throws NullPointerException
     *         when {@code id} is null
     * @throws NotFoundException
     *         when account with given {@code id} was not found
     * @throws ServerException
     *         when any other error occurs during account fetching
     */
    public Account getById(String id) throws NotFoundException, ServerException {
        return accountDao.getById(id);
    }

    /**
     * Gets account by name.
     *
     * @param name
     *         name of account to fetch
     * @return account instance with given name
     * @throws NullPointerException
     *         when {@code name} is null
     * @throws NotFoundException
     *         when account with given {@code name} was not found
     * @throws ServerException
     *         when any other error occurs during account fetching
     */
    public Account getByName(String name) throws NotFoundException, ServerException {
        return accountDao.getByName(name);
    }
}
