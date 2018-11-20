/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is the root interface for statements.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTStatement extends IASTAttributeOwner {
	public static final IASTStatement[] EMPTY_STATEMENT_ARRAY = {};

	/**
	 * @since 5.1
	 */
	@Override
	public IASTStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTStatement copy(CopyStyle style);
}
