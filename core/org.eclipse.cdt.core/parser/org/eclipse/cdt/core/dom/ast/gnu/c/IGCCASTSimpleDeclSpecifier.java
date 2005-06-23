/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;

public interface IGCCASTSimpleDeclSpecifier extends ICASTSimpleDeclSpecifier {
	
	/**
	 * <code>t_typeof</code> represents a typeof() expression type.
	 */
	public static final int t_typeof = ICASTSimpleDeclSpecifier.t_last + 1;
	
	/**
	 * <code>t_last</code> is specified for subinterfaces.
	 */
	public static final int t_last = t_typeof;

	/**
	 * <code>TYPEOF_EXPRESSION</code> represents the relationship between the
	 * decl spec & the expression for typeof().
	 */
	public static final ASTNodeProperty TYPEOF_EXPRESSION = new ASTNodeProperty(
			"IGCCASTSimpleDeclSpecifier.TYPEOF_EXPRESSION - typeof() Expression"); //$NON-NLS-1$
	
	/**
	 * Set the typeof() expression.
	 * 
	 * @param typeofExpression
	 *            <code>IASTExpression</code>
	 */
	public void setTypeofExpression(IASTExpression typeofExpression);

	/**
	 * Get the typeof expression.
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getTypeofExpression();
}
