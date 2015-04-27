/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
