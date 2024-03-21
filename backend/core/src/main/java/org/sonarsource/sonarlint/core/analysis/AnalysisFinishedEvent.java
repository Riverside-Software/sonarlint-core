/*
 * SonarLint Core - Implementation
 * Copyright (C) 2016-2024 SonarSource SA
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
package org.sonarsource.sonarlint.core.analysis;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonarsource.sonarlint.core.commons.api.SonarLanguage;

public class AnalysisFinishedEvent {
  private final String configurationScopeId;
  private final long analysisDuration;
  private final Map<URI, SonarLanguage> languagePerFile;
  private final boolean succeededForAllFiles;
  private final Set<String> reportedRuleKeys;
  private final Set<SonarLanguage> detectedLanguages;

  public AnalysisFinishedEvent(String configurationScopeId, long analysisDuration, Map<URI, SonarLanguage> languagePerFile, boolean succeededForAllFiles,
    Set<String> reportedRuleKeys) {
    this.configurationScopeId = configurationScopeId;
    this.analysisDuration = analysisDuration;
    this.languagePerFile = languagePerFile;
    this.succeededForAllFiles = succeededForAllFiles;
    this.reportedRuleKeys = reportedRuleKeys;
    this.detectedLanguages = languagePerFile.values().stream().filter(Objects::nonNull).collect(Collectors.toSet());
  }

  public String getConfigurationScopeId() {
    return configurationScopeId;
  }

  public long getAnalysisDuration() {
    return analysisDuration;
  }

  public Map<URI, SonarLanguage> getLanguagePerFile() {
    return languagePerFile;
  }

  public boolean succeededForAllFiles() {
    return succeededForAllFiles;
  }

  public Set<String> getReportedRuleKeys() {
    return reportedRuleKeys;
  }

  public Set<SonarLanguage> getDetectedLanguages() {
    return detectedLanguages;
  }
}