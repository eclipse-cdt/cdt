/*******************************************************************************
 * Copyright (c) 2012, 2014 Google, Inc and others.
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
 * 	   Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTToken;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAttribute;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttribute;

/**
 * C++-specific attribute.
 */
public class CPPASTAttribute extends ASTAttribute implements ICPPASTAttribute {
	private final char[] scope;
	private final boolean packExpansion;

	public CPPASTAttribute(char[] name, char[] scope, IASTToken argumentsClause, boolean packExpansion) {
		super(name, argumentsClause);
		this.scope = scope;
		this.packExpansion = packExpansion;
	}

	@Override
	public CPPASTAttribute copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTAttribute copy(CopyStyle style) {
		IASTToken argumentClause = getArgumentClause();
		if (argumentClause != null)
			argumentClause = argumentClause.copy(style);
		return copy(new CPPASTAttribute(getName(), getScope(), argumentClause, hasPackExpansion()), style);
	}

	@Override
	public char[] getScope() {
		return scope;
	}

	@Override
	public boolean hasPackExpansion() {
		return packExpansion;
	}
}
