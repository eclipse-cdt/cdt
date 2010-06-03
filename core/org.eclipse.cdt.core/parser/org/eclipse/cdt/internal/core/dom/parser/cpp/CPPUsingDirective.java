/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
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
		fNamespaceName= node.getQualifiedName();
	}

	/**
	 * Constructor for unnamed namespaces introducing an implicit using directive.
	 */
	public CPPUsingDirective(ICPPASTNamespaceDefinition nsdef) {
		fNamespaceName= nsdef.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective#getNamespaceScope()
	 */
	public ICPPNamespaceScope getNominatedScope() throws DOMException {
		IBinding binding= fNamespaceName.resolveBinding();
		if (binding instanceof ICPPNamespace) {
			return ((ICPPNamespace) binding).getNamespaceScope();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective#getPointOfDeclaration()
	 */
	public int getPointOfDeclaration() {
		final ASTNode astNode = (ASTNode) fNamespaceName;
		return astNode.getOffset() + astNode.getLength();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective#getContainingScope()
	 */
	public IScope getContainingScope() {
		return CPPVisitor.getContainingScope(fNamespaceName);
	}

	@Override
	public String toString() {
		return fNamespaceName.toString();
	}
}
