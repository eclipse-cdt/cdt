/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.rse.internal.useractions.ui.propertypages;

import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemModelChangeEvents;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.IUserActionsModelChangeEvents;
import org.eclipse.rse.internal.useractions.UserActionsResources;
import org.eclipse.rse.internal.useractions.ui.compile.ISystemCompileCommandEditPaneHoster;
import org.eclipse.rse.internal.useractions.ui.compile.ISystemCompileCommandEditPaneListener;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileCommand;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileCommandEditPane;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileManager;
import org.eclipse.rse.internal.useractions.ui.compile.teamview.SystemTeamViewCompileCommandNode;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.propertypages.SystemBasePropertyPage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * The property page for compile command nodes in the Team view.
 * This is an output-only page.
 */
public class SystemTeamViewCompileCommandPropertyPage extends SystemBasePropertyPage implements ISystemCompileCommandEditPaneHoster, ISystemCompileCommandEditPaneListener {
	protected SystemCompileManager compileManager;
	protected SystemCompileCommandEditPane editpane;
	protected Label labelType, labelProfile, labelOrigin, ccLabel;
	protected Composite composite_prompts;
	protected boolean initDone = false;
	protected int nbrColumns;

	/**
	 * Constructor
	 */
	public SystemTeamViewCompileCommandPropertyPage() {
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
		nbrColumns = 2;
		composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);
		// Type prompt
		String typeLabel = SystemUDAResources.RESID_PP_PROPERTIES_TYPE_LABEL;
		String typeTooltip = SystemUDAResources.RESID_PP_PROPERTIES_TYPE_TOOLTIP;
		labelType = SystemWidgetHelpers.createLabeledLabel(composite_prompts, typeLabel, typeTooltip, false);
		labelType.setText(UserActionsResources.RESID_PP_COMPILECMD_TYPE_VALUE);
		// Profile prompt
		String profileLabel = UserActionsResources.RESID_PP_COMPILECMD_PROFILE_LABEL;
		String profileTooltip = UserActionsResources.RESID_PP_COMPILECMD_PROFILE_TOOLTIP;
		labelProfile = createLabeledLabel(composite_prompts, profileLabel, profileTooltip);
		// Source Type prompt
		String origPromptLabel = UserActionsResources.RESID_PP_COMPILECMD_ORIGIN_LABEL;
		String origPromptTooltip = UserActionsResources.RESID_PP_COMPILECMD_ORIGIN_TOOLTIP;
		labelOrigin = createLabeledLabel(composite_prompts, origPromptLabel, origPromptTooltip);
		// now add a top spacer line and visual separator line, for the edit pane
		addFillerLine(composite_prompts, nbrColumns);
		ccLabel = SystemWidgetHelpers.createLabel(composite_prompts, ""); //$NON-NLS-1$
		((GridData) ccLabel.getLayoutData()).horizontalSpan = nbrColumns;
		addSeparatorLine(composite_prompts, nbrColumns);
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
	protected SystemTeamViewCompileCommandNode getCompileCommand() {
		Object element = getElement();
		return ((SystemTeamViewCompileCommandNode) element);
	}

	/**
	 * Initialize values of input fields based on input
	 */
	protected void doInitializeFields() {
		initDone = true;
		SystemTeamViewCompileCommandNode cmd = getCompileCommand();
		// populate GUI...
		labelProfile.setText(cmd.getProfile().getName());
		labelOrigin.setText(getOrigin(cmd.getCompileCommand()));
		// edit pane
		compileManager = cmd.getCompileCommand().getParentType().getParentProfile().getParentManager();
		ISubSystemConfiguration ssf = compileManager.getSubSystemFactory();
		boolean caseSensitive = true;
		if (ssf != null) caseSensitive = ssf.isCaseSensitive();
		editpane = compileManager.getCompileCommandEditPane(getShell(), this, caseSensitive);
		Control editpaneComposite = editpane.createContents(composite_prompts);
		((GridData) editpaneComposite.getLayoutData()).horizontalSpan = nbrColumns;
		editpane.addChangeListener(this);
		editpane.isComplete();// side effect is initial enablement of test button
		editpane.setCompileCommand(cmd.getCompileCommand().getParentType(), cmd.getCompileCommand());
		editpane.configureHeadingLabel(ccLabel); // sets the heading for edit mode
	}

	/**
	 * Get xlated origin value 
	 */
	private String getOrigin(SystemCompileCommand cmd) {
		if (cmd.isIBMSupplied()) {
			if (!cmd.getCurrentString().equals(cmd.getDefaultString()))
				return UserActionsResources.RESID_PROPERTY_ORIGIN_IBMUSER_VALUE;
			else
				return UserActionsResources.RESID_PROPERTY_ORIGIN_IBM_VALUE;
		} else if (cmd.isISVSupplied()) {
			if (!cmd.getCurrentString().equals(cmd.getDefaultString()))
				return UserActionsResources.RESID_PROPERTY_ORIGIN_ISVUSER_VALUE;
			else
				return UserActionsResources.RESID_PROPERTY_ORIGIN_ISV_VALUE;
		} else
			return UserActionsResources.RESID_PROPERTY_ORIGIN_USER_VALUE;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.compile.ISystemCompileCommandEditPaneListener#compileCommandChanged(com.ibm.etools.systems.core.ui.messages.SystemMessage)
	 */
	public void compileCommandChanged(SystemMessage message) {
		if (message == null)
			clearErrorMessage();
		else
			setErrorMessage(message);
	}

	/**
	 * Intercept of parent.
	 * Called when user presses OK.
	 */
	public boolean performOk() {
		boolean ok = super.performOk();
		if (!ok || (editpane.verify() != null)) // verify will call back to compileCommandChanged
			return false;
		SystemCompileCommand editedCompileCmd = editpane.saveChanges();
		ok = (editedCompileCmd != null);
		if (!ok) return false;
		getCompileCommand().getCompileCommand().getParentType().getParentProfile().writeToDisk();
		RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, IUserActionsModelChangeEvents.SYSTEM_RESOURCETYPE_COMPILECMD, editedCompileCmd, null);
		return ok;
	}

	/**
	 * Called by parent when user presses Default button
	 */
	public void performDefaults() {
		editpane.clearErrorMessage();
		SystemTeamViewCompileCommandNode cmd = getCompileCommand();
		editpane.setCompileCommand(cmd.getCompileCommand().getParentType(), cmd.getCompileCommand());
		clearErrorMessage();
	}

	/**
	 * Called by parent when user presses OK button
	 */
	public boolean performCancel() {
		return super.performCancel();
	}
}
