/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.TypedElementSelectionValidator;
import org.eclipse.cdt.internal.ui.dialogs.TypedViewerFilter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.TreeListDialogField;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * DialogPage for C/C++ Project Paths page for 3.X projects.
 */
public class CPathIncludeSymbolEntryPage extends CPathIncludeSymbolEntryBasePage {
	private TreeListDialogField<CPElementGroup> fIncludeSymPathsList;
	private SelectionButtonDialogField fShowInheritedPaths;
	private ICProject fCurrCProject;
	private CPElementFilter fFilter;
	private IStatusChangeListener fContext;

	private final int IDX_ADD_FOLDER_FILE = 0;
	private final int IDX_ADD_SYMBOL = 2;
	private final int IDX_ADD_EXT_INCLUDE = 4;
	private final int IDX_ADD_WS_INCLUDE = 5;
	private final int IDX_ADD_CONTRIBUTED = 7;
	private final int IDX_EDIT = 9;
	private final int IDX_REMOVE = 10;
	private final int IDX_EXPORT = 12;
	private final int IDX_UP = 14;
	private final int IDX_DOWN = 15;

	private static final String[] buttonLabel = new String[] {

			CPathEntryMessages.IncludeSymbolEntryPage_addFolderFile, null,
			CPathEntryMessages.IncludeSymbolEntryPage_addUserSymbol, null,
			CPathEntryMessages.IncludeSymbolEntryPage_addExternalInclude,
			CPathEntryMessages.IncludeSymbolEntryPage_addFromWorkspace, null,
			CPathEntryMessages.IncludeSymbolEntryPage_addContributed, null,
			CPathEntryMessages.IncludeSymbolEntryPage_edit, CPathEntryMessages.IncludeSymbolEntryPage_remove, null,
			CPathEntryMessages.IncludeSymbolEntryPage_export, null, CPathEntryMessages.IncludeSymbolEntryPage_up,
			CPathEntryMessages.IncludeSymbolEntryPage_down };
	private CPElementGroup fTopGroup;

	private class IncludeSymbolAdapter implements IDialogFieldListener, ITreeListAdapter<CPElementGroup> {
		private final Object[] EMPTY_ARR = new Object[0];

		// -------- IListAdapter --------
		@Override
		public void customButtonPressed(TreeListDialogField<CPElementGroup> field, int index) {
			ListCustomButtonPressed(field, index);
		}

		@Override
		public void selectionChanged(TreeListDialogField<CPElementGroup> field) {
			listPageSelectionChanged(field);
		}

		@Override
		public void doubleClicked(TreeListDialogField<CPElementGroup> field) {
			ListPageDoubleClicked(field);
		}

		@Override
		public void keyPressed(TreeListDialogField<CPElementGroup> field, KeyEvent event) {
			ListPageKeyPressed(field, event);
		}

		@Override
		public Object[] getChildren(TreeListDialogField<CPElementGroup> field, Object element) {
			if (element instanceof CPElement) {
				return ((CPElement) element).getChildren();
			} else if (element instanceof CPElementGroup) {
				return ((CPElementGroup) element).getChildren();
			}
			return EMPTY_ARR;
		}

		@Override
		public Object getParent(TreeListDialogField<CPElementGroup> field, Object element) {
			if (element instanceof CPElementGroup) {
				return ((CPElementGroup) element).getParent();
			} else if (element instanceof CPElement) {
				return ((CPElement) element).getParent();
			}
			return null;
		}

		@Override
		public boolean hasChildren(TreeListDialogField<CPElementGroup> field, Object element) {
			if (element instanceof CPElementGroup) {
				return true;
			}
			if (element instanceof CPElement) {
				return ((CPElement) element).getChildren().length > 0;
			}
			return false;
		}

		// ---------- IDialogFieldListener --------

		@Override
		public void dialogFieldChanged(DialogField field) {
			listPageDialogFieldChanged(field);
		}

	}

	public CPathIncludeSymbolEntryPage(IStatusChangeListener context) {
		super(CPathEntryMessages.IncludeSymbolEntryPage_title);
		fContext = context;
		IncludeSymbolAdapter adapter = new IncludeSymbolAdapter();
		fIncludeSymPathsList = new TreeListDialogField<CPElementGroup>(adapter, buttonLabel,
				new CPElementLabelProvider(true, false)) {

			@Override
			protected int getTreeStyle() {
				return super.getTreeStyle() & ~SWT.MULTI;
			}
		};
		fIncludeSymPathsList.setLabelText(CPathEntryMessages.IncludeSymbolEntryPage_label);
		fIncludeSymPathsList.enableButton(IDX_REMOVE, false);
		fIncludeSymPathsList.enableButton(IDX_EDIT, false);
		fIncludeSymPathsList.enableButton(IDX_ADD_CONTRIBUTED, true);
		fIncludeSymPathsList.enableButton(IDX_ADD_EXT_INCLUDE, true);
		fIncludeSymPathsList.enableButton(IDX_ADD_WS_INCLUDE, true);
		fIncludeSymPathsList.enableButton(IDX_ADD_SYMBOL, true);
		fIncludeSymPathsList.enableButton(IDX_EXPORT, false);
		fIncludeSymPathsList.enableButton(IDX_UP, false);
		fIncludeSymPathsList.enableButton(IDX_DOWN, false);
		fIncludeSymPathsList.setTreeExpansionLevel(2);
		fShowInheritedPaths = new SelectionButtonDialogField(SWT.CHECK);
		fShowInheritedPaths.setSelection(true);
		fShowInheritedPaths.setLabelText(CPathEntryMessages.IncludeSymbolsEntryPage_show_inherited_check);
		fShowInheritedPaths.setDialogFieldListener(adapter);

		fFilter = new CPElementFilter(
				new int[] { -1, IPathEntry.CDT_INCLUDE, IPathEntry.CDT_MACRO, IPathEntry.CDT_CONTAINER }, false, true);
	}

	@Override
	public void createControl(Composite parent) {
		PixelConverter converter = new PixelConverter(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { fIncludeSymPathsList, fShowInheritedPaths }, true);
		LayoutUtil.setHorizontalGrabbing(fIncludeSymPathsList.getTreeControl(null), true);

		int buttonBarWidth = converter.convertWidthInCharsToPixels(24);
		fIncludeSymPathsList.setButtonsMinWidth(buttonBarWidth);
		setControl(composite);
		fIncludeSymPathsList.getTreeViewer().addFilter(fFilter);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ICHelpContextIds.PROJECT_INCLUDE_PATHS_SYMBOLS);
	}

	@Override
	public Image getImage() {
		return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_INCLUDES_CONTAINER);
	}

	@Override
	public void init(ICElement cElement, List<CPElement> cPaths) {
		fCurrCProject = cElement.getCProject();
		List<CPElementGroup> elements = createGroups(cElement, cPaths);
		fIncludeSymPathsList.setElements(elements);
		updateStatus();
	}

	private void updateStatus() {
		CPElement entryError = null;
		int nErrorEntries = 0;
		IStatus status = Status.OK_STATUS;
		List<CPElement> elements = getCPaths();
		for (int i = elements.size() - 1; i >= 0; i--) {
			CPElement currElement = elements.get(i);
			if (currElement.getStatus().getSeverity() != IStatus.OK) {
				nErrorEntries++;
				if (entryError == null) {
					entryError = currElement;
				}
			}
		}

		if (nErrorEntries > 0) {
			if (nErrorEntries == 1 && entryError != null) {
				status = entryError.getStatus();
			} else {
				status = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, -1,
						NLS.bind(CPathEntryMessages.CPElement_status_multiplePathErrors, String.valueOf(nErrorEntries)),
						null);
			}
		}
		fContext.statusChanged(status);
	}

	private void updateStatus(List<?> selected) {
		if (selected.size() != 1) {
			updateStatus();
			return;
		}
		CPElement element = null;
		if (selected.get(0) instanceof CPElement) {
			element = (CPElement) selected.get(0);
		} else if (selected.get(0) instanceof CPElementAttribute) {
			element = ((CPElementAttribute) selected.get(0)).getParent();
		}
		if (element != null && element.getStatus().getSeverity() != IStatus.OK) {
			fContext.statusChanged(element.getStatus());
		} else {
			updateStatus();
			return;
		}
	}

	private List<CPElementGroup> createGroups(ICElement element, List<?> cPaths) {
		// create resource groups
		List<CPElementGroup> resourceGroups = new ArrayList<>(5);
		fTopGroup = new CPElementGroup(element.getResource());
		resourceGroups.add(fTopGroup);
		// add containers first so that they appear at top of list
		for (int i = 0; i < cPaths.size(); i++) {
			CPElement cpelement = (CPElement) cPaths.get(i);
			switch (cpelement.getEntryKind()) {
			case IPathEntry.CDT_CONTAINER:
				fTopGroup.addChild(cpelement);
				break;
			}
		}
		for (int i = 0; i < cPaths.size(); i++) {
			CPElement cpelement = (CPElement) cPaths.get(i);
			switch (cpelement.getEntryKind()) {
			case IPathEntry.CDT_INCLUDE:
			case IPathEntry.CDT_MACRO:
				CPElementGroup resGroup = new CPElementGroup(cpelement.getResource());
				int ndx = resourceGroups.indexOf(resGroup);
				if (ndx == -1) {
					resourceGroups.add(resGroup);
				} else {
					resGroup = resourceGroups.get(ndx);
				}
				resGroup.addChild(cpelement);
			}
		}

		// place each path in its appropriate inherited group (or not if
		// excluded)
		for (int i = 0; i < cPaths.size(); i++) {
			CPElement cpelement = (CPElement) cPaths.get(i);
			switch (cpelement.getEntryKind()) {
			case IPathEntry.CDT_INCLUDE:
			case IPathEntry.CDT_MACRO:
				addPathToResourceGroups(cpelement, null, resourceGroups);
			}
		}
		return resourceGroups;
	}

	private void addPathToResourceGroup(CPElement element, CPElementGroup parent, CPElementGroup group) {
		IPath resPath = element.getPath();
		IPath[] exclusions = (IPath[]) element.getAttribute(CPElement.EXCLUSION);
		if ((group != parent || !group.getResource().equals(element.getResource()))
				&& resPath.isPrefixOf(group.getPath())
				&& (resPath.equals(group.getPath()) || !CoreModelUtil.isExcludedPath(
						group.getResource().getFullPath().removeFirstSegments(resPath.segmentCount()), exclusions))) {
			if (parent != null) { // try to insert at proper place in group...
				int insertHere = -1;
				int ndx = parent.indexof(element);
				if (ndx != -1) {
					CPElement[] children = parent.getChildren(element.getEntryKind());
					for (int i = ndx; i < children.length; i++) {
						insertHere = group.indexof(new CPElement(children[i], group.getPath(), group.getResource()));
						if (insertHere != -1) {
							group.addChild(new CPElement(element, group.getPath(), group.getResource()), insertHere);
							break;
						}
					}
				}
				if (insertHere == -1) {
					group.addChild(new CPElement(element, group.getPath(), group.getResource()));
				}
			} else {
				group.addChild(new CPElement(element, group.getPath(), group.getResource()));
			}
		}
	}

	private void addPathToResourceGroups(CPElement element, CPElementGroup parent, List<CPElementGroup> groups) {
		if (parent != null) {
			parent.addChild(element);
		}
		for (int i = 0; i < groups.size(); i++) {
			CPElementGroup group = groups.get(i);
			addPathToResourceGroup(element, parent, group);
		}
	}

	private void updatePathOnResourceGroups(CPElement element, List<CPElementGroup> groups) {
		CPElementGroup parent = element.getParent();
		IPath resPath = element.getPath();
		IPath[] exclusions = (IPath[]) element.getAttribute(CPElement.EXCLUSION);
		for (int i = 0; i < groups.size(); i++) {
			CPElementGroup group = groups.get(i);
			if (group != parent) {
				boolean found = false;
				CPElement[] elements = group.getChildren(element.getEntryKind());
				for (CPElement element2 : elements) {
					if (element2.getInherited() == element) {
						found = true;
						if (!CoreModelUtil.isExcludedPath(
								group.getResource().getFullPath().removeFirstSegments(resPath.segmentCount()),
								exclusions)) {
							group.replaceChild(element2, new CPElement(element, group.getPath(), group.getResource()));
						} else {
							group.removeChild(element2);
						}
						break;
					}
				}
				if (!found) {
					addPathToResourceGroup(element, parent, group);
				}
			}
		}
	}

	private CPElement removePathFromResourceGroups(CPElement element, List<CPElementGroup> groups) {
		CPElement Inherited = element.getInherited();
		CPElementGroup resGroup = element.getParent();
		resGroup.removeChild(element);
		if (Inherited != null) { // applied exclusion to orig.
			IPath exclude = element.getPath().removeFirstSegments(Inherited.getPath().segmentCount());
			IPath[] exclusions = (IPath[]) Inherited.getAttribute(CPElement.EXCLUSION);
			IPath[] newExlusions = new IPath[exclusions.length + 1];
			System.arraycopy(exclusions, 0, newExlusions, 0, exclusions.length);
			newExlusions[exclusions.length] = exclude;
			Inherited.setAttribute(CPElement.EXCLUSION, newExlusions);
			return null;
		}
		// remove all inherited
		for (int i = 0; i < groups.size(); i++) {
			CPElementGroup group = groups.get(i);
			CPElement elements[] = group.getChildren(element.getEntryKind());
			for (CPElement element2 : elements) {
				if (element2.getInherited() == element) {
					group.removeChild(element2);
					break;
				}
			}
		}
		return element;
	}

	private boolean canAddPath(List<?> selected) {
		CPElementGroup group = getSelectedGroup();
		if (group != null) {
			return group.getEntryKind() == -1; // resource group
		}
		return false;
	}

	private boolean canRemove(List<?> selected) {
		if (selected.size() != 1) {
			return false;
		}
		Object elem = selected.get(0);
		if (elem instanceof CPElement) {
			CPElement element = (CPElement) elem;
			if (element.getParentContainer() == null) {
				return element.getEntryKind() == IPathEntry.CDT_INCLUDE
						|| element.getEntryKind() == IPathEntry.CDT_MACRO;
			}
		} else if (elem instanceof CPElementAttribute) {
			CPElementAttribute attrib = (CPElementAttribute) elem;
			if (attrib.getKey().equals(CPElement.EXCLUSION)) {
				if (((IPath[]) attrib.getValue()).length > 0) {
					return true;
				}
			}
		}
		return false;
	}

	private void removeEntry() {
		List<?> selected = getSelection();
		Object elem = selected.get(0);
		if (elem instanceof CPElement) {
			CPElement element = (CPElement) elem;
			CPElementGroup parent = element.getParent();
			if (removePathFromResourceGroups(element, fIncludeSymPathsList.getElements()) == null) {
				updatePathOnResourceGroups(element.getInherited(), fIncludeSymPathsList.getElements());
			}
			fIncludeSymPathsList.refresh();
			fIncludeSymPathsList.selectElements(new StructuredSelection(parent));
		} else if (elem instanceof CPElementAttribute) {
			CPElementAttribute attrib = (CPElementAttribute) elem;
			String key = attrib.getKey();
			Object value = key.equals(CPElement.EXCLUSION) ? new Path[0] : null;
			attrib.getParent().setAttribute(key, value);
			updatePathOnResourceGroups(attrib.getParent(), fIncludeSymPathsList.getElements());
			fIncludeSymPathsList.refresh();
		}
		updateStatus();
	}

	private boolean canEdit(List<?> selected) {
		if (selected.size() != 1) {
			return false;
		}
		Object elem = selected.get(0);
		if (elem instanceof CPElement) {
			CPElement element = (CPElement) selected.get(0);
			if (element.getParentContainer() == null && element.getInherited() == null) {
				IPath path = (IPath) element.getAttribute(CPElement.BASE_REF);
				if (path != null && !path.equals(Path.EMPTY)) {
					return false;
				}
				return element.getEntryKind() == IPathEntry.CDT_INCLUDE
						|| element.getEntryKind() == IPathEntry.CDT_MACRO
						|| element.getEntryKind() == IPathEntry.CDT_CONTAINER;
			}
		}
		if (elem instanceof CPElementAttribute) {
			return true;
		}
		return false;
	}

	private void editEntry() {
		List<?> selElements = fIncludeSymPathsList.getSelectedElements();
		if (selElements.size() != 1) {
			return;
		}
		Object element = selElements.get(0);

		if (element instanceof CPElement) {
			editElementEntry((CPElement) element);
		} else if (element instanceof CPElementAttribute) {
			editAttributeEntry((CPElementAttribute) element);
		}
	}

	private void editElementEntry(CPElement element) {
		IPath path = (IPath) element.getAttribute(CPElement.BASE_REF);
		if (path != null && !path.equals(Path.EMPTY)) {
			return;
		} else if (element.getEntryKind() == IPathEntry.CDT_MACRO) {
			addSymbol(element);
		} else if (element.getEntryKind() == IPathEntry.CDT_INCLUDE) {
			path = (IPath) element.getAttribute(CPElement.BASE);
			if (path != null && !path.equals(Path.EMPTY)) {
				CPElement[] includes = openWorkspacePathEntryDialog(null);
				if (includes != null && includes.length > 0) {
					includes[0].setExported(element.isExported());

				}
			} else {
				addInclude(element);
			}
		} else if (element.getEntryKind() == IPathEntry.CDT_CONTAINER) {
			CPElement[] res = null;

			res = openContainerSelectionDialog(element);
			if (res != null && res.length > 0) {
				CPElement curr = res[0];
				curr.setExported(element.isExported());
				fTopGroup.replaceChild(element, curr);
				fIncludeSymPathsList.refresh();
			}
		}
	}

	private void editAttributeEntry(CPElementAttribute elem) {
		String key = elem.getKey();
		if (key.equals(CPElement.EXCLUSION)) {
			CPElement selElement = elem.getParent();
			ExclusionPatternDialog dialog = new ExclusionPatternDialog(getShell(), selElement);
			if (dialog.open() == Window.OK) {
				selElement.setAttribute(CPElement.EXCLUSION, dialog.getExclusionPattern());
				updatePathOnResourceGroups(selElement, fIncludeSymPathsList.getElements());
				fIncludeSymPathsList.refresh();
				updateStatus();
			}
		}
	}

	private void exportEntry() {
		CPElement element = (CPElement) getSelection().get(0);
		element.setExported(!element.isExported()); // toggle
		fIncludeSymPathsList.refresh(element);
	}

	private boolean canExport(List<?> selected) {
		if (selected.size() != 1) {
			return false;
		}
		Object elem = selected.get(0);
		if (elem instanceof CPElement) {
			CPElement element = (CPElement) selected.get(0);
			if (element.getParentContainer() == null && element.getInherited() == null) {
				IPath base_ref = (IPath) element.getAttribute(CPElement.BASE_REF);
				if (base_ref != null && !base_ref.equals(Path.EMPTY))
					return false;
				return element.getEntryKind() == IPathEntry.CDT_INCLUDE
						|| element.getEntryKind() == IPathEntry.CDT_MACRO;
			}
		}
		return false;
	}

	private boolean canMove(List<?> selected) {
		if (selected.size() == 0) {
			return false;
		}
		for (int i = 0; i < selected.size(); i++) {
			Object element = selected.get(i);
			if (!(element instanceof CPElement))
				return false;
			CPElement elem = (CPElement) element;
			if (elem.getEntryKind() != IPathEntry.CDT_INCLUDE && elem.getEntryKind() != IPathEntry.CDT_MACRO) {
				return false;
			}
			if (elem.getParentContainer() != null || elem.getInherited() != null) {
				return false;
			}
		}
		return true;
	}

	private boolean canMoveUp(List<?> selected) {
		if (!canMove(selected)) {
			return false;
		}
		CPElement first = (CPElement) selected.get(0);
		CPElementGroup parent = first.getParent();
		CPElement children[] = parent.getChildren(first.getEntryKind());
		int indx = Arrays.asList(children).indexOf(first);
		if (indx <= 0) {
			return false;
		}
		return true;
	}

	private boolean canMoveDown(List<?> selected) {
		if (!canMove(selected)) {
			return false;
		}
		CPElement last = (CPElement) selected.get(selected.size() - 1);
		CPElementGroup parent = last.getParent();
		CPElement children[] = parent.getChildren(last.getEntryKind());
		int indx = Arrays.asList(children).indexOf(last);
		if (indx >= children.length - 1 || children[indx + 1].getInherited() != null) {
			return false;
		}
		return true;
	}

	private boolean moveUp(CPElement element) {
		boolean rc = false;
		int kind = element.getEntryKind();
		for (CPElementGroup group : fIncludeSymPathsList.getElements()) {
			CPElement[] children = group.getChildren(kind);
			for (int k = 0; k < children.length; ++k) {
				CPElement child = children[k];
				if (element.equals(child) || (child.getInherited() != null && child.getInherited().equals(element))) {
					if (child.getInherited() != null && k > 0 && children[k - 1].getInherited() == null) {
						break;
					}
					int prevIndex = k - 1;
					if (prevIndex >= 0) {
						// swap the two
						children[k] = children[prevIndex];
						children[prevIndex] = child;
						rc = true;
						break;
					}
				}
			}
			group.setChildren(children);
		}
		fIncludeSymPathsList.refresh();
		fIncludeSymPathsList.selectElements(new StructuredSelection(element));
		fIncludeSymPathsList.setFocus();
		return rc;
	}

	/**
	 *
	 */
	private boolean moveDown(CPElement element) {
		boolean rc = false;
		int kind = element.getEntryKind();
		for (CPElementGroup group : fIncludeSymPathsList.getElements()) {
			CPElement[] children = group.getChildren(kind);
			for (int k = children.length - 1; k >= 0; --k) {
				CPElement child = children[k];
				if (element.equals(child) || (child.getInherited() != null && child.getInherited().equals(element))) {
					int prevIndex = k + 1;
					if (prevIndex < children.length) {
						// swap the two
						children[k] = children[prevIndex];
						children[prevIndex] = child;
						rc = true;
						break;
					}
				}
			}
			group.setChildren(children);
		}
		fIncludeSymPathsList.refresh();
		fIncludeSymPathsList.selectElements(new StructuredSelection(element));
		fIncludeSymPathsList.setFocus();
		return rc;
	}

	private CPElementGroup getSelectedGroup() {
		List<?> selected = fIncludeSymPathsList.getSelectedElements();
		if (!selected.isEmpty()) {
			Object item = selected.get(0);
			if (item instanceof CPElement) {
				item = ((CPElement) item).getParent();
			}
			if (item instanceof CPElementGroup) {
				return (CPElementGroup) item;
			}
		}
		return fTopGroup;
	}

	protected void listPageDialogFieldChanged(DialogField field) {
		if (field == fShowInheritedPaths) {
			boolean showInherited = fShowInheritedPaths.isSelected();
			if (fFilter != null) {
				fIncludeSymPathsList.getTreeViewer().removeFilter(fFilter);
			}
			fFilter = new CPElementFilter(
					new int[] { -1, IPathEntry.CDT_INCLUDE, IPathEntry.CDT_MACRO, IPathEntry.CDT_CONTAINER }, false,
					showInherited);
			fIncludeSymPathsList.getTreeViewer().addFilter(fFilter);
			fIncludeSymPathsList.refresh();
		}
		updateStatus();
	}

	protected void listPageSelectionChanged(TreeListDialogField<?> field) {
		List<?> selected = field.getSelectedElements();
		field.enableButton(IDX_REMOVE, canRemove(selected));
		field.enableButton(IDX_EDIT, canEdit(selected));
		field.enableButton(IDX_ADD_CONTRIBUTED, canAddPath(selected));
		field.enableButton(IDX_ADD_EXT_INCLUDE, canAddPath(selected));
		field.enableButton(IDX_ADD_WS_INCLUDE, canAddPath(selected));
		field.enableButton(IDX_ADD_SYMBOL, canAddPath(selected));
		field.enableButton(IDX_EXPORT, canExport(selected));
		field.enableButton(IDX_DOWN, canMoveDown(selected));
		field.enableButton(IDX_UP, canMoveUp(selected));
		updateStatus(selected);
	}

	protected void ListCustomButtonPressed(TreeListDialogField<?> field, int index) {
		switch (index) {
		case IDX_ADD_FOLDER_FILE:
			addNewPathResource();
			break;
		case IDX_ADD_SYMBOL:
			addSymbol(null);
			break;
		case IDX_ADD_EXT_INCLUDE:
			addInclude(null);
			break;
		case IDX_ADD_WS_INCLUDE:
			addFromWorkspace();
			break;
		case IDX_ADD_CONTRIBUTED:
			addContributed();
			break;
		case IDX_EDIT:
			if (canEdit(field.getSelectedElements())) {
				editEntry();
			}
			break;
		case IDX_REMOVE:
			if (canRemove(field.getSelectedElements())) {
				removeEntry();
			}
			break;
		case IDX_DOWN:
			if (canMoveDown(field.getSelectedElements())) {
				moveDown((CPElement) field.getSelectedElements().get(0));
			}
			break;
		case IDX_UP:
			if (canMoveUp(field.getSelectedElements())) {
				moveUp((CPElement) field.getSelectedElements().get(0));
			}
			break;
		case IDX_EXPORT:
			if (canExport(field.getSelectedElements())) {
				exportEntry();
			}

		}
	}

	protected void ListPageDoubleClicked(TreeListDialogField<?> field) {
		if (canEdit(fIncludeSymPathsList.getSelectedElements())) {
			editEntry();
		}
	}

	protected void ListPageKeyPressed(TreeListDialogField<?> field, KeyEvent event) {
		if (field == fIncludeSymPathsList) {
			if (event.character == SWT.DEL && event.stateMask == 0) {
				List<?> selection = field.getSelectedElements();
				if (canEdit(selection)) {
					removeEntry();
				}
			}
		}
	}

	protected IPathEntry[] getRawPathEntries() {
		List<CPElement> paths = getCPaths();
		IPathEntry[] currEntries = new IPathEntry[paths.size()];
		for (int i = 0; i < currEntries.length; i++) {
			CPElement curr = paths.get(i);
			currEntries[i] = curr.getPathEntry();
		}
		return currEntries;
	}

	protected void addNewPathResource() {
		Class<?>[] acceptedClasses = new Class[] { ICProject.class, ICContainer.class, ITranslationUnit.class };
		TypedElementSelectionValidator validator = new TypedElementSelectionValidator(acceptedClasses, false);
		ViewerFilter filter = new TypedViewerFilter(acceptedClasses);

		String title = CPathEntryMessages.IncludeSymbolEntryPage_newResource_title;
		String message = CPathEntryMessages.IncludeSymbolEntryPage_newResource_description;

		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(),
				new CElementContentProvider());
		dialog.setValidator(validator);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.addFilter(filter);
		dialog.setInput(fCurrCProject);
		dialog.setInitialSelection(fCurrCProject);

		if (dialog.open() == Window.OK) {
			Object[] elements = dialog.getResult();
			IResource resource;
			if (elements[0] instanceof IResource) {
				resource = (IResource) elements[0];
			} else {
				resource = ((ICElement) elements[0]).getResource();
			}
			CPElementGroup newGroup = new CPElementGroup(resource);
			if (!fIncludeSymPathsList.getElements().contains(newGroup)) {
				List<CPElementGroup> groups = fIncludeSymPathsList.getElements();
				for (int i = 0; i < groups.size(); i++) {
					CPElementGroup group = groups.get(i);
					if (group.getPath().isPrefixOf(newGroup.getPath())) {
						CPElement[] cpelements = group.getChildren();
						for (CPElement cpelement : cpelements) {
							if (cpelement.getInherited() == null) {
								switch (cpelement.getEntryKind()) {
								case IPathEntry.CDT_INCLUDE:
								case IPathEntry.CDT_MACRO:
									addPathToResourceGroup(cpelement, null, newGroup);
								}
							}
						}
					}
				}
				fIncludeSymPathsList.addElement(newGroup);
			}
			fIncludeSymPathsList.selectElements(new StructuredSelection(newGroup));
			fIncludeSymPathsList.expandElement(newGroup, 1);
		}
	}

	protected void addSymbol(CPElement existing) {
		// Popup an entry dialog
		InputDialog dialog;
		if (existing == null) {
			dialog = new InputDialog(getShell(), CPathEntryMessages.IncludeSymbolEntryPage_addSymbol_title,
					CPathEntryMessages.IncludeSymbolEntryPage_addSymbol_message, "", //$NON-NLS-1$
					null);
		} else {
			StringBuilder initialValue = new StringBuilder();
			initialValue.append((String) existing.getAttribute(CPElement.MACRO_NAME));
			initialValue.append('=');
			initialValue.append((String) existing.getAttribute(CPElement.MACRO_VALUE));
			dialog = new InputDialog(getShell(), CPathEntryMessages.IncludeSymbolEntryPage_editSymbol_title,
					CPathEntryMessages.IncludeSymbolEntryPage_editSymbol_message, initialValue.toString(), null);
		}

		String symbol = null;
		if (dialog.open() == Window.OK) {
			symbol = dialog.getValue();
			if (symbol != null && symbol.length() > 0) {
				CPElementGroup group = getSelectedGroup();
				CPElement newPath = new CPElement(fCurrCProject, IPathEntry.CDT_MACRO,
						group.getResource().getFullPath(), group.getResource());
				String name, value = ""; //$NON-NLS-1$
				int index = symbol.indexOf("="); //$NON-NLS-1$
				if (index != -1) {
					name = symbol.substring(0, index).trim();
					value = symbol.substring(index + 1).trim();
				} else {
					name = symbol.trim();
				}
				if (existing != null) {
					existing.setAttribute(CPElement.MACRO_NAME, name);
					existing.setAttribute(CPElement.MACRO_VALUE, value);
					updatePathOnResourceGroups(existing, fIncludeSymPathsList.getElements());
					fIncludeSymPathsList.refresh();
				} else {
					newPath.setAttribute(CPElement.MACRO_NAME, name);
					newPath.setAttribute(CPElement.MACRO_VALUE, value);
					if (!group.contains(newPath)) {
						addPathToResourceGroups(newPath, group, fIncludeSymPathsList.getElements());
						fIncludeSymPathsList.refresh();
						fIncludeSymPathsList.selectElements(new StructuredSelection(newPath));
					}
					updateStatus();
				}
			}
		}
	}

	protected void addInclude(CPElement existing) {
		InputDialog dialog;
		if (existing == null) {
			dialog = new SelectPathInputDialog(getShell(), CPathEntryMessages.IncludeSymbolEntryPage_addExternal_title,
					CPathEntryMessages.IncludeSymbolEntryPage_addExternal_message, null, null);
		} else {
			dialog = new SelectPathInputDialog(getShell(), CPathEntryMessages.IncludeSymbolEntryPage_editExternal_title,
					CPathEntryMessages.IncludeSymbolEntryPage_editExternal_message,
					((IPath) existing.getAttribute(CPElement.INCLUDE)).toOSString(), null);
		}
		String newItem = null;
		if (dialog.open() == Window.OK) {
			newItem = dialog.getValue();
			if (newItem != null && !newItem.isEmpty()) {
				if (existing == null) {
					CPElementGroup group = getSelectedGroup();
					CPElement newPath = new CPElement(fCurrCProject, IPathEntry.CDT_INCLUDE,
							group.getResource().getFullPath(), group.getResource());
					newPath.setAttribute(CPElement.INCLUDE, new Path(newItem));
					if (!group.contains(newPath)) {
						addPathToResourceGroups(newPath, group, fIncludeSymPathsList.getElements());
						fIncludeSymPathsList.refresh();
						fIncludeSymPathsList.selectElements(new StructuredSelection(newPath));
					}
				} else {
					existing.setAttribute(CPElement.INCLUDE, new Path(newItem));
					updatePathOnResourceGroups(existing, fIncludeSymPathsList.getElements());
					fIncludeSymPathsList.refresh();
				}
				updateStatus();
			}
		}
	}

	protected void addFromWorkspace() {
		CPElement[] includes = openWorkspacePathEntryDialog(null);
		if (includes != null && includes.length > 0) {
			int nElementsChosen = includes.length;
			CPElementGroup group = getSelectedGroup();
			for (int i = 0; i < nElementsChosen; i++) {
				CPElement curr = includes[i];
				if (!group.contains(curr)) {
					addPathToResourceGroups(curr, group, fIncludeSymPathsList.getElements());
					fIncludeSymPathsList.refresh();
					fIncludeSymPathsList.selectElements(new StructuredSelection(curr));
					updateStatus();
				}
			}
		}

	}

	protected void addContributed() {
		CPElement[] includes = openContainerSelectionDialog(null);
		if (includes != null && includes.length > 0) {
			int nElementsChosen = includes.length;
			// remove duplicates
			CPElementGroup group = getSelectedGroup();
			for (int i = 0; i < nElementsChosen; i++) {
				CPElement curr = includes[i];
				if (!group.contains(curr)) {
					addPathToResourceGroups(curr, getSelectedGroup(), fIncludeSymPathsList.getElements());
					fIncludeSymPathsList.refresh();
					fIncludeSymPathsList.selectElements(new StructuredSelection(curr));
					updateStatus();
				}
			}
		}
	}

	protected CPElement[] openWorkspacePathEntryDialog(CPElement existing) {
		Class<?>[] acceptedClasses = new Class[] { ICProject.class, IProject.class, IContainer.class,
				ICContainer.class };
		TypedElementSelectionValidator validator = new TypedElementSelectionValidator(acceptedClasses,
				existing == null);
		ViewerFilter filter = new TypedViewerFilter(acceptedClasses);

		String title = (existing == null) ? CPathEntryMessages.IncludeSymbolEntryPage_fromWorkspaceDialog_new_title
				: CPathEntryMessages.IncludeSymbolEntryPage_fromWorkspaceDialog_edit_title;
		String message = (existing == null)
				? CPathEntryMessages.IncludeSymbolEntryPage_fromWorkspaceDialog_new_description
				: CPathEntryMessages.IncludeSymbolEntryPage_fromWorkspaceDialog_edit_description;

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
				CPElementGroup group = getSelectedGroup();
				res[i] = new CPElement(fCurrCProject, IPathEntry.CDT_INCLUDE, group.getResource().getFullPath(),
						group.getResource());
				res[i].setAttribute(CPElement.BASE, project.getFullPath().makeRelative());
				res[i].setAttribute(CPElement.INCLUDE, includePath);
			}
			return res;
		}
		return null;
	}

	protected CPElement[] openContainerSelectionDialog(CPElement existing) {
		IContainerEntry elem = null;
		String title;
		if (existing == null) {
			title = CPathEntryMessages.IncludeSymbolEntryPage_ContainerDialog_new_title;
		} else {
			title = CPathEntryMessages.IncludeSymbolEntryPage_ContainerDialog_edit_title;
			elem = (IContainerEntry) existing.getPathEntry();
		}
		CPathContainerWizard wizard = new CPathContainerWizard(elem, null, fCurrCProject, getRawPathEntries(),
				new int[] { IPathEntry.CDT_INCLUDE, IPathEntry.CDT_MACRO });
		wizard.setWindowTitle(title);
		if (CPathContainerWizard.openWizard(getShell(), wizard) == Window.OK) {
			IPathEntry parent = wizard.getEntriesParent();
			IPathEntry[] elements = wizard.getEntries();

			if (elements != null) {
				CPElement[] res = new CPElement[elements.length];
				CPElementGroup group = getSelectedGroup();
				for (int i = 0; i < res.length; i++) {
					if (elements[i].getEntryKind() == IPathEntry.CDT_INCLUDE) {
						res[i] = new CPElement(fCurrCProject, IPathEntry.CDT_INCLUDE, group.getResource().getFullPath(),
								group.getResource());
						res[i].setAttribute(CPElement.INCLUDE, ((IIncludeEntry) elements[i]).getIncludePath());
						res[i].setAttribute(CPElement.BASE_REF, parent.getPath());
					} else if (elements[i].getEntryKind() == IPathEntry.CDT_MACRO) {
						res[i] = new CPElement(fCurrCProject, IPathEntry.CDT_MACRO, group.getResource().getFullPath(),
								group.getResource());
						res[i].setAttribute(CPElement.MACRO_NAME, ((IMacroEntry) elements[i]).getMacroName());
						res[i].setAttribute(CPElement.BASE_REF, parent.getPath());
					}
				}
				return res;
			}
			return new CPElement[] { CPElement.createFromExisting(parent, fCurrCProject) };
		}
		return null;
	}

	private class SelectPathInputDialog extends InputDialog {

		public SelectPathInputDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue,
				IInputValidator validator) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			Button browse = createButton(parent, 3, CPathEntryMessages.IncludeSymbolEntryPage_addExternal_button_browse,
					false);
			browse.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent ev) {
					DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
					dialog.setText(CPathEntryMessages.IncludeSymbolEntryPage_browseForFolder);
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

	@Override
	public List<?> getSelection() {
		return fIncludeSymPathsList.getSelectedElements();
	}

	@Override
	public void setSelection(List<?> selElements) {
		fIncludeSymPathsList.selectElements(new StructuredSelection(selElements));
	}

	@Override
	public boolean isEntryKind(int kind) {
		return kind == IPathEntry.CDT_INCLUDE || kind == IPathEntry.CDT_MACRO;
	}

	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
	}

	@Override
	public void performDefaults() {
	}

	@Override
	public List<CPElement> getCPaths() {
		List<CPElement> cPaths = new ArrayList<>();
		List<CPElementGroup> groups = fIncludeSymPathsList.getElements();
		for (int i = 0; i < groups.size(); i++) {
			CPElementGroup group = groups.get(i);
			CPElement[] elements = group.getChildren();
			for (CPElement element : elements) {
				if (element.getInherited() == null) {
					cPaths.add(element);
				}
			}
		}
		return cPaths;
	}
}
