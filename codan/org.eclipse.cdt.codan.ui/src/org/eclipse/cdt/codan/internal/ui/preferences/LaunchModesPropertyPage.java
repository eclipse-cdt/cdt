/*******************************************************************************
 * Copyright (c) 2010 Alena Laskavaia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceStore;

public class LaunchModesPropertyPage extends FieldEditorPreferencePage {
	private ArrayList<FieldEditor> editors;

	/**
	 * @param prefStore
	 * 
	 */
	public LaunchModesPropertyPage(PreferenceStore prefStore) {
		super(GRID);
		setPreferenceStore(prefStore);
		editors = new ArrayList<FieldEditor>();
	}

	@Override
	public void noDefaultAndApplyButton() {
		super.noDefaultAndApplyButton();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors
	 * ()
	 */
	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(CheckerLaunchMode.RUN_ON_FULL_BUILD.name(), "Run on full build", getFieldEditorParent()));
		addField(new BooleanFieldEditor(CheckerLaunchMode.RUN_ON_INC_BUILD.name(), "Run on incremental build", getFieldEditorParent()));
		addField(new BooleanFieldEditor(CheckerLaunchMode.RUN_ON_DEMAND.name(), "Run on demand", getFieldEditorParent()));
		addField(new BooleanFieldEditor(CheckerLaunchMode.RUN_AS_YOU_TYPE.name(), "Run as you type", getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#addField(org.eclipse
	 * .jface.preference.FieldEditor)
	 */
	@Override
	protected void addField(FieldEditor editor) {
		editors.add(editor);
		super.addField(editor);
	}

	/**
	 * 
	 */
	protected void configureProjectSettings() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean performOk() {
		boolean result = super.performOk();
		return result;
	}
}
