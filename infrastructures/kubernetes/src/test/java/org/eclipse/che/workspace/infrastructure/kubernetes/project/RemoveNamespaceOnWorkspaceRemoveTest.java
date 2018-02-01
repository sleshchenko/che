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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.event.WorkspaceRemovedEvent;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link RemoveNamespaceOnWorkspaceRemove}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class RemoveNamespaceOnWorkspaceRemoveTest {

  private static final String WORKSPACE_ID = "workspace123";

  @Mock private Workspace workspace;

  private RemoveNamespaceOnWorkspaceRemove removeProjectOnWorkspaceRemove;

  @BeforeMethod
  public void setUp() throws Exception {
    removeProjectOnWorkspaceRemove = spy(new RemoveNamespaceOnWorkspaceRemove(null, null));

    doNothing().when(removeProjectOnWorkspaceRemove).doRemoveNamespace(anyString());

    when(workspace.getId()).thenReturn(WORKSPACE_ID);
  }

  @Test
  public void shouldSubscribeListenerToEventService() {
    EventService eventService = mock(EventService.class);

    removeProjectOnWorkspaceRemove.subscribe(eventService);

    verify(eventService).subscribe(removeProjectOnWorkspaceRemove);
  }

  @Test
  public void shouldRemoveProjectOnWorkspaceRemovedEvent() throws Exception {
    removeProjectOnWorkspaceRemove.onEvent(new WorkspaceRemovedEvent(workspace));

    verify(removeProjectOnWorkspaceRemove).doRemoveNamespace(WORKSPACE_ID);
  }
}
