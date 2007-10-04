/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.speedtest;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

public class SpeedTestConnection extends Thread {
	private static int fgNo;
	private final ITerminalControl fControl;
	private final InputStream fInputStream;
	private final SpeedTestSettings fSettings;
	protected SpeedTestConnection(InputStream inputStream, SpeedTestSettings settings,ITerminalControl control) {
		super("SpeedTestConnection-"+fgNo++);
		fControl = control;
		fInputStream=inputStream;
		fSettings=settings;
	}
	public void run() {
		fControl.setState(TerminalState.CONNECTED);

		try {
			readDataForever(fInputStream);
		} catch (IOException e) {
			connectFailed(e.getMessage(),e.getMessage());
		}
		// when reading is done, we set the state to closed
		fControl.setState(TerminalState.CLOSED);
	}
    private void connectFailed(String terminalText, String msg) {
		Logger.log(terminalText);
		fControl.displayTextInTerminal(terminalText);
		fControl.setState(TerminalState.CLOSED);
		fControl.setMsg(msg);
	}
	/**
	 * Read the data from the input file and display it in the terminal.
	 * @param in
	 * @throws IOException
	 */
	private void readDataForever(InputStream in) throws IOException {
		long N=0;
		long T=0;
		long tDisplay=0;
		int NCalls=0;
		// read the data
		byte bytes[]=new byte[fSettings.getBufferSize()];
		// read until the thread gets interrupted....
		String info="";
		while(!isInterrupted()) {
			// read some bytes
			int n;
			if((n=in.read(bytes))==-1) {
				fControl.displayTextInTerminal("\033[2J\033c"+info);
//				long rate=(1000*N)/T;
//				setTitle(rate+" baud DONE");
//				try {
//					Thread.sleep(10000);
//				} catch (InterruptedException e) {
//					// no need to catch it
//				}
				return;
			}
			// we assume we get ASCII UTF8 bytes
			long t0=System.currentTimeMillis();
			fControl.getRemoteToTerminalOutputStream().write(bytes,0,n);
			long t=System.currentTimeMillis();
			T+=t-t0;
			N+=n;
			NCalls++;
			if(t-tDisplay>1000 && T>0) {
				long rate=(1000*N)/T;
				info=rate+" byte/s = "+rate*8+" baud "+"bytes/call="+N/NCalls;
				info=rate+" byte/s with buffer size "+fSettings.getBufferSize();
				setTitle(info);
				tDisplay=System.currentTimeMillis();
			}
		}
	}
	private void setTitle(final String title) {
		Display.getDefault().asyncExec(new Runnable(){
			public void run() {
				fControl.setTerminalTitle(title);
			}});
	}

}
