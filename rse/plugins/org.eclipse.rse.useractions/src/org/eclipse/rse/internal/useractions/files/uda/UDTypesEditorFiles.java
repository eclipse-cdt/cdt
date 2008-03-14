/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared                              
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.files.uda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.rse.internal.useractions.Activator;
import org.eclipse.rse.internal.useractions.IUserActionsMessageIds;
import org.eclipse.rse.internal.useractions.UserActionsResources;
import org.eclipse.rse.internal.useractions.ui.uda.ISystemUDTypeEditPaneTypesSelector;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FileEditorMappingContentProvider;
import org.eclipse.ui.dialogs.FileEditorMappingLabelProvider;

/**
 * This class implements the interface needed to supply 
 * custom widgets allowing the user to specify the file types
 * for a named file type, in the Work With File Types dialog
 */
public class UDTypesEditorFiles implements ISystemUDTypeEditPaneTypesSelector, ICheckStateListener {
	public static final String NO_EXTENSION_PLACEHOLDER = UDActionSubsystemFiles.NO_EXTENSION_PLACEHOLDER;
	// gui widgets
	private CheckboxTableViewer typesSelectionList;
	//private Label               typesSelectionListVerbage;	
	//private Button              addTypesButton;
	//private Text                definedTypesText;
	private Label definedTypesLabel;
	private Text userDefinedText;
	private Label nonEditableVerbage;
	private Composite typesComposite;
	// state
	private java.util.List inpTypes;
	private IFileEditorMapping[] currentInput;
	private int currentDomain;
	private boolean ignoreModifyEvents = false;
	// constants
	private static final int LIST_HEIGHT = 150;
	private static final int LIST_WIDTH = 50;
	private static final String TYPE_DELIMITER = ","; //GenericMessages.getString("TypesFiltering.typeDelimiter"); //$NON-NLS-1$
	// registered listeners
	private Vector listeners = new Vector();

	/**
	 * Constructor for UDTypesEditorFiles.
	 */
	public UDTypesEditorFiles(Composite comp, int nbrColumns) //, SystemPromptDialog parentDialog) 
	{
		super();
		// List of currently selected types if given parent composite has 2 columns
		if (nbrColumns == 2) {
			//definedTypesText = 
			//   SystemWidgetHelpers.createLabeledTextField(comp, null, rb, RESID_UDT_FILES_DEFINEDTYPES_ROOT);        
			definedTypesLabel = SystemWidgetHelpers.createLabeledLabel(comp, SystemUDAResources.RESID_UDT_FILES_DEFINEDTYPES_LABEL, SystemUDAResources.RESID_UDT_FILES_DEFINEDTYPES_TOOLTIP, true);
		}
		nonEditableVerbage = SystemWidgetHelpers.createVerbiage(comp, "", nbrColumns, false, 350); //$NON-NLS-1$
		nonEditableVerbage.setVisible(false);
		//typesComposite = SystemWidgetHelpers.createGroupComposite(comp, 1, rb.getString(RESID_UDT_FILES_TYPESGROUP_ROOT_LABEL);
		typesComposite = SystemWidgetHelpers.createTightComposite(comp, 1);
		typesComposite.setToolTipText(SystemUDAResources.RESID_UDT_FILES_TYPESGROUP_TOOLTIP);
		((GridData) typesComposite.getLayoutData()).horizontalSpan = nbrColumns;
		nbrColumns = 1;
		// List of currently selected types if given parent composite did not have 2 columns
		if (definedTypesLabel == null)
		//definedTypesText = 
			//   SystemWidgetHelpers.createLabeledTextField(typesComposite, null, rb, RESID_UDT_FILES_DEFINEDTYPES_ROOT);        		
			definedTypesLabel = SystemWidgetHelpers.createLabeledLabel(typesComposite, SystemUDAResources.RESID_UDT_FILES_DEFINEDTYPES_LABEL, SystemUDAResources.RESID_UDT_FILES_DEFINEDTYPES_TOOLTIP,
					true);
		//definedTypesLabel.setEnabled(false);
		definedTypesLabel.setForeground(definedTypesLabel.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		//definedTypesText = 
		//     SystemWidgetHelpers.createReadonlyTextField(typesComposite, rb, RESID_UDT_FILES_DEFINEDTYPES_ROOT);        		
		//definedTypesText.setToolTipText("");        
		//definedTypesText.setVisible(false);
		// types selection label
		//typesSelectionListVerbage = 
		//   SystemWidgetHelpers.createLabel(typesComposite, rb, RESID_UDT_TYPESLIST_LABEL_ROOT, nbrColumns, false);
		// types selection list
		//typesSelectionList = CheckboxTableViewer.newCheckList(comp, SWT.BORDER);
		Table table = new Table(typesComposite, SWT.CHECK | SWT.BORDER);
		table.setToolTipText(SystemUDAResources.RESID_UDT_FILES_TYPESGROUP_TOOLTIP);
		typesSelectionList = new CheckboxTableViewer(table);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = nbrColumns;
		data.heightHint = LIST_HEIGHT;
		data.widthHint = LIST_WIDTH;
		//data.grabExcessHorizontalSpace = false;
		//data.grabExcessVerticalSpace = false;
		typesSelectionList.getTable().setLayoutData(data);
		typesSelectionList.setLabelProvider(FileEditorMappingLabelProvider.INSTANCE);
		typesSelectionList.setContentProvider(FileEditorMappingContentProvider.INSTANCE);
		addSelectionButtons(typesComposite);
		Composite userComp = createUserEntryGroup(typesComposite);
		((GridData) userComp.getLayoutData()).horizontalSpan = nbrColumns;
		// configure widgets...
		initializeViewer();
		//if ((this.initialSelections != null) && !this.initialSelections.isEmpty())
		//	checkInitialSelections();
		typesSelectionList.addCheckStateListener(this);
	}

	/**
	 * Set domain.
	 * The edit pane may possibly appear differently, depending on the domain.
	 * When the domain changes (either in "new" or "edit" mode) this method is called.
	 */
	public void setDomain(int domain) {
		this.currentDomain = domain;
	}

	/**
	 * Set the message line for issuing msgs to
	 */
	public void setMessageLine(ISystemMessageLine msgLine) {
	}

	/**
	 * Return the domain of the currently selected existing named type, or "new" node
	 */
	public int getDomain() {
		return currentDomain;
	}

	/**
	 * @see org.eclipse.rse.internal.useractions.ui.uda.ISystemUDTypeEditPaneTypesSelector#setTypes(String)
	 */
	public void setTypes(String types) {
		//this.inpTypes = types;
		if (types == null) {
			setTypes((String[]) null);
		} else {
			setTypes(RemoteFileFilterString.parseTypes(types));
			setDefinedTypesText(types);
		}
	}

	/**
	 * Set defined types text
	 */
	private void setDefinedTypesText(String types) {
		definedTypesLabel.setText(" " + types); //$NON-NLS-1$
		definedTypesLabel.setToolTipText(types);
		//definedTypesText.setText(types);		
	}

	/**
	 * Clear defined types text
	 */
	private void clearDefinedTypesText() {
		definedTypesLabel.setText(""); //$NON-NLS-1$
		definedTypesLabel.setToolTipText(""); //$NON-NLS-1$
		//definedTypesText.setText("");		
	}

	/**
	 * Set the types via an array
	 */
	private void setTypes(String[] types) {
		clearTypes();
		if (types != null) {
			this.inpTypes = Arrays.asList(types);
			ignoreModifyEvents = true;
			checkInitialSelections();
			ignoreModifyEvents = false;
		}
	}

	/**
	 * @see org.eclipse.rse.internal.useractions.ui.uda.ISystemUDTypeEditPaneTypesSelector#clearTypes()
	 */
	public void clearTypes() {
		ignoreModifyEvents = true;
		this.inpTypes = null;
		typesSelectionList.setAllChecked(false);
		clearDefinedTypesText();
		userDefinedText.setText(""); //$NON-NLS-1$
		ignoreModifyEvents = false;
	}

	/**
	 * @see org.eclipse.rse.internal.useractions.ui.uda.ISystemUDTypeEditPaneTypesSelector#getTypes()
	 */
	public String getTypes() {
		return RemoteFileFilterString.getTypesString(getTypesAsArray());
	}

	/**
	 * @see org.eclipse.rse.internal.useractions.ui.uda.ISystemUDTypeEditPaneTypesSelector#getTypes()
	 */
	public String[] getTypesAsArray() {
		java.util.List selectedEntries = getSelectedTypes();
		String[] seldArray = new String[selectedEntries.size()];
		for (int idx = 0; idx < seldArray.length; idx++)
			seldArray[idx] = (String) selectedEntries.get(idx);
		return seldArray;
	}

	/**
	 * @see org.eclipse.rse.internal.useractions.ui.uda.ISystemUDTypeEditPaneTypesSelector#addModifyListener(ModifyListener)
	 */
	public void addModifyListener(ModifyListener listener) {
		listeners.add(listener);
	}

	/**
	 * @see org.eclipse.rse.internal.useractions.ui.uda.ISystemUDTypeEditPaneTypesSelector#removeModifyListener(ModifyListener)
	 */
	public void removeModifyListener(ModifyListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @see org.eclipse.rse.internal.useractions.ui.uda.ISystemUDTypeEditPaneTypesSelector#validate()
	 */
	public SystemMessage validate() {
		if (typesSelectionList == null) return null;
		
		
		
		if (!areTypesSelected()) {
			return new SimpleSystemMessage(Activator.PLUGIN_ID, 
					IUserActionsMessageIds.MSG_VALIDATE_UDTTYPES_EMPTY,
					IStatus.ERROR, 
					UserActionsResources.MSG_VALIDATE_UDTTYPES_EMPTY, 
					UserActionsResources.MSG_VALIDATE_UDTTYPES_EMPTY_DETAILS);
		}
		// validate that user-defined entry field!
		return validateUserDefinedTypes();
	}

	/**
	 * Validate the contents of the user-defined types entry field
	 */
	public SystemMessage validateUserDefinedTypes() {
		String filename = userDefinedText.getText().trim();
		// copied from SystemSelectFileTypes...
		// check for empty name and extension
		if (filename.length() == 0) return null;
		// check for empty extension if there is no name
		int index = filename.indexOf('.');
		if (index == filename.length() - 1) {
			if (index == 0 || (index == 1 && filename.charAt(0) == '*')) {
				return new SimpleSystemMessage(Activator.PLUGIN_ID, 
						IUserActionsMessageIds.MSG_VALIDATE_UDTTYPES_NOTVALID,
						IStatus.ERROR, 
						UserActionsResources.MSG_VALIDATE_UDTTYPES_NOTVALID, 
						UserActionsResources.MSG_VALIDATE_UDTTYPES_NOTVALID_DETAILS);
			}
		}
		int startScan = 0;
		if (filename.startsWith("*.")) //$NON-NLS-1$
			startScan = 2;
		// check for characters before * 
		// or no other characters
		// or next character not '.'
		index = filename.indexOf('*', startScan);
		if (index > -1) {
			if (filename.length() == 1) {
				return new SimpleSystemMessage(Activator.PLUGIN_ID, 
						IUserActionsMessageIds.MSG_VALIDATE_UDTTYPES_NOTVALID,
						IStatus.ERROR, 
						UserActionsResources.MSG_VALIDATE_UDTTYPES_NOTVALID, 
						UserActionsResources.MSG_VALIDATE_UDTTYPES_NOTVALID_DETAILS);
			}
			if (index != 0 || filename.charAt(1) != '.') {
				return new SimpleSystemMessage(Activator.PLUGIN_ID, 						
						IUserActionsMessageIds.MSG_VALIDATE_UDTTYPES_NOTVALID,
						IStatus.ERROR, 
						UserActionsResources.MSG_VALIDATE_UDTTYPES_NOTVALID, 
						UserActionsResources.MSG_VALIDATE_UDTTYPES_NOTVALID_DETAILS);
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.rse.internal.useractions.ui.uda.ISystemUDTypeEditPaneTypesSelector#getControl()
	 */
	public Control getControl() {
		return typesSelectionList.getControl();
	}

	/**
	 * Enable or disable the input-capability of the constituent controls
	 */
	public void setEnabled(boolean enable) {
		typesSelectionList.getControl().setEnabled(enable);
		userDefinedText.setEnabled(enable);
	}

	/**
	 * We want to disable editing of IBM or vendor-supplied 
	 * types, so when one of these is selected, this method is
	 * called to enter non-editable mode. 
	 * @param editable Whether to disable editing of this type or not
	 * @param vendor When disabling, it contains the name of the vendor for substitution purposes
	 */
	public void setEditable(boolean editable, String vendor) {
		//setEnabled(editable);
		typesComposite.setVisible(editable);
		if (editable)
			nonEditableVerbage.setVisible(false);
		else {
			nonEditableVerbage.setVisible(true);
			if (vendor.equals("IBM")) //$NON-NLS-1$
				nonEditableVerbage.setText(SystemUDAResources.RESID_UDT_IBM_VERBAGE);
			else {
				String verbage = SystemUDAResources.RESID_UDT_VENDOR_VERBAGE;
				verbage = SystemMessage.sub(verbage, "%1", vendor); //$NON-NLS-1$
				nonEditableVerbage.setText(verbage);
			}
		}
	}

	// private methods...
	/**
	 * Fire event to all listeners...
	 */
	private void fireModifiedEvent() {
		Event event = new Event();
		event.widget = getControl();
		event.type = SWT.Modify;
		event.data = this;
		ModifyEvent mEvent = new ModifyEvent(event);
		for (int idx = 0; idx < listeners.size(); idx++) {
			ModifyListener l = (ModifyListener) listeners.elementAt(idx);
			//System.out.println("...firing modify event");
			l.modifyText(mEvent);
		}
		setDefinedTypesText(getTypes());
	}

	/**
	 * From ICheckStateListener interface.
	 * Called when user checks/unchecks an item
	 */
	public void checkStateChanged(CheckStateChangedEvent event) {
		//System.out.println("inside checkStateChanged");
		if (!ignoreModifyEvents) fireModifiedEvent();
		ignoreModifyEvents = false; // non-sticky
	}

	// --------------------------------------------
	// Similar to org.eclipse.ui.dialogs.TypeFilteringDialog
	// --------------------------------------------
	/**
	 * Add the selection and deselection buttons to the dialog.
	 * @param composite org.eclipse.swt.widgets.Composite
	 */
	private void addSelectionButtons(Composite composite) {
		/*
		 Composite buttonComposite = new Composite(composite, SWT.RIGHT);
		 GridLayout layout = new GridLayout();
		 layout.numColumns = 2;
		 buttonComposite.setLayout(layout);
		 GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		 data.grabExcessHorizontalSpace = true;
		 composite.setData(data);

		 Button selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID, GenericMessages.getString("WizardTransferPage.selectAll"), false); //$NON-NLS-1$

		 SelectionListener listener = new SelectionAdapter() 
		 {
		 public void widgetSelected(SelectionEvent e) {
		 typesSelectionList.setAllChecked(true);
		 }
		 };
		 selectButton.addSelectionListener(listener);

		 Button deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID, GenericMessages.getString("WizardTransferPage.deselectAll"), false); //$NON-NLS-1$

		 listener = new SelectionAdapter() 
		 {
		 public void widgetSelected(SelectionEvent e) {
		 typesSelectionList.setAllChecked(false);

		 }
		 };
		 deselectButton.addSelectionListener(listener);
		 */
	}

	protected static Button createPushButton(Composite group, String label, String tooltip) {
		Button button = createPushButton(group, label);
		button.setToolTipText(tooltip);
		return button;
	}

	public static Button createPushButton(Composite group, String label) {
		Button button = new Button(group, SWT.PUSH);
		button.setText(label);
		//button.setText("THIS IS A LONG LABEL. I MEAN, IT IS JUST HUGE!");
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		button.setLayoutData(data);
		return button;
	}

	/**
	 * Add the currently-specified extensions.
	 */
	private void addUserDefinedEntries(java.util.List result) {
		StringTokenizer tokenizer = new StringTokenizer(userDefinedText.getText(), TYPE_DELIMITER);
		//Allow the *. and . prefix and extract the extension
		while (tokenizer.hasMoreTokens()) {
			String currentExtension = tokenizer.nextToken().trim();
			if (!currentExtension.equals("")) //$NON-NLS-1$
			{
				if (currentExtension.startsWith("*."))//$NON-NLS-1$
					result.add(currentExtension.substring(2));
				else {
					if (currentExtension.startsWith("."))//$NON-NLS-1$
						result.add(currentExtension.substring(1));
					else
						result.add(currentExtension);
				}
			}
		}
	}

	/**
	 * Visually checks the previously-specified elements in this dialog's list viewer.
	 */
	private void checkInitialSelections() {
		if ((inpTypes == null) || (inpTypes.size() == 0)) return;
		IFileEditorMapping editorMappings[] = PlatformUI.getWorkbench().getEditorRegistry().getFileEditorMappings();
		ArrayList selectedMappings = new ArrayList();
		for (int i = 0; i < editorMappings.length; i++) {
			IFileEditorMapping mapping = editorMappings[i];
			if (inpTypes.contains(mapping.getLabel())) {
				typesSelectionList.setChecked(mapping, true);
				selectedMappings.add(mapping.getLabel());
			} else {
				//System.out.println("name = '" + mapping.getName() + "', label = '" + mapping.getLabel() + "', ext = '" + mapping.getExtension() + "'");
				if (mapping.getName().equals("*")) //$NON-NLS-1$
				{
					if (inpTypes.contains(mapping.getExtension())) {
						typesSelectionList.setChecked(mapping, true);
						selectedMappings.add(mapping.getExtension());
					}
				} else if (mapping.getExtension().equals("")) // extension-less name like "makefile" //$NON-NLS-1$
				{
					if (inpTypes.contains(mapping.getName() + NO_EXTENSION_PLACEHOLDER)) {
						typesSelectionList.setChecked(mapping, true);
						selectedMappings.add(mapping.getName() + NO_EXTENSION_PLACEHOLDER);
					}
				}
			}
		}
		//Now add in the ones not selected to the user defined list
		Iterator initialIterator = inpTypes.iterator();
		StringBuffer entries = new StringBuffer();
		while (initialIterator.hasNext()) {
			String nextExtension = (String) initialIterator.next();
			if (!selectedMappings.contains(nextExtension)) {
				entries.append(nextExtension);
				entries.append(',');
			}
		}
		this.userDefinedText.setText(entries.toString());
	}

	/**
	 * Create the group that shows the user defined entries for the dialog.
	 * @param parent the parent this is being created in.
	 */
	private Composite createUserEntryGroup(Composite parent) {
		// destination specification group
		int nbrColumns = 2;
		Composite composite = SystemWidgetHelpers.createFlushComposite(parent, nbrColumns);
		userDefinedText = SystemWidgetHelpers.createLabeledTextField(composite, null, SystemUDAResources.RESID_UDT_FILES_USERTYPES_LABEL, SystemUDAResources.RESID_UDT_FILES_USERTYPES_TOOLTIP);
		userDefinedText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreModifyEvents) fireModifiedEvent();
				ignoreModifyEvents = false; // non-sticky flag
			}
		});
		SystemWidgetHelpers.setHelp(userDefinedText, RSEUIPlugin.HELPPREFIX + "wwnt0002"); //$NON-NLS-1$
		return composite;
	}

	/**
	 * Return the input to the dialog.
	 */
	private IFileEditorMapping[] getInput() {
		//Filter the mappings to be just those with a wildcard extension
		// Hmm, why does Eclipse do this? Phil
		if (currentInput == null) {
			currentInput =
			//IFileEditorMapping [] allMappings =
			PlatformUI.getWorkbench().getEditorRegistry().getFileEditorMappings();
			//java.util.List wildcardEditors = new ArrayList();
			//for (int i = 0; i < allMappings.length; i++) 
			//{
			//if (allMappings[i].getName().equals("*"))//$NON-NLS-1$
			//wildcardEditors.add(allMappings[i]);
			//}
			//currentInput = new IFileEditorMapping[wildcardEditors.size()];
			//wildcardEditors.toArray(currentInput);
		}
		return currentInput;
	}

	/**
	 * Initializes this dialog's viewer after it has been laid out.
	 */
	private void initializeViewer() {
		typesSelectionList.setInput(getInput());
	}

	/**
	 * Return the currently selected items as a java.util.List of Strings
	 */
	protected java.util.List getSelectedTypes() {
		// Get the input children.
		IFileEditorMapping[] children = getInput();
		java.util.List list = new ArrayList();
		// Build a list of selected children.
		for (int i = 0; i < children.length; ++i) {
			IFileEditorMapping element = children[i];
			if (typesSelectionList.getChecked(element)) {
				if (element.getName().equals("*")) //$NON-NLS-1$
					list.add(element.getExtension());
				else if (element.getExtension().equals("")) //$NON-NLS-1$
					list.add(element.getName() + NO_EXTENSION_PLACEHOLDER);
				else
					list.add(element.getLabel());
			}
		}
		addUserDefinedEntries(list);
		//setResult(list);
		return list;
	}

	/**
	 * Return true if there are any types currently selected
	 */
	protected boolean areTypesSelected() {
		// Get the input children.
		IFileEditorMapping[] children = getInput();
		// Test list of selected children.
		for (int i = 0; i < children.length; ++i) {
			IFileEditorMapping element = children[i];
			if (typesSelectionList.getChecked(element)) {
				return true;
			}
		}
		String udtText = userDefinedText.getText().trim();
		if (udtText.length() == 0) return false;
		//StringTokenizer tokenizer =
		//	new StringTokenizer(udtText, TYPE_DELIMITER);
		//return tokenizer.hasMoreTokens();
		return true;
	}
}
