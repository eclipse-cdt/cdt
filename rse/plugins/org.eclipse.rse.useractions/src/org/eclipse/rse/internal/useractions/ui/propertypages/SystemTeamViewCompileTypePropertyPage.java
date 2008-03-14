package org.eclipse.rse.internal.useractions.ui.propertypages;

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
import org.eclipse.rse.internal.useractions.UserActionsResources;
import org.eclipse.rse.internal.useractions.ui.compile.teamview.SystemTeamViewCompileTypeNode;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.propertypages.SystemBasePropertyPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * The property page for compile type nodes in the Team view.
 * This is an output-only page.
 */
public class SystemTeamViewCompileTypePropertyPage extends SystemBasePropertyPage {
	protected Label labelType, labelProfile, labelFileType;
	protected String errorMessage;
	protected boolean initDone = false;

	/**
	 * Constructor
	 */
	public SystemTeamViewCompileTypePropertyPage() {
		super();
	}

	/**
	 * Create the page's GUI contents.
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContentArea(Composite parent) {
		// Inner composite
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, 2);
		// Type prompt
		String typeLabel = SystemUDAResources.RESID_PP_PROPERTIES_TYPE_LABEL;
		String typeTooltip = SystemUDAResources.RESID_PP_PROPERTIES_TYPE_TOOLTIP;
		labelType = createLabeledLabel(composite_prompts, typeLabel, typeTooltip);
		labelType.setText(UserActionsResources.RESID_PP_COMPILETYPE_TYPE_VALUE);
		// Profile prompt
		String profileLabel = UserActionsResources.RESID_PP_COMPILETYPE_PROFILE_LABEL;
		String profileTooltip = UserActionsResources.RESID_PP_COMPILETYPE_PROFILE_TOOLTIP;
		labelProfile = createLabeledLabel(composite_prompts, profileLabel, profileTooltip);
		// Source Type prompt
		String fileTypeLabel = UserActionsResources.RESID_PP_COMPILETYPE_FILETYPE_LABEL;
		String fileTypeTooltip = UserActionsResources.RESID_PP_COMPILETYPE_FILETYPE_TOOLTIP;
		labelFileType = createLabeledLabel(composite_prompts, fileTypeLabel, fileTypeTooltip);
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
	protected SystemTeamViewCompileTypeNode getCompileType() {
		Object element = getElement();
		return ((SystemTeamViewCompileTypeNode) element);
	}

	/**
	 * Initialize values of input fields based on input
	 */
	protected void doInitializeFields() {
		initDone = true;
		SystemTeamViewCompileTypeNode type = getCompileType();
		// populate GUI...
		labelProfile.setText(type.getProfile().getName());
		labelFileType.setText(type.getCompileType().getType());
	}
}
