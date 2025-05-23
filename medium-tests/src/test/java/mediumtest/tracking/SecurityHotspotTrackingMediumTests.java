/*
 * SonarLint Core - Medium Tests
 * Copyright (C) 2016-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package mediumtest.tracking;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.sonarsource.sonarlint.core.commons.HotspotReviewStatus;
import org.sonarsource.sonarlint.core.commons.VulnerabilityProbability;
import org.sonarsource.sonarlint.core.commons.api.TextRange;
import org.sonarsource.sonarlint.core.rpc.client.SonarLintRpcClientDelegate;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.analysis.AnalyzeFilesAndTrackParams;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.config.binding.BindingConfigurationDto;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.config.scope.ConfigurationScopeDto;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.config.scope.DidAddConfigurationScopesParams;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.hotspot.HotspotStatus;
import org.sonarsource.sonarlint.core.rpc.protocol.client.hotspot.RaisedHotspotDto;
import org.sonarsource.sonarlint.core.rpc.protocol.common.ClientFileDto;
import org.sonarsource.sonarlint.core.rpc.protocol.common.IssueSeverity;
import org.sonarsource.sonarlint.core.rpc.protocol.common.RuleType;
import org.sonarsource.sonarlint.core.test.utils.SonarLintTestRpcServer;
import org.sonarsource.sonarlint.core.test.utils.junit5.SonarLintTest;
import org.sonarsource.sonarlint.core.test.utils.junit5.SonarLintTestHarness;
import utils.TestPlugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.sonarsource.sonarlint.core.rpc.protocol.backend.initialize.BackendCapability.SECURITY_HOTSPOTS;

class SecurityHotspotTrackingMediumTests {

  private static final String CONFIG_SCOPE_ID = "CONFIG_SCOPE_ID";

  @SonarLintTest
  void it_should_track_server_hotspot(SonarLintTestHarness harness, @TempDir Path baseDir) {
    var ideFilePath = "Foo.java";
    var filePath = createFile(baseDir, ideFilePath,
      """
        package sonar;
        
        public class Foo {
          public void run() {
            String username = "steve";
            String password = "blue";
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/test?" +
              "user=" + username + "&password=" + password); // Sensitive
          }
        }""");
    var projectKey = "projectKey";
    var connectionId = "connectionId";
    var branchName = "main";
    var ruleKey = "java:S2068";
    var message = "'password' detected in this expression, review this potentially hard-coded password.";

    var fileUri = filePath.toUri();
    var client = harness.newFakeClient()
      .withInitialFs(CONFIG_SCOPE_ID, baseDir, List.of(new ClientFileDto(fileUri, baseDir.relativize(filePath), CONFIG_SCOPE_ID, false, null, filePath, null, null, true)))
      .build();
    var server = harness.newFakeSonarQubeServer("10.0")
      .withProject(projectKey, project -> project.withBranch(branchName, branch -> branch
        .withHotspot("uuid", hotspot -> hotspot.withAuthor("author")
          .withCreationDate(Instant.ofEpochSecond(123456789L))
          .withFilePath(ideFilePath)
          .withMessage(message)
          .withRuleKey(ruleKey)
          .withTextRange(new TextRange(6, 11, 6, 12))
          .withStatus(HotspotReviewStatus.TO_REVIEW)
          .withVulnerabilityProbability(VulnerabilityProbability.HIGH))))
      .withQualityProfile("qp", qualityProfile -> qualityProfile.withLanguage("java")
        .withActiveRule(ruleKey, activeRule -> activeRule.withSeverity(IssueSeverity.MAJOR)))
      .start();
    var backend = harness.newBackend()
      .withSonarQubeConnection(connectionId, server,
        storage -> storage.withPlugin(TestPlugin.JAVA).withProject(projectKey,
          project -> project.withRuleSet("java", ruleSet -> ruleSet.withActiveRule(ruleKey, "MINOR"))
            .withMainBranch(branchName)))
      .withBackendCapability(SECURITY_HOTSPOTS)
      .withConnectedEmbeddedPluginAndEnabledLanguage(TestPlugin.JAVA)
      .start(client);
    backend.getConfigurationService()
      .didAddConfigurationScopes(new DidAddConfigurationScopesParams(List.of(
        new ConfigurationScopeDto(CONFIG_SCOPE_ID, null, true, CONFIG_SCOPE_ID,
          new BindingConfigurationDto(connectionId, projectKey, true)))));

    var firstPublishedHotspot = analyzeFileAndGetHotspot(backend, fileUri, client);

    assertThat(firstPublishedHotspot)
      .extracting("ruleKey", "primaryMessage", "severityMode.left.severity", "severityMode.left.type", "serverKey", "status", "introductionDate",
        "textRange.startLine", "textRange.startLineOffset", "textRange.endLine", "textRange.endLineOffset")
      .containsExactly(ruleKey, message, IssueSeverity.MINOR, RuleType.SECURITY_HOTSPOT, "uuid", HotspotStatus.TO_REVIEW, Instant.ofEpochSecond(123456789L), 6, 11, 6, 19);
  }

  @SonarLintTest
  void it_should_track_known_server_hotspots(SonarLintTestHarness harness, @TempDir Path baseDir) {
    var ideFilePath = "Foo.java";
    var filePath = createFile(baseDir, ideFilePath,
      """
        package sonar;
        
        public class Foo {
          public void run() {
            String username = "steve";
            String password = "blue";
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/test?" +
              "user=" + username + "&password=" + password); // Sensitive
          }
        }""");
    var projectKey = "projectKey";
    var connectionId = "connectionId";
    var branchName = "main";
    var ruleKey = "java:S2068";
    var message = "'password' detected in this expression, review this potentially hard-coded password.";

    var fileUri = filePath.toUri();
    var client = harness.newFakeClient()
      .withInitialFs(CONFIG_SCOPE_ID, baseDir, List.of(new ClientFileDto(fileUri, baseDir.relativize(filePath), CONFIG_SCOPE_ID, false, null, filePath, null, null, true)))
      .build();
    var server = harness.newFakeSonarQubeServer("10.0")
      .withProject(projectKey, project -> project.withBranch(branchName, branch -> branch
        .withHotspot("uuid", hotspot -> hotspot.withAuthor("author")
          .withCreationDate(Instant.ofEpochSecond(123456789L))
          .withFilePath(ideFilePath)
          .withMessage(message)
          .withRuleKey(ruleKey)
          .withTextRange(new TextRange(6, 11, 6, 12))
          .withStatus(HotspotReviewStatus.TO_REVIEW)
          .withVulnerabilityProbability(VulnerabilityProbability.HIGH))))
      .withQualityProfile("qp", qualityProfile -> qualityProfile.withLanguage("java")
        .withActiveRule(ruleKey, activeRule -> activeRule.withSeverity(IssueSeverity.MAJOR)))
      .start();
    var backend = harness.newBackend()
      .withSonarQubeConnection(connectionId, server,
        storage -> storage.withPlugin(TestPlugin.JAVA).withProject(projectKey,
          project -> project.withRuleSet("java", ruleSet -> ruleSet.withActiveRule(ruleKey, "MINOR"))
            .withMainBranch(branchName)))
      .withBackendCapability(SECURITY_HOTSPOTS)
      .withConnectedEmbeddedPluginAndEnabledLanguage(TestPlugin.JAVA)
      .start(client);
    backend.getConfigurationService()
      .didAddConfigurationScopes(new DidAddConfigurationScopesParams(List.of(
        new ConfigurationScopeDto(CONFIG_SCOPE_ID, null, true, CONFIG_SCOPE_ID,
          new BindingConfigurationDto(connectionId, projectKey, true)))));

    var firstPublishedHotspot = analyzeFileAndGetHotspot(backend, fileUri, client);
    var secondPublishedHotspot = analyzeFileAndGetHotspot(backend, fileUri, client);

    assertThat(secondPublishedHotspot)
      .extracting("id", "ruleKey", "primaryMessage", "severityMode.left.severity", "severityMode.left.type", "serverKey", "introductionDate",
        "textRange.startLine", "textRange.startLineOffset", "textRange.endLine", "textRange.endLineOffset")
      .containsExactly(firstPublishedHotspot.getId(), ruleKey, message, IssueSeverity.MINOR, RuleType.SECURITY_HOTSPOT, "uuid", Instant.ofEpochSecond(123456789L), 6, 11, 6, 19);
  }

  @SonarLintTest
  void it_should_not_track_server_hotspots_in_standalone_mode(SonarLintTestHarness harness, @TempDir Path baseDir) {
    var ideFilePath = "Foo.java";
    var filePath = createFile(baseDir, ideFilePath,
      """
        package sonar;
        
        public class Foo {
          public void run() {
            String username = "steve";
            String password = "blue";
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/test?" +
              "user=" + username + "&password=" + password); // Sensitive
          }
        }""");
    var projectKey = "projectKey";
    var connectionId = "connectionId";
    var branchName = "main";
    var ruleKey = "java:S2068";

    var fileUri = filePath.toUri();
    var client = harness.newFakeClient()
      .withInitialFs(CONFIG_SCOPE_ID, baseDir, List.of(new ClientFileDto(fileUri, baseDir.relativize(filePath), CONFIG_SCOPE_ID, false, null, filePath, null, null, true)))
      .build();
    var backend = harness.newBackend()
      .withSonarQubeConnection(connectionId,
        storage -> storage.withPlugin(TestPlugin.JAVA).withProject(projectKey,
          project -> project.withRuleSet("java", ruleSet -> ruleSet.withActiveRule(ruleKey, "MINOR"))
            .withMainBranch(branchName)))
      .withBackendCapability(SECURITY_HOTSPOTS)
      .withUnboundConfigScope(CONFIG_SCOPE_ID)
      .withStandaloneEmbeddedPluginAndEnabledLanguage(TestPlugin.JAVA)
      .start(client);

    analyzeFileAndAssertNoHotspotsRaised(backend, fileUri, client);
  }

  private RaisedHotspotDto analyzeFileAndGetHotspot(SonarLintTestRpcServer backend, URI fileUri, SonarLintRpcClientDelegate client) {
    var analysisId = UUID.randomUUID();
    var analysisResult = backend.getAnalysisService().analyzeFilesAndTrack(
      new AnalyzeFilesAndTrackParams(CONFIG_SCOPE_ID, analysisId, List.of(fileUri), Map.of(), true, System.currentTimeMillis()))
      .join();
    var publishedHotspotsByFile = getPublishedHotspots(client, analysisId);
    assertThat(analysisResult.getFailedAnalysisFiles()).isEmpty();
    assertThat(publishedHotspotsByFile).containsOnlyKeys(fileUri);
    var publishedHotspots = publishedHotspotsByFile.get(fileUri);
    assertThat(publishedHotspots).hasSize(1);
    return publishedHotspots.get(0);
  }

  private Map<URI, List<RaisedHotspotDto>> getPublishedHotspots(SonarLintRpcClientDelegate client, UUID analysisId) {
    ArgumentCaptor<Map<URI, List<RaisedHotspotDto>>> trackedIssuesCaptor = ArgumentCaptor.forClass(Map.class);
    verify(client, timeout(300)).raiseHotspots(eq(CONFIG_SCOPE_ID), trackedIssuesCaptor.capture(), eq(false), eq(analysisId));
    return trackedIssuesCaptor.getValue();
  }

  private void analyzeFileAndAssertNoHotspotsRaised(SonarLintTestRpcServer backend, URI fileUri, SonarLintRpcClientDelegate client) {
    var analysisId = UUID.randomUUID();
    var analysisResult = backend.getAnalysisService().analyzeFilesAndTrack(
      new AnalyzeFilesAndTrackParams(CONFIG_SCOPE_ID, analysisId, List.of(fileUri), Map.of(), true, System.currentTimeMillis()))
      .join();
    ArgumentCaptor<Map<URI, List<RaisedHotspotDto>>> trackedIssuesCaptor = ArgumentCaptor.forClass(Map.class);
    verify(client, times(0)).raiseHotspots(eq(CONFIG_SCOPE_ID), trackedIssuesCaptor.capture(), eq(false), eq(analysisId));
    assertThat(analysisResult.getFailedAnalysisFiles()).isEmpty();
  }

  private static Path createFile(Path folderPath, String fileName, String content) {
    var filePath = folderPath.resolve(fileName);
    try {
      Files.writeString(filePath, content);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return filePath;
  }

}
