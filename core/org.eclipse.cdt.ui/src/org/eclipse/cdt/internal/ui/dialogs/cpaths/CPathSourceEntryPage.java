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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.TypedViewerFilter;
import org.eclipse.cdt.internal.ui.util.PixelConverter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.TreeListDialogField;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.NewFolderDialog;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class CPathSourceEntryPage extends CPathBasePage {

	private ListDialogField fCPathList;
	private ICProject fCurrCProject;
	private IPath fProjPath;

	private IWorkspaceRoot fWorkspaceRoot;

	private TreeListDialogField fFoldersList;

	private final int IDX_ADD = 0;
	private final int IDX_EDIT = 2;
	private final int IDX_REMOVE = 3;

	/**
	 * @param title
	 */
	public CPathSourceEntryPage(ListDialogField classPathList) {
		super(CPathEntryMessages.getString("SourcePathEntryPage.title")); //$NON-NLS-1$
		setDescription(CPathEntryMessages.getString("SourcePathEntryPage.description")); //$NON-NLS-1$

		fWorkspaceRoot = CUIPlugin.getWorkspace().getRoot();
		fCPathList = classPathList;

		SourceContainerAdapter adapter = new SourceContainerAdapter();

		String[] buttonLabels;

		buttonLabels = new String[]{
		/* 0 = IDX_ADDEXIST */CPathEntryMessages.getString("SourcePathEntryPage.folders.add.button"), //$NON-NLS-1$
				/* 1 */null, /* 2 = IDX_EDIT */CPathEntryMessages.getString("SourcePathEntryPath.folders.edit.button"), //$NON-NLS-1$
				/* 3 = IDX_REMOVE */CPathEntryMessages.getString("SourcePathEntryPage.folders.remove.button") //$NON-NLS-1$
		};

		fFoldersList = new TreeListDialogField(adapter, buttonLabels, new CPElementLabelProvider());
		fFoldersList.setDialogFieldListener(adapter);
		fFoldersList.setLabelText(CPathEntryMessages.getString("SourcePathEntryPage.folders.label")); //$NON-NLS-1$

		fFoldersList.setViewerSorter(new CPElementSorter());
		fFoldersList.enableButton(IDX_EDIT, false);
		fFoldersList.enableButton(IDX_REMOVE, false);
	}

	public Image getImage() {
		return CPluginImages.get(CPluginImages.IMG_OBJS_SOURCE_ROOT);
	}

	public void init(ICProject cproject) {
		fCurrCProject = cproject;
		fProjPath = fCurrCProject.getProject().getFullPath();
		updateFoldersList();
	}

	private void updateFoldersList() {
		List folders = filterList(fCPathList.getElements());
		fFoldersList.setElements(folders);

		for (int i = 0; i < folders.size(); i++) {
			CPElement cpe = (CPElement)folders.get(i);
			IPath[] patterns = (IPath[])cpe.getAttribute(CPElement.EXCLUSION);
			if (patterns.length > 0) {
				fFoldersList.expandElement(cpe, 3);
			}
		}
	}

	public void createControl(Composite parent) {
		PixelConverter converter = new PixelConverter(parent);
		Composite composite = new Composite(parent, SWT.NONE);

		LayoutUtil.doDefaultLayout(composite, new DialogField[]{fFoldersList}, true);
		LayoutUtil.setHorizontalGrabbing(fFoldersList.getTreeControl(null));

		int buttonBarWidth = converter.convertWidthInCharsToPixels(24);
		fFoldersList.setButtonsMinWidth(buttonBarWidth);

		// expand
		List elements = fFoldersList.getElements();
		for (int i = 0; i < elements.size(); i++) {
			CPElement elem = (CPElement)elements.get(i);
			IPath[] patterns = (IPath[])elem.getAttribute(CPElement.EXCLUSION);
			if (patterns.length > 0) {
				fFoldersList.expandElement(elem, 3);
			}
		}
		setControl(composite);
		
		WorkbenchHelp.setHelp(composite, ICHelpContextIds.PROJECT_PATHS_SOURCE);	
	}

	private class SourceContainerAdapter implements ITreeListAdapter, IDialogFieldListener {

		private final Object[] EMPTY_ARR = new Object[0];

		// -------- IListAdapter --------
		public void customButtonPressed(TreeListDialogField field, int index) {
			sourcePageCustomButtonPressed(field, index);
		}

		public void selectionChanged(TreeListDialogField field) {
			sourcePageSelectionChanged(field);
		}

		public void doubleClicked(TreeListDialogField field) {
			sourcePageDoubleClicked(field);
		}

		public void keyPressed(TreeListDialogField field, KeyEvent event) {
			sourcePageKeyPressed(field, event);
		}

		public Object[] getChildren(TreeListDialogField field, Object element) {
			if (element instanceof CPElement) {
				return ((CPElement)element).getChildren();
			}
			return EMPTY_ARR;
		}

		public Object getParent(TreeListDialogField field, Object element) {
			if (element instanceof CPElementAttribute) {
				return ((CPElementAttribute)element).getParent();
			}
			return null;
		}

		public boolean hasChildren(TreeListDialogField field, Object element) {
			return (element instanceof CPElement);
		}

		// ---------- IDialogFieldListener --------
		public void dialogFieldChanged(DialogField field) {
			sourcePageDialogFieldChanged(field);
		}

	}

	protected void sourcePageKeyPressed(TreeListDialogField field, KeyEvent event) {
		if (field == fFoldersList) {
			if (event.character == SWT.DEL && event.stateMask == 0) {
				List selection = field.getSelectedElements();
				if (canRemove(selection)) {
					removeEntry();
				}
			}
		}
	}

	protected void sourcePageDoubleClicked(TreeListDialogField field) {
		if (field == fFoldersList) {
			List selection = field.getSelectedElements();
			if (canEdit(selection)) {
				editEntry();
			}
		}
	}

	protected void sourcePageCustomButtonPressed(DialogField field, int index) {
		if (field == fFoldersList) {
			if (index == IDX_ADD) {
				List elementsToAdd = new ArrayList(10);
				IProject project = fCurrCProject.getProject();
				if (project.exists()) {
					CPElement[] srcentries = openSourceContainerDialog(null);
					if (srcentries != null) {
						for (int i = 0; i < srcentries.length; i++) {
							elementsToAdd.add(srcentries[i]);
						}
					}
				} else {
					CPElement entry = openNewSourceContainerDialog(null, false);
					if (entry != null) {
						elementsToAdd.add(entry);
					}
				}
				if (!elementsToAdd.isEmpty()) {
					HashSet modifiedElements = new HashSet();
					askForAddingExclusionPatternsDialog(elementsToAdd, modifiedElements);

					fFoldersList.addElements(elementsToAdd);
					fFoldersList.postSetSelection(new StructuredSelection(elementsToAdd));

					if (!modifiedElements.isEmpty()) {
						for (Iterator iter = modifiedElements.iterator(); iter.hasNext();) {
							Object elem = iter.next();
							fFoldersList.refresh(elem);
							fFoldersList.expandElement(elem, 3);
						}
					}

				}
			} else if (index == IDX_EDIT) {
				editEntry();
			} else if (index == IDX_REMOVE) {
				removeEntry();
			}
		}
	}

	private void editEntry() {
		List selElements = fFoldersList.getSelectedElements();
		if (selElements.size() != 1) {
			return;
		}
		Object elem = selElements.get(0);
		if (fFoldersList.getIndexOfElement(elem) != -1) {
			editElementEntry((CPElement)elem);
		} else if (elem instanceof CPElementAttribute) {
			editAttributeEntry((CPElementAttribute)elem);
		}
	}

	private void editElementEntry(CPElement elem) {
		CPElement res = null;

		res = openNewSourceContainerDialog(elem, true);

		if (res != null) {
			fFoldersList.replaceElement(elem, res);
		}
	}

	private void editAttributeEntry(CPElementAttribute elem) {
		String key = elem.getKey();
		if (key.equals(CPElement.EXCLUSION)) {
			CPElement selElement = elem.getParent();
			ExclusionPatternDialog dialog = new ExclusionPatternDialog(getShell(), selElement);
			if (dialog.open() == Window.OK) {
				selElement.setAttribute(CPElement.EXCLUSION, dialog.getExclusionPattern());
				fFoldersList.refresh();
				fCPathList.dialogFieldChanged(); // validate
			}
		}
	}

	protected void sourcePageSelectionChanged(DialogField field) {
		List selected = fFoldersList.getSelectedElements();
		fFoldersList.enableButton(IDX_EDIT, canEdit(selected));
		fFoldersList.enableButton(IDX_REMOVE, canRemove(selected));
	}

	private void removeEntry() {
		List selElements = fFoldersList.getSelectedElements();
		for (int i = selElements.size() - 1; i >= 0; i--) {
			Object elem = selElements.get(i);
			if (elem instanceof CPElementAttribute) {
				CPElementAttribute attrib = (CPElementAttribute)elem;
				String key = attrib.getKey();
				Object value = key.equals(CPElement.EXCLUSION) ? new Path[0] : null;
				attrib.getParent().setAttribute(key, value);
				selElements.remove(i);
			}
		}
		if (selElements.isEmpty()) {
			fFoldersList.refresh();
			fCPathList.dialogFieldChanged(); // validate
		} else {
			fFoldersList.removeElements(selElements);
		}
	}

	private boolean canRemove(List selElements) {
		if (selElements.size() == 0) {
			return false;
		}
		for (int i = 0; i < selElements.size(); i++) {
			Object elem = selElements.get(i);
			if (elem instanceof CPElementAttribute) {
				CPElementAttribute attrib = (CPElementAttribute)elem;
				if (attrib.getKey().equals(CPElement.EXCLUSION)) {
					if ( ((IPath[])attrib.getValue()).length == 0) {
						return false;
					}
				} else if (attrib.getValue() == null) {
					return false;
				}
			} else if (elem instanceof CPElement) {
				CPElement curr = (CPElement)elem;
				if (curr.getParentContainer() != null) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean canEdit(List selElements) {
		if (selElements.size() != 1) {
			return false;
		}
		Object elem = selElements.get(0);
		if (elem instanceof CPElement) {
			return false;
		}
		if (elem instanceof CPElementAttribute) {
			return true;
		}
		return false;
	}

	void sourcePageDialogFieldChanged(DialogField field) {
		if (fCurrCProject == null) {
			// not initialized
			return;
		}

		if (field == fFoldersList) {
			updateClasspathList();
		}
	}

	private void updateClasspathList() {
		List srcelements = fFoldersList.getElements();

		List cpelements = fCPathList.getElements();
		int nEntries = cpelements.size();
		// backwards, as entries will be deleted
		int lastRemovePos = nEntries;
		int afterLastSourcePos = 0;
		for (int i = nEntries - 1; i >= 0; i--) {
			CPElement cpe = (CPElement)cpelements.get(i);
			int kind = cpe.getEntryKind();
			if (isEntryKind(kind)) {
				if (!srcelements.remove(cpe)) {
					cpelements.remove(i);
					lastRemovePos = i;
				} else if (lastRemovePos == nEntries) {
					afterLastSourcePos = i + 1;
				}
			}
		}

		if (!srcelements.isEmpty()) {
			int insertPos = Math.min(afterLastSourcePos, lastRemovePos);
			cpelements.addAll(insertPos, srcelements);
		}

		if (lastRemovePos != nEntries || !srcelements.isEmpty()) {
			fCPathList.setElements(cpelements);
		}
	}

	private CPElement openNewSourceContainerDialog(CPElement existing, boolean includeLinked) {
		if (includeLinked) {
			NewFolderDialog dialog = new NewFolderDialog(getShell(), fCurrCProject.getProject());
			if (dialog.open() == Window.OK) {
				IResource createdFolder = (IResource)dialog.getResult()[0];
				return newCPSourceElement(createdFolder);
			}
			return null;
		}
		String title = (existing == null) ? CPathEntryMessages.getString("SourcePathEntryPage.NewSourceFolderDialog.new.title") //$NON-NLS-1$
				: CPathEntryMessages.getString("SourcePathEntryPage.NewSourceFolderDialog.edit.title"); //$NON-NLS-1$

		IProject proj = fCurrCProject.getProject();
		NewSourceFolderDialog dialog = new NewSourceFolderDialog(getShell(), title, proj, getExistingContainers(existing), existing);
		dialog.setMessage(CPathEntryMessages.getFormattedString("SourcePathEntryPage.NewSourceFolderDialog.description", //$NON-NLS-1$
				fProjPath.toString()));
		if (dialog.open() == Window.OK) {
			IResource folder = dialog.getSourceFolder();
			return newCPSourceElement(folder);
		}
		return null;
	}

	private void askForAddingExclusionPatternsDialog(List newEntries, Set modifiedEntries) {
		fixNestingConflicts(newEntries, fFoldersList.getElements(), modifiedEntries);
		if (!modifiedEntries.isEmpty()) {
			String title = CPathEntryMessages.getString("SourcePathEntryPage.exclusion_added.title"); //$NON-NLS-1$
			String message = CPathEntryMessages.getString("SourcePathEntryPage.exclusion_added.message"); //$NON-NLS-1$
			MessageDialog.openInformation(getShell(), title, message);
		}
	}

	private CPElement[] openSourceContainerDialog(CPElement existing) {

		Class[] acceptedClasses = new Class[]{IProject.class, IFolder.class};
		List existingContainers = getExistingContainers(null);

		IProject[] allProjects = fWorkspaceRoot.getProjects();
		ArrayList rejectedElements = new ArrayList(allProjects.length);
		IProject currProject = fCurrCProject.getProject();
		for (int i = 0; i < allProjects.length; i++) {
			if (!allProjects[i].equals(currProject)) {
				rejectedElements.add(allProjects[i]);
			}
		}

		ViewerFilter filter = new TypedViewerFilter(acceptedClasses, rejectedElements.toArray());

		ILabelProvider lp = new WorkbenchLabelProvider();
		ITreeContentProvider cp = new BaseWorkbenchContentProvider();

		String title = (existing == null)
				? CPathEntryMessages.getString("SourcePathEntryPage.ExistingSourceFolderDialog.new.title") //$NON-NLS-1$
				: CPathEntryMessages.getString("SourcePathEntryPage.ExistingSourceFolderDialog.edit.title"); //$NON-NLS-1$
		String message = (existing == null)
				? CPathEntryMessages.getString("SourcePathEntryPage.ExistingSourceFolderDialog.new.description") //$NON-NLS-1$
				: CPathEntryMessages.getString("SourcePathEntryPage.ExistingSourceFolderDialog.edit.description"); //$NON-NLS-1$

		MultipleFolderSelectionDialog dialog = new MultipleFolderSelectionDialog(getShell(), lp, cp);
		dialog.setExisting(existingContainers.toArray());
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.addFilter(filter);
		dialog.setInput(fCurrCProject.getProject().getParent());
		if (existing == null) {
			dialog.setInitialFocus(fCurrCProject.getProject());
		} else {
			dialog.setInitialFocus(existing.getResource());
		}
		if (dialog.open() == Window.OK) {
			Object[] elements = dialog.getResult();
			CPElement[] res = new CPElement[elements.length];
			for (int i = 0; i < res.length; i++) {
				IResource elem = (IResource)elements[i];
				res[i] = newCPSourceElement(elem);
			}
			return res;
		}
		return null;
	}

	private List getExistingContainers(CPElement existing) {
		List res = new ArrayList();
		List cplist = fFoldersList.getElements();
		for (int i = 0; i < cplist.size(); i++) {
			CPElement elem = (CPElement)cplist.get(i);
			if (elem != existing) {
				IResource resource = elem.getResource();
				if (resource instanceof IContainer) { // defensive code
					res.add(resource);
				}
			}
		}
		return res;
	}

	private CPElement newCPSourceElement(IResource res) {
		Assert.isNotNull(res);
		return new CPElement(fCurrCProject, IPathEntry.CDT_SOURCE, res.getFullPath(), res);
	}

	/*
	 * @see BuildPathBasePage#getSelection
	 */
	public List getSelection() {
		return fFoldersList.getSelectedElements();
	}

	/*
	 * @see BuildPathBasePage#setSelection
	 */
	public void setSelection(List selElements) {
		fFoldersList.selectElements(new StructuredSelection(selElements));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathBasePage#isEntryKind(int)
	 */
	public boolean isEntryKind(int kind) {
		return kind == IPathEntry.CDT_SOURCE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.AbstractCOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.AbstractCOptionPage#performDefaults()
	 */
	public void performDefaults() {
	}
}