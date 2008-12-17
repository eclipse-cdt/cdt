/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;

/**
 * <pre> class X {
 *     int a;
 *     X();
 * };
 * X::X : a(0) {}  // a(0) is a constructor chain initializer.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTConstructorChainInitializer extends IASTInitializer, IASTNameOwner {
	/**
	 * Constant.
	 */
	public static final ICPPASTConstructorChainInitializer[] EMPTY_CONSTRUCTORCHAININITIALIZER_ARRAY = new ICPPASTConstructorChainInitializer[0];

	/**
	 * <code>MEMBER_ID</code> represents the class field name being
	 * initialized.
	 */
	public static final ASTNodeProperty MEMBER_ID = new ASTNodeProperty(
			"ICPPASTConstructorChainInitializer.MEMBER_ID - Class field name initialized"); //$NON-NLS-1$

	/**
	 * Get the field name.
	 * 
	 * @return <code>IASTName</code>
	 */
	public IASTName getMemberInitializerId();

	/**
	 * Set the field name.
	 * 
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void setMemberInitializerId(IASTName name);

	/**
	 * <code>Expression field is being initialized to.</code>
	 */
	public static final ASTNodeProperty INITIALIZER = new ASTNodeProperty(
			"ICPPASTConstructorChainInitializer.INITIALIZER - Expression Field Initializer"); //$NON-NLS-1$

	/**
	 * Get the initializer value.
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getInitializerValue();

	/**
	 * Set the initializer value.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code>
	 */
	public void setInitializerValue(IASTExpression expression);

	/**
	 * @since 5.1
	 */
	public ICPPASTConstructorChainInitializer copy();
}
