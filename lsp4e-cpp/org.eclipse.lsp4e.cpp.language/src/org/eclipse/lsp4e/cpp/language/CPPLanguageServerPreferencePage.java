/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

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

		serverChoice = new RadioGroupFieldEditor(PreferenceConstants.P_SERVER_CHOICE,
				Messages.ServerChoiceLabel, 1,
				new String[][] { { "ClangD", "clangd" }, { "CQuery", "cquery" } }, getFieldEditorParent()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		addField(serverChoice);

		serverPath = new FileFieldEditor(PreferenceConstants.P_SERVER_PATH, Messages.ServerPathLabel,
				getFieldEditorParent());
		addField(serverPath);

		serverOptions = new StringFieldEditor(PreferenceConstants.P_SERVER_OPTIONS, Messages.ServerOptionsLabel,
				getFieldEditorParent());
		addField(serverOptions);
	}

	@Override
	public void init(IWorkbench workbench) {
	}
}