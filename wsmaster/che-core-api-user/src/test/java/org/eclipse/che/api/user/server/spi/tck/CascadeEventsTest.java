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
package org.eclipse.che.api.user.server.spi.tck;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.Constants;
import org.eclipse.che.api.user.server.event.PostUserPersistedEvent;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.core.db.event.CascadeEventSubscriber;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Tests {@link UserDao} contract.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(TckListener.class)
@Test(suiteName = CascadeEventsTest.SUITE_NAME)
public class CascadeEventsTest {

    public static final String SUITE_NAME = "UserDaoTck";

    private static final int COUNT_OF_USERS = 5;

    private UserImpl user;

    @Inject
    private UserDao userDao;

    @Inject
    private EventService eventService;

    @Inject
    private TckRepository<UserImpl>    userRepo;
    @Inject
    private TckRepository<ProfileImpl> profileRepo;

    @Inject
    private MyClass myClass;

    @BeforeMethod
    public void setUp() throws TckRepositoryException {
        final String id = "userok";
        final String name = "user_name-";
        final String email = name + "@eclipse.org";
        final String password = NameGenerator.generate("", Constants.PASSWORD_LENGTH);
        final List<String> aliases = new ArrayList<>(asList("google:" + name, "github:" + name));
        user = new UserImpl(id, email, name, password, aliases);

        eventService.subscribe(myClass, PostUserPersistedEvent.class);
    }

    @AfterMethod
    public void cleanUp() throws TckRepositoryException {
        eventService.unsubscribe(myClass, PostUserPersistedEvent.class);
        userRepo.removeAll();
        profileRepo.removeAll();
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Profile for user with id 'userok' already exists")
    public void shouldGetUserByNameAndPassword() throws Exception {
        userDao.create(user);
    }

    public static class MyClass extends CascadeEventSubscriber<PostUserPersistedEvent> {

        @Inject
        ProfileDao profileDao;

        @Override
        public void onCascadeEvent(PostUserPersistedEvent event) throws Exception {
            profileDao.create(new ProfileImpl(event.getUser().getId(),
                                              new HashMap<>()));
            profileDao.create(new ProfileImpl(event.getUser().getId(),
                                              new HashMap<>()));
        }
    }
}
