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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.internal.ui.util.PixelConverter;
import org.eclipse.cdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.cdt.internal.ui.wizards.TypedElementSelectionValidator;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.TreeListDialogField;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public abstract class ExtendedCPathBasePage extends CPathBasePage {

	protected ListDialogField fPathList;
	protected TreeListDialogField fSrcList;
	protected List fCPathList;
	protected ICProject fCurrCProject;

	private static final int IDX_ADD = 0;
	private static final int IDX_ADD_WORKSPACE = 1;
	private static final int IDX_ADD_CONTRIBUTED = 2;
	private static final int IDX_REMOVE = 4;
	private String fPrefix;

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
		fPrefix = prefix;
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
	
	public boolean isEntryKind(int kind) {
		return kind == getEntryKind();
	}
	
	abstract protected void addPath();

	abstract int getEntryKind();
	
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

	protected IPathEntry[] getRawClasspath() {
		IPathEntry[] currEntries = new IPathEntry[fCPathList.size()];
		for (int i = 0; i < currEntries.length; i++) {
			CPListElement curr = (CPListElement) fCPathList.get(i);
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

	protected CPListElement[] openContainerSelectionDialog(CPListElement existing) {
		IPathEntry elem = null;
		String title;
		if (existing == null) {
			title = CPathEntryMessages.getString(fPrefix + ".ContainerDialog.new.title"); //$NON-NLS-1$
		} else {
			title = CPathEntryMessages.getString(fPrefix + ".ContainerDialog.edit.title"); //$NON-NLS-1$
			elem = existing.getPathEntry();
		}
		CPathContainerWizard wizard = new CPathContainerWizard(elem, fCurrCProject, getRawClasspath());
		wizard.setWindowTitle(title);
		if (CPathContainerWizard.openWizard(getShell(), wizard) == Window.OK) {
			IPathEntry[] created = wizard.getNewEntries();
			if (created != null) {
				CPListElement[] res = new CPListElement[created.length];
				for (int i = 0; i < res.length; i++) {
					res[i] = newCPElement(((ICElement) getSelection().get(0)).getResource());
					res[i].setAttribute(CPListElement.BASE_REF, created[i].getPath());
				}
				return res;
			}
		}
		return null;
	}

	private CPListElement newCPElement(IResource resource) {
		return new CPListElement(fCurrCProject, getEntryKind(), resource.getFullPath(), resource);
	}

	private class WorkbenchCPathLabelProvider extends CPListLabelProvider {

		WorkbenchLabelProvider fWorkbenchLabelProvider = new WorkbenchLabelProvider();

		public String getText(Object element) {
			if (element instanceof CPListElement) {
				return super.getText(element);
			}
			return fWorkbenchLabelProvider.getText(element);
		}

		public Image getImage(Object element) {
			if (element instanceof CPListElement) {
				return super.getImage(element);
			}
			return fWorkbenchLabelProvider.getImage(element);
		}
	}

	private class WorkbenchCPathContentProvider extends WorkbenchContentProvider {

		public Object[] getChildren(Object element) {
			if (element instanceof ICProject) {
				try {
					IPathEntry[] entries = ((ICProject) element).getRawPathEntries();
					List list = new ArrayList(entries.length);
					for (int i = 0; i < entries.length; i++) {
						if (entries[i].isExported()) {
							list.add(CPListElement.createFromExisting(entries[i], (ICProject) element));
						}
					}
					return list.toArray();
				} catch (CModelException e) {
					CUIPlugin.getDefault().log(e);
					return new Object[0];
				}
			}
			return super.getChildren(element);
		}

		public boolean hasChildren(Object element) {
			if (element instanceof ICProject) {
				try {
					IPathEntry[] entries = ((ICProject) element).getRawPathEntries();
					for (int i = 0; i < entries.length; i++) {
						if (entries[i].isExported()) {
							return true;
						}
					}
				} catch (CModelException e) {
					CUIPlugin.getDefault().log(e);
					return false;
				}
			}
			return super.hasChildren(element);
		}

		public Object getParent(Object element) {
			if (element instanceof CPListElement) {
				return ((CPListElement) element).getCProject().getProject();
			}
			return super.getParent(element);
		}
	}

	protected CPListElement[] openWorkspacePathEntryDialog(CPListElement existing) {
		Class[] acceptedClasses = new Class[]{CPListElement.class};
		TypedElementSelectionValidator validator = new TypedElementSelectionValidator(acceptedClasses, existing == null);
		ViewerFilter filter = new CPListElementFilter((CPListElement[]) fPathList.getElements().toArray(new CPListElement[0]), getEntryKind(), true);

		ILabelProvider lp = new WorkbenchCPathLabelProvider();
		ITreeContentProvider cp = new WorkbenchCPathContentProvider();

		String title = (existing == null) ? CPathEntryMessages.getString(fPrefix + ".fromWorkspaceDialog.new.title") //$NON-NLS-1$
				: CPathEntryMessages.getString(fPrefix + ".fromWorkspaceDialog.edit.title"); //$NON-NLS-1$
		String message = (existing == null) ? CPathEntryMessages.getString(fPrefix + ".fromWorkspaceDialog.new.description") //$NON-NLS-1$
				: NewWizardMessages.getString(fPrefix + ".fromWorkspaceDialog.edit.description"); //$NON-NLS-1$

		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), lp, cp);
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
			CPListElement[] res = new CPListElement[elements.length];
			for (int i = 0; i < res.length; i++) {
				res[i] = newCPElement(((ICElement) getSelection().get(0)).getResource());

			}
			return res;
		}
		return null;
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
				}
			}
			
			fPathList.addElements(elementsToAdd);
			fPathList.postSetSelection(new StructuredSelection(includes));
		}
	}

	protected void addFromWorkspace() {
		CPListElement[] includes = openWorkspacePathEntryDialog(null);
		if (includes != null) {
			int nElementsChosen= includes.length;					
			// remove duplicates
			List cplist= fPathList.getElements();
			List elementsToAdd= new ArrayList(nElementsChosen);
			
			for (int i= 0; i < nElementsChosen; i++) {
				CPListElement curr= includes[i];
				if (!cplist.contains(curr) && !elementsToAdd.contains(curr)) {
					elementsToAdd.add(curr);
				}
			}
			
			fPathList.addElements(elementsToAdd);
			fPathList.postSetSelection(new StructuredSelection(includes));
		}
	
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

