package org.eclipse.che.multiuser.resource.api.usage.tracker;

import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;

/**
 * @author Sergii Leshchenko
 */
public class PluginRamCalculator {
  private final PluginMetaRetriever metaRetriever;

  @Inject
  public PluginRamCalculator(PluginMetaRetriever metaRetriever) {
    this.metaRetriever = metaRetriever;
  }

  public long calculate(Component component) {
    ChePlugin retrieved = metaRetriever.retrieve(component);
    return 0;
  }
}
