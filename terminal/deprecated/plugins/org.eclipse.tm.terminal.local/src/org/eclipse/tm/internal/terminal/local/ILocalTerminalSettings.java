/***************************************************************************************************
 * Copyright (c) 2008 Mirko Raner and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - [196337] Adapted from org.eclipse.tm.terminal.ssh/ISshSettings
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

/**
 * The interface {@link ILocalTerminalSettings} defines the public interface for connector-specific
 * settings needed by the {@link LocalTerminalConnector}. The interface is implemented by class
 * {@link LocalTerminalSettings}.
 *
 * @author Mirko Raner
 * @version $Revision: 1.3 $
 */
public interface ILocalTerminalSettings {

	/**
	 * The line separator setting CR (carriage return only; for example, used by Mac OS 9).
	 */
	public final static String LINE_SEPARATOR_CR = "\\r"; //$NON-NLS-1$

	/**
	 * The line separator setting CRLF (carriage return and line feed; for example, used by
	 * Windows).
	 */
	public final static String LINE_SEPARATOR_CRLF = "\\r\\n"; //$NON-NLS-1$

	/**
	 * The line separator setting LF (line feed only; used by all UNIX-based systems).
	 */
	public final static String LINE_SEPARATOR_LF = "\\n"; //$NON-NLS-1$

	/**
	 * Loads the settings from a specified {@link ISettingsStore}.
	 *
	 * TODO: the {@link #load(ISettingsStore)} method should probably extracted to a super-interface
	 *       as it appears to be common to all customized settings interfaces
	 *
	 * @param store the {@link ISettingsStore} to load the settings from
	 */
	public abstract void load(ISettingsStore store);

	/**
	 * Saves the settings to a specified {@link ISettingsStore}.
	 *
	 * TODO: the {@link #save(ISettingsStore)} method should probably extracted to a super-interface
	 *       as it appears to be common to all customized settings interfaces
	 *
	 * @param store the {@link ISettingsStore} for storing the settings
	 */
	public abstract void save(ISettingsStore store);

	/**
	 * Gets the name of the launch configuration that will be started in the terminal.
	 *
	 * @return the launch configuration name
	 */
	public abstract String getLaunchConfigurationName();

	/**
	 * Sets the name of the launch configuration that will be started in the terminal.
	 *
	 * @param configurationName the launch configuration name
	 */
	public abstract void setLaunchConfigurationName(String configurationName);
}
