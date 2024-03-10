/********************************************************************************
 * Copyright (c) 2023, 2024 Renesas Electronics Corp. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.compilationdatabase;

import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for JSON Compilation Database Generator.
 */
public class JsonCdbGeneratorPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage, ICOptionContainer {

	private final CompilationDatabaseGeneratorBlock fOptionBlock;

	public JsonCdbGeneratorPreferencePage() {
		fOptionBlock = new CompilationDatabaseGeneratorBlock();
	}

	@Override
	protected Control createContents(Composite parent) {
		GridLayout gl;
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(gl = new GridLayout());
		composite.setLayoutData(new GridData());
		gl.verticalSpacing = 0;
		fOptionBlock.createControl(composite);
		return composite;
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	public void updateContainer() {
		if (!fOptionBlock.isValid()) {
			setErrorMessage(fOptionBlock.getErrorMessage());
			setValid(false);
		} else {
			setErrorMessage(null);
			setValid(true);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public org.eclipse.core.runtime.Preferences getPreferences() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean performOk() {
		return true;
	}

	@Override
	public void performDefaults() {
		fOptionBlock.performDefaults();
	}

	@Override
	public IProject getProject() {
		return null;
	}

}