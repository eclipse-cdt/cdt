/*******************************************************************************
 * Copyright (c) 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTAttribute;

/**
 * Represents a C++11 (ISO/IEC 14882:2011 7.6) attribute.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.7
 */
public interface ICPPASTAttribute extends IASTAttribute {
	/**
	 * Returns the scope of the attribute, or {@code null} if the attribute doesn't have a scope.
	 */
	public char[] getScope();

	/**
	 * Returns true if this attribute has a pack expansion.
	 */
	public boolean hasPackExpansion();

	@Override
	public ICPPASTAttribute copy();

	@Override
	public ICPPASTAttribute copy(CopyStyle style);
}
