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

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.IWorkbenchAdapter;

public abstract class ExtendedCPathBasePage extends CPathBasePage {

	private ListDialogField fPathList;
	private TreeListDialogField fSrcList;
	private List fCPathList;

	private class IncludeListAdapter implements IListAdapter, IDialogFieldListener {

		public void dialogFieldChanged(DialogField field) {
		}

		public void customButtonPressed(ListDialogField field, int index) {
		}

		public void selectionChanged(ListDialogField field) {
		}

		public void doubleClicked(ListDialogField field) {
		}
	}

	private class GlobalElement implements IAdaptable, IWorkbenchAdapter {

		private final IProject fProject;

		public GlobalElement(IProject project) {
			fProject = project;
		}

		public IPath getPath() {
			return fProject.getFullPath();
		}

		public Object getAdapter(Class adapter) {
			if (adapter.equals(IWorkbenchAdapter.class)) {
				return this;
			}
			return null;
		}

		public Object[] getChildren(Object o) {
			return new Object[0];
		}

		public ImageDescriptor getImageDescriptor(Object object) {
			return CUIPlugin.getDefault().getWorkbench().getSharedImages().getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT);
		}

		public String getLabel(Object o) {
			return "Global Entries"; //$NON-NLS-1$
		}

		public Object getParent(Object o) {
			return null;
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

		String[] buttonLabel = new String[] { CPathEntryMessages.getString(prefix + ".add"), //$NON-NLS-1$
				CPathEntryMessages.getString(prefix + ".addFromWorkspace"), //$NON-NLS-1$
				CPathEntryMessages.getString(prefix + ".addContributed"), null, //$NON-NLS-1$
				CPathEntryMessages.getString(prefix + ".remove")}; //$NON-NLS-1$
		fPathList = new ListDialogField(includeListAdaper, buttonLabel, new ModifiedCPListLabelProvider()) {

			protected int getListStyle() {
				return super.getListStyle() & ~SWT.MULTI;
			}
		};
		fPathList.setDialogFieldListener(includeListAdaper);
		fPathList.setLabelText(CPathEntryMessages.getString(prefix + ".listName")); //$NON-NLS-1$
		fSrcList = new TreeListDialogField(adapter, new String[] { CPathEntryMessages.getString(prefix + ".editSourcePaths")}, //$NON-NLS-1$
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

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { fSrcList, fPathList}, true);
		LayoutUtil.setHorizontalGrabbing(fPathList.getListControl(null));

		int buttonBarWidth = converter.convertWidthInCharsToPixels(30);
		fPathList.setButtonsMinWidth(buttonBarWidth);
	}

	public void init(ICProject project, List cPaths) {
		List list = new ArrayList(project.getChildrenOfType(ICElement.C_CCONTAINER));
		list.add(0, new GlobalElement(project.getProject()));
		fSrcList.setElements(list);
		fCPathList = filterList(cPaths);
		fPathList.setElements(fCPathList);
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
		if (sel instanceof ICElement) {
			resPath = ((ICElement) sel).getPath();
		} else {
			resPath = ((GlobalElement) sel).getPath();
		}
		List newList = new ArrayList(list.size());
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			CPListElement element = (CPListElement) iter.next();
			if (element.getPath().isPrefixOf(resPath) && !CoreModelUtil.isExcludedPath(resPath, (IPath[]) element.getAttribute(CPListElement.EXCLUSION))) { //$NON-NLS-1$
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

