package org.eclipse.che.commons.test.tck;

import com.google.inject.Injector;

/**
 * This class is designed to clean up resources after tck test.
 *
 * <i>META-INF/services/org.eclipse.che.commons.test.tck.OnFinishTckOperation</i>
 * org.eclipse.che.commons.test.tck.DoSomethingOperation
 * </pre>
 *
 * @author Sergii Leschenko
 */
public interface OnFinishTckOperation {
    void onFinish(Injector injector);
}
