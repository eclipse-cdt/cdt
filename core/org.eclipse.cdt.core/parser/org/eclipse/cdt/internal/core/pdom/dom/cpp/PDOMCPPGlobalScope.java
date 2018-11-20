/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMGlobalScope;

/**
 * Represents the global C++ index scope.
 */
public class PDOMCPPGlobalScope extends PDOMGlobalScope implements ICPPNamespaceScope {
	public static final PDOMCPPGlobalScope INSTANCE = new PDOMCPPGlobalScope();

	private PDOMCPPGlobalScope() {
	}

	@Override
	public void addUsingDirective(ICPPUsingDirective usingDirective) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ICPPUsingDirective[] getUsingDirectives() {
		return ICPPUsingDirective.EMPTY_ARRAY;
	}

	@Override
	public ICPPNamespaceScope[] getInlineNamespaces() {
		return ICPPNamespaceScope.EMPTY_NAMESPACE_SCOPE_ARRAY;
	}
}
