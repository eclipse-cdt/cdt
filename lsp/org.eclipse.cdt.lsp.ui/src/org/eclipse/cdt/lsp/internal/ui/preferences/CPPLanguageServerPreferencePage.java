/*******************************************************************************
 * Copyright (c) 2018-2019 Manish Khurana, Nathan Ridge and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Manish Khurana <mkmanishkhurana98@gmail.com> - initial API and implementation
 *     Nathan Ridge <zeratul976@hotmail.com> - initial API and implementation
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 558516
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.ui.preferences;

import java.io.File;

import org.eclipse.cdt.lsp.core.Activator;
import org.eclipse.cdt.lsp.core.CPPStreamConnectionProvider;
import org.eclipse.cdt.lsp.core.PreferenceConstants;
import org.eclipse.cdt.lsp.internal.ui.LspUiMessages;
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
		setDescription(LspUiMessages.CPPLanguageServerPreferencePage_description);
	}

	@Override
	public void createFieldEditors() {

		serverChoice = new RadioGroupFieldEditor(PreferenceConstants.P_SERVER_CHOICE,
				LspUiMessages.CPPLanguageServerPreferencePage_server_selector, 1,
				new String[][] {
						{ LspUiMessages.CPPLanguageServerPreferencePage_clangd, CPPStreamConnectionProvider.CLANGD_ID },
						{ LspUiMessages.CPPLanguageServerPreferencePage_cquery,
								CPPStreamConnectionProvider.CQUERY_ID } },
				getFieldEditorParent());
		addField(serverChoice);

		serverPath = new FileFieldEditor(PreferenceConstants.P_SERVER_PATH,
				LspUiMessages.CPPLanguageServerPreferencePage_server_path, getFieldEditorParent());
		addField(serverPath);

		serverOptions = new StringFieldEditor(PreferenceConstants.P_SERVER_OPTIONS,
				LspUiMessages.CPPLanguageServerPreferencePage_server_options, getFieldEditorParent());
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