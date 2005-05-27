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
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;

/**
 * G++ adds its own modifiers and types to the Simple Decl Specifier.
 * 
 * @author jcamelon
 */
public interface IGPPASTSimpleDeclSpecifier extends IGPPASTDeclSpecifier,
		ICPPASTSimpleDeclSpecifier {

	/**
	 * <code>t_typeof</code> represents a typeof() expression type.
	 */
	public static final int t_typeof = ICPPASTSimpleDeclSpecifier.t_last + 1;

	/**
	 * <code>t_last</code> is for subinterfaces to extend these types.
	 */
	public static final int t_last = t_typeof;
	
	/**
	 * Is complex number? e.g. _Complex t;
	 * @return true if it is a complex number, false otherwise
	 */
	public boolean isComplex();
	
	/**
	 * Set the number to be complex.
	 * @param value true if it is a complex number, false otherwise
	 */
	public void setComplex(boolean value);
	
	/**
	 * Is imaginary number? e.g. _Imaginr
	 * @return true if it is an imaginary number, false otherwise
	 */
	public boolean isImaginary();
	
	/**
	 * Set the number to be imaginary.
	 * @param value true if it is an imaginary number, false otherwise
	 */
	public void setImaginary(boolean value);
	
	/**
	 * <code>TYPEOF_EXPRESSION</code> represents the relationship between the
	 * decl spec & the expression for typeof().
	 */
	public static final ASTNodeProperty TYPEOF_EXPRESSION = new ASTNodeProperty(
			"IGPPASTSimpleDeclSpecifier.TYPEOF_EXPRESSION - typeof() Expression"); //$NON-NLS-1$

	/**
	 * Did we encounter "long long" as a modifier?
	 * 
	 * @return boolean
	 */
	public boolean isLongLong();

	/**
	 * Encountered "long long" - set true or false.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setLongLong(boolean value);

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
