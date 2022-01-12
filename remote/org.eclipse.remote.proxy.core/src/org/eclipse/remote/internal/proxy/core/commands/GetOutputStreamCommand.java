/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.core.commands;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.remote.internal.proxy.core.ProxyConnection;
import org.eclipse.remote.proxy.protocol.core.Protocol;
import org.eclipse.remote.proxy.protocol.core.StreamChannel;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public class GetOutputStreamCommand extends AbstractCommand<OutputStream> {

	private final DataOutputStream out;
	private final DataInputStream in;
	private final int options;
	private final String path;

	public GetOutputStreamCommand(ProxyConnection conn, int options, String path) {
		super(conn);
		this.out = new DataOutputStream(conn.getCommandChannel().getOutputStream());
		this.in = new DataInputStream(conn.getCommandChannel().getInputStream());
		this.options = options;
		this.path = path;
	}

	public OutputStream call() throws ProxyException {
		try {
			StreamChannel chan = openChannel();

			out.writeByte(Protocol.PROTO_COMMAND);
			out.writeShort(Protocol.CMD_GETOUTPUTSTREAM);
			out.writeByte(chan.getId());
			out.writeInt(options);
			out.writeUTF(path);
			out.flush();
			
			byte res = in.readByte();
			if (res != Protocol.PROTO_OK) {
				String errMsg = in.readUTF();
				throw new ProxyException(errMsg);
			}
			
			return new BufferedOutputStream(chan.getOutputStream());
		} catch (IOException e) {
			throw new ProxyException(e.getMessage());
		}
	}
}
