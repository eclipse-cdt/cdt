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
 * Martin Oberhuber (Wind River) - [cleanup] Avoid using SystemStartHere in production code
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.ui.uda;

import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.widgets.ISystemEditPaneStates;
import org.eclipse.rse.ui.widgets.SystemEditPaneStateMachine;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * A dialog that allows the user to manipulate their user defined actions for a 
 *  given subsystem factory.
 */
public class SystemWorkWithUDAsDialog extends SystemPromptDialog implements ISystemUDWorkWithDialog, ISystemUDAEditPaneHoster, Listener, SelectionListener,
		Runnable {
	// Changes:
	// June 2002, Phil Coulthard: Added prompt for parent profile, similar to New Connection and New Filter Pool wizards. 
	// Similar to SystemConnectionForm
	protected Shell shell; // shell hosting this viewer
	// GUI widgets
	protected Label labelProfile, labelProfileValue;
	protected Combo profileCombo;
	protected SystemUDActionTreeView treeView;
	protected int prevProfileComboSelection = 0;
	// inputs
	protected ISubSystem subsystem;
	protected ISubSystemConfiguration subsystemFactory;
	protected SystemUDActionSubsystem udaActionSubsystem;
	//protected String           defaultProfileName; 
	//protected String[]         defaultProfileNames;
	protected ISystemProfile[] systemProfiles;
	protected ISystemProfile currentProfile;
	// state
	protected SystemUDActionEditPane editpane;
	protected Button applyButton, revertButton;
	protected SystemEditPaneStateMachine sm;

	/**
	 * Constructor when we have a subsystem
	 */
	public SystemWorkWithUDAsDialog(Shell shell, ISubSystem ss, SystemUDActionSubsystem udaActionSubsystem) {
		super(shell, SystemUDAResources.RESID_WORKWITH_UDAS_TITLE);
		setCancelButtonLabel(SystemUDAResources.BUTTON_CLOSE);
		setShowOkButton(false);
		this.shell = shell;
		this.subsystem = ss;
		this.subsystemFactory = ss.getSubSystemConfiguration();
		this.udaActionSubsystem = udaActionSubsystem;
		setProfiles(RSECorePlugin.getTheSystemProfileManager().getActiveSystemProfiles(), subsystem.getSystemProfile());
		//setMinimumSize(600, 520); // x, y
		//pack();
		setHelp();
	}

	/**
	 * Constructor when we have a subsystem factory
	 */
	public SystemWorkWithUDAsDialog(Shell shell, ISubSystemConfiguration ssFactory, ISystemProfile profile, SystemUDActionSubsystem udaActionSubsystem) {
		super(shell, SystemUDAResources.RESID_WORKWITH_UDAS_TITLE);
		setCancelButtonLabel(SystemUDAResources.BUTTON_CLOSE);
		setShowOkButton(false);
		this.shell = shell;
		this.subsystemFactory = ssFactory;
		this.udaActionSubsystem = udaActionSubsystem;
		setProfiles(RSECorePlugin.getTheSystemProfileManager().getActiveSystemProfiles(), profile);
		//setMinimumSize(600, 520); // x, y
		//pack();
		setHelp();
	}

	/**
	 * Overridable extension point for setting dialog help
	 */
	protected void setHelp() {
		setHelp(RSEUIPlugin.HELPPREFIX + "wwua0000"); //$NON-NLS-1$
	}

	/**
	 * Set the profiles to show in the combo.
	 * @param profiles array of profiles to show
	 * @param profile the profile to pre-select
	 */
	public void setProfiles(ISystemProfile[] profiles, ISystemProfile profile) {
		if (profiles == null) profiles = new ISystemProfile[0];
		this.systemProfiles = profiles;
		this.currentProfile = profile;
		initProfileCombo();
	}

	/**
	 * @see SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() {
		return null;
	}

	/**
	 * Return the user defined action subsystem
	 */
	protected SystemUDActionSubsystem getUDActionSubsystem() {
		return udaActionSubsystem;
	}

	/**
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent) {
		// 2 columns
		int nbrColumns = 2;
		Composite composite = SystemWidgetHelpers.createComposite(parent, nbrColumns);
		Composite profileComposite = SystemWidgetHelpers.createFlushComposite(composite, 2);
		((GridData) profileComposite.getLayoutData()).horizontalSpan = nbrColumns;
		String temp = SystemWidgetHelpers.appendColon(SystemUDAResources.RESID_UDA_PROFILE_LABEL);
		labelProfile = SystemWidgetHelpers.createLabel(profileComposite, temp);
		labelProfile.setToolTipText(SystemUDAResources.RESID_UDA_PROFILE_TOOLTIP);
		profileCombo = SystemWidgetHelpers.createReadonlyCombo(profileComposite, null, SystemUDAResources.RESID_UDA_PROFILE_TOOLTIP);
		//SystemWidgetHelpers.setHelp(profileCombo, RSEUIPlugin.HELPPREFIX + "ccon0001", parentHelpId);     
		if (currentProfile != null) // important to set this before instantiating action tree
			getUDActionSubsystem().getUDActionManager().setCurrentProfile(currentProfile);
		// create tree view on left
		if (subsystem != null)
			treeView = new SystemUDActionTreeView(composite, this, subsystem, udaActionSubsystem);
		else
		{
			// FIXME - Xuan 
			//treeView = new SystemUDActionTreeView(composite, this, subsystemFactory, currentProfile);
		}
		Control c = treeView.getControl();
		//c.setToolTipText(RSEUIPlugin.getString(RESID_UDA_TREE_TIP));
		GridData data = (GridData) c.getLayoutData();
		if (data == null) data = new GridData();
		data.grabExcessHorizontalSpace = false;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessVerticalSpace = true;
		data.verticalAlignment = GridData.FILL;
		data.widthHint = 140; // 170
		data.heightHint = publicConvertHeightInCharsToPixels(12); // high enough to show 12 entries
		c.setLayoutData(data);
		// we want the tree view on the left to extend to the bottom of the page, so on the right
		// we create a 1-column composite that will hold the edit pane on top, and the apply/revert
		// buttons on the bottom...
		Composite rightSideComposite = SystemWidgetHelpers.createFlushComposite(composite, 1);
		// now populate top of right-side composite with edit pane...
		if (subsystem != null)
			editpane = getUDActionSubsystem().getCustomUDActionEditPane(subsystem, this, treeView);
		else
			editpane = getUDActionSubsystem().getCustomUDActionEditPane(subsystemFactory, currentProfile, this, treeView);
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
		revertButton = SystemWidgetHelpers.createPushButton(applyResetButtonComposite, this, SystemUDAResources.RESID_UDA_REVERT_BUTTON_LABEL, SystemUDAResources.RESID_UDA_REVERT_BUTTON_TOOLTIP);
		// now add a spacer to soak up left-over height...
		addGrowableFillerLine(rightSideComposite, 1);
		// populate profile dropdown
		initProfileCombo();
		// add state machine to edit pane
		sm = new SystemEditPaneStateMachine(rightSideComposite, applyButton, revertButton);
		editpane.setStateMachine(sm);
		// add listeners...
		profileCombo.addSelectionListener(this);
		treeView.addSelectionChangedListener(editpane);
		getShell().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				//System.out.println("Inside dispose for SystemWorkWithUDAsDialog");
				getUDActionSubsystem().getUDActionManager().setCurrentProfile(null);
			}
		});
		treeView.getControl().addMouseListener(editpane);
		treeView.getControl().addKeyListener(editpane);
		composite.layout(true);
		rightSideComposite.setVisible(false);
		treeView.expandDomainNodes();
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
	 * Initialize contents and selection of profile combo
	 */
	private void initProfileCombo() {
		if (profileCombo != null) {
			if ((systemProfiles != null) && (systemProfiles.length > 0)) {
				String[] names = new String[systemProfiles.length];
				int selIdx = 0;
				for (int idx = 0; idx < names.length; idx++) {
					names[idx] = systemProfiles[idx].getName();
					if ((currentProfile != null) && (currentProfile == systemProfiles[idx])) selIdx = idx;
				}
				profileCombo.setItems(names);
				profileCombo.setText(names[selIdx]);
				prevProfileComboSelection = selIdx;
			}
		}
	}

	/**
	 * Intercept of parent method so we can direct it to the Apply button versus the OK button
	 */
	public void setPageComplete(boolean complete) {
		if (applyButton != null) {
			if (!complete) applyButton.setEnabled(false);
			// else: we never enable it because the state machine does that anyway on any user-input change
		}
	}

	/**
	 * Parent override.
	 * Called when user presses CLOSE button. 
	 * We simply close the dialog (since we save as we go), unless there are pending changes.
	 */
	protected boolean processCancel() {
		if (sm.isSaveRequired()) {
			if (!editpane.validateInput(true, null)) {
				sm.setChangesMade(); // defect 45773
				return false; // pending errors. Cannot save, so cannot close!
			}
			editpane.saveData();
		}
		return super.processCancel();
	}

	// --------------------------------- 
	// METHODS FOR INTERFACES...
	// --------------------------------- 
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

	/**
	 * Combo selection listener method
	 */
	public void widgetDefaultSelected(SelectionEvent event) {
	}

	/**
	 * Combo selection listener method
	 */
	public void widgetSelected(SelectionEvent event) {
		Object src = event.getSource();
		if (src == profileCombo) {
			if (editpane.areErrorsPending()) {
				profileCombo.getDisplay().asyncExec(this);
				return;
			}
			//			SystemUDActionManager udam = getUDActionSubsystem().getUDActionManager();
			if (sm.isSaveRequired()) {
				if (!editpane.validateInput(true, null)) // errors in pending input?
				{
					sm.setChangesMade();
					profileCombo.getDisplay().asyncExec(this);
					return;
				}
				//udam.saveUserData(udam.getCurrentProfile());
				editpane.saveData(); // defect 45771
			}
			sm.applyPressed();
			int idx = profileCombo.getSelectionIndex();
			if (idx < 0) // should never happen?
				idx = 0;
			prevProfileComboSelection = idx;
			currentProfile = systemProfiles[idx];
			getUDActionSubsystem().getUDActionManager().setCurrentProfile(currentProfile);
			treeView.clearClipboard();
			treeView.setInput("0"); //$NON-NLS-1$
			treeView.expandDomainNodes();
		}
	}

	// -------------------------------------------------------
	// METHOD REQUIRED BY RUNNABLE, USED IN CALL TO ASYNCEXEC
	// -------------------------------------------------------
	/**
	 * Run asynchronously verification of data when user changes profile
	 *  selection. If errors pending, re-select previous profile
	 */
	public void run() {
		profileCombo.select(prevProfileComboSelection);
		super.run();
	}

	// ---------------
	// HELPER METHODS
	// ---------------  
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
		boolean can = (sm.getMode() == ISystemEditPaneStates.MODE_EDIT) && !sm.areChangesPending();
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
		boolean can = (sm.getMode() == ISystemEditPaneStates.MODE_EDIT) && !sm.areChangesPending();
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
		boolean can = (sm.getMode() == ISystemEditPaneStates.MODE_EDIT) && !sm.areChangesPending();
		return can;
	}

	/**
	 * Return true if currently selected type is vendor supplied
	 */
	protected boolean isSelectionVendorSupplied() {
		SystemXMLElementWrapper selectedElement = treeView.getSelectedElement();
		if (selectedElement != null) {
			String vendor = selectedElement.getVendor();
			//System.out.println("Vendor value: '"+vendor+"'");
			return ((vendor != null) && (vendor.length() > 0));
		}
		return false;
	}

	/**
	 * Return the vendor that is responsible for pre-supplying this existing type,
	 *  or null if not applicable.
	 */
	protected String getVendorOfSelection() {
		SystemXMLElementWrapper selectedElement = treeView.getSelectedElement();
		if (selectedElement != null) {
			String vendor = selectedElement.getVendor();
			if ((vendor != null) && (vendor.length() > 0)) return vendor;
		}
		return null;
	}

	/**
	 * Return true if changes are pending in the edit pane
	 */
	public boolean areChangesPending() {
		return sm.areChangesPending();
	}
}
