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
package org.eclipse.che.commons.test.tck;

import com.google.inject.Injector;

import org.testng.ITestClass;

import javax.persistence.EntityManagerFactory;
import java.util.Map;

/**
 * This class is designed to close {@link EntityManagerFactory}
 * on finish of tck test for jpa implementation.
 *
 * <p/>
 * Examples of usage:<br>
 * <code>bind(TckResourcesCleaner.class).to(JpaCleaner.class)</code><br>
 * <code>bind(TckResourcesCleaner.class).annotatedWith(Names.named(MyTckTest.class.getName())).to(JpaCleaner.class);</code>
 *
 * @author Sergii Leschenko
 */
public class JpaCleaner implements TckResourcesCleaner {
    @Override
    public void onFinish(ITestClass testClass, Map<String, Object> attributes) {
        final Injector injector = (Injector)attributes.get(testClass.getRealClass().getName() + TckListener.CLASS_INJECTOR_SUFFIX);
        injector.getInstance(EntityManagerFactory.class).close();
    }
}
