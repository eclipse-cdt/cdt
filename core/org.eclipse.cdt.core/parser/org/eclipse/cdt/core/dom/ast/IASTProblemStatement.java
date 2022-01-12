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
 *    John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represents a parse problem where we tried to match against a statement.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTProblemStatement extends IASTStatement, IASTProblemHolder {
	/**
	 * @since 5.1
	 */
	@Override
	public IASTProblemStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTProblemStatement copy(CopyStyle style);
}
