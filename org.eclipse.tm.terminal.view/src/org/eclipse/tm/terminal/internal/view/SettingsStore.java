/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *     
 *******************************************************************************/
package org.eclipse.tm.terminal.internal.view;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.tm.terminal.ISettingsStore;

/**
 *  
 * A {@link IDialogSettings} based {@link ISettingsStore}.
 * 
 * @author Michael Scharf
 */
class SettingsStore implements ISettingsStore {
	final private IDialogSettings fDialogSettings;
	final private String fPrefix;
	public SettingsStore(String terminalPartName) {
		fDialogSettings=TerminalViewPlugin.getDefault().getDialogSettings();
		fPrefix=getClass().getName() + "." + terminalPartName + "."; //$NON-NLS-1$ //$NON-NLS-2$;
	}

	public String get(String key) {
		return get(key,null);
	}
	public String get(String key, String defaultValue) {
		String value = fDialogSettings.get(fPrefix + key);

		if ((value == null) || (value.equals(""))) //$NON-NLS-1$
			return defaultValue;

		return value;
	}

	public void put(String key, String strValue) {
		fDialogSettings.put(fPrefix + key , strValue);
	}
}
