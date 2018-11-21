/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.TreeListDialogField;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * Path Containers tab for C/C++ Project Paths page for 3.X projects.
 *
 * @deprecated as of CDT 4.0. This tab was used to set preferences/properties
 * for 3.X style projects.
 */
@Deprecated
public class CPathContainerEntryPage extends CPathBasePage {
	private ListDialogField<CPElement> fCPathList;
	private ICProject fCurrCProject;

	private TreeListDialogField<CPElement> fContainersList;

	private final int IDX_ADD = 0;

	private final int IDX_EDIT = 2;
	private final int IDX_REMOVE = 3;
	private final int IDX_EXPORT = 5;

	public CPathContainerEntryPage(ListDialogField<CPElement> cPathList) {
		super(CPathEntryMessages.ContainerEntryPage_title);
		fCPathList = cPathList;

		String[] buttonLabels = new String[] { CPathEntryMessages.ContainerEntryPage_add_button, /* */null,
				CPathEntryMessages.ContainerEntryPage_edit_button, CPathEntryMessages.ContainerEntryPage_remove_button,
				null, CPathEntryMessages.ContainerEntryPage_export_button };

		ContainersAdapter adapter = new ContainersAdapter();

		fContainersList = new TreeListDialogField<>(adapter, buttonLabels, new CPElementLabelProvider());
		fContainersList.setDialogFieldListener(adapter);
		fContainersList.setLabelText(CPathEntryMessages.ContainerEntryPage_libraries_label);

		fContainersList.enableButton(IDX_REMOVE, false);
		fContainersList.enableButton(IDX_EDIT, false);
		fContainersList.enableButton(IDX_EXPORT, false);
		fContainersList.setTreeExpansionLevel(2);

		fContainersList.setViewerComparator(new CPElementSorter());

	}

	public void init(ICProject jproject) {
		fCurrCProject = jproject;
		updateLibrariesList();
	}

	private void updateLibrariesList() {
		List<CPElement> cpelements = fCPathList.getElements();
		List<CPElement> libelements = new ArrayList<>(cpelements.size());

		int nElements = cpelements.size();
		for (int i = 0; i < nElements; i++) {
			CPElement cpe = cpelements.get(i);
			if (isEntryKind(cpe.getEntryKind())) {
				libelements.add(cpe);
			}
		}
		fContainersList.setElements(libelements);
	}

	// -------- ui creation

	@Override
	public void createControl(Composite parent) {
		PixelConverter converter = new PixelConverter(parent);

		Composite composite = new Composite(parent, SWT.NONE);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { fContainersList }, true);
		LayoutUtil.setHorizontalGrabbing(fContainersList.getTreeControl(null), true);

		int buttonBarWidth = converter.convertWidthInCharsToPixels(24);
		fContainersList.setButtonsMinWidth(buttonBarWidth);

		fContainersList.getTreeViewer().addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof CPElementGroup) {
					return ((CPElementGroup) element).getChildren().length != 0;
				}
				return true;
			}
		});

		setControl(composite);

		CUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(composite,
				ICHelpContextIds.PROJECT_PATHS_CONTAINERS);
	}

	private class ContainersAdapter implements IDialogFieldListener, ITreeListAdapter<CPElement> {

		private final Object[] EMPTY_ARR = new Object[0];

		// -------- IListAdapter --------
		@Override
		public void customButtonPressed(TreeListDialogField<CPElement> field, int index) {
			containerPageCustomButtonPressed(field, index);
		}

		@Override
		public void selectionChanged(TreeListDialogField<CPElement> field) {
			containerPageSelectionChanged(field);
		}

		@Override
		public void doubleClicked(TreeListDialogField<CPElement> field) {
			containerPageDoubleClicked(field);
		}

		@Override
		public void keyPressed(TreeListDialogField<CPElement> field, KeyEvent event) {
			containerPageKeyPressed(field, event);
		}

		@Override
		public Object[] getChildren(TreeListDialogField<CPElement> field, Object element) {
			if (element instanceof CPElement) {
				return ((CPElement) element).getChildren();
			} else if (element instanceof CPElementGroup) {
				return ((CPElementGroup) element).getChildren();
			}
			return EMPTY_ARR;
		}

		@Override
		public Object getParent(TreeListDialogField<CPElement> field, Object element) {
			if (element instanceof CPElementAttribute) {
				return ((CPElementAttribute) element).getParent();
			} else if (element instanceof CPElementGroup) {
				return ((CPElementGroup) element).getParent();
			}
			return null;
		}

		@Override
		public boolean hasChildren(TreeListDialogField<CPElement> field, Object element) {
			if (element instanceof CPElementGroup) {
				return true;
			} else if (element instanceof CPElement) {
				return ((CPElement) element).getChildren().length != 0;
			}
			return false;

		}

		// ---------- IDialogFieldListener --------

		@Override
		public void dialogFieldChanged(DialogField field) {
			containerPageDialogFieldChanged(field);
		}
	}

	void containerPageCustomButtonPressed(DialogField field, int index) {
		CPElement[] containers = null;
		switch (index) {
		case IDX_ADD:
			/* add container */
			containers = openContainerSelectionDialog(null);
			break;
		case IDX_EDIT:
			/* edit */
			editEntry();
			return;
		case IDX_REMOVE:
			/* remove */
			removeEntry();
			return;
		case IDX_EXPORT:
			/* export */
			exportEntry();
			return;
		}
		if (containers != null) {
			int nElementsChosen = containers.length;
			// remove duplicates
			List<CPElement> cplist = fContainersList.getElements();
			List<CPElement> elementsToAdd = new ArrayList<>(nElementsChosen);

			for (int i = 0; i < nElementsChosen; i++) {
				CPElement curr = containers[i];
				if (!cplist.contains(curr) && !elementsToAdd.contains(curr)) {
					elementsToAdd.add(curr);
					//					curr.setAttribute(CPElement.SOURCEATTACHMENT, BuildPathSupport.guessSourceAttachment(curr));
				}
			}

			fContainersList.addElements(elementsToAdd);
			if (index == IDX_ADD) {
				fContainersList.refresh();
			}
			fContainersList.postSetSelection(new StructuredSelection(containers));
		}
	}

	protected void containerPageDoubleClicked(TreeListDialogField<?> field) {
		List<?> selection = fContainersList.getSelectedElements();
		if (canEdit(selection)) {
			editEntry();
		}
	}

	protected void containerPageKeyPressed(TreeListDialogField<?> field, KeyEvent event) {
		if (field == fContainersList) {
			if (event.character == SWT.DEL && event.stateMask == 0) {
				List<?> selection = field.getSelectedElements();
				if (canRemove(selection)) {
					removeEntry();
				}
			}
		}
	}

	private boolean canRemove(List<?> selElements) {
		if (selElements.size() == 0) {
			return false;
		}
		for (int i = 0; i < selElements.size(); i++) {
			Object elem = selElements.get(i);
			if (elem instanceof CPElement) {
				CPElement curr = (CPElement) elem;
				if (curr.getParentContainer() != null) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	private void removeEntry() {
		List<?> selElements = fContainersList.getSelectedElements();
		fContainersList.removeElements(selElements);
	}

	private boolean canExport(List<?> selElements) {
		if (selElements.size() == 0) {
			return false;
		}
		for (int i = 0; i < selElements.size(); i++) {
			Object elem = selElements.get(i);
			if (elem instanceof CPElement) {
				CPElement curr = (CPElement) elem;
				if (curr.getParentContainer() != null) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	private void exportEntry() {
		List<?> selElements = fContainersList.getSelectedElements();
		if (selElements.size() == 0) {
			return;
		}
		for (int i = 0; i < selElements.size(); i++) {
			Object elem = selElements.get(i);
			if (elem instanceof CPElement) {
				((CPElement) elem).setExported(!((CPElement) elem).isExported()); // toggle export
			}
		}
		fContainersList.refresh();
	}

	/**
	 * Method editEntry.
	 */
	private void editEntry() {
		List<?> selElements = fContainersList.getSelectedElements();
		if (selElements.size() != 1) {
			return;
		}
		Object elem = selElements.get(0);
		if (fContainersList.getIndexOfElement(elem) != -1) {
			editElementEntry((CPElement) elem);
		} else if (elem instanceof CPElementAttribute) {
			editAttributeEntry((CPElementAttribute) elem);
		}
	}

	private void editAttributeEntry(CPElementAttribute elem) {
		//		String key = elem.getKey();
		//		if (key.equals(CPElement.SOURCEATTACHMENT)) {
		//			CPElement selElement = elem.getParent();
		//
		//			IPath containerPath = null;
		//			boolean applyChanges = false;
		//			Object parentContainer = selElement.getParentContainer();
		//			if (parentContainer instanceof CPElement) {
		//				containerPath = ((CPElement)parentContainer).getPath();
		//				applyChanges = true;
		//			}
		//			SourceAttachmentDialog dialog = new SourceAttachmentDialog(getShell(), (ILibraryEntry)selElement.getPathEntry(), containerPath,
		//					fCurrCProject, applyChanges);
		//			if (dialog.open() == Window.OK) {
		//				selElement.setAttribute(CPElement.SOURCEATTACHMENT, dialog.getSourceAttachmentPath());
		//				fContainersList.refresh();
		//				fCPathList.refresh(); // images
		//			}
		//		}
	}

	private void editElementEntry(CPElement elem) {
		CPElement[] res = null;

		res = openContainerSelectionDialog(elem);
		if (res != null && res.length > 0) {
			CPElement curr = res[0];
			curr.setExported(elem.isExported());
			fContainersList.replaceElement(elem, curr);
		}

	}

	void containerPageSelectionChanged(DialogField field) {
		List<?> selElements = fContainersList.getSelectedElements();
		fContainersList.enableButton(IDX_EDIT, canEdit(selElements));
		fContainersList.enableButton(IDX_REMOVE, canRemove(selElements));
		fContainersList.enableButton(IDX_EXPORT, canExport(selElements));
	}

	private boolean canEdit(List<?> selElements) {
		if (selElements.size() != 1) {
			return false;
		}
		Object elem = selElements.get(0);
		if (elem instanceof CPElement) {
			CPElement curr = (CPElement) elem;
			return !(curr.getResource() instanceof IFolder) && curr.getParentContainer() == null;
		}
		if (elem instanceof CPElementAttribute) {
			return true;
		}
		return false;
	}

	void containerPageDialogFieldChanged(DialogField field) {
		if (fCurrCProject != null) {
			// already initialized
			updateCPathList();
		}
	}

	private void updateCPathList() {
		List<CPElement> projelements = fContainersList.getElements();

		List<CPElement> cpelements = fCPathList.getElements();
		int nEntries = cpelements.size();
		// backwards, as entries will be deleted
		int lastRemovePos = nEntries;
		for (int i = nEntries - 1; i >= 0; i--) {
			CPElement cpe = cpelements.get(i);
			int kind = cpe.getEntryKind();
			if (isEntryKind(kind)) {
				if (!projelements.remove(cpe)) {
					cpelements.remove(i);
					lastRemovePos = i;
				}
			}
		}

		cpelements.addAll(lastRemovePos, projelements);

		if (lastRemovePos != nEntries || !projelements.isEmpty()) {
			fCPathList.setElements(cpelements);
		}
	}

	private CPElement[] openContainerSelectionDialog(CPElement existing) {
		IContainerEntry elem = null;
		String title;
		if (existing == null) {
			title = CPathEntryMessages.ContainerEntryPage_ContainerDialog_new_title;
		} else {
			title = CPathEntryMessages.ContainerEntryPage_ContainerDialog_edit_title;
			elem = (IContainerEntry) existing.getPathEntry();
		}
		CPathContainerWizard wizard = new CPathContainerWizard(elem, fCurrCProject, getRawClasspath());
		wizard.setWindowTitle(title);
		if (CPathContainerWizard.openWizard(getShell(), wizard) == Window.OK) {
			IPathEntry[] created = wizard.getContainers();
			if (created != null) {
				CPElement[] res = new CPElement[created.length];
				for (int i = 0; i < res.length; i++) {
					res[i] = new CPElement(fCurrCProject, IPathEntry.CDT_CONTAINER, created[i].getPath(), null);
				}
				return res;
			}
		}
		return null;
	}

	private IPathEntry[] getRawClasspath() {
		IPathEntry[] currEntries = new IPathEntry[fCPathList.getSize()];
		for (int i = 0; i < currEntries.length; i++) {
			CPElement curr = fCPathList.getElement(i);
			currEntries[i] = curr.getPathEntry();
		}
		return currEntries;
	}

	@Override
	public boolean isEntryKind(int kind) {
		return kind == IPathEntry.CDT_CONTAINER;
	}

	/*
	 * @see BuildPathBasePage#getSelection
	 */
	@Override
	public List<?> getSelection() {
		return fContainersList.getSelectedElements();
	}

	/*
	 * @see BuildPathBasePage#setSelection
	 */
	@Override
	public void setSelection(List<?> selElements) {
		fContainersList.selectElements(new StructuredSelection(selElements));
	}

	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
	}

	@Override
	public void performDefaults() {
	}
}
