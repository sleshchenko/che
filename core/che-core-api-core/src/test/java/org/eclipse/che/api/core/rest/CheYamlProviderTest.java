/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.rest;

import static com.jayway.restassured.RestAssured.given;
import static java.util.stream.Collectors.toList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * @author Sergii Leschenko
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class CheYamlProviderTest {

  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private ApiExceptionMapper mapper;

  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private CheJsonProvider jsonProvider = new CheJsonProvider(new HashSet<>());

  private TestService testService = new TestService();

  @Test
  public void shouldCreateOrganization() throws Exception {
    List<Link> links = new ArrayList<>();
    links.add(DtoFactory.newDto(Link.class).withMethod("GET").withHref("/api/test"));
    links.add(DtoFactory.newDto(Link.class).withMethod("POST").withHref("/api/test"));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/yaml")
            .body(links)
            .when()
            .expect()
            .statusCode(201)
            .post(SECURE_PATH + "/test");

    final List<Link> fetchedLinks = unwrapDtoList(response, Link.class);
    assertEquals(fetchedLinks, links);
  }

  private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass) {
    return DtoFactory.getInstance()
        .createListDtoFromYaml(response.body().print(), dtoClass)
        .stream()
        .collect(toList());
  }
}
