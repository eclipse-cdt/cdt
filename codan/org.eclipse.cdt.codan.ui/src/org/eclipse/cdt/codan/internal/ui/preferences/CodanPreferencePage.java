/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.preferences;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.model.CheckersRegisry;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */
public class CodanPreferencePage extends FieldEditorOverlayPage implements
		IWorkbenchPreferencePage {
	private IProblemProfile profile;

	public CodanPreferencePage() {
		super(GRID);
		setPreferenceStore(new ScopedPreferenceStore(new InstanceScope(),
				CodanCorePlugin.PLUGIN_ID));
		setDescription("Code Analyzers Preference Page");
	}

	protected String getPageId() {
		return "org.eclipse.cdt.codan.internal.ui.preferences.CodanPreferencePage";
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		profile = isPropertyPage() ? CheckersRegisry.getInstance()
				.getResourceProfileWorkingCopy((IResource) getElement())
				: CheckersRegisry.getInstance().getWorkspaceProfile();
		CheckedTreeEditor checkedTreeEditor = new ProblemsTreeEditor(
				getFieldEditorParent(), profile);
		addField(checkedTreeEditor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	public boolean performOk() {
		if (isPropertyPage())
			CheckersRegisry.getInstance().updateProfile(
					(IResource) getElement(), null);
		return super.performOk();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
}