/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IArrayType extends IType {
	/**
	 * Returns the type that this is an array of.
	 */
	IType getType();

	/**
	 * Returns the value for the size of the array type, or {@code null} if it is unspecified.
	 * @since 5.2
	 */
	IValue getSize();

	/**
	 * Checks is the array type has specified size.
	 * @since 5.9
	 */
	boolean hasSize();

	/**
	 * @deprecated Replaced by {@link #getSize()}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	IASTExpression getArraySizeExpression() throws DOMException;
}
