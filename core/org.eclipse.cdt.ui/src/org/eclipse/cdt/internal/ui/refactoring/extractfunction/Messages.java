/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik  
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
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
	public static String ExtractFunctionRefactoring_ExtractFunction;
	public static String ExtractFunctionRefactoring_NoStmtSelected;
	public static String ExtractFunctionRefactoring_TooManyDeclarations;
	public static String ExtractFunctionRefactoring_no_declaration_of_surrounding_method;
	public static String ExtractFunctionRefactoring_FunctionAmbiguity;
	public static String ExtractFunctionRefactoring_parameter_name_in_use;
	public static String ExtractFunctionRefactoring_duplicate_parameter;
	public static String ExtractFunctionRefactoring_invalid_type;
	public static String ExtractFunctionRefactoring_CreateMethodDef;
	public static String ExtractFunctionRefactoring_CreateFunctionDef;
	public static String ExtractFunctionRefactoring_CreateMethodCall;
	public static String ExtractFunctionRefactoring_CreateFunctionCall;
	public static String ExtractFunctionRefactoring_Error_Return;
	public static String ExtractFunctionRefactoring_Error_Continue;
	public static String ExtractFunctionRefactoring_Error_Break;
	public static String ExtractFunctionRefactoring_Error_Case;
	public static String ExtractFunctionRefactoring_Error_Default;
	public static String ExtractFunctionRefactoring_IllegalGotoSelection;
	public static String ExtractFunctionRefactoring_IllegalHeadSelection;
	public static String ExtractFunctionRefactoring_IllegalHeadAndSomethingElseSelection;
	public static String ExtractFunctionRefactoring_IllegalBodyAndOutsideSelection;
	public static String ExtractFunctionRefactoring_IllegalBodySelection;
	public static String ExtractFunctionRefactoring_IllegalReturnSelection;
	public static String ExtractFunctionRefactoring_IllegalReturnSelectionCompound;
	public static String ExtractFunctionRefactoring_IllegalReturnSelectionIf;
	public static String ExtractFunctionRefactoring_IllegalReturnSelectionFor;
	public static String ExtractFunctionRefactoring_IllegalReturnSelectionWhile;
	public static String ExtractFunctionRefactoring_IllegalReturnSelectionDo;
	public static String ExtractFunctionRefactoring_IllegalReturnSelectionDefault;
	public static String ExtractFunctionRefactoring_IllegalReturnSelectionSwitch;
	public static String ExtractFunctionRefactoring_IllegalFunctionNameSelection;
	public static String ExtractFunctionRefactoring_Lvalue;
	public static String ExtractFunctionRefactoring_LocalType;
	public static String ExtractFunctionInputPage_description;
	public static String ExtractFunctionInputPage_access_modifier;
	public static String ExtractFunctionInputPage_public;
	public static String ExtractFunctionInputPage_protected;
	public static String ExtractFunctionInputPage_private;
	public static String ExtractFunctionInputPage_signature_preview;
	public static String ExtractFunctionInputPage_label_text;
	public static String ExtractFunctionInputPage_parameters;
	public static String ExtractFunctionInputPage_validation_empty_function_name;
	public static String ExtractFunctionInputPage_validation_empty_parameter_name;
	public static String ExtractFunctionInputPage_duplicates_none;
	public static String ExtractFunctionInputPage_duplicates_single;
	public static String ExtractFunctionInputPage_duplicates_multi;
	public static String SimilarFinderVisitor_replaceDuplicateCode;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
