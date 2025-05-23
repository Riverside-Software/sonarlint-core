/*
 * SonarLint Core - Implementation
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
package org.sonarsource.sonarlint.core.tracking;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonarsource.sonarlint.core.commons.IssueStatus;
import org.sonarsource.sonarlint.core.commons.LineWithHash;
import org.sonarsource.sonarlint.core.commons.LocalOnlyIssue;
import org.sonarsource.sonarlint.core.commons.LocalOnlyIssueResolution;
import org.sonarsource.sonarlint.core.commons.api.TextRangeWithHash;
import org.sonarsource.sonarlint.core.tracking.matching.LocalOnlyIssueMatchingAttributesMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocalOnlyIssueMatchingAttributesMapperTests {

  private final LocalOnlyIssue localOnlyIssue = mock(LocalOnlyIssue.class);
  private final LocalOnlyIssueMatchingAttributesMapper underTest = new LocalOnlyIssueMatchingAttributesMapper();

  @BeforeEach
  void prepare() {
    when(localOnlyIssue.getId()).thenReturn(UUID.randomUUID());
    when(localOnlyIssue.getMessage()).thenReturn("msg");
    when(localOnlyIssue.getResolution()).thenReturn(new LocalOnlyIssueResolution(IssueStatus.WONT_FIX, Instant.now(), null));
    when(localOnlyIssue.getRuleKey()).thenReturn("ruleKey");
    when(localOnlyIssue.getServerRelativePath()).thenReturn(Path.of("file/path"));
    when(localOnlyIssue.getTextRangeWithHash()).thenReturn(new TextRangeWithHash(1, 2, 3, 4, "rangehash"));
    when(localOnlyIssue.getLineWithHash()).thenReturn(new LineWithHash(1, "linehash"));
  }

  @Test
  void should_delegate_fields_to_server_issue() {
    assertThat(underTest.getMessage(localOnlyIssue)).isEqualTo(localOnlyIssue.getMessage());
    assertThat(underTest.getRuleKey(localOnlyIssue)).isEqualTo(localOnlyIssue.getRuleKey());
    assertThat(underTest.getLine(localOnlyIssue)).contains(localOnlyIssue.getLineWithHash().getNumber());
    assertThat(underTest.getLineHash(localOnlyIssue)).contains(localOnlyIssue.getLineWithHash().getHash());
    assertThat(underTest.getTextRangeHash(localOnlyIssue)).contains(localOnlyIssue.getTextRangeWithHash().getHash());
    assertThat(underTest.getServerIssueKey(localOnlyIssue)).isEmpty();
  }

}
