/*
 * SonarLint Core - RPC Protocol
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
package org.sonarsource.sonarlint.core.rpc.protocol.backend.telemetry;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.analysis.AnalyzeFilesParams;
import org.sonarsource.sonarlint.core.rpc.protocol.client.telemetry.AddQuickFixAppliedForRuleParams;
import org.sonarsource.sonarlint.core.rpc.protocol.client.telemetry.AddReportedRulesParams;
import org.sonarsource.sonarlint.core.rpc.protocol.client.telemetry.AnalysisDoneOnSingleLanguageParams;
import org.sonarsource.sonarlint.core.rpc.protocol.client.telemetry.DevNotificationsClickedParams;
import org.sonarsource.sonarlint.core.rpc.protocol.client.telemetry.FixSuggestionResolvedParams;
import org.sonarsource.sonarlint.core.rpc.protocol.client.telemetry.HelpAndFeedbackClickedParams;

@JsonSegment("telemetry")
public interface TelemetryRpcService {

  @JsonRequest
  CompletableFuture<GetStatusResponse> getStatus();

  @JsonNotification
  void enableTelemetry();

  @JsonNotification
  void disableTelemetry();

  /**
   * @deprecated managed automatically when using {@link org.sonarsource.sonarlint.core.rpc.protocol.backend.analysis.AnalysisRpcService#analyzeFiles(AnalyzeFilesParams)}
   */
  @JsonNotification
  @Deprecated(since = "10.1")
  void analysisDoneOnSingleLanguage(AnalysisDoneOnSingleLanguageParams params);

  /**
   * @deprecated managed automatically when using {@link org.sonarsource.sonarlint.core.rpc.protocol.backend.analysis.AnalysisRpcService#analyzeFiles(AnalyzeFilesParams)}
   */
  @JsonNotification
  @Deprecated(since = "10.1")
  void analysisDoneOnMultipleFiles();

  @JsonNotification
  void devNotificationsClicked(DevNotificationsClickedParams params);

  @JsonNotification
  void taintVulnerabilitiesInvestigatedLocally();

  @JsonNotification
  void taintVulnerabilitiesInvestigatedRemotely();

  /**
   * @deprecated managed automatically when using {@link org.sonarsource.sonarlint.core.rpc.protocol.backend.analysis.AnalysisRpcService#analyzeFiles(AnalyzeFilesParams)}
   */
  @JsonNotification
  @Deprecated(since = "10.1")
  void addReportedRules(AddReportedRulesParams params);

  @JsonNotification
  void addQuickFixAppliedForRule(AddQuickFixAppliedForRuleParams params);

  @JsonNotification
  void helpAndFeedbackLinkClicked(HelpAndFeedbackClickedParams params);

  @JsonNotification
  void fixSuggestionResolved(FixSuggestionResolvedParams params);

  @JsonNotification
  void addedManualBindings();

  @JsonNotification
  void addedImportedBindings();

  @JsonNotification
  void addedAutomaticBindings();

}
