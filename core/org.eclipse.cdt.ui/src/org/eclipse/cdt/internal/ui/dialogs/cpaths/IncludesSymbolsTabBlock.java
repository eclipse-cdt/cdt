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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.TreeListDialogField;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class IncludesSymbolsTabBlock extends AbstractPathOptionBlock {

	private CPathIncludeEntryPage fIncludePage;
	private CPathSymbolEntryPage fSymbolsPage;
	private SourceTreeAdapter fSourceTreeAdapter;

	private class SourceTreeAdapter implements ITreeListAdapter {

		public void customButtonPressed(TreeListDialogField field, int index) {
		}

		public void selectionChanged(TreeListDialogField field) {
		}

		public void doubleClicked(TreeListDialogField field) {
		}

		public void keyPressed(TreeListDialogField field, KeyEvent event) {
		}

		public Object[] getChildren(TreeListDialogField field, Object element) {
			List children = new ArrayList();
			if (element instanceof ICContainer) {
				children.addAll(((ICContainer) element).getChildrenOfType(ICElement.C_CCONTAINER));
				children.addAll(((ICContainer) element).getChildrenOfType(ICElement.C_UNIT));
			}
			return children.toArray();
		}

		public Object getParent(TreeListDialogField field, Object element) {
			return ((ICElement) element).getParent();
		}

		public boolean hasChildren(TreeListDialogField field, Object element) {
			if (element instanceof ICContainer) {
				return ((ICContainer) element).hasChildren();
			}
			return false;
		}
	}

	public IncludesSymbolsTabBlock(IStatusChangeListener context, int pageToShow) {
		super(context, pageToShow);
		fSourceTreeAdapter = new SourceTreeAdapter();
	}

	protected void addTabs() {
		fIncludePage = new CPathIncludeEntryPage(fSourceTreeAdapter);
		addPage(fIncludePage);
		fSymbolsPage = new CPathSymbolEntryPage(fSourceTreeAdapter);
		addPage(fSymbolsPage);
	}
		
	public Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		if (getCProject() != null) {
			fIncludePage.init(getCProject());
			fSymbolsPage.init(getCProject());
		}
		Dialog.applyDialogFont(control);
		return control;
	}
	
	protected List getCPaths() {
		return new ArrayList();
	}

	public void init(ICElement cElement, IPathEntry[] cpathEntries) {
		setCProject(cElement.getCProject());
		boolean projectExists = false;
		List newClassPath = null;

		IProject project = getProject();
		if (cpathEntries == null) {
			try {
				cpathEntries = getCProject().getRawPathEntries();
			} catch (CModelException e) {
			}
		}
		if (cpathEntries != null) {
			newClassPath = getExistingEntries(cpathEntries);
		}
		if (fIncludePage != null) {
			fIncludePage.init(getCProject());
			fSymbolsPage.init(getCProject());
		}
		doStatusLineUpdate();
		initializeTimeStamps();
	}

	protected void internalConfigureCProject(List cPathEntries, IProgressMonitor monitor) throws CoreException,
			InterruptedException {

	}
}
