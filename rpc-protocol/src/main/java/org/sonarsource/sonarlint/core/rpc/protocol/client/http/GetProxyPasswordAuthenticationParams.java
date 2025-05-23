/*
 * SonarLint Core - RPC Protocol
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
package org.sonarsource.sonarlint.core.rpc.protocol.client.http;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.URL;
import javax.annotation.Nullable;

/**
 * @see Authenticator#requestPasswordAuthentication(String, InetAddress, int, String, String, String)
 *
 */
public class GetProxyPasswordAuthenticationParams {

  private final String host;
  private final int port;
  private final String protocol;
  private final String prompt;
  private final String scheme;
  private final URL targetHost;

  public GetProxyPasswordAuthenticationParams(String host, int port, String protocol, @Nullable String prompt, @Nullable String scheme, URL targetHost) {
    this.host = host;
    this.port = port;
    this.protocol = protocol;
    this.prompt = prompt;
    this.scheme = scheme;
    this.targetHost = targetHost;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getProtocol() {
    return protocol;
  }

  @Nullable
  public String getPrompt() {
    return prompt;
  }

  @Nullable
  public String getScheme() {
    return scheme;
  }

  public URL getTargetHost() {
    return targetHost;
  }
}
