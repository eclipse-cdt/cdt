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
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;

/**
 * C++ adds a few things to function declarators.
 * 
 * @author Doug Schaefer
 */
public interface ICPPASTFunctionDeclarator extends
		IASTStandardFunctionDeclarator {

	/**
	 * Is this a const method?
	 * 
	 * @return boolean
	 */
	public boolean isConst();

	/**
	 * Set the method to be const or not.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setConst(boolean value);

	/**
	 * Is this a volatile method?
	 * 
	 * @return boolean
	 */
	public boolean isVolatile();

	/**
	 * Set the method to be volatile or not.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setVolatile(boolean value);

	/**
	 * <code>EXCEPTION_TYPEID</code> represents the type IDs throws in the
	 * exception specification.
	 */
	public static final ASTNodeProperty EXCEPTION_TYPEID = new ASTNodeProperty(
			"Exception TypeId"); //$NON-NLS-1$

	/**
	 * Get the exception specification.
	 * 
	 * @return <code>IASTTypeId []</code>
	 */
	public IASTTypeId[] getExceptionSpecification();

	/**
	 * Add an exception specification type Id.
	 * 
	 * @param typeId
	 *            <code>IASTTypeId</code>
	 */
	public void addExceptionSpecificationTypeId(IASTTypeId typeId);

	/**
	 * Is the method pure virtual?
	 * 
	 * @return boolean
	 */
	public boolean isPureVirtual();

	/**
	 * Set thid method to be pure virtual.
	 * 
	 * @param isPureVirtual
	 *            boolean
	 */
	public void setPureVirtual(boolean isPureVirtual);

	/**
	 * <code>CONSTRUCTOR_CHAIN_MEMBER</code> is the role of a constructor
	 * chain initializer.
	 */
	public static final ASTNodeProperty CONSTRUCTOR_CHAIN_MEMBER = new ASTNodeProperty(
			"Constructor Chain Member"); //$NON-NLS-1$

	/**
	 * Get constructor chain.
	 * 
	 * @return <code>ICPPASTConstructorChainInitializer[]</code>
	 */
	public ICPPASTConstructorChainInitializer[] getConstructorChain();

	/**
	 * Add a constructor chain initializer to constructor chain.
	 * 
	 * @param initializer
	 *            ICPPASTConstructorChainInitializer
	 */
	public void addConstructorToChain(
			ICPPASTConstructorChainInitializer initializer);

	/**
	 * Get function scope this node represents.
	 * 
	 * @return ICPPFunctionScope scope
	 */
	public ICPPFunctionScope getFunctionScope();
}
