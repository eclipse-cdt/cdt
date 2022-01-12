/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;

/**
 * This interface represents the role of a C array modifier. C allows for
 * modifiers (const, restrict, etc.) as well as variable sized arrays.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICASTArrayModifier extends IASTArrayModifier {

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
	 * @since 5.1
	 */
	@Override
	public ICASTArrayModifier copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICASTArrayModifier copy(CopyStyle style);
}
