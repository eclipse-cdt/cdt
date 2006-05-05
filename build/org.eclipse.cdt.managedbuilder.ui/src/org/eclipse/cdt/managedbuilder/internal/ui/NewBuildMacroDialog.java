/*******************************************************************************
 * Copyright (c) 2005, 2006 Intel Corporation and others.
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

import org.eclipse.cdt.internal.ui.dialogs.StatusDialog;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacro;
import org.eclipse.cdt.managedbuilder.internal.macros.MacroResolver;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.ui.properties.FileListControl;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/*
 * the dialog used to create or edit the build macro
 */
public class NewBuildMacroDialog extends StatusDialog {
	// String constants
	private static final String PREFIX = "NewBuildMacroDialog";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String NAME = LABEL + ".name";	//$NON-NLS-1$
	private static final String VALUE = LABEL + ".value";	//$NON-NLS-1$

	private static final String TYPE = LABEL + ".type";	//$NON-NLS-1$
	private static final String TYPE_TEXT = TYPE + ".text";	//$NON-NLS-1$
	private static final String TYPE_TEXT_LIST = TYPE + ".text.list";	//$NON-NLS-1$
	private static final String TYPE_PATH_FILE = TYPE + ".path.file";	//$NON-NLS-1$
	private static final String TYPE_PATH_FILE_LIST = TYPE + ".path.file.list";	//$NON-NLS-1$
	private static final String TYPE_PATH_DIR = TYPE + ".path.dir";	//$NON-NLS-1$
	private static final String TYPE_PATH_DIR_LIST = TYPE + ".path.dir.list";	//$NON-NLS-1$
	private static final String TYPE_PATH_ANY = TYPE + ".path.any";	//$NON-NLS-1$
	private static final String TYPE_PATH_ANY_LIST = TYPE + ".path.any.list";	//$NON-NLS-1$
	

	private static final String BROWSE = LABEL + ".browse";	//$NON-NLS-1$
	private static final String LIST_TITLE = LABEL + ".list.title";	//$NON-NLS-1$

	private static final String TITLE_NEW = LABEL + ".title.new"; 	//$NON-NLS-1$
	private static final String TITLE_EDIT = LABEL + ".title.edit"; 	//$NON-NLS-1$
	
	private static final String STATUS = LABEL + ".status"; 	//$NON-NLS-1$
	private static final String STATUS_CANNOT_CTREATE = STATUS + ".cannot.create"; 	//$NON-NLS-1$
	
	private static final String EMPTY_STRING = new String();

	// The title of the dialog.
	private String fTitle;
	// hold the macro being edited(in the case of the "edit" mode)
	private IBuildMacro fEditedMacro;
	//the macros block block from which the dialog was called
	private MacrosBlock fMacrosBlock;
	//the resulting macro. Can be accessed only when the dialog is closed
	private IBuildMacro fResultingMacro;
	//the string that holds the value is used in the "replace" operation
	private String fReplaceValue = null;
	//the string that holds the value is used in the "append/prepend" operations
	private String fAppPrepValue = null;
	//specifies whether the fAppPrepValue holds the prepended or appended value
	private boolean fAppPrepPrepend = true;
	
	private boolean fTotalSizeCalculated;

	private String fTypedName;
	private int fTypedType = -1;

	// Widgets
//	protected Text fMacroNameEdit;
	private Composite fContainer;
	private Combo fMacroNameEdit;
	private Label fMacroValueLabel;
	private Text fMacroValueEdit;
	private Button fBrowseButton;
	private Combo fTypeSelector;
	private Composite fListEditorContainier;
	
	private FileListControl fListEditor;


	public NewBuildMacroDialog(Shell parentShell, MacrosBlock macrosBlock, IBuildMacro editedMacro) {
		super(parentShell);
		if(editedMacro != null)
			fTitle = ManagedBuilderUIMessages.getResourceString(TITLE_EDIT);
		else
			fTitle = ManagedBuilderUIMessages.getResourceString(TITLE_NEW);
		fEditedMacro = editedMacro;
		fMacrosBlock = macrosBlock;
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
		comp.setLayout(new GridLayout(3, false));
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);

		fContainer = comp;

		Label nameLabel = new Label(comp, SWT.LEFT);
		nameLabel.setFont(comp.getFont());
		nameLabel.setText(ManagedBuilderUIMessages.getResourceString(NAME));
		nameLabel.setLayoutData(new GridData());
		
//		fMacroNameEdit = new Text(comp, SWT.SINGLE | SWT.BORDER);
		fMacroNameEdit = new Combo(comp, SWT.SINGLE | SWT.DROP_DOWN);
		fMacroNameEdit.setItems(getMacroNames());
		fMacroNameEdit.setFont(comp.getFont());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH + 50;
		fMacroNameEdit.setLayoutData(gd);
		fMacroNameEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleMacroNameModified();
			}
		});
		fMacroNameEdit.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				handleMacroNameSelection();
			}
		});
		
		Label typeLabel = new Label(comp, SWT.LEFT);
		typeLabel.setFont(comp.getFont());
		typeLabel.setText(ManagedBuilderUIMessages.getResourceString(TYPE));
		gd = new GridData();
		gd.horizontalSpan = 1;		
		typeLabel.setLayoutData(gd);
		
		fTypeSelector = new Combo(comp, SWT.READ_ONLY|SWT.DROP_DOWN);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
//		gd.widthHint = 100;
		fTypeSelector.setLayoutData(gd);
		fTypeSelector.setItems(new String[]{
				ManagedBuilderUIMessages.getResourceString(TYPE_TEXT),
				ManagedBuilderUIMessages.getResourceString(TYPE_TEXT_LIST),
				ManagedBuilderUIMessages.getResourceString(TYPE_PATH_FILE),
				ManagedBuilderUIMessages.getResourceString(TYPE_PATH_FILE_LIST),
				ManagedBuilderUIMessages.getResourceString(TYPE_PATH_DIR),
				ManagedBuilderUIMessages.getResourceString(TYPE_PATH_DIR_LIST),
				ManagedBuilderUIMessages.getResourceString(TYPE_PATH_ANY),
				ManagedBuilderUIMessages.getResourceString(TYPE_PATH_ANY_LIST)
		});
		setSelectedType(IBuildMacro.VALUE_TEXT);

		fTypeSelector.addListener(SWT.Selection, new Listener () {
			public void handleEvent(Event e) {
				handleTypeModified();
			}
		});

		fMacroValueLabel = new Label(comp, SWT.LEFT);
		fMacroValueLabel.setFont(comp.getFont());
		fMacroValueLabel.setText(ManagedBuilderUIMessages.getResourceString(VALUE));
		gd = new GridData();
		gd.horizontalSpan = 1;		
		fMacroValueLabel.setLayoutData(gd);
		
		fMacroValueEdit = new Text(comp, SWT.SINGLE | SWT.BORDER);
		fMacroValueEdit.setFont(comp.getFont());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH + 100;
		gd.horizontalSpan = 1;
		fMacroValueEdit.setLayoutData(gd);
		fMacroValueEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleMacroValueModified();
			}
		});

		fBrowseButton = new Button(comp,SWT.PUSH);
		fBrowseButton.setFont(comp.getFont());
		fBrowseButton.setText(ManagedBuilderUIMessages.getResourceString(BROWSE));
		fBrowseButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				handleBrowseButtonPressed();
			}
		});
		
		gd = new GridData();
		gd.widthHint = IDialogConstants.BUTTON_WIDTH;
		gd.horizontalSpan = 1;
		fBrowseButton.setLayoutData(gd);
		
		fListEditorContainier = new Composite(comp,0);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		fListEditorContainier.setLayoutData(gd);
		fListEditorContainier.setLayout(new GridLayout());

		fListEditor = new FileListControl(fListEditorContainier, ManagedBuilderUIMessages.getResourceString(LIST_TITLE), IOption.BROWSE_NONE);
		/* Enable workspace support for list editor */
		fListEditor.setWorkspaceSupport(true);
		
		fListEditor.setContext(fMacrosBlock.getContextInfo());

		if(fEditedMacro != null){
			loadMacroSettings(fEditedMacro,true);
			fMacroNameEdit.setEnabled(false);
		}

		return comp;
	}
	
	/*
	 * get the names to be displayed in the var Name combo.
	 */
	private String[] getMacroNames(){
		IBuildMacro macros[] = fMacrosBlock.getSystemMacros(true);
		String names[] = null;
		if(macros == null || macros.length == 0)
			names = new String[0];
		else{
			names = new String[macros.length];
			for(int i = 0; i < macros.length; i++){
				names[i] = macros[i].getName();
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

	private void handleBrowseButtonPressed(){
		switch(getBrowseType(getSelectedType())){
		case IOption.BROWSE_FILE:
			FileDialog fileDlg = new FileDialog(fBrowseButton.getShell());
			String file = fileDlg.open();
			if(file != null)
				fMacroValueEdit.setText(file);
			break;
		case IOption.BROWSE_DIR:
			DirectoryDialog dirDlg = new DirectoryDialog(fBrowseButton.getShell());
			String dir = dirDlg.open();
			if(dir != null)
				fMacroValueEdit.setText(dir);
			break;
		}
	}
	
	private int getBrowseType(int type){
		int browseType = IOption.BROWSE_NONE;
		switch(type){
		case IBuildMacro.VALUE_PATH_FILE:
		case IBuildMacro.VALUE_PATH_FILE_LIST:
			browseType = IOption.BROWSE_FILE;
			break;
		case IBuildMacro.VALUE_PATH_DIR:
		case IBuildMacro.VALUE_PATH_DIR_LIST:
			browseType = IOption.BROWSE_DIR;
			break;
		case IBuildMacro.VALUE_PATH_ANY:
		case IBuildMacro.VALUE_PATH_ANY_LIST:
			break;
		case IBuildMacro.VALUE_TEXT:
		case IBuildMacro.VALUE_TEXT_LIST:
		default:
		}
		return browseType;

	}

	/*
	 * called when the variable name is selected, loads all the dialog fields with the variable settings
	 */
	private void handleMacroNameSelection(){
		int index = fMacroNameEdit.getSelectionIndex();
		if(index == -1)
			loadMacroSettings(null);
		else
			loadMacroSettings(fMacroNameEdit.getItem(index));
	}
	
	private void loadMacroSettings(String name){
		IBuildMacro macro = fMacrosBlock.getSystemMacro(name,true);
		if(macro != null)
			loadMacroSettings(macro,false);
		else
			loadMacroSettings(name,IBuildMacro.VALUE_TEXT,EMPTY_STRING);
	}
	
	private void loadMacroSettings(String name,
			int type,
			String value[]){
		setSelectedType(type);
		setSelectedMacroName(notNull(name));
		fListEditor.setList(value);

		updateWidgetState();
	}
	
	private void loadMacroSettings(String name,
					int type,
					String value){

		setSelectedType(type);
		setSelectedMacroName(notNull(name));
		fMacroValueEdit.setText(notNull(value));

		updateWidgetState();

	}
	
	/*
	 * loads all the dialog fields with the variable settings
	 */
	private void loadMacroSettings(IBuildMacro macro, boolean isUser){
		try{
			if(MacroResolver.isStringListMacro(macro.getMacroValueType()))
				loadMacroSettings(macro.getName(),macro.getMacroValueType(),macro.getStringListValue());
			else
				loadMacroSettings(macro.getName(),macro.getMacroValueType(),macro.getStringValue());
		}catch(BuildMacroException e){
		}
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
		return fMacroNameEdit.getText().trim();
	}
	
	/*
	 * sets the variable name to the dialog "variable name" edit control
	 */
	private void setSelectedMacroName(String name){
		if(!macroNamesEqual(fMacroNameEdit.getText(),name)){
			fTypedName = name;
			fMacroNameEdit.setText(notNull(name).trim());
		}
	}
	
	private boolean macroNamesEqual(String name1, String name2){
		name1 = name1.trim();
		name2 = name2.trim();
		return name1.equalsIgnoreCase(name2);
	}

	/*
	 * returns the selected type
	 */
	private int getSelectedType(){
		switch(fTypeSelector.getSelectionIndex()){
			case 1:
				return IBuildMacro.VALUE_TEXT_LIST;
			case 2:
				return IBuildMacro.VALUE_PATH_FILE;
			case 3:
				return IBuildMacro.VALUE_PATH_FILE_LIST;
			case 4:
				return IBuildMacro.VALUE_PATH_DIR;
			case 5:
				return IBuildMacro.VALUE_PATH_DIR_LIST;
			case 6:
				return IBuildMacro.VALUE_PATH_ANY;
			case 7:
				return IBuildMacro.VALUE_PATH_ANY_LIST;
			case 0:
			default:
				return IBuildMacro.VALUE_TEXT;
		}
	}
	
	/*
	 * sets the selected type
	 */
	private void setSelectedType(int type){
		switch(type){
		case IBuildMacro.VALUE_TEXT_LIST:
			fTypeSelector.select(1);
			break;
		case IBuildMacro.VALUE_PATH_FILE:
			fTypeSelector.select(2);
			break;
		case IBuildMacro.VALUE_PATH_FILE_LIST:
			fTypeSelector.select(3);
			break;
		case IBuildMacro.VALUE_PATH_DIR:
			fTypeSelector.select(4);
			break;
		case IBuildMacro.VALUE_PATH_DIR_LIST:
			fTypeSelector.select(5);
			break;
		case IBuildMacro.VALUE_PATH_ANY:
			fTypeSelector.select(6);
			break;
		case IBuildMacro.VALUE_PATH_ANY_LIST:
			fTypeSelector.select(7);
			break;
		case IBuildMacro.VALUE_TEXT:
		default:
			fTypeSelector.select(0);
			break;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed(){
		String name = getSelectedVarName();
		if(name != null || !EMPTY_STRING.equals(name)){
			int type = getSelectedType();
			if(MacroResolver.isStringListMacro(type))
				fResultingMacro = new BuildMacro(name,type,getSelectedStringListValue());
			else
				fResultingMacro = new BuildMacro(name,type,getSelectedStringValue());
		}

		super.okPressed();
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open(){
		fResultingMacro = null;
		return super.open();
	}
	
	/*
	 * returns the macro value that should be stored in the resulting variable
	 */
	private String getSelectedStringValue(){
		return fMacroValueEdit.getText();
	}

	private String[] getSelectedStringListValue(){
		return fListEditor.getItems();
	}

	/*
	 * this method should be called after the dialog is closed
	 * to obtain the created variable.
	 * if the variable was not created, e.g. because a user has pressed 
	 * the cancel button this method returns null
	 */
	public IBuildMacro getDefinedMacro(){
		return fResultingMacro;
	}
	
	/*
	 * called when the variable name is modified
	 */
	private void handleMacroNameModified(){
		String name = getSelectedVarName();
		if(fTypedName == null || !fTypedName.equals(name)){
			loadMacroSettings(name);
		}
	}
	
	/*
	 * called when the macro value is modified
	 */
	private void handleMacroValueModified(){

	}
	
	/*
	 * called when the operation is modified
	 */
	private void handleTypeModified(){
		int type = getSelectedType();
		if(fTypedType != -1 && fTypedType == type)
			return;
	
		fTypedType = type;

		adjustLayout(type);
	}
	
	private void adjustLayout(int type){
		GridData listGd = (GridData)fListEditorContainier.getLayoutData();
		GridData labelGd = (GridData)fMacroValueLabel.getLayoutData();
		GridData editGd = (GridData)fMacroValueEdit.getLayoutData();
		GridData browseGd = (GridData)fBrowseButton.getLayoutData();

		if(MacroResolver.isStringListMacro(type)){
			listGd.exclude = false;
			labelGd.exclude = true;
			editGd.exclude = true;
			browseGd.exclude = true;
			fListEditorContainier.setVisible(true);
			fListEditor.setType(getBrowseType(type));
			fMacroValueLabel.setVisible(false);
			fMacroValueEdit.setVisible(false);
			fBrowseButton.setVisible(false);
		} else {
			listGd.exclude = true;
			labelGd.exclude = false;
			editGd.exclude = false;
			int editSpan;
			fListEditorContainier.setVisible(false);
			fMacroValueLabel.setVisible(true);
			fMacroValueEdit.setVisible(true);
			if(getBrowseType(type) != IOption.BROWSE_NONE){
				browseGd.exclude = false;
				editSpan = 1;
				fBrowseButton.setVisible(true);
			} else {
				browseGd.exclude = true;
				editSpan = 2;
				fBrowseButton.setVisible(false);
			}
			editGd.horizontalSpan = editSpan;
		}
		fContainer.layout(true,true);
	}
	
	/*
	 * updates the state of the dialog controls
	 */
	private void updateWidgetState(){
		if(!fTotalSizeCalculated)
			return;
		handleTypeModified();
		validateState();
	}
	
	protected Point getInitialSize() {
		Point size = super.getInitialSize();
		fTotalSizeCalculated = true;
		updateWidgetState();
		return size;
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
		else if(!fMacrosBlock.canCreate(name)){
			status.setError(ManagedBuilderUIMessages.getFormattedString(STATUS_CANNOT_CTREATE, name));
		}

		
		updateStatus(status);
		return;
	}

}
