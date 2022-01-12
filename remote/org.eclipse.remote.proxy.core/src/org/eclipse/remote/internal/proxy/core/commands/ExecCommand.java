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
import java.util.List;
import java.util.Map;

import org.eclipse.remote.internal.proxy.core.ProxyConnection;
import org.eclipse.remote.proxy.protocol.core.Protocol;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public class ExecCommand extends AbstractCommand<Void> {

	private final DataOutputStream out;
	private final DataInputStream in;
	private final List<String> command;
	private final Map<String, String> env;
	private final String directory;
	private final boolean appendEnv;
	private final boolean redirect;
	private final int cmdChan;
	private final int ioChan;
	private final int errChan;

	public ExecCommand(ProxyConnection conn, List<String> command, Map<String, String> env, String directory,
			boolean redirect, boolean appendEnv, int cmdChan, int ioChan, int errChan) {
		super(conn);
		this.out = new DataOutputStream(conn.getCommandChannel().getOutputStream());
		this.in = new DataInputStream(conn.getCommandChannel().getInputStream());
		this.command = command;
		this.env = env;
		this.directory = directory;
		this.redirect = redirect;
		this.appendEnv = appendEnv;
		this.cmdChan = cmdChan;
		this.ioChan = ioChan;
		this.errChan = errChan;
	}

	public Void call() throws ProxyException {
		try {
			out.writeByte(Protocol.PROTO_COMMAND);
			out.writeShort(Protocol.CMD_EXEC);
			out.writeByte(cmdChan);
			out.writeByte(ioChan);
			out.writeByte(errChan);
			out.writeInt(command.size());
			for (String arg : command) {
				out.writeUTF(arg);
			}
			out.writeInt(env.size());
			for (Map.Entry<String, String> entry : env.entrySet()) {
				out.writeUTF(entry.getKey());
				out.writeUTF(entry.getValue());
			}
			out.writeUTF(directory);
			out.writeBoolean(redirect);
			out.writeBoolean(appendEnv);
			out.flush();

			byte res = in.readByte();
			if (res != Protocol.PROTO_OK) {
				String errMsg = in.readUTF();
				throw new ProxyException(errMsg);
			}
		} catch (IOException e) {
			throw new ProxyException(e.getMessage());
		}
		return null;
	}
}
