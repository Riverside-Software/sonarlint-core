/*
 * SonarLint Core - Server API
 * Copyright (C) 2016-2022 SonarSource SA
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
package org.sonarsource.sonarlint.core.serverapi.push;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

public class IssueChangedEvent implements ServerEvent {
  private final List<String> impactedIssueKeys;
  private final String userSeverity;
  private final String userType;
  private final Boolean resolved;

  public IssueChangedEvent(List<String> impactedIssueKeys, @Nullable String userSeverity, @Nullable String userType, @Nullable Boolean resolved) {
    this.impactedIssueKeys = impactedIssueKeys;
    this.userSeverity = userSeverity;
    this.userType = userType;
    this.resolved = resolved;
  }

  public List<String> getImpactedIssueKeys() {
    return impactedIssueKeys;
  }

  @CheckForNull
  public String getUserSeverity() {
    return userSeverity;
  }

  @CheckForNull
  public String getUserType() {
    return userType;
  }

  @CheckForNull
  public Boolean getResolved() {
    return resolved;
  }
}
