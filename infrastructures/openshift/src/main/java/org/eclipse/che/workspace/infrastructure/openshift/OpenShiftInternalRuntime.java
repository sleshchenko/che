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
package org.eclipse.che.workspace.infrastructure.openshift;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.assistedinject.Assisted;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.Route;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.URLRewriter.NoOpURLRewriter;
import org.eclipse.che.api.workspace.server.hc.ServersCheckerFactory;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeScheduler;
import org.eclipse.che.api.workspace.server.hc.probe.WorkspaceProbesFactory;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInternalRuntime;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesMachine;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.bootstrapper.KubernetesBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.project.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;
import org.eclipse.che.workspace.infrastructure.openshift.server.OpenShiftServerResolver;

/**
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
public class OpenShiftInternalRuntime extends KubernetesInternalRuntime<OpenShiftRuntimeContext> {

  private final OpenShiftProject project;

  @Inject
  public OpenShiftInternalRuntime(
      @Named("che.infra.kubernetes.machine_start_timeout_min") int machineStartTimeoutMin,
      NoOpURLRewriter urlRewriter,
      EventService eventService,
      KubernetesBootstrapperFactory bootstrapperFactory,
      ServersCheckerFactory serverCheckerFactory,
      WorkspaceVolumesStrategy volumesStrategy,
      ProbeScheduler probeScheduler,
      WorkspaceProbesFactory probesFactory,
      @Assisted OpenShiftRuntimeContext context,
      @Assisted OpenShiftProject project,
      @Assisted List<Warning> warnings) {
    super(
        machineStartTimeoutMin,
        urlRewriter,
        eventService,
        bootstrapperFactory,
        serverCheckerFactory,
        volumesStrategy,
        probeScheduler,
        probesFactory,
        context,
        project,
        warnings);
    this.project = project;
  }

  @Override
  protected void createMachines() throws InfrastructureException {
    OpenShiftEnvironment osEnv = getContext().getEnvironment();
    List<Service> createdServices = new ArrayList<>();
    for (Service service : osEnv.getServices().values()) {
      createdServices.add(project.services().create(service));
    }

    List<Route> createdRoutes = new ArrayList<>();
    for (Route route : osEnv.getRoutes().values()) {
      createdRoutes.add(project.routes().create(route));
    }
    // TODO https://github.com/eclipse/che/issues/7653
    // project.pods().watch(new AbnormalStopHandler());
    // project.pods().watchContainers(new MachineLogsPublisher());

    createPods(createdServices, createdRoutes);
  }

  /**
   * Creates OpenShift pods and resolves machine servers based on routes and services.
   *
   * @param services created OpenShift services
   * @param routes created OpenShift routes
   * @throws InfrastructureException when any error occurs while creating OpenShift pods
   */
  @VisibleForTesting
  void createPods(List<Service> services, List<Route> routes) throws InfrastructureException {
    final OpenShiftServerResolver serverResolver = new OpenShiftServerResolver(services, routes);
    final OpenShiftEnvironment environment = getContext().getEnvironment();
    final Map<String, InternalMachineConfig> machineConfigs = environment.getMachines();
    for (Pod toCreate : environment.getPods().values()) {
      final Pod createdPod = project.pods().create(toCreate);
      final ObjectMeta podMetadata = createdPod.getMetadata();
      for (Container container : createdPod.getSpec().getContainers()) {
        String machineName = Names.machineName(toCreate, container);
        KubernetesMachine machine =
            new KubernetesMachine(
                machineName,
                podMetadata.getName(),
                container.getName(),
                serverResolver.resolve(machineName),
                project,
                MachineStatus.STARTING,
                machineConfigs.get(machineName).getAttributes());
        machines.put(machine.getName(), machine);
        sendStartingEvent(machine.getName());
      }
    }
  }
}
