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
package org.sonarsource.sonarlint.core.nodejs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.utils.System2;
import org.sonar.api.utils.command.Command;
import org.sonar.api.utils.command.CommandException;
import org.sonar.api.utils.command.CommandExecutor;
import org.sonarsource.sonarlint.core.commons.Version;
import org.sonarsource.sonarlint.core.commons.log.SonarLintLogger;

public class NodeJsHelper {

  private static final SonarLintLogger LOG = SonarLintLogger.get();

  private static final Pattern NODEJS_VERSION_PATTERN = Pattern.compile("v?(\\d+\\.\\d+\\.\\d+(-.*)?)");
  private final System2 system2;
  private final Path pathHelperLocationOnMac;
  private final CommandExecutor commandExecutor;

  public NodeJsHelper() {
    this(System2.INSTANCE, Paths.get("/usr/libexec/path_helper"), CommandExecutor.create());
  }

  // For testing
  NodeJsHelper(System2 system2, Path pathHelperLocationOnMac, CommandExecutor commandExecutor) {
    this.system2 = system2;
    this.pathHelperLocationOnMac = pathHelperLocationOnMac;
    this.commandExecutor = commandExecutor;
  }

  @CheckForNull
  public InstalledNodeJs autoDetect() {
    return detect(null);
  }

  @CheckForNull
  public InstalledNodeJs detect(@Nullable Path configuredNodejsPath) {
    var detectedNodePath = locateNode(configuredNodejsPath);
    if (detectedNodePath != null) {
      var nodeJsVersion = readNodeVersion(detectedNodePath);
      if (nodeJsVersion == null) {
        LOG.warn("Unable to query node version");
      } else {
        return new InstalledNodeJs(detectedNodePath, nodeJsVersion);
      }
    }
    return null;
  }

  @CheckForNull
  private Version readNodeVersion(Path detectedNodePath) {
    LOG.debug("Checking node version...");
    String nodeVersionStr;
    var forcedNodeVersion = System.getProperty("sonarlint.internal.nodejs.forcedVersion");
    if (forcedNodeVersion != null) {
      nodeVersionStr = forcedNodeVersion;
    } else {
      var command = Command.create(detectedNodePath.toString()).addArgument("-v");
      nodeVersionStr = runSimpleCommand(command);
    }
    Version nodeJsVersion = null;
    if (nodeVersionStr != null) {
      var matcher = NODEJS_VERSION_PATTERN.matcher(nodeVersionStr);
      if (matcher.matches()) {
        var version = matcher.group(1);
        nodeJsVersion = Version.create(version);
        LOG.debug("Detected node version: {}", nodeJsVersion);
      } else {
        LOG.debug("Unable to parse node version: {}", nodeVersionStr);
      }
    }
    return nodeJsVersion;
  }

  @CheckForNull
  private Path locateNode(@Nullable Path configuredNodejsPath) {
    if (configuredNodejsPath != null) {
      LOG.debug("Node.js path provided by configuration: {}", configuredNodejsPath);
      return configuredNodejsPath;
    }
    LOG.debug("Looking for node in the PATH");

    var forcedPath = System.getProperty("sonarlint.internal.nodejs.forcedPath");
    String result;
    if (forcedPath != null) {
      result = forcedPath;
    } else if (system2.isOsWindows()) {
      result = runSimpleCommand(Command.create("C:\\Windows\\System32\\where.exe").addArgument("$PATH:node.exe"));
    } else {
      // INFO: Based on the Linux / macOS shell we require the full path as "which" is a built-in on some shells!
      var which = Command.create("/usr/bin/which").addArgument("node");
      computePathEnvForMacOs(which);
      result = runSimpleCommand(which);
    }
    if (result != null) {
      LOG.debug("Found node at {}", result);
      return Paths.get(result);
    } else {
      LOG.debug("Unable to locate node");
      return null;
    }
  }

  private void computePathEnvForMacOs(Command which) {
    if (system2.isOsMac() && Files.exists(pathHelperLocationOnMac)) {
      var command = Command.create(pathHelperLocationOnMac.toString()).addArgument("-s");
      var pathHelperOutput = runSimpleCommand(command);
      if (pathHelperOutput != null) {
        var regex = Pattern.compile(".*PATH=\"(.*)\"; export PATH;.*");
        var matchResult = regex.matcher(pathHelperOutput);
        if (matchResult.matches()) {
          which.setEnvironmentVariable("PATH", matchResult.group(1));
        }
      }
    }
  }

  /**
   * Run a simple command that should return a single line on stdout
   */
  @CheckForNull
  private String runSimpleCommand(Command command) {
    List<String> stdOut = new ArrayList<>();
    List<String> stdErr = new ArrayList<>();
    LOG.debug("Execute command '{}'...", command);
    int exitCode;
    try {
      exitCode = commandExecutor.execute(command, stdOut::add, stdErr::add, 10_000);
    } catch (CommandException e) {
      LOG.debug("Unable to execute the command", e);
      return null;
    }
    var msg = new StringBuilder(String.format("Command '%s' exited with %s", command, exitCode));
    if (!stdOut.isEmpty()) {
      msg.append("\nstdout: ").append(String.join("\n", stdOut));
    }
    if (!stdErr.isEmpty()) {
      msg.append("\nstderr: ").append(String.join("\n", stdErr));
    }
    LOG.debug("{}", msg);
    if (exitCode != 0 || stdOut.isEmpty()) {
      return null;
    }
    return stdOut.get(0);
  }

}
