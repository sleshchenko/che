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
package org.eclipse.che.api.local;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.local.storage.LocalStorage;
import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.machine.server.recipe.adapters.RecipeTypeAdapter;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

/**
 * In memory based implementation of {@link WorkspaceDao}.
 *
 * <p>{@link #loadWorkspaces() Loads} & {@link #saveWorkspaces() stores} in memory workspaces
 * to/from filesystem, when component starts/stops.
 *
 * @implNote it is thread-safe, guarded by <i>this</i> instance
 *
 * @author Eugene Voevodin
 * @author Dmitry Shnurenko
 *
 */
@Singleton
public class LocalWorkspaceDaoImpl implements WorkspaceDao {

    private final Map<String, WorkspaceImpl> workspaces;
    private final LocalStorage               localStorage;

    @Inject
    public LocalWorkspaceDaoImpl(LocalStorageFactory factory) throws IOException {
        final Map<Class<?>, Object> adapters = ImmutableMap.of(Recipe.class, new RecipeTypeAdapter(),
                                                               ProjectConfig.class, new ProjectConfigAdapter());
        this.localStorage = factory.create("workspaces.json", adapters);
        this.workspaces = new HashMap<>();
    }

    @PostConstruct
    public synchronized void loadWorkspaces() {
        workspaces.putAll(localStorage.loadMap(new TypeToken<Map<String, WorkspaceImpl>>() {}));
    }

    @PreDestroy
    public synchronized void saveWorkspaces() throws IOException {
        localStorage.store(workspaces);
    }

    @Override
    public synchronized WorkspaceImpl create(WorkspaceImpl workspace) throws ConflictException, ServerException {
        if (workspaces.containsKey(workspace.getId())) {
            throw new ConflictException("Workspace with id " + workspace.getId() + " already exists");
        }
        if (find(workspace.getConfig().getName(), workspace.getNamespace()).isPresent()) {
            throw new ConflictException(format("Workspace with name %s and owner %s already exists",
                                               workspace.getConfig().getName(),
                                               workspace.getNamespace()));
        }
        workspace.setStatus(WorkspaceStatus.STOPPED);
        workspaces.put(workspace.getId(), new WorkspaceImpl(workspace));
        return workspace;
    }

    @Override
    public synchronized WorkspaceImpl update(WorkspaceImpl workspace)
            throws NotFoundException, ConflictException, ServerException {
        if (!workspaces.containsKey(workspace.getId())) {
            throw new NotFoundException("Workspace with id " + workspace.getId() + " was not found");
        }
        workspace.setStatus(null);
        workspaces.put(workspace.getId(), new WorkspaceImpl(workspace));
        return workspace;
    }

    @Override
    public synchronized void remove(String id) throws ConflictException, ServerException {
        workspaces.remove(id);
    }

    @Override
    public synchronized WorkspaceImpl get(String id) throws NotFoundException, ServerException {
        final WorkspaceImpl workspace = workspaces.get(id);
        if (workspace == null) {
            throw new NotFoundException("Workspace with id " + id + " was not found");
        }
        return new WorkspaceImpl(workspace);
    }

    @Override
    public synchronized WorkspaceImpl get(String name, String namespace) throws NotFoundException, ServerException {
        final Optional<WorkspaceImpl> wsOpt = find(name, namespace);
        if (!wsOpt.isPresent()) {
            throw new NotFoundException(format("Workspace with name %s and owner %s was not found", name, namespace));
        }
        return new WorkspaceImpl(wsOpt.get());
    }

    @Override
    public synchronized List<WorkspaceImpl> getByNamespace(String namespace) throws ServerException {
        return workspaces.values()
                         .stream()
                         .filter(ws -> ws.getNamespace().equals(namespace))
                         .map(WorkspaceImpl::new)
                         .collect(toList());
    }

    @Override
    public List<WorkspaceImpl> getWorkspaces(String username) throws ServerException {
        return new ArrayList<>(workspaces.values());
    }

    private Optional<WorkspaceImpl> find(String name, String owner) {
        return workspaces.values()
                         .stream()
                         .filter(ws -> ws.getConfig().getName().equals(name) && ws.getNamespace().equals(owner))
                         .findFirst();
    }
}
