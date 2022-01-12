/*******************************************************************************
 * Copyright (c) 2010, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;

/**
 * For namespace scopes from the AST or mapped index namespace scopes.
 */
public interface ICPPInternalNamespaceScope extends ICPPNamespaceScope {
	public ICPPInternalNamespaceScope[] EMPTY_NAMESPACE_SCOPE_ARRAY = {};

	/**
	 * Returns the enclosing namespace set (7.3.1-9)
	 */
	public ICPPNamespaceScope[] getEnclosingNamespaceSet();

	/**
	 * Returns whether this namespace scope is inline.
	 */
	public boolean isInlineNamepace();

	/**
	 * Returns the inline namespace scopes mapped back to the AST.
	 */
	@Override
	public ICPPInternalNamespaceScope[] getInlineNamespaces();
}
