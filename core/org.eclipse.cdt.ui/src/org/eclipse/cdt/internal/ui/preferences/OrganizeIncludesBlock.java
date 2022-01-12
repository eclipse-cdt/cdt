/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
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

import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludePreferences.UnusedStatementDisposition;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * The preference block for configuring Organize Includes command.
 */
public class OrganizeIncludesBlock extends OptionsConfigurationBlock {
	private static final Key KEY_HEURISTIC_HEADER_SUBSTITUTION = getCDTUIKey(
			PreferenceConstants.INCLUDES_HEURISTIC_HEADER_SUBSTITUTION);
	private static final Key KEY_PARTNER_INDIRECT_INCLUSION = getCDTUIKey(
			PreferenceConstants.INCLUDES_ALLOW_PARTNER_INDIRECT_INCLUSION);
	private static final Key KEY_INCLUDES_REORDERING = getCDTUIKey(PreferenceConstants.INCLUDES_ALLOW_REORDERING);
	private static final Key KEY_UNUSED_STATEMENTS_DISPOSITION = getCDTUIKey(
			PreferenceConstants.INCLUDES_UNUSED_STATEMENTS_DISPOSITION);
	private static final Key KEY_FORWARD_DECLARE_COMPOSITE_TYPES = getCDTUIKey(
			PreferenceConstants.FORWARD_DECLARE_COMPOSITE_TYPES);
	private static final Key KEY_FORWARD_DECLARE_ENUMS = getCDTUIKey(PreferenceConstants.FORWARD_DECLARE_ENUMS);
	private static final Key KEY_FORWARD_DECLARE_FUNCTIONS = getCDTUIKey(PreferenceConstants.FORWARD_DECLARE_FUNCTIONS);
	private static final Key KEY_FORWARD_DECLARE_EXTERNAL_VARIABLES = getCDTUIKey(
			PreferenceConstants.FORWARD_DECLARE_EXTERNAL_VARIABLES);
	private static final Key KEY_FORWARD_DECLARE_TEMPLATES = getCDTUIKey(PreferenceConstants.FORWARD_DECLARE_TEMPLATES);
	private static final Key KEY_FORWARD_DECLARE_NAMESPACE_ELEMENTS = getCDTUIKey(
			PreferenceConstants.FORWARD_DECLARE_NAMESPACE_ELEMENTS);

	private static final String[] DISPOSITION_VALUES = { UnusedStatementDisposition.REMOVE.toString(),
			UnusedStatementDisposition.COMMENT_OUT.toString(), UnusedStatementDisposition.KEEP.toString(), };
	private static final String[] DISPOSITION_LABELS = { PreferencesMessages.OrganizeIncludesBlock_remove,
			PreferencesMessages.OrganizeIncludesBlock_comment_out, PreferencesMessages.OrganizeIncludesBlock_keep, };

	private static Key[] ALL_KEYS = { KEY_HEURISTIC_HEADER_SUBSTITUTION, KEY_PARTNER_INDIRECT_INCLUSION,
			KEY_INCLUDES_REORDERING, KEY_UNUSED_STATEMENTS_DISPOSITION, KEY_FORWARD_DECLARE_COMPOSITE_TYPES,
			KEY_FORWARD_DECLARE_ENUMS, KEY_FORWARD_DECLARE_FUNCTIONS, KEY_FORWARD_DECLARE_EXTERNAL_VARIABLES,
			KEY_FORWARD_DECLARE_TEMPLATES, KEY_FORWARD_DECLARE_NAMESPACE_ELEMENTS, };

	public OrganizeIncludesBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, ALL_KEYS, container);
	}

	@Override
	protected Control createContents(Composite parent) {
		setShell(parent.getShell());

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		Control control = addCheckBox(composite, PreferencesMessages.OrganizeIncludesBlock_allow_reordering,
				KEY_INCLUDES_REORDERING, TRUE_FALSE, 0);
		LayoutUtil.setHorizontalSpan(control, 2);
		control = addCheckBox(composite, PreferencesMessages.OrganizeIncludesBlock_heuristic_header_substitution,
				KEY_HEURISTIC_HEADER_SUBSTITUTION, TRUE_FALSE, 0);
		LayoutUtil.setHorizontalSpan(control, 2);
		control = addCheckBox(composite, PreferencesMessages.OrganizeIncludesBlock_partner_indirect_inclusion,
				KEY_PARTNER_INDIRECT_INCLUSION, TRUE_FALSE, 0);
		LayoutUtil.setHorizontalSpan(control, 2);
		control = addCheckBox(composite, PreferencesMessages.OrganizeIncludesBlock_forward_declare_composite_types,
				KEY_FORWARD_DECLARE_COMPOSITE_TYPES, TRUE_FALSE, 0);
		LayoutUtil.setHorizontalSpan(control, 2);
		control = addCheckBox(composite, PreferencesMessages.OrganizeIncludesBlock_forward_declare_enums,
				KEY_FORWARD_DECLARE_ENUMS, TRUE_FALSE, 0);
		LayoutUtil.setHorizontalSpan(control, 2);
		control = addCheckBox(composite, PreferencesMessages.OrganizeIncludesBlock_forward_declare_functions,
				KEY_FORWARD_DECLARE_FUNCTIONS, TRUE_FALSE, 0);
		LayoutUtil.setHorizontalSpan(control, 2);
		control = addCheckBox(composite, PreferencesMessages.OrganizeIncludesBlock_forward_declare_external_variables,
				KEY_FORWARD_DECLARE_EXTERNAL_VARIABLES, TRUE_FALSE, 0);
		LayoutUtil.setHorizontalSpan(control, 2);
		control = addCheckBox(composite, PreferencesMessages.OrganizeIncludesBlock_forward_declare_templates,
				KEY_FORWARD_DECLARE_TEMPLATES, TRUE_FALSE, 0);
		LayoutUtil.setHorizontalSpan(control, 2);
		control = addCheckBox(composite, PreferencesMessages.OrganizeIncludesBlock_forward_declare_namespace_elements,
				KEY_FORWARD_DECLARE_NAMESPACE_ELEMENTS, TRUE_FALSE, 0);
		LayoutUtil.setHorizontalSpan(control, 2);
		control = addComboBox(composite, PreferencesMessages.OrganizeIncludesBlock_unused_statements,
				KEY_UNUSED_STATEMENTS_DISPOSITION, DISPOSITION_VALUES, DISPOSITION_LABELS, 0);
		LayoutUtil.setHorizontalSpan(getLabel(control), 1);

		updateControls();
		return composite;
	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		StatusInfo status = new StatusInfo();
		fContext.statusChanged(status);
	}
}
