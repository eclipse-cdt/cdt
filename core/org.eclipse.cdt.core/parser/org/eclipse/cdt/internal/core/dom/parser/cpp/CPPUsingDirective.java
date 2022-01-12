/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems, Inc. and others.
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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Represents a using-directive found in the AST.
 */
public class CPPUsingDirective implements ICPPUsingDirective {
	private IASTName fNamespaceName;

	/**
	 * Constructor for explicit using directives
	 */
	public CPPUsingDirective(ICPPASTUsingDirective node) {
		fNamespaceName = node.getQualifiedName();
	}

	/**
	 * Constructor for unnamed namespaces introducing an implicit using directive.
	 */
	public CPPUsingDirective(ICPPASTNamespaceDefinition nsdef) {
		fNamespaceName = nsdef.getName();
	}

	@Override
	public ICPPNamespaceScope getNominatedScope() throws DOMException {
		IBinding binding = fNamespaceName.resolveBinding();
		if (binding instanceof ICPPNamespace) {
			return ((ICPPNamespace) binding).getNamespaceScope();
		}
		return null;
	}

	@Override
	public int getPointOfDeclaration() {
		final ASTNode astNode = (ASTNode) fNamespaceName;
		return astNode.getOffset() + astNode.getLength();
	}

	@Override
	public IScope getContainingScope() {
		return CPPVisitor.getContainingScope(fNamespaceName);
	}

	@Override
	public String toString() {
		return "using namespace " + fNamespaceName.toString(); //$NON-NLS-1$
	}
}
