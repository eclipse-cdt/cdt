/*******************************************************************************
 * Copyright (c) 2009,2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.osgi.util.NLS;

/**
 * Messages
 */
public class CheckersMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.codan.internal.checkers.messages"; //$NON-NLS-1$
	public static String CatchByReference_ReportForUnknownType;
	public static String NamingConventionFunctionChecker_LabelNamePattern;
	public static String ReturnChecker_Param0;
	public static String GenericParameter_ParameterExceptions;
	public static String GenericParameter_ParameterExceptionsItem;
	public static String StatementHasNoEffectChecker_ParameterMacro;
	public static String SuggestedParenthesisChecker_SuggestParanthesesAroundNot;
	
	public static String ProblemBindingChecker_Candidates;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, CheckersMessages.class);
	}

	private CheckersMessages() {
	}
}
