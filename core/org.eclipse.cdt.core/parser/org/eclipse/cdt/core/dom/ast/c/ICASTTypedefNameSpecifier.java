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
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;

/**
 * This interface is just as an IASTNamedTypeSpecifier, except that it also
 * includes the abiliy to use the restrict modifier.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICASTTypedefNameSpecifier extends IASTNamedTypeSpecifier, ICASTDeclSpecifier {
	/**
	 * @since 5.1
	 */
	@Override
	public ICASTTypedefNameSpecifier copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICASTTypedefNameSpecifier copy(CopyStyle style);
}
