/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.provisional.api.provider;

import java.io.OutputStream;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;

/**
 * Abstract base class for all terminal connector implementations to be
 * registered via the <code>org.eclipse.tm.terminal.terminalConnectors</code>
 * extension point.
 *
 * @since org.eclipse.tm.terminal 2.0
 */
public abstract class TerminalConnectorImpl {

	/**
	 * Initialize this connector. This is called once after the constructor, in
	 * order to perform any required initializations such as loading required
	 * native libraries. Any work that may lead to runtime exceptions should be
	 * done in this method rather than in the constructor.
	 *
	 * @throws Exception when the connector fails to initialize (due to missing
	 *             required libraries, for instance).
	 */
	public void initialize() throws Exception {
	}

	/**
	 * Connect using the current state of the settings.
	 * @param control Used to inform the UI about state changes and messages from the connection.
	 */
	abstract public void connect(ITerminalControl control);

	/**
	 * Disconnect if connected. Else do nothing.
	 * Has to set the state of the {@link ITerminalControl}
	 */
	abstract public void disconnect();

    /**
     * @return the terminal to remote stream (bytes written to this stream will
     * be sent to the remote site). For the stream in the other direction (remote to
     * terminal see {@link ITerminalControl#getRemoteToTerminalOutputStream()}
     */
	abstract public OutputStream getOutputStream();

	/**
	 * @return A string that represents the settings of the connection. This representation
	 * may be shown in the status line of the terminal view.
	 */
	abstract public String getSettingsSummary();

	/**
	 * @return true if a local echo is needed.
	 * TODO:Michael Scharf: this should be handed within the connection....
	 */
	public boolean isLocalEcho() {
		return false;
	}

	/**
	 * @return a new page that can be used in a dialog to setup this connection.
	 * The dialog should persist its settings with the {@link #load(ISettingsStore)}
	 * and {@link #save(ISettingsStore)} methods.
	 *
	 */
	abstract public ISettingsPage makeSettingsPage();

	/**
	 * Load the state of this connection. Is typically called before
	 * {@link #connect(ITerminalControl)}.
	 *
	 * @param store a string based data store. Short keys like "foo" can be used to
	 * store the state of the connection.
	 */
	abstract public void load(ISettingsStore store);

	/**
	 * When the view or dialog containing the terminal is closed, the state of
	 * the connection is saved into the settings store <code>store</code>
	 *
	 * @param store the store for persisting settings.
	 */
	abstract public void save(ISettingsStore store);

    /**
	 * Notify the remote site that the size of the terminal has changed.
	 *
	 * @param newWidth the new width in characters.
	 * @param newHeight the new height in characters.
	 */
	public void setTerminalSize(int newWidth, int newHeight) {
	}
}
