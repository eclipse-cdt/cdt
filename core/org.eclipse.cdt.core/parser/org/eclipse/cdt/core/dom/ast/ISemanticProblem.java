/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast;

/**
 * Base interface for all semantic problems: {@link IProblemBinding}, {@link IProblemType}
 * 
 * @since 5.3
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISemanticProblem {
	int BINDING_NOT_FOUND 							= 	  1; 
    int BINDING_INVALID_OVERLOAD 					=     2;
    int BINDING_INVALID_USING 						=     3;
    int BINDING_AMBIGUOUS_LOOKUP 					=     4;
    int BINDING_INVALID_TYPE 						=     5;
    int BINDING_CIRCULAR_INHERITANCE  				=     6;
    int BINDING_DEFINITION_NOT_FOUND  				=     7;
    int BINDING_KNR_PARAMETER_DECLARATION_NOT_FOUND = 	  8;
    int BINDING_LABEL_STATEMENT_NOT_FOUND  			=     9;
    int BINDING_BAD_SCOPE							=	 10;
    int BINDING_INVALID_REDEFINITION 				=    11;
    int BINDING_INVALID_REDECLARATION  				=    12;
    int BINDING_MEMBER_DECLARATION_NOT_FOUND  		=    13;
	int BINDING_RECURSION_IN_LOOKUP 				=    14;
	int BINDING_INVALID_TEMPLATE_ARGUMENTS 			=    15;
	int BINDING_NO_CLASS 							= 	 16; 

	int TYPE_NO_NAME 								= 10000;
	int TYPE_UNRESOLVED_NAME 						= 10001;
	int TYPE_AUTO_FOR_NON_STATIC_FIELD 				= 10002;
	int TYPE_CANNOT_DEDUCE_AUTO_TYPE 				= 10003;
	int TYPE_UNKNOWN_FOR_EXPRESSION 				= 10004;
	int TYPE_NOT_PERSISTED 							= 10005;

	/**
     * Returns the ID of the problem.
     */
    int getID();
    
    /**
     * A human-readable message that describes the problem.
     */
    String getMessage();
}
