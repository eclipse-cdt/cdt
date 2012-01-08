/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTReturnStatement extends IASTStatement {

	public static final ASTNodeProperty RETURNVALUE = new ASTNodeProperty(
			"IASTReturnValue.RETURNVALUE - [IASTInitializerClause]"); //$NON-NLS-1$

	/**
	 * This is the optional return value for this function.
	 * 
	 * @return the return expression or null.
	 */
	public IASTExpression getReturnValue();

	/**
	 * Returns the return value as {@link IASTInitializerClause}, or <code>null</code>.
	 * In c++ this can be an braced initializer list.
	 * @since 5.2
	 */
	public IASTInitializerClause getReturnArgument();

	/**
	 * Not allowed on frozen ast.
	 * @since 5.2
	 */
	public void setReturnArgument(IASTInitializerClause returnValue);

	/**
	 * Not allowed on frozen ast.
	 */
	public void setReturnValue(IASTExpression returnValue);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTReturnStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTReturnStatement copy(CopyStyle style);
}
