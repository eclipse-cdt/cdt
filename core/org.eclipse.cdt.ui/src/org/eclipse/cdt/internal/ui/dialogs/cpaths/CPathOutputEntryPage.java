/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.NewFolderDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.TypedViewerFilter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.TreeListDialogField;

/**
 * Output tab for C/C++ Project Paths page for 3.X projects.
 * 
 * @deprecated as of CDT 4.0. This tab was used to set preferences/properties
 * for 3.X style projects.
 */
@Deprecated
public class CPathOutputEntryPage extends CPathBasePage {

	private ListDialogField<CPElement> fCPathList;
	private ICProject fCurrCProject;
	private IPath fProjPath;

	private IWorkspaceRoot fWorkspaceRoot;

	private TreeListDialogField<CPElement> fOutputList;

	private final int IDX_ADD = 0;
	private final int IDX_EDIT = 2;
	private final int IDX_REMOVE = 3;

	public CPathOutputEntryPage(ListDialogField<CPElement> cPathList) {
		super(CPathEntryMessages.OutputPathEntryPage_title); 
		setDescription(CPathEntryMessages.OutputPathEntryPage_description); 

		fWorkspaceRoot = CUIPlugin.getWorkspace().getRoot();
		fCPathList = cPathList;

		OutputContainerAdapter adapter = new OutputContainerAdapter();

		String[] buttonLabels;

		buttonLabels = new String[]{
		CPathEntryMessages.OutputPathEntryPage_folders_add_button, 
				/* 1 */null, CPathEntryMessages.OutputPathEntryPage_folders_edit_button, 
				CPathEntryMessages.OutputPathEntryPage_folders_remove_button
		};

		fOutputList = new TreeListDialogField<CPElement>(adapter, buttonLabels, new CPElementLabelProvider());
		fOutputList.setDialogFieldListener(adapter);
		fOutputList.setLabelText(CPathEntryMessages.OutputPathEntryPage_folders_label); 

		fOutputList.setViewerComparator(new CPElementSorter());
		fOutputList.enableButton(IDX_EDIT, false);
		fOutputList.enableButton(IDX_REMOVE, false);
		fOutputList.setTreeExpansionLevel(2);
	}

	@Override
	public Image getImage() {
		return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_CONTAINER);
	}

	public void init(ICProject cproject) {
		fCurrCProject = cproject;
		fProjPath = fCurrCProject.getProject().getFullPath();
		updateFoldersList();
	}

	private void updateFoldersList() {

		List<CPElement> folders = filterList(fCPathList.getElements());
		fOutputList.setElements(folders);

		for (int i = 0; i < folders.size(); i++) {
			CPElement cpe = folders.get(i);
			IPath[] patterns = (IPath[])cpe.getAttribute(CPElement.EXCLUSION);
			if (patterns.length > 0) {
				fOutputList.expandElement(cpe, 3);
			}
		}
	}

	@Override
	public void createControl(Composite parent) {
		PixelConverter converter = new PixelConverter(parent);
		Composite composite = new Composite(parent, SWT.NONE);

		LayoutUtil.doDefaultLayout(composite, new DialogField[]{fOutputList}, true);
		LayoutUtil.setHorizontalGrabbing(fOutputList.getTreeControl(null), true);

		int buttonBarWidth = converter.convertWidthInCharsToPixels(24);
		fOutputList.setButtonsMinWidth(buttonBarWidth);

		// expand
		List<CPElement> elements = fOutputList.getElements();
		for (int i = 0; i < elements.size(); i++) {
			CPElement elem = elements.get(i);
			IPath[] patterns = (IPath[])elem.getAttribute(CPElement.EXCLUSION);
			if (patterns.length > 0) {
				fOutputList.expandElement(elem, 3);
			}
		}
		setControl(composite);
		
		CUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(composite, ICHelpContextIds.PROJECT_PATHS_OUTPUT);
	}

	private class OutputContainerAdapter implements ITreeListAdapter<CPElement>, IDialogFieldListener {

		private final Object[] EMPTY_ARR = new Object[0];

		// -------- IListAdapter --------
		@Override
		public void customButtonPressed(TreeListDialogField<CPElement> field, int index) {
			outputPageCustomButtonPressed(field, index);
		}

		@Override
		public void selectionChanged(TreeListDialogField<CPElement> field) {
			outputPageSelectionChanged(field);
		}

		@Override
		public void doubleClicked(TreeListDialogField<CPElement> field) {
			outputPageDoubleClicked(field);
		}

		@Override
		public void keyPressed(TreeListDialogField<CPElement> field, KeyEvent event) {
			outputPageKeyPressed(field, event);
		}

		@Override
		public Object[] getChildren(TreeListDialogField<CPElement> field, Object element) {
			if (element instanceof CPElement) {
				return ((CPElement)element).getChildren();
			}
			return EMPTY_ARR;
		}

		@Override
		public Object getParent(TreeListDialogField<CPElement> field, Object element) {
			if (element instanceof CPElementAttribute) {
				return ((CPElementAttribute)element).getParent();
			}
			return null;
		}

		@Override
		public boolean hasChildren(TreeListDialogField<CPElement> field, Object element) {
			return (element instanceof CPElement);
		}

		// ---------- IDialogFieldListener --------
		@Override
		public void dialogFieldChanged(DialogField field) {
			outputPageDialogFieldChanged(field);
		}

	}

	protected void outputPageKeyPressed(TreeListDialogField<CPElement> field, KeyEvent event) {
		if (field == fOutputList) {
			if (event.character == SWT.DEL && event.stateMask == 0) {
				List<?> selection = field.getSelectedElements();
				if (canRemove(selection)) {
					removeEntry();
				}
			}
		}
	}

	protected void outputPageDoubleClicked(TreeListDialogField<CPElement> field) {
		if (field == fOutputList) {
			List<?> selection = field.getSelectedElements();
			if (canEdit(selection)) {
				editEntry();
			}
		}
	}

	private boolean hasFolders(IContainer container) {
		try {
			IResource[] members = container.members();
			for (IResource member : members) {
				if (member instanceof IContainer) {
					return true;
				}
			}
		} catch (CoreException e) {
			// ignore
		}
		return false;
	}

	protected void outputPageCustomButtonPressed(DialogField field, int index) {
		if (field == fOutputList) {
			if (index == IDX_ADD) {
				List<CPElement> elementsToAdd = new ArrayList<CPElement>(10);
				IProject project = fCurrCProject.getProject();
				if (project.exists()) {
					if (hasFolders(project)) {
						CPElement[] srcentries = openOutputContainerDialog(null);
						if (srcentries != null) {
							for (CPElement srcentrie : srcentries) {
								elementsToAdd.add(srcentrie);
							}
						}
					} else {
						CPElement entry = openNewOutputContainerDialog(null, true);
						if (entry != null) {
							elementsToAdd.add(entry);
						}
					}
				} else {
					CPElement entry = openNewOutputContainerDialog(null, false);
					if (entry != null) {
						elementsToAdd.add(entry);
					}
				}
				if (!elementsToAdd.isEmpty()) {
					HashSet<CPElement> modifiedElements = new HashSet<CPElement>();
					askForAddingExclusionPatternsDialog(elementsToAdd, modifiedElements);

					fOutputList.addElements(elementsToAdd);
					fOutputList.postSetSelection(new StructuredSelection(elementsToAdd));

					if (!modifiedElements.isEmpty()) {
						for (Iterator<CPElement> iter = modifiedElements.iterator(); iter.hasNext();) {
							Object elem = iter.next();
							fOutputList.refresh(elem);
							fOutputList.expandElement(elem, 3);
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
		List<?> selElements = fOutputList.getSelectedElements();
		if (selElements.size() != 1) {
			return;
		}
		Object elem = selElements.get(0);
		if (fOutputList.getIndexOfElement(elem) != -1) {
			editElementEntry((CPElement)elem);
		} else if (elem instanceof CPElementAttribute) {
			editAttributeEntry((CPElementAttribute)elem);
		}
	}

	private void editElementEntry(CPElement elem) {
		CPElement res = null;

		res = openNewOutputContainerDialog(elem, true);

		if (res != null) {
			fOutputList.replaceElement(elem, res);
		}
	}

	private void editAttributeEntry(CPElementAttribute elem) {
		String key = elem.getKey();
		if (key.equals(CPElement.EXCLUSION)) {
			CPElement selElement = elem.getParent();
			ExclusionPatternDialog dialog = new ExclusionPatternDialog(getShell(), selElement);
			if (dialog.open() == Window.OK) {
				selElement.setAttribute(CPElement.EXCLUSION, dialog.getExclusionPattern());
				fOutputList.refresh();
				fCPathList.dialogFieldChanged(); // validate
			}
		}
	}

	protected void outputPageSelectionChanged(DialogField field) {
		List<?> selected = fOutputList.getSelectedElements();
		fOutputList.enableButton(IDX_EDIT, canEdit(selected));
		fOutputList.enableButton(IDX_REMOVE, canRemove(selected));
	}

	private void removeEntry() {
		List<Object> selElements = fOutputList.getSelectedElements();
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
			fOutputList.refresh();
			fCPathList.dialogFieldChanged(); // validate
		} else {
			fOutputList.removeElements(selElements);
		}
	}

	private boolean canRemove(List<?> selElements) {
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

	private boolean canEdit(List<?> selElements) {
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

	protected void outputPageDialogFieldChanged(DialogField field) {
		if (fCurrCProject == null) {
			// not initialized
			return;
		}

		if (field == fOutputList) {
			updateCPathList();
		}
	}

	private void updateCPathList() {
		List<CPElement> srcelements = fOutputList.getElements();

		List<CPElement> cpelements = fCPathList.getElements();
		int nEntries = cpelements.size();
		// backwards, as entries will be deleted
		int lastRemovePos = nEntries;
		int afterLastSourcePos = 0;
		for (int i = nEntries - 1; i >= 0; i--) {
			CPElement cpe = cpelements.get(i);
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

	private CPElement openNewOutputContainerDialog(CPElement existing, boolean includeLinked) {
		if (includeLinked) {
			NewFolderDialog dialog = new NewFolderDialog(getShell(), fCurrCProject.getProject());
			if (dialog.open() == Window.OK) {
				IResource createdFolder = (IResource)dialog.getResult()[0];
				return newCPOutputElement(createdFolder);
			}
			return null;
		}
		String title = (existing == null) ? CPathEntryMessages.SourcePathEntryPage_NewSourceFolderDialog_new_title
				: CPathEntryMessages.SourcePathEntryPage_NewSourceFolderDialog_edit_title; 
		IProject proj = fCurrCProject.getProject();
		NewSourceFolderDialog dialog = new NewSourceFolderDialog(getShell(), title, proj, getExistingContainers(existing), existing);
		dialog.setMessage(NLS.bind(CPathEntryMessages.SourcePathEntryPage_NewSourceFolderDialog_description, 
				fProjPath.toString()));
		if (dialog.open() == Window.OK) {
			IResource folder = dialog.getSourceFolder();
			return newCPOutputElement(folder);
		}
		return null;
	}

	private void askForAddingExclusionPatternsDialog(List<CPElement> newEntries, Set<CPElement> modifiedEntries) {
		fixNestingConflicts(newEntries, fOutputList.getElements(), modifiedEntries);
		if (!modifiedEntries.isEmpty()) {
			String title = CPathEntryMessages.OutputPathEntryPage_exclusion_added_title; 
			String message = CPathEntryMessages.OutputPathEntryPage_exclusion_added_message; 
			MessageDialog.openInformation(getShell(), title, message);
		}
	}

	private CPElement[] openOutputContainerDialog(CPElement existing) {

		Class<?>[] acceptedClasses = new Class[]{IProject.class, IFolder.class};
		List<IContainer> existingContainers = getExistingContainers(null);

		IProject[] allProjects = fWorkspaceRoot.getProjects();
		ArrayList<IProject> rejectedElements = new ArrayList<IProject>(allProjects.length);
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
				? CPathEntryMessages.OutputPathEntryPage_ExistingOutputFolderDialog_new_title
				: CPathEntryMessages.OutputPathEntryPage_ExistingOutputFolderDialog_edit_title; 
		String message = (existing == null)
				? CPathEntryMessages.OutputPathEntryPage_ExistingOutputFolderDialog_new_description
				: CPathEntryMessages.OutputPathEntryPage_ExistingOutputFolderDialog_edit_description; 

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
				res[i] = newCPOutputElement(elem);
			}
			return res;
		}
		return null;
	}

	private List<IContainer> getExistingContainers(CPElement existing) {
		List<IContainer> res = new ArrayList<IContainer>();
		List<CPElement> cplist = fOutputList.getElements();
		for (int i = 0; i < cplist.size(); i++) {
			CPElement elem = cplist.get(i);
			if (elem != existing) {
				IResource resource = elem.getResource();
				if (resource instanceof IContainer) { // defensive code
					res.add((IContainer) resource);
				}
			}
		}
		return res;
	}

	private CPElement newCPOutputElement(IResource res) {
		Assert.isNotNull(res);
		return new CPElement(fCurrCProject, IPathEntry.CDT_OUTPUT, res.getFullPath(), res);
	}

	/*
	 * @see BuildPathBasePage#getSelection
	 */
	@Override
	public List<Object> getSelection() {
		return fOutputList.getSelectedElements();
	}

	/*
	 * @see BuildPathBasePage#setSelection
	 */
	@Override
	public void setSelection(List<?> selElements) {
		fOutputList.selectElements(new StructuredSelection(selElements));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathBasePage#isEntryKind(int)
	 */
	@Override
	public boolean isEntryKind(int kind) {
		return kind == IPathEntry.CDT_OUTPUT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	@Override
	public void performDefaults() {
	}

}
