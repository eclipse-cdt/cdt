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

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.cdt.internal.ui.wizards.TypedElementSelectionValidator;
import org.eclipse.cdt.internal.ui.wizards.TypedViewerFilter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class CPathIncludeEntryPage extends ExtendedCPathBasePage {

	private static final int IDX_ADD_USER = 0;
	private static final int IDX_ADD_WORKSPACE = 1;
	private static final int IDX_ADD_CONTRIBUTED = 2;
	private static final int IDX_EDIT = 4;
	private static final int IDX_REMOVE = 5;

	private static final String[] buttonLabel = new String[] {
	/* 0 */CPathEntryMessages.getString("IncludeEntryPage.addExternal"), //$NON-NLS-1$
			/* 1 */CPathEntryMessages.getString("IncludeEntryPage.addFromWorkspace"), //$NON-NLS-1$
			/* 2 */CPathEntryMessages.getString("IncludeEntryPage.addContributed"), null, //$NON-NLS-1$
			/* 4 */CPathEntryMessages.getString("IncludeEntryPage.edit"), //$NON-NLS-1$
			/* 5 */CPathEntryMessages.getString("IncludeEntryPage.remove")}; //$NON-NLS-1$

	public CPathIncludeEntryPage(ITreeListAdapter adapter) {
		super(adapter, CPathEntryMessages.getString("IncludeEntryPage.title"), //$NON-NLS-1$
				CPathEntryMessages.getString("IncludeEntryPage.listName"), buttonLabel); //$NON-NLS-1$
	}

	protected void buttonPressed(int indx, List selected) {
		switch (indx) {
			case IDX_ADD_USER:
				addInclude();
				break;
			case IDX_ADD_WORKSPACE:
				addFromWorkspace();
				break;
			case IDX_ADD_CONTRIBUTED:
				addContributed();
				break;
			case IDX_EDIT:
				if (canEdit(selected)) {
					editInclude((CPElement) selected.get(0));
				}
				break;
			case IDX_REMOVE:
				if (canRemove(selected)) {
					removeInclude((CPElement) selected.get(0));
				}
				break;
		}
	}

	public boolean isEntryKind(int kind) {
		return IPathEntry.CDT_INCLUDE == kind;
	}

	protected void pathSelectionChanged() {
		List selected = getPathList().getSelectedElements();
		getPathList().enableButton(IDX_REMOVE, canRemove(selected));
		getPathList().enableButton(IDX_EDIT, canEdit(selected));
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		getPathList().enableButton(IDX_REMOVE, false);
		getPathList().enableButton(IDX_EDIT, false);
	}

	protected boolean canRemove(List selected) {
		return !selected.isEmpty();
	}

	protected boolean canEdit(List selected) {
		if (!selected.isEmpty()) {
			return !isPathInheritedFromSelected((CPElement) selected.get(0));
		}
		return false;
	}

	protected void editInclude(CPElement element) {
	}

	protected void removeInclude(CPElement element) {
		removeFromSelectedPath(element);
	}

	protected void addInclude() {
		InputDialog dialog = new SelectPathInputDialog(getShell(),
				CPathEntryMessages.getString("IncludeEntryPage.addExternal.title"), //$NON-NLS-1$
				CPathEntryMessages.getString("IncludeEntryPage.addExternal.message"), null, null); //$NON-NLS-1$
		String newItem = null;
		if (dialog.open() == Window.OK) {
			newItem = dialog.getValue();
			if (newItem != null && !newItem.equals("")) { //$NON-NLS-1$
				List cplist = getPathList().getElements();
				ICElement element = (ICElement) getSelection().get(0);
				CPElement newPath = new CPElement(element.getCProject(), IPathEntry.CDT_INCLUDE, element.getPath(),
						element.getResource());
				newPath.setAttribute(CPElement.INCLUDE, new Path(newItem));
				if (!cplist.contains(newPath)) {
					getPathList().addElement(newPath);
					fCPathList.add(newPath);
					getPathList().postSetSelection(new StructuredSelection(newPath));
				}
			}
		}
	}

	protected void addFromWorkspace() {
		CPElement[] includes = openWorkspacePathEntryDialog(null);
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
			getPathList().postSetSelection(new StructuredSelection(elementsToAdd));
		}

	}

	protected CPElement[] openWorkspacePathEntryDialog(CPElement existing) {
		Class[] acceptedClasses = new Class[] { ICProject.class, IProject.class, IContainer.class, ICContainer.class};
		TypedElementSelectionValidator validator = new TypedElementSelectionValidator(acceptedClasses, existing == null);
		ViewerFilter filter = new TypedViewerFilter(acceptedClasses);

		String title = (existing == null) ? CPathEntryMessages.getString("IncludeEntryPage.fromWorkspaceDialog.new.title") //$NON-NLS-1$
				: CPathEntryMessages.getString("IncludeEntryPage.fromWorkspaceDialog.edit.title"); //$NON-NLS-1$
		String message = (existing == null) ? CPathEntryMessages.getString("IncludeEntryPage.fromWorkspaceDialog.new.description") //$NON-NLS-1$
				: NewWizardMessages.getString("IncludeEntryPage.fromWorkspaceDialog.edit.description"); //$NON-NLS-1$

		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(),
				new CElementContentProvider());
		dialog.setValidator(validator);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.addFilter(filter);
		dialog.setInput(CoreModel.getDefault().getCModel());
		if (existing == null) {
			dialog.setInitialSelection(fCurrCProject);
		} else {
			dialog.setInitialSelection(existing.getCProject());
		}

		if (dialog.open() == Window.OK) {
			Object[] elements = dialog.getResult();
			CPElement[] res = new CPElement[elements.length];
			ICElement element = (ICElement) getSelection().get(0);
			for (int i = 0; i < res.length; i++) {
				IProject project;
				IPath includePath;
				if (elements[i] instanceof IResource) {
					project = ((IResource) elements[i]).getProject();
					includePath = ((IResource) elements[i]).getProjectRelativePath();
				} else {
					project = ((ICElement) elements[i]).getCProject().getProject();
					includePath = ((ICElement) elements[i]).getResource().getProjectRelativePath();
				}
				res[i] = new CPElement(element.getCProject(), IPathEntry.CDT_INCLUDE, element.getPath(), element.getResource());
				res[i].setAttribute(CPElement.BASE, project.getFullPath().makeRelative());
				res[i].setAttribute(CPElement.INCLUDE, includePath);
			}
			return res;
		}
		return null;
	}

	protected CPElement[] openContainerSelectionDialog(CPElement existing) {
		IPathEntry elem = null;
		String title;
		if (existing == null) {
			title = CPathEntryMessages.getString("IncludeEntryPage.ContainerDialog.new.title"); //$NON-NLS-1$
		} else {
			title = CPathEntryMessages.getString("IncludeEntryPage.ContainerDialog.edit.title"); //$NON-NLS-1$
			elem = existing.getPathEntry();
		}
		CPathContainerWizard wizard = new CPathContainerWizard(elem, null, fCurrCProject, getRawClasspath(), IPathEntry.CDT_INCLUDE);
		wizard.setWindowTitle(title);
		if (CPathContainerWizard.openWizard(getShell(), wizard) == Window.OK) {
			IPathEntry parent = wizard.getEntriesParent();
			IPathEntry[] elements = wizard.getEntries();
			IResource resource = ((ICElement) getSelection().get(0)).getResource();

			if (elements != null) {
				CPElement[] res = new CPElement[elements.length];
				for (int i = 0; i < res.length; i++) {
					res[i] = new CPElement(fCurrCProject, IPathEntry.CDT_INCLUDE, resource.getFullPath(), resource);
					res[i].setAttribute(CPElement.INCLUDE, ((IIncludeEntry)elements[i]).getIncludePath());
					res[i].setAttribute(CPElement.BASE_REF, parent.getPath());
				}
				return res;
			}
		}
		return null;
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
}