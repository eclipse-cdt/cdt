/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Mike Kucera (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * An implicit name is used to resolve uses of implicit bindings, such as overloaded operators.
 * 
 * Implicit names are not generated unless they resolve to something.
 *
 * @see ASTVisitor#shouldVisitImplicitNames
 * @since 5.1
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTImplicitName extends IASTName {

	public static final IASTImplicitName[] EMPTY_NAME_ARRAY = {}; 

	/**
	 * {@inheritDoc}
	 * Redeclared with strengthened postcondition.
	 * 
	 * Will not return null or a problem binding.
     * Implicit names are not generated unless they resolve to something.
	 */
	@Override
	public IBinding resolveBinding();

	/**
	 * Returns true if this node is an alternate.
	 * 
	 * Sometimes more than one implicit name is generated for the same binding
	 * but with different offsets, when this happens the additional names 
	 * generated are considered alternates.
	 * 
	 * @see ASTVisitor#shouldVisitImplicitNameAlternates
	 */
	public boolean isAlternate();

	/**
	 * Convenience method that returns true if this 
	 * name represents an overloaded operator.
	 */
	public boolean isOperator();

	/**
	 * This method is not supported on implicit names.
	 * 
	 * Implicit names are not copied when an AST is copied,
	 * instead the implicit names are regenerated when needed.
	 * 
	 * @throws UnsupportedOperationException always
	 */
	@Override
	IASTName copy() throws UnsupportedOperationException;
}
