/*
 * SonarLint Core - Server Connection
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
package org.sonarsource.sonarlint.core.serverconnection;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.sonarsource.sonarlint.core.serverconnection.issues.ServerIssue;

public class IssueStoreReader {
  private final ConnectionStorage storage;

  public IssueStoreReader(ConnectionStorage storage) {
    this.storage = storage;
  }

  public List<ServerIssue<?>> getServerIssues(ProjectBinding projectBinding, String branchName, Path ideFilePath) {
    var sqPath = IssueStorePaths.idePathToServerPath(projectBinding, ideFilePath);
    if (sqPath == null) {
      return Collections.emptyList();
    }
    var loadedIssues = storage.project(projectBinding.projectKey()).findings().load(branchName, sqPath);
    loadedIssues.forEach(issue -> issue.setFilePath(ideFilePath));
    return loadedIssues;
  }
}
