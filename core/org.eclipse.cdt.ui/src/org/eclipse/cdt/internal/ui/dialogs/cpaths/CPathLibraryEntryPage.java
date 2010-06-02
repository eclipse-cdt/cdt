/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.TypedElementSelectionValidator;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.TreeListDialogField;

/**
 * Libraries tab for C/C++ Project Paths page for 3.X projects.
 * 
 * @deprecated as of CDT 4.0. This tab was used to set preferences/properties
 * for 3.X style projects.
 */
@Deprecated
public class CPathLibraryEntryPage extends CPathBasePage {

	private ListDialogField<CPElement> fCPathList;
	private ICProject fCurrCProject;
	private IPath fProjPath;
	private TreeListDialogField<CPElement> fLibrariesList;

	private IWorkspaceRoot fWorkspaceRoot;

	private final int IDX_ADD_LIBEXT = 0;
	private final int IDX_ADD_LIB = 1;
	private final int IDX_ADD_CONTRIBUTED = 2;
	private final int IDX_EDIT = 4;
	private final int IDX_REMOVE = 5;
	private final int IDX_EXPORT = 7;

	public CPathLibraryEntryPage(ListDialogField<CPElement> cPathList) {
		super(CPathEntryMessages.LibrariesEntryPage_title); 
		setDescription(CPathEntryMessages.LibrariesEntryPage_description); 

		fWorkspaceRoot = CUIPlugin.getWorkspace().getRoot();
		fCPathList = cPathList;

		LibrariesAdapter adapter = new LibrariesAdapter();

		String[] buttonLabels= new String[] { 
			CPathEntryMessages.LibrariesEntryPage_libraries_addextlib_button, 
			CPathEntryMessages.LibrariesEntryPage_libraries_addworkspacelib_button, 
			CPathEntryMessages.LibrariesEntryPage_libraries_addcontriblib_button, 
			/* */ null,  
			CPathEntryMessages.LibrariesEntryPage_libraries_edit_button, 
			CPathEntryMessages.LibrariesEntryPage_libraries_remove_button, 
			null,
			CPathEntryMessages.LibrariesEntryPage_libraries_export_button
		};		

		fLibrariesList = new TreeListDialogField<CPElement>(adapter, buttonLabels, new CPElementLabelProvider());
		fLibrariesList.setDialogFieldListener(adapter);
		fLibrariesList.setLabelText(CPathEntryMessages.LibrariesEntryPage_libraries_label); 

		fLibrariesList.setViewerComparator(new CPElementSorter());
		fLibrariesList.enableButton(IDX_EDIT, false);
		fLibrariesList.enableButton(IDX_REMOVE, false);
		fLibrariesList.enableButton(IDX_EXPORT, false);
		fLibrariesList.setTreeExpansionLevel(2);
	}

	@Override
	public Image getImage() {
		return CPluginImages.get(CPluginImages.IMG_OBJS_ARCHIVE);
	}

	public void init(ICProject cproject) {
		fCurrCProject = cproject;
		fProjPath = fCurrCProject.getProject().getFullPath();
		updateLibrariesList();
	}

	private void updateLibrariesList() {
		List<CPElement> cpelements = filterList(fCPathList.getElements());
		fLibrariesList.setElements(cpelements);
	}		

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.dialogs.cpaths.CPathBasePage#getSelection()
	 */
	@Override
	public List<?> getSelection() {
		return fLibrariesList.getSelectedElements();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.dialogs.cpaths.CPathBasePage#setSelection(java.util.List)
	 */
	@Override
	public void setSelection(List<?> selElements) {
		fLibrariesList.selectElements(new StructuredSelection(selElements));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.dialogs.cpaths.CPathBasePage#isEntryKind(int)
	 */
	@Override
	public boolean isEntryKind(int kind) {
		return kind == IPathEntry.CDT_LIBRARY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	@Override
	public void performDefaults() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		PixelConverter converter = new PixelConverter(parent);
		Composite composite = new Composite(parent, SWT.NONE);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] {fLibrariesList}, true);
		LayoutUtil.setHorizontalGrabbing(fLibrariesList.getTreeControl(null));

		int buttonBarWidth = converter.convertWidthInCharsToPixels(24);
		fLibrariesList.setButtonsMinWidth(buttonBarWidth);

		fLibrariesList.getTreeViewer().setSorter(new CPElementSorter());

		setControl(composite);
		
		CUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(composite, ICHelpContextIds.PROJECT_PATHS_LIBRARIES);
	}

	private class LibrariesAdapter implements IDialogFieldListener, ITreeListAdapter<CPElement> {
		
		private final Object[] EMPTY_ARR= new Object[0];
		
		// -------- IListAdapter --------
		public void customButtonPressed(TreeListDialogField<CPElement> field, int index) {
			libraryPageCustomButtonPressed(field, index);
		}
		
		public void selectionChanged(TreeListDialogField<CPElement> field) {
			libraryPageSelectionChanged(field);
		}
		
		public void doubleClicked(TreeListDialogField<CPElement> field) {
			libraryPageDoubleClicked(field);
		}
		
		public void keyPressed(TreeListDialogField<CPElement> field, KeyEvent event) {
			libraryPageKeyPressed(field, event);
		}

		public Object[] getChildren(TreeListDialogField<CPElement> field, Object element) {
			if (element instanceof CPElement) {
				return ((CPElement) element).getChildren();
			}
			return EMPTY_ARR;
		}

		public Object getParent(TreeListDialogField<CPElement> field, Object element) {
			if (element instanceof CPElementAttribute) {
				return ((CPElementAttribute) element).getParent();
			}
			return null;
		}

		public boolean hasChildren(TreeListDialogField<CPElement> field, Object element) {
//			return (element instanceof CPElement);
			return false;
		}		
			
		// ---------- IDialogFieldListener --------
	
		public void dialogFieldChanged(DialogField field) {
		}
	}

	protected void libraryPageCustomButtonPressed(DialogField field, int index) {
		CPElement[] libentries= null;
		switch (index) {
		case IDX_ADD_LIB: /* add jar */
			libentries= openLibFileDialog(null);
			break;
		case IDX_ADD_LIBEXT: /* add external jar */
			libentries= openExtLibFileDialog(null);
			break;
		case IDX_ADD_CONTRIBUTED: /* add variable */
			libentries= openContainerSelectionDialog(null);
			break;
		case IDX_EDIT: /* edit */
			editEntry();
			return;
		case IDX_REMOVE: /* remove */
			removeEntry();
			return;
		case IDX_EXPORT :
			/* export */
			exportEntry();
			return;		
		}
		
		if (libentries != null) {
			int nElementsChosen= libentries.length;					
			// remove duplicates
			List<CPElement> cplist= fLibrariesList.getElements();
			List<CPElement> elementsToAdd= new ArrayList<CPElement>(nElementsChosen);
			
			for (int i= 0; i < nElementsChosen; i++) {
				CPElement curr= libentries[i];
				if (!cplist.contains(curr) && !elementsToAdd.contains(curr)) {
					elementsToAdd.add(curr);
					//curr.setAttribute(CPElement.SOURCEATTACHMENT, BuildPathSupport.guessSourceAttachment(curr));
				}
			}
			fLibrariesList.addElements(elementsToAdd);
			fCPathList.addElements(elementsToAdd);
			if (index == IDX_ADD_LIB) {
				fLibrariesList.refresh();
			}
			fLibrariesList.postSetSelection(new StructuredSelection(libentries));
		}
	}

	private boolean canExport(List<?> selElements) {
		if (selElements.size() == 0) {
			return false;
		}
		for (int i = 0; i < selElements.size(); i++) {
			Object elem = selElements.get(i);
			if (elem instanceof CPElement) {
				CPElement curr = (CPElement)elem;
				if (curr.getParentContainer() != null) {
					return false;
				}
				IPath base_ref = (IPath)curr.getAttribute(CPElement.BASE_REF);
				if (base_ref != null && !base_ref.equals(Path.EMPTY))
					return false;

			}
		}
		return true;
	}

	private void exportEntry() {
		List<?> selElements = fLibrariesList.getSelectedElements();
		if (selElements.size() != 1) {
			return;
		}
		Object elem = selElements.get(0);
		if (fLibrariesList.getIndexOfElement(elem) != -1) {
			((CPElement)elem).setExported(!((CPElement)elem).isExported()); // toggle export
			fLibrariesList.refresh(elem);
		}
	}
	
	protected void libraryPageDoubleClicked(TreeListDialogField<CPElement> field) {
		List<?> selection= fLibrariesList.getSelectedElements();
		if (canEdit(selection)) {
			editEntry();
		}
	}

	protected void libraryPageKeyPressed(TreeListDialogField<CPElement> field, KeyEvent event) {
		if (field == fLibrariesList) {
			if (event.character == SWT.DEL && event.stateMask == 0) {
				List<?> selection= field.getSelectedElements();
				if (canRemove(selection)) {
					removeEntry();
				}
			}
		}	
	}	

	private void removeEntry() {
		List<?> selElements= fLibrariesList.getSelectedElements();
		for (int i= selElements.size() - 1; i >= 0 ; i--) {
			Object elem= selElements.get(i);
			if (elem instanceof CPElementAttribute) {
				CPElementAttribute attrib= (CPElementAttribute) elem;
				attrib.getParent().setAttribute(attrib.getKey(), null);
				selElements.remove(i);				
			}
		}
		if (selElements.isEmpty()) {
			fLibrariesList.refresh();
			fCPathList.dialogFieldChanged(); // validate
		} else {
			fCPathList.removeElements(selElements);
			fLibrariesList.removeElements(selElements);
		}
	}

	private boolean canRemove(List<?> selElements) {
		if (selElements.size() == 0) {
			return false;
		}
		for (int i= 0; i < selElements.size(); i++) {
			Object elem= selElements.get(i);
			if (elem instanceof CPElementAttribute) {
				if (((CPElementAttribute)elem).getValue() == null) {
					return false;
				}
			} else if (elem instanceof CPElement) {
				CPElement curr= (CPElement) elem;
				if (curr.getParentContainer() != null) {
					return false;
				}
			}
		}
		return true;
	}	

	/**
	 * Method editEntry.
	 */
	private void editEntry() {
		List<?> selElements= fLibrariesList.getSelectedElements();
		if (selElements.size() != 1) {
			return;
		}
		Object elem= selElements.get(0);
		if (fLibrariesList.getIndexOfElement(elem) != -1) {
			editElementEntry((CPElement) elem);
		} else if (elem instanceof CPElementAttribute) {
			editAttributeEntry((CPElementAttribute) elem);
		}
	}
	
	private void editAttributeEntry(CPElementAttribute elem) {
		String key= elem.getKey();
		if (key.equals(CPElement.SOURCEATTACHMENT)) {
			CPElement selElement= elem.getParent();
			ILibraryEntry libEntry = (ILibraryEntry)selElement.getPathEntry();
			SourceAttachmentDialog dialog= new SourceAttachmentDialog(getShell(), libEntry, fCurrCProject, true);
			if (dialog.open() == Window.OK) {
				selElement.setAttribute(CPElement.SOURCEATTACHMENT, dialog.getSourceAttachmentPath());
				fLibrariesList.refresh();
				fCPathList.refresh(); // images
			}
		}
	}

	private void editElementEntry(CPElement elem) {
		CPElement[] res= null;
		switch (elem.getEntryKind()) {
			case IPathEntry.CDT_LIBRARY:
				IPath p = (IPath)elem.getAttribute(CPElement.LIBRARY);
				if (p.isAbsolute()) {
					res= openExtLibFileDialog(elem);
				} else {
					res= openLibFileDialog(elem);			
				}
			break;
		}
		if (res != null && res.length > 0) {
			CPElement curr= res[0];
			curr.setExported(elem.isExported());
			fLibrariesList.replaceElement(elem, curr);
		}					
	}

	protected void libraryPageSelectionChanged(DialogField field) {
		List<?> selElements= fLibrariesList.getSelectedElements();
		fLibrariesList.enableButton(IDX_EDIT, canEdit(selElements));
		fLibrariesList.enableButton(IDX_REMOVE, canRemove(selElements));
		fLibrariesList.enableButton(IDX_EXPORT, canExport(selElements));
	}

	private IFile[] getUsedLibFiles(CPElement existing) {
		List<IResource> res= new ArrayList<IResource>();
		List<CPElement> cplist= fLibrariesList.getElements();
		for (int i= 0; i < cplist.size(); i++) {
			CPElement elem= cplist.get(i);
			if (elem.getEntryKind() == IPathEntry.CDT_LIBRARY && (elem != existing)) {
				IResource resource= elem.getResource();
				if (resource instanceof IFile) {
					res.add(resource);
				}
			}
		}
		return res.toArray(new IFile[res.size()]);
	}

	private CPElement newCPLibraryElement(IPath libraryPath) {
		CPElement element = new CPElement(fCurrCProject, IPathEntry.CDT_LIBRARY, fProjPath, null);
		element.setAttribute(CPElement.LIBRARY, libraryPath);
		return element;
	}

	private CPElement[] openExtLibFileDialog(CPElement existing) {
		String title= CPathEntryMessages.LibrariesEntryPage_ExtLibDialog_new_title; 
		
		FileDialog dialog= new FileDialog(getShell(), existing == null ? SWT.MULTI : SWT.SINGLE);
		dialog.setText(title);
		dialog.setFilterExtensions(new String[] {"*.a;*.so;*.dll;*.lib"}); //$NON-NLS-1$
		//dialog.setFilterPath(lastUsedPath);
		if (existing != null) {
			dialog.setFileName(existing.getPath().lastSegment());
		}
		
		String res= dialog.open();
		if (res == null) {
			return null;
		}
		String[] fileNames= dialog.getFileNames();
		int nChosen= fileNames.length;
			
		IPath filterPath= new Path(dialog.getFilterPath());
		CPElement[] elems= new CPElement[nChosen];
		for (int i= 0; i < nChosen; i++) {
			IPath path= filterPath.append(fileNames[i]).makeAbsolute();	
			elems[i]= newCPLibraryElement(path);
		}
		//fDialogSettings.put(IUIConstants.DIALOGSTORE_LASTEXTJAR, filterPath.toOSString());
		
		return elems;	
	}

	private CPElement[] openLibFileDialog(CPElement existing) {
		Class<?>[] acceptedClasses= new Class[] { IFile.class };
		TypedElementSelectionValidator validator= new TypedElementSelectionValidator(acceptedClasses, existing == null);
		ViewerFilter filter= new ArchiveFileFilter(getUsedLibFiles(existing), true);
		
		ILabelProvider lp= new WorkbenchLabelProvider();
		ITreeContentProvider cp= new WorkbenchContentProvider();
		
		String title= (existing == null) ? CPathEntryMessages.LibrariesEntryPage_ExtLibDialog_new_title : CPathEntryMessages.LibrariesEntryPage_ExtLibDialog_edit_title; 
		String message= (existing == null) ? CPathEntryMessages.LibrariesEntryPage_ExtLibDialog_new_description : CPathEntryMessages.LibrariesEntryPage_ExtLibDialog_edit_description; 
		
		ElementTreeSelectionDialog dialog= new ElementTreeSelectionDialog(getShell(), lp, cp);
		dialog.setValidator(validator);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.addFilter(filter);
		dialog.setInput(fWorkspaceRoot);
		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
		if (existing == null) {
			dialog.setInitialSelection(fCurrCProject.getProject());		
		} else {
			dialog.setInitialSelection(existing.getResource());
		}
		
		if (dialog.open() == Window.OK) {
			Object[] elements= dialog.getResult();
			CPElement[] res= new CPElement[elements.length];
			for (int i= 0; i < res.length; i++) {
				IPath path= ((IResource)elements[i]).getLocation();
				res[i]= newCPLibraryElement(path);
			}
			return res;
		}
		return null;
	}

	protected IPathEntry[] getRawPathEntries() {
		IPathEntry[] currEntries = new IPathEntry[fCPathList.getSize()];
		for (int i = 0; i < currEntries.length; i++) {
			CPElement curr = fCPathList.getElement(i);
			currEntries[i] = curr.getPathEntry();
		}
		return currEntries;
	}

	protected CPElement[] openContainerSelectionDialog(CPElement existing) {
		IContainerEntry elem = null;
		String title;
		if (existing == null) {
			title = CPathEntryMessages.LibrariesEntryPage_ContainerDialog_new_title; 
		} else {
			title = CPathEntryMessages.LibrariesEntryPage_ContainerDialog_edit_title; 
			elem = (IContainerEntry)existing.getPathEntry();
		}
		CPathContainerWizard wizard = new CPathContainerWizard(elem, null, fCurrCProject, getRawPathEntries(),
				new int[] {IPathEntry.CDT_LIBRARY});
		wizard.setWindowTitle(title);
		if (CPathContainerWizard.openWizard(getShell(), wizard) == Window.OK) {
			IPathEntry parent = wizard.getEntriesParent();
			IPathEntry[] elements = wizard.getEntries();

			if (elements != null) {
				CPElement[] res = new CPElement[elements.length];
				for (int i = 0; i < res.length; i++) {
					res[i] = new CPElement(fCurrCProject, IPathEntry.CDT_LIBRARY, fProjPath, null);
					res[i].setAttribute(CPElement.LIBRARY, ((ILibraryEntry)elements[i]).getLibraryPath());
					res[i].setAttribute(CPElement.BASE_REF, parent.getPath());
				}
				return res;
			}
		}
		return null;
	}

	private boolean canEdit(List<?> selElements) {
		if (selElements.size() != 1) {
			return false;
		}
		Object elem= selElements.get(0);
		if (elem instanceof CPElement) {
			CPElement curr= (CPElement) elem;
			return !(curr.getResource() instanceof IFolder) && curr.getParentContainer() == null;
		}
		if (elem instanceof CPElementAttribute) {
			return true;
		}
		return false;
	}

}
