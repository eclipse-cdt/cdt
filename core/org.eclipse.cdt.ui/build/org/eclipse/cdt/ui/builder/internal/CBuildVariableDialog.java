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
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.builder.BuilderPlugin;
import org.eclipse.cdt.core.builder.ICBuildVariablePoint;
import org.eclipse.cdt.core.builder.model.CBuildVariable;
import org.eclipse.cdt.core.builder.model.ICBuildVariable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * @author gene.sally
 *
 * This class displays the selection tool for library paths or libraries
 * 
 */
public class CBuildVariableDialog extends Dialog {

	private static final int DLG_WIDTH = 60;		// for setting the height and width of the dialog
	private static final int DLG_HEIGHT = 25;

	private Combo 				fBrowseHow;			// combo control 
	private TreeViewer 		fTree;				// tree view control 
	private String 			fTitle;				// title for dialog
	private Map 				fBuildVariables;
	private ICBuildVariable	fSelection;

	private class CNoFiles implements FileFilter {
		public boolean accept(File file) {
			return (file.isDirectory() && !file.getName().equals(".metadata"));
		}
	}

	private class FileLabelProvider extends LabelProvider {
	
		private final Image IMG_FOLDER= PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		private final Image IMG_FILE= PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
	
		public Image getImage(Object element) {
			if (element instanceof File) {
				File curr= (File) element;
				if (curr.isDirectory()) {
					return IMG_FOLDER;
				} else {
					return IMG_FILE;
				}
			}
			return null;
		}
	
		public String getText(Object element) {
			if (element instanceof File) {
				return ((File) element).getName();
			}
			return super.getText(element);
		}
	}
	
	private class FileContentProvider implements ITreeContentProvider {
		
		private final Object[] EMPTY= new Object[0];
		
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof File) {
				File[] children = ((File) parentElement).listFiles(new CNoFiles());
				
				if (children != null) {
					return children;
				}
			}
			return EMPTY;
		}
	
		public Object getParent(Object element) {
			if (element instanceof File) {
				return ((File) element).getParentFile();
			}
			return null;
		}
	
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
	
		public Object[] getElements(Object element) {
			return getChildren(element);
		}
	
		public void dispose() {
		}
	
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	
	}

	private class CDoubleClickInTree implements IDoubleClickListener {

		public void doubleClick(DoubleClickEvent event) {
			
			ISelection selection; 
			
			selection = fTree.getSelection();
			if (selection != null) {
				updateSelection();
				getButton(OK).notifyListeners(SWT.Selection, new Event());
			}
			
		}

	}

	public CBuildVariableDialog(Shell parentShell, String title) {
		super(parentShell);
		fTitle = title;
		fBuildVariables = getBuildVariables();
	}

	public ICBuildVariable getSelection() {
		return fSelection;
	}
	
	protected Control createContents(Composite parent) {

		super.createContents(parent);
		 
		Composite composite = (Composite) getDialogArea();		
		
		GridLayout layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		composite.setLayout(layout);
		
		// row 1
		new Label(composite, SWT.NULL).setText(("Starting_Point_1")); //$NON-NLS-1$
		
		fBrowseHow = new Combo(composite, SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
		fBrowseHow.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));

		for (Iterator iter = fBuildVariables.entrySet().iterator(); iter.hasNext();) {
			Map.Entry element = (Map.Entry) iter.next();
			String value = element.getValue().toString();		
			fBrowseHow.add(value);
		}

		fBrowseHow.setData(fBuildVariables);
		fBrowseHow.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent sel) {
					String strSelection = fBrowseHow.getText();
					if (strSelection != null && fTree != null) {
						IPath path = expandBuildVar(strSelection);
						if (path != null) {
							fTree.setInput(path.toFile());
							CBuildVariableDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(fTree.getTree().getItemCount() != 0);
						}
					}
				}
			}
		
		);
		fBrowseHow.select(0);
		
		// row 2
		fTree = new TreeViewer(composite, SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
		GridData gdTree = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_BOTH);
		gdTree.horizontalSpan = 2;
		
		gdTree.widthHint= convertWidthInCharsToPixels(DLG_WIDTH);
		gdTree.heightHint= convertHeightInCharsToPixels(DLG_HEIGHT);	
		fTree.getControl().setLayoutData(gdTree);
		fTree.setLabelProvider(new FileLabelProvider());
		fTree.setSorter(new ViewerSorter() {});
		fTree.setContentProvider(new FileContentProvider());
	
		fTree.setInput(expandBuildVar((String) fBuildVariables.keySet().iterator().next()));
		
		fTree.addDoubleClickListener(new CDoubleClickInTree());
		
		return composite;
	}

	/**
	 * Method expandBuildVar.
	 * @param string
	 */
	private IPath expandBuildVar(String name) {
		ICBuildVariable bv = (ICBuildVariable) fBuildVariables.get(name);
		return new Path((null != bv) ? bv.getValue() : "");
	}


	/**
	 * Method getBuildVariables.
	 * @return Map
	 */
	private Map getBuildVariables() {
		Map vars = new HashMap();
		try {
			Map providers = BuilderPlugin.getDefault().getBuildVariableProviders();
			for (Iterator iter = providers.entrySet().iterator(); iter.hasNext();) {
				ICBuildVariablePoint expt = (ICBuildVariablePoint) iter.next();
				ICBuildVariable[] bv = expt.getProvider().getVariables();
				for (int i = 0; i < bv.length; i++) {
					vars.put(bv[i].getFixed(), bv[i]);
				}
			}
		} catch (CoreException e) {
			vars.clear();
		}
		return vars;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		updateSelection();				
		super.okPressed();
	}

	/**
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(fTitle);
	}
	
	private void updateSelection() {

		ISelection selection; 					// the current selection 
		
		// store the result of the user selection 
		selection = fTree.getSelection();
		if (selection != null) {
			File selectedPath = (File) ((StructuredSelection) selection).getFirstElement();
			if (selectedPath != null) {
				String strCurrRoot = fBrowseHow.getText();
				IPath selPath = new Path(selectedPath.getAbsolutePath());
				ICBuildVariable bv = (ICBuildVariable) fBuildVariables.get(strCurrRoot);
				IPath currPath = new Path((null != bv) ? bv.getValue() : "");
				int nMatchCount = currPath.matchingFirstSegments(selPath);
				IPath delta = selPath.removeFirstSegments(nMatchCount);
				delta = delta.setDevice(null);
				fSelection = new CBuildVariable(delta.toString(), bv);
			}
		}
	
	}
	

}
