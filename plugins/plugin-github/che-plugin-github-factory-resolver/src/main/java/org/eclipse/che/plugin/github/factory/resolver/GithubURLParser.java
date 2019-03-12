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
package org.eclipse.che.plugin.github.factory.resolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.factory.server.urlfactory.URLFetcher;

/**
 * Parser of String Github URLs and provide {@link GithubUrl} objects.
 *
 * @author Florent Benoit
 */
@Singleton
public class GithubURLParser {

  /** Fetcher to grab PR data */
  @Inject private URLFetcher urlFetcher;

  /** Regexp to match protocol `http(s)://` */
  private static final String PROTOCOL_PATTERN = "(?:http)(?:s)?(?:\\:\\/\\/)";
  /** Regexp to match user which should be the first segment */
  private static final String USER_PATTERN = "(?<repoUser>[^/]++)";
  /** Regexp to match repo which should be the first segment */
  private static final String REPO_NAME = "(?<repoName>[^/]++)";
  /**
   * Regexp to match pull request ID, like in the following URL
   * https://github.com/eclipse/che/pull/12860
   */
  private static final String PULL_PULL_REQUEST_ID = "(/pull/(?<pullRequestId>[^/]++))";
  /**
   * Regexp to match repo path which includes branch name and subfolder, like in the following URL
   * https://github.com/eclipse/che/tree/master/deploy/kubernetes
   */
  private static final String REPO_PATH = "(?:/tree/(?<branchName>[^/]++)(?:/(?<subFolder>.*))?)";

  /**
   * Regexp to find repository details (repository name, project name and branch and subfolder)
   * Examples of valid URLs are in the test class.
   */
  protected static final Pattern GITHUB_PATTERN =
      Pattern.compile(
          "^"
              + PROTOCOL_PATTERN
              + "github.com/"
              + USER_PATTERN
              + "/"
              + REPO_NAME
              + "((/)|"
              + REPO_PATH
              + "|"
              + PULL_PULL_REQUEST_ID
              + ")?$");

  /** Regexp to find repository and branch name from PR link */
  protected static final Pattern PR_DATA_PATTERN =
      Pattern.compile(
          ".*<div class=\"State[\\s|\\S]+(?<prState>Closed|Open|Merged)[\\s|\\S]+<\\/div>[\\s|\\S]+into[\\s]+(from[\\s]*)*<span title=\"(?<prRepoUser>[^\\\\/]+)\\/(?<prRepoName>[^\\:]+):(?<prBranch>[^\\\"]+).*",
          Pattern.DOTALL);

  public boolean isValid(@NotNull String url) {
    return GITHUB_PATTERN.matcher(url).matches();
  }

  public GithubUrl parse(String url) {
    // Apply github url to the regexp
    Matcher matcher = GITHUB_PATTERN.matcher(url);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          String.format(
              "The given github url %s is not a valid URL github url. It should start with https://github.com/<user>/<repo>",
              url));
    }

    String repoUser = matcher.group("repoUser");
    String repoName = matcher.group("repoName");
    String branchName = matcher.group("branchName");

    String pullRequestId = matcher.group("pullRequestId");
    if (pullRequestId != null) {
      // there is a Pull Request ID, analyze content to extract repository and branch to use
      String prData = this.urlFetcher.fetchSafely(url);
      Matcher prMatcher = PR_DATA_PATTERN.matcher(prData);
      if (prMatcher.matches()) {
        String prState = prMatcher.group("prState");
        if (!"open".equalsIgnoreCase(prState)) {
          throw new IllegalArgumentException(
              String.format(
                  "The given Pull Request url %s is not Opened, (found %s), thus it can't be opened as branch may have been removed.",
                  url, prState));
        }
        repoUser = prMatcher.group("prRepoUser");
        repoName = prMatcher.group("prRepoName");
        branchName = prMatcher.group("prBranch");
      } else {
        throw new IllegalArgumentException(
            String.format(
                "The given Pull Request github url %s is not a valid Pull Request URL github url. Unable to extract the data",
                url));
      }
    }

    return new GithubUrl()
        .withUsername(repoUser)
        .withRepository(repoName)
        .withBranch(branchName)
        .withSubfolder(matcher.group("subFolder"))
        .withDevfileFilename(".devfile")
        .withFactoryFilename(".factory.json");
  }
}
