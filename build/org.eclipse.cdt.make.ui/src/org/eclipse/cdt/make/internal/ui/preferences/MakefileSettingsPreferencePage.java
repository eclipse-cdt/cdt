/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.preferences;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.text.PreferencesAdapter;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * MakePreferencePage
 */
public class MakefileSettingsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final String POSIX_MAKE_LABEL = "Posix Make"; //$NON-NLS-1$
	private static final String POSIX_MAKE_VALUE = "POSIX"; //$NON-NLS-1$
	private static final String GNU_MAKE_LABEL = "GNU Make"; //$NON-NLS-1$
	private static final String GNU_MAKE_VALUE = "GNU"; //$NON-NLS-1$

	public MakefileSettingsPreferencePage() {
		super(GRID);
		IPreferenceStore store = new PreferencesAdapter(MakeCorePlugin.getDefault().getPluginPreferences());
		setPreferenceStore(store);
	}

	/**
	 * @see FieldEditorPreferencePage#createControl(Composite)
	 */
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		String[][] personalities = {{POSIX_MAKE_LABEL, POSIX_MAKE_VALUE}, {GNU_MAKE_LABEL, GNU_MAKE_VALUE}};
		RadioGroupFieldEditor combo = new RadioGroupFieldEditor(MakeCorePlugin.MAKEFILE_STYLE,
				MakefilePreferencesMessages.getString("MakefileSettingsPreferencePage.style"),//$NON-NLS-1$
				2,
				personalities,
				getFieldEditorParent());
		addField(combo);

		PathEditor pathEditor = new PathEditor(MakeCorePlugin.MAKEFILE_DIRS,
				MakefilePreferencesMessages.getString("MakefileSettingsPreferencePage.path.label"), //$NON-NLS-1$
				MakefilePreferencesMessages.getString("MakefileSettingsPreferencePage.path.browse"),//$NON-NLS-1$
				getFieldEditorParent());
		addField(pathEditor);
	}

	/**
	 * Initializes the default values of this page in the preference bundle.
	 */
	public static void initDefaults(IPreferenceStore prefs) {
	}

	public void init(IWorkbench workbench) {
	}
}
