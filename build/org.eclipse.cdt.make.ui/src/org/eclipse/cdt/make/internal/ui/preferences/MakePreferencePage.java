/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Axel Mueller - Rebuild last target
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.preferences;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.IMakeHelpContextIds;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class MakePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final String PREF_BUILD_TARGET_IN_BACKGROUND = "MakeTargetPrefs.buildTargetInBackground"; //$NON-NLS-1$
	private static final String TARGET_BUILDS_IN_BACKGROUND = "MakeTargetPreferencePage.buildTargetInBackground.label"; //$NON-NLS-1$

	private static final String PREF_BUILD_LAST_TARGET = "MakeTargetPrefs.buildLastTarget"; //$NON-NLS-1$
	private static final String BUILD_LAST_TARGET = "MakeTargetPreferencePage.buildLastTarget.title"; //$NON-NLS-1$
	private static final String PREF_BUILD_LAST_PROJECT = "MakeTargetPrefs.buildLastTarget.project"; //$NON-NLS-1$
	private static final String BUILD_LAST_PROJECT = "MakeTargetPreferencePage.buildLastTarget.project"; //$NON-NLS-1$
	private static final String PREF_BUILD_LAST_RESOURCE = "MakeTargetPrefs.buildLastTarget.resource"; //$NON-NLS-1$
	private static final String BUILD_LAST_RESOURCE = "MakeTargetPreferencePage.buildLastTarget.resource"; //$NON-NLS-1$
	private static final String PREF_BUILD_LAST_PROJECTROOT = "MakeTargetPrefs.buildLastTarget.projectRoot"; //$NON-NLS-1$
	private static final String BUILD_LAST_PROJECTROOT = "MakeTargetPreferencePage.buildLastTarget.projectRoot"; //$NON-NLS-1$

	public MakePreferencePage() {
		super(GRID);
		setPreferenceStore(MakeUIPlugin.getDefault().getPreferenceStore());
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IMakeHelpContextIds.MAKE_TARGETS_PREFERENCE_PAGE);
	}

	/**
	 * @see FieldEditorPreferencePage#createControl(Composite)
	 */
	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		BooleanFieldEditor tagetBackgroundEditor = new BooleanFieldEditor(PREF_BUILD_TARGET_IN_BACKGROUND,
				MakeUIPlugin.getResourceString(TARGET_BUILDS_IN_BACKGROUND), parent);
		addField(tagetBackgroundEditor);

		// make last target for selected resource or project
		RadioGroupFieldEditor edit = new RadioGroupFieldEditor(
				PREF_BUILD_LAST_TARGET,
				MakeUIPlugin.getResourceString(BUILD_LAST_TARGET), 1,
				new String[][] {
						{MakeUIPlugin.getResourceString(BUILD_LAST_PROJECT), PREF_BUILD_LAST_PROJECT},
						{MakeUIPlugin.getResourceString(BUILD_LAST_RESOURCE), PREF_BUILD_LAST_RESOURCE},
						{MakeUIPlugin.getResourceString(BUILD_LAST_PROJECTROOT), PREF_BUILD_LAST_PROJECTROOT} },
				parent,
				true);
		addField(edit);
	}

	public static boolean isBuildTargetInBackground() {
		return MakeUIPlugin.getDefault().getPreferenceStore().getBoolean(PREF_BUILD_TARGET_IN_BACKGROUND);
	}

	public static void setBuildTargetInBackground(boolean enable) {
		MakeUIPlugin.getDefault().getPreferenceStore().setValue(PREF_BUILD_TARGET_IN_BACKGROUND, enable);
	}

	/**
	 * preference to rebuild last target
	 *
	 * @return {@code true} if from root folder of selected project
	 */
	public static boolean useProjectRootForLastMakeTarget() {
		return MakeUIPlugin.getDefault().getPreferenceStore().getString(PREF_BUILD_LAST_TARGET).equals(PREF_BUILD_LAST_PROJECTROOT);
	}

	/**
	 * preference to rebuild last target
	 *
	 * @return {@code true} if from selected project irrespective in which container
	 */
	public static boolean useProjectLastMakeTarget() {
		return MakeUIPlugin.getDefault().getPreferenceStore().getString(PREF_BUILD_LAST_TARGET).equals(PREF_BUILD_LAST_PROJECT);
	}
	/**
	 * Initializes the default values of this page in the preference bundle.
	 */
	public static void initDefaults(IPreferenceStore prefs) {
		prefs.setDefault(PREF_BUILD_TARGET_IN_BACKGROUND, true);
		prefs.setDefault(PREF_BUILD_LAST_TARGET, PREF_BUILD_LAST_PROJECT);
	}

	@Override
	public void init(IWorkbench workbench) {
	}
}
