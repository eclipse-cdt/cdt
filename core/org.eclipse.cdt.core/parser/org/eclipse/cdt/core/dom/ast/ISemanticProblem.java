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
 * mstodo IProblemScope.
 * 
 * @since 5.3
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISemanticProblem {
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
