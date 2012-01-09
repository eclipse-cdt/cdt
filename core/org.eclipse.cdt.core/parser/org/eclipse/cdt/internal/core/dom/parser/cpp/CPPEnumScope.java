/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;

/**
 * Implementation of namespace scopes, including global scope.
 */
public class CPPEnumScope extends CPPScope {
	public CPPEnumScope(ICPPASTEnumerationSpecifier specifier) {
		super(specifier);
	}

	@Override
	public EScopeKind getKind() {
		return EScopeKind.eNamespace;
	}

	@Override
	public IName getScopeName() {
		ICPPASTEnumerationSpecifier node = (ICPPASTEnumerationSpecifier) getPhysicalNode();
		return node.getName();
    }
}
