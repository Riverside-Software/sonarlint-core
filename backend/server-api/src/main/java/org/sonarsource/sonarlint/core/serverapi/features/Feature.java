/*
 * SonarLint Core - Server API
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
package org.sonarsource.sonarlint.core.serverapi.features;

import java.util.Arrays;
import java.util.Optional;

public enum Feature {
  AI_CODE_FIX("fix-suggestions");

  public static Optional<Feature> fromKey(String key) {
    return Arrays.stream(values()).filter(f -> f.key.equals(key)).findFirst();
  }

  private final String key;

  Feature(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
