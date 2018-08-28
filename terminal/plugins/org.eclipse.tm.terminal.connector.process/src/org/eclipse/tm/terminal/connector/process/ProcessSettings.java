/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.process;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalServiceOutputStreamMonitorListener;
import org.eclipse.tm.terminal.view.ui.internal.SettingsStore;

/**
 * Process connector settings implementation.
 */
@SuppressWarnings("restriction")
public class ProcessSettings {
	// Reference to the process image
	private String image;
	// Reference to the process arguments (space separated string)
	private String arguments;
	// Reference to the process object
	private Process process;
	// Reference to the pseudo terminal object
	private PTY pty;
	// Flag to control the local echo (defaults to true if
	// the PTY is not supported on the current host platform)
	private boolean localEcho = !PTY.isSupported();
	// The line separator setting
	private String lineSeparator = null;
	// The list of stdout output listeners
	private ITerminalServiceOutputStreamMonitorListener[] stdoutListeners = null;
	// The list of stderr output listeners
	private ITerminalServiceOutputStreamMonitorListener[] stderrListeners = null;
	// working directory for process
	private String workingDir;
	// environment
	private String[] environment;
	// Flag to control if the provided environment is
	// automatically merged with the native process environment.
	// Defaults to "true".
	private boolean mergeWithNativeEnvironment = true;

	/**
	 * Sets the process image.
	 *
	 * @param image The process image or <code>null</code>.
	 */
	public void setImage(String image) {
		this.image = image;
	}

	/**
	 * Returns the process image.
	 *
	 * @return The process image or <code>null</code>.
	 */
	public String getImage() {
		return image;
	}

	/**
	 * Sets the process arguments.
	 * <p>
	 * The arguments are space separated. The caller is responsible for
	 * correct quoting.
	 *
	 * @param arguments The process arguments or <code>null</code>.
	 */
	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	/**
	 * Returns the process arguments.
	 *
	 * @return The process arguments as space separated list or <code>null</code>.
	 */
	public String getArguments() {
		return arguments;
	}

	/**
	 * Sets the process object.
	 *
	 * @param image The process object or <code>null</code>.
	 */
	public void setProcess(Process process) {
		this.process = process;
	}

	/**
	 * Returns the process object.
	 *
	 * @return The process object or <code>null</code>.
	 */
	public Process getProcess() {
		return process;
	}

	/**
	 * Sets the pseudo terminal object.
	 *
	 * @param pty The pseudo terminal or <code>null</code>.
	 */
	public void setPTY(PTY pty) {
		this.pty = pty;
		// If the PTY is set to "null", the local echo will be set to "true"
		if (pty == null) setLocalEcho(true);
	}

	/**
	 * Returns the pseudo terminal object.
	 *
	 * @return The pseudo terminal or <code>null</code>.
	 */
	public PTY getPTY() {
		return pty;
	}

	/**
	 * Sets if the process requires a local echo from the
	 * terminal widget.
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
	 * Sets the process line separator.
	 *
	 * @param separator The process line separator <code>null</code>.
	 */
	public void setLineSeparator(String separator) {
		this.lineSeparator = separator;
	}

	/**
	 * Returns the process line separator.
	 *
	 * @return The process line separator or <code>null</code>.
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
	 * Returns the working directory
	 *
	 * @return
	 */
	public String getWorkingDir() {
		return this.workingDir;
	}

	/**
	 * Sets the working directory of the process
	 *
	 * @param workingDir the absolute path of the working directory
	 */
	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}

	/**
	 * Get the process environment
	 *
	 * @return
	 */
	public String[] getEnvironment() {
		return environment;
	}

	/**
	 * Sets the process environment
	 *
	 * @param environment - will be added to the "parent" environment of the process
	 */
	public void setEnvironment(String[] environment) {
		this.environment = environment;
	}

	/**
	 * Returns if or if not the provided environment is merged with
	 * the native process environment.
	 *
	 * @return <code>True</code> if the provided environment is merged with the native process environment, <code>false</code> otherwise.
	 */
	public boolean isMergeWithNativeEnvironment() {
		return mergeWithNativeEnvironment;
	}

	/**
	 * Sets if or if not the provided environment is merged with the
	 * native process environment.
	 *
	 * @param value <code>True</code> if the provided environment is merged with the native process environment, <code>false</code> otherwise.
	 */
	public void setMergeWithNativeEnvironment(boolean value) {
		this.mergeWithNativeEnvironment = value;
	}

	/**
	 * Loads the process settings from the given settings store.
	 *
	 * @param store The settings store. Must not be <code>null</code>.
	 */
	public void load(ISettingsStore store) {
		Assert.isNotNull(store);
		image = store.get("Path", null);//$NON-NLS-1$
		arguments = store.get("Arguments", null); //$NON-NLS-1$
		localEcho = Boolean.parseBoolean(store.get("LocalEcho", Boolean.FALSE.toString())); //$NON-NLS-1$
		mergeWithNativeEnvironment = Boolean.parseBoolean(store.get("MergeWithNativeEnvironment", Boolean.FALSE.toString())); //$NON-NLS-1$
		lineSeparator = store.get("LineSeparator", null); //$NON-NLS-1$
		workingDir = store.get("WorkingDir", null); //$NON-NLS-1$
		if (store instanceof SettingsStore) {
			process = (Process)((SettingsStore)store).getSettings().get("Process"); //$NON-NLS-1$
			pty = (PTY)((SettingsStore)store).getSettings().get("PTY"); //$NON-NLS-1$
			stdoutListeners = (ITerminalServiceOutputStreamMonitorListener[])((SettingsStore)store).getSettings().get("StdOutListeners"); //$NON-NLS-1$
			stderrListeners = (ITerminalServiceOutputStreamMonitorListener[])((SettingsStore)store).getSettings().get("StdErrListeners"); //$NON-NLS-1$
			environment = (String[])((SettingsStore)store).getSettings().get("Environment"); //$NON-NLS-1$
		}
	}

	/**
	 * Saves the process settings to the given settings store.
	 *
	 * @param store The settings store. Must not be <code>null</code>.
	 */
	public void save(ISettingsStore store) {
		Assert.isNotNull(store);
		store.put("Path", image);//$NON-NLS-1$
		store.put("Arguments", arguments); //$NON-NLS-1$
		store.put("LocalEcho", Boolean.toString(localEcho)); //$NON-NLS-1$
		store.put("MergeWithNativeEnvironment", Boolean.toString(mergeWithNativeEnvironment)); //$NON-NLS-1$
		store.put("LineSeparator", lineSeparator); //$NON-NLS-1$
		store.put("WorkingDir", workingDir); //$NON-NLS-1$
		if (store instanceof SettingsStore) {
			((SettingsStore)store).getSettings().put("Process", process); //$NON-NLS-1$
			((SettingsStore)store).getSettings().put("PTY", pty); //$NON-NLS-1$
			((SettingsStore)store).getSettings().put("StdOutListeners", stdoutListeners); //$NON-NLS-1$
			((SettingsStore)store).getSettings().put("StdErrListeners", stderrListeners); //$NON-NLS-1$
			((SettingsStore)store).getSettings().put("Environment", environment); //$NON-NLS-1$
		}
	}
}
