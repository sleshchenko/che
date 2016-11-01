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
package org.eclipse.che.api.machine.server.jpa;

import com.google.inject.TypeLiteral;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.core.jdbc.jpa.eclipselink.EntityListenerInjectionManagerInitializer;
import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.commons.test.tck.JpaCleaner;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;

/**
 * @author Anton Korneta
 */
public class JpaTckModule extends TckModule {

    @Override
    protected void configure() {
        install(new JpaPersistModule("main"));
        bind(JpaInitializer.class).asEagerSingleton();

        bind(TckResourcesCleaner.class).to(JpaCleaner.class);

        bind(EntityListenerInjectionManagerInitializer.class).asEagerSingleton();

        bind(new TypeLiteral<TckRepository<RecipeImpl>>() {}).toInstance(new JpaTckRepository<>(RecipeImpl.class));
        bind(new TypeLiteral<TckRepository<SnapshotImpl>>() {}).toInstance(new JpaTckRepository<>(SnapshotImpl.class));

        bind(RecipeDao.class).to(JpaRecipeDao.class);
        bind(SnapshotDao.class).to(JpaSnapshotDao.class);
    }
}
