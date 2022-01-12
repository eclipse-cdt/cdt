/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
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
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;

/**
 * GCC-specific designator that allows for shorthand array range to be specified
 * in a designated initializer, e.g. in int a[6] = { [2 ... 4] = 29 }; or
 * struct ABC { int def[10]; } abc = { .def[4 ... 6] = 3 };
 * @since 6.0
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IGPPASTArrayRangeDesignator extends ICPPASTDesignator {
	/** The start of the index range. */
	public static final ASTNodeProperty SUBSCRIPT_FLOOR_EXPRESSION = new ASTNodeProperty(
			"IGPPASTArrayRangeDesignator.SUBSCRIPT_FLOOR_EXPRESSION - start of index range"); //$NON-NLS-1$

	/** The end of the index range. */
	public static final ASTNodeProperty SUBSCRIPT_CEILING_EXPRESSION = new ASTNodeProperty(
			"IGPPASTArrayRangeDesignator.SUBSCRIPT_CEILING_EXPRESSION - end of index range"); //$NON-NLS-1$

	/**
	 * Returns the start expression of the index range.
	 */
	public ICPPASTExpression getRangeFloor();

	/**
	 * Sets the start expression of the index range.
	 *
	 * @param expression the expression for the start of the range
	 */
	public void setRangeFloor(ICPPASTExpression expression);

	/**
	 * Returns the end expression of the index range.
	 */
	public ICPPASTExpression getRangeCeiling();

	/**
	 * Sets the end expression of the index range.
	 *
	 * @param expression the expression for the end of the range
	 */
	public void setRangeCeiling(ICPPASTExpression expression);

	@Override
	public IGPPASTArrayRangeDesignator copy();

	@Override
	public IGPPASTArrayRangeDesignator copy(CopyStyle style);
}
