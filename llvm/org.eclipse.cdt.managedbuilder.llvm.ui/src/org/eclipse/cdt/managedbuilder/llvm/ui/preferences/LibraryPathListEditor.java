/*******************************************************************************
 * Copyright (c) 2010-2013 Nokia Siemens Networks Oyj, Finland.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Nokia Siemens Networks - initial implementation
 *      Petri Tuononen - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.ui.preferences;

import org.eclipse.cdt.managedbuilder.llvm.ui.LlvmEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.llvm.util.LlvmToolOptionPathUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * New implementation of LlvmListEditor.
 * Used to select a library path from the dialog.
 *
 */
public class LibraryPathListEditor extends LlvmListEditor {

	/**
	 * Constructor.
	 *
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param parent the parent of the field editor's control
	 */
	LibraryPathListEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}

	@Override
	/**
	 * Functionality for New button.
	 * Shows a browser dialog to select a directory and returns that directory path.
	 */
	protected String getNewInputObject() {
		DirectoryDialog dlg = new DirectoryDialog(getShell());
		final Text text = new Text(getShell(), SWT.BORDER);
		dlg.setFilterPath(text.getText());
		dlg.setText(Messages.LibraryPathListEditor_0);
		dlg.setMessage(Messages.LibraryPathListEditor_1);
		String dir = dlg.open();
		if (dir == null) {
			return null;
		}
		//remove white spaces
		dir = dir.trim();
		if (dir.length() != 0) {
			//get all existing items in the list
			String[] existingItems = getList().getItems();
			//check that the list doesn't already contain the added item
			if (existingItems.length > 0) {
				//return null if duplicate item found
				for (String item : existingItems) {
					if (item.equalsIgnoreCase(dir)) {
						return null;
					}
				}
			}
			//add a new library search path to LLVM preference store
			LlvmPreferenceStore.appendLibraryPath(dir);
			//add a new library path to LLVM linker's option
			LlvmToolOptionPathUtil.addLlvmLibraryPath(dir);
			//inform LLVM environment variable supplier that there has been a change
			LlvmEnvironmentVariableSupplier.notifyPreferenceChange();
			return dir;
		}
		return null;
	}

	@Override
	/**
	 * Removes the path from the list as well as from the Tool's Option.
	 */
	protected void removePressed() {
		List libPathList = getList();
		setPresentsDefaultValue(false);
		String[] selected = libPathList.getSelection();
		for (String s : selected) {
			//remove a library path from the LLVM preference store
			LlvmPreferenceStore.removeLibraryPath(s);
			//remove a library path from LLVM linker's option
			LlvmToolOptionPathUtil.removeLlvmLibraryPath(s);
			//inform LLVM environment variable supplier that there has been a change
			LlvmEnvironmentVariableSupplier.notifyPreferenceChange();
			libPathList.remove(s);
			selectionChanged();
		}
	}

}