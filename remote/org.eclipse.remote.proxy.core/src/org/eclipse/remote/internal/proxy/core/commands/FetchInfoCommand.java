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

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.remote.internal.proxy.core.ProxyConnection;
import org.eclipse.remote.proxy.protocol.core.Protocol;
import org.eclipse.remote.proxy.protocol.core.SerializableFileInfo;
import org.eclipse.remote.proxy.protocol.core.StreamChannel;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public class FetchInfoCommand extends AbstractCommand<IFileInfo> {

	private final DataOutputStream out;
	private final DataInputStream in;
	private final String path;

	public FetchInfoCommand(ProxyConnection conn, String path) {
		super(conn);
		this.out = new DataOutputStream(conn.getCommandChannel().getOutputStream());
		this.in = new DataInputStream(conn.getCommandChannel().getInputStream());
		this.path = path;
	}

	public IFileInfo call() throws ProxyException {
		try {
			final StreamChannel chan = openChannel();

			out.writeByte(Protocol.PROTO_COMMAND);
			out.writeShort(Protocol.CMD_FETCHINFO);
			out.writeByte(chan.getId());
			out.writeUTF(path);
			out.flush();

			byte res = in.readByte();
			if (res != Protocol.PROTO_OK) {
				String errMsg = in.readUTF();
				throw new ProxyException(errMsg);
			}

			DataInputStream resultStream = new DataInputStream(chan.getInputStream());
			SerializableFileInfo info = new SerializableFileInfo();
			info.readObject(resultStream);
			chan.close();
			return info.getIFileInfo();
		} catch (IOException e) {
			throw new ProxyException(e.getMessage());
		}
	}
}
