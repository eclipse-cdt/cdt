/*******************************************************************************
 * Copyright (c) 2011,2016 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.osgi.util.NLS;

public class QuickFixMessages extends NLS {
	public static String CaseBreakQuickFixBreak_Label;
	public static String CaseBreakQuickFixComment_Label;
	public static String CaseBreakQuickFixFallthroughAttribute_Label;
	public static String QuickFixCreateClass_CreateNewClass;
	public static String QuickFixCreateField_create_field;
	public static String QuickFixCreateLocalVariable_create_local_variable;
	public static String QuickFixCreateParameter_create_parameter;
	public static String QuickFixRenameMember_rename_member;
	public static String QuickFixAddSemicolon_add_semicolon;
	public static String QuickFixUsePointer_replace_dot;
	public static String QuickFixUseDotOperator_replace_ptr;
	public static String QuickFixForFixit_apply_fixit;
	public static String QuickFixSuppressProblem_Label;
	public static String QuickFixAddDefaultSwitch_add_default_to_switch;
	public static String QuickFixAddCaseSwitch_add_cases_to_switch;
	public static String QuickFixCppCast_const_cast;
	public static String QuickFixCppCast_dynamic_cast;
	public static String QuickFixCppCast_reinterpret_cast;
	public static String QuickFixCppCast_static_cast;

	static {
		NLS.initializeMessages(QuickFixMessages.class.getName(), QuickFixMessages.class);
	}

	private QuickFixMessages() {
	}
}
