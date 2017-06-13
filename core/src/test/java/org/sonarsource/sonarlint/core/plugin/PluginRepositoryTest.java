/*
 * SonarLint Core - Implementation
 * Copyright (C) 2009-2017 SonarSource SA
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
package org.sonarsource.sonarlint.core.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.Plugin;
import org.sonarsource.sonarlint.core.container.connected.validate.PluginVersionChecker;

public class PluginRepositoryTest {
  private PluginRepository pluginRepository;
  private PluginCacheLoader cacheLoader;
  private PluginLoader loader;
  private PluginVersionChecker versionChecker;

  @Before
  public void setup() {
    cacheLoader = mock(PluginCacheLoader.class);
    loader = mock(PluginLoader.class);
    versionChecker = mock(PluginVersionChecker.class);
    pluginRepository = new PluginRepository(cacheLoader, loader, versionChecker);
  }

  @Test
  public void test() {
    PluginInfo info = new PluginInfo("key");
    Plugin plugin = mock(Plugin.class);
    Map<String, PluginInfo> infos = Collections.singletonMap("key", info);
    when(cacheLoader.load()).thenReturn(infos);
    when(loader.load(infos)).thenReturn(Collections.singletonMap("key", plugin));

    pluginRepository.start();

    verify(loader).load(infos);
    verify(cacheLoader).load();

    assertThat(pluginRepository.getLoadedAnalyzers()).hasSize(1);
    assertThat(pluginRepository.getPluginInfo("key")).isEqualTo(info);
    assertThat(pluginRepository.getPluginInfos()).containsExactly(info);
    assertThat(pluginRepository.getPluginInstance("key")).isEqualTo(plugin);
    assertThat(pluginRepository.hasPlugin("key")).isTrue();
  }

}