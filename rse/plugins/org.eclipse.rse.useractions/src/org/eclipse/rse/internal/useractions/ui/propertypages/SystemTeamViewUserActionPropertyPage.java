package org.eclipse.rse.internal.useractions.ui.propertypages;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.UserActionsResources;
import org.eclipse.rse.internal.useractions.ui.uda.ISystemUDAEditPaneHoster;
import org.eclipse.rse.internal.useractions.ui.uda.ISystemUDTreeView;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDActionEditPane;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDActionElement;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDActionSubsystem;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDBaseManager;
import org.eclipse.rse.internal.useractions.ui.uda.SystemXMLElementWrapper;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.propertypages.SystemBasePropertyPage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;

/**
 * The property page for user action nodes in the Team view.
 * This is an output-only page.
 */
public class SystemTeamViewUserActionPropertyPage extends SystemBasePropertyPage implements ISystemUDAEditPaneHoster, ISystemUDTreeView {
	protected SystemUDActionEditPane editpane;
	protected Composite composite_prompts;
	protected Label labelType, labelProfile, labelOrigin, labelDomain;
	protected String errorMessage;
	protected boolean initDone = false;

	/**
	 * Constructor
	 */
	public SystemTeamViewUserActionPropertyPage() {
		super();
	}

	/**
	 * We do want the Apply and the Default buttons 
	 */
	protected boolean wantDefaultAndApplyButton() {
		return true;
	}

	/**
	 * Create the page's GUI contents.
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContentArea(Composite parent) {
		// Inner composite
		composite_prompts = SystemWidgetHelpers.createComposite(parent, 2);
		// Type prompt
		labelType = createLabeledLabel(composite_prompts, SystemUDAResources.RESID_PP_PROPERTIES_TYPE_LABEL, SystemUDAResources.RESID_PP_PROPERTIES_TYPE_TOOLTIP);
		labelType.setText(UserActionsResources.RESID_PP_USERACTION_TYPE_VALUE);
		// Profile prompt
		labelProfile = createLabeledLabel(composite_prompts, UserActionsResources.RESID_PP_USERACTION_PROFILE_LABEL, UserActionsResources.RESID_PP_USERACTION_PROFILE_TOOLTIP);
		// Origin prompt
		labelOrigin = createLabeledLabel(composite_prompts, UserActionsResources.RESID_PP_USERACTION_ORIGIN_LABEL, UserActionsResources.RESID_PP_USERACTION_ORIGIN_TOOLTIP);
		if (!initDone) doInitializeFields();
		return composite_prompts;
	}

	/**
	 * From parent: do full page validation
	 */
	protected boolean verifyPageContents() {
		return true;
	}

	/**
	 * Get the input node
	 */
	protected SystemUDActionElement getAction() {
		Object element = getElement();
		return ((SystemUDActionElement) element);
	}

	/**
	 * Return the user defined action subsystem
	 */
	protected SystemUDActionSubsystem getUDActionSubsystem(SystemUDActionElement action) {
		return action.getManager().getActionSubSystem();
	}

	/**
	 * Initialize values of input fields based on input
	 */
	protected void doInitializeFields() {
		initDone = true;
		SystemUDActionElement action = getAction();
		// populate GUI...
		labelProfile.setText(action.getProfile().getName());
		labelOrigin.setText(getOrigin(action));
		// Domain prompt
		if (action.getDomain() != -1) {
			labelDomain = createLabeledLabel(composite_prompts, UserActionsResources.RESID_PP_USERACTION_DOMAIN_LABEL, UserActionsResources.RESID_PP_USERACTION_DOMAIN_TOOLTIP);
			String[] domainNames = action.getManager().getActionSubSystem().getXlatedDomainNames();
			labelDomain.setText(domainNames[action.getDomain()]);
		}
		addSeparatorLine(composite_prompts, 2);
		// add edit pane...
		ISubSystemConfiguration ssf = getUDActionSubsystem(action).getSubSystemFactory();
		ISystemProfile profile = action.getProfile();
		editpane = getUDActionSubsystem(action).getCustomUDActionEditPane(null, ssf, profile, this, this);
		//System.out.println("UDActionSubsystem is of type: "+getUDActionSubsystem(action).getClass().getName());
		//System.out.println("EditPane is of type: "+editpane.getClass().getName());
		Control c = editpane.createContents(composite_prompts);
		((GridData) c.getLayoutData()).horizontalSpan = 2;
		editpane.setAction(action);
	}

	/**
	 * Return xlated string stating where the origin of the given user action is from:
	 */
	private String getOrigin(SystemUDActionElement action) {
		if (action.isIBM()) {
			if (action.isUserChanged())
				return UserActionsResources.RESID_PROPERTY_ORIGIN_IBMUSER_VALUE;
			else
				return UserActionsResources.RESID_PROPERTY_ORIGIN_IBM_VALUE;
		} else
			return UserActionsResources.RESID_PROPERTY_ORIGIN_USER_VALUE;
	}

	/**
	 * Called by parent when user presses OK or Apply
	 */
	public boolean performOk() {
		boolean ok = super.performOk();
		if (!ok) return false;
		editpane.applyPressed();
		return ok;
	}

	/**
	 * Called by parent when user presses Default button
	 */
	public void performDefaults() {
		editpane.revertPressed();
	}

	/**
	 * Called by parent when user presses OK button
	 */
	public boolean performCancel() {
		return super.performCancel();
	}

	/**
	 * Identify that the page/dialog is complete
	 */
	public void setPageComplete(boolean complete) {
		setValid(complete);
	}

	/**
	 * Set the help for the given control
	 */
	public void setHelp(Control c, String id) {
		SystemWidgetHelpers.setHelp(c, id);
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#expandDomainNodes()
	 */
	public void expandDomainNodes() {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#expandDomainNode(java.lang.String)
	 */
	public void expandDomainNode(String displayName) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#getDocumentManager()
	 */
	public SystemUDBaseManager getDocumentManager() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#getSelectedElementName()
	 */
	public String getSelectedElementName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#getSelectedElementDomain()
	 */
	public int getSelectedElementDomain() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#isElementAllSelected()
	 */
	public boolean isElementAllSelected() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#getSelectedElement()
	 */
	public SystemXMLElementWrapper getSelectedElement() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#selectElement(com.ibm.etools.systems.core.ui.uda.SystemXMLElementWrapper)
	 */
	public void selectElement(SystemXMLElementWrapper element) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#findParentItem(com.ibm.etools.systems.core.ui.uda.SystemXMLElementWrapper)
	 */
	public TreeItem findParentItem(SystemXMLElementWrapper element) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#refreshElementParent(com.ibm.etools.systems.core.ui.uda.SystemXMLElementWrapper)
	 */
	public void refreshElementParent(SystemXMLElementWrapper element) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#getSelectedTreeItem()
	 */
	public TreeItem getSelectedTreeItem() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#getSelectedPreviousTreeItem()
	 */
	public TreeItem getSelectedPreviousTreeItem() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#getSelectedNextTreeItem()
	 */
	public TreeItem getSelectedNextTreeItem() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#getSelectedNextNextTreeItem()
	 */
	public TreeItem getSelectedNextNextTreeItem() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#isSelectionVendorSupplied()
	 */
	public boolean isSelectionVendorSupplied() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#getVendorOfSelection()
	 */
	public String getVendorOfSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.uda.ISystemUDTreeView#refresh(java.lang.Object)
	 */
	public void refresh(Object element) {
		// TODO Auto-generated method stub
	}
}
