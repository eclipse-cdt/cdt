/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.ui.builder.internal;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;

import org.eclipse.cdt.core.builder.model.ICBuildVariable;
import org.eclipse.cdt.core.builder.model.ICToolchain;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * @author gene.sally
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CLibFileDialog extends Dialog {
	
	private static final int DLG_WIDTH = 60;
	private static final int DLG_HEIGHT = 25;

	private ICBuildVariable[] fLibPaths;	// the list of library paths used to populate the drop down
	private ICToolchain fToolchain;		// selected toolchain
	private TreeViewer fFileTree;			// tree control that's displayed
	private File fSelection;				// what the user selected in the dialog
	private List fLibNames;
	
	private class CItemSelectedIsLibrary implements ISelectionChangedListener {
		
		/**
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			
			// only enable the OK button when the user is resting on a file
			Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();			
			getButton(OK).setEnabled(selection instanceof File);
		}
	}

	private class CDoubleClickInTree implements IDoubleClickListener {

		public void doubleClick(DoubleClickEvent event) {

			// make sure that the user has double-clicked on a file before accepting this as the
			// selection			
			Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
			if (selection instanceof File) {
				getButton(OK).notifyListeners(SWT.Selection, new Event());
			}
		}
	}

	private class FileLabelProvider extends LabelProvider {
	
		private final Image IMG_FOLDER= PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		private final Image IMG_FILE= PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);

		public Image getImage(Object element) {

			if (element instanceof File) {
				if (((File) element).isFile()) {
					return IMG_FILE;
				}
			}
			else if (element instanceof TreeLibPathObject) {
				return IMG_FOLDER;
			}
			
			return null;
		}
	
		public String getText(Object element) {
	
			if (element instanceof File) {
				return ((File) element).getName();
			} else if (element instanceof TreeLibPathObject) {
				return ((TreeLibPathObject) element).toString();
			}
			
			return super.getText(element);
		}
	}
	
	private class CNoDirectories implements FileFilter {

		/**
		 * @see java.io.FileFilter#accept(File)
		 */
		public boolean accept(File pathname) {
			
			boolean isLibFile = false;
			if (pathname.isFile()) {
				String name = pathname.getName();
				if ((name.endsWith(".lib")) || (name.endsWith(".a")) || (name.indexOf(".so") != -1)) {
					isLibFile = true;
				}
			}	
			return (isLibFile);
		}
	}

	
	private class TreeLibPathObject implements IAdaptable {
		
		private ICBuildVariable m_location;			// the location this object represents
		private File m_fileSystem;				// the representation of this object on the file system
		private TreeParent m_parent;			// parent of this object
		
		public TreeLibPathObject(ICBuildVariable location, ICToolchain toolchain) {
			
			if (location != null) {
				m_location = location;
				m_fileSystem = new File(location.getValue());
			}
			
		}

		public File[] getChildren() {			
			
			// get the files in the file system matching this item
			File[] children  = m_fileSystem.listFiles(new CNoDirectories());

			return children;
		}

		public boolean hasChildren() {
			
			// ug, not the best for performance, consider caching
			return getChildren().length != 0;
		}
		
		public Object getAdapter(Class key) {
			return null;
		}
		
		public ICBuildVariable getBuildVar() {
			return m_location;
		}
		
		public String toString() {
			return m_location.toString();
		}
		
		public TreeParent getParent() {
			return m_parent;
		}
		
		public void setParent(TreeParent parent) {
			m_parent = parent;
		}
		
	}
	
	private class TreeParent  {
	
		private HashMap m_children;
		
		public TreeParent() {
			m_children = new HashMap();
		}
		public void addChild(TreeLibPathObject child) {
			m_children.put(child.m_fileSystem.getAbsolutePath(), child);
			child.setParent(this);
		}
		public void removeChild(TreeLibPathObject child) {
			m_children.remove(child.m_fileSystem.getAbsolutePath());
			child.setParent(null);
		}
		public TreeLibPathObject[] getChildren() {
			
			int nArraySize = m_children.entrySet().size();
			TreeLibPathObject[] retval = (TreeLibPathObject[]) m_children.values().toArray(new TreeLibPathObject[nArraySize]);
			
			return retval;

		}
		
		public boolean hasChildren() {
			return m_children.entrySet().size() > 0;
		}
		public TreeLibPathObject matchingParent(File test) {
			
			String parentPath = test.getParent();
			
			if (m_children.keySet().contains(parentPath)) {
				return (TreeLibPathObject) m_children.get(parentPath);
			}
			
			return null;
		}
	}
	
	class ViewContentProvider implements IStructuredContentProvider, 
									   ITreeContentProvider {

		private TreeParent invisibleRoot;
	
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {}
		public void dispose() {}
		
		public Object[] getElements(Object parent) {
			
			if (parent.equals(ResourcesPlugin.getWorkspace())) {
			// if (parent instanceof TreeRoot) {
				
				if (invisibleRoot == null) {
					initialize();
				}
				
				return getChildren(invisibleRoot);
			}
			
			return getChildren(parent);
		}
		
		public Object getParent(Object child) {

			// this is where things are going to get icky.
			// when the child is a file type, inspect the path and see if
			// it matches any of the parents in the list
			if (child instanceof File) {
				File currfile = (File) child;
				return invisibleRoot.matchingParent(currfile);
				
			}
			else if (child instanceof TreeLibPathObject) {
				return ((TreeLibPathObject) child).getParent();
			}

			return null;
		}
		
		public Object [] getChildren(Object parent) {

			// the root of the tree
			if (parent instanceof TreeParent) {
				
				return ((TreeParent) parent).getChildren();
			
			// the first level
			} else if (parent instanceof TreeLibPathObject) {
				
				return ((TreeLibPathObject) parent).getChildren();
			
			}
			
			// since we're not showing anything past the first level, 
			// just return an empty array
			return new Object[0];
		}
		
		public boolean hasChildren(Object parent) {			
	
			// the root of the tree
			if (parent instanceof TreeParent) {
				
				return ((TreeParent) parent).hasChildren();
			
			// the first level
			} else if (parent instanceof TreeLibPathObject) {
				
				return ((TreeLibPathObject) parent).hasChildren();
			
			}
			
			// since we're not showing anything past the first level, 
			// just return an empty array
			return false;

		}
	
		private void initialize() {
			
			invisibleRoot = new TreeParent();
			
			// read from the parent's list of items 
			for (int nIndex = 0; nIndex < fLibPaths.length; nIndex++) {
				invisibleRoot.addChild(new TreeLibPathObject(fLibPaths[nIndex], fToolchain));
			}
			
		}
	}

	// methods for CLibFileDialog
	public CLibFileDialog(Shell shell, ICToolchain toolchain, ICBuildVariable[] libPaths) {
		
		super(shell);		
		fLibPaths = libPaths;	
		fToolchain = toolchain;
		fLibNames = new List (shell, shell.getStyle());
		
	}

	// methods for CLibFileDialog
	public CLibFileDialog(Shell shell, ICToolchain toolchain, ICBuildVariable[] libPaths, List libList) {

		super(shell);		
		fLibPaths = libPaths;	
		fToolchain = toolchain;
		fLibNames = libList;

	}	
	protected Control createContents(Composite parent) {

		super.createContents(parent);
		 
		Composite composite = (Composite) getDialogArea();		
		
		GridLayout layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		layout.numColumns = 1;
		composite.setLayout(layout);
		
		// row 1
		fFileTree = new TreeViewer(composite, SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
		GridData gdTree = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_BOTH);
		
		gdTree.widthHint= convertWidthInCharsToPixels(DLG_WIDTH);
		gdTree.heightHint= convertHeightInCharsToPixels(DLG_HEIGHT);	
		fFileTree.getControl().setLayoutData(gdTree);
		fFileTree.setLabelProvider(new FileLabelProvider());
		fFileTree.setSorter(new ViewerSorter() {});
		fFileTree.setContentProvider(new ViewContentProvider());
	
		fFileTree.setInput(ResourcesPlugin.getWorkspace());
		
		fFileTree.addSelectionChangedListener(new CItemSelectedIsLibrary());
		fFileTree.addDoubleClickListener(new CDoubleClickInTree());
		
		return composite;
	}	

	/**
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(("Select_Library_1")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {

		ISelection selection; 					// the current selection 
		
		selection = fFileTree.getSelection();
		if (selection != null) {
			fSelection = (File) ((StructuredSelection) selection).getFirstElement();
		}
		
		// Let's check if this name exists or not.
		if (fLibNames.getItemCount() > 0) {
			boolean exists = checkExistance();
			if (exists) {
				return;
			}
		}			
		super.okPressed();
	}
	
	public File getSelection() {
		return fSelection;
	}
	
	private boolean checkExistance () {
		String[] existingItems = fLibNames.getItems();
		for (int i = 0; i < existingItems.length; i++) {
			if (existingItems[i].toString().equals(getSelection().getName())) {
 				String errorMsg = "This Identifier already exists in the Preprocessor definitions for this project";
 				MessageDialog.openError(this.getShell(), "Naming problems", errorMsg);				
 				return true;
 			}
 		}	
 		return false;	
 	} 

}
