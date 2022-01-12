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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * New implementation of LlvmListEditor.
 * Used to select a library file from the dialog.
 *
 */
public class LibraryListEditor extends LlvmListEditor {

	/**
	 * Constructor.
	 *
	 * @param name
	 * @param labelText
	 * @param parent
	 */
	LibraryListEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}

	@Override
	/**
	 * Functionality for New button.
	 * Shows a browser dialog to select a file and returns that file.
	 */
	protected String getNewInputObject() {
		FileDialog dlg = new FileDialog(getShell());
		final Text text = new Text(getShell(), SWT.BORDER);
		dlg.setFilterPath(text.getText());
		dlg.setText(Messages.LibraryListEditor_0);
		dlg.open();
		String file = dlg.getFileName();
		if (file == null) {
			return null;
		}
		//remove white spaces
		file = file.trim();
		if (file.length() != 0) {
			//get all existing items in the list
			String[] existingItems = getList().getItems();
			//return null if duplicate item found
			for (String item : existingItems) {
				if (item.equalsIgnoreCase(file)) {
					return null;
				}
			}
			//add a new library to LLVM preference store
			LlvmPreferenceStore.appendLibrary(file);
			//add a new library to LLVM linker's option
			LlvmToolOptionPathUtil.addLlvmLib(file);
			//inform LLVM environment variable supplier that there has been a change
			LlvmEnvironmentVariableSupplier.notifyPreferenceChange();
			return file;
		}
		return null;
	}

	@Override
	/**
	 * Removes the path from the list as well as from the Tool's Option.
	 */
	protected void removePressed() {
		List libList = getList();
		setPresentsDefaultValue(false);
		String[] selected = libList.getSelection();
		for (String s : selected) {
			//remove a library from the LLVM preference store
			LlvmPreferenceStore.removeLibrary(s);
			//remove a library from LLVM linker's option
			LlvmToolOptionPathUtil.removeLlvmLib(s);
			//inform LLVM environment variable supplier that there has been a change
			LlvmEnvironmentVariableSupplier.notifyPreferenceChange();
			libList.remove(s);
			selectionChanged();
		}
	}

}