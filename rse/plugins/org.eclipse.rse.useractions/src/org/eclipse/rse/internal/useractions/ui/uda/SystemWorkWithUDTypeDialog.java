package org.eclipse.rse.internal.useractions.ui.uda;

/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.util.ResourceBundle;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.widgets.ISystemEditPaneStates;
import org.eclipse.rse.ui.widgets.SystemEditPaneStateMachine;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * A dialog that allows the user to manipulate their user defined actions for a given subsystem factory.
 * <p>
 */
public class SystemWorkWithUDTypeDialog extends SystemPromptDialog implements ISystemUDWorkWithDialog, Listener, Runnable, ISystemUDAEditPaneHoster {
	protected Shell shell; // shell hosting this viewer
	protected ResourceBundle rb;
	protected SystemUDActionSubsystem udaActionSubsystem;
	protected ISubSystem subsystem;
	protected ISubSystemConfiguration subsystemFactory;
	protected ISystemProfile profile;
	protected SystemUDTypeEditPane editpane;
	protected Button applyButton, revertButton;
	protected SystemEditPaneStateMachine sm;
	protected SystemUDTypeTreeView treeView;
	private String typeToPreSelect;
	private String currentType;
	private int preSelectTypeDomain;
	private int currentDomain = -1;
	private Object objectToPreSelect;
	private String domainToPreExpand;

	/**
	 * Constructor when we have a subsystem
	 */
	public SystemWorkWithUDTypeDialog(Shell shell, ISubSystem ss, SystemUDActionSubsystem udaActionSubsystem) {
		super(shell, SystemUDAResources.RESID_WORKWITH_UDT_TITLE);
		setCancelButtonLabel(SystemUDAResources.BUTTON_CLOSE);
		setShowOkButton(false);
		this.shell = shell;
		this.udaActionSubsystem = udaActionSubsystem;
		this.subsystem = ss;
		this.subsystemFactory = subsystem.getSubSystemConfiguration();
		this.profile = subsystem.getSystemProfile();
		setOutputObject(null);
		//setMinimumSize(550, 300); // x, y
		setHelp();
	}

	/**
	 * Constructor when we have a subsystem factory and profile
	 */
	public SystemWorkWithUDTypeDialog(Shell shell, ISubSystemConfiguration ssFactory, ISystemProfile profile) {
		super(shell, SystemUDAResources.RESID_WORKWITH_UDT_TITLE);
		setCancelButtonLabel(SystemUDAResources.BUTTON_CLOSE);
		setShowOkButton(false);
		this.shell = shell;
		this.subsystemFactory = ssFactory;
		this.profile = profile;
		setOutputObject(null);
		//setMinimumSize(550, 300); // x, y
		setHelp();
	}

	/**
	 * Overridable extension point for setting dialog help
	 */
	protected void setHelp() {
		setHelp(RSEUIPlugin.HELPPREFIX + "wwnt0000"); //$NON-NLS-1$
	}

	/**
	 * Set a type to preselect in the dialog.
	 * If domains are supported, specify the domain number, else
	 *  pass -1.
	 */
	public void preSelectType(int domain, String type) {
		this.preSelectTypeDomain = domain;
		this.typeToPreSelect = type;
	}

	/**
	 * @see SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() {
		return treeView.getControl();
	}

	/**
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent) {
		// Inner composite
		int nbrColumns = 2;
		Composite composite = SystemWidgetHelpers.createComposite(parent, nbrColumns);
		// create tree view on left
		if (subsystem != null)
			treeView = new SystemUDTypeTreeView(composite, this, subsystem, udaActionSubsystem);
		else
			treeView = new SystemUDTypeTreeView(composite, this, subsystemFactory, profile);
		Control c = treeView.getControl();
		//c.setToolTipText(SystemUDAResources.RESID_UDA_TREE_TIP)); it is too annoying
		GridData data = (GridData) c.getLayoutData();
		if (data == null) data = new GridData();
		data.grabExcessHorizontalSpace = false;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessVerticalSpace = true;
		data.verticalAlignment = GridData.FILL;
		data.widthHint = 140; // 150, or 170
		data.heightHint = publicConvertHeightInCharsToPixels(12); // high enough to show 12 entries
		c.setLayoutData(data);
		// we want the tree view on the left to extend to the bottom of the page, so on the right
		// we create a 1-column composite that will hold the edit pane on top, and the apply/revert
		// buttons on the bottom...
		Composite rightSideComposite = SystemWidgetHelpers.createFlushComposite(composite, 1);
		// now populate top of right-side composite with edit pane...
		//editpane = new SystemUDTypeEditPane( subsystem, this, treeView);
		//if (subsystem!=null)
		// 	editpane =  getUDActionSubsystem().getCustomUDTypeEditPane( subsystem, this, treeView);
		//else	   
		//	editpane =  getUDActionSubsystem().getCustomUDTypeEditPane( subsystemFactory, profile, this, treeView);
		editpane = getUDActionSubsystem().getCustomUDTypeEditPane(this, treeView);
		editpane.createContents(rightSideComposite);
		// now add a visual separator line
		addSeparatorLine(rightSideComposite, 1);
		// now populate bottom of right-side composite with apply/revert buttons within their own composite
		int nbrColumns_buttonComposite = 4;
		Composite applyResetButtonComposite = SystemWidgetHelpers.createFlushComposite(rightSideComposite, nbrColumns_buttonComposite);
		//((GridData)applyResetButtonComposite.getLayoutData()).horizontalIndent = 200; // shift buttons to the right
		// now populate the buttons composite with apply and revert buttons
		Label filler = SystemWidgetHelpers.createLabel(applyResetButtonComposite, ""); //$NON-NLS-1$
		((GridData) filler.getLayoutData()).grabExcessHorizontalSpace = true;
		((GridData) filler.getLayoutData()).horizontalAlignment = GridData.FILL;
		applyButton = SystemWidgetHelpers.createPushButton(applyResetButtonComposite, this, SystemUDAResources.RESID_UDA_APPLY_BUTTON_LABEL, SystemUDAResources.RESID_UDA_APPLY_BUTTON_TOOLTIP);
		//applyButton.setImage(RSEUIPlugin.getDefault().getImage(ISystemConstants.ICON_SYSTEM_OK_ID));
		revertButton = SystemWidgetHelpers.createPushButton(applyResetButtonComposite, this, SystemUDAResources.RESID_UDA_REVERT_BUTTON_LABEL, SystemUDAResources.RESID_UDA_REVERT_BUTTON_TOOLTIP);
		// now add a spacer to soak up left-over height...
		addGrowableFillerLine(rightSideComposite, 1);
		// add state machine to edit pane
		sm = new SystemEditPaneStateMachine(rightSideComposite, applyButton, revertButton);
		editpane.setStateMachine(sm);
		composite.layout(true);
		rightSideComposite.setVisible(false);
		// if we have been given a type to preselect, do so now...
		//System.out.println("typeToPreSelect = " + typeToPreSelect);
		if (typeToPreSelect != null) {
			SystemUDTypeManager udtm = getUDActionSubsystem().getUDTypeManager();
			SystemUDTypeElement type = null;
			if (preSelectTypeDomain >= 0) {
				domainToPreExpand = getUDActionSubsystem().mapDomainXlatedName(preSelectTypeDomain);
				//treeView.expandDomainNode(domainToPreExpand);
			}
			// add listeners, after expansion...		
			treeView.addSelectionChangedListener(editpane);
			if (subsystem != null)
			{
				type = (SystemUDTypeElement) udtm.findByName(subsystem.getSystemProfile(), typeToPreSelect, preSelectTypeDomain);
			}
			if (type != null) objectToPreSelect = type;
		} else {
			//treeView.expandDomainNodes();
			// add listeners, after expansion...		
			treeView.addSelectionChangedListener(editpane);
		}
		//System.out.println("Test1"); 	    
		treeView.getShell().getDisplay().asyncExec(this);
		//System.out.println("Test2"); 	    
		return composite;
	}

	/**
	 * Intercept of parent so we can reset the default button
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getShell().setDefaultButton(applyButton); // defect 46129
	}

	/**
	 * Return the user defined action subsystem
	 */
	protected SystemUDActionSubsystem getUDActionSubsystem() {
		return udaActionSubsystem;
	}

	/**
	 * Parent override.
	 * Called when user presses CLOSE button. 
	 * If we exit, then we set the dialog's output object to be the
	 *  name of the selected type.
	 */
	protected boolean processCancel() {
		if (sm.isSaveRequired()) {
			if (!editpane.validateInput(true, null)) {
				sm.setChangesMade(); // defect 45773
				return false; // pending errors. Cannot save, so cannot close!
			}
			editpane.saveData();
		}
		currentType = treeView.getSelectedTypeName();
		if (currentType.length() > 0) setOutputObject(currentType);
		currentDomain = treeView.getSelectedTypeDomain();
		return super.processCancel();
	}

	/**
	 * Get the name of the type that was selected at the time we left
	 */
	public String getSelectedTypeName() {
		return currentType;
	}

	/**
	 * Get the domain of the type that was selected at the time we left
	 */
	public int getSelectedTypeDomain() {
		return currentDomain;
	}

	/**
	 * Override of parent method so we can direct it to the Apply button versus the OK button
	 */
	public void setPageComplete(boolean complete) {
		if (applyButton != null) {
			if (!complete) applyButton.setEnabled(false);
			// else: we never enable it because the state machine does that anyway on any user-input change
		}
	}

	/**
	 * Return true if currently selected type is "ALL"
	 */
	protected boolean isAllTypeSelected() {
		return treeView.getSelectedTypeName().equals("ALL"); //$NON-NLS-1$
	}

	/**
	 * Return true if currently selected type is vendor supplied
	 */
	protected boolean isSelectionVendorSupplied() {
		return treeView.isSelectionVendorSupplied();
	}

	/**
	 * Return the vendor that is responsible for pre-supplying this existing type,
	 *  or null if not applicable.
	 */
	protected String getVendorOfSelection() {
		return treeView.getVendorOfSelection();
	}

	/**
	 * Handles events generated by controls on this page.
	 */
	public void handleEvent(Event e) {
		clearMessage();
		Widget source = e.widget;
		if (source == applyButton) {
			processApply();
		} else if (source == revertButton) {
			processRevert();
		}
	}

	/**
	 * Process the apply button
	 */
	public void processApply() {
		editpane.applyPressed();
	}

	/**
	 * Process the revert button
	 */
	public void processRevert() {
		editpane.revertPressed();
	}

	// ---------------
	// HELPER METHODS
	// ---------------  
	/**
	 * Expose inherited protected method convertWidthInCharsToPixels as a publicly
	 *  excessible method
	 */
	public int publicConvertWidthInCharsToPixels(int chars) {
		return convertWidthInCharsToPixels(chars);
	}

	/**
	 * Expose inherited protected method convertHeightInCharsToPixels as a publicly
	 *  excessible method
	 */
	public int publicConvertHeightInCharsToPixels(int chars) {
		return convertHeightInCharsToPixels(chars);
	}

	// -----------------------------------	
	// ISystemUDWorkWithDialog methods...
	// -----------------------------------
	/**
	 * Decide if we can do the delete or not.
	 * Will decide the enabled state of the delete action.
	 */
	public boolean canDelete(Object selectedObject) {
		return (sm.getMode() == ISystemEditPaneStates.MODE_EDIT) && !sm.areChangesPending() && !isSelectionVendorSupplied();
	}

	/**
	 * Decide if we can do the move up or not.
	 * Will decide the enabled state of the move up action.
	 */
	public boolean canMoveUp(Object selectedObject) {
		boolean can = (sm.getMode() == ISystemEditPaneStates.MODE_EDIT) && !sm.areChangesPending() && !isAllTypeSelected();
		if (can) {
			TreeItem selectedItem = treeView.getSelectedTreeItem();
			TreeItem parentItem = selectedItem.getParentItem();
			if (parentItem != null)
				can = (parentItem.getItems()[0] != selectedItem);
			else // this means we don't have domains
			{
				TreeItem[] roots = treeView.getTree().getItems();
				for (int idx = 0; idx < roots.length; idx++) {
					if (roots[idx].getData() instanceof SystemXMLElementWrapper) {
						can = (roots[idx] != selectedItem);
						break;
					}
				}
			}
		}
		return can;
	}

	/**
	 * Decide if we can do the move down or not.
	 * Will decide the enabled state of the move down action.
	 */
	public boolean canMoveDown(Object selectedObject) {
		boolean can = (sm.getMode() == ISystemEditPaneStates.MODE_EDIT) && !sm.areChangesPending() && !isAllTypeSelected();
		if (can) {
			TreeItem selectedItem = treeView.getSelectedTreeItem();
			TreeItem parentItem = selectedItem.getParentItem();
			if (parentItem != null)
				can = (parentItem.getItems()[parentItem.getItemCount() - 1] != selectedItem);
			else // this means we don't have domains
			{
				TreeItem[] roots = treeView.getTree().getItems();
				can = (roots[roots.length - 1] != selectedItem);
			}
		}
		return can;
	}

	/**
	 * Decide if we can do the copy or not.
	 * Will decide the enabled state of the copy action.
	 */
	public boolean canCopy(Object selectedObject) {
		boolean can = (sm.getMode() == ISystemEditPaneStates.MODE_EDIT) && !sm.areChangesPending() && !isAllTypeSelected();
		return can;
	}

	/**
	 * For AsyncExec.
	 * Do selection, after tree is created
	 */
	public void run() {
		// check it out! This run method executes multiple times even though it is only called once!!!
		//System.out.println("Test1"); 	    
		if (domainToPreExpand != null)
			treeView.expandDomainNode(domainToPreExpand);
		else
			treeView.expandDomainNodes();
		//System.out.println("Test2");
		//if (true)
		//  return; // for debugging
		if (objectToPreSelect != null) {
			if (objectToPreSelect instanceof SystemXMLElementWrapper)
				treeView.selectElement((SystemXMLElementWrapper) objectToPreSelect);
			else {
				ISelection selection = new StructuredSelection(objectToPreSelect);
				treeView.setSelection(selection, true);
			}
		} else
		//else if (treeView.getTree().getSelectionCount() == 0)
		{
			objectToPreSelect = (treeView.getTree().getItems()[0]).getData();
			ISelection selection = new StructuredSelection(objectToPreSelect);
			//System.out.println("Test5");
			treeView.setSelection(selection, true);
			//System.out.println("Test6");
		}
	}

	/**
	 * Return true if changes are pending in the edit pane
	 */
	public boolean areChangesPending() {
		return sm.areChangesPending();
	}
}
