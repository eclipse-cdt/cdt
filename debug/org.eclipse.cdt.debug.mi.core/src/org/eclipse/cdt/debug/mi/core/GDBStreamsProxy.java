/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.cdt.debug.mi.core.command.CLICommand;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;

/**
 */
public class GDBStreamsProxy implements IStreamsProxy {

	MISession session;
	GDBStreamMonitor miConsole;
	GDBStreamMonitor miLog;
	OutputStream out;
	
	public GDBStreamsProxy(MISession ses) {
		session = ses;
	}
	
	/**
	 * @see org.eclipse.debug.core.model.IStreamsProxy#getErrorStreamMonitor()
	 */
	public IStreamMonitor getErrorStreamMonitor() {
		if (miLog == null) {
			miLog = new GDBStreamMonitor(session.getMILogStream());
		}
		return miLog;
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamsProxy#getOutputStreamMonitor()
	 */
	public IStreamMonitor getOutputStreamMonitor() {
		if (miConsole == null) {
			miConsole = new GDBStreamMonitor(session.getMIConsoleStream());
		}
		return miConsole;
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamsProxy#write(String)
	 */
	public void write(String input) throws IOException {
		if (out == null) {
			out = new OutputStream() {
				StringBuffer buf = new StringBuffer();
				public void write(int b) throws IOException {
					buf.append((char)b);
					if (b == '\n') {
						flush();
					}
				}
				// Encapsulate the string sent to gdb in a fake
				// command and post it to the TxThread.
				public void flush() throws IOException {
					CLICommand cmd = new CLICommand(buf.toString()) {
						public void setToken(int token) {
							token = token;
							// override to do nothing;
						}
					};
					try {
						session.postCommand(cmd);
					} catch (MIException e) {
						throw new IOException("no session");
					}
				}
			};
		}
		out.write(input.getBytes());
	}
}
