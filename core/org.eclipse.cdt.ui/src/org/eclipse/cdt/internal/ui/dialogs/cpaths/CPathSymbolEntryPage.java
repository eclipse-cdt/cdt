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
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;

public class CPathSymbolEntryPage extends ExtendedCPathBasePage {

	public CPathSymbolEntryPage(ITreeListAdapter adapter) {
		super(adapter, "SymbolEntryPage"); //$NON-NLS-1$
	}

	protected int getEntryKind() {
		return IPathEntry.CDT_MACRO;
	}

	protected void addPath() {
		// Popup an entry dialog
		InputDialog dialog = new InputDialog(getShell(), CPathEntryMessages.getString("SymbolEntryPage.addExternal.title"), //$NON-NLS-1$
				CPathEntryMessages.getString("SymbolEntryPage.addExternal.message"), "", //$NON-NLS-1$ //$NON-NLS-2$
				null);
		String symbol = null;
		if (dialog.open() == Window.OK) {
			symbol = dialog.getValue();
			if (symbol != null && symbol.length() > 0) {
				List cplist = fPathList.getElements();

				CPListElement newPath = newCPElement(((ICElement) getSelection().get(0)).getResource());
				newPath.setAttribute(CPListElement.MACRO_NAME, symbol);
				if (!cplist.contains(newPath)) {
					fPathList.addElement(newPath);
					fCPathList.add(newPath);
					fPathList.postSetSelection(new StructuredSelection(newPath));
				}
			}
		}
	}
}