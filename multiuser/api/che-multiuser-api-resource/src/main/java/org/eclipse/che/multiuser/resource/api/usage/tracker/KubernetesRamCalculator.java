package org.eclipse.che.multiuser.resource.api.usage.tracker;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import java.util.List;
import javax.inject.Named;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.lang.Size;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;

/**
 * @author Sergii Leshchenko
 */
public class KubernetesRamCalculator {

  private final long defaultContainerMemoryBytes;
  private KubernetesRecipeParser k8sRecipeParser;

  public KubernetesRamCalculator(
      @Named("che.workspace.default_memory_limit_mb") long defaultContainerMemoryMb,
      KubernetesRecipeParser k8sRecipeParser) {
    this.defaultContainerMemoryBytes = defaultContainerMemoryMb * 1024 * 1024;
    this.k8sRecipeParser = k8sRecipeParser;
  }

  public long calculate(Component component) {
    long ram = 0;
    try {
      List<HasMetadata> parsed = k8sRecipeParser.parse(component.getReferenceContent());
      for (HasMetadata hasMetadata : parsed) {
        if (hasMetadata instanceof Deployment) {
          Deployment deployment = (Deployment) hasMetadata;
          // T_O_D_O Create an issue for init containers
          // init containers should not influence of memory that is used by workspace
          // at the same time it would be nicely to check that memory of init containers is less than
          // memory of containers
          // it's not clear what to do if init containers memory is greater.
          // Deny workspace? Occupy ram temporary until it's starting?
          List<Container> containers = deployment.getSpec().getTemplate().getSpec().getContainers();
          ram += getRam(containers);
        } else if (hasMetadata instanceof Pod) {
          ram += getRam(((Pod) hasMetadata).getSpec().getContainers());
        } else {
          //T_O_D_O Think more about other types that can contain containers
        }
      }
    } catch (ValidationException | InfrastructureException e) {
      e.printStackTrace(); //T_O_D_O
    }
    return ram;
  }

  private long getRam(List<Container> containers) {
    long ram = 0;
    for (Container container : containers) {
      long ramLimit = Containers.getRamLimit(container);
      if (ramLimit > 0) {
        ram += ramLimit;
      } else {
        ram += defaultContainerMemoryBytes;
      }
    }
    return ram;
  }
}
