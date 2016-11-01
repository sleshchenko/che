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

import java.util.Map;

/**
 * This class is designed to clean up resources after tck test.
 *
 * <p>Implementation should be defined in guice module.
 * It can be defined common for all tests or test specific by using @Named annotation.
 *
 * @author Sergii Leschenko
 */
public interface TckResourcesCleaner {
    void onFinish(Map<String, Object> attributes);
}
