/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.internal.remote.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.internal.remote.core.RemoteServicesDescriptor;
import org.eclipse.internal.remote.core.RemoteServicesImpl;
import org.eclipse.internal.remote.ui.messages.Messages;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.remote.core.IRemotePreferenceConstants;
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

		for (RemoteServicesDescriptor service : RemoteServicesImpl.getRemoteServiceDescriptors()) {
			nameAndValue = new String[2];
			nameAndValue[0] = service.getName();
			nameAndValue[1] = service.getId();
			namesAndValues.add(nameAndValue);
		}
		addField(new ComboFieldEditor(IRemotePreferenceConstants.PREF_REMOTE_SERVICES_ID,
				Messages.RemoteDevelopmentPreferencePage_defaultRemoteServicesProvider,
				namesAndValues.toArray(new String[namesAndValues.size()][2]), getFieldEditorParent()));
	}
}
