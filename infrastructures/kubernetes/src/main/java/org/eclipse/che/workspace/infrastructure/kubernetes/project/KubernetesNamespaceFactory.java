/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.project;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;

/**
 * Helps to create {@link KubernetesNamespace} instances.
 *
 * @author Anton Korneta
 */
@Singleton
public class KubernetesNamespaceFactory {

  private final String projectName;
  private final KubernetesClientFactory clientFactory;

  @Inject
  public KubernetesNamespaceFactory(
      @Nullable @Named("che.infra.kubernetes.namespace") String projectName,
      KubernetesClientFactory clientFactory) {
    this.projectName = projectName;
    this.clientFactory = clientFactory;
  }

  public KubernetesNamespace create(String workspaceId) throws InfrastructureException {
    final String projectName = isNullOrEmpty(this.projectName) ? workspaceId : this.projectName;
    return new KubernetesNamespace(clientFactory, projectName, workspaceId);
  }
}
