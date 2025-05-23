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
package org.sonarsource.sonarlint.core.serverconnection.issues;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.sonarsource.sonarlint.core.commons.IssueSeverity;
import org.sonarsource.sonarlint.core.commons.RuleType;
import org.sonarsource.sonarlint.core.commons.api.TextRangeWithHash;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.sonarsource.sonarlint.core.serverconnection.storage.ServerIssueFixtures.aServerTaintIssue;

class ServerTaintIssueTests {
  @Test
  void testRoundTrips() {
    var issue = aServerTaintIssue();
    var i1 = Instant.ofEpochMilli(100_000_000);
    assertThat(issue.setCreationDate(i1).getCreationDate()).isEqualTo(i1);
    assertThat(issue.setFilePath(Path.of("path1")).getFilePath()).isEqualTo(Path.of("path1"));
    assertThat(issue.setKey("key1").getSonarServerKey()).isEqualTo("key1");
    issue.setTextRange(new TextRangeWithHash(1,
      2,
      3,
      4, "checksum1"));
    assertThat(issue.getTextRange().getStartLine()).isEqualTo(1);
    assertThat(issue.getTextRange().getStartLineOffset()).isEqualTo(2);
    assertThat(issue.getTextRange().getEndLine()).isEqualTo(3);
    assertThat(issue.getTextRange().getEndLineOffset()).isEqualTo(4);
    assertThat(issue.getTextRange().getHash()).isEqualTo("checksum1");
    assertThat(issue.setSeverity(IssueSeverity.MAJOR).getSeverity()).isEqualTo(IssueSeverity.MAJOR);
    assertThat(issue.setRuleKey("rule1").getRuleKey()).isEqualTo("rule1");
    assertThat(issue.isResolved()).isFalse();
    assertThat(issue.setMessage("msg1").getMessage()).isEqualTo("msg1");
    assertThat(issue.setType(RuleType.BUG).getType()).isEqualTo(RuleType.BUG);

    assertThat(issue.getFlows())
      .flatExtracting("locations")
      .extracting("message", "filePath", "textRange.startLine", "textRange.startLineOffset", "textRange.endLine", "textRange.endLineOffset", "textRange.hash")
      .containsOnly(
        // flow 1
        tuple("message", Path.of("file/path"), 5, 6, 7, 8, "rangeHash"));

    issue.setFlows(Arrays.asList(mock(ServerTaintIssue.Flow.class), mock(ServerTaintIssue.Flow.class)));
    assertThat(issue.getFlows()).hasSize(2);
  }
}
