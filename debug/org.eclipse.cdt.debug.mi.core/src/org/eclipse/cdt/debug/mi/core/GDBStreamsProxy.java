/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.cdt.debug.mi.core.command.CLICommand;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;

/**
 */
public class GDBStreamsProxy implements IStreamsProxy {

	MISession session;
	GDBStreamMonitor miConsole;
	GDBStreamMonitor miLog;
	Writer out;
	int offset;
	
	public GDBStreamsProxy(MISession ses) {
		session = ses;
	}
	
	/**
	 * @see org.eclipse.debug.core.model.IStreamsProxy#getErrorStreamMonitor()
	 */
	public IStreamMonitor getErrorStreamMonitor() {
		if (miLog == null) {
			miLog = new GDBStreamMonitor(session.getMILogStream());
			miLog.startMonitoring();
		}
		return miLog;
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamsProxy#getOutputStreamMonitor()
	 */
	public IStreamMonitor getOutputStreamMonitor() {
		if (miConsole == null) {
			miConsole = new GDBStreamMonitor(session.getMIConsoleStream());
			miConsole.startMonitoring();
		}
		return miConsole;
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamsProxy#write(String)
	 */
	public void write(String input) throws IOException {
		if (out == null) {
			out = new Writer() {
				StringBuffer buf = new StringBuffer();
				public void write(char[] cbuf, int off, int len) throws IOException {
					for (int i = off; i < cbuf.length && len > 0; i++, len--) {
						if (cbuf[i] == '\n') {
							flush();
						} else {
							buf.append(cbuf[i]);
						}
					}
				}
				
				public void close () {
					buf.setLength(0);
				}
				
				// Encapsulate the string sent to gdb in a fake
				// command and post it to the TxThread.
				public void flush() throws IOException {
					CLICommand cmd = new CLICommand(buf.toString());
					buf.setLength(0);
					try {
						session.postCommand(cmd);
					} catch (MIException e) {
						// throw new IOException("no session:" + e.getMessage());
					}
				}
			};
		}

		if (input.length() > offset) {
			input = input.substring(offset);
			offset += input.length();
		} else {
			offset = input.length();
		}
		out.write(input);
	}
}
