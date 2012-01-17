/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Receiving, and printing to the console, stderr output
 * @since 7.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ErrorThread extends Thread {

	final MISession session;

	public ErrorThread(MISession s) {
		super("MI Error Thread"); //$NON-NLS-1$
		session = s;
	}

	/*
	 * Sit on the error stream output, and append to the GDB console
	 */
	@Override
	public void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(session.getChannelErrorStream()));
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				OutputStream console = session.getLogPipe();
				if (console != null) {
					console.write((line + "\n").getBytes()); //$NON-NLS-1$
					console.flush();
				}
			}
		} catch (IOException e) {
			try {
				reader.close();
			} catch (IOException e1) {/* closing anyway */}
		}
	}

}
