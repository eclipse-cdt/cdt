/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia
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
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.osgi.util.NLS;

/**
 * Messages
 */
public class CheckersMessages extends NLS {
	public static String CaseBreakChecker_DefaultNoBreakCommentDescription;
	public static String CaseBreakChecker_EmptyCaseDescription;
	public static String CaseBreakChecker_LastCaseDescription;
	public static String CaseBreakChecker_EnableFallthroughQuickfixDescription;
	public static String CatchByReference_ReportForUnknownType;
	public static String ClassMembersInitializationChecker_SkipConstructorsWithFCalls;
	public static String NamingConventionFunctionChecker_LabelNamePattern;
	public static String NamingConventionFunctionChecker_ParameterMethods;
	public static String ReturnChecker_Param0;
	public static String GenericParameter_ParameterExceptions;
	public static String GenericParameter_ParameterExceptionsItem;
	public static String StatementHasNoEffectChecker_ParameterMacro;
	public static String SuggestedParenthesisChecker_SuggestParanthesesAroundNot;
	public static String SuspiciousSemicolonChecker_ParamAfterElse;
	public static String SuspiciousSemicolonChecker_ParamElse;
	public static String ProblemBindingChecker_Candidates;
	public static String SwitchCaseChecker_ParameterDefaultAllEnums;
	public static String BlacklistChecker_list;
	public static String BlacklistChecker_list_item;
	public static String UnusedSymbolInFileScopeChecker_CharacterSequence;
	public static String UnusedSymbolInFileScopeChecker_Exceptions;
	public static String SymbolShadowingChecker_CheckFunctionParameters;

	public static String Copyright_regex;

	static {
		NLS.initializeMessages(CheckersMessages.class.getName(), CheckersMessages.class);
	}

	// Do not instantiate
	private CheckersMessages() {
	}
}
