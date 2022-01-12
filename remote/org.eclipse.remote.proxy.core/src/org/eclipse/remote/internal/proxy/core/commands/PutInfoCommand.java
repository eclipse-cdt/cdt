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
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public class PutInfoCommand extends AbstractCommand<Void> {

	private final DataOutputStream out;
	private final DataInputStream in;
	private final IFileInfo info;
	private final int options;
	private final String path;

	public PutInfoCommand(ProxyConnection conn, IFileInfo info, int options, String path) {
		super(conn);
		this.out = new DataOutputStream(conn.getCommandChannel().getOutputStream());
		this.in = new DataInputStream(conn.getCommandChannel().getInputStream());
		this.info = info;
		this.options = options;
		this.path = path;
	}

	public Void call() throws ProxyException {
		try {
			out.writeByte(Protocol.PROTO_COMMAND);
			out.writeShort(Protocol.CMD_PUTINFO);
			out.writeInt(options);
			out.writeUTF(path);
			SerializableFileInfo sInfo = new SerializableFileInfo(info);
			sInfo.writeObject(out);
			out.flush();

			byte res = in.readByte();
			if (res != Protocol.PROTO_OK) {
				String errMsg = in.readUTF();
				throw new ProxyException(errMsg);
			}
			return null;
		} catch (IOException e) {
			throw new ProxyException(e.getMessage());
		}
	}
}
