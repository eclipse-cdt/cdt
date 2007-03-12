/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.newui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableStatus;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.internal.core.cdtvariables.CdtVariableManager;
import org.eclipse.cdt.internal.core.cdtvariables.EclipseVariablesVariableSupplier;
import org.eclipse.cdt.internal.core.cdtvariables.ICoreVariableContextInfo;
import org.eclipse.cdt.internal.core.cdtvariables.StorableCdtVariables;
import org.eclipse.cdt.internal.core.cdtvariables.UserDefinedVariableSupplier;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.CDTListComparator;
import org.eclipse.cdt.ui.newui.NewUIMessages;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.cdt.utils.envvar.EnvVarOperationProcessor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * displays the build macros for the given context
 */
public class CPropertyVarsTab extends AbstractCPropertyTab {
	/*
	 * String constants
	 */
	private static final String PREFIX = "MacrosBlock";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$

	private static final String HEADER = LABEL + ".header";  //$NON-NLS-1$
	private static final String HEADER_NAME = HEADER + ".name";  //$NON-NLS-1$
	private static final String HEADER_TYPE = HEADER + ".type";  //$NON-NLS-1$
	private static final String HEADER_VALUE = HEADER + ".value";  //$NON-NLS-1$

	private static final String TYPE = LABEL + ".type";	//$NON-NLS-1$
	private static final String TYPE_TEXT = TYPE + ".text";	//$NON-NLS-1$
	private static final String TYPE_TEXT_LIST = TYPE + ".text.list";	//$NON-NLS-1$
	private static final String TYPE_PATH_FILE = TYPE + ".path.file";	//$NON-NLS-1$
	private static final String TYPE_PATH_FILE_LIST = TYPE + ".path.file.list";	//$NON-NLS-1$
	private static final String TYPE_PATH_DIR = TYPE + ".path.dir";	//$NON-NLS-1$
	private static final String TYPE_PATH_DIR_LIST = TYPE + ".path.dir.list";	//$NON-NLS-1$
	private static final String TYPE_PATH_ANY = TYPE + ".path.any";	//$NON-NLS-1$
	private static final String TYPE_PATH_ANY_LIST = TYPE + ".path.any.list";	//$NON-NLS-1$

	private static final String DELETE_CONFIRM_TITLE = LABEL + ".delete.confirm.title";	//$NON-NLS-1$
	private static final String DELETE_CONFIRM_MESSAGE = LABEL + ".delete.confirm.message";	//$NON-NLS-1$

	private static final String DELETE_ALL_CONFIRM_TITLE = LABEL + ".delete.all.confirm.title";	//$NON-NLS-1$
	private static final String DELETE_ALL_CONFIRM_MESSAGE = LABEL + ".delete.all.confirm.message";	//$NON-NLS-1$
	
	private static final String VALUE = LABEL + ".value";	//$NON-NLS-1$
	private static final String VALUE_ECLIPSE_DYNAMIC = VALUE + ".eclipse.dynamic";	//$NON-NLS-1$
	
	private static final String VALUE_DELIMITER = " || ";	//$NON-NLS-1$

	private static final ICdtVariableManager mgr = CCorePlugin.getDefault().getCdtVariableManager();
	private static final UserDefinedVariableSupplier fUserSupplier = CdtVariableManager.fUserDefinedMacroSupplier;

	private ICConfigurationDescription cfgd = null;
	private StorableCdtVariables vars = null;
	
	//currently the "CWD" and "PWD" macros are not displayed in UI
	private static final String fHiddenMacros[] = new String[]{
			"CWD",   //$NON-NLS-1$
			"PWD"	  //$NON-NLS-1$
		};
	
	private boolean fShowSysMacros = false;
	private Set fIncorrectlyDefinedMacrosNames = new HashSet();
	private static final int CONTEXT = ICoreVariableContextInfo.CONTEXT_CONFIGURATION;

	private TableViewer tv;
	private Label fStatusLabel;
	
	private static final String[] fEditableTableColumnProps = new String[] {
		"editable name",	//$NON-NLS-1$
		"editable type",	//$NON-NLS-1$
		"editable value",	//$NON-NLS-1$
	};

	private static final String[] fTableColumnNames = new String[] {
		NewUIMessages.getResourceString(HEADER_NAME),
		NewUIMessages.getResourceString(HEADER_TYPE),
		NewUIMessages.getResourceString(HEADER_VALUE),
	};

	private static final ColumnLayoutData[] fTableColumnLayouts = {new ColumnPixelData(100), new ColumnPixelData(100), new ColumnPixelData(250)};
	
	private class MacroContentProvider implements IStructuredContentProvider{
		public Object[] getElements(Object inputElement) { return (Object[])inputElement; }
		public void dispose() {	}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}
	
	private class MacroLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider , ITableFontProvider, IColorProvider{
		public Image getImage(Object element) { return null; }
		public String getText(Object element) { return getColumnText(element, 0); }
		public Font getFont(Object element) { return getFont(element, 0); }
		public Image getColumnImage(Object element, int columnIndex) { return null; }
		public Color getBackground(Object element){	return null; }
		
		public String getColumnText(Object element, int columnIndex) {
			ICdtVariable var = (ICdtVariable)element;
			switch(columnIndex){
			case 0:
				return var.getName();
			case 1:
				switch(var.getValueType()){
				case ICdtVariable.VALUE_PATH_FILE:
					return NewUIMessages.getResourceString(TYPE_PATH_FILE);
				case ICdtVariable.VALUE_PATH_FILE_LIST:
					return NewUIMessages.getResourceString(TYPE_PATH_FILE_LIST);
				case ICdtVariable.VALUE_PATH_DIR:
					return NewUIMessages.getResourceString(TYPE_PATH_DIR);
				case ICdtVariable.VALUE_PATH_DIR_LIST:
					return NewUIMessages.getResourceString(TYPE_PATH_DIR_LIST);
				case ICdtVariable.VALUE_PATH_ANY:
					return NewUIMessages.getResourceString(TYPE_PATH_ANY);
				case ICdtVariable.VALUE_PATH_ANY_LIST:
					return NewUIMessages.getResourceString(TYPE_PATH_ANY_LIST);
				case ICdtVariable.VALUE_TEXT:
					return NewUIMessages.getResourceString(TYPE_TEXT);
				case ICdtVariable.VALUE_TEXT_LIST:
					return NewUIMessages.getResourceString(TYPE_TEXT_LIST);
				default:
					return "? " + var.getValueType();   //$NON-NLS-1$
				}
			case 2:
				return getString(var);  
			}
			return EMPTY_STR;
		}
		
		private Font getValueFont(ICdtVariable var){
			Font font = null;
			if(isUserVar(var))
				font = JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
			return font;
		}
		public Font getFont(Object element, int columnIndex) {
			ICdtVariable var = (ICdtVariable)element;
			switch(columnIndex){
			case 0:
			case 1:
				break;
			case 2:
				return getValueFont(var);
			}
			if(isUserVar(var))
				return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
			return null;
		}
	    public Color getForeground(Object element){
			if(fIncorrectlyDefinedMacrosNames.contains(((ICdtVariable)element).getName()))
				return JFaceResources.getColorRegistry().get(JFacePreferences.ERROR_COLOR);
			return null;
	    }
	}

	/*
	 * called when the user macro selection was changed 
	 */
	private void handleSelectionChanged(SelectionChangedEvent event){
		updateButtons();
	}
	
	private void updateButtons() {
		Object[] obs = ((IStructuredSelection)tv.getSelection()).toArray();
		boolean canEdit = false;
		boolean canDel  = false;
		if (obs != null && obs.length > 0) {
			canEdit = (obs.length == 1);
			for (int i=0; i<obs.length; i++) {
				if (obs[i] instanceof ICdtVariable && isUserVar((ICdtVariable)obs[i])) { 
					canDel = true;
					break;
				}
			}
		}
		buttonSetEnabled(1, canEdit);
		buttonSetEnabled(2, canDel);
	}
	
	/*
	 * called when a custom button was pressed
	 */
	public void buttonPressed(int index){
		switch(index){
		case 0:{
			NewVarDialog dlg = new NewVarDialog(usercomp.getShell(), null, cfgd);
			if(dlg.open() == Dialog.OK){
				ICdtVariable macro = dlg.getDefinedMacro();
				if(canCreate(macro)) {
					if (cfgd != null) {
						if (dlg.isForAllCfgs) {
							ICConfigurationDescription[] cfgs = page.getCfgsEditable();
							for (int k=0; k<cfgs.length; k++) 
								fUserSupplier.createMacro(macro, CONTEXT, cfgs[k]);
						} else 
							fUserSupplier.createMacro(macro, CONTEXT, cfgd);
					} 
					else if (vars != null)
						vars.createMacro(macro);
					updateData();
				}
			}
		}
		break;
		case 1:{
			ICdtVariable _vars[] = getSelectedUserMacros();
			if(_vars != null && _vars.length == 1){
				NewVarDialog dlg = new NewVarDialog(usercomp.getShell() ,_vars[0], cfgd);
				if(dlg.open() == Dialog.OK){
					ICdtVariable macro = dlg.getDefinedMacro();
					if(canCreate(macro)) {
						if (cfgd != null)
							fUserSupplier.createMacro(macro, CONTEXT, cfgd);
						else if (vars != null)
							vars.createMacro(macro);
						updateData();
					}
				}
			}
		}
		break;
		case 2:{
			ICdtVariable macros[] = getSelectedUserMacros();
			if(macros != null && macros.length > 0){
				if(MessageDialog.openQuestion(usercomp.getShell(),
						NewUIMessages.getResourceString(DELETE_CONFIRM_TITLE),
						NewUIMessages.getResourceString(DELETE_CONFIRM_MESSAGE))){
					for(int i = 0; i < macros.length; i++){
						if (cfgd != null)
							fUserSupplier.deleteMacro(macros[i].getName(), CONTEXT, cfgd);
						else if (vars != null)
							vars.deleteMacro(macros[i].getName());
					}
					updateData();
				}
			}
		}
		break;
		}
	}
	
	/*
	 * returnes the selected user-defined macros
	 */
	private ICdtVariable[] getSelectedUserMacros(){
		if(tv == null)	return null;
		List list = ((IStructuredSelection)tv.getSelection()).toList();
		return (ICdtVariable[])list.toArray(new ICdtVariable[list.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	protected void performDefaults() {
		if(MessageDialog.openQuestion(usercomp.getShell(),
				NewUIMessages.getResourceString(DELETE_ALL_CONFIRM_TITLE),
				NewUIMessages.getResourceString(DELETE_ALL_CONFIRM_MESSAGE))){
			if (cfgd != null)			
				fUserSupplier.deleteAll(CONTEXT, cfgd);
			else if (vars != null)
				vars.deleteAll();
			updateData();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControls(Composite parent) {
		super.createControls(parent);
		initButtons(new String[] {ADD_STR, EDIT_STR, DEL_STR});
		usercomp.setLayout(new GridLayout(1, false));
		createTableControl();
		
		// Create a "show parent levels" button 
		final Button b = new Button(usercomp, SWT.CHECK);
		b.setFont(usercomp.getFont());
		b.setText(Messages.getString("CPropertyVarsTab.0")); //$NON-NLS-1$
		b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b.setSelection(fShowSysMacros);
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fShowSysMacros = b.getSelection();
				updateData(getResDesc());
			}
		});
//		if (page.isForPrefs()) b.setVisible(false);
		
		fStatusLabel = new Label(usercomp, SWT.LEFT);
		fStatusLabel.setFont(usercomp.getFont());
		fStatusLabel.setText(EMPTY_STR);
		fStatusLabel.setLayoutData(new GridData(GridData.BEGINNING));
		fStatusLabel.setForeground(JFaceResources.getColorRegistry().get(JFacePreferences.ERROR_COLOR));
	}
	
	private void createTableControl(){
		TableViewer tableViewer;
		tableViewer = new TableViewer(usercomp, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
				| SWT.FULL_SELECTION);
		
		Table table = tableViewer.getTable();
		TableLayout tableLayout = new TableLayout();
		for (int i = 0; i < fTableColumnNames.length; i++) {
			tableLayout.addColumnData(fTableColumnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			tc.setResizable(fTableColumnLayouts[i].resizable);
			tc.setText(fTableColumnNames[i]);
		}
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData gd = new GridData(GridData.FILL_BOTH);
		tableViewer.getControl().setLayoutData(gd);
		tableViewer.setContentProvider(new MacroContentProvider());
		tableViewer.setLabelProvider(new MacroLabelProvider());
		tableViewer.setSorter(new ViewerSorter());
		
		tableViewer.setColumnProperties(fEditableTableColumnProps);
		tv = tableViewer;
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
	
			public void doubleClick(DoubleClickEvent event) {
				if (!tv.getSelection().isEmpty()) {	buttonPressed(1); }
			}
		});
	
		table.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e){
				if(e.keyCode == SWT.DEL) buttonPressed(2);
			}

			public void keyReleased(KeyEvent e){}
		});
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	/*
	 * answers whether the macro of a given name can be sreated
	 */
	private boolean canCreate(ICdtVariable v){
		if (v == null) return false;
		String name = v.getName();
		if(name == null || (name = name.trim()).length() == 0)
			return false;
		if(fHiddenMacros != null){
			for(int i = 0; i < fHiddenMacros.length; i++){
				if(fHiddenMacros[i].equals(EnvVarOperationProcessor.normalizeName(name)))
					return false;
			}
		}
		return true; 
	}
		
	public void updateData(ICResourceDescription _cfgd) {
		if (_cfgd == null) {
			cfgd = null;
			if (vars == null)
				vars = fUserSupplier.getWorkspaceVariablesCopy();
		} else {
			cfgd = _cfgd.getConfiguration();
			vars = null;
		}
		updateData();
	}
	
	private void updateData() {
		if(tv == null) return;
			
		// check integrity
		try{
			mgr.checkVariableIntegrity(cfgd);
			updateState(null);
		} catch (CdtVariableException e){
			updateState(e);
		}
		// get variables
		ICdtVariable[] _vars = mgr.getVariables(cfgd);
		if (_vars == null) return;
		
		if (cfgd == null) {
			if (fShowSysMacros) {
				List lst = new ArrayList(_vars.length);
				ICdtVariable[] uvars = vars.getMacros();
				for (int i=0; i<uvars.length; i++) {
					lst.add(uvars[i]);
					for (int j=0; j<_vars.length; j++) {
						if (_vars[j] != null && _vars[j].getName().equals(uvars[i].getName())) {
							_vars[j] = null;
							break;
						}
					}
				}
				// add system vars not rewritten by user's
				for (int j=0; j<_vars.length; j++) {
					if (_vars[j] != null && !mgr.isUserVariable(_vars[j], null)) 
						lst.add(_vars[j]);
				}
				_vars = (ICdtVariable[])lst.toArray(new ICdtVariable[lst.size()]);
			} else {
				_vars = vars.getMacros();
			}
		}
		
		ArrayList list = new ArrayList(_vars.length);
		for(int i = 0; i < _vars.length; i++){
			if(_vars[i] != null && (fShowSysMacros || isUserVar(_vars[i]))) 
				list.add(_vars[i]);
		}
		Collections.sort(list, CDTListComparator.getInstance());
		tv.setInput(list.toArray(new ICdtVariable[list.size()]));
		updateButtons();
	}
	
	private void updateState(CdtVariableException e){
		fIncorrectlyDefinedMacrosNames.clear();
		if(e != null){
			fStatusLabel.setText(e.getMessage());
			fStatusLabel.setVisible(true);
			ICdtVariableStatus statuses[] = e.getVariableStatuses();
			for(int i = 0; i < statuses.length; i++){
				String name = statuses[i].getVariableName();
				if(name != null)
					fIncorrectlyDefinedMacrosNames.add(name);
			}
		}
		else
			fStatusLabel.setVisible(false);
	}
	
	private boolean isUserVar(ICdtVariable v) {
		return cfgd != null ? mgr.isUserVariable(v, cfgd) : vars.contains(v);
	}
	
	/* check whether variable is dynamic */
	private boolean isDynamic(ICdtVariable v) {
		if (v instanceof EclipseVariablesVariableSupplier.EclipseVarMacro) {
			EclipseVariablesVariableSupplier.EclipseVarMacro evar =
				(EclipseVariablesVariableSupplier.EclipseVarMacro)v;
			if (evar.getVariable() instanceof IDynamicVariable)
				return true;
		}		
		return false;
	}
	
	private String getString(ICdtVariable v) {
		if (isDynamic(v)) 
			return NewUIMessages.getResourceString(VALUE_ECLIPSE_DYNAMIC);
		String value = EMPTY_STR; 
		try {			
			if (CdtVariableResolver.isStringListVariable(v.getValueType()))
				value = mgr.convertStringListToString(v.getStringListValue(), VALUE_DELIMITER);
			else
				value = v.getStringValue();
		} catch (CdtVariableException e1) {}
		return value;
	}
	
	public void performApply(ICResourceDescription src, ICResourceDescription dst) {
		if (cfgd != null) // only for project, not for prefs 
			fUserSupplier.setMacros(fUserSupplier.getMacros(CONTEXT, src), CONTEXT, dst);
		else if (vars != null)
			fUserSupplier.storeWorkspaceVariables(true);
	}

	/**
	 * Unlike other pages, workspace variables 
	 * should be stored explicitly on "OK".  
	 */
	protected void performOK() {
		if (vars != null) try {
			fUserSupplier.setWorkspaceVariables(vars);
		} catch (CoreException e) {}
		vars = null;
		super.performOK();
	}
	protected void performCancel() {
		vars = null;
		super.performCancel();
	}

	// This page can be displayed for project only
	public boolean canBeVisible() {
		return page.isForProject() || page.isForPrefs();
	}
}
