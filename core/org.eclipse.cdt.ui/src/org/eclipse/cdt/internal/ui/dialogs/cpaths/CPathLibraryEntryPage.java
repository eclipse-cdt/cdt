/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.util.PixelConverter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.TreeListDialogField;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * CPathLibraryEntryPage
 */
public class CPathLibraryEntryPage extends CPathBasePage {

	private ListDialogField fCPathList;
	private ICProject fCurrCProject;
	private TreeListDialogField fLibrariesList;

	private IWorkspaceRoot fWorkspaceRoot;

	private final int IDX_ADD = 0;
	private final int IDX_EDIT = 2;
	private final int IDX_REMOVE = 3;

	/**
	 * @param title
	 */
	public CPathLibraryEntryPage(ListDialogField cPathList) {
		super(CPathEntryMessages.getString("LibrariesEntryPage.title")); //$NON-NLS-1$
		setDescription(CPathEntryMessages.getString("LibrariesEntryPage.description")); //$NON-NLS-1$

		fWorkspaceRoot = CUIPlugin.getWorkspace().getRoot();
		fCPathList = cPathList;

		LibrariesAdapter adapter = new LibrariesAdapter();

		String[] buttonLabels= new String[] { 
			/* IDX_ADDLIB*/ CPathEntryMessages.getString("LibrariesEntryPage.libraries.addworkspacelib.button"),	//$NON-NLS-1$
			/* IDX_ADDEXT */ CPathEntryMessages.getString("LibrariesEntryPage.libraries.addextlib.button"), //$NON-NLS-1$
			/* */ null,  
			/* IDX_EDIT */ CPathEntryMessages.getString("LibrariesEntryPage.libraries.edit.button"), //$NON-NLS-1$
			/* IDX_REMOVE */ CPathEntryMessages.getString("LibrariesEntryPage.libraries.remove.button") //$NON-NLS-1$
		};		

		fLibrariesList = new TreeListDialogField(adapter, buttonLabels, new CPElementLabelProvider());
		fLibrariesList.setDialogFieldListener(adapter);
		fLibrariesList.setLabelText(CPathEntryMessages.getString("LibrariesEntryPage.libraries.label")); //$NON-NLS-1$

		fLibrariesList.setViewerSorter(new CPElementSorter());
		fLibrariesList.enableButton(IDX_EDIT, false);
	}

	public Image getImage() {
		return CPluginImages.get(CPluginImages.IMG_OBJS_ARCHIVE);
	}

	public void init(ICProject cproject) {
		fCurrCProject = cproject;
		updateLibrariesList();
	}

	private void updateLibrariesList() {
		//List cpelements= fLibrariesList.getElements();
		List cpelements = filterList(fCPathList.getElements());
		List libelements= new ArrayList(cpelements.size());
		
		int nElements= cpelements.size();
		for (int i= 0; i < nElements; i++) {
			CPElement cpe= (CPElement)cpelements.get(i);
			if (isEntryKind(cpe.getEntryKind())) {
				libelements.add(cpe);
			}
		}
		fLibrariesList.setElements(libelements);
	}		

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.dialogs.cpaths.CPathBasePage#getSelection()
	 */
	public List getSelection() {
		return fLibrariesList.getSelectedElements();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.dialogs.cpaths.CPathBasePage#setSelection(java.util.List)
	 */
	public void setSelection(List selElements) {
		fLibrariesList.selectElements(new StructuredSelection(selElements));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.dialogs.cpaths.CPathBasePage#isEntryKind(int)
	 */
	public boolean isEntryKind(int kind) {
		return kind == IPathEntry.CDT_LIBRARY || kind == IPathEntry.CDT_CONTAINER;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	public void performDefaults() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		PixelConverter converter = new PixelConverter(parent);
		Composite composite = new Composite(parent, SWT.NONE);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] {fLibrariesList}, true);
		LayoutUtil.setHorizontalGrabbing(fLibrariesList.getTreeControl(null));

		int buttonBarWidth = converter.convertWidthInCharsToPixels(24);
		fLibrariesList.setButtonsMinWidth(buttonBarWidth);

		fLibrariesList.getTreeViewer().setSorter(new CPElementSorter());

		setControl(composite);
	}

	private class LibrariesAdapter implements IDialogFieldListener, ITreeListAdapter {
		
		private final Object[] EMPTY_ARR= new Object[0];
		
		// -------- IListAdapter --------
		public void customButtonPressed(TreeListDialogField field, int index) {
			//libraryPageCustomButtonPressed(field, index);
		}
		
		public void selectionChanged(TreeListDialogField field) {
			//libraryPageSelectionChanged(field);
		}
		
		public void doubleClicked(TreeListDialogField field) {
			//libraryPageDoubleClicked(field);
		}
		
		public void keyPressed(TreeListDialogField field, KeyEvent event) {
			//libraryPageKeyPressed(field, event);
		}

		public Object[] getChildren(TreeListDialogField field, Object element) {
			if (element instanceof CPElement) {
				return ((CPElement) element).getChildren();
			}
			return EMPTY_ARR;
		}

		public Object getParent(TreeListDialogField field, Object element) {
			if (element instanceof CPElementAttribute) {
				return ((CPElementAttribute) element).getParent();
			}
			return null;
		}

		public boolean hasChildren(TreeListDialogField field, Object element) {
			return (element instanceof CPElement);
		}		
			
		// ---------- IDialogFieldListener --------
	
		public void dialogFieldChanged(DialogField field) {
			//libaryPageDialogFieldChanged(field);
		}
	}

}
