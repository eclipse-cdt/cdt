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
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
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
import org.eclipse.cdt.ui.CUIPlugin;
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

	protected List fCPathList;
	protected ICProject fCurrCProject;

	private ListDialogField fPathList;
	private TreeListDialogField fSrcList;

	private class ListAdapter implements IListAdapter, IDialogFieldListener {

		public void dialogFieldChanged(DialogField field) {
		}

		public void customButtonPressed(ListDialogField field, int index) {
			buttonPressed(index, field.getSelectedElements());
		}

		public void selectionChanged(ListDialogField field) {
			pathSelectionChanged();
		}

		public void doubleClicked(ListDialogField field) {
		}
	}

	private class ModifiedCPListLabelProvider extends CPElementLabelProvider implements IColorProvider {

		private final Color inDirect = new Color(Display.getDefault(), new RGB(170, 170, 170));

		public Color getBackground(Object element) {
			return null;
		}

		public Color getForeground(Object element) {
			if (isPathInheritedFromSelected((CPElement) element)) {
				return inDirect;
			}
			return null;
		}
	}

	public ExtendedCPathBasePage(ITreeListAdapter adapter, String title, String pathTitle, String[] buttons) {
		super(title);
		ListAdapter includeListAdaper = new ListAdapter();

		fPathList = new ListDialogField(includeListAdaper, buttons, new ModifiedCPListLabelProvider()) {

			protected int getListStyle() {
				return super.getListStyle() & ~SWT.MULTI;
			}
		};
		fPathList.setDialogFieldListener(includeListAdaper);
		fPathList.setLabelText(pathTitle);
		fSrcList = new TreeListDialogField(adapter,
				null /*new String[]{CPathEntryMessages.getString("ExtendingCPathBasePage.editSourcePaths")}*/, //$NON-NLS-1$
				new CElementLabelProvider()) {

			protected int getTreeStyle() {
				return super.getTreeStyle() & ~SWT.MULTI;
			}
		};
		fSrcList.setLabelText(CPathEntryMessages.getString("ExtendingCPathBasePage.sourcePaths")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		PixelConverter converter = new PixelConverter(parent);

		Composite composite = new Composite(parent, SWT.NONE);

		setControl(composite);

		LayoutUtil.doDefaultLayout(composite, new DialogField[]{fSrcList, fPathList}, true);
		LayoutUtil.setHorizontalGrabbing(fPathList.getListControl(null));

		int buttonBarWidth = converter.convertWidthInCharsToPixels(30);
		fPathList.setButtonsMinWidth(buttonBarWidth);
	}

	protected ListDialogField getPathList() {
		return fPathList;
	}
	
	abstract protected void buttonPressed(int indx, List selected);

	protected abstract void pathSelectionChanged();

	protected boolean isPathInheritedFromSelected(CPElement element) {
		IPath resPath = element.getPath();
		List sel = getSelection();
		if (!sel.isEmpty()) {
			if (sel.get(0) instanceof ICElement) {
				ICElement celem = (ICElement) sel.get(0);
				if (!celem.getPath().equals(resPath)) {
					return true;
				}
			}
		}
		return false;
	}

	protected void removeFromSelectedPath(CPElement element) {
		ICElement celem = (ICElement) getSelection().get(0);
		if (!celem.getPath().equals(element.getPath())) {
			IPath exclude = celem.getPath().removeFirstSegments(element.getPath().segmentCount()).addTrailingSeparator();
			IPath[] exclusions = (IPath[]) element.getAttribute(CPElement.EXCLUSION);
			IPath[] newExlusions = new IPath[exclusions.length + 1];
			System.arraycopy(exclusions, 0, newExlusions, 0, exclusions.length);
			newExlusions[exclusions.length] = exclude;
			element.setAttribute(CPElement.EXCLUSION, newExlusions);
			selectionChanged(new StructuredSelection(getSelection()));
		} else {
			fCPathList.remove(element);
			fPathList.removeElement(element);
		}
	}

	public void init(ICProject project, List cPaths) {
		fCurrCProject = project;
		List list = new ArrayList();
		try {
			List clist = project.getChildrenOfType(ICElement.C_CCONTAINER);
			list.addAll(clist);
		} catch (CModelException e) {
			CUIPlugin.getDefault().log(e);
		}

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

	protected IPathEntry[] getRawPathEntries() {
		IPathEntry[] currEntries = new IPathEntry[fCPathList.size()];
		for (int i = 0; i < currEntries.length; i++) {
			CPElement curr = (CPElement) fCPathList.get(i);
			currEntries[i] = curr.getPathEntry();
		}
		return currEntries;
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

	protected List filterList(List list, IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return list;
		}
		Object sel = selection.getFirstElement();
		IPath resPath;
		resPath = ((ICElement) sel).getPath();
		List newList = new ArrayList(list.size());
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			CPElement element = (CPElement) iter.next();
			if (element.getPath().isPrefixOf(resPath)
					&& (element.getPath().equals(resPath) || !CoreModelUtil.isExcludedPath(resPath.removeFirstSegments(1),
							(IPath[]) element.getAttribute(CPElement.EXCLUSION)))) {
				newList.add(element);
			}
		}
		return newList;
	}

	public void performApply(IProgressMonitor monitor) throws CoreException {
	}

	public void performDefaults() {
	}

	//	private IPathEntry[] getUsedPathFiles(CPListElement existing) {
	//		List res= new ArrayList();
	//		List cplist= fPathList.getElements();
	//		for (int i= 0; i < cplist.size(); i++) {
	//			CPListElement elem= (CPListElement)cplist.get(i);
	//			if (isEntryKind(elem.getEntryKind()) && (elem != existing)) {
	//				IResource resource= elem.getResource();
	//				if (resource instanceof IFile) {
	//					res.add(resource);
	//				}
	//			}
	//		}
	//		return (IFile[]) res.toArray(new IFile[res.size()]);
	//	}

}

