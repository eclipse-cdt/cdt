/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Bryan Wilkinson (QNX)
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICPPASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

public class CPPASTUsingDirective extends CPPASTAttributeOwner
		implements ICPPASTUsingDirective, ICPPASTCompletionContext {
	private IASTName name;

	public CPPASTUsingDirective() {
	}

	public CPPASTUsingDirective(IASTName name) {
		setQualifiedName(name);
	}

	@Override
	public CPPASTUsingDirective copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTUsingDirective copy(CopyStyle style) {
		CPPASTUsingDirective copy = new CPPASTUsingDirective(name == null ? null : name.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTName getQualifiedName() {
		return name;
	}

	@Override
	public void setQualifiedName(IASTName qualifiedName) {
		assertNotFrozen();
		this.name = qualifiedName;
		if (qualifiedName != null) {
			qualifiedName.setParent(this);
			qualifiedName.setPropertyInParent(QUALIFIED_NAME);
		}
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclarations) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (!acceptByCPPAttributeSpecifiers(action))
			return false;
		if (name != null && !name.accept(action))
			return false;
		if (!acceptByGCCAttributeSpecifiers(action))
			return false;

		if (action.shouldVisitDeclarations) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		return true;
	}

	@Override
	public int getRoleForName(IASTName n) {
		if (n == name)
			return r_reference;
		return r_unclear;
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix, String[] namespaces) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix, namespaces);

		int j = 0;
		for (int i = 0; i < bindings.length; i++) {
			IBinding binding = bindings[i];
			if (binding instanceof ICPPNamespace) {
				if (i != j)
					bindings[j] = binding;
				j++;
			}
		}

		if (j < bindings.length)
			return Arrays.copyOfRange(bindings, 0, j);
		return bindings;
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return findBindings(n, isPrefix, null);
	}
}
