/*******************************************************************************
 * Copyright (c) 2015, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.remote.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.tm.terminal.connector.remote.IRemoteTerminalConstants;
import org.eclipse.tm.terminal.connector.remote.internal.Activator;

public class RemoteTerminalPreferenceInitializer extends AbstractPreferenceInitializer {

	public RemoteTerminalPreferenceInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences defaultPrefs = DefaultScope.INSTANCE.getNode(Activator.getUniqueIdentifier());
		defaultPrefs.put(IRemoteTerminalConstants.PREF_TERMINAL_SHELL_COMMAND, ""); //$NON-NLS-1$
	}
}
