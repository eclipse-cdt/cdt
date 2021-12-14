/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.core.commands;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.remote.internal.proxy.core.ProxyConnection;
import org.eclipse.remote.proxy.protocol.core.Protocol;
import org.eclipse.remote.proxy.protocol.core.StreamChannel;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public class GetCwdCommand extends AbstractCommand<String> {

	private final DataOutputStream out;
	private final DataInputStream in;

	public GetCwdCommand(ProxyConnection conn) {
		super(conn);
		this.out = new DataOutputStream(conn.getCommandChannel().getOutputStream());
		this.in = new DataInputStream(conn.getCommandChannel().getInputStream());
	}

	public String call() throws ProxyException {
		try {
			final StreamChannel chan = openChannel();
			DataInputStream resultStream = new DataInputStream(chan.getInputStream());
			
			out.writeByte(Protocol.PROTO_COMMAND);
			out.writeShort(Protocol.CMD_GETCWD);
			out.writeByte(chan.getId());
			out.flush();
			
			byte res = in.readByte();
			if (res != Protocol.PROTO_OK) {
				String errMsg = in.readUTF();
				throw new ProxyException(errMsg);
			}
			
			String cwd = resultStream.readUTF();
			chan.close();
			return cwd;
		} catch (IOException e) {
			throw new ProxyException(e.getMessage());
		}
	}
}
