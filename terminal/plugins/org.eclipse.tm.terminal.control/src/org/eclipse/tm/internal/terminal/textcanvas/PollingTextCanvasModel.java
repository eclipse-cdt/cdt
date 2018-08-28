/*******************************************************************************
 * Copyright (c) 2007, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 * Anton Leherbauer (Wind River) - [420928] Terminal widget leaks memory
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.textcanvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.terminal.model.ITerminalTextDataSnapshot;

/**
 * @author Michael.Scharf@scharf-software.com
 *
 */
public class PollingTextCanvasModel extends AbstractTextCanvasModel {
	private static final int DEFAULT_POLL_INTERVAL = 50;
	int fPollInterval = -1;

	/**
	 * 
	 */
	public PollingTextCanvasModel(ITerminalTextDataSnapshot snapshot) {
		super(snapshot);
		startPolling();
	}
	public void setUpdateInterval(int t) {
		fPollInterval = t;
	}
	public void stopPolling() {
		// timerExec only dispatches if the delay is >=0
		fPollInterval = -1;
	}
	public void startPolling() {
		if (fPollInterval < 0) {
			fPollInterval = DEFAULT_POLL_INTERVAL;
			Display.getDefault().timerExec(fPollInterval, new Runnable(){
				public void run() {
					update();
					Display.getDefault().timerExec(fPollInterval, this);
				}
			});
		}
	}
}
