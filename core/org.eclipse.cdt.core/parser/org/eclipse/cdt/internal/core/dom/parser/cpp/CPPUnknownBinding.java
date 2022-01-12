/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Represents a binding that is unknown because it depends on template arguments.
 */
public abstract class CPPUnknownBinding extends PlatformObject
		implements ICPPUnknownBinding, ICPPInternalBinding, Cloneable {
	private ICPPScope unknownScope;
	protected char[] name;

	public CPPUnknownBinding(char[] name) {
		super();
		this.name = name;
	}

	@Override
	public IASTNode[] getDeclarations() {
		return null;
	}

	@Override
	public IASTNode getDefinition() {
		return null;
	}

	@Override
	public void addDefinition(IASTNode node) {
	}

	@Override
	public void addDeclaration(IASTNode node) {
	}

	@Override
	public String[] getQualifiedName() {
		return CPPVisitor.getQualifiedName(this);
	}

	@Override
	public char[][] getQualifiedNameCharArray() {
		return CPPVisitor.getQualifiedNameCharArray(this);
	}

	@Override
	public boolean isGloballyQualified() {
		return false;
	}

	@Override
	public final String getName() {
		return new String(getNameCharArray());
	}

	@Override
	public char[] getNameCharArray() {
		return name;
	}

	@Override
	public IScope getScope() throws DOMException {
		// Use getOwner(), it is overridden by derived classes.
		final IBinding owner = getOwner();
		if (owner instanceof ICPPUnknownBinding) {
			return ((ICPPUnknownBinding) owner).asScope();
		} else if (owner instanceof ICPPClassType) {
			return ((ICPPClassType) owner).getCompositeScope();
		} else if (owner instanceof ICPPNamespace) {
			return ((ICPPNamespace) owner).getNamespaceScope();
		} else if (owner instanceof ICPPFunction) {
			return ((ICPPFunction) owner).getFunctionScope();
		}
		return null;
	}

	@Override
	public ICPPScope asScope() {
		if (unknownScope == null && this instanceof ICPPUnknownType) {
			unknownScope = createScope();
		}
		return unknownScope;
	}

	protected CPPUnknownTypeScope createScope() {
		return new CPPUnknownTypeScope((ICPPUnknownType) this, new CPPASTName(name));
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	public CPPUnknownBinding clone() {
		try {
			return (CPPUnknownBinding) super.clone();
		} catch (CloneNotSupportedException e) {
			return null; // Never happens
		}
	}

	@Override
	public String toString() {
		return getName();
	}
}
