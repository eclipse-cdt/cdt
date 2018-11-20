/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import java.io.OutputStream;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

/**
 * Class that connects the GDB process I/O with the terminal.
 */
public class GdbTerminalPageConnector extends PlatformObject implements ITerminalConnector {

	private int fTerminalWidth, fTerminalHeight;
	private ITerminalControl fControl;
	private final PTY fPty;
	private final IGdbTerminalControlConnector fGdbTerminalCtrlConnector;

	public GdbTerminalPageConnector(IGdbTerminalControlConnector gdbTerminalCtrlConnector, PTY pty) {
		fPty = pty;
		fGdbTerminalCtrlConnector = gdbTerminalCtrlConnector;
	}

	@Override
	public void disconnect() {
		fGdbTerminalCtrlConnector.removePageTerminalControl(fControl);

		if (fControl != null) {
			fControl.setState(TerminalState.CLOSED);
		}
	}

	@Override
	public OutputStream getTerminalToRemoteStream() {
		return fGdbTerminalCtrlConnector.getTerminalToRemoteStream();
	}

	@Override
	public void connect(ITerminalControl control) {
		if (control == null) {
			throw new IllegalArgumentException("Invalid ITerminalControl"); //$NON-NLS-1$
		}

		fControl = control;
		fGdbTerminalCtrlConnector.addPageTerminalControl(fControl);

		// Set the terminal control state to CONNECTED
		fControl.setState(TerminalState.CONNECTED);
	}

	@Override
	public void setTerminalSize(int newWidth, int newHeight) {
		if (newWidth != fTerminalWidth || newHeight != fTerminalHeight) {
			fTerminalWidth = newWidth;
			fTerminalHeight = newHeight;
			if (fPty != null) {
				fPty.setTerminalSize(newWidth, newHeight);
			}
		}
	}

	@Override
	public String getId() {
		// No need for an id, as we're are just used locally
		return null;
	}

	@Override
	public String getName() {
		// No name
		return null;
	}

	@Override
	public boolean isHidden() {
		// in case we do leak into the TM world, we shouldn't be visible
		return true;
	}

	@Override
	public boolean isInitialized() {
		return true;
	}

	@Override
	public String getInitializationErrorMessage() {
		return null;
	}

	@Override
	public boolean isLocalEcho() {
		return false;
	}

	@Override
	public void setDefaultSettings() {
		// we don't do settings
	}

	@Override
	public String getSettingsSummary() {
		// we don't do settings
		return null;
	}

	@Override
	public void load(ISettingsStore arg0) {
		// we don't do settings
	}

	@Override
	public void save(ISettingsStore arg0) {
		// we don't do settings
	}
}
