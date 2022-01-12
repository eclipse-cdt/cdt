/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.core.commands;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.remote.internal.proxy.core.ProxyConnection;
import org.eclipse.remote.proxy.protocol.core.Protocol;
import org.eclipse.remote.proxy.protocol.core.StreamChannel;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public class GetEnvCommand extends AbstractCommand<Map<String, String>> {

	private final DataOutputStream out;
	private final DataInputStream in;

	public GetEnvCommand(ProxyConnection conn) {
		super(conn);
		this.out = new DataOutputStream(conn.getCommandChannel().getOutputStream());
		this.in = new DataInputStream(conn.getCommandChannel().getInputStream());
	}

	public Map<String, String> call() throws ProxyException {
		try {
			final StreamChannel chan = openChannel();
			DataInputStream resultStream = new DataInputStream(chan.getInputStream());

			out.writeByte(Protocol.PROTO_COMMAND);
			out.writeShort(Protocol.CMD_GETENV);
			out.writeByte(chan.getId());
			out.flush();

			byte res = in.readByte();
			if (res != Protocol.PROTO_OK) {
				String errMsg = in.readUTF();
				throw new ProxyException(errMsg);
			}

			int len = resultStream.readInt();
			Map<String, String> env = new HashMap<String, String>(len);
			for (int i = 0; i < len; i++) {
				String key = resultStream.readUTF();
				String value = resultStream.readUTF();
				env.put(key, value);
			}
			chan.close();
			return env;
		} catch (IOException e) {
			throw new ProxyException(e.getMessage());
		}
	}
}
