/*
 * SonarLint Core - Java Client Utils
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
package org.sonarsource.sonarlint.core.client.utils;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;

class ClientFileExclusionsTests {
  ClientFileExclusions underTest;

  @BeforeEach
  void before() {
    Set<String> glob = Collections.singleton("**/*.js");

    // Setup file exclusions with both separator styles
    Set<String> files = Set.of(
            new File("dir/file.java").getAbsolutePath(),
            "dir/file-with-slash.java",
            "other\\file-with-backslash.java"
    );

    // Setup directory exclusions with both separator styles
    Set<String> dirs = Set.of(
            "src",
            "excluded/dir",
            "another\\excluded\\dir"
    );

    underTest = new ClientFileExclusions(files, dirs, glob);
  }

  @Test
  void should_exclude_with_glob_relative_path() {
    assertThat(underTest.test(new File("dir2/file.js").getAbsolutePath())).isTrue();
    assertThat(underTest.test(new File("dir2/file.java").getAbsolutePath())).isFalse();
  }

  @Test
  void should_exclude_with_glob_absolute_path() {
    assertThat(underTest.test(new File("/absolute/dir/file.js").getAbsolutePath())).isTrue();
    assertThat(underTest.test(new File("/absolute/dir/file.java").getAbsolutePath())).isFalse();
  }

  @Test
  void should_exclude_with_file() {
    assertThat(underTest.test(new File("dir/file2.java").getAbsolutePath())).isFalse();
    assertThat(underTest.test(new File("dir/file.java").getAbsolutePath())).isTrue();
  }

  @Test
  void should_exclude_with_dir() {
    assertThat(underTest.test(new File("dir/class2.java").getAbsolutePath())).isFalse();
    assertThat(underTest.test("src/class.java")).isTrue();
  }

  @Test
  void should_handle_file_exclusions_with_different_separators() {
    assertThat(underTest.test("dir/file-with-slash.java")).isTrue();
    assertThat(underTest.test("other/file-with-backslash.java")).isTrue();

    assertThat(underTest.test("different/dir/file-with-slash.java")).isFalse();
    assertThat(underTest.test("other2/file-with-backslash.java")).isFalse();
  }

  @Test
  void should_handle_directory_exclusions_with_different_separators() {
    assertThat(underTest.test("excluded/dir/some-file.java")).isTrue();
    assertThat(underTest.test("another/excluded/dir/some-file.java")).isTrue();

    assertThat(underTest.test("different/excluded/some-file.java")).isFalse();
    assertThat(underTest.test("another2\\excluded\\dir\\some-file.java")).isFalse();
  }

  @EnabledOnOs(OS.WINDOWS)
  @Test
  void testFileExclusionsWithBackslashes() {
    assertThat(underTest.test("dir\\file-with-slash.java")).isTrue();
  }

  @EnabledOnOs(OS.WINDOWS)
  @Test
  void testDirectoryExclusionsWithBackslashes() {
    assertThat(underTest.test("excluded\\dir\\some-file.java")).isTrue();
  }
}
