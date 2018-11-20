/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;

/**
 * This interface represents a designated initializer,
 * e.g. struct x y = { .z = 4, .t[1] = 3 };
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICASTDesignatedInitializer extends IASTInitializer, IASTInitializerClause {
	public static final ICASTDesignator[] EMPTY_DESIGNATOR_ARRAY = {};

	public static final ASTNodeProperty DESIGNATOR = new ASTNodeProperty(
			"ICASTDesignatedInitializer.DESIGNATOR [ICASTDesignator]"); //$NON-NLS-1$

	public static final ASTNodeProperty OPERAND = new ASTNodeProperty(
			"ICASTDesignatedInitializer.OPERAND - [IASTInitializerClause]"); //$NON-NLS-1$

	/**
	 * Add a designator to this initializer.
	 */
	public void addDesignator(ICASTDesignator designator);

	/**
	 * Get all of the designators.
	 */
	public ICASTDesignator[] getDesignators();

	/**
	 * Returns the operand initializer.
	 * @since 5.2
	 */
	public IASTInitializerClause getOperand();

	/**
	 * Not allowed on frozen ast
	 * @since 5.2
	 */
	void setOperand(IASTInitializerClause operand);

	/**
	 * @since 5.1
	 */
	@Override
	public ICASTDesignatedInitializer copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICASTDesignatedInitializer copy(CopyStyle style);

	/**
	 * @deprecated Replaced by {@link #getOperand()};
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public IASTInitializer getOperandInitializer();

	/**
	 * @deprecated Replaced by setOperand();
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public void setOperandInitializer(IASTInitializer rhs);
}
