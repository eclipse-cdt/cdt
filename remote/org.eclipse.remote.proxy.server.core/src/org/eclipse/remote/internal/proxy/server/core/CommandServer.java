/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.server.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.remote.internal.proxy.server.core.commands.AbstractServerCommand;
import org.eclipse.remote.internal.proxy.server.core.commands.ServerChildInfosCommand;
import org.eclipse.remote.internal.proxy.server.core.commands.ServerDeleteCommand;
import org.eclipse.remote.internal.proxy.server.core.commands.ServerExecCommand;
import org.eclipse.remote.internal.proxy.server.core.commands.ServerFetchInfoCommand;
import org.eclipse.remote.internal.proxy.server.core.commands.ServerGetCwdCommand;
import org.eclipse.remote.internal.proxy.server.core.commands.ServerGetEnvCommand;
import org.eclipse.remote.internal.proxy.server.core.commands.ServerGetInputStreamCommand;
import org.eclipse.remote.internal.proxy.server.core.commands.ServerGetOutputStreamCommand;
import org.eclipse.remote.internal.proxy.server.core.commands.ServerGetPropertiesCommand;
import org.eclipse.remote.internal.proxy.server.core.commands.ServerMkdirCommand;
import org.eclipse.remote.internal.proxy.server.core.commands.ServerPutInfoCommand;
import org.eclipse.remote.internal.proxy.server.core.commands.ServerShellCommand;
import org.eclipse.remote.proxy.protocol.core.Protocol;
import org.eclipse.remote.proxy.protocol.core.SerializableFileInfo;
import org.eclipse.remote.proxy.protocol.core.StreamChannel;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public class CommandServer implements Runnable {
	private Server server;
	private DataInputStream cmdIn;
	private DataOutputStream cmdOut;
	private boolean running = true;
	private StreamChannel chan;
	
	public CommandServer(StreamChannel chan, Server server) {
		this.chan = chan;
		this.server = server;
		this.cmdIn = new DataInputStream(chan.getInputStream());
		this.cmdOut = new DataOutputStream(chan.getOutputStream());
	}
	
	public void run() {
		new Thread("cmd reader") { //$NON-NLS-1$
			@Override
			public void run() {
				try {
					while (running) {
						byte proto = cmdIn.readByte();
						switch (proto) {
						case Protocol.PROTO_COMMAND:
							try {
								dispatchCommand(cmdIn);
								sendOKResult();
							} catch (ProxyException e) {
								sendErrorResult(e.getMessage());
							}
							break;
							
						case Protocol.PROTO_SHUTDOWN:
							running = false;
							break;
							
						default:
							System.err.println("Invalid protocol ID: " + proto);
							break;
						}
					}
				} catch (EOFException e) {
					// Exit server
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					running = false;
				}
			}
		}.start();
	}
	
	private void sendOKResult() throws IOException {
		cmdOut.writeByte(Protocol.PROTO_OK);
		cmdOut.flush();
	}
	
	private void sendErrorResult(String error) throws IOException {
		cmdOut.writeByte(Protocol.PROTO_ERROR);
		cmdOut.writeUTF(error);
		cmdOut.flush();
	}

	
	/**
	 * TODO replace with dynamic dispatcher
	 */
	private void dispatchCommand(DataInputStream in) throws ProxyException, IOException {
		AbstractServerCommand serverCmd;
		
		short cmd = in.readShort();
		switch (cmd) {
		case Protocol.CMD_CHILDINFOS:
			serverCmd = cmdChildInfos(in);
			break;
			
		case Protocol.CMD_DELETE:
			serverCmd = cmdDelete(in);
			break;
			
		case Protocol.CMD_EXEC:
			serverCmd = cmdExec(in);
			break;

		case Protocol.CMD_SHELL:
			serverCmd = cmdShell(in);
			break;

		case Protocol.CMD_FETCHINFO:
			serverCmd = cmdFetchInfo(in);
			break;
			
		case Protocol.CMD_GETCWD:
			serverCmd = cmdGetCwd(in);
			break;
			
		case Protocol.CMD_GETENV:
			serverCmd = cmdGetEnv(in);
			break;

		case Protocol.CMD_GETINPUTSTREAM:
			serverCmd = cmdGetInputStream(in);
			break;

		case Protocol.CMD_GETOUTPUTSTREAM:
			serverCmd = cmdGetOutputStream(in);
			break;
 
		case Protocol.CMD_GETPROPERTIES:
			serverCmd = cmdGetProperties(in);
			break;

		case Protocol.CMD_MKDIR:
			serverCmd = cmdMkdir(in);
			break;
			
		case Protocol.CMD_PUTINFO:
			serverCmd = cmdPutInfo(in);
			break;
			
		default:
			System.err.println("Invalid command ID: " + cmd);
			throw new ProxyException("Invalid command ID: " + cmd); //$NON-NLS-1$
		}
		
		serverCmd.exec();
	}
	
	private AbstractServerCommand cmdExec(DataInputStream in) throws ProxyException, IOException {
		int cmdChanId = in.readByte();
		int ioChanId = in.readByte();
		int errChanId = in.readByte();
		int length = in.readInt();
		List<String> command = new ArrayList<String>(length);
		for (int i = 0; i < length; i++) {
			command.add(in.readUTF());
		}
		length = in.readInt();
		Map<String, String> env = new HashMap<String, String>(length);
		for (int i = 0; i < length; i++) {
			String key = in.readUTF();
			String val = in.readUTF();
			env.put(key, val);
		}
		String dir = in.readUTF();
		boolean redirect = in.readBoolean();
		boolean appendEnv = in.readBoolean();
		StreamChannel cmdChan = server.getChannel(cmdChanId);
		StreamChannel ioChan = server.getChannel(ioChanId);
		StreamChannel errChan= server.getChannel(errChanId);
		if (cmdChan == null || ioChan == null || errChan == null) {
			throw new ProxyException("Unable to locate channels for command"); //$NON-NLS-1$
		}
		return new ServerExecCommand(command, env, dir, redirect, appendEnv, cmdChan, ioChan, errChan);
	}

	private AbstractServerCommand cmdShell(DataInputStream in) throws ProxyException, IOException {
		int cmdChanId = in.readByte();
		int ioChanId = in.readByte();
		StreamChannel cmdChan = server.getChannel(cmdChanId);
		StreamChannel ioChan = server.getChannel(ioChanId);
		if (cmdChan == null || ioChan == null) {
			throw new ProxyException("Unable to locate channels for command"); //$NON-NLS-1$
		}
		return new ServerShellCommand(cmdChan, ioChan);
	}
	private AbstractServerCommand cmdGetCwd(DataInputStream in) throws ProxyException, IOException {
		int chanId = in.readByte();
		StreamChannel chan = server.getChannel(chanId);
		if (chan == null) {
			throw new ProxyException("Unable to locate channel for command"); //$NON-NLS-1$
		}
		return new ServerGetCwdCommand(chan);
	}
	
	private AbstractServerCommand cmdGetEnv(DataInputStream in) throws ProxyException, IOException {
		int chanId = in.readByte();
		StreamChannel chan = server.getChannel(chanId);
		if (chan == null) {
			throw new ProxyException("Unable to locate channel for command"); //$NON-NLS-1$
		}
		return new ServerGetEnvCommand(chan);
	}
	
	private AbstractServerCommand cmdGetProperties(DataInputStream in) throws ProxyException, IOException {
		int chanId = in.readByte();
		StreamChannel chan = server.getChannel(chanId);
		if (chan == null) {
			throw new ProxyException("Unable to locate channel for command"); //$NON-NLS-1$
		}
		return new ServerGetPropertiesCommand(chan);
	}
	
	private AbstractServerCommand cmdChildInfos(DataInputStream in) throws ProxyException, IOException {
		int chanId = in.readByte();
		StreamChannel chan = server.getChannel(chanId);
		if (chan == null) {
			throw new ProxyException("Unable to locate channel for command"); //$NON-NLS-1$
		}
		String path = in.readUTF();
		return new ServerChildInfosCommand(chan, path);
	}

	private AbstractServerCommand cmdFetchInfo(DataInputStream in) throws ProxyException, IOException {
		int chanId = in.readByte();
		StreamChannel chan = server.getChannel(chanId);
		if (chan == null) {
			throw new ProxyException("Unable to locate channel for command"); //$NON-NLS-1$
		}
		String path = in.readUTF();
		return new ServerFetchInfoCommand(chan, path);
	}
	
	private AbstractServerCommand cmdGetInputStream(DataInputStream in) throws ProxyException, IOException {
		int chanId = in.readByte();
		StreamChannel chan = server.getChannel(chanId);
		if (chan == null) {
			throw new ProxyException("Unable to locate channel for command"); //$NON-NLS-1$
		}
		int options = in.readInt();
		String path = in.readUTF();
		return new ServerGetInputStreamCommand(chan, options, path);
	}
	
	private AbstractServerCommand cmdGetOutputStream(DataInputStream in) throws ProxyException, IOException {
		int chanId = in.readByte();
		StreamChannel chan = server.getChannel(chanId);
		if (chan == null) {
			throw new ProxyException("Unable to locate channel for command"); //$NON-NLS-1$
		}
		int options = in.readInt();
		String path = in.readUTF();
		return new ServerGetOutputStreamCommand(chan, options, path);
	}

	private AbstractServerCommand cmdDelete(DataInputStream in) throws ProxyException, IOException {
		int options = in.readInt();
		String path = in.readUTF();
		return new ServerDeleteCommand(options, path);
	}

	private AbstractServerCommand cmdMkdir(DataInputStream in) throws ProxyException, IOException {
		int options = in.readInt();
		String path = in.readUTF();
		return new ServerMkdirCommand(options, path);
	}
	
	private AbstractServerCommand cmdPutInfo(DataInputStream in) throws ProxyException, IOException {
		int options = in.readInt();
		String path = in.readUTF();
		SerializableFileInfo info = new SerializableFileInfo();
		info.readObject(in);
		return new ServerPutInfoCommand(info.getIFileInfo(), options, path);
	}
}
