/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui.search;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.rse.ui.dialogs.SystemSelectFileTypesDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;


/**
 * File types selection dialog for the search page.
 */
public class SystemSearchSelectFileTypesDialog extends SystemSelectFileTypesDialog {

	/**
	 * Creates the dialog.
	 * @param shell the shell.
	 * @param currentTypes types to preselect.
	 */
	public SystemSearchSelectFileTypesDialog(Shell shell, Collection currentTypes) {
		super(shell, currentTypes);
	}
	
	/**
	 * @see org.eclipse.rse.ui.dialogs.SystemSelectFileTypesDialog#checkInitialSelections()
	 */
	protected void checkInitialSelections() {
		
		IFileEditorMapping editorMappings[] = PlatformUI.getWorkbench().getEditorRegistry().getFileEditorMappings();
		ArrayList selectedMappings = new ArrayList();

		// go through all editor mappings, and check of those that are in the initial selections
		for (int i = 0; i < editorMappings.length; i++) {
			IFileEditorMapping mapping = editorMappings[i];
			
			if (initialSelections.contains(mapping.getExtension())) {
				listViewer.setChecked(mapping, true);
				selectedMappings.add(mapping.getExtension());
			}
		}

		// now find those entries in the initial selections that are not in editor mappings
		// add these entries to the user defined list
		Iterator initialIterator = initialSelections.iterator();
		StringBuffer entries = new StringBuffer();
		boolean first = true;
		
		while(initialIterator.hasNext()) {
			String nextExtension = (String)initialIterator.next();
			
			if(!selectedMappings.contains(nextExtension)) {
				
				if (!first) {
					// if not the first entry, add a comma and a space
					entries.append(TYPE_DELIMITER);
					entries.append(" ");
				}
				else {
					first = false;
				}
				
				entries.append(nextExtension);
			}
		}
		
		userDefinedText.setText(entries.toString());
	}

	/**
	 * @see org.eclipse.rse.ui.dialogs.SystemSelectFileTypesDialog#addUserDefinedEntries(java.util.List)
	 */
	protected void addUserDefinedEntries(List result) {
		StringTokenizer tokenizer = new StringTokenizer(userDefinedText.getText(), TYPE_DELIMITER);

		while (tokenizer.hasMoreTokens()) {
			String currentExtension = tokenizer.nextToken().trim();
			
			if (!currentExtension.equals("")) {
				result.add(currentExtension);
			}
		}
	}
	
	/**
	 * @see org.eclipse.rse.ui.dialogs.SystemPromptDialog#processOK()
	 */
	protected boolean processOK() {
		
		IFileEditorMapping[] children = getInput();
		List list = new ArrayList();
		
		// build a list of selected children
		for (int i = 0; i < children.length; ++i) {
			IFileEditorMapping element = children[i];
			
			if (listViewer.getChecked(element)) {
				list.add(element.getLabel());
			}
		}
		
		addUserDefinedEntries(list);
		setResult(list);
		return true;
	}
	
	/**
	 * @see org.eclipse.rse.ui.dialogs.SystemSelectFileTypesDialog#validateFileType(java.lang.String)
	 */
	protected boolean validateFileType(String filename) {
		return true;
	}
}