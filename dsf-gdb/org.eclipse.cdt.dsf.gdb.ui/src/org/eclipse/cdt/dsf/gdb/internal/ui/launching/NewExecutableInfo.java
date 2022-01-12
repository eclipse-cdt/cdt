/*******************************************************************************
 * Copyright (c) 2013 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This class provides information required to start
 * debugging an executable.
 */
public class NewExecutableInfo {

	public static final String ATTR_SESSION_TYPE = "sessionType"; //$NON-NLS-1$

	final private SessionType fSessionType;
	private String fHostPath = ""; //$NON-NLS-1$

	private Map<String, Object> fAttributes = new HashMap<>();

	public NewExecutableInfo(SessionType sessionType) {
		super();
		fSessionType = sessionType;
		setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_BINARY, ""); //$NON-NLS-1$
		setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
		IPreferenceStore preferences = GdbUIPlugin.getDefault().getPreferenceStore();
		setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,
				preferences.getBoolean(IGdbDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN));
		setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL,
				preferences.getString(IGdbDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN_SYMBOL));
	}

	/**
	 * Returns the path of the executable on the host
	 */
	public String getHostPath() {
		return fHostPath;
	}

	/**
	 * Sets the host path
	 */
	public void setHostPath(String hostPath) {
		fHostPath = hostPath;
	}

	/**
	 * For remote sessions returns the path of the executable
	 * on the target. Otherwise returns null.
	 */
	public String getTargetPath() {
		return (String) fAttributes.get(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_BINARY);
	}

	/**
	 * Returns the arguments to pass to the executable, or null
	 */
	public String getArguments() {
		return (String) fAttributes.get(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS);
	}

	/**
	 * Returns the attribute map
	 */
	public Map<String, Object> getAttributes() {
		return fAttributes;
	}

	/**
	 * Returns the session type
	 */
	public SessionType getSessionType() {
		return fSessionType;
	}

	public Object getAttribute(String name) {
		return fAttributes.get(name);
	}

	public void setAttribute(String name, Object value) {
		fAttributes.put(name, value);
	}
}