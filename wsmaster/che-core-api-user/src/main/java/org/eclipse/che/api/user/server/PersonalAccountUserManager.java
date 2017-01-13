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
package org.eclipse.che.api.user.server;

import com.google.inject.persist.Transactional;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author Sergii Leschenko
 */
@Singleton
public class PersonalAccountUserManager extends UserManager {
    private final AccountManager accountManager;

    @Inject
    public PersonalAccountUserManager(UserDao userDao,
                                      ProfileDao profileDao,
                                      PreferenceDao preferencesDao,
                                      @Named("che.auth.reserved_user_names") String[] reservedNames,
                                      AccountManager accountManager) {
        super(userDao, profileDao, preferencesDao, reservedNames);
        this.accountManager = accountManager;
        LoggerFactory.getLogger("Test").info("PersonalAccountUserManager is installed");
    }

    @Transactional(rollbackOn = {NotFoundException.class, ServerException.class, ConflictException.class})
    @Override
    public User create(User newUser, boolean isTemporary) throws ConflictException, ServerException {
        User createdUser = super.create(newUser, isTemporary);

        accountManager.create(new AccountImpl(createdUser.getId(), createdUser.getName(), "personal"));

        return createdUser;
    }

    @Transactional(rollbackOn = {NotFoundException.class, ServerException.class, ConflictException.class})
    @Override
    public void update(User user) throws NotFoundException, ServerException, ConflictException {
        User originalUser = getById(user.getId());

        if (!originalUser.getName().equals(user.getName())) {
            accountManager.update(new AccountImpl(user.getId(), user.getName(), "personal"));
        }

        super.update(user);
    }

    @Transactional(rollbackOn = {NotFoundException.class, ServerException.class, ConflictException.class})
    @Override
    public void remove(String id) throws ServerException, ConflictException {
        super.remove(id);
        accountManager.remove(id);
    }
}
