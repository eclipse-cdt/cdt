/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.filetype.ICFileTypeAssociation;
import org.eclipse.cdt.core.filetype.ICFileTypeResolver;
import org.eclipse.cdt.core.filetype.IResolverModel;
import org.eclipse.cdt.internal.ui.util.PixelConverter;
import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/*
 * Preference block that encapsulates the controls used
 * for displaying/editing CDT file type associations
 */
public class CFileTypesPreferenceBlock {

	private static final int 	COL_PATTERN		= 0;
	private static final int 	COL_DESCRIPTION	= 1;
	private static final int 	COL_LANGUAGE	= 2;
	
	private ICFileTypeResolver	fResolver;
	private ArrayList			fAddAssoc;
	private ArrayList			fRemoveAssoc;
	private boolean				fDirty = false;
	
	private TableViewer 		fAssocViewer;
	private Button				fBtnNew;
	private Button				fBtnRemove;
	
	private class AssocSorter extends ViewerSorter {
		public int category(Object element) {
			if (element instanceof ICFileTypeAssociation) {
				ICFileTypeAssociation assoc = (ICFileTypeAssociation) element;
				if (-1 != assoc.getPattern().indexOf('*')) {
					return 10;
				}
				return 20;
			}
			return 30;
		}
	}
	
	private class AssocContentProvider implements IStructuredContentProvider {
		ICFileTypeAssociation[] assocs;
		
		public Object[] getElements(Object inputElement) {
			return assocs;
		}

		public void dispose() {
			assocs = null;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof ICFileTypeAssociation[]) {	
				assocs = (ICFileTypeAssociation[]) newInput;
			}
		}
	}
	
	private class AssocLabelProvider implements ILabelProvider, ITableLabelProvider {
		private ListenerList listeners = new ListenerList();
		
		public Image getColumnImage(Object element, int columnIndex) {
			if (element instanceof ICFileTypeAssociation) {
				if (COL_PATTERN == columnIndex) {
					// TODO: add image support to table
					return null;
				}
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof ICFileTypeAssociation) {
				ICFileTypeAssociation assoc = (ICFileTypeAssociation) element;
				switch (columnIndex) {
					case COL_PATTERN:
						return assoc.getPattern();
					
					case COL_DESCRIPTION:
						return assoc.getType().getName();
						
					case COL_LANGUAGE:
						return assoc.getType().getLanguage().getName();
				}
			}
			return element.toString();
		}

		public void addListener(ILabelProviderListener listener) {
			listeners.add(listener);
		}

		public void dispose() {
			listeners.clear();
			listeners = null;
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			listeners.remove(listener);
		}

		public Image getImage(Object element) {
			return getColumnImage(element, 0);
		}

		public String getText(Object element) {
			return getColumnText(element, 0);
		}

	}
	
	public CFileTypesPreferenceBlock(ICFileTypeResolver input) {
		fAddAssoc = new ArrayList();
		fRemoveAssoc = new ArrayList();
		setResolver(input);
		setDirty(false);
	}

	public Control createControl(Composite parent) {
		Composite 	control 		= new Composite(parent, SWT.NONE);
		GridLayout	controlLayout 	= new GridLayout(2, false);

		controlLayout.marginHeight = 0;
		controlLayout.marginWidth  = 0;

		control.setLayout(controlLayout);
		control.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL));
		
		// Create the table viewer for file associations
		
		Composite 	tablePane 		= new Composite(control, SWT.NONE);
		GridLayout	tablePaneLayout = new GridLayout();
		GridData	gridData		= new GridData(GridData.FILL_BOTH);
		
		tablePaneLayout.marginHeight = 0;
		tablePaneLayout.marginWidth  = 0;
		
		tablePane.setLayout(tablePaneLayout);
		tablePane.setLayoutData(gridData);

		Table table = new Table(tablePane, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		
		TableLayout 	tblLayout	= new TableLayout();
		TableColumn 	col			= null;
		
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = SWTUtil.getTableHeightHint(table, 25);
		gridData.widthHint = new PixelConverter(parent).convertWidthInCharsToPixels(60);
		
		tblLayout.addColumnData(new ColumnWeightData(20));
		tblLayout.addColumnData(new ColumnWeightData(60));
		tblLayout.addColumnData(new ColumnWeightData(20));
		
		table.setLayout(tblLayout);
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);		
		
		col = new TableColumn(table, SWT.LEFT);
		col.setText(PreferencesMessages.getString("CFileTypesPreferencePage.colTitlePattern")); //$NON-NLS-1$
		
		col = new TableColumn(table, SWT.LEFT);
		col.setText(PreferencesMessages.getString("CFileTypesPreferencePage.colTitleDescription")); //$NON-NLS-1$
		
		col = new TableColumn(table, SWT.LEFT);
		col.setText(PreferencesMessages.getString("CFileTypesPreferencePage.colTitleLanguage")); //$NON-NLS-1$

		// Create the button pane

		Composite	buttonPane			= new Composite(control, SWT.NONE);
		GridLayout	buttonPaneLayout	= new GridLayout();
		
		buttonPaneLayout.marginHeight = 0;
		buttonPaneLayout.marginWidth  = 0;
		
		buttonPane.setLayout(buttonPaneLayout);
		buttonPane.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		// New button
		
		fBtnNew		= new Button(buttonPane, SWT.PUSH);
		fBtnNew.setText(PreferencesMessages.getString("CFileTypesPreferenceBlock.New..."));  //$NON-NLS-1$
		
		gridData	= new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint	= SWTUtil.getButtonWidthHint(fBtnNew);
		gridData.heightHint	= SWTUtil.getButtonHeigthHint(fBtnNew);
		fBtnNew.setLayoutData(gridData);
		
		fBtnNew.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				handleAdd();
			}
		});

		// Remove button
		
		fBtnRemove 	= new Button(buttonPane, SWT.PUSH);
		fBtnRemove.setText(PreferencesMessages.getString("CFileTypesPreferenceBlock.Remove"));  //$NON-NLS-1$
		
		gridData	= new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint	= SWTUtil.getButtonWidthHint(fBtnRemove);
		gridData.heightHint	= SWTUtil.getButtonHeigthHint(fBtnRemove);
		fBtnRemove.setLayoutData(gridData);
		
		fBtnRemove.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				handleRemove();
			}
		});

		// Hook up the viewer
		
		fAssocViewer = new TableViewer(table);
		
		fAssocViewer.setSorter(new AssocSorter());
		fAssocViewer.setContentProvider(new AssocContentProvider());
		fAssocViewer.setLabelProvider(new AssocLabelProvider());
		fAssocViewer.setInput(getResolver().getFileTypeAssociations());

		fAssocViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged();
			}
		});
		
		handleSelectionChanged();
		
		return control;
	}

	public void setEnabled(boolean enabled) {
		fAssocViewer.getTable().setEnabled(enabled);
		fBtnNew.setEnabled(enabled);
		fBtnRemove.setEnabled(enabled);
		setDirty(enabled);
	}
	
	public void setResolver(ICFileTypeResolver resolver) {
		fAddAssoc.clear();
		fRemoveAssoc.clear();
		fResolver = resolver.createWorkingCopy();
		if (null != fAssocViewer) {
			fAssocViewer.setInput(fResolver.getFileTypeAssociations());
		}
		setDirty(true);
	}

	public ICFileTypeResolver getResolver() {
		return fResolver;
	}
	
	public void setDirty(boolean dirty) {
		fDirty = dirty;
	}
	
	public boolean isDirty() {
		return fDirty;
	}

	public boolean performOk() {
		boolean changed = fDirty;
		
		if (fDirty) {
			ICFileTypeAssociation[] add = (ICFileTypeAssociation[]) fAddAssoc.toArray(new ICFileTypeAssociation[fAddAssoc.size()]);
			ICFileTypeAssociation[] rem = (ICFileTypeAssociation[]) fRemoveAssoc.toArray(new ICFileTypeAssociation[fRemoveAssoc.size()]);
			
			fResolver.adjustAssociations(add, rem);
	
			fAddAssoc.clear();
			fRemoveAssoc.clear();

			setDirty(false);
		}
		
		return changed;
	}
	
	protected void handleSelectionChanged() {
		IStructuredSelection sel = getSelection();
		fBtnRemove.setEnabled(!sel.isEmpty());
	}

	private IResolverModel getResolverModel() {
		return CCorePlugin.getDefault().getResolverModel();
	}
	
	protected void handleAdd() {
		ICFileTypeAssociation assoc = null;

		CFileTypeDialog dlg = new CFileTypeDialog(fBtnNew.getParent().getShell());
		
		if (Window.OK == dlg.open()) {
			assoc = getResolverModel().createAssocation(dlg.getPattern(), dlg.getType());
			if (null != assoc) {
				fAssocViewer.add(assoc);
				fAddAssoc.add(assoc);
				fRemoveAssoc.remove(assoc);
				setDirty(true);
			}
		}
	}
	
	protected void handleRemove() {
		IStructuredSelection sel = getSelection();
		if ((null != sel) && (!sel.isEmpty())) {
			for (Iterator iter = sel.iterator(); iter.hasNext();) {
				ICFileTypeAssociation assoc = (ICFileTypeAssociation) iter.next();
				fAssocViewer.remove(assoc);
				fAddAssoc.remove(assoc);
				fRemoveAssoc.add(assoc);
				setDirty(true);
			}
		}
	}

	private IStructuredSelection getSelection() {
		return (IStructuredSelection) fAssocViewer.getSelection();
	}
}
