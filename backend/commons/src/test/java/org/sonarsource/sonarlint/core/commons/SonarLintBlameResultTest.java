/*
 * SonarLint Core - Commons
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
package org.sonarsource.sonarlint.core.commons;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.util.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jgit.util.FileUtils.RECURSIVE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sonarsource.sonarlint.core.commons.testutils.GitUtils.appendFile;
import static org.sonarsource.sonarlint.core.commons.testutils.GitUtils.commit;
import static org.sonarsource.sonarlint.core.commons.testutils.GitUtils.commitAtDate;
import static org.sonarsource.sonarlint.core.commons.testutils.GitUtils.createFile;
import static org.sonarsource.sonarlint.core.commons.testutils.GitUtils.createRepository;
import static org.sonarsource.sonarlint.core.commons.testutils.GitUtils.modifyFile;
import static org.sonarsource.sonarlint.core.commons.util.git.GitService.blameWithFilesGitCommand;

class SonarLintBlameResultTest {

  private Git git;
  @TempDir
  private Path projectDir;

  @BeforeEach
  public void prepare() throws IOException, GitAPIException {
    git = createRepository(projectDir);
  }

  @AfterEach
  public void cleanup() throws IOException {
    FileUtils.delete(projectDir.toFile(), RECURSIVE);
  }

  @Test
  void it_should_return_correct_latest_changed_date_for_file_lines() throws IOException, GitAPIException, InterruptedException {
    createFile(projectDir, "fileA", "line1", "line2", "line3");
    var c1 = commit(git, "fileA");

    // Wait for one second to achieve different commit time
    TimeUnit.MILLISECONDS.sleep(10);
    appendFile(projectDir.resolve("fileA"), "new line 4");
    var c2 = commit(git, "fileA");

    // Wait for one second to achieve different commit time
    TimeUnit.MILLISECONDS.sleep(10);
    createFile(projectDir, "fileB", "line1", "line2", "line3");
    var c3 = commit(git, "fileB");

    createFile(projectDir, "fileC", "line1", "line2", "line3");
    commit(git, "fileC");

    var results = blameWithFilesGitCommand(projectDir, Set.of(Path.of("fileA"), Path.of("fileB")));

    assertThat(results.getLatestChangeDateForLinesInFile(Path.of("fileA"), List.of(1, 2))).isPresent().contains(c1);
    assertThat(results.getLatestChangeDateForLinesInFile(Path.of("fileA"), List.of(2, 3))).isPresent().contains(c1);
    assertThat(results.getLatestChangeDateForLinesInFile(Path.of("fileA"), List.of(3, 4))).isPresent().contains(c2);
    assertThat(results.getLatestChangeDateForLinesInFile(Path.of("fileB"), List.of(1, 2))).isPresent().contains(c3);
    assertThat(results.getLatestChangeDateForLinesInFile(Path.of("fileC"), List.of(1, 2))).isEmpty();
  }

  @Test
  void it_should_handle_all_line_modified() throws IOException, GitAPIException {
    createFile(projectDir, "fileA", "line1", "line2", "line3");
    var c1 = commit(git, "fileA");

    var results = blameWithFilesGitCommand(projectDir, Set.of(Path.of("fileA")));
    assertThat(results.getLatestChangeDateForLinesInFile(Path.of("fileA"), List.of(1, 2, 3))).isPresent().contains(c1);

    modifyFile(projectDir.resolve("fileA"), "new line1", "new line2", "new line3");

    results = blameWithFilesGitCommand(projectDir, Set.of(Path.of("fileA")));
    assertThat(results.getLatestChangeDateForLinesInFile(Path.of("fileA"), List.of(1, 2, 3))).isEmpty();
  }

  @Test
  void it_should_return_latest_change_date() throws IOException, GitAPIException {
    createFile(projectDir, "fileA", "line1", "line2", "line3");
    var now = Instant.now();
    var c1 = commitAtDate(git, now.minus(1, ChronoUnit.DAYS), "fileA");

    var results = blameWithFilesGitCommand(projectDir, Set.of(Path.of("fileA")));
    assertThat(results.getLatestChangeDateForLinesInFile(Path.of("fileA"), List.of(1, 2, 3))).isPresent().contains(c1);
    modifyFile(projectDir.resolve("fileA"), "line1", "line2", "new line3");
    commitAtDate(git, now, "fileA");

    results = blameWithFilesGitCommand(projectDir, Set.of(Path.of("fileA")));
    var result = results.getLatestChangeDateForLinesInFile(Path.of("fileA"), List.of(1, 2, 3));

    assertThat(result).isPresent();
    assertThat(ChronoUnit.MINUTES.between(result.get(), now)).isZero();
  }

  @Test
  void it_should_handle_end_of_line_modified() throws IOException, GitAPIException {
    createFile(projectDir, "fileA", "line1", "line2");
    var c1 = commit(git, "fileA");

    var results = blameWithFilesGitCommand(projectDir, Set.of(Path.of("fileA")));
    assertThat(results.getLatestChangeDateForLinesInFile(Path.of("fileA"), List.of(1, 2))).isPresent().contains(c1);

    appendFile(projectDir.resolve("fileA"), "new line3", "new line4");

    results = blameWithFilesGitCommand(projectDir, Set.of(Path.of("fileA")));
    assertThat(results.getLatestChangeDateForLinesInFile(Path.of("fileA"), List.of(1, 2, 3))).isEmpty();
  }

  @Test
  void it_should_handle_dodgy_input() throws IOException, GitAPIException {
    createFile(projectDir, "fileA", "line1", "line2", "line3");
    var c1 = commit(git, "fileA");

    var results = blameWithFilesGitCommand(projectDir, Set.of(Path.of("fileA"), Path.of("fileB")));

    assertThat(results.getLatestChangeDateForLinesInFile(Path.of("fileA"),
      IntStream.rangeClosed(1, 100).boxed().toList())).isPresent().contains(c1);
    assertThat(results.getLatestChangeDateForLinesInFile(Path.of("fileA"),
      IntStream.rangeClosed(100, 1000).boxed().toList())).isEmpty();
  }

  @Test
  void it_should_raise_exception_if_wrong_line_numbering_provided() throws IOException, GitAPIException {
    createFile(projectDir, "fileA", "line1", "line2", "line3");
    commit(git, "fileA");

    var fileA = Path.of("fileA");
    var results = blameWithFilesGitCommand(projectDir, Set.of(fileA));
    var invalidLineNumbers = List.of(0, 1, 2);
    assertThrows(IllegalArgumentException.class, () -> results.getLatestChangeDateForLinesInFile(fileA, invalidLineNumbers));
  }

  @Test
  void it_should_handle_files_within_inner_dir() throws IOException, GitAPIException {
    var deepFilePath = Path.of("innerDir").resolve("fileA").toString();
    createFile(projectDir, deepFilePath, "line1", "line2", "line3");
    var c1 = commit(git, deepFilePath);

    var results = blameWithFilesGitCommand(projectDir, Set.of(Path.of(deepFilePath)));
    assertThat(results.getLatestChangeDateForLinesInFile(
      Path.of(deepFilePath),
      IntStream.rangeClosed(1, 100).boxed().toList()))
      .isPresent().contains(c1);
  }
}
