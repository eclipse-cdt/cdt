/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.remote.internal.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemotePreferenceConstants;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.internal.ui.RemoteUIPlugin;
import org.eclipse.remote.internal.ui.messages.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @since 4.1
 *
 */
public class RemoteDevelopmentPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public RemoteDevelopmentPreferencePage() {
		super(GRID);
		setPreferenceStore(new PreferencesAdapter());
	}

	@Override
	public void init(IWorkbench workbench) {
		// Do nothing
	}

	@Override
	protected void createFieldEditors() {
		List<String[]> namesAndValues = new ArrayList<String[]>();
		String[] nameAndValue = new String[2];
		nameAndValue[0] = "None"; //$NON-NLS-1$
		nameAndValue[1] = ""; //$NON-NLS-1$
		namesAndValues.add(nameAndValue);

		IRemoteServicesManager manager = RemoteUIPlugin.getService(IRemoteServicesManager.class);
		for (IRemoteConnectionType service : manager.getRemoteConnectionTypes()) {
			nameAndValue = new String[2];
			nameAndValue[0] = service.getName();
			nameAndValue[1] = service.getId();
			namesAndValues.add(nameAndValue);
		}
		addField(new ComboFieldEditor(IRemotePreferenceConstants.PREF_CONNECTION_TYPE_ID,
				Messages.RemoteDevelopmentPreferencePage_Default_connection_type,
				namesAndValues.toArray(new String[namesAndValues.size()][2]), getFieldEditorParent()));
	}
}
