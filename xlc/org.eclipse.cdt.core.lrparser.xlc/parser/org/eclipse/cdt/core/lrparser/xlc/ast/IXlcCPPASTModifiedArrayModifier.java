/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.xlc.ast;

import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;

/**
 * Allow C99 style variable length arrays in XL C++.
 *
 */
public interface IXlcCPPASTModifiedArrayModifier extends IASTArrayModifier {

	/**
	 * Is the const modifier used?
	 *
	 * @return boolean
	 */
	public boolean isConst();

	/**
	 * Is the static modifier used?
	 *
	 * @return boolean
	 */
	public boolean isStatic();

	/**
	 * Is the restrict modifier used?
	 *
	 * @return boolean
	 */
	public boolean isRestrict();

	/**
	 * Is the volatile modifier used?
	 *
	 * @return boolean
	 */
	public boolean isVolatile();

	/**
	 * Set true/false that the const modifier is used.
	 *
	 * @param value
	 *            boolean
	 */
	public void setConst(boolean value);

	/**
	 * Set true/false that the volatile modifier is used.
	 *
	 * @param value
	 *            boolean
	 */
	public void setVolatile(boolean value);

	/**
	 * Set true/false that the restrict modifier is used.
	 *
	 * @param value
	 *            boolean
	 */
	public void setRestrict(boolean value);

	/**
	 * Set true/false that the static modifier is used.
	 *
	 * @param value
	 *            boolean
	 */
	public void setStatic(boolean value);

	/**
	 * Is the array variable sized? ( used ... )
	 *
	 * @return boolean
	 */
	public boolean isVariableSized();

	/**
	 * Set the array to be variable sized dependent upon value.
	 *
	 * @param value
	 *            boolean
	 */
	public void setVariableSized(boolean value);

	/**
	 */
	@Override
	public IXlcCPPASTModifiedArrayModifier copy();

}
