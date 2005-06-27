/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.base;


public class RefactoringStatusCodes {

	private RefactoringStatusCodes() {
	}

	public static final int NONE= 0;
	
	public static final int OVERRIDES_ANOTHER_METHOD= 						1;
	public static final int METHOD_DECLARED_IN_INTERFACE= 					2;
	
	public static final int EXPRESSION_NOT_RVALUE= 								64;
	public static final int EXPRESSION_NOT_RVALUE_VOID= 						65;
	public static final int EXTRANEOUS_TEXT= 											66;
	
	public static final int NOT_STATIC_FINAL_SELECTED= 							128;
	public static final int SYNTAX_ERRORS= 												129;
	public static final int DECLARED_IN_CLASSFILE= 									130;
	public static final int CANNOT_INLINE_BLANK_FINAL= 							131;
	public static final int LOCAL_AND_ANONYMOUS_NOT_SUPPORTED= 	132;
	public static final int REFERENCE_IN_CLASSFILE= 								133;
	public static final int WILL_NOT_REMOVE_DECLARATION= 					134;
	
	public static int CANNOT_MOVE_STATIC= 												192;
	public static int SELECT_METHOD_IMPLEMENTATION= 								193;
	public static int CANNOT_MOVE_NATIVE= 												194;
	public static int CANNOT_MOVE_SYNCHRONIZED= 									195;
	public static int CANNOT_MOVE_CONSTRUCTOR= 									196;
	public static int SUPER_REFERENCES_NOT_ALLOWED= 							197;
	public static int ENCLOSING_INSTANCE_REFERENCES_NOT_ALLOWED= 	198;
	public static int CANNOT_MOVE_RECURSIVE= 											199;
	public static int CANNOT_MOVE_TO_SAME_CU= 										200;
	public static int CANNOT_MOVE_TO_LOCAL= 											201;
	public static int METHOD_NOT_SELECTED= 												202;
	public static int NO_NEW_RECEIVERS= 														203;
	public static int PARAM_NAME_ALREADY_USED=										204;
	
	// inline method error codes
	public static final int INLINE_METHOD_FIELD_INITIALIZER= 						256;
	public static final int INLINE_METHOD_LOCAL_INITIALIZER= 						257;
	public static final int INLINE_METHOD_NULL_BINDING= 						   		258;
	public static final int INLINE_METHOD_ONLY_SIMPLE_FUNCTIONS=				259;
	public static final int INLINE_METHOD_EXECUTION_FLOW= 							260;
	public static final int INLINE_METHOD_INITIALIZER_IN_FRAGEMENT= 			261;
}
