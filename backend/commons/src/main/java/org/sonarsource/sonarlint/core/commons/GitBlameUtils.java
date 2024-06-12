/*
 * SonarLint Core - Commons
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
package org.sonarsource.sonarlint.core.commons;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.sonar.scm.git.blame.RepositoryBlameCommand;

import static java.util.Optional.ofNullable;

public class GitBlameUtils {

  private GitBlameUtils() {
    // Utility class
  }

  public static SonarLintBlameResult blameWithFilesGitCommand(Path baseDir, Set<Path> gitRelativePath, @Nullable UnaryOperator<String> fileContentProvider) {
    var repo = buildRepository(baseDir);
    return blameWithFilesGitCommand(repo, gitRelativePath, fileContentProvider);
  }

  private static Repository buildRepository(Path basedir) {
    try {
      var repo = getVerifiedRepositoryBuilder(basedir).build();
      try (ObjectReader objReader = repo.getObjectDatabase().newReader()) {
        // SONARSCGIT-2 Force initialization of shallow commits to avoid later concurrent modification issue
        objReader.getShallowCommits();
        return repo;
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unable to open Git repository", e);
    }
  }

  private static RepositoryBuilder getVerifiedRepositoryBuilder(Path basedir) {
    var unixStyleBasedir = convertToUnixPath(basedir);
    var builder = new RepositoryBuilder()
      .findGitDir(unixStyleBasedir.toFile())
      .setMustExist(true);

    if (builder.getGitDir() == null) {
      throw new IllegalStateException("Not inside a Git work tree: " + basedir);
    }
    return builder;
  }

  private static SonarLintBlameResult blameWithFilesGitCommand(Repository repo, Set<Path> gitRelativePath, @Nullable UnaryOperator<String> fileContentProvider) {
    var pathStrings = gitRelativePath.stream().map(Path::toString).map(FilenameUtils::separatorsToUnix).collect(Collectors.toSet());
    var blameCommand = new RepositoryBlameCommand(repo)
      .setTextComparator(RawTextComparator.WS_IGNORE_ALL)
      .setMultithreading(true)
      .setFilePaths(pathStrings);
    ofNullable(fileContentProvider)
      .ifPresent(blameCommand::setFileContentProvider);
    try {
      var blameResult = blameCommand.call();
      return new SonarLintBlameResult(blameResult);
    } catch (GitAPIException e) {
      throw new IllegalStateException("Failed to blame repository files", e);
    }
  }

  private static Path convertToUnixPath(Path basedir) {
    return Path.of(FilenameUtils.separatorsToUnix(basedir.toString()));
  }
}
