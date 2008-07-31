/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 * Kevin Doyle	 (IBM) - [242717] Need a way to set the name validator of Named Types
 *******************************************************************************/

package org.eclipse.rse.internal.useractions.ui.uda;

import java.util.Vector;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemModelChangeEvents;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.IUserActionsModelChangeEvents;
import org.eclipse.rse.internal.useractions.ui.validators.ValidatorUserTypeName;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ISystemValidatorUniqueString;
import org.eclipse.rse.ui.widgets.SystemEditPaneStateMachine;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * This is the eidt pane on the right, when a named type is selected
 * on the left (or "New" is selected). It is used to create or edit
 * a named type definition, which is nothing more than a name associated
 * with one or more file types.
 */
public class SystemUDTypeEditPane implements ISelectionChangedListener {
	// gui
	private Composite comp;
	private Text textName;
	private ISystemUDTypeEditPaneTypesSelector typesEditor;
	// input
	protected SystemUDActionSubsystem udaActionSubsys;
	protected ISubSystem subsystem;
	protected ISubSystemConfiguration subsystemFactory;
	protected ISystemProfile profile;
	protected ISystemUDTreeView treeView;
	protected ISystemUDAEditPaneHoster parentDialog;
	// validators
	private ISystemValidator nameValidator;
	// listeners   
	private NameModifyListener nameML = new NameModifyListener();
	private TypesModifyListener typesML = new TypesModifyListener();
	// current error message
	private SystemMessage errorMessage;
	// state related to current selection or state
	private boolean newMode = false;
	private boolean recursiveCall = false;
	private int newModeDomain = -1;
	private SystemUDTreeViewNewItem newModeNewItem;
	private SystemEditPaneStateMachine stateMachine;
	private int currentDomain = -1;
	private SystemUDTypeElement currentType;
	// misc state
	private boolean nameChanged = false;
	private boolean isEnabled = false;
	private boolean ignoreChanges = false;
	// constants	
	private static final Vector EMPTY_VECTOR = new Vector();

	/**
	 * Constructor 
	 */
	public SystemUDTypeEditPane(SystemUDActionSubsystem udaActionSubsys, ISystemUDAEditPaneHoster parent, ISystemUDTreeView tv) {
		super();
		this.udaActionSubsys = udaActionSubsys;
		subsystem = udaActionSubsys.getSubsystem();
		subsystemFactory = subsystem.getSubSystemConfiguration();
		this.profile =  subsystem.getSystemProfile();
		//this.subsystemFactory = ss.getParentSubSystemFactory();
		//this.profile = ss.getSystemProfile();
		treeView = tv;
		parentDialog = parent;
	}

	/**
	 * Set domain.
	 * The edit pane may possibly appear differently, depending on the domain.
	 * When the domain changes (either in "new" or "edit" mode) this method is called.
	 */
	public void setDomain(int domain) {
		this.currentDomain = domain;
		if (typesEditor != null) typesEditor.setDomain(domain);
	}

	/**
	 * Get the current domain.
	 * This is equivalent to newModeDomain in "new" mode, and currentType.getDomain() in "edit" mode
	 */
	public int getDomain() {
		if (currentDomain == -1) {
			if (newMode)
				return newModeDomain;
			else if (currentType != null)
				return currentType.getDomain();
			else
				return -1;
		} else
			return currentDomain;
	}

	/**
	 * Set the state machine.
	 * Called by the UDA dialog
	 */
	public void setStateMachine(SystemEditPaneStateMachine sm) {
		this.stateMachine = sm;
	}

	/**
	 * Create widgets and populate/return composite
	 */
	public Control createContents(Composite parent) {
		if (nameValidator == null)
			nameValidator = new ValidatorUserTypeName();
		
		// Inner composite
		int nbrColumns = 2;
		comp = SystemWidgetHelpers.createComposite(parent, nbrColumns);
		// Action name
		textName = SystemWidgetHelpers.createLabeledTextField(comp, null, SystemUDAResources.RESID_UDT_NAME_LABEL, SystemUDAResources.RESID_UDT_NAME_TOOLTIP);
		// List of selected types as a single string...
		typesEditor = createTypesListEditor(comp, nbrColumns);
		typesEditor.setMessageLine(parentDialog);
		// configuration of widgets...
		textName.setTextLimit(ValidatorUserTypeName.MAX_UDTNAME_LENGTH);
		return comp;
	}

	/**
	 * Overridable exit point.
	 * Create the edit widgets that will allow the user to see and
	 *  edit the list of file types that constitute this named type.
	 * <p>
	 * To better facilitate this, the only requirement is that this
	 *  "editor" meet the minimal interface 
	 *   {@link org.eclipse.rse.internal.useractions.ui.uda.ISystemUDTypeEditPaneTypesSelector}
	 * <p>
	 * The default implementation is simply a labeled entry field!
	 * 
	 * @param parent - the parent composite where the widgets are to go
	 * @param nbrColumns - the number of columns in the parent composite, which these
	 *   widgets should span
	 * @return a class implementing the required interface
	 */
	protected ISystemUDTypeEditPaneTypesSelector createTypesListEditor(Composite parent, int nbrColumns) {
		SystemUDSimpleTypesListEditor simpleEditor = new SystemUDSimpleTypesListEditor(parent, nbrColumns);
		simpleEditor.setAutoUpperCase(getAutoUpperCaseTypes());
		return simpleEditor;
	}

	/**
	 * Overridable exit point.
	 * Return true if the types are to be auto-uppercased. 
	 * Default is true.
	 * Only used if not supplying your own types editor.
	 */
	protected boolean getAutoUpperCaseTypes() {
		return true;
	}

	/**
	 * Enable/disable entire pane
	 */
	public void setEnabled(boolean enable) {
		textName.setEnabled(enable);
		typesEditor.setEnabled(enable);
	}

	/**
	 * Check all input for errors
	 * @param setFocus - true if to set focus on offending control
	 * @param skipControl - control to skip since already checked
	 * @return true if no errors
	 */
	protected boolean validateInput(boolean setFocus, Control skipControl) {
		Control errCtl = null;
		errorMessage = null;
		if (skipControl != textName) errorMessage = nameValidator.validate(textName.getText().trim());
		errCtl = textName;
		if ((errorMessage == null) && (skipControl != typesEditor.getControl())) {
			errorMessage = typesEditor.validate();
			if (errorMessage == null) errorMessage = doTypesStringValidation(setFocus);
			errCtl = typesEditor.getControl();
		}
		if (errorMessage == null) errorMessage = doAdditionalValidation(setFocus); // let child classes try
		if (errorMessage != null) {
			parentDialog.setErrorMessage(errorMessage);
			if (setFocus) errCtl.setFocus();
		} else
			parentDialog.clearErrorMessage();
		setPageComplete();
		return (errorMessage == null);
	}

	/**
	 * Overridable entry point for doing validation of the type string.
	 * Called by validateInput.
	 * If setFocus is true, set the focus at the appropriate widget that is in error.
	 * If setFocus is true, you can assume we are doing OK processing vs keystroke processor.
	 * @return error message if an error detected, else null
	 */
	protected SystemMessage doTypesStringValidation(boolean doSetFocus) {
		return null;
	}

	/**
	 * Overridable entry point for doing validation of input.
	 * Called by validateInput.
	 * If setFocus is true, set the focus at the appropriate widget that is in error.
	 * If setFocus is true, you can assume we are doing OK processing vs keystroke processor.
	 * @return error message if an error detected, else null
	 */
	protected SystemMessage doAdditionalValidation(boolean doSetFocus) {
		return null;
	}

	// Scenario:  User edits an item, producing a syntax error.
	//  (eg. clear action name field)  Gets error msg, OK button disabled.
	// then changes selection to another item.
	// Current Problem: Error msg stays, OK remains disabled, until
	// they edit a field.  (ValidateInput isnt re-reun until
	// another field is changed.)
	// Solution:  When changing selection, reset the errorMessage and
	// page-valid status.  Can get away with this because we
	// do not propagate invalid field changes to the UDA data in memory.
	private void resetPageValidation() {
		errorMessage = null;
		parentDialog.clearErrorMessage();
		parentDialog.setPageComplete(true);
	}

	/**
	 * Return true if the page is complete, so to enable Finish.
	 * Called by setPageComplete
	 */
	protected boolean isPageComplete() {
		return ((errorMessage == null) && (textName.getText().trim().length() > 0) && (typesEditor.getTypes().length() > 0));
	}

	/**
	 * Set page complete... enables/disables Apply button
	 */
	protected void setPageComplete() {
		boolean complete = isPageComplete();
		parentDialog.setPageComplete(complete);
	}

	/**
	 * Call this whenever the user makes ANY changes.
	 * Used to enable/disable apply/revert buttons
	 */
	protected void setChangesMade() {
		if (stateMachine != null) stateMachine.setChangesMade();
	}

	/**
	 * Are errors pending? If so, don't allow user to change selection
	 *  or profile or anything!
	 */
	public boolean areErrorsPending() {
		return ((errorMessage != null) && ((currentType != null) || newMode));
	}

	/**
	 * This is called when user changes their selection in the left-side tree view
	 */
	public void selectionChanged(SelectionChangedEvent se) {
		if (recursiveCall) return; // ignore!
		// Calling the setText() methods here was causing Modify events
		// when just switching the selection, even on Domain items, leading
		// to setComment(), etc calls on the Action item, caausing these tags to
		// even be written in the saved XML.  Even for Domain items!
		// So, turning off/on the modifyListeners around the selection change,
		// based on the isEnabled switch
		IStructuredSelection ss = (IStructuredSelection) se.getSelection();
		Object so = ss.getFirstElement();
		// if old selection has validation errors, don't allow selection to be changed.
		if (areErrorsPending()) {
			// Verify old selection has not been deleted from tree
			if (newMode || SystemUDBaseManager.inCurrentTree(currentType.getElement())) {
				if (!newMode && (so != currentType))
					treeView.setSelection(new StructuredSelection(currentType));
				else if (newMode && (so != newModeNewItem)) treeView.setSelection(new StructuredSelection(newModeNewItem));
				return;
			}
		}
		// We need to test for pending changes, and if any are pending, prompt
		// user to continue (and lose changes) or cancel...
		if ((stateMachine != null) && stateMachine.isSaveRequired()) {
			saveData();
			if (newMode) {
				// interesting problem! The save of the new data resulted in a new node,
				//  but this is not visible in the tree view. To make it visible means we
				//  we will lose focus, and this method will be recalled recursively...
				recursiveCall = true;
				treeView.refreshElementParent(currentType); // show new item in tree view
				recursiveCall = false;
				if (so instanceof SystemUDTypeElement) // if user was selecting a type, it might have a new binary address after the refresh
					treeView.selectElement((SystemUDTypeElement) so);
				else if (so != null) treeView.setSelection(new StructuredSelection(so)); // restore what user selected
				return; // avoid recursion!
			}
		}
		recursiveCall = false;
		// Clear any page-valid errors remaining from previous selection
		// (Since validation on the new selection is only run if editing
		// changes are made
		errorMessage = null;
		resetPageValidation();
		newMode = ((so instanceof SystemUDTreeViewNewItem) && ((SystemUDTreeViewNewItem) so).isExecutable());
		// Refresh tree view if name changed on last item
		if (nameChanged) {
			nameChanged = false;
			if (null != currentType) treeView.refresh(currentType);
		}
		SystemUDTypeElement sn = null;
		if ((null != so) && (so instanceof SystemUDTypeElement)) sn = (SystemUDTypeElement) so;
		currentType = sn;
		// Disable modifyListeners prior to resetting fields
		if (isEnabled) {
			textName.removeModifyListener(nameML);
			typesEditor.removeModifyListener(typesML);
		}
		// Clear all fields if not a file type entry.  Could be a domain node
		//boolean prevEnabledState = isEnabled;
		//boolean newEnabledState = false;
		// domain node selected. Note we will be hidden in this case, by the
		// state machine
		if (!newMode && ((null == sn) || sn.isDomain())) {
			isEnabled = false;
			//newEnabledState = false;
			textName.setText(""); //$NON-NLS-1$
			typesEditor.clearTypes();
		}
		// "new" node or existing node selected
		else {
			isEnabled = true;
			//newEnabledState = true;
			if (!newMode && sn != null) {
				textName.setText(sn.toString());
				typesEditor.setTypes(sn.getTypes());
				//setEnabled(!treeView.isElementAllSelected() && !treeView.isSelectionVendorSupplied());			   
				//typesEditor.setEditable(!treeView.isSelectionVendorSupplied(), treeView.getVendorOfSelection());
				setEnabled(!treeView.isElementAllSelected());
				typesEditor.setEditable(!treeView.isElementAllSelected(), treeView.isElementAllSelected() ? treeView.getVendorOfSelection() : null);
			} else {
				textName.setText(""); //$NON-NLS-1$
				typesEditor.clearTypes();
				typesEditor.setEditable(true, null);
				setEnabled(true);
			}
			// isEnabled will = true when leaving this logic branch
			// Will always need to re-add the listeners
			textName.addModifyListener(nameML);
			typesEditor.addModifyListener(typesML);
		}
		//System.out.println("selection changed: " + (testCounter++) + ", new? " + newMode + ", enabled? " + isEnabled);
		// update state machine
		if (newMode) {
			stateMachine.setNewMode(); // resets Apply/Reset button status
			newModeNewItem = (SystemUDTreeViewNewItem) so;
			newModeDomain = newModeNewItem.getDomain();
			if (newModeDomain != currentDomain) setDomain(newModeDomain); //indicate domain change
		} else if ((sn == null) || sn.isDomain()) {
			stateMachine.setUnsetMode(); // resets Apply/Reset button status		   
		} else {
			stateMachine.setEditMode(); // resets Apply/Reset button status		}
			if (sn.getDomain() != currentDomain) setDomain(sn.getDomain()); //indicate domain change
		}
		if (nameValidator instanceof ISystemValidatorUniqueString)
			((ISystemValidatorUniqueString) nameValidator).setExistingNamesList(getExistingNames());
		setPageComplete();
	}

	/**
	 * Need to add/remove listeners around selection changes, so
	 * I can set text fields without triggering modify  event.
	 * So listeners implemented as internal classes
	 */
	private class NameModifyListener implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			if (ignoreChanges) return;
			setChangesMade();
			String s = textName.getText().trim().toUpperCase();
			errorMessage = nameValidator.validate(s);
			if (errorMessage != null) {
				parentDialog.setErrorMessage(errorMessage);
				setPageComplete();
			} else {
				validateInput(false, textName);
				if (currentType != null) {
					nameChanged = true;
				}
			}
		}
	} //class

	private class TypesModifyListener implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			if (ignoreChanges) return;
			setChangesMade();
			errorMessage = typesEditor.validate();
			if (errorMessage != null) {
				parentDialog.setErrorMessage(errorMessage);
				setPageComplete();
			} else {
				validateInput(false, typesEditor.getControl());
			}
		}
	} //class

	/**
	 * For uniqueness checking, get the list of existing type names
	 */
	protected Vector getExistingNames() {
		if (newMode) {
			SystemUDActionSubsystem udas = getUDActionSubsystem();
			SystemUDTypeManager udtm = udas.getUDTypeManager();
			return udtm.getExistingNames(null, newModeDomain);
		} else if (currentType != null)
			return currentType.getExistingNames();
		else
			return EMPTY_VECTOR;
	}

	/**
	 * Return the user defined action subsystem
	 */
	protected SystemUDActionSubsystem getUDActionSubsystem() {
		return udaActionSubsys;
	}

	/**
	 * When user presses Apply, commit all pending changes...
	 */
	protected void processChanges() {
		currentType.setName(textName.getText().trim());
		currentType.setTypes(typesEditor.getTypes());
	} //process changes

	/**
	 * Save current state to disk
	 */
	protected void saveData() {
		if (newMode) {
			currentType = createNewType(textName.getText().trim(), newModeDomain);
		}
		processChanges();
		SystemUDActionSubsystem udas = getUDActionSubsystem();
		SystemUDTypeManager udtm = udas.getUDTypeManager();
		udtm.saveUserData();
		// inform anybody registered as listeners that we have created/changed model object...
		if (newMode)
			RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED, IUserActionsModelChangeEvents.SYSTEM_RESOURCETYPE_NAMEDTYPE, currentType, null);
		else
			RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, IUserActionsModelChangeEvents.SYSTEM_RESOURCETYPE_NAMEDTYPE, currentType, null);
	}

	/**
	 * In "new" mode, create a new type when Apply is pressed.
	 * This only creates the type. It does not populate the attributes
	 * @return The new action
	 */
	protected SystemUDTypeElement createNewType(String typeName, int domain) {
		// code was originally in SystemNewUDAsWizardMainPage
		SystemUDActionSubsystem udas = getUDActionSubsystem();
		SystemUDTypeManager udtm = udas.getUDTypeManager();
		SystemUDTypeElement nt = udtm.addType(domain, typeName);
		return nt;
	}

	/**
	 * Revert button pressed
	 */
	public void revertPressed() {
		ignoreChanges = true;
		resetPageValidation();
		if ((currentType != null) && !currentType.isDomain()) {
			textName.setText(currentType.toString());
			typesEditor.setTypes(currentType.getTypes());
			if (stateMachine != null) stateMachine.resetPressed();
		} else if (newMode) {
			textName.setText(""); //$NON-NLS-1$
			typesEditor.clearTypes();
			if (stateMachine != null) stateMachine.resetPressed();
		}
		ignoreChanges = false;
		setPageComplete();
	}

	/**
	 * Process the apply button
	 */
	public void applyPressed() {
		if ((newMode || ((currentType != null) && !currentType.isDomain())) && validateInput(true, null)) {
			saveData();
			if (stateMachine != null) stateMachine.applyPressed();
			if (newMode) {
				// Now update tree view to show new item
				recursiveCall = true;
				treeView.refreshElementParent(currentType);
				recursiveCall = false;
				treeView.selectElement(currentType);
			} else
				treeView.refresh(currentType);
		}
		setPageComplete();
	} //apply
	
	public void setNameValidator(ISystemValidator validator) {
		nameValidator = validator;
	}
}
