/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.search.indexing;
/**
 * @author bgheorgh
 */
public interface IIndexConstants {

	/* index encoding */
	char[] REF= "ref/".toCharArray(); //$NON-NLS-1$
	
	char[] TYPE_REF= "typeRef/".toCharArray(); //$NON-NLS-1$
	int TYPE_REF_LENGTH = 8;
	
	char[] TYPE_DECL = "typeDecl/".toCharArray(); //$NON-NLS-1$
	char[] TYPE_ALL = "type".toCharArray(); //$NON-NLS-1$
	int TYPE_DECL_LENGTH = 9;
	
	char[] FUNCTION_REF= "functionRef/".toCharArray(); //$NON-NLS-1$
	int FUNCTION_REF_LENGTH=12;
	
	char[] FUNCTION_DECL= "functionDecl/".toCharArray(); //$NON-NLS-1$
	char[] FUNCTION_ALL= "function".toCharArray(); //$NON-NLS-1$
	int FUNCTION_DECL_LENGTH = 13;
		
	char[] CONSTRUCTOR_REF= "constructorRef/".toCharArray(); //$NON-NLS-1$
	char[] CONSTRUCTOR_DECL= "constructorDecl/".toCharArray(); //$NON-NLS-1$
	
	char[] NAMESPACE_REF= "namespaceRef/".toCharArray(); //$NON-NLS-1$
	int NAMESPACE_REF_LENGTH=13;
	
	char[] NAMESPACE_DECL= "namespaceDecl/".toCharArray(); //$NON-NLS-1$
	char[] NAMESPACE_ALL = "namespace".toCharArray(); //$NON-NLS-1$
	int NAMESPACE_DECL_LENGTH = 14;
	
		
	char[] FIELD_REF= "fieldRef/".toCharArray(); //$NON-NLS-1$
	int FIELD_REF_LENGTH=9;
	
	char[] FIELD_DECL= "fieldDecl/".toCharArray(); //$NON-NLS-1$
	char[] FIELD_ALL= "field".toCharArray(); //$NON-NLS-1$
	int FIELD_DECL_LENGTH = 10;
	
	char[] ENUMTOR_REF= "enumtorRef/".toCharArray(); //$NON-NLS-1$
	int ENUMTOR_REF_LENGTH=11;
	
	char[] ENUMTOR_DECL = "enumtorDecl/".toCharArray(); //$NON-NLS-1$
	char[] ENUMTOR_ALL = "enumtor".toCharArray(); //$NON-NLS-1$
	int ENUMTOR_DECL_LENGTH = 12;
	
	char[] METHOD_REF= "methodRef/".toCharArray(); //$NON-NLS-1$
	int METHOD_REF_LENGTH = 10;
	
	char[] METHOD_DECL= "methodDecl/".toCharArray(); //$NON-NLS-1$
	char[] METHOD_ALL= "method".toCharArray(); //$NON-NLS-1$
	int METHOD_DECL_LENGTH = 11;
		
	char[] TYPEDEF_DECL = "typeDecl/T/".toCharArray(); //$NON-NLS-1$
	int TYPEDEF_DECL_LENGTH = 11;
	
	char[] MACRO_DECL = "macroDecl/".toCharArray(); //$NON-NLS-1$
	int MACRO_DECL_LENGTH = 10;
	
	char[] INCLUDE_REF = "includeRef/".toCharArray(); //$NON-NLS-1$
	int INCLUDE_REF_LENGTH = 11;
	//a Var REF will be treated as  a typeREF
	//char[] VAR_REF= "varRef/".toCharArray(); //$NON-NLS-1$
	
	//a Struct REF will be treated as  a typeREF
	//char[] STRUCT_REF= "structRef/".toCharArray(); //$NON-NLS-1$
	
	//a Enum REF will be treated as a typeREF
	//char[] ENUM_REF= "enumRef/".toCharArray(); //$NON-NLS-1$

    //a UNION REF will be treated as a typeREF
	//char[] UNION_REF= "unionRef/".toCharArray(); //$NON-NLS-1$
	
	
	char[] SUPER_REF = "superRef/".toCharArray(); //$NON-NLS-1$

	char[] CLASS_DECL= "typeDecl/C/".toCharArray(); //$NON-NLS-1$
	char[] VAR_DECL= "typeDecl/V/".toCharArray(); //$NON-NLS-1$
	char[] STRUCT_DECL= "typeDecl/S/".toCharArray(); //$NON-NLS-1$
	char[] ENUM_DECL= "typeDecl/E/".toCharArray(); //$NON-NLS-1$
	char[] UNION_DECL= "typeDecl/U/".toCharArray(); //$NON-NLS-1$
	
	char[] OBJECT = "Object".toCharArray(); //$NON-NLS-1$
	char[][] COUNTS= 
		new char[][] { new char[] {'0'}, new char[] {'1'}, new char[] {'2'}, new char[] {'3'}, new char[] {'4'}, new char[] {'5'}, new char[] {'6'}, new char[] {'7'}, new char[] {'8'}, new char[] {'9'}
	};
	char CLASS_SUFFIX = 'C';
	char VAR_SUFFIX = 'V';
	char STRUCT_SUFFIX = 'S';
	char ENUM_SUFFIX = 'E';
	char UNION_SUFFIX = 'U';
	char TYPEDEF_SUFFIX = 'T';
	char DERIVED_SUFFIX = 'D';
	char FRIEND_SUFFIX = 'F';
	
	char TYPE_SUFFIX = 0;
	char SEPARATOR= '/';

	char[] ONE_STAR = new char[] {'*'};
	char[][] ONE_STAR_CHAR = new char[][] {ONE_STAR};

	// used as special marker for enclosing type name of local and anonymous classes
	char[] ONE_ZERO = new char[] {'0'}; 
	char[][] ONE_ZERO_CHAR = new char[][] {ONE_ZERO};
}
