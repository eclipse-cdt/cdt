/*******************************************************************************
 * Copyright (c) 2002, 2003, 2004 QNX Software Systems Ltd. and others. All
 * rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies
 * this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

public class CPathIncludeEntryPage extends ExtendedCPathBasePage {

	public CPathIncludeEntryPage(ITreeListAdapter adapter) {
		super(adapter, "IncludeEntryPage"); //$NON-NLS-1$
	}

	public int getEntryKind() {
		return IPathEntry.CDT_INCLUDE;
	}

	protected void addPath() {
		InputDialog dialog = new SelectPathInputDialog(getShell(),
				CPathEntryMessages.getString("IncludeEntryPage.addExternal.title"), //$NON-NLS-1$
				CPathEntryMessages.getString("IncludeEntryPage.addExternal.message"), null, null); //$NON-NLS-1$
		String newItem = null;
		if (dialog.open() == Window.OK) {
			newItem = dialog.getValue();
			if (newItem != null && !newItem.equals("")) { //$NON-NLS-1$
				List cplist = fPathList.getElements();

				CPListElement newPath = newCPElement(((ICElement) getSelection().get(0)).getResource(), null);
				newPath.setAttribute(CPListElement.INCLUDE, new Path(newItem));
				if (!cplist.contains(newPath)) {
					fPathList.addElement(newPath);
					fCPathList.add(newPath);
					fPathList.postSetSelection(new StructuredSelection(newPath));
				}
			}
		}
	}

	private class SelectPathInputDialog extends InputDialog {

		public SelectPathInputDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue,
				IInputValidator validator) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
		}

		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			Button browse = createButton(parent, 3, CPathEntryMessages.getString("IncludeEntryPage.addExternal.button.browse"), //$NON-NLS-1$
					true); //$NON-NLS-1$
			browse.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent ev) {
					DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
					String currentName = getText().getText();
					if (currentName != null && currentName.trim().length() != 0) {
						dialog.setFilterPath(currentName);
					}
					String dirname = dialog.open();
					if (dirname != null) {
						getText().setText(dirname);
					}
				}
			});
		}

	}

	protected CPListElement newCPElement(IResource resource, CPListElement copyFrom) {
		CPListElement element =  new CPListElement(fCurrCProject, getEntryKind(), resource.getFullPath(), resource);
		if (copyFrom != null) {
			element.setAttribute(CPListElement.INCLUDE, copyFrom.getAttribute(CPListElement.INCLUDE));
			element.setAttribute(CPListElement.SYSTEM_INCLUDE, copyFrom.getAttribute(CPListElement.SYSTEM_INCLUDE));
		}
		return element;
	}

}