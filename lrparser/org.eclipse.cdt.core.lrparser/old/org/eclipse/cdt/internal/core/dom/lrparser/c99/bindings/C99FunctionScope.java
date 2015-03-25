/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;

public class C99FunctionScope extends C99Scope implements ICFunctionScope {

	public C99FunctionScope() {
		super(EScopeKind.eLocal);
	}

	/**
	 * Scope that represents the compound statement of the body of this scope.
	 * Does not include the parameters which are part of this function scope.
	 */
	private IScope bodyScope;
	
	
	@Override
	public IBinding getBinding(@SuppressWarnings("unused") char[] name) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setBodyScope(IScope bodyScope) {
		this.bodyScope = bodyScope; 
	}
	
	@Override
	public IScope getBodyScope() throws DOMException {
		return bodyScope;
	}
}
