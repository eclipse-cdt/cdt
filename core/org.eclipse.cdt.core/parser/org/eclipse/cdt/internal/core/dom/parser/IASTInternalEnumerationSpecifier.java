/*******************************************************************************
 * Copyright (c) 2008, 2014 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;

/**
 * Internal interface for C or C++ enumeration specifiers.
 */
public interface IASTInternalEnumerationSpecifier extends IASTEnumerationSpecifier {
	/**
	 * Notifies that the value computation for the enumeration has started.
	 * Returns {@code true} if this is the first attempt to do so.
	 */
	boolean startValueComputation();

	/**
	 * Notifies that the value computation for the enumeration has finished.
	 */
	void finishValueComputation();

	/**
	 * Returns {@code true} if the value computation has started but hasn't finished yet.
	 */
	boolean isValueComputationInProgress();

	/**
	 * @since 5.1
	 */
	@Override
	public IASTInternalEnumerationSpecifier copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTInternalEnumerationSpecifier copy(CopyStyle style);
}
