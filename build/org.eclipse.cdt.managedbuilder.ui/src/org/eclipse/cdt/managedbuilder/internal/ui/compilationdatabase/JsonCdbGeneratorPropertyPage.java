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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Property page for JSON Compilation Database Generator.
 */

public class JsonCdbGeneratorPropertyPage extends PropertyPage implements ICOptionContainer {
	private CompilationDatabaseGeneratorBlock optionPage;

	public JsonCdbGeneratorPropertyPage() {
		super();
		optionPage = new CompilationDatabaseGeneratorBlock();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		optionPage.createControl(composite);
		return composite;
	}

	@Override
	protected void performDefaults() {
		optionPage.performDefaults();
	}

	@Override
	public boolean performOk() {
		return true;
	}

	@Override
	public IProject getProject() {
		IProject project = null;
		IAdaptable elem = getElement();
		if (elem instanceof IProject) {
			project = (IProject) elem;
		} else if (elem != null) {
			project = elem.getAdapter(IProject.class);
		}
		return project;
	}

	@Override
	public void updateContainer() {
	}

	@Override
	public Preferences getPreferences() {
		return null;
	}

}
