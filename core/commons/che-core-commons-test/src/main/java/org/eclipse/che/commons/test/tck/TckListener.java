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

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;

import org.testng.IClassListener;
import org.testng.ITestClass;
import org.testng.ITestNGListener;
import org.testng.annotations.Listeners;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import static java.lang.String.format;

/**
 * The listener is designed to instantiate {@link TckModule tck modules}
 * using {@link ServiceLoader} mechanism. The components
 * provided by those modules will be injected into a test class
 * whether it's necessary to do so.
 *
 * <p>The listener expects at least one implementation of {@code TckModule}
 * to be configured, if it doesn't find any of the {@code TckModule}
 * implementations then it will report an appropriate exception
 * and TckTest will fail(as it requires components to be injected into it).
 * If it finds more than one {@code TckModule} implementation it will
 * use all of the found.
 *
 * <p>The usage example:
 * <pre>
 * package org.eclipse.mycomponent;
 *
 * &#064;org.testng.annotations.Listeners(TckListener)
 * // Good practice to define suiteName for TCK tests as it makes easier
 * // to implement logic related to certain tests in ITestNGListener implementations
 * &#064;org.testng.annotations.Test(suiteName = "MySuite")
 * class SubjectTest {
 *
 *     &#064;javax.inject.Inject
 *     private Component1 component1.
 *     &#064;javax.inject.Inject
 *     private Component2 component2;
 *
 *     &#064;org.testng.annotations.Test
 *     public void test() {
 *          // use components
 *     }
 * }
 *
 * class MyTckModule extends TckModule {
 *     public void configure() {
 *         bind(Component1.class).to(...);
 *         bind(Component2.class).toInstance(new Component2(() -> testContext.getAttribute("server_url").toString()));
 *     }
 * }
 *
 * // Allows to add pre/post test actions like db server start/stop
 * class DBServerListener implements ITestListener {
 *      // ...
 *      public void onStart(ITestContext context) {
 *          String url = dbServer.start();
 *          context.setAttribute("server_url", url)l
 *      }
 *
 *      public void onFinish(ITestContext context) {
 *          dbServer.stop();
 *      }
 *      // ...
 * }
 * </pre>
 *
 * <p>Configuring:
 * <pre>
 * <i>META-INF/services/org.eclipse.che.commons.test.tck.TckModule</i>
 * org.eclipse.mycomponent.MyTckModule
 *
 * <i>META-INF/services/org.testng.ITestNGListener</i>
 * org.eclipse.mycomponent.DBServerListener
 * </pre>
 *
 * @author Yevhenii Voevodin
 * @author Sergii Leschenko
 * @see org.testng.annotations.Listeners
 * @see org.testng.IInvokedMethodListener
 */
public class TckListener implements IClassListener {
    public static final String CLASS_INJECTOR_SUFFIX = "Injector";

    private final Map<String, Injector> injectors = new HashMap<>();

    @Override
    public void onBeforeClass(ITestClass testClass) {
        if (hasTckListenerAnnotation(testClass.getRealClass())) {
            final String name = testClass.getRealClass().getName();
            synchronized (injectors) {
                if (!injectors.containsKey(name)) {
                    final Injector injector = Guice.createInjector(createModule(name));
                    for (Object instance : testClass.getInstances(false)) {
                        injector.injectMembers(instance);
                    }
                    injectors.put(name, injector);
                }
            }
        }
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
        synchronized (injectors) {
            for (Injector injector : injectors.values()) {
                final String className = testClass.getRealClass().getName();
                TckResourcesCleaner cleaner;
                try {
                    // try to get test specific cleaner
                    cleaner = injector.getInstance(Key.get(TckResourcesCleaner.class,
                                                           Names.named(className)));
                } catch (ConfigurationException e) {
                    // try to get common cleaner
                    cleaner = injector.getInstance(TckResourcesCleaner.class);
                }
                cleaner.onFinish(testClass, ImmutableMap.of(className + CLASS_INJECTOR_SUFFIX, injector));
            }
            injectors.clear();
        }
    }

    private Module createModule(String name) {
        final Iterator<TckModule> moduleIterator = ServiceLoader.load(TckModule.class).iterator();
        if (!moduleIterator.hasNext()) {
            throw new IllegalStateException(format("Couldn't find a TckModule configuration. " +
                                                   "You probably forgot to configure resources/META-INF/services/%s, or even " +
                                                   "provide an implementation of the TckModule which is required by the tck test class %s",
                                                   TckModule.class.getName(),
                                                   name));
        }
        return new CompoundModule(moduleIterator);
    }

    private boolean hasTckListenerAnnotation(Class<?> clazz) {
        Listeners listeners = clazz.getAnnotation(Listeners.class);
        if (listeners == null) {
            return false;
        }

        for (Class<? extends ITestNGListener> listenerClass : listeners.value()) {
            if (TckListener.class == listenerClass) {
                return true;
            }
        }
        return false;
    }

    private static class CompoundModule extends AbstractModule {
        private final Iterator<TckModule> moduleIterator;

        private CompoundModule(Iterator<TckModule> moduleIterator) {
            this.moduleIterator = moduleIterator;
        }

        @Override
        protected void configure() {
            while (moduleIterator.hasNext()) {
                final TckModule module = moduleIterator.next();
                install(module);
            }
        }
    }
}
