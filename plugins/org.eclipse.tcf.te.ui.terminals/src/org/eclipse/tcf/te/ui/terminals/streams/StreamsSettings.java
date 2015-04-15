/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.streams;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.core.terminals.interfaces.ITerminalServiceOutputStreamMonitorListener;
import org.eclipse.tcf.te.ui.terminals.internal.SettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

/**
 * Streams connector settings implementation.
 */
@SuppressWarnings("restriction")
public class StreamsSettings {
	// Reference to the stdin stream
	private OutputStream stdin;
	// Reference to the stdout stream
	private InputStream stdout;
	// Reference to the stderr stream
	private InputStream stderr;
	// Flag to control the local echo
	private boolean localEcho = true;
	// The line separator setting
	private String lineSeparator = null;
    // The list of stdout output listeners
    private ITerminalServiceOutputStreamMonitorListener[] stdoutListeners = null;
    // The list of stderr output listeners
    private ITerminalServiceOutputStreamMonitorListener[] stderrListeners = null;

	/**
	 * Sets the stdin stream instance.
	 *
	 * @param stdin The stream instance or <code>null</code>.
	 */
	public void setStdinStream(OutputStream stdin) {
		this.stdin = stdin;
	}

	/**
	 * Returns the stdin stream instance.
	 *
	 * @return The stream instance or <code>null</code>.
	 */
	public OutputStream getStdinStream() {
		return stdin;
	}

	/**
	 * Sets the stdout stream instance.
	 *
	 * @param stdout The stream instance or <code>null</code>.
	 */
	public void setStdoutStream(InputStream stdout) {
		this.stdout = stdout;
	}

	/**
	 * Returns the stdout stream instance.
	 *
	 * @return The stream instance or <code>null</code>.
	 */
	public InputStream getStdoutStream() {
		return stdout;
	}

	/**
	 * Sets the stderr stream instance.
	 *
	 * @param stderr The stream instance or <code>null</code>.
	 */
	public void setStderrStream(InputStream stderr) {
		this.stderr = stderr;
	}

	/**
	 * Returns the stderr stream instance.
	 *
	 * @return The stream instance or <code>null</code>.
	 */
	public InputStream getStderrStream() {
		return stderr;
	}

	/**
	 * Sets if the process requires a local echo from the terminal widget.
	 *
	 * @param value Specify <code>true</code> to enable the local echo, <code>false</code> otherwise.
	 */
	public void setLocalEcho(boolean value) {
		this.localEcho = value;
	}

	/**
	 * Returns <code>true</code> if the process requires a local echo
	 * from the terminal widget.
	 *
	 * @return <code>True</code> if local echo is enabled, <code>false</code> otherwise.
	 */
	public boolean isLocalEcho() {
		return localEcho;
	}

	/**
	 * Sets the stream line separator.
	 *
	 * @param separator The stream line separator <code>null</code>.
	 */
	public void setLineSeparator(String separator) {
		this.lineSeparator = separator;
	}

	/**
	 * Returns the stream line separator.
	 *
	 * @return The stream line separator or <code>null</code>.
	 */
	public String getLineSeparator() {
		return lineSeparator;
	}

	/**
	 * Sets the list of stdout listeners.
	 *
	 * @param listeners The list of stdout listeners or <code>null</code>.
	 */
	public void setStdOutListeners(ITerminalServiceOutputStreamMonitorListener[] listeners) {
		this.stdoutListeners = listeners;
	}

	/**
	 * Returns the list of stdout listeners.
	 *
	 * @return The list of stdout listeners or <code>null</code>.
	 */
	public ITerminalServiceOutputStreamMonitorListener[] getStdOutListeners() {
		return stdoutListeners;
	}

	/**
	 * Sets the list of stderr listeners.
	 *
	 * @param listeners The list of stderr listeners or <code>null</code>.
	 */
	public void setStdErrListeners(ITerminalServiceOutputStreamMonitorListener[] listeners) {
		this.stderrListeners = listeners;
	}

	/**
	 * Returns the list of stderr listeners.
	 *
	 * @return The list of stderr listeners or <code>null</code>.
	 */
	public ITerminalServiceOutputStreamMonitorListener[] getStdErrListeners() {
		return stderrListeners;
	}

	/**
	 * Loads the streams settings from the given settings store.
	 *
	 * @param store The settings store. Must not be <code>null</code>.
	 */
	public void load(ISettingsStore store) {
		Assert.isNotNull(store);
		localEcho = Boolean.parseBoolean(store.get("LocalEcho", Boolean.FALSE.toString())); //$NON-NLS-1$
		lineSeparator = store.get("LineSeparator", null); //$NON-NLS-1$
		if (store instanceof SettingsStore) {
			stdin = (OutputStream)((SettingsStore)store).getSettings().get("stdin"); //$NON-NLS-1$
			stdout = (InputStream)((SettingsStore)store).getSettings().get("stdout"); //$NON-NLS-1$
			stderr = (InputStream)((SettingsStore)store).getSettings().get("stderr"); //$NON-NLS-1$
			stdoutListeners = (ITerminalServiceOutputStreamMonitorListener[])((SettingsStore)store).getSettings().get("StdOutListeners"); //$NON-NLS-1$
			stderrListeners = (ITerminalServiceOutputStreamMonitorListener[])((SettingsStore)store).getSettings().get("StdErrListeners"); //$NON-NLS-1$
		}
	}

	/**
	 * Saves the process settings to the given settings store.
	 *
	 * @param store The settings store. Must not be <code>null</code>.
	 */
	public void save(ISettingsStore store) {
		Assert.isNotNull(store);
		store.put("LocalEcho", Boolean.toString(localEcho)); //$NON-NLS-1$
		store.put("LineSeparator", lineSeparator); //$NON-NLS-1$
		if (store instanceof SettingsStore) {
			((SettingsStore)store).getSettings().put("stdin", stdin); //$NON-NLS-1$
			((SettingsStore)store).getSettings().put("stdout", stdout); //$NON-NLS-1$
			((SettingsStore)store).getSettings().put("stderr", stderr); //$NON-NLS-1$
			((SettingsStore)store).getSettings().put("StdOutListeners", stdoutListeners); //$NON-NLS-1$
			((SettingsStore)store).getSettings().put("StdErrListeners", stderrListeners); //$NON-NLS-1$
		}
	}
}
