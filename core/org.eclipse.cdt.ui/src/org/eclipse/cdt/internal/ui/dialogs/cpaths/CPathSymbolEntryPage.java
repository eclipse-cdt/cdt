/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;

public class CPathSymbolEntryPage extends ExtendedCPathBasePage {

	private static final int IDX_ADD = 0;
	private static final int IDX_ADD_CONTRIBUTED = 1;
	private static final int IDX_EDIT = 3;
	private static final int IDX_REMOVE = 4;

	private final static String[] buttonLabel = new String[]{
	/* 0 */CPathEntryMessages.getString("SymbolEntryPage.addUser"), //$NON-NLS-1$
			/* 1 */CPathEntryMessages.getString("SymbolEntryPage.addContributed"), //$NON-NLS-1$
			null, //$NON-NLS-1$
			/* 3 */CPathEntryMessages.getString("SymbolEntryPage.edit"), //$NON-NLS-1$
			/* 4 */CPathEntryMessages.getString("SymbolEntryPage.remove")}; //$NON-NLS-1$

	public CPathSymbolEntryPage(ITreeListAdapter adapter) {
		super(adapter, CPathEntryMessages.getString("SymbolEntryPage.title"), //$NON-NLS-1$
				CPathEntryMessages.getString("SymbolEntryPage.listName"), buttonLabel); //$NON-NLS-1$
	}

	protected void buttonPressed(int indx, List selected) {
		switch (indx) {
			case IDX_ADD :
				addSymbol();
				break;
			case IDX_ADD_CONTRIBUTED :
				addContributed();
				break;
			case IDX_EDIT :
				if (canEdit(selected)) {
					editSymbol((CPElement)selected.get(0));
				}
				break;
			case IDX_REMOVE :
				if (canRemove(selected)) {
					removeSymbol((CPElement)selected.get(0));
				}
				break;
		}
	}

	protected void pathSelectionChanged() {
		List selected = getPathList().getSelectedElements();
		getPathList().enableButton(IDX_REMOVE, canRemove(selected));
		getPathList().enableButton(IDX_EDIT, canEdit(selected));
	}

	protected boolean canRemove(List selected) {
		return !selected.isEmpty();
	}

	protected boolean canEdit(List selected) {
		if (!selected.isEmpty()) {
			return !isPathInheritedFromSelected((CPElement)selected.get(0));
		}
		return false;
	}

	protected void editSymbol(CPElement element) {
	}

	protected void removeSymbol(CPElement element) {
		removeFromSelectedPath(element);
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		getPathList().enableButton(IDX_REMOVE, false);
		getPathList().enableButton(IDX_EDIT, false);
	}

	public boolean isEntryKind(int kind) {
		return IPathEntry.CDT_MACRO == kind;
	}

	protected void addSymbol() {
		// Popup an entry dialog
		InputDialog dialog = new InputDialog(getShell(), CPathEntryMessages.getString("SymbolEntryPage.addExternal.title"), //$NON-NLS-1$
				CPathEntryMessages.getString("SymbolEntryPage.addExternal.message"), "", //$NON-NLS-1$ //$NON-NLS-2$
				null);
		String symbol = null;
		if (dialog.open() == Window.OK) {
			symbol = dialog.getValue();
			if (symbol != null && symbol.length() > 0) {
				List cplist = getPathList().getElements();
				IResource resource = ((ICElement)getSelection().get(0)).getResource();
				CPElement newPath = new CPElement(fCurrCProject, IPathEntry.CDT_MACRO, resource.getFullPath(), resource);
				String name, value = ""; //$NON-NLS-1$
				int index = symbol.indexOf("="); //$NON-NLS-1$
				if (index != -1) {
					name = symbol.substring(0, index).trim();
					value = symbol.substring(index + 1).trim();
				} else {
					name = symbol.trim();
				}
				newPath.setAttribute(CPElement.MACRO_NAME, name);
				newPath.setAttribute(CPElement.MACRO_VALUE, value);
				if (!cplist.contains(newPath)) {
					getPathList().addElement(newPath);
					fCPathList.add(newPath);
					getPathList().postSetSelection(new StructuredSelection(newPath));
				}
			}
		}
	}

	protected void addContributed() {
		CPElement[] includes = openContainerSelectionDialog(null);
		if (includes != null) {
			int nElementsChosen = includes.length;
			// remove duplicates
			List cplist = getPathList().getElements();
			List elementsToAdd = new ArrayList(nElementsChosen);

			for (int i = 0; i < nElementsChosen; i++) {
				CPElement curr = includes[i];
				if (!cplist.contains(curr) && !elementsToAdd.contains(curr)) {
					elementsToAdd.add(curr);
				}
			}

			getPathList().addElements(elementsToAdd);
			fCPathList.addAll(elementsToAdd);
			getPathList().postSetSelection(new StructuredSelection(includes));
		}
	}

	protected CPElement[] openContainerSelectionDialog(CPElement existing) {
		IPathEntry elem = null;
		String title;
		if (existing == null) {
			title = CPathEntryMessages.getString("SymbolEntryPage.ContainerDialog.new.title"); //$NON-NLS-1$
		} else {
			title = CPathEntryMessages.getString("SymbolEntryPage.ContainerDialog.edit.title"); //$NON-NLS-1$
			elem = existing.getPathEntry();
		}
		CPathContainerWizard wizard = new CPathContainerWizard(elem, null, (ICElement)getSelection().get(0), getRawPathEntries(),
				new int[] {IPathEntry.CDT_MACRO});
		wizard.setWindowTitle(title);
		if (CPathContainerWizard.openWizard(getShell(), wizard) == Window.OK) {
			IPathEntry parent = wizard.getEntriesParent();
			IPathEntry[] elements = wizard.getEntries();
			IResource resource = ((ICElement)getSelection().get(0)).getResource();
			if (elements != null) {
				CPElement[] res = new CPElement[elements.length];
				for (int i = 0; i < res.length; i++) {
					res[i] = new CPElement(fCurrCProject, IPathEntry.CDT_MACRO, resource.getFullPath(), resource);
					res[i].setAttribute(CPElement.MACRO_NAME, ((IMacroEntry)elements[i]).getMacroName());
					res[i].setAttribute(CPElement.BASE_REF, parent.getPath());
				}
				return res;
			}
		}
		return null;
	}
}