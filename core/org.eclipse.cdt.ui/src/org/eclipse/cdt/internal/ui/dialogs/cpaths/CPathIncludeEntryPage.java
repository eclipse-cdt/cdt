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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;

public class CPathIncludeEntryPage extends ExtendedCPathBasePage {

	public CPathIncludeEntryPage(ITreeListAdapter adapter) {
		super(adapter, "IncludeEntryPage"); //$NON-NLS-1$
	}

	public boolean isEntryKind(int kind) {
		return kind == IPathEntry.CDT_INCLUDE;
	}

	protected void addContributed() {
		CPListElement[] includes = openContainerSelectionDialog(null);
		if (includes != null) {
			int nElementsChosen= includes.length;					
			// remove duplicates
			List cplist= fPathList.getElements();
			List elementsToAdd= new ArrayList(nElementsChosen);
			
			for (int i= 0; i < nElementsChosen; i++) {
				CPListElement curr= includes[i];
				if (!cplist.contains(curr) && !elementsToAdd.contains(curr)) {
					elementsToAdd.add(curr);
//					curr.setAttribute(CPListElement.SOURCEATTACHMENT, BuildPathSupport.guessSourceAttachment(curr));
//					curr.setAttribute(CPListElement.JAVADOC, JavaUI.getLibraryJavadocLocation(curr.getPath()));
				}
			}
			
			fPathList.addElements(elementsToAdd);
			fPathList.postSetSelection(new StructuredSelection(includes));
		}
	}

	protected void addFromWorkspace() {
		// dinglis-TODO Auto-generated method stub

	}

	protected void addPath() {
		// dinglis-TODO Auto-generated method stub

	}

	private CPListElement[] openContainerSelectionDialog(CPListElement existing) {
		IPathEntry elem= null;
		String title;
		if (existing == null) {
			title= CPathEntryMessages.getString("IncludeEntryPage.ContainerDialog.new.title"); //$NON-NLS-1$
		} else {
			title= CPathEntryMessages.getString("IncludeEntryPage.ContainerDialog.edit.title"); //$NON-NLS-1$
			elem= existing.getPathEntry();
		}
		CPathContainerWizard wizard= new CPathContainerWizard(elem, fCurrCProject, getRawClasspath());
		wizard.setWindowTitle(title);
		if (CPathContainerWizard.openWizard(getShell(), wizard) == Window.OK) {
			IPathEntry[] created= wizard.getNewEntries();
			if (created != null) {
				CPListElement[] res= new CPListElement[created.length];
				for (int i= 0; i < res.length; i++) {
					res[i]= new CPListElement(fCurrCProject, IPathEntry.CDT_CONTAINER, created[i].getPath(), null);
				}
				return res;
			}
		}			
		return null;
	}
}