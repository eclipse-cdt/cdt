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
 *    Doug Schaefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;

/**
 * This interface represents a built-in type in C.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICASTSimpleDeclSpecifier extends IASTSimpleDeclSpecifier, ICASTDeclSpecifier {
	/**
	 * @since 5.1
	 */
	@Override
	public ICASTSimpleDeclSpecifier copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICASTSimpleDeclSpecifier copy(CopyStyle style);

	/**
	 * @deprecated Replaced by {@link IASTSimpleDeclSpecifier#t_bool}.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_Bool = t_bool;
}
