/*******************************************************************************
 * Copyright (c) 2010, 2014 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;

/**
 * Implementation of namespace scopes, including global scope.
 */
public class CPPEnumScope extends CPPScope implements ICPPEnumScope {
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

	@Override
	public ICPPEnumeration getEnumerationType() {
		ICPPASTEnumerationSpecifier node = (ICPPASTEnumerationSpecifier) getPhysicalNode();
		final IASTName name = node.getName();
		IBinding binding = name.resolveBinding();
		if (binding instanceof ICPPEnumeration)
			return (ICPPEnumeration) binding;

		return new CPPEnumeration.CPPEnumerationProblem(name, ISemanticProblem.BINDING_NO_CLASS, name.toCharArray());
	}
}
