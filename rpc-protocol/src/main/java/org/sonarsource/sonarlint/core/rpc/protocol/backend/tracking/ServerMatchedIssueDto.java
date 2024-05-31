/*
 * SonarLint Core - RPC Protocol
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
package org.sonarsource.sonarlint.core.rpc.protocol.backend.tracking;

import java.util.UUID;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.analysis.AnalysisRpcService;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.analysis.AnalyzeFilesAndTrackParams;
import org.sonarsource.sonarlint.core.rpc.protocol.common.IssueSeverity;
import org.sonarsource.sonarlint.core.rpc.protocol.common.RuleType;

/**
 * @deprecated Use {@link AnalysisRpcService#analyzeFilesAndTrack(AnalyzeFilesAndTrackParams)} instead.
 */
@Deprecated(since = "10.2")
public class ServerMatchedIssueDto {
  private final UUID id;
  private final String serverKey;
  private final long introductionDate;
  private final boolean resolved;
  private final IssueSeverity overriddenSeverity;
  private final RuleType type;
  private final boolean isOnNewCode;

  public ServerMatchedIssueDto(UUID id, String serverKey, long introductionDate,
    boolean resolved, @Nullable IssueSeverity overriddenSeverity, RuleType type, boolean isOnNewCode) {
    this.id = id;
    this.serverKey = serverKey;
    this.introductionDate = introductionDate;
    this.resolved = resolved;
    this.overriddenSeverity = overriddenSeverity;
    this.type = type;
    this.isOnNewCode = isOnNewCode;
  }

  public UUID getId() {
    return id;
  }

  public String getServerKey() {
    return serverKey;
  }

  public long getIntroductionDate() {
    return introductionDate;
  }

  public boolean isResolved() {
    return resolved;
  }

  @CheckForNull
  public IssueSeverity getOverriddenSeverity() {
    return overriddenSeverity;
  }

  public RuleType getType() {
    return type;
  }

  public boolean isOnNewCode() {
    return isOnNewCode;
  }

}
