/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.server.core.commands;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.remote.proxy.protocol.core.StreamChannel;

/**
 * TODO: Fix hang if command fails...
 *
 */
public class ServerExecCommand extends AbstractServerExecCommand {
	public Process doRun() throws IOException {
		System.err.print("exec: ");
		for (String arg:getCommand()) {
			System.err.print(arg + " ");
		}
		System.err.println();
		ProcessBuilder builder = new ProcessBuilder(getCommand());
		try {
			if (!isAppendEnv()) {
				builder.environment().clear();
				builder.environment().putAll(getEnv());
			} else {
				for (Map.Entry<String, String> entry : getEnv().entrySet()) {
					String val = builder.environment().get(entry.getKey());
					if (val == null || !val.equals(entry.getValue())) {
						builder.environment().put(entry.getKey(), entry.getValue());
					}
				}
			}
		} catch (UnsupportedOperationException | IllegalArgumentException  e) {
			// Leave environment untouched
		}
		File dir = new File(getDirectory());
		if (dir.exists() && dir.isAbsolute()) {
			builder.directory(dir);
		}
		builder.redirectErrorStream(isRedirect());
		return builder.start();
	}
	
	protected void doKill(Process proc) {
		if (proc.isAlive()) {
			proc.destroyForcibly();
		}
	}
	
	protected void doSetTerminalSize(Process proc, int cols, int rows) {
		// Not supported
	}
	
	public ServerExecCommand(List<String> command, Map<String, String> env, String directory, boolean redirect, boolean appendEnv, StreamChannel cmdChan, StreamChannel ioChan, StreamChannel errChan) {
		super(command, env, directory, redirect, appendEnv, cmdChan, ioChan, errChan);
	}
}
