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
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;

/**
 * This interface represents a designated initializer. e.g. struct x y = { .z=4,
 * .t[1] = 3 };
 * 
 * @author jcamelon
 * 
 */
public interface ICASTDesignatedInitializer extends IASTInitializer {

	/**
	 * Constant.
	 */
	public static final ICASTDesignator[] EMPTY_DESIGNATOR_ARRAY = new ICASTDesignator[0];

	/**
	 * <code>DESIGNATOR</code> represents the relationship between an
	 * <code>ICASTDesignatedInitializer</code> and
	 * <code>ICASTDesignator</code>.
	 */
	public static final ASTNodeProperty DESIGNATOR = new ASTNodeProperty(
			"ICASTDesignatedInitializer.DESIGNATOR - relationship between ICASTDesignatedInitializer and ICASTDesignator"); //$NON-NLS-1$

	/**
	 * Add a designator to this initializer.
	 * 
	 * @param designator
	 *            <code>ICASTDesignator</code>
	 */
	public void addDesignator(ICASTDesignator designator);

	/**
	 * Get all of the designators.
	 * 
	 * @return <code>ICASTDesignator []</code>
	 */
	public ICASTDesignator[] getDesignators();

	/**
	 * <code>OPERAND</code> represents the relationship between
	 * <code>ICASTDesignatedInitializer</code> and its
	 * <code>IASTInitializer</code>.
	 */
	public static final ASTNodeProperty OPERAND = new ASTNodeProperty(
			"ICASTDesignatedInitializer.OPERAND - RHS IASTInitializer for ICASTDesignatedInitializer"); //$NON-NLS-1$

	/**
	 * Get the nested initializer.
	 * 
	 * @return <code>IASTInitializer</code>
	 */
	public IASTInitializer getOperandInitializer();

	/**
	 * Set the nested initializer.
	 * 
	 * @param rhs
	 *            <code>IASTInitializer</code>
	 */
	public void setOperandInitializer(IASTInitializer rhs);
}
