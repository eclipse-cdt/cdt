/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.cdt.internal.ui.dialogs.StatusDialog;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.internal.envvar.BuildEnvVar;
import org.eclipse.cdt.managedbuilder.internal.envvar.EnvVarOperationProcessor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/*
 * the dialog used to create or edit the environment variable
 */
public class NewEnvVarDialog extends StatusDialog {
	// String constants
	private static final String PREFIX = "NewEnvVarDialog";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String NAME = LABEL + ".name";	//$NON-NLS-1$
	private static final String VALUE = LABEL + ".value";	//$NON-NLS-1$

	private static final String DELIMITER = LABEL + ".delimiter";	//$NON-NLS-1$
	private static final String OPERATION = LABEL + ".operation";	//$NON-NLS-1$
	private static final String OPERATION_REPLACE = OPERATION + ".replace";	//$NON-NLS-1$
	private static final String OPERATION_PREPEND = OPERATION + ".prepend";	//$NON-NLS-1$
	private static final String OPERATION_APPEND = OPERATION + ".append";	//$NON-NLS-1$
	private static final String OPERATION_REMOVE = OPERATION + ".remove";	//$NON-NLS-1$
	
	private static final String VALUE_PREPEND = VALUE + ".prepend"; 	//$NON-NLS-1$
	private static final String VALUE_APPEND = VALUE + ".append"; 	//$NON-NLS-1$

	private static final String TITLE_NEW = LABEL + ".title.new"; 	//$NON-NLS-1$
	private static final String TITLE_EDIT = LABEL + ".title.edit"; 	//$NON-NLS-1$
	
	private static final String STATUS = LABEL + ".status"; 	//$NON-NLS-1$
	private static final String STATUS_CANNOT_CTREATE = STATUS + ".cannot.create"; 	//$NON-NLS-1$
	
	private static final String EMPTY_STRING = new String();

	// The title of the dialog.
	private String fTitle;
	// hold the variable being edited(in the case of the "edit" mode)
	private IBuildEnvironmentVariable fEditedVar;
	//the environment block from which the dialog was called
	private EnvironmentBlock fEnvVarBlock;
	//the resulting variable. Can be accessed only when the dialog is closed
	private IBuildEnvironmentVariable fResultingVar;
	//the string that holds the value is used in the "replace" operation
	private String fReplaceValue = null;
	//the string that holds the value is used in the "append/prepend" operations
	private String fAppPrepValue = null;
	//specifies whether the fAppPrepValue holds the prepended or appended value
	private boolean fAppPrepPrepend = true;

	private String fTypedName;

	// Widgets
//	protected Text fVarNameEdit;
	private Combo fVarNameEdit;
	private Text fVarValueEdit;
	private Label fDelimiterLabel;
	private Text fDelimiterEdit;
	private Combo fOpSelector;
	private Label fOpVarValueLabel;
	private Text fOpVarValueEdit;

	public NewEnvVarDialog(Shell parentShell, EnvironmentBlock envBlock, IBuildEnvironmentVariable editedVar) {
		super(parentShell);
		if(editedVar != null)
			fTitle = ManagedBuilderUIMessages.getResourceString(TITLE_EDIT);
		else
			fTitle = ManagedBuilderUIMessages.getResourceString(TITLE_NEW);
		fEditedVar = editedVar;
		fEnvVarBlock = envBlock;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (fTitle != null)
			shell.setText(fTitle);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setFont(parent.getFont());
		comp.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);
		
		Label nameLabel = new Label(comp, SWT.LEFT);
		nameLabel.setFont(comp.getFont());
		nameLabel.setText(ManagedBuilderUIMessages.getResourceString(NAME));
		nameLabel.setLayoutData(new GridData());
		
//		fVarNameEdit = new Text(comp, SWT.SINGLE | SWT.BORDER);
		fVarNameEdit = new Combo(comp, SWT.SINGLE | SWT.DROP_DOWN);
		fVarNameEdit.setItems(getVarNames());
		fVarNameEdit.setFont(comp.getFont());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH + 50;
		fVarNameEdit.setLayoutData(gd);
		fVarNameEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleVarNameModified();
			}
		});
		fVarNameEdit.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				handleVarNameSelection();
			}
		});
		
		Label valueLabel = new Label(comp, SWT.LEFT);
		valueLabel.setFont(comp.getFont());
		valueLabel.setText(ManagedBuilderUIMessages.getResourceString(VALUE));
		gd = new GridData();
		gd.horizontalSpan = 1;		
		valueLabel.setLayoutData(gd);
		
		fVarValueEdit = new Text(comp, SWT.SINGLE | SWT.BORDER);
		fVarValueEdit.setFont(comp.getFont());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH + 100;
		gd.horizontalSpan = 1;
		fVarValueEdit.setLayoutData(gd);
		fVarValueEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleVarValueModified();
			}
		});

		fDelimiterLabel = new Label(comp, SWT.LEFT);
		fDelimiterLabel.setFont(comp.getFont());
		fDelimiterLabel.setText(ManagedBuilderUIMessages.getResourceString(DELIMITER));
		gd = new GridData();
		gd.horizontalSpan = 1;		
		gd.widthHint = 100;
		fDelimiterLabel.setLayoutData(gd);

		fDelimiterEdit = new Text(comp, SWT.SINGLE | SWT.BORDER);
		fDelimiterEdit.setFont(comp.getFont());
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.widthHint = 50;
		fDelimiterEdit.setLayoutData(gd);
		fDelimiterEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleDelimiterModified();
			}
		});

		Label opLabel = new Label(comp, SWT.LEFT);
		opLabel.setFont(comp.getFont());
		opLabel.setText(ManagedBuilderUIMessages.getResourceString(OPERATION));
		gd = new GridData();
		gd.horizontalSpan = 1;		
		opLabel.setLayoutData(gd);
		
		fOpSelector = new Combo(comp, SWT.READ_ONLY|SWT.DROP_DOWN);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 1;
//		gd.widthHint = 70;
		fOpSelector.setLayoutData(gd);
		fOpSelector.setItems(new String[]{
				ManagedBuilderUIMessages.getResourceString(OPERATION_REPLACE),
				ManagedBuilderUIMessages.getResourceString(OPERATION_PREPEND),
				ManagedBuilderUIMessages.getResourceString(OPERATION_APPEND),
				ManagedBuilderUIMessages.getResourceString(OPERATION_REMOVE),
		});
		setSelectedOperation(IBuildEnvironmentVariable.ENVVAR_REPLACE);

		fOpSelector.addListener(SWT.Selection, new Listener () {
			public void handleEvent(Event e) {
				handleOperationModified();
			}
		});
		
		fOpVarValueLabel = new Label(comp, SWT.LEFT);
		fOpVarValueLabel.setFont(comp.getFont());
		gd = new GridData();
		gd.horizontalSpan = 1;
//		gd.widthHint = 100;
		fOpVarValueLabel.setText(ManagedBuilderUIMessages.getResourceString(VALUE_PREPEND));
		fOpVarValueLabel.setLayoutData(gd);
		fOpVarValueEdit = new Text(comp, SWT.SINGLE | SWT.BORDER);
		fOpVarValueEdit.setFont(comp.getFont());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH + 50;
		fOpVarValueEdit.setLayoutData(gd);
		fOpVarValueEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleOperationVarValueModified();
			}
		});

		if(fEditedVar != null){
			loadVariableSettings(fEditedVar,true);
			fVarNameEdit.setEnabled(false);
		}

		updateWidgetState();
		
		return comp;
	}
	
	/*
	 * get the names to be displayed in the var Name combo.
	 */
	private String[] getVarNames(){
		IBuildEnvironmentVariable vars[] = fEnvVarBlock.getSystemVariables(true);
		String names[] = null;
		if(vars == null || vars.length == 0)
			names = new String[0];
		else{
			names = new String[vars.length];
			for(int i = 0; i < vars.length; i++){
				names[i] = vars[i].getName();
			}
	
			final Collator collator = Collator.getInstance();
			Arrays.sort(names, new Comparator() {
	            public int compare(Object a, Object b) {
					String strA = ((String)a).toUpperCase();
					String strB = ((String)b).toUpperCase();
					return collator.compare(strA,strB);
	            }
	        });
		}
		
		return names;
	}

	/*
	 * called when the variable name is selected, loads all the dialog fields with the variable settings
	 */
	private void handleVarNameSelection(){
		int index = fVarNameEdit.getSelectionIndex();
		if(index == -1)
			loadVariableSettings(null);
		else
			loadVariableSettings(fVarNameEdit.getItem(index));
	}
	
	private void loadVariableSettings(String name){
		IBuildEnvironmentVariable var = fEnvVarBlock.getSystemVariable(name,true);
		if(var != null)
			loadVariableSettings(var,false);
		else
			loadVariableSettings(name,EMPTY_STRING,IBuildEnvironmentVariable.ENVVAR_REPLACE,EMPTY_STRING);
	}
	
	private void loadVariableSettings(String name,
					String value,
					int op,
					String delimiter){
		setSelectedOperation(op);

		setSelectedVarName(notNull(name));
		
		switch(op){
		case IBuildEnvironmentVariable.ENVVAR_PREPEND:
			fOpVarValueEdit.setText(notNull(value));
			fReplaceValue = null;
			fAppPrepValue = notNull(value);
			fAppPrepPrepend = true;
			break;
		case IBuildEnvironmentVariable.ENVVAR_APPEND:
			fOpVarValueEdit.setText(notNull(value));
			fReplaceValue = null;
			fAppPrepValue = notNull(value);
			fAppPrepPrepend = false;
			break;
		case IBuildEnvironmentVariable.ENVVAR_REMOVE:
			break;
		case IBuildEnvironmentVariable.ENVVAR_REPLACE:
		default:
			fVarValueEdit.setText(notNull(value));
			fReplaceValue = notNull(value);
			fAppPrepValue = null;
		}

		fDelimiterEdit.setText(notNull(delimiter));
	
		updateWidgetState();
	}
	
	/*
	 * loads all the dialog fields with the variable settings
	 */
	private void loadVariableSettings(IBuildEnvironmentVariable var, boolean isUser){
		int op = var.getOperation();
		if(!isUser && op != IBuildEnvironmentVariable.ENVVAR_REMOVE)
			op = IBuildEnvironmentVariable.ENVVAR_REPLACE;
		
		loadVariableSettings(var.getName(),var.getValue(),op,var.getDelimiter());
	}

	/*
	 * returns an empty string in the case the string passed is null.
	 * otherwise returns the string passed
	 */
	private String notNull(String str){
		return str == null ? EMPTY_STRING : str;
	}
	
	/*
	 * returns the name typed in the dialog var name edit triming spaces  
	 */
	private String getSelectedVarName(){
		return fVarNameEdit.getText().trim();
	}
	
	/*
	 * sets the variable name to the dialog "variable name" edit control
	 */
	private void setSelectedVarName(String name){
		if(!varNamesEqual(fVarNameEdit.getText(),name)){
			fTypedName = name;
			fVarNameEdit.setText(notNull(name).trim());
		}
	}
	
	private boolean varNamesEqual(String name1, String name2){
		name1 = name1.trim();
		name2 = name2.trim();
		if(ManagedBuildManager.getEnvironmentVariableProvider().isVariableCaseSensitive())
			return name1.equals(name2);
		return name1.equalsIgnoreCase(name2);
	}

	/*
	 * returns the selected operation
	 */
	private int getSelectedOperation(){
		switch(fOpSelector.getSelectionIndex()){
			case 1:
				return IBuildEnvironmentVariable.ENVVAR_PREPEND;
			case 2:
				return IBuildEnvironmentVariable.ENVVAR_APPEND;
			case 3:
				return IBuildEnvironmentVariable.ENVVAR_REMOVE;
			case 0:
			default:
				return IBuildEnvironmentVariable.ENVVAR_REPLACE;
		}
	}
	
	/*
	 * sets the selected operation
	 */
	private void setSelectedOperation(int op){
		switch(op){
			case IBuildEnvironmentVariable.ENVVAR_PREPEND:
				fOpSelector.select(1);
				break;
			case IBuildEnvironmentVariable.ENVVAR_APPEND:
				fOpSelector.select(2);
				break;
			case IBuildEnvironmentVariable.ENVVAR_REMOVE:
				fOpSelector.select(3);
				break;
			case IBuildEnvironmentVariable.ENVVAR_REPLACE:
			default:
				fOpSelector.select(0);
				break;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed(){
		String name = getSelectedVarName();
		if(name != null || !EMPTY_STRING.equals(name))
			fResultingVar = new BuildEnvVar(name,getSelectedVariableValue(),getSelectedOperation(),fDelimiterEdit.getText());

		super.okPressed();
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open(){
		fResultingVar = null;
		return super.open();
	}
	
	/*
	 * returns the variable value that should be stored in the resulting variable
	 */
	private String getSelectedVariableValue(){
		switch(getSelectedOperation()){
		case IBuildEnvironmentVariable.ENVVAR_PREPEND:
		case IBuildEnvironmentVariable.ENVVAR_APPEND:
			return fOpVarValueEdit.getText();
		case IBuildEnvironmentVariable.ENVVAR_REMOVE:
			return EMPTY_STRING;
		case IBuildEnvironmentVariable.ENVVAR_REPLACE:
		default:
			return fVarValueEdit.getText();
		}
	}

	/*
	 * this method should be called after the dialog is closed
	 * to obtain the created variable.
	 * if the variable was not created, e.g. because a user has pressed 
	 * the cancel button this method returns null
	 */
	public IBuildEnvironmentVariable getDefinedVariable(){
		return fResultingVar;
	}
	
	/*
	 * called when the variable name is modified
	 */
	private void handleVarNameModified(){
		String name = getSelectedVarName();
		if(fTypedName == null || !fTypedName.equals(name)){
			loadVariableSettings(name);
		}
	}
	
	/*
	 * called when the variable value is modified
	 */
	private void handleVarValueModified(){
		switch(getSelectedOperation()){
		case IBuildEnvironmentVariable.ENVVAR_PREPEND:
		case IBuildEnvironmentVariable.ENVVAR_APPEND:
			fAppPrepValue = fVarValueEdit.getText();
			fReplaceValue = null;
			break;
		case IBuildEnvironmentVariable.ENVVAR_REMOVE:
			break;
		case IBuildEnvironmentVariable.ENVVAR_REPLACE:
		default:
			fReplaceValue = fVarValueEdit.getText();
			fAppPrepValue = null;
			break;
		}
	}
	
	/*
	 * called when the operation is modified
	 */
	private void handleOperationModified(){
		int op = getSelectedOperation();
		String newValue = recalculateValueString();
			
		switch(op){
			case IBuildEnvironmentVariable.ENVVAR_PREPEND:
				fVarValueEdit.setText(calculateAppendPrepend(
						getSelectedVarName(),
						newValue,
						fDelimiterEdit.getText(),
						true));
				fVarValueEdit.setEnabled(false);
				fOpVarValueEdit.setText(newValue);
				fOpVarValueLabel.setText(ManagedBuilderUIMessages.getResourceString(VALUE_PREPEND));
				fOpVarValueLabel.setVisible(true);
				fOpVarValueEdit.setVisible(true);
				fDelimiterEdit.setEnabled(true);
				fAppPrepPrepend = true;
				fAppPrepValue = newValue;
				fReplaceValue = null;
				break;
			case IBuildEnvironmentVariable.ENVVAR_APPEND:
				fVarValueEdit.setText(calculateAppendPrepend(
						getSelectedVarName(),
						newValue,
						fDelimiterEdit.getText(),
						false));			
				fVarValueEdit.setEnabled(false);
				fOpVarValueEdit.setText(newValue);
				fOpVarValueLabel.setText(ManagedBuilderUIMessages.getResourceString(VALUE_APPEND));
				fOpVarValueLabel.setVisible(true);
				fOpVarValueEdit.setVisible(true);
				fDelimiterEdit.setEnabled(true);
				fAppPrepPrepend = false;
				fAppPrepValue = newValue;
				fReplaceValue = null;
				break;
			case IBuildEnvironmentVariable.ENVVAR_REMOVE:
				fOpVarValueLabel.setVisible(false);
				fOpVarValueEdit.setVisible(false);
				fDelimiterEdit.setEnabled(false);
				fVarValueEdit.setText(EMPTY_STRING);
				fVarValueEdit.setEnabled(false);
				break;
			case IBuildEnvironmentVariable.ENVVAR_REPLACE:
			default:
				fVarValueEdit.setText(newValue);
				fOpVarValueLabel.setVisible(false);
				fOpVarValueEdit.setVisible(false);
				fDelimiterEdit.setEnabled(true);
				fVarValueEdit.setEnabled(true);
				fAppPrepValue = null;
				fReplaceValue = newValue;


				break;
		}
		
		fOpVarValueLabel.getParent().layout(true);
	}
	
	private String recalculateValueString(){
		String val = EMPTY_STRING;
		
		switch(getSelectedOperation()){
		case IBuildEnvironmentVariable.ENVVAR_PREPEND:		
			if(fAppPrepValue != null)
				val = fAppPrepValue;
			else if(fReplaceValue != null)
				val = calculateAppPrepFromReplace(fReplaceValue,fDelimiterEdit.getText(),true);
			break;
		case IBuildEnvironmentVariable.ENVVAR_APPEND:
			if(fAppPrepValue != null)
				val = fAppPrepValue;
			else if(fReplaceValue != null)
				val = calculateAppPrepFromReplace(fReplaceValue,fDelimiterEdit.getText(),false);
			break;
		case IBuildEnvironmentVariable.ENVVAR_REMOVE:
			val = EMPTY_STRING;
			break;
		case IBuildEnvironmentVariable.ENVVAR_REPLACE:
		default:
			if(fReplaceValue != null)
				val = fReplaceValue;
			else if(fAppPrepValue != null)
				val = fReplaceValue = calculateReplaceFromAppPrep(fAppPrepValue,fDelimiterEdit.getText(),fAppPrepPrepend);
			break;
		}
		return val;
	}
	
	private String calculateAppPrepFromReplace(String replace, String delimiter,  boolean prepend){
		String val = replace;
		IBuildEnvironmentVariable var = fEnvVarBlock.getSystemVariable(getSelectedVarName(),true);
		if(var != null && var.getOperation() != IBuildEnvironmentVariable.ENVVAR_REMOVE){
			String varValue = var.getValue();
			if(delimiter != null && !EMPTY_STRING.equals(delimiter)){
				List replaceValues = EnvVarOperationProcessor.convertToList(replace,delimiter);
				List varValues =  EnvVarOperationProcessor.convertToList(varValue,delimiter);
				List result = EnvVarOperationProcessor.removeDuplicates(replaceValues,varValues);
				val = EnvVarOperationProcessor.convertToString(result,delimiter);
			}
			else{
				if(varValue != null && !EMPTY_STRING.equals(varValue)){
					int index = replace.indexOf(varValue);
					if(index == -1)
						val = EMPTY_STRING;
					else {
						try{
							if(index == 0)
								val = replace.substring(replace.length());
							else
								val = replace.substring(0,index);
							
						} catch (IndexOutOfBoundsException e){
							val = EMPTY_STRING;
						}
					}
				}
			}
		}
		return val;
	}

	private String calculateReplaceFromAppPrep(String value, String delimiter, boolean prepend){
		return calculateAppendPrepend(getSelectedVarName(),value,delimiter,prepend);
	}

	/*
	 * updates the state of the dialog controls
	 */
	private void updateWidgetState(){
		handleOperationModified();
		validateState();
	}
	
	/*
	 * updates the variable value displayed in the dialog in case of append/prepend operation
	 */
	private void updateVariableValueForAppendPrepend(boolean prepend){
		String name = getSelectedVarName();
		if(name == null || EMPTY_STRING.equals(name))
			return;
		
		String opValue = fOpVarValueEdit.getText();
	
		fVarValueEdit.setText(calculateAppendPrepend(name,opValue,fDelimiterEdit.getText(),prepend));
	}
	
	/*
	 * calculates the resulting variable value
	 */
	private String calculateAppendPrepend(String name, String opValue, String delimiter, boolean prepend){
		String varValue = null;
		IBuildEnvironmentVariable var = fEnvVarBlock.getSystemVariable(name,true);
		if(var == null)
			return opValue;
		return EnvVarOperationProcessor.performAppendPrepend(var.getValue(),opValue,delimiter,prepend);
	}

	/*
	 * called when the delimiter is modified
	 */
	private void handleDelimiterModified(){
		int op = getSelectedOperation();
		switch(op){
		case IBuildEnvironmentVariable.ENVVAR_PREPEND:{
				String value = getSelectedVariableValue();
				fVarValueEdit.setText(calculateAppendPrepend(
						getSelectedVarName(),
						value,
						fDelimiterEdit.getText(),
						true));
				fAppPrepValue = value;
				fReplaceValue = null;
			}
			break;
		case IBuildEnvironmentVariable.ENVVAR_APPEND:{
				String value = getSelectedVariableValue();
				fVarValueEdit.setText(calculateAppendPrepend(
						getSelectedVarName(),
						value,
						fDelimiterEdit.getText(),
						false));
				fAppPrepValue = value;
				fReplaceValue = null;
			}
			break;
		case IBuildEnvironmentVariable.ENVVAR_REMOVE:
			break;
		case IBuildEnvironmentVariable.ENVVAR_REPLACE:
		default:
			fAppPrepValue = null;
			fReplaceValue = getSelectedVariableValue();
			break;
		}
	}
	
	/*
	 * called when the appended/prepended value is modified
	 */
	private void handleOperationVarValueModified(){
		handleDelimiterModified();
	}
	
	/* (non-Javadoc)
	 * Update the status message and button state based on the variable name
	 * 
	 */
	private void validateState() {
		StatusInfo status= new StatusInfo();
		String name = getSelectedVarName();
		
		if(EMPTY_STRING.equals(name)){
			// Not an error
			status.setError("");	//$NON-NLS-1$
		}
		else if(!fEnvVarBlock.canCreate(name)){
			status.setError(ManagedBuilderUIMessages.getFormattedString(STATUS_CANNOT_CTREATE, name));
		}

		
		updateStatus(status);
		return;
	}
}
	
