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
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.internal.ui.util.PixelConverter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.TreeListDialogField;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public abstract class ExtendedCPathBasePage extends CPathBasePage {

	ListDialogField fPathList;
	TreeListDialogField fSrcList;
	List fCPathList;

	private static final int IDX_ADD = 0;
	private static final int IDX_ADD_WORKSPACE = 1;
	private static final int IDX_ADD_CONTRIBUTED = 2;
	private static final int IDX_REMOVE = 4;

	private class IncludeListAdapter implements IListAdapter, IDialogFieldListener {

		public void dialogFieldChanged(DialogField field) {
		}

		public void customButtonPressed(ListDialogField field, int index) {
			switch (index) {
				case IDX_ADD :
					addPath();
					break;
				case IDX_ADD_WORKSPACE :
					addFromWorkspace();
					break;
				case IDX_ADD_CONTRIBUTED :
					addContributed();
					break;
				case IDX_REMOVE :
					if (canRemove(field.getSelectedElements())) {
						removePath((CPListElement) field.getSelectedElements().get(0));
					}
					break;
			}
		}

		public void selectionChanged(ListDialogField field) {
			List selected = fPathList.getSelectedElements();
			fPathList.enableButton(IDX_REMOVE, canRemove(selected));
		}

		public void doubleClicked(ListDialogField field) {
		}
	}

	private class ModifiedCPListLabelProvider extends CPListLabelProvider implements IColorProvider {

		private final Color inDirect = new Color(Display.getDefault(), new RGB(170, 170, 170));

		public Color getBackground(Object element) {
			return null;
		}

		public Color getForeground(Object element) {
			IPath resPath = ((CPListElement) element).getPath();
			List sel = getSelection();
			if (!sel.isEmpty()) {
				if (sel.get(0) instanceof ICElement) {
					ICElement celem = (ICElement) sel.get(0);
					if (!celem.getPath().equals(resPath)) {
						return inDirect;
					}
				}
			}
			return null;
		}
	}

	public ExtendedCPathBasePage(ITreeListAdapter adapter, String prefix) {
		super(CPathEntryMessages.getString(prefix + ".title")); //$NON-NLS-1$
		IncludeListAdapter includeListAdaper = new IncludeListAdapter();

		String[] buttonLabel = new String[]{ /* 0 */CPathEntryMessages.getString(prefix + ".add"), //$NON-NLS-1$
				/* 1 */CPathEntryMessages.getString(prefix + ".addFromWorkspace"), //$NON-NLS-1$
				/* 2 */CPathEntryMessages.getString(prefix + ".addContributed"), null, //$NON-NLS-1$
				/* 4 */CPathEntryMessages.getString(prefix + ".remove")}; //$NON-NLS-1$
		fPathList = new ListDialogField(includeListAdaper, buttonLabel, new ModifiedCPListLabelProvider()) {

			protected int getListStyle() {
				return super.getListStyle() & ~SWT.MULTI;
			}
		};
		fPathList.setDialogFieldListener(includeListAdaper);
		fPathList.setLabelText(CPathEntryMessages.getString(prefix + ".listName")); //$NON-NLS-1$
		fSrcList = new TreeListDialogField(adapter, new String[]{CPathEntryMessages.getString(prefix + ".editSourcePaths")}, //$NON-NLS-1$
				new CElementLabelProvider()) {

			protected int getTreeStyle() {
				return super.getTreeStyle() & ~SWT.MULTI;
			}
		};
		fSrcList.setLabelText(CPathEntryMessages.getString(prefix + ".sourcePaths")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		PixelConverter converter = new PixelConverter(parent);

		Composite composite = new Composite(parent, SWT.NONE);

		setControl(composite);

		LayoutUtil.doDefaultLayout(composite, new DialogField[]{fSrcList, fPathList}, true);
		LayoutUtil.setHorizontalGrabbing(fPathList.getListControl(null));

		int buttonBarWidth = converter.convertWidthInCharsToPixels(30);
		fPathList.setButtonsMinWidth(buttonBarWidth);
		fPathList.enableButton(IDX_REMOVE, false);

	}

	abstract protected void addPath();
	abstract protected void addFromWorkspace();
	abstract protected void addContributed();

	protected boolean canRemove(List selected) {
		return !selected.isEmpty();
	}

	protected void removePath(CPListElement element) {
		ICElement celem = (ICElement) getSelection().get(0);
		if (!celem.getPath().equals(element.getPath())) {
			IPath exclude = celem.getPath();

			IPath[] exclusions = (IPath[]) element.getAttribute(CPListElement.EXCLUSION);
			IPath[] newExlusions = new IPath[exclusions.length + 1];
			System.arraycopy(exclusions, 0, newExlusions, 0, exclusions.length);
			newExlusions[exclusions.length] = exclude;
			element.setAttribute(CPListElement.EXCLUSION, newExlusions);
			selectionChanged(new StructuredSelection(getSelection()));
		} else {
			fCPathList.remove(element);
			fPathList.removeElement(element);
		}
	}

	public void init(ICProject project, List cPaths) {
		List list = new ArrayList(project.getChildrenOfType(ICElement.C_CCONTAINER));
		int i;
		for (i = 0; i < list.size(); i++) {
			if (((ISourceRoot) list.get(i)).getResource() == project.getProject()) {
				break;
			}
		}
		if (i == list.size()) {
			list.add(0, project);
		}
		fSrcList.setElements(list);
		fCPathList = filterList(cPaths);
		fPathList.setElements(fCPathList);
		fSrcList.selectElements(new StructuredSelection(list.get(0)));
	}

	public List getCPaths() {
		return fCPathList;
	}

	public List getSelection() {
		return fSrcList.getSelectedElements();
	}

	public void setSelection(List selection) {
		fSrcList.selectElements(new StructuredSelection(selection));
	}

	public void selectionChanged(IStructuredSelection selection) {
		fPathList.setElements(filterList(getCPaths(), selection));
	}

	private List filterList(List list, IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return list;
		}
		Object sel = selection.getFirstElement();
		IPath resPath;
		resPath = ((ICElement) sel).getPath();
		List newList = new ArrayList(list.size());
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			CPListElement element = (CPListElement) iter.next();
			if (element.getPath().isPrefixOf(resPath)
					&& !CoreModelUtil.isExcludedPath(resPath, (IPath[]) element.getAttribute(CPListElement.EXCLUSION))) { //$NON-NLS-1$
				newList.add(element);
			}
		}
		return newList;
	}

	public void performApply(IProgressMonitor monitor) throws CoreException {
	}

	public void performDefaults() {
	}
}

