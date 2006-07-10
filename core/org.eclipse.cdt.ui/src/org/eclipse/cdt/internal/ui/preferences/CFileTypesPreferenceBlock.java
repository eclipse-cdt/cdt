/**********************************************************************
 * Copyright (c) 2004, 2005 TimeSys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     TimeSys Corporation - Initial implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.content.IContentTypeSettings;
import org.eclipse.core.runtime.preferences.IScopeContext;
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

import org.eclipse.cdt.core.model.CoreModel;

import org.eclipse.cdt.internal.ui.util.PixelConverter;
import org.eclipse.cdt.internal.ui.util.SWTUtil;

/*
 * Preference block that encapsulates the controls used
 * for displaying/editing CDT file type associations
 */
public class CFileTypesPreferenceBlock {

	private static final int 	COL_PATTERN		= 0;
	private static final int 	COL_DESCRIPTION	= 1;
	private static final int 	COL_STATUS	= 2;
	
	private ArrayList	fAddAssoc;
	private ArrayList	fRemoveAssoc;
	private boolean		fDirty = false;
	private IProject	fInput;
	private IContentType[] fContentTypes;

	private TableViewer 		fAssocViewer;
	private Button				fBtnNew;
	private Button				fBtnRemove;

	private class AssocSorter extends ViewerSorter {
		public int category(Object element) {
			if (element instanceof CFileTypeAssociation) {
				CFileTypeAssociation assoc = (CFileTypeAssociation) element;
				if (assoc.isExtSpec()) {
					return 10;
				}
				return 20;
			}
			return 30;
		}
	}
	
	private class AssocContentProvider implements IStructuredContentProvider {
		CFileTypeAssociation[] assocs;
		
		public Object[] getElements(Object inputElement) {
			return assocs;
		}

		public void dispose() {
			assocs = null;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof CFileTypeAssociation[]) {	
				assocs = (CFileTypeAssociation[]) newInput;
			}
		}
	}
	
	private class AssocLabelProvider implements ILabelProvider, ITableLabelProvider {
		private ListenerList listeners = new ListenerList();
		
		public Image getColumnImage(Object element, int columnIndex) {
			if (element instanceof CFileTypeAssociation) {
				if (COL_PATTERN == columnIndex) {
					return null;
				}
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof CFileTypeAssociation) {
				CFileTypeAssociation assoc = (CFileTypeAssociation) element;
				switch (columnIndex) {
					case COL_PATTERN:
						return assoc.getPattern();
					
					case COL_DESCRIPTION:
						return assoc.getDescription();
						
					case COL_STATUS:
						if (assoc.isUserDefined()) {
							return PreferencesMessages.getString("CFileTypesPreferencePage.userDefined"); //$NON-NLS-1$
						} else if (assoc.isPredefined()) {
							return PreferencesMessages.getString("CFileTypesPreferencePage.preDefined"); //$NON-NLS-1$
						}
						return new String();
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

	public CFileTypesPreferenceBlock() {
		this(null);
	}

	public CFileTypesPreferenceBlock(IProject input) {
		fAddAssoc = new ArrayList();
		fRemoveAssoc = new ArrayList();
		fInput = input;
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
		col.setText(PreferencesMessages.getString("CFileTypesPreferencePage.colTitleStatus")); //$NON-NLS-1$

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
		fAssocViewer.setInput(getCFileTypeAssociations());

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
	
	public void setInput(IProject input) {
		fAddAssoc.clear();
		fRemoveAssoc.clear();
		fInput = input;
		if (null != fAssocViewer) {
			fAssocViewer.setInput(getCFileTypeAssociations());
		}
		setDirty(true);
	}

	private void setDirty(boolean dirty) {
		fDirty = dirty;
	}
	
	public boolean performOk() {
		boolean changed = fDirty;
		
		if (fDirty) {
			CFileTypeAssociation[] add = (CFileTypeAssociation[]) fAddAssoc.toArray(new CFileTypeAssociation[fAddAssoc.size()]);
			CFileTypeAssociation[] rem = (CFileTypeAssociation[]) fRemoveAssoc.toArray(new CFileTypeAssociation[fRemoveAssoc.size()]);
			
			changed = add.length > 0 || rem.length > 0;
			adjustAssociations(add, rem);
	
			fAddAssoc.clear();
			fRemoveAssoc.clear();

			setDirty(false);
		}		
		return changed;
	}
	
	private CFileTypeAssociation[] getCFileTypeAssociations() {
		ArrayList list = new ArrayList();
		if (fInput == null) {
			fillWithUserDefinedCFileTypeAssociations(list);
			fillWithPredefinedCFileTypeAssociations(list);
		} else {
			fillWithProjectCFileTypeAssociations(list, fInput);
		}
		CFileTypeAssociation[] assocs = new CFileTypeAssociation[list.size()];
		list.toArray(assocs);
		return assocs;
	}

	protected void adjustAssociations(CFileTypeAssociation[] add, CFileTypeAssociation[] rem) {
		IScopeContext context = null;
		if (fInput != null) {
			context = new ProjectScope(fInput);
		}
		addAssociations(add, context);
		removeAssociations(rem, context);
	}

	protected void addAssociations(CFileTypeAssociation[] add, IScopeContext context) {
		for (int i = 0; i < add.length; ++i) {
			CFileTypeAssociation assoc = add[i];
			String spec = assoc.getSpec();
			IContentType contentType = assoc.getContentType();
			int type = IContentType.FILE_NAME_SPEC;
			if (assoc.isExtSpec()) {
				type = IContentType.FILE_EXTENSION_SPEC;
			}			
			addAssociation(context, contentType, spec, type);
		}
	}

	protected void addAssociation(IScopeContext context, IContentType contentType, String spec, int type) {
		try {
			IContentTypeSettings settings = contentType.getSettings(context);
			settings.addFileSpec(spec, type);
		} catch (CoreException e) {
			// ignore ??
		}
	}

	protected void removeAssociations(CFileTypeAssociation[] rem, IScopeContext context) {
		for (int i = 0; i < rem.length; ++i) {
			CFileTypeAssociation assoc = rem[i];
			IContentType contentType = assoc.getContentType();
			String spec = assoc.getSpec();
			int type = IContentType.FILE_NAME_SPEC;
			if (assoc.isExtSpec()) {
				type = IContentType.FILE_EXTENSION_SPEC;
			}
			removeAssociation(context, contentType, spec, type);
		}
	}

	protected void removeAssociation(IScopeContext context, IContentType contentType, String spec, int type) {
		try {
			IContentTypeSettings settings = contentType.getSettings(context);
			settings.removeFileSpec(spec, type);
		} catch (CoreException e) {
			// ignore ??
		}
	}

	public IContentType[] getRegistedContentTypes() {
		if (fContentTypes == null) {
			String [] ids = CoreModel.getRegistedContentTypeIds();
			IContentTypeManager manager = Platform.getContentTypeManager();
			IContentType [] ctypes = new IContentType[ids.length];
			for (int i = 0; i < ids.length; i++) {
				ctypes[i] = manager.getContentType(ids[i]);
			}
			fContentTypes = ctypes;
		}
		return fContentTypes;
	}

	private void fillWithUserDefinedCFileTypeAssociations(ArrayList list) {
		IContentType[] ctypes = getRegistedContentTypes();
		fillWithCFileTypeAssociations(ctypes, null, IContentType.IGNORE_PRE_DEFINED | IContentType.FILE_EXTENSION_SPEC, list);
		fillWithCFileTypeAssociations(ctypes, null, IContentType.IGNORE_PRE_DEFINED | IContentType.FILE_NAME_SPEC, list);
	}

	private void fillWithPredefinedCFileTypeAssociations(ArrayList list) {
		IContentType[] ctypes = getRegistedContentTypes();
		fillWithCFileTypeAssociations(ctypes, null, IContentType.IGNORE_USER_DEFINED | IContentType.FILE_EXTENSION_SPEC, list);
		fillWithCFileTypeAssociations(ctypes, null, IContentType.IGNORE_USER_DEFINED | IContentType.FILE_NAME_SPEC, list);
	}

	private void fillWithProjectCFileTypeAssociations(ArrayList list, IProject project) {
		IContentType[] ctypes = getRegistedContentTypes();
		IScopeContext context = new ProjectScope(project);
		fillWithCFileTypeAssociations(ctypes, context, IContentType.IGNORE_PRE_DEFINED | IContentType.FILE_EXTENSION_SPEC, list);
		fillWithCFileTypeAssociations(ctypes, context, IContentType.IGNORE_PRE_DEFINED | IContentType.FILE_NAME_SPEC, list);
	}

	private void fillWithCFileTypeAssociations(IContentType[] ctypes, IScopeContext context, int type, ArrayList list) {
		for (int i = 0; i < ctypes.length; i++) {
			try {
				IContentType ctype = ctypes[i];
				IContentTypeSettings setting = ctype.getSettings(context);
				String[] specs = setting.getFileSpecs(type);
				for (int j = 0; j < specs.length; j++) {
					CFileTypeAssociation assoc = new CFileTypeAssociation(specs[j], type, ctype);
					list.add(assoc);
				}
			} catch (CoreException e) {
				// skip over it.
			}
		}
	}

	private CFileTypeAssociation createAssociation(String pattern, IContentType contentType) {
		int type = IContentType.FILE_NAME_SPEC;
		if (pattern.startsWith("*.")) { //$NON-NLS-1$
			pattern = pattern.substring(2);
			type = IContentType.FILE_EXTENSION_SPEC;
		}
		return new CFileTypeAssociation(pattern, type, contentType);
	}

	protected void handleSelectionChanged() {
		IStructuredSelection sel = getSelection();
		if (sel.isEmpty()) {
			fBtnRemove.setEnabled(false);
		} else {
			boolean enabled = true;
			List elements = sel.toList();
			for (Iterator i = elements.iterator(); i.hasNext();) {
				CFileTypeAssociation assoc = (CFileTypeAssociation) i.next();
				if (assoc.isPredefined())
					enabled = false;
			}
			fBtnRemove.setEnabled(enabled);
		}
	}

	protected void handleAdd() {
		CFileTypeAssociation assoc = null;

		CFileTypeDialog dlg = new CFileTypeDialog(fBtnNew.getParent().getShell());
		
		if (Window.OK == dlg.open()) {
			assoc = createAssociation(dlg.getPattern(), dlg.getContentType());
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
				CFileTypeAssociation assoc = (CFileTypeAssociation) iter.next();
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
