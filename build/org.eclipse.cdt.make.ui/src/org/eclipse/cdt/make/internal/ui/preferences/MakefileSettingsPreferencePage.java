/*******************************************************************************
 * Copyright (c) 2002, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.preferences;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.IMakeHelpContextIds;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

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
		IPreferenceStore store = MakeUIPlugin.getDefault().getCorePreferenceStore();
		setPreferenceStore(store);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				IMakeHelpContextIds.MAKE_SETTINGS_PREFERENCE_PAGE);
	}

	/**
	 * @see FieldEditorPreferencePage#createControl(Composite)
	 */
	@Override
	protected void createFieldEditors() {
		String[][] personalities = { { POSIX_MAKE_LABEL, POSIX_MAKE_VALUE }, { GNU_MAKE_LABEL, GNU_MAKE_VALUE } };
		RadioGroupFieldEditor combo = new RadioGroupFieldEditor(MakeCorePlugin.MAKEFILE_STYLE,
				MakefilePreferencesMessages.getString("MakefileSettingsPreferencePage.style"), //$NON-NLS-1$
				2, personalities, getFieldEditorParent());
		addField(combo);

		PathEditor pathEditor = new PathEditor(MakeCorePlugin.MAKEFILE_DIRS,
				MakefilePreferencesMessages.getString("MakefileSettingsPreferencePage.path.label"), //$NON-NLS-1$
				MakefilePreferencesMessages.getString("MakefileSettingsPreferencePage.path.browse"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(pathEditor);
	}

	/**
	 * Initializes the default values of this page in the preference bundle.
	 * @param prefs  preference store
	 */
	public static void initDefaults(IPreferenceStore prefs) {
	}

	@Override
	public void init(IWorkbench workbench) {
	}
}
