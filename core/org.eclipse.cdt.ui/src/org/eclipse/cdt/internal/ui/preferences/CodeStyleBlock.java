/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * Configures elements of C/C++ code style affecting refactoring.
 */
class CodeStyleBlock extends OptionsConfigurationBlock {
	private static final Key CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER = getCDTUIKey(
			PreferenceConstants.CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER);
	private static final Key FUNCTION_OUTPUT_PARAMETERS_BEFORE_INPUT = getCDTUIKey(
			PreferenceConstants.FUNCTION_OUTPUT_PARAMETERS_BEFORE_INPUT);
	private static final Key FUNCTION_PASS_OUTPUT_PARAMETERS_BY_POINTER = getCDTUIKey(
			PreferenceConstants.FUNCTION_PASS_OUTPUT_PARAMETERS_BY_POINTER);
	private static final Key PLACE_CONST_RIGHT_OF_TYPE = getKey(CCorePlugin.PLUGIN_ID,
			CCorePreferenceConstants.PLACE_CONST_RIGHT_OF_TYPE);
	private static final Key ADD_OVERRIDE_KEYWORD = getKey(CCorePlugin.PLUGIN_ID,
			CCorePreferenceConstants.ADD_OVERRIDE_KEYWORD);
	private static final Key PRESERVE_VIRTUAL_KEYWORD = getKey(CCorePlugin.PLUGIN_ID,
			CCorePreferenceConstants.PRESERVE_VIRTUAL_KEYWORD);

	private static Key[] getAllKeys() {
		return new Key[] { CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER, FUNCTION_OUTPUT_PARAMETERS_BEFORE_INPUT,
				FUNCTION_PASS_OUTPUT_PARAMETERS_BY_POINTER, PLACE_CONST_RIGHT_OF_TYPE, ADD_OVERRIDE_KEYWORD,
				PRESERVE_VIRTUAL_KEYWORD };
	}

	public CodeStyleBlock(IStatusChangeListener context, IProject project, IWorkbenchPreferenceContainer container) {
		super(context, project, getAllKeys(), container);
	}

	@Override
	public Control createContents(Composite parent) {
		ScrolledPageContent scrolled = new ScrolledPageContent(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolled.setExpandHorizontal(true);
		scrolled.setExpandVertical(true);

		Composite control = new Composite(scrolled, SWT.NONE);
		GridLayout layout = new GridLayout();
		control.setLayout(layout);

		Composite composite = addSubsection(control, PreferencesMessages.CodeStyleBlock_class_member_order);
		fillClassMemberOrderSection(composite);

		composite = addSubsection(control, PreferencesMessages.CodeStyleBlock_function_parameter_order);
		fillFunctionParameterOrderSection(composite);

		composite = addSubsection(control, PreferencesMessages.CodeStyleBlock_function_output_parameter_style);
		fillFunctionOutputParameterStyleSection(composite);

		composite = addSubsection(control, PreferencesMessages.CodeStyleBlock_const_keyword_placement);
		fillConstPlacementsSections(composite);

		composite = addSubsection(control, PreferencesMessages.CodeStyleBlock_function_overridden_methods);
		fillOverriddenSection(composite);

		scrolled.setContent(control);
		final Point size = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrolled.setMinSize(size.x, size.y);
		return scrolled;
	}

	private void fillClassMemberOrderSection(Composite composite) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);

		addRadioButton(composite, PreferencesMessages.CodeStyleBlock_public_private,
				CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER, FALSE_TRUE, 0);
		addRadioButton(composite, PreferencesMessages.CodeStyleBlock_private_public,
				CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER, TRUE_FALSE, 0);
	}

	private void fillFunctionParameterOrderSection(Composite composite) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);

		addRadioButton(composite, PreferencesMessages.CodeStyleBlock_input_output,
				FUNCTION_OUTPUT_PARAMETERS_BEFORE_INPUT, FALSE_TRUE, 0);
		addRadioButton(composite, PreferencesMessages.CodeStyleBlock_output_input,
				FUNCTION_OUTPUT_PARAMETERS_BEFORE_INPUT, TRUE_FALSE, 0);
	}

	private void fillFunctionOutputParameterStyleSection(Composite composite) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);

		addRadioButton(composite, PreferencesMessages.CodeStyleBlock_pass_by_reference,
				FUNCTION_PASS_OUTPUT_PARAMETERS_BY_POINTER, FALSE_TRUE, 0);
		addRadioButton(composite, PreferencesMessages.CodeStyleBlock_pass_by_pointer,
				FUNCTION_PASS_OUTPUT_PARAMETERS_BY_POINTER, TRUE_FALSE, 0);
	}

	private void fillConstPlacementsSections(Composite composite) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);

		addRadioButton(composite, PreferencesMessages.CodeStyleBlock_const_left, PLACE_CONST_RIGHT_OF_TYPE, FALSE_TRUE,
				0);
		addRadioButton(composite, PreferencesMessages.CodeStyleBlock_const_right, PLACE_CONST_RIGHT_OF_TYPE, TRUE_FALSE,
				0);
	}

	private void fillOverriddenSection(Composite composite) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);

		addCheckBox(composite, PreferencesMessages.CodeStyleBlock_add_override_keyword, ADD_OVERRIDE_KEYWORD,
				TRUE_FALSE, 0);
		addCheckBox(composite, PreferencesMessages.CodeStyleBlock_preserve_virtual, PRESERVE_VIRTUAL_KEYWORD,
				TRUE_FALSE, 0);
	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
	}
}
