/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;

/**
 * This interface represents a delete expression. delete [] operand;
 * 
 * @author jcamelon
 */
public interface ICPPASTDeleteExpression extends IASTExpression {

	/**
	 * <code>OPERAND</code> is the expression representing the pointer being
	 * deleted.
	 */
	public static final ASTNodeProperty OPERAND = new ASTNodeProperty("ICPPASTDeleteExpression.OPERAND - Expression of poniter being deleted"); //$NON-NLS-1$

	/**
	 * Get the operand.
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getOperand();

	/**
	 * @param expression
	 *            <code>IASTExpression</code>
	 */
	public void setOperand(IASTExpression expression);

	/**
	 * Set this to be the global delete function called.
	 * 
	 * @param global
	 *            boolean
	 */
	public void setIsGlobal(boolean global);

	/**
	 * Is this the global delete function called?
	 * 
	 * @return boolean
	 */
	public boolean isGlobal();

	/**
	 * Set this to be a vector delete. ([])
	 * 
	 * @param vectored
	 *            boolean
	 */
	public void setIsVectored(boolean vectored);

	/**
	 * Is this a delete [] ?
	 * 
	 * @return boolean
	 */
	public boolean isVectored();

}
