/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.downloads;

import java.util.Collection;

import org.eclipse.cdt.arduino.core.internal.board.ArduinoLibrary;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class SelectLibrariesDialog extends Dialog {

	private Collection<ArduinoLibrary> libraries;
	private Collection<ArduinoLibrary> checkedLibraries;
	private LibraryTree libraryTree;

	public SelectLibrariesDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Point getInitialSize() {
		Point size = super.getInitialSize();
		if (size.x < 600 || size.y < 400) {
			return new Point(600, 400);
		} else {
			return size;
		}
	}

	public void setLibraries(Collection<ArduinoLibrary> libraries) {
		this.libraries = libraries;
	}

	public Collection<ArduinoLibrary> getChecked() {
		return checkedLibraries;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout());

		libraryTree = new LibraryTree(comp);
		libraryTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		libraryTree.setIncludePackages(false);
		libraryTree.getViewer().setInput(libraries);

		applyDialogFont(comp);
		return comp;
	}

	@Override
	protected void okPressed() {
		checkedLibraries = libraryTree.getChecked();
		super.okPressed();
	}

}
