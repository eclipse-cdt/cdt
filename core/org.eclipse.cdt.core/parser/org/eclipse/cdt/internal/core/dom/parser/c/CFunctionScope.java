/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Niefer (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;

public class CFunctionScope extends CScope implements ICFunctionScope {
	public CFunctionScope(IASTFunctionDefinition function) {
		super(function, EScopeKind.eLocal);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICFunctionScope#getBinding(char[])
	 */
	@Override
	public IBinding getBinding(char[] name) {
		return super.getBinding(NAMESPACE_TYPE_OTHER, name);
	}

	@Override
	public IScope getBodyScope() {
		IASTNode node = getPhysicalNode();
		IASTStatement statement = ((IASTFunctionDefinition) node).getBody();
		if (statement instanceof IASTCompoundStatement) {
			return ((IASTCompoundStatement) statement).getScope();
		}
		return null;
	}
}
