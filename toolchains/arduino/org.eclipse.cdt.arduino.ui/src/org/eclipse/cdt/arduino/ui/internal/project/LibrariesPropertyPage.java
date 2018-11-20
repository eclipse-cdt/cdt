/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.project;

import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.cdt.arduino.ui.internal.LibraryTree;
import org.eclipse.cdt.arduino.ui.internal.Messages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public class LibrariesPropertyPage extends PropertyPage {

	private static ArduinoManager manager = Activator.getService(ArduinoManager.class);

	private LibraryTree libraryTree;

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setLayout(new GridLayout());

		Text desc = new Text(comp, SWT.READ_ONLY | SWT.WRAP);
		GridData layoutData = new GridData(SWT.LEFT, SWT.FILL, true, false);
		layoutData.widthHint = 500;
		desc.setLayoutData(layoutData);
		desc.setBackground(parent.getBackground());
		desc.setText(Messages.LibrariesPropertyPage_desc);

		libraryTree = new LibraryTree(comp);
		libraryTree.setLayoutData(new GridData(GridData.FILL_BOTH));

		try {
			IProject project = getElement().getAdapter(IProject.class);
			libraryTree.setIncludePlatforms(true);
			libraryTree.setChecked(manager.getLibraries(project));
			libraryTree.getViewer().setInput(manager.getInstalledLibraries());
		} catch (CoreException e) {
			Activator.log(e);
		}

		return comp;
	}

	private IProject getProject() {
		return getElement().getAdapter(IProject.class);
	}

	@Override
	public boolean performOk() {
		try {
			manager.setLibraries(getProject(), libraryTree.getChecked());
		} catch (CoreException e) {
			Activator.log(e);
		}
		return true;
	}

}
