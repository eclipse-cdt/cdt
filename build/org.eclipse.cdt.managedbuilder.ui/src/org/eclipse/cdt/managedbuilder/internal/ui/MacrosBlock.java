/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.envvar.EnvVarOperationProcessor;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacro;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroContextInfo;
import org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroSubstitutor;
import org.eclipse.cdt.managedbuilder.internal.macros.EclipseVariablesMacroSupplier;
import org.eclipse.cdt.managedbuilder.internal.macros.EnvironmentMacroSupplier;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo;
import org.eclipse.cdt.managedbuilder.internal.macros.MacroResolver;
import org.eclipse.cdt.managedbuilder.internal.macros.UserDefinedMacroSupplier;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroStatus;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier;
import org.eclipse.cdt.managedbuilder.ui.properties.BuildPreferencePage;
import org.eclipse.cdt.managedbuilder.ui.properties.BuildPropertyPage;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * displays the build macros for the given context
 */
public class MacrosBlock extends AbstractCOptionPage {
	/*
	 * String constants
	 */
	private static final String PREFIX = "MacrosBlock";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$

	private static final String USER_MACROS = LABEL + ".user.macros";	//$NON-NLS-1$
	private static final String SYSTEM_MACROS = LABEL + ".system.macros";	//$NON-NLS-1$

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

	
	private static final String BUTTON = LABEL + ".button";	//$NON-NLS-1$
	private static final String BUTTON_NEW = BUTTON + ".new";	//$NON-NLS-1$
	private static final String BUTTON_EDIT = BUTTON + ".edit";	//$NON-NLS-1$
	private static final String BUTTON_DELETE = BUTTON + ".delete";	//$NON-NLS-1$

	private static final String BUTTON_CHECK_SHOW_PARENT = BUTTON + ".check.chow.parent";	//$NON-NLS-1$
	
	private static final String DELETE_CONFIRM_TITLE = LABEL + ".delete.confirm.title";	//$NON-NLS-1$
	private static final String DELETE_CONFIRM_MESSAGE = LABEL + ".delete.confirm.message";	//$NON-NLS-1$

	private static final String DELETE_ALL_CONFIRM_TITLE = LABEL + ".delete.all.confirm.title";	//$NON-NLS-1$
	private static final String DELETE_ALL_CONFIRM_MESSAGE = LABEL + ".delete.all.confirm.message";	//$NON-NLS-1$
	
	private static final String VALUE = LABEL + ".value";	//$NON-NLS-1$
	private static final String VALUE_ECLIPSE_DYNAMIC = VALUE + ".eclipse.dynamic";	//$NON-NLS-1$
	
	
	private static final String VALUE_DELIMITER = " || ";	//$NON-NLS-1$
	
	
	private static final String fHiddenMacros[] = new String[]{
			//currently the "CWD" and "PWD" macros are not displayed in UI
			"CWD",   //$NON-NLS-1$
			"PWD"	  //$NON-NLS-1$
		};
	
	/*
	 * button IDs
	 */
	private static final int IDX_BUTTON_NEW = 0;
	private static final int IDX_BUTTON_EDIT = 1;
	private static final int IDX_BUTTON_DELETE = 2;

	//macro names deleted by a user
	private HashSet fDeletedUserMacroNames;
	//macros added by a user
	private Map fAddedUserMacros;
	//specifies whether a "delete All" button was previousely pressed 
	private boolean fDeleteAll = false;
	//specifies whether the set of the user-defined macros was changed by a user
	//and the changes are not applied to the User Macro Supplier 
	private boolean fModified = false;
	//holds the visible state. 
	private boolean fVisible = false;
	//specifies whether the "show parent context macros" checkbox should be created
	private boolean fShowParentViewCheckBox = true;
	//inexistent context
	private static final Object fInexistentContext = new Object();
	//the context tyte for which the macros are displayed
	private int fContextType = 0;
	//the context data for which the macros are displayed
	private Object fContextData = fInexistentContext;
	//specifies whether the parent level macros should be displayed
	private boolean fShowParentMacros = false;

	private IMacroContextInfo fSystemContextInfo;
	private IMacroContextInfo fCurrentContextInfo;
	private IMacroContextInfo fParentContextInfo;
	private boolean fUseDefaultParentContextInfo = true;
	
	private Set fIncorrectlyDefinedMacrosNames = new HashSet();
	
	//the user defined macro supplier
	private UserDefinedMacroSupplier fUserSupplier;
	//editable list that displayes user-defined macros
	private ListDialogField fEditableList;
	//non-editable list that holds all macros other than user-defined ones
	private ListDialogField fNonEditableList;
	
	/*
	 * widgets
	 */
	//show parent level macros check-box
	private Button fShowParentButton;
	//parent composite
	private Composite fParent;
	//status label
	private Label fStatusLabel;

	
	private class MacroUIMacroSubstitutor extends DefaultMacroSubstitutor{
		public MacroUIMacroSubstitutor(IMacroContextInfo contextInfo, String inexistentMacroValue, String listDelimiter) {
			super(contextInfo, inexistentMacroValue, listDelimiter);
		}
		
		public MacroUIMacroSubstitutor(int contextType, Object contextData, String inexistentMacroValue, String listDelimiter){
			super(contextType, contextData, inexistentMacroValue, listDelimiter);
		}

		protected ResolvedMacro resolveMacro(String macroName) throws BuildMacroException{
			String names[] = BuildMacroProvider.fMbsMacroSupplier.getMacroNames(IBuildMacroProvider.CONTEXT_FILE);
			for(int i = 0; i < names.length; i++){
				if(macroName.equals(names[i]))
					return new ResolvedMacro(macroName,MacroResolver.createMacroReference(macroName));
			}
			names = BuildMacroProvider.fMbsMacroSupplier.getMacroNames(IBuildMacroProvider.CONTEXT_OPTION);
			for(int i = 0; i < names.length; i++){
				if(macroName.equals(names[i]))
					return new ResolvedMacro(macroName,MacroResolver.createMacroReference(macroName));
			}
			return super.resolveMacro(macroName);
		}

		protected ResolvedMacro resolveMacro(IBuildMacro macro) throws BuildMacroException{
			if(macro instanceof EclipseVariablesMacroSupplier.EclipseVarMacro){
				EclipseVariablesMacroSupplier.EclipseVarMacro eclipseVarMacro = 
					(EclipseVariablesMacroSupplier.EclipseVarMacro)macro;
				IStringVariable var = eclipseVarMacro.getVariable();
				String value = null;
				if(var instanceof IDynamicVariable){
					value = "dynamic<" + var.getName() + ">"; //$NON-NLS-1$//$NON-NLS-2$
				} else {
					value = macro.getStringValue();
				}
				return new ResolvedMacro(macro.getName(),value);
			} 
			return super.resolveMacro(macro);
		}
	}
	
	private class MacroUIMacroSontextInfo extends DefaultMacroContextInfo{
		protected MacroUIMacroSontextInfo(int contextType, Object contextData){
			super(contextType,contextData);
		}

		protected  IBuildMacroSupplier[] getSuppliers(int contextType, Object contextData){
			IBuildMacroSupplier suppliers[] = super.getSuppliers(contextType,contextData);
			if(suppliers == null || suppliers.length == 0)
				return null;
			IEnvironmentVariableProvider envProvider = obtainEnvironmentVariableProvider();
			if(envProvider != null){
				for(int i = 0; i < suppliers.length; i++){
					if((suppliers[i] instanceof EnvironmentMacroSupplier)){
						suppliers[i] = new EnvironmentMacroSupplier(envProvider);
						break;
					}
				}
			}
			return suppliers;
		}

	}
	
	private class SystemContextInfo extends MacroUIMacroSontextInfo{
		protected SystemContextInfo(int contextType, Object contextData){
			super(contextType,contextData);
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroContextInfo#getSuppliers(int, java.lang.Object)
		 */
		protected  IBuildMacroSupplier[] getSuppliers(int contextType, Object contextData){
			IBuildMacroSupplier suppliers[] = super.getSuppliers(contextType,contextData);
			if(suppliers == null || suppliers.length == 0)
				return null;
			
			List list = new ArrayList();
			for(int i = 0; i < suppliers.length; i++){
				if(!(suppliers[i] instanceof UserDefinedMacroSupplier))
					list.add(suppliers[i]);
			}
			
			return (IBuildMacroSupplier[])list.toArray(new IBuildMacroSupplier[list.size()]);

		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo#getNext()
		 */
		public IMacroContextInfo getNext(){
			if(fUseDefaultParentContextInfo)
				return super.getNext();
			return fParentContextInfo;
		}
	}
	
	private class CurrentContextInfo extends MacroUIMacroSontextInfo{
		protected CurrentContextInfo(int contextType, Object contextData){
			super(contextType,contextData);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroContextInfo#getSuppliers(int, java.lang.Object)
		 */
		public IBuildMacroSupplier[] getSuppliers(int contextType, Object contextData){
			IBuildMacroSupplier suppliers[] = super.getSuppliers(contextType,contextData);
			if(fParentContextInfo == null){
				int i = 0;
			}
			if(suppliers == null || suppliers.length == 0)
				return suppliers;
			if(!(suppliers[0] instanceof UserDefinedMacroSupplier))
				return suppliers;
			
			List list = new ArrayList(suppliers.length);
			list.add(new UIMacroSupplier());
			
			for(int i = 1; i < suppliers.length; i++){
					list.add(suppliers[i]);
			}
			
			return (IBuildMacroSupplier[])list.toArray(new IBuildMacroSupplier[list.size()]);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo#getNext()
		 */
		public IMacroContextInfo getNext(){
			if(fUseDefaultParentContextInfo)
				return super.getNext();
			return fParentContextInfo;
		}
	}
	
	private class UIMacroSupplier implements IBuildMacroSupplier{
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacro(java.lang.String, int, java.lang.Object)
		 */
		public IBuildMacro getMacro(String name, int contextType, Object contextData){
			if(contextType != fContextType || contextData != fContextData)
				return null;
			
			return (IBuildMacro)getUserMacros().get(name);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacros(int, java.lang.Object)
		 */
		public IBuildMacro[] getMacros(int contextType, Object contextData){
			if(contextType != fContextType || contextData != fContextData)
				return null;
			
			Collection macros = getUserMacros().values(); 
			return (IBuildMacro[])macros.toArray(new IBuildMacro[macros.size()]);
		}
	}

	public class ListAdapter implements IListAdapter, IDialogFieldListener {
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter#customButtonPressed(org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField, int)
		 */
		public void customButtonPressed(ListDialogField field, int index) {
			if(field == fEditableList)
				handleCustomButtonPressed(index);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter#selectionChanged(org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField)
		 */
		public void selectionChanged(ListDialogField field) {
			handleSelectionChanged(field);
		}
			
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter#doubleClicked(org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField)
		 */
		public void doubleClicked(ListDialogField field) {
			if (isEditable(field) && field.getSelectedElements().size() == 1) 
				handleCustomButtonPressed(IDX_BUTTON_EDIT);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener#dialogFieldChanged(org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField)
		 */
		public void dialogFieldChanged(DialogField field) {
		}			
	}
	
	private class MacroLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider , ITableFontProvider, IColorProvider{
		private boolean fUser;
		public MacroLabelProvider(boolean user){
			fUser = user;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			return null; // JavaPluginImages.get(JavaPluginImages.IMG_OBJS_REFACTORING_INFO);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			return getColumnText(element, 0);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			IBuildMacro macro = (IBuildMacro)element;
			switch(columnIndex){
			case 0:
				return macro.getName();
			case 1:
				switch(macro.getMacroValueType()){
				case IBuildMacro.VALUE_PATH_FILE:
					return ManagedBuilderUIMessages.getResourceString(TYPE_PATH_FILE);
				case IBuildMacro.VALUE_PATH_FILE_LIST:
					return ManagedBuilderUIMessages.getResourceString(TYPE_PATH_FILE_LIST);
				case IBuildMacro.VALUE_PATH_DIR:
					return ManagedBuilderUIMessages.getResourceString(TYPE_PATH_DIR);
				case IBuildMacro.VALUE_PATH_DIR_LIST:
					return ManagedBuilderUIMessages.getResourceString(TYPE_PATH_DIR_LIST);
				case IBuildMacro.VALUE_PATH_ANY:
					return ManagedBuilderUIMessages.getResourceString(TYPE_PATH_ANY);
				case IBuildMacro.VALUE_PATH_ANY_LIST:
					return ManagedBuilderUIMessages.getResourceString(TYPE_PATH_ANY_LIST);
				case IBuildMacro.VALUE_TEXT:
					return ManagedBuilderUIMessages.getResourceString(TYPE_TEXT);
				case IBuildMacro.VALUE_TEXT_LIST:
					return ManagedBuilderUIMessages.getResourceString(TYPE_TEXT_LIST);
				default:
				}
			case 2:
				return getDisplayedMacroValue(macro);  //$NON-NLS-1$
			}
			return "";  //$NON-NLS-1$
		}
		
		private String getDisplayedMacroValue(IBuildMacro macro){
			String value = "";  //$NON-NLS-1$
			try{
				if(macro instanceof EclipseVariablesMacroSupplier.EclipseVarMacro){
					EclipseVariablesMacroSupplier.EclipseVarMacro eclipseVarMacro = 
						(EclipseVariablesMacroSupplier.EclipseVarMacro)macro;
					IStringVariable var = eclipseVarMacro.getVariable();
					if(var instanceof IDynamicVariable){
						value = ManagedBuilderUIMessages.getResourceString(VALUE_ECLIPSE_DYNAMIC);
					} else {
						value = macro.getStringValue();
					}
				} else {
					if(MacroResolver.isStringListMacro(macro.getMacroValueType()))
						value = BuildMacroProvider.getDefault().convertStringListToString(macro.getStringListValue(),VALUE_DELIMITER);
					else
						value = macro.getStringValue(); 
					
				}
			}catch (BuildMacroException e){
			}
			return value;
		}
		
		private Font getValueFont(IBuildMacro macro){
			Font font = null;
			if(macro instanceof EclipseVariablesMacroSupplier.EclipseVarMacro){
				EclipseVariablesMacroSupplier.EclipseVarMacro eclipseVarMacro = 
					(EclipseVariablesMacroSupplier.EclipseVarMacro)macro;
				IStringVariable var = eclipseVarMacro.getVariable();
				if(var instanceof IDynamicVariable){
					font = JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
				}
			}

			return font;
		}
		

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
		 */
		public Font getFont(Object element) {
			return getFont(element, 0);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object, int)
		 */
		public Font getFont(Object element, int columnIndex) {
			IBuildMacro macro = (IBuildMacro)element;

			switch(columnIndex){
			case 0:
			case 1:
				break;
			case 2:
				return getValueFont(macro);
			}

			if(!fUser && getUserMacro(macro.getName()) != null)
				return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);

			return null;
		}
		
	    /* (non-Javadoc)
	     * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	     */
	    public Color getForeground(Object element){
			IBuildMacro macro = (IBuildMacro)element;
			boolean incorrect = false;
			String name = macro.getName();
			if(fUser || getUserMacro(name) == null)
				incorrect = fIncorrectlyDefinedMacrosNames.contains(name);
			
			if(incorrect)
				return JFaceResources.getColorRegistry().get(JFacePreferences.ERROR_COLOR);
			return null;
	    }


		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
		 */
		public Color getBackground(Object element){
			return null;
	    }
	}

	/*
	 * constructor
	 */
	public MacrosBlock(ICOptionContainer parent, String title, boolean editable, boolean showParentViewCheckBox){
		super(title);
		super.setContainer(parent);
		
		ListAdapter adapter = new ListAdapter();
		
		if(editable){
			String[] buttons= new String[] {
					ManagedBuilderUIMessages.getResourceString(BUTTON_NEW),
					ManagedBuilderUIMessages.getResourceString(BUTTON_EDIT),
					ManagedBuilderUIMessages.getResourceString(BUTTON_DELETE),
				};

			fEditableList= new ListDialogField(adapter, buttons, new MacroLabelProvider(true));
			fEditableList.setDialogFieldListener(adapter);
			
			String[] columnsHeaders= new String[] {
				ManagedBuilderUIMessages.getResourceString(HEADER_NAME),
				ManagedBuilderUIMessages.getResourceString(HEADER_TYPE),
				ManagedBuilderUIMessages.getResourceString(HEADER_VALUE),
			};
			
			fEditableList.setTableColumns(new ListDialogField.ColumnsDescription(columnsHeaders, true));
			fEditableList.setViewerSorter(new ViewerSorter());


		}
	
			//create non-editable list
			fNonEditableList= new ListDialogField(adapter, null, new MacroLabelProvider(false));
			fNonEditableList.setDialogFieldListener(adapter);
			
			String[] columnsHeaders= new String[] {
				ManagedBuilderUIMessages.getResourceString(HEADER_NAME),
				ManagedBuilderUIMessages.getResourceString(HEADER_TYPE),
				ManagedBuilderUIMessages.getResourceString(HEADER_VALUE),
			};
			
			fNonEditableList.setTableColumns(new ListDialogField.ColumnsDescription(columnsHeaders, true));
			fNonEditableList.setViewerSorter(new ViewerSorter());
			
			fShowParentViewCheckBox = showParentViewCheckBox;

	}
	
	/*
	 * returns the map containing the user-defined macros
	 */
	private Map getUserMacros(){
		if(fUserSupplier == null)
			return null;
		Map map = new HashMap();
		
		if(!fDeleteAll){
			IBuildMacro macros[] = fUserSupplier.getMacros(fContextType,fContextData);
			if(macros != null) {
				for(int i = 0; i < macros.length; i++){
					String name = macros[i].getName();
					map.put(name,macros[i]);
				}
			}
			
			Iterator iter = getDeletedUserMacroNames().iterator();
			while(iter.hasNext()){
				map.remove((String)iter.next());
			}
			
			iter = getAddedUserMacros().values().iterator();
			while(iter.hasNext()){
				IBuildMacro macro = (IBuildMacro)iter.next();
				String name = macro.getName(); 
				map.put(name,macro);
			}
		}
		return map;
	}
	
	/*
	 * returns the HashSet holding the names of the user-deleted macros
	 */
	private HashSet getDeletedUserMacroNames(){
		if(fDeletedUserMacroNames == null)
			fDeletedUserMacroNames = new HashSet();
		return fDeletedUserMacroNames;
	}
	
	/*
	 * returns the map holding user-created/modified macros
	 */
	private Map getAddedUserMacros(){
		if(fAddedUserMacros == null)
			fAddedUserMacros = new HashMap();
		return fAddedUserMacros;
	}

	/*
	 * creates a user macro
	 * the macros created are stored in the fAddedUserMacros Map, and are not actually added to the user supplier
	 * the applyUserMacros() should be called to store those macros to the user supplier
	 */
	private void addUserMacro(String name, int type, String value){
		if(!canCreate(name))
			return;
		fDeleteAll = false;
		BuildMacro newMacro = new BuildMacro(name,type,value);
		getDeletedUserMacroNames().remove(name);
		getAddedUserMacros().put(name,newMacro);
		
		fModified = true;
	}

	/*
	 * creates a user macro
	 * the macros created are stored in the fAddedUserMacros Map, and are not actually added to the user supplier
	 * the applyUserMacros() should be called to store those macros to the user supplier
	 */
	private void addUserMacro(IBuildMacro newMacro){
		String name = newMacro.getName();
		if(!canCreate(name))
			return;
		fDeleteAll = false;
		getDeletedUserMacroNames().remove(name);
		getAddedUserMacros().put(name,newMacro);
		
		fModified = true;
	}

	/*
	 * creates a user macro
	 * the macros created are stored in the fAddedUserMacros Map, and are not actually added to the user supplier
	 * the applyUserMacros() should be called to store those macros to the user supplier
	 */
	private void addUserMacro(String name, int type, String value[]){
		if(!canCreate(name))
			return;
		fDeleteAll = false;
		BuildMacro newMacro = new BuildMacro(name,type,value);
		getDeletedUserMacroNames().remove(name);
		getAddedUserMacros().put(name,newMacro);
		
		fModified = true;
	}

	/*
	 * deletes a user macro
	 * the macros deleted are stored in the fDeletedUserMacroNames HashSet, and are not actually deleted from the user supplier
	 * the applyUserMacros() should be called to delete those macros from the user supplier
	 */
	private void deleteUserMacro(String name){
		fDeleteAll = false;
		getAddedUserMacros().remove(name);
		getDeletedUserMacroNames().add(name);
		
		fModified = true;
	}
	
	/*
	 * deletes all user macros
	 * the applyUserMacros() should be called to delete those macros from the user supplier
	 */
	private void deleteAllUserMacros(){
		fDeleteAll = true;
		getDeletedUserMacroNames().clear();
		getAddedUserMacros().clear();
		
		fModified = true;
	}
	
	/*
	 * returns whether the user macros were modified
	 */
	public boolean isModified(){
		return fModified;
	}

	/*
	 * sets the modify state
	 */
	public void setModified(boolean modified){
		fModified = modified;
	}
	
	/*
	 * returns a user macro of a given name
	 */
	private IBuildMacro getUserMacro(String name){
		Map macros = getUserMacros();
		if(macros == null)
			return null;

		return (IBuildMacro)macros.get(name);
	}

	/*
	 * applies user macros.
	 * 
	 */
	private void applyUserMacros(){
		if(fUserSupplier != null){
			if(fDeleteAll){
				fUserSupplier.deleteAll(fContextType,fContextData);
			}
			else{
				Iterator iter = getDeletedUserMacroNames().iterator();
				while(iter.hasNext()){
					fUserSupplier.deleteMacro((String)iter.next(),fContextType,fContextData);
				}
				
				iter = getAddedUserMacros().values().iterator();
				while(iter.hasNext()){
					IBuildMacro macro = (IBuildMacro)iter.next();
					fUserSupplier.createMacro(macro,fContextType,fContextData);
				}
				
				getDeletedUserMacroNames().clear();
				getAddedUserMacros().clear();
			}
		}
	}
	
	/*
	 * applies user macros and asks the user supplier to serialize
	 */
	private void storeUserMacros(){
		applyUserMacros();
		if(fUserSupplier != null)
			fUserSupplier.serialize(false);
	}

	/*
	 * called when the user macro selection was changed 
	 */
	private void handleSelectionChanged(ListDialogField field){
		if(isEditable(field)){
			List selectedElements= field.getSelectedElements();
			field.enableButton(IDX_BUTTON_EDIT, selectedElements.size() == 1);
			field.enableButton(IDX_BUTTON_DELETE, selectedElements.size() > 0);
		}
	}
	
	/*
	 * answers whether the list values can be edited
	 */
	private boolean isEditable(ListDialogField field) {
		return field == fEditableList;
	}

	/*
	 * called when a custom button was pressed
	 */
	private void handleCustomButtonPressed(int index){

		switch(index){
		case IDX_BUTTON_NEW:{
			NewBuildMacroDialog dlg = new NewBuildMacroDialog(fParent.getShell(),this,null);
			if(dlg.open() == Dialog.OK){
				IBuildMacro macro = dlg.getDefinedMacro();
				if(macro != null){
					addUserMacro(macro);
					updateValues();
				}
			}
		}
		break;
		case IDX_BUTTON_EDIT:{
			IBuildMacro macros[] = getSelectedUserMacros();
			if(macros != null && macros.length == 1){
				NewBuildMacroDialog dlg = new NewBuildMacroDialog(fParent.getShell(),this,getUserMacro(macros[0].getName()));
				if(dlg.open() == Dialog.OK){
					IBuildMacro macro = dlg.getDefinedMacro();
					if(macro != null){
						addUserMacro(macro);
						updateValues();
					}
				}
			}
		}
		break;
		case IDX_BUTTON_DELETE:{
			IBuildMacro macros[] = getSelectedUserMacros();
			if(macros != null && macros.length > 0){
				if(MessageDialog.openQuestion(fParent.getShell(),
						ManagedBuilderUIMessages.getResourceString(DELETE_CONFIRM_TITLE),
						ManagedBuilderUIMessages.getResourceString(DELETE_CONFIRM_MESSAGE))){
					for(int i = 0; i < macros.length; i++){
						deleteUserMacro(macros[i].getName());
					}
					updateValues();
				}
			}
		}
		break;
		}

	}
	
	/*
	 * returnes the selected user-defined macros
	 */
	private IBuildMacro[] getSelectedUserMacros(){
		if(fEditableList == null)
			return null;
		
		List list = fEditableList.getSelectedElements();
		return (IBuildMacro[])list.toArray(new IBuildMacro[list.size()]);
	}

	/*
	 * sets the context for which the macros should be displayed
	 */
	public void setContext(int contextType, Object contextData){
		if(contextType == fContextType && contextData == fContextData)
			return;
	
		fContextType = contextType;
		fContextData = contextData;

		IBuildMacroProvider provider = BuildMacroProvider.getDefault();
		IBuildMacroSupplier suppliers[] = provider.getSuppliers(fContextType, fContextData);
		if(suppliers != null && suppliers.length != 0 && suppliers[0] instanceof UserDefinedMacroSupplier){
			fUserSupplier = (UserDefinedMacroSupplier)suppliers[0];
		}

		fSystemContextInfo = new SystemContextInfo(fContextType, fContextData);
		fCurrentContextInfo = new CurrentContextInfo(fContextType, fContextData);
	}
	
	public void setParentContextInfo(IMacroContextInfo info){
		fParentContextInfo = info;
		fUseDefaultParentContextInfo = false;
	}
	
	public void resetDefaultParentContextInfo(){
		fUseDefaultParentContextInfo = true;
		fParentContextInfo = null;
	}
	
	public IMacroContextInfo getContextInfo(){
		return fCurrentContextInfo;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
		if(fUserSupplier == null)
			return;
		storeUserMacros();
		setModified(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	public void performDefaults() {
		if(MessageDialog.openQuestion(fParent.getShell(),
				ManagedBuilderUIMessages.getResourceString(DELETE_ALL_CONFIRM_TITLE),
				ManagedBuilderUIMessages.getResourceString(DELETE_ALL_CONFIRM_MESSAGE))){
			deleteAllUserMacros();
			updateValues();
		}
	}
	
	/*
	 * updates both user- and sytem- macros tables.
	 */
	public void updateValues(){
		try{
			MacroResolver.checkIntegrity(fCurrentContextInfo,new MacroUIMacroSubstitutor(fCurrentContextInfo,null," "));//$NON-NLS-1$ //$NON-NLS-2$
			updateState(null);
		} catch (BuildMacroException e){
			updateState(e);
		}
		updateUserMacros();
		updateSystemMacros();	}
	
	private void updateState(BuildMacroException e){
		ICOptionContainer container = getContainer();
		fIncorrectlyDefinedMacrosNames.clear();
		
			if(e != null){
				fStatusLabel.setText(e.getMessage());
				fStatusLabel.setVisible(true);
				IBuildMacroStatus statuses[] = e.getMacroStatuses();
				for(int i = 0; i < statuses.length; i++){
					String name = statuses[i].getMacroName();
					if(name != null)
						fIncorrectlyDefinedMacrosNames.add(name);
				}
			}
			else{
				fStatusLabel.setVisible(false);
			}
	}
	
	/*
	 * apdates a user-defined macros table
	 */
	private void updateUserMacros(){
		if(fEditableList == null || fContextType == 0)
			return;
		
		fEditableList.selectFirstElement();
		handleSelectionChanged(fEditableList);
	
		if(fUserSupplier != null)
			fEditableList.setElements(new ArrayList(getUserMacros().values()));
		else
			fEditableList.removeAllElements();
	}
	
	
	/*
	 * apdates a system-defined macros table
	 */
	private void updateSystemMacros(){
		if(fNonEditableList == null || fContextType == 0)
			return;
		
		List list = null;
		IBuildMacro macros[] = getSystemMacros(fShowParentMacros);
		if(macros != null && macros.length != 0){
			list = new ArrayList(macros.length);
			for(int i = 0; i < macros.length; i++){
				list.add(macros[i]);
			}
		}
		
		if(list != null)
			fNonEditableList.setElements(list);
		else
			fNonEditableList.removeAllElements();
	}
	
	/*
	 * return a system macro of a given name
	 */
	public IBuildMacro getSystemMacro(String name,boolean includeParentLevels){
		if(name == null)
			return null;
		if(fSystemContextInfo == null)
			return null;
		if(!canDisplay(name))
			return null;
		
		return BuildMacroProvider.getMacro(name,fSystemContextInfo,includeParentLevels);
	}

	/*
	 * returns an array of system macros
	 */
	public IBuildMacro[] getSystemMacros(boolean includeParentLevels){
		IBuildMacro macros[] =  BuildMacroProvider.getMacros(fSystemContextInfo,includeParentLevels);
		if(macros == null)
			return null;
		return filterDisplayedMacros(macros);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		fParent = parent;
		FormLayout layout= new FormLayout();
		FormData fd;
		Control buttonsControl = null;
		Control listControl = null;
		
		Composite composite= new Composite(parent, SWT.NULL);
		composite.setLayout(layout);

		if(fEditableList != null){	
			Label nameLabel = new Label(composite, SWT.LEFT);
			nameLabel.setFont(composite.getFont());
			nameLabel.setText(ManagedBuilderUIMessages.getResourceString(USER_MACROS));
			fd = new FormData();
			fd.top = new FormAttachment(0,2);
			fd.left = new FormAttachment(0,0);
			nameLabel.setLayoutData(fd);

			listControl= fEditableList.getListControl(composite);
			fEditableList.getTableViewer().getTable().addKeyListener(new KeyListener(){
				public void keyPressed(KeyEvent e){
					if(e.keyCode == SWT.DEL)
						handleCustomButtonPressed(IDX_BUTTON_DELETE);
				}

				public void keyReleased(KeyEvent e){
					
				}
			});

			buttonsControl= fEditableList.getButtonBox(composite);

			fd = new FormData();
			fd.top = new FormAttachment(nameLabel,0);
			fd.right = new FormAttachment(100,0);
			buttonsControl.setLayoutData(fd);

			fd = new FormData();
			fd.top = new FormAttachment(nameLabel,0);
			fd.left = new FormAttachment(0,0);
			fd.right = new FormAttachment(buttonsControl,-5);
			fd.bottom = new FormAttachment(50,-15);
			listControl.setLayoutData(fd);

		}

		Label nameLabel = new Label(composite, SWT.LEFT);
		nameLabel.setFont(composite.getFont());
		nameLabel.setText(ManagedBuilderUIMessages.getResourceString(SYSTEM_MACROS));
		fd = new FormData();
		if(fEditableList != null)
			fd.top = new FormAttachment(listControl,2);
		else
			fd.top = new FormAttachment(0,2);
		fd.left = new FormAttachment(0,0);
		nameLabel.setLayoutData(fd);
		
		fStatusLabel = new Label(composite, SWT.LEFT);
		fStatusLabel.setFont(composite.getFont());
		fStatusLabel.setForeground(JFaceResources.getColorRegistry().get(JFacePreferences.ERROR_COLOR));
		fd = new FormData();
		fd.bottom = new FormAttachment(100,-10);
		fd.left = new FormAttachment(0,10);
		fd.right = new FormAttachment(100,-10);
		fStatusLabel.setLayoutData(fd);

		
		if(fShowParentViewCheckBox){
			// Create a "show parent levels" button 
			fShowParentButton = new Button(composite, SWT.CHECK);
			fShowParentButton.setFont(composite.getFont());
			fShowParentButton.setText(ManagedBuilderUIMessages.getResourceString(BUTTON_CHECK_SHOW_PARENT));
			fd = new FormData();
			fd.left = new FormAttachment(0,0);
			fd.bottom = new FormAttachment(fStatusLabel,-10);
			fShowParentButton.setLayoutData(fd);
			fShowParentButton.setSelection(fShowParentMacros);
			fShowParentButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					fShowParentMacros = fShowParentButton.getSelection();
					updateSystemMacros();
				}
			});
		}
		
		listControl= fNonEditableList.getListControl(composite);
		fd = new FormData();
		fd.top = new FormAttachment(nameLabel,0);
		fd.left = new FormAttachment(0,0);
		if(buttonsControl != null)
			fd.right = new FormAttachment(buttonsControl,-5);
		else
			fd.right = new FormAttachment(100,0);
		if(fShowParentButton != null)
			fd.bottom = new FormAttachment(fShowParentButton,-2);
		else
			fd.bottom = new FormAttachment(fStatusLabel,-10);

		listControl.setLayoutData(fd);

		this.setControl(composite);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible){
		fVisible = visible;
		if(visible)
			updateValues();
		super.setVisible(visible);
	}
	
	/*
	 * return the context for which the macros are displayed
	 */
	public Object getContextData(){
		return fContextData;
	}
	
	public int getContextType(){
		return fContextType;
	}

	public void displayParentMacros(boolean display){
		fShowParentMacros = display;
		if(fShowParentButton != null)
			fShowParentButton.setSelection(fShowParentMacros);
		updateSystemMacros();
	}
	
	/*
	 * answers whether the given macro should be displayed in UI or not
	 */
	protected boolean canDisplay(String name){
		return canCreate(name);
	}
	
	/*
	 * answers whether the macro of a given name can be sreated
	 */
	public boolean canCreate(String name){
		if((name = EnvVarOperationProcessor.normalizeName(name)) == null)
			return false;

		if(fHiddenMacros != null){
			for(int i = 0; i < fHiddenMacros.length; i++){
				if(name.equals(fHiddenMacros[i].toUpperCase()))
					return false;
			}
		}
		return true; 
	}
	
	/*
	 * filteres the macros to be displayed
	 */
	protected IBuildMacro[] filterDisplayedMacros(IBuildMacro macros[]){
		if(macros == null || macros.length == 0)
			return macros;
		
		IBuildMacro filtered[] = new IBuildMacro[macros.length];
		int filteredNum = 0;
		for(int i = 0; i < macros.length; i++){
			if(canDisplay(macros[i].getName()))
				filtered[filteredNum++] = macros[i];
		}

		if(filteredNum != filtered.length){
			IBuildMacro tmp[] = new IBuildMacro[filteredNum];
			for(int i = 0; i < filteredNum; i++)
				tmp[i] = filtered[i];
			filtered = tmp;
		}
		return filtered;
	}
	
	protected IEnvironmentVariableProvider obtainEnvironmentVariableProvider(){
		ICOptionContainer container = getContainer();
		ManagedBuildOptionBlock optionBlock = null;
		if(container instanceof BuildPropertyPage){
			BuildPropertyPage page = (BuildPropertyPage)container;
			optionBlock = page.getOptionBlock();
		} else if(container instanceof BuildPreferencePage){
			BuildPreferencePage page = (BuildPreferencePage)container;
			optionBlock = page.getOptionBlock();
		}
		if(optionBlock != null){
			EnvironmentSetBlock block = optionBlock.getEnvironmentBlock();
			if(block != null)
				return block.getEnvironmentVariableProvider();
		}
		return null;
	}
}
