package org.eclipse.che.commons.test.tck;

import com.google.inject.Injector;

import javax.persistence.EntityManagerFactory;

/**
 * This class is designed to close {@link EntityManagerFactory}
 * on finish of tck test for jpa implementation.
 *
 * <i>META-INF/services/org.eclipse.che.commons.test.tck.OnFinishTckOperation</i>
 * org.eclipse.che.commons.test.tck.CloseEntityManagerFactoryOperation
 * </pre>
 *
 * @author Sergii Leschenko
 */
public class CloseEntityManagerFactoryOperation implements OnFinishTckOperation {
    @Override
    public void onFinish(Injector injector) {
        injector.getInstance(EntityManagerFactory.class).close();
    }
}
