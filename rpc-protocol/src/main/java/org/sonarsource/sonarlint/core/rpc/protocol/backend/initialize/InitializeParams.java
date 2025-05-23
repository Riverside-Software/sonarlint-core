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
package org.sonarsource.sonarlint.core.rpc.protocol.backend.initialize;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.connection.config.SonarCloudConnectionConfigurationDto;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.connection.config.SonarQubeConnectionConfigurationDto;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.rules.StandaloneRuleConfigDto;
import org.sonarsource.sonarlint.core.rpc.protocol.common.Language;

import static org.sonarsource.sonarlint.core.rpc.protocol.backend.initialize.BackendCapability.DATAFLOW_BUG_DETECTION;
import static org.sonarsource.sonarlint.core.rpc.protocol.backend.initialize.BackendCapability.EMBEDDED_SERVER;
import static org.sonarsource.sonarlint.core.rpc.protocol.backend.initialize.BackendCapability.FULL_SYNCHRONIZATION;
import static org.sonarsource.sonarlint.core.rpc.protocol.backend.initialize.BackendCapability.MONITORING;
import static org.sonarsource.sonarlint.core.rpc.protocol.backend.initialize.BackendCapability.PROJECT_SYNCHRONIZATION;
import static org.sonarsource.sonarlint.core.rpc.protocol.backend.initialize.BackendCapability.SECURITY_HOTSPOTS;
import static org.sonarsource.sonarlint.core.rpc.protocol.backend.initialize.BackendCapability.SERVER_SENT_EVENTS;
import static org.sonarsource.sonarlint.core.rpc.protocol.backend.initialize.BackendCapability.SMART_NOTIFICATIONS;
import static org.sonarsource.sonarlint.core.rpc.protocol.backend.initialize.BackendCapability.TELEMETRY;

public class InitializeParams {
  private final ClientConstantInfoDto clientConstantInfo;
  private final TelemetryClientConstantAttributesDto telemetryConstantAttributes;
  private final HttpConfigurationDto httpConfiguration;
  private final SonarCloudAlternativeEnvironmentDto alternativeSonarCloudEnvironment;
  private final Set<BackendCapability> backendCapabilities;
  private final Path storageRoot;
  private final Path workDir;
  private final Set<Path> embeddedPluginPaths;
  private final Map<String, Path> connectedModeEmbeddedPluginPathsByKey;
  private final Set<Language> enabledLanguagesInStandaloneMode;
  private final Set<Language> extraEnabledLanguagesInConnectedMode;
  private final Set<String> disabledPluginKeysForAnalysis;
  private final List<SonarQubeConnectionConfigurationDto> sonarQubeConnections;
  private final List<SonarCloudConnectionConfigurationDto> sonarCloudConnections;
  private final String sonarlintUserHome;
  private final Map<String, StandaloneRuleConfigDto> standaloneRuleConfigByKey;
  private final boolean isFocusOnNewCode;
  private final LanguageSpecificRequirements languageSpecificRequirements;
  private final boolean automaticAnalysisEnabled;
  private final TelemetryMigrationDto telemetryMigration;


  /**
   * @param enabledLanguagesInStandaloneMode if IPYTHON is part of the list and a configuration scope is bound, standalone active rules will be used
   * @param telemetryConstantAttributes Static information about the client, that will be sent with the telemetry payload
   * @param workDir                     Path to work directory. If null, will default to [sonarlintUserHome]/work
   * @param sonarlintUserHome           Path to SonarLint user home directory. If null, will default to the SONARLINT_USER_HOME env variable if set, else ~/.sonarlint
   * @param standaloneRuleConfigByKey   Local rule configuration for standalone analysis. This configuration will override defaults rule activation and parameters.
   */
  public InitializeParams(
    ClientConstantInfoDto clientConstantInfo,
    TelemetryClientConstantAttributesDto telemetryConstantAttributes,
    HttpConfigurationDto httpConfiguration,
    @Nullable SonarCloudAlternativeEnvironmentDto alternativeSonarCloudEnvironment,
    Set<BackendCapability> backendCapabilities,
    Path storageRoot,
    @Nullable Path workDir,
    @Nullable Set<Path> embeddedPluginPaths,
    @Nullable Map<String, Path> connectedModeEmbeddedPluginPathsByKey,
    @Nullable Set<Language> enabledLanguagesInStandaloneMode,
    @Nullable Set<Language> extraEnabledLanguagesInConnectedMode,
    @Nullable Set<String> disabledPluginKeysForAnalysis,
    @Nullable List<SonarQubeConnectionConfigurationDto> sonarQubeConnections,
    @Nullable List<SonarCloudConnectionConfigurationDto> sonarCloudConnections,
    @Nullable String sonarlintUserHome,
    @Nullable Map<String, StandaloneRuleConfigDto> standaloneRuleConfigByKey,
    boolean isFocusOnNewCode,
    @Nullable LanguageSpecificRequirements languageSpecificRequirements,
    boolean automaticAnalysisEnabled,
    @Nullable TelemetryMigrationDto telemetryMigration) {
    this.clientConstantInfo = clientConstantInfo;
    this.telemetryConstantAttributes = telemetryConstantAttributes;
    this.httpConfiguration = httpConfiguration;
    this.alternativeSonarCloudEnvironment = alternativeSonarCloudEnvironment;
    this.backendCapabilities = backendCapabilities;
    this.storageRoot = storageRoot;
    this.workDir = workDir;
    this.embeddedPluginPaths = embeddedPluginPaths;
    this.connectedModeEmbeddedPluginPathsByKey = connectedModeEmbeddedPluginPathsByKey;
    this.enabledLanguagesInStandaloneMode = enabledLanguagesInStandaloneMode;
    this.extraEnabledLanguagesInConnectedMode = extraEnabledLanguagesInConnectedMode;
    this.disabledPluginKeysForAnalysis = disabledPluginKeysForAnalysis;
    this.sonarQubeConnections = sonarQubeConnections;
    this.sonarCloudConnections = sonarCloudConnections;
    this.sonarlintUserHome = sonarlintUserHome;
    this.standaloneRuleConfigByKey = standaloneRuleConfigByKey;
    this.isFocusOnNewCode = isFocusOnNewCode;
    this.languageSpecificRequirements = languageSpecificRequirements;
    this.automaticAnalysisEnabled = automaticAnalysisEnabled;
    this.telemetryMigration = telemetryMigration;
  }

  /**
   * @deprecated use newer constructor with BackendCapabilities parameter instead of FeatureFlagDto
   * @param enabledLanguagesInStandaloneMode if IPYTHON is part of the list and a configuration scope is bound, standalone active rules will be used
   * @param telemetryConstantAttributes Static information about the client, that will be sent with the telemetry payload
   * @param workDir                     Path to work directory. If null, will default to [sonarlintUserHome]/work
   * @param sonarlintUserHome           Path to SonarLint user home directory. If null, will default to the SONARLINT_USER_HOME env variable if set, else ~/.sonarlint
   * @param standaloneRuleConfigByKey   Local rule configuration for standalone analysis. This configuration will override defaults rule activation and parameters.
   */
  @Deprecated(since = "10.19", forRemoval = true)
  public InitializeParams(
    ClientConstantInfoDto clientConstantInfo,
    TelemetryClientConstantAttributesDto telemetryConstantAttributes,
    HttpConfigurationDto httpConfiguration,
    @Nullable SonarCloudAlternativeEnvironmentDto alternativeSonarCloudEnvironment,
    FeatureFlagsDto featureFlags,
    Path storageRoot,
    @Nullable Path workDir,
    @Nullable Set<Path> embeddedPluginPaths,
    @Nullable Map<String, Path> connectedModeEmbeddedPluginPathsByKey,
    @Nullable Set<Language> enabledLanguagesInStandaloneMode,
    @Nullable Set<Language> extraEnabledLanguagesInConnectedMode,
    @Nullable Set<String> disabledPluginKeysForAnalysis,
    @Nullable List<SonarQubeConnectionConfigurationDto> sonarQubeConnections,
    @Nullable List<SonarCloudConnectionConfigurationDto> sonarCloudConnections,
    @Nullable String sonarlintUserHome,
    @Nullable Map<String, StandaloneRuleConfigDto> standaloneRuleConfigByKey,
    boolean isFocusOnNewCode,
    @Nullable LanguageSpecificRequirements languageSpecificRequirements,
    boolean automaticAnalysisEnabled,
    @Nullable TelemetryMigrationDto telemetryMigration) {
    this(clientConstantInfo, telemetryConstantAttributes, httpConfiguration, alternativeSonarCloudEnvironment,
      featureFlagsToBackendCapabilities(featureFlags), storageRoot, workDir, embeddedPluginPaths, connectedModeEmbeddedPluginPathsByKey,
      enabledLanguagesInStandaloneMode, extraEnabledLanguagesInConnectedMode, disabledPluginKeysForAnalysis, sonarQubeConnections,
      sonarCloudConnections, sonarlintUserHome, standaloneRuleConfigByKey, isFocusOnNewCode, languageSpecificRequirements,
      automaticAnalysisEnabled, telemetryMigration);
  }

  public ClientConstantInfoDto getClientConstantInfo() {
    return clientConstantInfo;
  }

  public TelemetryClientConstantAttributesDto getTelemetryConstantAttributes() {
    return telemetryConstantAttributes;
  }

  public HttpConfigurationDto getHttpConfiguration() {
    return httpConfiguration;
  }

  @CheckForNull
  public SonarCloudAlternativeEnvironmentDto getAlternativeSonarCloudEnvironment() {
    return alternativeSonarCloudEnvironment;
  }

  public Set<BackendCapability> getBackendCapabilities() {
    return backendCapabilities;
  }

  public Path getStorageRoot() {
    return storageRoot;
  }

  @CheckForNull
  public Path getWorkDir() {
    return workDir;
  }

  public Set<Path> getEmbeddedPluginPaths() {
    return embeddedPluginPaths != null ? embeddedPluginPaths : Set.of();
  }

  public Map<String, Path> getConnectedModeEmbeddedPluginPathsByKey() {
    return connectedModeEmbeddedPluginPathsByKey != null ? connectedModeEmbeddedPluginPathsByKey : Map.of();
  }

  public Set<Language> getEnabledLanguagesInStandaloneMode() {
    return enabledLanguagesInStandaloneMode != null ? enabledLanguagesInStandaloneMode : Set.of();
  }

  public Set<Language> getExtraEnabledLanguagesInConnectedMode() {
    return extraEnabledLanguagesInConnectedMode != null ? extraEnabledLanguagesInConnectedMode : Set.of();
  }

  public List<SonarQubeConnectionConfigurationDto> getSonarQubeConnections() {
    return sonarQubeConnections != null ? sonarQubeConnections : List.of();
  }

  public List<SonarCloudConnectionConfigurationDto> getSonarCloudConnections() {
    return sonarCloudConnections != null ? sonarCloudConnections : List.of();
  }

  @CheckForNull
  public String getSonarlintUserHome() {
    return sonarlintUserHome;
  }

  public Map<String, StandaloneRuleConfigDto> getStandaloneRuleConfigByKey() {
    return standaloneRuleConfigByKey != null ? standaloneRuleConfigByKey : Map.of();
  }

  public boolean isFocusOnNewCode() {
    return isFocusOnNewCode;
  }

  @Nullable
  public LanguageSpecificRequirements getLanguageSpecificRequirements() {
    return languageSpecificRequirements;
  }

  public boolean isAutomaticAnalysisEnabled() {
    return automaticAnalysisEnabled;
  }

  public Set<String> getDisabledPluginKeysForAnalysis() {
    return disabledPluginKeysForAnalysis != null ? disabledPluginKeysForAnalysis : Set.of();
  }

  @CheckForNull
  public TelemetryMigrationDto getTelemetryMigration() {
    return telemetryMigration;
  }

  private static Set<BackendCapability> featureFlagsToBackendCapabilities(@Nullable FeatureFlagsDto featureFlags) {
    var capabilities = EnumSet.noneOf(BackendCapability.class);
    if (featureFlags == null) {
      return capabilities;
    }
    addIfTrue(capabilities, featureFlags.shouldManageSmartNotifications(), SMART_NOTIFICATIONS);
    addIfTrue(capabilities, featureFlags.shouldSynchronizeProjects(), PROJECT_SYNCHRONIZATION);
    addIfTrue(capabilities, featureFlags.shouldManageLocalServer(), EMBEDDED_SERVER);
    addIfTrue(capabilities, featureFlags.isEnablesSecurityHotspots(), SECURITY_HOTSPOTS);
    addIfTrue(capabilities, featureFlags.shouldManageServerSentEvents(), SERVER_SENT_EVENTS);
    addIfTrue(capabilities, featureFlags.isEnabledDataflowBugDetection(), DATAFLOW_BUG_DETECTION);
    addIfTrue(capabilities, featureFlags.shouldManageFullSynchronization(), FULL_SYNCHRONIZATION);
    addIfTrue(capabilities, featureFlags.isEnabledTelemetry(), TELEMETRY);
    addIfTrue(capabilities, featureFlags.isEnabledMonitoring(), MONITORING);
    return capabilities;
  }

  private static void addIfTrue(Set<BackendCapability> capabilities, boolean enabled, BackendCapability backendCapability) {
    if (enabled) {
      capabilities.add(backendCapability);
    }
  }
}
