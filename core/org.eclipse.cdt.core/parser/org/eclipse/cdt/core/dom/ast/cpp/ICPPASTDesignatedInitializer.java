/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sergey Prigogin (Google) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;

/**
 * This interface represents a designated initializer,
 * e.g. in struct A y = { .z = 4, .t[1] = 3 };
 * @since 6.0
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTDesignatedInitializer extends IASTInitializer, ICPPASTInitializerClause {
	/** The part of the initializer before the equal sign. */
	public static final ASTNodeProperty DESIGNATOR = new ASTNodeProperty(
			"ICPPASTDesignatedInitializer.DESIGNATOR - [ICPPASTDesignator]"); //$NON-NLS-1$

	/** The part of the initializer after the equal sign. */
	public static final ASTNodeProperty OPERAND = new ASTNodeProperty(
			"ICPPASTDesignatedInitializer.OPERAND - [ICPPASTInitializerClause]"); //$NON-NLS-1$

	/**
	 * Adds a designator to this initializer.
	 */
	public void addDesignator(ICPPASTDesignator designator);

	/**
	 * Returns all of the designators.
	 */
	public ICPPASTDesignator[] getDesignators();

	/**
	 * Returns the operand of the initializer.
	 */
	public ICPPASTInitializerClause getOperand();

	/**
	 * Sets the initializer clause. Not allowed on a frozen AST.
	 */
	void setOperand(ICPPASTInitializerClause operand);

	@Override
	public ICPPASTDesignatedInitializer copy();

	@Override
	public ICPPASTDesignatedInitializer copy(CopyStyle style);
}
