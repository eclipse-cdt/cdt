/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.lsp.core;

import java.io.File;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents the preference page for C/C++ Language Server.
 */

public class CPPLanguageServerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private FileFieldEditor serverPath;
	private RadioGroupFieldEditor serverChoice;
	private StringFieldEditor serverOptions;

	public CPPLanguageServerPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.PreferencePageDescription);
	}

	@Override
	public void createFieldEditors() {

		serverChoice = new RadioGroupFieldEditor(PreferenceConstants.P_SERVER_CHOICE, Messages.ServerChoiceLabel, 1,
				new String[][] { { "ClangD", CPPStreamConnectionProvider.CLANGD_ID }, //$NON-NLS-1$
						{ "CQuery", CPPStreamConnectionProvider.CQUERY_ID } }, //$NON-NLS-1$
				getFieldEditorParent());
		addField(serverChoice);

		serverPath = new FileFieldEditor(PreferenceConstants.P_SERVER_PATH, Messages.ServerPathLabel,
				getFieldEditorParent());
		addField(serverPath);

		serverOptions = new StringFieldEditor(PreferenceConstants.P_SERVER_OPTIONS, Messages.ServerOptionsLabel,
				getFieldEditorParent());
		addField(serverOptions);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getSource() == serverChoice && event.getProperty() == FieldEditor.VALUE) {
			File changedLSLocation = CPPStreamConnectionProvider.getDefaultLSLocation((String) event.getNewValue());
			if (changedLSLocation != null) {
				serverPath.setStringValue(changedLSLocation.getAbsolutePath());
			}
		}
		super.propertyChange(event);
	}

	@Override
	public void init(IWorkbench workbench) {
	}
}