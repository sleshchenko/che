package org.eclipse.che.multiuser.resource.api.usage.tracker;

import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sergii Leshchenko
 */
public class DevfileRamCalculator {
  private static final Logger LOG = LoggerFactory.getLogger(DevfileRamCalculator.class);

  private final PluginRamCalculator pluginRamCalculator;
  private final KubernetesRamCalculator k8sRamCalculator;

  @Inject
  public DevfileRamCalculator(
      PluginRamCalculator pluginRamCalculator,
      KubernetesRamCalculator k8sRamCalculator) {
    this.pluginRamCalculator = pluginRamCalculator;
    this.k8sRamCalculator = k8sRamCalculator;
  }

  public long calculate(Devfile devfile) {
    long devfileRam = 0;
    for (Component component : devfile.getComponents()) {
      switch (component.getType()) {
        case "dockerimage":
          devfileRam += KubernetesSize.toBytes(component.getMemoryLimit());
          break;

        case "kubernetes":
        case "openshift":
          devfileRam += k8sRamCalculator.calculate(component);
          break;

        case "chePlugin":
        case "cheEditor":
          devfileRam += pluginRamCalculator.calculate(component);
          break;

        default:
          LOG.error("Unknown component type is used. It's memory is not taken into account.");
      }
    }
    return devfileRam;
  }
}
