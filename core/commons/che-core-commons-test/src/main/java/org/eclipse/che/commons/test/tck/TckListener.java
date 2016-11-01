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

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.Listeners;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import static java.lang.String.format;

/**
 * The listener is designed to instantiate {@link TckModule tck modules}
 * using {@link ServiceLoader} mechanism. The components  provided by those
 * modules will be injected into a test class whether it's necessary to do so.
 * For each test class will be used new instance of injector.
 * After test suite is finished listener'll try to find test specific or common
 * instance of {@link TckResourcesCleaner}. It is optional and can be bound in modules.
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
 *         bind(TckResourcesCleaner.class).to(...);
 *         bind(TckResourcesCleaner.class).annotatedWith(Names.named(SubjectTest.class.getName())).to(...);
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
 * @see TckResourcesCleaner
 */
public class TckListener extends AbstractTestListener implements IInvokedMethodListener {
    public static final String CLASS_INJECTOR_PROPERTY = "Injector";

    private final Map<String, Injector> injectors = new HashMap<>();

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        final ITestNGMethod testClass = method.getTestMethod();
        if (hasTckListenerAnnotation(testClass.getRealClass())) {
            final String name = testClass.getRealClass().getName();
            synchronized (injectors) {
                if (!injectors.containsKey(name)) {
                    final Injector injector = Guice.createInjector(createModule(testResult.getTestContext(), name));
                    injector.injectMembers(testClass.getInstance());
                    injectors.put(name, injector);
                }
            }
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    }

    @Override
    public void onFinish(ITestContext context) {
        // using of onFinish method for resources cleaning will work incorrect
        // in case when two tck tests which should clean resources are in one suite
        // it's because onFinish will be invoked on finish of all tests in suite
        // but resources should be clean after each test
        // TODO Rework it to use IClassListener to avoid described problem
        for (Map.Entry<String, Injector> className2Injector : injectors.entrySet()) {
            final Injector injector = className2Injector.getValue();
            final String className = className2Injector.getKey();
            //try to get test specific cleaner
            TckResourcesCleaner cleaner = getResourcesCleaner(injector,
                                                              Key.get(TckResourcesCleaner.class,
                                                                      Names.named(className)));
            if (cleaner == null) {
                //try to get common cleaner
                cleaner = getResourcesCleaner(injector, Key.get(TckResourcesCleaner.class));
            }

            if (cleaner != null) {
                cleaner.onFinish(ImmutableMap.of(CLASS_INJECTOR_PROPERTY, injector));
            }
        }
    }

    private TckResourcesCleaner getResourcesCleaner(Injector injector, Key<TckResourcesCleaner> key) {
        try {
            return injector.getInstance(key);
        } catch (ConfigurationException ignored) {
        }
        return null;
    }

    private Module createModule(ITestContext testContext, String name) {
        final Iterator<TckModule> moduleIterator = ServiceLoader.load(TckModule.class).iterator();
        if (!moduleIterator.hasNext()) {
            throw new IllegalStateException(format("Couldn't find a TckModule configuration. " +
                                                   "You probably forgot to configure resources/META-INF/services/%s, or even " +
                                                   "provide an implementation of the TckModule which is required by the tck test class %s",
                                                   TckModule.class.getName(),
                                                   name));
        }
        return new CompoundModule(testContext, moduleIterator);
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
        private final ITestContext        testContext;
        private final Iterator<TckModule> moduleIterator;

        private CompoundModule(ITestContext testContext, Iterator<TckModule> moduleIterator) {
            this.testContext = testContext;
            this.moduleIterator = moduleIterator;
        }

        @Override
        protected void configure() {
            bind(ITestContext.class).toInstance(testContext);
            while (moduleIterator.hasNext()) {
                final TckModule module = moduleIterator.next();
                module.setTestContext(testContext);
                install(module);
            }
        }
    }
}
