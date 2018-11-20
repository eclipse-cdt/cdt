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
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICPPASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

public class CPPASTUsingDeclaration extends CPPASTAttributeOwner
		implements ICPPASTUsingDeclaration, ICPPASTCompletionContext {
	private boolean typeName;
	private IASTName name;

	// The using-declaration has an implicit name referencing every delegate binding.
	private IASTImplicitName[] fImplicitNames;

	public CPPASTUsingDeclaration() {
	}

	public CPPASTUsingDeclaration(IASTName name) {
		setName(name);
	}

	@Override
	public CPPASTUsingDeclaration copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTUsingDeclaration copy(CopyStyle style) {
		CPPASTUsingDeclaration copy = new CPPASTUsingDeclaration(name == null ? null : name.copy(style));
		copy.typeName = typeName;
		return copy(copy, style);
	}

	@Override
	public void setIsTypename(boolean value) {
		assertNotFrozen();
		this.typeName = value;
	}

	@Override
	public boolean isTypename() {
		return typeName;
	}

	@Override
	public IASTName getName() {
		return name;
	}

	@Override
	public void setName(IASTName name) {
		assertNotFrozen();
		this.name = name;
		if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(NAME);
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

		if (!acceptByAttributeSpecifiers(action))
			return false;
		if (name != null && !name.accept(action))
			return false;

		if (action.shouldVisitImplicitNames) {
			for (IASTImplicitName name : getImplicitNames()) {
				if (!name.accept(action))
					return false;
			}
		}

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
			return r_declaration;
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
	public String toString() {
		return name.toString();
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return findBindings(n, isPrefix, null);
	}

	@Override
	public IASTImplicitName[] getImplicitNames() {
		if (fImplicitNames == null) {
			fImplicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			IBinding usingDecl = name.resolveBinding();
			if (usingDecl instanceof ICPPUsingDeclaration) {
				IBinding[] delegates = ((ICPPUsingDeclaration) usingDecl).getDelegates();
				if (delegates.length > 0) {
					fImplicitNames = new IASTImplicitName[delegates.length];
					for (int i = 0; i < delegates.length; ++i) {
						CPPASTImplicitName reference = new CPPASTImplicitName(name.getSimpleID(), this);
						reference.setBinding(delegates[i]);
						reference.setOffsetAndLength((ASTNode) name.getLastName());
						fImplicitNames[i] = reference;
					}
				}
			}
		}
		return fImplicitNames;
	}
}
