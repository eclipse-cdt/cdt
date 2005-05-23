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
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.internal.envvar.BuildEnvVar;
import org.eclipse.cdt.managedbuilder.internal.envvar.DefaultContextInfo;
import org.eclipse.cdt.managedbuilder.internal.envvar.EnvVarCollector;
import org.eclipse.cdt.managedbuilder.internal.envvar.EnvVarOperationProcessor;
import org.eclipse.cdt.managedbuilder.internal.envvar.EnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.envvar.IContextInfo;
import org.eclipse.cdt.managedbuilder.internal.envvar.UserDefinedEnvironmentSupplier;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroSubstitutor;
import org.eclipse.cdt.managedbuilder.internal.macros.EclipseVariablesMacroSupplier;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo;
import org.eclipse.cdt.managedbuilder.internal.macros.MacroResolver;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroStatus;
import org.eclipse.cdt.managedbuilder.ui.properties.BuildPreferencePage;
import org.eclipse.cdt.managedbuilder.ui.properties.BuildPropertyPage;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.resources.IWorkspace;
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
 * displays the environment for the given context
 */
public class EnvironmentBlock extends AbstractCOptionPage {
	/*
	 * String constants
	 */
	private static final String PREFIX = "EnvironmentBlock";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$

	private static final String USER_VAR = LABEL + ".user.var";	//$NON-NLS-1$
	private static final String SYSTEM_VAR = LABEL + ".system.var";	//$NON-NLS-1$

	private static final String HEADER = LABEL + ".header";  //$NON-NLS-1$
	private static final String HEADER_NAME = HEADER + ".name";  //$NON-NLS-1$
	private static final String HEADER_VALUE = HEADER + ".value";  //$NON-NLS-1$
	
	private static final String BUTTON = LABEL + ".button";	//$NON-NLS-1$
	private static final String BUTTON_NEW = BUTTON + ".new";	//$NON-NLS-1$
	private static final String BUTTON_EDIT = BUTTON + ".edit";	//$NON-NLS-1$
	private static final String BUTTON_DELETE = BUTTON + ".delete";	//$NON-NLS-1$
	private static final String BUTTON_UNDEF = BUTTON + ".undef";	//$NON-NLS-1$

	private static final String BUTTON_CHECK_SHOW_PARENT = BUTTON + ".check.chow.parent";	//$NON-NLS-1$

	private static final String VALUE_UNDEF = LABEL + ".value.undef";	//$NON-NLS-1$
	
	private static final String DELETE_CONFIRM_TITLE = LABEL + ".delete.confirm.title";	//$NON-NLS-1$
	private static final String DELETE_CONFIRM_MESSAGE = LABEL + ".delete.confirm.message";	//$NON-NLS-1$

	private static final String DELETE_ALL_CONFIRM_TITLE = LABEL + ".delete.all.confirm.title";	//$NON-NLS-1$
	private static final String DELETE_ALL_CONFIRM_MESSAGE = LABEL + ".delete.all.confirm.message";	//$NON-NLS-1$
	
	private static final String fHiddenVariables[] = new String[]{
			//currently the "CWD" and "PWD" variables are not displayed in UI and can not be creater by a user
			EnvVarOperationProcessor.normalizeName("CWD"),   //$NON-NLS-1$
			EnvVarOperationProcessor.normalizeName("PWD")	  //$NON-NLS-1$
		};
	
	/*
	 * button IDs
	 */
	private static final int IDX_BUTTON_NEW = 0;
	private static final int IDX_BUTTON_EDIT = 1;
	private static final int IDX_BUTTON_UNDEF = 2;
	private static final int IDX_BUTTON_DELETE = 3;

	//variable names deleted by a user
	private HashSet fDeletedUserVariableNames;
	//variables added by a user
	private Map fAddedUserVariables;
	//specifies whether a "delete All" button was previousely pressed 
	private boolean fDeleteAll = false;
	//specifies whether the set of the user-defined variables was changed by a user
	//and the changes are not applied to the User Variable Supplier 
	private boolean fModified = false;
	//holds the visible state. 
	private boolean fVisible = false;
	//specifies whether the "show parent level variables" checkbox should be created
	private boolean fShowParentViewCheckBox = true;
	//inexistent context
	private static final Object fInexistentContext = new Object();
	//the context for which the variables are displayed
	private Object fContext = fInexistentContext;
	//specifies whether the parent level variables should be displayed
	private boolean fShowParentVariables = false;

	private IContextInfo fSystemContextInfo;
	private IContextInfo fCurrentContextInfo;
	private IContextInfo fParentContextInfo;
	private boolean fUseDefaultParentContextInfo = true;
	
	//the user defined variable supplier
	private UserDefinedEnvironmentSupplier fUserSupplier;
	//editable list that displayes user-defined variables
	private ListDialogField fEditableList;
	//non-editable list that holds all variables other than user-defined ones
	private ListDialogField fNonEditableList;
	//the set of names of the incorrestly defined variables
	private Set fIncorrectlyDefinedVariablesNames = new HashSet();

	
	/*
	 * widgets
	 */
	//show parent level variables check-box
	private Button fShowParentButton;
	//parent composite
	private Composite fParent;
	//status label
	private Label fStatusLabel;
	
	private class SystemContextInfo extends DefaultContextInfo{
		protected SystemContextInfo(Object context){
			super(context);
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.envvar.DefaultContextInfo#getSuppliers(java.lang.Object)
		 */
		protected  IEnvironmentVariableSupplier[] getSuppliers(Object context){
			IEnvironmentVariableSupplier suppliers[] = super.getSuppliers(context);
			if(suppliers == null || suppliers.length == 0)
				return null;
			
			List list = new ArrayList();
			for(int i = 0; i < suppliers.length; i++){
				if(!(suppliers[i] instanceof UserDefinedEnvironmentSupplier))
					list.add(suppliers[i]);
			}
			
			return (IEnvironmentVariableSupplier[])list.toArray(new IEnvironmentVariableSupplier[list.size()]);

		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.envvar.IContextInfo#getNext()
		 */
		public IContextInfo getNext(){
			if(fUseDefaultParentContextInfo)
				return super.getNext();
			return fParentContextInfo;
		}
	}
	
	private class CurrentContextInfo extends DefaultContextInfo{
		protected CurrentContextInfo(Object context){
			super(context);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.envvar.DefaultContextInfo#getSuppliers(java.lang.Object)
		 */
		public IEnvironmentVariableSupplier[] getSuppliers(Object context){
			IEnvironmentVariableSupplier suppliers[] = super.getSuppliers(context);
			if(fParentContextInfo == null){
				int i = 0;
			}
			if(suppliers == null || suppliers.length == 0)
				return suppliers;
			if(!(suppliers[0] instanceof UserDefinedEnvironmentSupplier))
				return suppliers;
			
			List list = new ArrayList(suppliers.length);
			list.add(new UIVariableSupplier());
			
			for(int i = 1; i < suppliers.length; i++){
					list.add(suppliers[i]);
			}
			
			return (IEnvironmentVariableSupplier[])list.toArray(new IEnvironmentVariableSupplier[list.size()]);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.envvar.IContextInfo#getNext()
		 */
		public IContextInfo getNext(){
			if(fUseDefaultParentContextInfo)
				return super.getNext();
			return fParentContextInfo;
		}
	}
	
	private class UIVariableSupplier implements IEnvironmentVariableSupplier{
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariable(java.lang.String, java.lang.Object)
		 */
		public IBuildEnvironmentVariable getVariable(String name, Object context){
			if(context != fContext)
				return null;
			
			return (IBuildEnvironmentVariable)getUserVariables().get(name);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariables(java.lang.Object)
		 */
		public IBuildEnvironmentVariable[] getVariables(Object context){
			if(context != fContext)
				return null;
			
			Collection vars = getUserVariables().values(); 
			return (IBuildEnvironmentVariable[])vars.toArray(new IBuildEnvironmentVariable[vars.size()]);
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
	
	private class EnvironmentLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider , ITableFontProvider, IColorProvider{
		private boolean fUser;
		public EnvironmentLabelProvider(boolean user){
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
			IBuildEnvironmentVariable var = (IBuildEnvironmentVariable)element;
			switch(columnIndex){
			case 0:
				return var.getName();
			case 1:
				if(var.getOperation() == IBuildEnvironmentVariable.ENVVAR_REMOVE)
					return ManagedBuilderUIMessages.getResourceString(VALUE_UNDEF);
				return var.getValue();
			}
			return "";  //$NON-NLS-1$
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
			IBuildEnvironmentVariable var = (IBuildEnvironmentVariable)element;

			switch(columnIndex){
			case 0:
				break;
			case 1:
				if(var.getOperation() == IBuildEnvironmentVariable.ENVVAR_REMOVE)
					return JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);;
				break;
			}

			if(!fUser && getUserVariable(var.getName()) != null)
				return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);

			return null;
		}
		
	    /* (non-Javadoc)
	     * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	     */
	    public Color getForeground(Object element){
			IBuildEnvironmentVariable var = (IBuildEnvironmentVariable)element;
			boolean incorrect = false;
			String name = var.getName();
			if(fUser || getUserVariable(name) == null)
				incorrect = fIncorrectlyDefinedVariablesNames.contains(name);
			
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

	private class EnvVarUIMacroSubstitutor extends DefaultMacroSubstitutor{
		public EnvVarUIMacroSubstitutor(IMacroContextInfo contextInfo, String inexistentMacroValue, String listDelimiter) {
			super(contextInfo, inexistentMacroValue, listDelimiter);
		}
		
		public EnvVarUIMacroSubstitutor(int contextType, Object contextData, String inexistentMacroValue, String listDelimiter){
			super(contextType, contextData, inexistentMacroValue, listDelimiter);
		}

		protected ResolvedMacro resolveMacro(IBuildMacro macro) throws BuildMacroException{
			if(macro instanceof EclipseVariablesMacroSupplier.EclipseVarMacro){
				EclipseVariablesMacroSupplier.EclipseVarMacro eclipseVarMacro = 
					(EclipseVariablesMacroSupplier.EclipseVarMacro)macro;
				IStringVariable var = eclipseVarMacro.getVariable();
				String value = null;
				if(var instanceof IDynamicVariable){
					value = "dynamic<" + var.getName() + ">"; //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					value = macro.getStringValue();
				}
				return new ResolvedMacro(macro.getName(),value);
			}
			return super.resolveMacro(macro);
		}
	}

	
	/*
	 * constructor
	 */
	public EnvironmentBlock(ICOptionContainer parent, String title, boolean editable, boolean showParentViewCheckBox){
		super(title);
		super.setContainer(parent);

		ListAdapter adapter = new ListAdapter();
		
		if(editable){
			String[] buttons= new String[] {
					ManagedBuilderUIMessages.getResourceString(BUTTON_NEW),
					ManagedBuilderUIMessages.getResourceString(BUTTON_EDIT),
					ManagedBuilderUIMessages.getResourceString(BUTTON_UNDEF),
					ManagedBuilderUIMessages.getResourceString(BUTTON_DELETE),
				};

			fEditableList= new ListDialogField(adapter, buttons, new EnvironmentLabelProvider(true));
			fEditableList.setDialogFieldListener(adapter);
			
			String[] columnsHeaders= new String[] {
				ManagedBuilderUIMessages.getResourceString(HEADER_NAME),
				ManagedBuilderUIMessages.getResourceString(HEADER_VALUE),
			};
			
			fEditableList.setTableColumns(new ListDialogField.ColumnsDescription(columnsHeaders, true));
			fEditableList.setViewerSorter(new ViewerSorter());


		}
	
			//create non-editable list
			fNonEditableList= new ListDialogField(adapter, null, new EnvironmentLabelProvider(false));
			fNonEditableList.setDialogFieldListener(adapter);
			
			String[] columnsHeaders= new String[] {
				ManagedBuilderUIMessages.getResourceString(HEADER_NAME),
				ManagedBuilderUIMessages.getResourceString(HEADER_VALUE),
			};
			
			fNonEditableList.setTableColumns(new ListDialogField.ColumnsDescription(columnsHeaders, true));
			fNonEditableList.setViewerSorter(new ViewerSorter());
			
			fShowParentViewCheckBox = showParentViewCheckBox;

	}
	
	/*
	 * returns the map containing the user-defined variables
	 */
	private Map getUserVariables(){
		if(fUserSupplier == null)
			return null;
		Map map = new HashMap();
		
		if(!fDeleteAll){
			IBuildEnvironmentVariable vars[] = fUserSupplier.getVariables(fContext);
			if(vars != null) {
				for(int i = 0; i < vars.length; i++){
					String name = vars[i].getName();
					if(!ManagedBuildManager.getEnvironmentVariableProvider().isVariableCaseSensitive())
						name = name.toUpperCase();
					map.put(name,vars[i]);
				}
			}
			
			Iterator iter = getDeletedUserVariableNames().iterator();
			while(iter.hasNext()){
				map.remove((String)iter.next());
			}
			
			iter = getAddedUserVariables().values().iterator();
			while(iter.hasNext()){
				IBuildEnvironmentVariable var = (IBuildEnvironmentVariable)iter.next();
				String name = var.getName(); 
				if(!ManagedBuildManager.getEnvironmentVariableProvider().isVariableCaseSensitive())
					name = name.toUpperCase();
				map.put(name,var);
			}
		}
		return map;
	}
	
	/*
	 * returns the HashSet holding the names of the user-deleted variables
	 */
	private HashSet getDeletedUserVariableNames(){
		if(fDeletedUserVariableNames == null)
			fDeletedUserVariableNames = new HashSet();
		return fDeletedUserVariableNames;
	}
	
	/*
	 * returns the map holding  user-created.modified variables
	 */
	private Map getAddedUserVariables(){
		if(fAddedUserVariables == null)
			fAddedUserVariables = new HashMap();
		return fAddedUserVariables;
	}

	/*
	 * creates a user variable
	 * the variables created are stored in the fAddedUserVariables Map, and are not actually added to the user supplier
	 * the applyUserVariables() should be called to store those variabes to the user supplier
	 */
	private void addUserVariable(String name, String value, int op, String delimiter){
		if(!canCreate(name))
			return;
		fDeleteAll = false;
		BuildEnvVar newVar = new BuildEnvVar(name,value,op,delimiter);
		if(!ManagedBuildManager.getEnvironmentVariableProvider().isVariableCaseSensitive())
			name = name.toUpperCase();
		getDeletedUserVariableNames().remove(name);
		getAddedUserVariables().put(name,newVar);
		
		fModified = true;
	}
	
	/*
	 * deletes a user variable
	 * the variables deleted are stored in the fDeletedUserVariableNames HashSet, and are not actually deleted from the user supplier
	 * the applyUserVariables() should be called to delete those variabes from the user supplier
	 */
	private void deleteUserVariable(String name){
		fDeleteAll = false;
		if(!ManagedBuildManager.getEnvironmentVariableProvider().isVariableCaseSensitive())
			name = name.toUpperCase();
		getAddedUserVariables().remove(name);
		getDeletedUserVariableNames().add(name);
		
		fModified = true;
	}
	
	/*
	 * deletes all user variables
	 * the applyUserVariables() should be called to delete those variabes from the user supplier
	 */
	private void deleteAllUserVariables(){
		fDeleteAll = true;
		getDeletedUserVariableNames().clear();
		getAddedUserVariables().clear();
		
		fModified = true;
	}
	
	/*
	 * returns whether the user variables were modified
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
	 * returns a user variable of a given name
	 */
	private IBuildEnvironmentVariable getUserVariable(String name){
		Map vars = getUserVariables();
		if(vars == null)
			return null;

		if(!ManagedBuildManager.getEnvironmentVariableProvider().isVariableCaseSensitive())
			name = name.toUpperCase();
		return (IBuildEnvironmentVariable)vars.get(name);
	}

	/*
	 * applies user variables.
	 * 
	 */
	private void applyUserVariables(){
		if(fUserSupplier != null){
			if(fDeleteAll){
				fUserSupplier.deleteAll(fContext);
			}
			else{
				Iterator iter = getDeletedUserVariableNames().iterator();
				while(iter.hasNext()){
					fUserSupplier.deleteVariable((String)iter.next(),fContext);
				}
				
				iter = getAddedUserVariables().values().iterator();
				while(iter.hasNext()){
					IBuildEnvironmentVariable var = (IBuildEnvironmentVariable)iter.next();
					fUserSupplier.createVariable(var.getName(),var.getValue(),var.getOperation(),var.getDelimiter(),fContext);
				}
				
				getDeletedUserVariableNames().clear();
				getAddedUserVariables().clear();
			}
		}
	}
	
	/*
	 * applies user variables and asks the user supplier to serialize
	 */
	private void storeUserVariables(){
		applyUserVariables();
		if(fUserSupplier != null)
			fUserSupplier.serialize(false);
	}

	/*
	 * called when the user variable selection was changed 
	 */
	private void handleSelectionChanged(ListDialogField field){
		if(isEditable(field)){
			List selectedElements= field.getSelectedElements();
			field.enableButton(IDX_BUTTON_EDIT, selectedElements.size() == 1);
			field.enableButton(IDX_BUTTON_UNDEF, selectedElements.size() > 0);
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
			NewEnvVarDialog dlg = new NewEnvVarDialog(fParent.getShell(),this,null);
			if(dlg.open() == Dialog.OK){
				IBuildEnvironmentVariable var = dlg.getDefinedVariable();
				if(var != null){
					addUserVariable(var.getName(),var.getValue(),var.getOperation(),var.getDelimiter());
					updateValues();
				}
			}
		}
		break;
		case IDX_BUTTON_EDIT:{
			IBuildEnvironmentVariable vars[] = getSelectedUserVariables();
			if(vars != null && vars.length == 1){
				NewEnvVarDialog dlg = new NewEnvVarDialog(fParent.getShell(),this,getUserVariable(vars[0].getName()));
				if(dlg.open() == Dialog.OK){
					IBuildEnvironmentVariable var = dlg.getDefinedVariable();
					if(var != null){
						addUserVariable(var.getName(),var.getValue(),var.getOperation(),var.getDelimiter());
						updateValues();
					}
				}
			}
		}
		break;
		case IDX_BUTTON_UNDEF:{
			IBuildEnvironmentVariable vars[] = getSelectedUserVariables();
			if(vars != null){
				for(int i = 0; i < vars.length; i++){
					addUserVariable(vars[i].getName(),null,IBuildEnvironmentVariable.ENVVAR_REMOVE,null);
				}
				updateValues();
			}
		}
		break;
		case IDX_BUTTON_DELETE:{
			IBuildEnvironmentVariable vars[] = getSelectedUserVariables();
			if(vars != null && vars.length > 0){
				if(MessageDialog.openQuestion(fParent.getShell(),
						ManagedBuilderUIMessages.getResourceString(DELETE_CONFIRM_TITLE),
						ManagedBuilderUIMessages.getResourceString(DELETE_CONFIRM_MESSAGE))){
					for(int i = 0; i < vars.length; i++){
						deleteUserVariable(vars[i].getName());
					}
					updateValues();
				}
			}
		}
		break;
		}
	}
	
	/*
	 * returnes the selected user-defined variables
	 */
	private IBuildEnvironmentVariable[] getSelectedUserVariables(){
		if(fEditableList == null)
			return null;
		
		List list = fEditableList.getSelectedElements();
		return (IBuildEnvironmentVariable[])list.toArray(new IBuildEnvironmentVariable[list.size()]);
	}

	/*
	 * sets the context for which the variables should be displayed
	 */
	public void setContext(Object context){
		if(context == fContext)
			return;
	
		fContext = context;

		IEnvironmentVariableProvider provider = ManagedBuildManager.getEnvironmentVariableProvider();
		IEnvironmentVariableSupplier suppliers[] = provider.getSuppliers(fContext);
		if(suppliers != null && suppliers.length != 0 && suppliers[0] instanceof UserDefinedEnvironmentSupplier){
			fUserSupplier = (UserDefinedEnvironmentSupplier)suppliers[0];
		}

			fSystemContextInfo = new SystemContextInfo(context);
			fCurrentContextInfo = new CurrentContextInfo(context);
	}
	
	public void setParentContextInfo(IContextInfo info){
		fParentContextInfo = info;
		fUseDefaultParentContextInfo = false;
	}
	
	public void resetDefaultParentContextInfo(){
		fUseDefaultParentContextInfo = true;
		fParentContextInfo = null;
	}
	
	public IContextInfo getContextInfo(){
		return fCurrentContextInfo;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
		if(fUserSupplier == null)
			return;
		storeUserVariables();
		setModified(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	public void performDefaults() {
		if(MessageDialog.openQuestion(fParent.getShell(),
				ManagedBuilderUIMessages.getResourceString(DELETE_ALL_CONFIRM_TITLE),
				ManagedBuilderUIMessages.getResourceString(DELETE_ALL_CONFIRM_MESSAGE))){
			deleteAllUserVariables();
			updateValues();
		}
	}
	
	/*
	 * updates both user- and sytem- variables tables.
	 */
	public void updateValues(){
		if(fCurrentContextInfo == null)
			return;
		try{
			Object context = fCurrentContextInfo.getContext();
			int contextType = 0;
			Object contextData = null;
			if(context instanceof IConfiguration){
				contextType = IBuildMacroProvider.CONTEXT_CONFIGURATION;
				contextData = context;
			} else if(context instanceof IManagedProject) {
				contextType = IBuildMacroProvider.CONTEXT_PROJECT;
				contextData = context;
			} else if(context instanceof IWorkspace){
				contextType = IBuildMacroProvider.CONTEXT_WORKSPACE;
				contextData = context;
			} else {
				contextType = IBuildMacroProvider.CONTEXT_ECLIPSEENV;
				contextData = null;
			}
		
			BuildMacroProvider macroProvider = obtainMacroProvider();
			if(macroProvider != null){
				IMacroContextInfo macroContextInfo = macroProvider.getMacroContextInfo(contextType,contextData);
				if(macroContextInfo != null){
					EnvironmentVariableProvider provider = (EnvironmentVariableProvider)ManagedBuildManager.getEnvironmentVariableProvider();
					EnvVarCollector v = provider.getVariables(fCurrentContextInfo,true);
					if(v != null){
						EnvVarUIMacroSubstitutor substitutor = new EnvVarUIMacroSubstitutor(macroContextInfo, null, " "); //$NON-NLS-2$
						IBuildEnvironmentVariable vars[] = v.toArray(false);
						for(int i = 0; i < vars.length; i++){
							MacroResolver.checkMacros(vars[i].getValue(), substitutor);
						}
					}
				}
			}
			updateState(null);
		} catch (BuildMacroException e){
			updateState(e);
		}
		updateUserVariables();
		updateSystemVariables();
	}
	
	/*
	 * apdates a user-defined variables table
	 */
	private void updateUserVariables(){
		if(fEditableList == null || fContext == fInexistentContext)
			return;
		
		fEditableList.selectFirstElement();
		handleSelectionChanged(fEditableList);
	
		List list = null;
		if(fUserSupplier != null) {
			Collection vars = getUserVariables().values();
			Iterator iter = vars.iterator();

			list = new ArrayList(vars.size());
			while(iter.hasNext()){
				IBuildEnvironmentVariable userVar = (IBuildEnvironmentVariable)iter.next();
				IBuildEnvironmentVariable sysVar = getSystemVariable(userVar.getName(),true);
				IBuildEnvironmentVariable var =	EnvVarOperationProcessor.performOperation(sysVar,userVar);
				if(var != null)
					list.add(var);
			}
			
			if(list != null)
				fEditableList.setElements(list);
			else
				fEditableList.removeAllElements();
		}
	}
	
	/*
	 * apdates a system-defined variables table
	 */
	private void updateSystemVariables(){
		if(fNonEditableList == null || fContext == fInexistentContext)
			return;
		
		List list = null;
		IBuildEnvironmentVariable vars[] = getSystemVariables(fShowParentVariables);
		if(vars != null && vars.length != 0){
			list = new ArrayList(vars.length);
			for(int i = 0; i < vars.length; i++){
				list.add(vars[i]);
			}
		}
		
		if(list != null)
			fNonEditableList.setElements(list);
		else
			fNonEditableList.removeAllElements();
	}
	
	/*
	 * return a system variable of a given name
	 */
	public IBuildEnvironmentVariable getSystemVariable(String name,boolean includeParentLevels){
		if(name == null)
			return null;
		if(fSystemContextInfo == null)
			return null;
		if(!canDisplay(name))
			return null;
		
		EnvironmentVariableProvider provider = (EnvironmentVariableProvider)ManagedBuildManager.getEnvironmentVariableProvider();
		return provider.getVariable(name,fSystemContextInfo,includeParentLevels);
	}

	/*
	 * returns an array of system variables
	 */
	public IBuildEnvironmentVariable[] getSystemVariables(boolean includeParentLevels){
		EnvironmentVariableProvider provider = (EnvironmentVariableProvider)ManagedBuildManager.getEnvironmentVariableProvider();
		EnvVarCollector variables =  provider.getVariables(fSystemContextInfo,includeParentLevels);
		if(variables == null)
			return null;
		return filterDisplayedVariables(variables.toArray(false));
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
			nameLabel.setText(ManagedBuilderUIMessages.getResourceString(USER_VAR));
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
		nameLabel.setText(ManagedBuilderUIMessages.getResourceString(SYSTEM_VAR));
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
//			fd.bottom = new FormAttachment(100,0);
			fd.bottom = new FormAttachment(fStatusLabel,-10);
			fShowParentButton.setLayoutData(fd);
			fShowParentButton.setSelection(fShowParentVariables);
			fShowParentButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					fShowParentVariables = fShowParentButton.getSelection();
					updateSystemVariables();
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
//			fd.bottom = new FormAttachment(100,0);
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
	 * return the context for which the variables are displayed
	 */
	public Object getContext(){
		return fContext;
	}
	
	public void displayParentVariables(boolean display){
		fShowParentVariables = display;
		if(fShowParentButton != null)
			fShowParentButton.setSelection(fShowParentVariables);
		updateSystemVariables();
	}
	
	/*
	 * answers whether the given variable should be displayed in UI or not
	 */
	protected boolean canDisplay(String name){
		return canCreate(name);
	}
	
	/*
	 * answers whether the variable of a given name can be sreated
	 */
	public boolean canCreate(String name){
		if((name = EnvVarOperationProcessor.normalizeName(name)) == null)
			return false;
		
		if(fHiddenVariables != null){
			for(int i = 0; i < fHiddenVariables.length; i++){
				if(name.equals(fHiddenVariables[i]))
					return false;
			}
		}
		return true; 
	}
	
	/*
	 * filteres the names to be displayed
	 */
	protected IBuildEnvironmentVariable[] filterDisplayedVariables(IBuildEnvironmentVariable variables[]){
		if(variables == null || variables.length == 0)
			return variables;
		
		IBuildEnvironmentVariable filtered[] = new IBuildEnvironmentVariable[variables.length];
		int filteredNum = 0;
		for(int i = 0; i < variables.length; i++){
			if(canDisplay(variables[i].getName()))
				filtered[filteredNum++] = variables[i];
		}

		if(filteredNum != filtered.length){
			IBuildEnvironmentVariable vars[] = new IBuildEnvironmentVariable[filteredNum];
			for(int i = 0; i < filteredNum; i++)
				vars[i] = filtered[i];
			filtered = vars;
		}
		return filtered;
	}
	
	private void updateState(BuildMacroException e){
		ICOptionContainer container = getContainer();
		fIncorrectlyDefinedVariablesNames.clear();
		
		if(e != null){
			fStatusLabel.setText(e.getMessage());
			fStatusLabel.setVisible(true);
			IBuildMacroStatus statuses[] = e.getMacroStatuses();
			for(int i = 0; i < statuses.length; i++){
				String name = statuses[i].getMacroName();
				if(name != null)
					fIncorrectlyDefinedVariablesNames.add(name);
			}
		}
		else{
			fStatusLabel.setVisible(false);
		}
	}
	
	/**
	 * Returns the build macro provider to be used for macro resolution
	 * In case the "Build Macros" tab is available, returns the BuildMacroProvider
	 * supplied by that tab. 
	 * Unlike the default provider, that provider also contains
	 * the user-modified macros that are not applied yet
	 * If the "Build Macros" tab is not available, returns the default BuildMacroProvider
	 */
	protected BuildMacroProvider obtainMacroProvider(){
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
			MacrosSetBlock block = optionBlock.getMacrosBlock();
			if(block != null)
				return block.getBuildMacroProvider();
		}
		return (BuildMacroProvider)ManagedBuildManager.getBuildMacroProvider();
	}

}
