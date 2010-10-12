/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *    Andrew Niefer (IBM Corporation) - Initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.core.dom.ast.ISemanticProblem;

import com.ibm.icu.text.MessageFormat;

public class ParserMessages {
	private static final String BUNDLE_NAME = ParserMessages.class.getName();
	private static ResourceBundle resourceBundle;
	
	static {
		try {
			resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}
	
	private ParserMessages() {
	}

	public static String getString(String key) {
		if (resourceBundle == null)
			return '#' + key +'#';
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	/**
	 * Gets a string from the resource bundle and formats it with the argument
	 * 
	 * @param key	the string used to get the bundle value, must not be null
	 */
	public static String getFormattedString(String key, Object[] args) {
		String format = getString(key);
		return MessageFormat.format(format, args);
	}

	/**
	 * Gets a string from the resource bundle and formats it with the argument
	 * 
	 * @param key	the string used to get the bundle value, must not be null
	 */
	public static String getFormattedString(String key, Object arg) {
		String format = getString(key);
		
		if (arg == null)
			arg = ""; //$NON-NLS-1$
		
		return MessageFormat.format(format, new Object[] { arg });
	}
	
	public static String getProblemPattern(ISemanticProblem problem) {
		String key= getProblemKey(problem.getID());
		if (key != null)
			return getString(key);
		return null;
	}

	@SuppressWarnings("nls")
	private static String getProblemKey(int id) {
		switch(id) {
		case ISemanticProblem.BINDING_AMBIGUOUS_LOOKUP: return "ISemanticProblem.BINDING_AMBIGUOUS_LOOKUP";
		case ISemanticProblem.BINDING_BAD_SCOPE: return "ISemanticProblem.BINDING_BAD_SCOPE";
		case ISemanticProblem.BINDING_CIRCULAR_INHERITANCE: return "ISemanticProblem.BINDING_CIRCULAR_INHERITANCE";
		case ISemanticProblem.BINDING_DEFINITION_NOT_FOUND: return "ISemanticProblem.BINDING_DEFINITION_NOT_FOUND";
		case ISemanticProblem.BINDING_INVALID_OVERLOAD: return "ISemanticProblem.BINDING_INVALID_OVERLOAD";
		case ISemanticProblem.BINDING_INVALID_REDECLARATION: return "ISemanticProblem.BINDING_INVALID_REDECLARATION";
		case ISemanticProblem.BINDING_INVALID_REDEFINITION: return "ISemanticProblem.BINDING_INVALID_REDEFINITION";
		case ISemanticProblem.BINDING_INVALID_TEMPLATE_ARGUMENTS: return "ISemanticProblem.BINDING_INVALID_TEMPLATE_ARGUMENTS";
		case ISemanticProblem.BINDING_INVALID_TYPE: return "ISemanticProblem.BINDING_INVALID_TYPE";
		case ISemanticProblem.BINDING_INVALID_USING: return "ISemanticProblem.BINDING_INVALID_USING";
		case ISemanticProblem.BINDING_KNR_PARAMETER_DECLARATION_NOT_FOUND: return "ISemanticProblem.BINDING_KNR_PARAMETER_DECLARATION_NOT_FOUND";
		case ISemanticProblem.BINDING_LABEL_STATEMENT_NOT_FOUND: return "ISemanticProblem.BINDING_LABEL_STATEMENT_NOT_FOUND";
		case ISemanticProblem.BINDING_MEMBER_DECLARATION_NOT_FOUND: return "ISemanticProblem.BINDING_MEMBER_DECLARATION_NOT_FOUND";
		case ISemanticProblem.BINDING_NO_CLASS: return "ISemanticProblem.BINDING_NO_CLASS";
		case ISemanticProblem.BINDING_NOT_FOUND: return "ISemanticProblem.BINDING_NOT_FOUND";
		case ISemanticProblem.BINDING_RECURSION_IN_LOOKUP: return "ISemanticProblem.BINDING_RECURSION_IN_LOOKUP";
		
		case ISemanticProblem.TYPE_NO_NAME: return "ISemanticProblem.TYPE_NO_NAME";
		case ISemanticProblem.TYPE_UNRESOLVED_NAME: return "ISemanticProblem.TYPE_UNRESOLVED_NAME";
		case ISemanticProblem.TYPE_AUTO_FOR_NON_STATIC_FIELD: return "ISemanticProblem.TYPE_AUTO_FOR_NON_STATIC_FIELD";
		case ISemanticProblem.TYPE_CANNOT_DEDUCE_AUTO_TYPE: return "ISemanticProblem.TYPE_CANNOT_DEDUCE_AUTO_TYPE";
		case ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION: return "ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION";
		case ISemanticProblem.TYPE_NOT_PERSISTED: return "ISemanticProblem.TYPE_NOT_PERSISTED";		
		}
		return null;
	}
}
