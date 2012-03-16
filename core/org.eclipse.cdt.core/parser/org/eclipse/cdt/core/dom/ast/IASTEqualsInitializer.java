/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast;

/**
 * Initializer with equals sign (copy initialization) as in <code>int x= 0;</code>.
 * @since 5.2
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTEqualsInitializer extends IASTInitializer {
	ASTNodeProperty INITIALIZER = new ASTNodeProperty(
		"IASTEqualsInitializer - INITIALIZER [IASTInitializerClause]"); //$NON-NLS-1$
	
	/**
	 * Returns the expression or braced initializer list of this initializer.
	 */
	IASTInitializerClause getInitializerClause();
	
	/**
	 * Not allowed on frozen ast.
	 */
	void setInitializerClause(IASTInitializerClause clause);
}
