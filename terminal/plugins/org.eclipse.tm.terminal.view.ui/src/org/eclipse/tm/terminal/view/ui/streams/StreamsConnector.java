/*******************************************************************************
 * Copyright (c) 2011, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.streams;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.NullSettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.ui.services.IDisposable;

/**
 * Streams connector implementation.
 */
public class StreamsConnector extends AbstractStreamsConnector implements IDisposable {
	// Reference to the streams settings
	private final StreamsSettings settings;

	public StreamsConnector() {
		this(new StreamsSettings());
	}

	/**
	 * Constructor.
	 *
	 * @param settings The streams settings. Must not be <code>null</code>
	 */
	public StreamsConnector(StreamsSettings settings) {
		super();

		Assert.isNotNull(settings);
		this.settings = settings;
	}

	@Override
	public void connect(ITerminalControl control) {
		Assert.isNotNull(control);
		super.connect(control);

		// Setup the listeners
		setStdoutListeners(settings.getStdOutListeners());
		setStderrListeners(settings.getStdErrListeners());

		// connect the streams
		connectStreams(control, settings.getStdinStream(), settings.getStdoutStream(), settings.getStderrStream(),
				settings.isLocalEcho(), settings.getLineSeparator());

		// Set the terminal control state to CONNECTED
		control.setState(TerminalState.CONNECTED);
	}

	@Override
	public boolean isLocalEcho() {
		return settings.isLocalEcho();
	}

	@Override
	public void dispose() {
		disconnect();
	}

	@Override
	public void doDisconnect() {
		// Dispose the streams
		super.doDisconnect();

		// Set the terminal control state to CLOSED.
		fControl.setState(TerminalState.CLOSED);
	}

	@Override
	public String getSettingsSummary() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public void setDefaultSettings() {
		settings.load(new NullSettingsStore());
	}

	@Override
	public void load(ISettingsStore store) {
		settings.load(store);
	}

	@Override
	public void save(ISettingsStore store) {
		settings.save(store);
	}
}
