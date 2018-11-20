/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.downloads;

import java.util.Collection;

import org.eclipse.cdt.arduino.core.internal.board.ArduinoLibrary;
import org.eclipse.cdt.arduino.ui.internal.FormTextHoverManager;
import org.eclipse.cdt.arduino.ui.internal.LibraryTree;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

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
		libraryTree.setIncludePlatforms(false);
		libraryTree.getViewer().setInput(libraries);

		FormTextHoverManager hoverManager = new FormTextHoverManager() {
			@Override
			protected void computeInformation() {
				TreeViewer viewer = libraryTree.getViewer();
				Tree tree = viewer.getTree();
				TreeItem item = tree.getItem(getHoverEventLocation());
				if (item != null) {
					Object data = item.getData();
					if (data instanceof ArduinoLibrary) {
						ArduinoLibrary library = (ArduinoLibrary) data;
						setInformation(library.toFormText(), item.getBounds());
					}
				} else {
					setInformation(null, null);
				}
			}
		};
		hoverManager.install(libraryTree.getViewer().getTree());

		applyDialogFont(comp);
		return comp;
	}

	@Override
	protected void okPressed() {
		checkedLibraries = libraryTree.getChecked();
		super.okPressed();
	}

}
