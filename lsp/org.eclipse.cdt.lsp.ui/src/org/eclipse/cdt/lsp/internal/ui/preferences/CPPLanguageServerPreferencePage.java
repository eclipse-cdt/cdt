/*******************************************************************************
 * Copyright (c) 2018, 2020 Manish Khurana, Nathan Ridge and others.
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
 *     Philip Langer <planger@eclipsesource.com> - Bug 563280
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.ui.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.cdt.lsp.LanguageServerConfiguration;
import org.eclipse.cdt.lsp.SupportedLanguageServers;
import org.eclipse.cdt.lsp.core.CPPStreamConnectionProvider;
import org.eclipse.cdt.lsp.core.PreferenceConstants;
import org.eclipse.cdt.lsp.internal.core.preferences.LanguageServerDefaults;
import org.eclipse.cdt.lsp.internal.ui.LspUiActivator;
import org.eclipse.cdt.lsp.internal.ui.LspUiMessages;
import org.eclipse.cdt.ui.newui.MultiLineTextFieldEditor;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents the preference page for C/C++ Language Server.
 */

public class CPPLanguageServerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private FileFieldEditor serverPath;
	private RadioGroupFieldEditor serverChoice;
	private MultiLineTextFieldEditor serverOptions;

	public CPPLanguageServerPreferencePage() {
		super(GRID);
		setPreferenceStore(LspUiActivator.getDefault().getLspCorePreferences());
		setDescription(LspUiMessages.CPPLanguageServerPreferencePage_description);
	}

	@Override
	public void createFieldEditors() {
		PreferenceMetadata<Boolean> prefer = new LanguageServerDefaults().preferLanguageServer();
		addField(new BooleanFieldEditor(prefer.identifer(), prefer.name(), getFieldEditorParent()));
		serverChoice = new RadioGroupFieldEditor(PreferenceConstants.P_SERVER_CHOICE,
				LspUiMessages.CPPLanguageServerPreferencePage_server_selector, 1, contributedServers(),
				getFieldEditorParent());
		addField(serverChoice);

		serverPath = new FileFieldEditor(PreferenceConstants.P_SERVER_PATH,
				LspUiMessages.CPPLanguageServerPreferencePage_server_path, getFieldEditorParent());
		addField(serverPath);

		serverOptions = new MultiLineTextFieldEditor(PreferenceConstants.P_SERVER_OPTIONS,
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

	private String[][] contributedServers() {
		List<LanguageServerConfiguration> servers = new ArrayList<>();
		ServiceCaller.callOnce(getClass(), SupportedLanguageServers.class, x -> servers.addAll(x.all()));
		return servers.stream()//
				.map(x -> new String[] { x.label(), x.identifier() })//
				.collect(Collectors.toList()).toArray(new String[0][]);
	}
}