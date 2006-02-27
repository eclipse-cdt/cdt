/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup; 
 
import java.util.ArrayList;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.sourcelookup.ISourceContainerBrowser;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

/**
 * The dialog for adding new source containers. Presents the user with a list of
 * source container types and allows them to select one.
 * 
 * @since 3.0
 */
public class AddSourceContainerDialog extends TitleAreaDialog {
	
	private TableViewer fViewer;
	private SourceContainerViewer fSourceContainerViewer;
	private boolean fDoubleClickSelects = true;
	private ISourceLookupDirector fDirector;
	
	/**
	 * Constructor
	 */
	public AddSourceContainerDialog(Shell shell, SourceContainerViewer viewer, ISourceLookupDirector director) {		
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fSourceContainerViewer=viewer;		
		fDirector = director;
	}
	
	/**
	 * Creates the dialog area to display source container types that are "browseable"
	 */
	protected Control createDialogArea(Composite ancestor) {			
		
		getShell().setText(SourceLookupUIMessages.getString( "AddSourceContainerDialog.0" )); //$NON-NLS-1$
		setTitle(SourceLookupUIMessages.getString( "AddSourceContainerDialog.1" )); //$NON-NLS-1$
		
		Composite parent = new Composite(ancestor, SWT.NULL);
		GridData gd= new GridData(GridData.FILL_BOTH);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 1;
		parent.setLayout(topLayout);
		parent.setLayoutData(gd);	
				
		ISourceContainerType[] types = filterTypes(DebugPlugin.getDefault().getLaunchManager().getSourceContainerTypes());
		
		fViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE);
		final Table table = fViewer.getTable();
		gd = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gd);

		if (fDoubleClickSelects) {
			table.addSelectionListener(new SelectionAdapter() {
				public void widgetDefaultSelected(SelectionEvent e) {
					if (table.getSelectionCount() == 1)
						okPressed();
				}
			});
		}
		
		fViewer.setLabelProvider(new SourceContainerLabelProvider());
		fViewer.setContentProvider(new ArrayContentProvider());			
		fViewer.setSorter(new ViewerSorter());
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				String desc = null;
				if (!selection.isEmpty()) {
					ISourceContainerType type = (ISourceContainerType) ((IStructuredSelection)selection).getFirstElement();
					desc = type.getDescription();
				}
				setMessage(desc);
			}
		});
		if(types.length != 0) {	
			fViewer.setInput(types);
		}
		Dialog.applyDialogFont(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp( getShell(), ICDebugHelpContextIds.ADD_SOURCE_CONTAINER_DIALOG );
		return parent;
	}	
	
	/**
	 * Removes types without browsers from the provided list of types.
	 * @param types the complete list of source container types
	 * @return the list of source container types that have browsers
	 */
	private ISourceContainerType[] filterTypes(ISourceContainerType[] types){
		ArrayList validTypes = new ArrayList();
		for (int i=0; i< types.length; i++) {
			ISourceContainerType type = types[i];
			if (fDirector.supportsSourceContainerType(type)) {
				ISourceContainerBrowser sourceContainerBrowser = DebugUITools.getSourceContainerBrowser(type.getId());
				if(sourceContainerBrowser != null && sourceContainerBrowser.canAddSourceContainers(fDirector)) {
					validTypes.add(type);
				}
			}
		}	
		return (ISourceContainerType[]) validTypes.toArray(new ISourceContainerType[validTypes.size()]);
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		//single selection dialog, so take first item in array
		//there will always be a selected item since we set it with viewer.setSelection
		ISourceContainerType type = (ISourceContainerType) ((StructuredSelection) fViewer.getSelection()).getFirstElement();
		ISourceContainerBrowser browser = DebugUITools.getSourceContainerBrowser(type.getId());
		if (browser != null) {
			ISourceContainer[] results = browser.addSourceContainers(getShell(), fDirector);
			if(results != null) {
				fSourceContainerViewer.addEntries(results);
			}
		}
		super.okPressed();
	}
	
}
