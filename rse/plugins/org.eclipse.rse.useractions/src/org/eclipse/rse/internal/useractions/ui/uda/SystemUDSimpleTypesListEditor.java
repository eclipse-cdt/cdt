package org.eclipse.rse.internal.useractions.ui.uda;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 * David Dykstal (IBM) - [186589] move user actions API out of org.eclipse.rse.ui   
 *******************************************************************************/
import org.eclipse.rse.internal.useractions.ui.validators.ValidatorUserTypeTypes;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Default implementation of ISystemUDTypeEditPaneTypesSelector, which is
 *  simply a labeled text field.
 * These editors are used in the Named Types dialog, to prompt for the list of
 *  constituent types.
 */
public class SystemUDSimpleTypesListEditor implements ISystemUDTypeEditPaneTypesSelector {
	protected Text textTypes;
	protected Label typesLabel, nonEditableVerbage;
	private boolean autoUpperCase = false;
	private ISystemValidator typesValidator;
	private int currentDomain = -1;
	protected ISystemMessageLine msgLine;
	protected Shell shell;

	/**
	 * constructor
	 */
	public SystemUDSimpleTypesListEditor(Composite parent, int nbrColumns) {
		shell = parent.getShell();
		createContents(parent, nbrColumns);
		setValidator(new ValidatorUserTypeTypes());
	}

	/**
	 * Set the msg line in case this composite widget needs to issue an error msg
	 */
	public void setMessageLine(ISystemMessageLine msgLine) {
		this.msgLine = msgLine;
	}

	/**
	 * Create and populate widgets
	 */
	protected void createContents(Composite parent, int nbrColumns) {
		textTypes = SystemWidgetHelpers.createLabeledTextField(parent, null, SystemUDAResources.RESID_UDT_TYPES_LABEL, SystemUDAResources.RESID_UDT_TYPES_TOOLTIP);
		typesLabel = SystemWidgetHelpers.getLastLabel();
		((GridData) textTypes.getLayoutData()).horizontalSpan = nbrColumns - 1;
		textTypes.setTextLimit(ValidatorUserTypeTypes.MAX_UDTTYPES_LENGTH);
		nonEditableVerbage = SystemWidgetHelpers.createVerbiage(parent, "", nbrColumns, false, 200); //$NON-NLS-1$
		nonEditableVerbage.setVisible(false);
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
	 * Get the domain of the currently selected existing new type, or "new" node.
	 */
	public int getDomain() {
		return currentDomain;
	}

	/**
	 * Set the validator to use for the types
	 */
	public void setValidator(ISystemValidator validator) {
		typesValidator = validator;
	}

	/**
	 * Initialize the types. These are stored as a single string using 
	 *  a subsystem-decidable delimiter character. 
	 */
	public void setTypes(String types) {
		textTypes.setText(types);
	}

	/**
	 * Clear the types. That is, make sure none are selected. This is 
	 *  called when entering "new" mode.
	 */
	public void clearTypes() {
		textTypes.setText(""); //$NON-NLS-1$
	}

	/**
	 * Retrieve the types as a single string. The delimiter used is up to
	 *  the implementor, as long as it knows how to parse and assemble the
	 *  types list as a single string.
	 */
	public String getTypes() {
		if (autoUpperCase)
			return textTypes.getText().trim().toUpperCase();
		else
			return textTypes.getText().trim();
	}

	/**
	 * Allow the edit pane (or any consumer) to be informed as
	 *  changes are made to the list. When events are fired, the consumer
	 *  will call getTypes() to get the new list.
	 */
	public void addModifyListener(ModifyListener listener) {
		textTypes.addModifyListener(listener);
	}

	/**
	 * Allow the edit pane (or any consumer) to stop listening as
	 *  changes are made to the list. 
	 */
	public void removeModifyListener(ModifyListener listener) {
		textTypes.removeModifyListener(listener);
	}

	/**
	 * Validate input, and return the error message if an error is found.
	 * This is called by the consumer upon receipt of a modify event, to
	 * show any error messages and to know if there are errors pending or
	 * not.
	 */
	public SystemMessage validate() {
		return typesValidator.validate(getTypes());
	}

	/**
	 * Return primary control for setting focus, among other things
	 */
	public Control getControl() {
		return textTypes;
	}

	/**
	 * Enable or disable the input-capability of the constituent controls
	 */
	public void setEnabled(boolean enable) {
		textTypes.setEnabled(enable);
	}

	/**
	 * We want to disable editing of IBM or vendor-supplied 
	 * types, so when one of these is selected, this method is
	 * called to enter non-editable mode. 
	 * @param editable Whether to disable editing of this type or not
	 * @param vendor When disabling, it contains the name of the vendor for substitution purposes
	 */
	public void setEditable(boolean editable, String vendor) {
		textTypes.setEditable(editable);
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

	/**
	 * Not from interface.
	 * Specify if the types are to be auto-uppercased or not.
	 * Default is false.
	 */
	public void setAutoUpperCase(boolean autoUpperCase) {
		this.autoUpperCase = autoUpperCase;
	}
}
