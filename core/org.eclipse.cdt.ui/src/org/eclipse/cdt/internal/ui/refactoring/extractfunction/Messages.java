/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	public static String ExtractFunctionRefactoringWizard_FunctionName;
	public static String ExtractFunctionRefactoring_ExtractFunction;
	public static String ExtractFunctionRefactoring_NoStmtSelected;
	public static String ExtractFunctionRefactoring_TooManySelected;
	public static String ExtractFunctionRefactoring_NameInUse;
	public static String ExtractFunctionComposite_MethodName;
	public static String ExtractFunctionComposite_FunctionName;
	public static String ExtractFunctionInputPage_EnterName;
	public static String ExtractFunctionInputPage_CheckFunctionName;
	public static String ExtractFunctionInputPage_1;
	public static String ExtractFunctionComposite_ReturnValue;
	public static String ExtractFunctionRefactoring_CreateMethodDef;
	public static String ExtractFunctionRefactoring_CreateFunctionDef;
	public static String ExtractFunctionComposite_ReplaceDuplicates;
	public static String ExtractFunctionComposite_Virtual;
	public static String ExtractFunctionRefactoring_CreateMethodCall;
	public static String ExtractFunctionRefactoring_CreateFunctionCall;
	public static String ChooserComposite_Return;
	public static String ChooserComposite_CallByRef;
	public static String ChooserComposite_const;
	public static String ChooserComposite_Name;
	public static String ChooserComposite_Type;
	public static String ChooserComposite_NoReturnValue;
	public static String ExtractFunctionRefactoring_Error_Return;
	public static String ExtractFunctionRefactoring_Error_Continue;
	public static String ExtractFunctionRefactoring_Error_Break;
	public static String SimilarFinderVisitor_replaceDuplicateCode;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
