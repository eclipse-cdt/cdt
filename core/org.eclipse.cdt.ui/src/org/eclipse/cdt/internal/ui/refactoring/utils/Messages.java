/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	public static String IdentifierHelper_isKeyword;
	public static String IdentifierHelper_isValid;
	public static String IdentifierHelper_leadingDigit;
	public static String IdentifierHelper_emptyIdentifier;
	public static String IdentifierHelper_illegalCharacter;
	public static String IdentifierHelper_unidentifiedMistake;
	public static String Checks_validate_edit;
	public static String Checks_choose_name;
	public static String CodeAnalyzer_initializer_list;
	public static String StatementAnalyzer_doesNotCover;
	public static String StatementAnalyzer_do_body_expression;
	public static String StatementAnalyzer_for_initializer_expression;
	public static String StatementAnalyzer_for_expression_updater;
	public static String StatementAnalyzer_for_updater_body;
	public static String StatementAnalyzer_catch_argument;
	public static String StatementAnalyzer_while_expression_body;
	public static String StatementAnalyzer_try_statement;
	public static String StatementAnalyzer_switch_statement;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
