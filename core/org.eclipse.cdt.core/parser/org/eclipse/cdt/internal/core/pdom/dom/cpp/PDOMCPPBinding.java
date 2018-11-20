/*******************************************************************************
 * Copyright (c) 2006, 2015 Symbian Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Symbian - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Mirrors type-hierarchy from DOM interfaces
 */
public abstract class PDOMCPPBinding extends PDOMBinding implements ICPPBinding {
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 0;

	public PDOMCPPBinding(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	public PDOMCPPBinding(PDOMLinkage linkage, PDOMNode parent, char[] name) throws CoreException {
		super(linkage, parent, name);
	}

	@Override
	final public String[] getQualifiedName() {
		return CPPVisitor.getQualifiedName(this);
	}

	@Override
	final public char[][] getQualifiedNameCharArray() {
		return CPPVisitor.getQualifiedNameCharArray(this);
	}

	@Override
	public final boolean isGloballyQualified() throws DOMException {
		// Local stuff is not stored in the index.
		return true;
	}

	@Override
	public final IIndexScope getScope() {
		// The parent node in the binding hierarchy is the scope.
		try {
			IBinding parent = getParentBinding();
			while (parent != null) {
				if (parent instanceof ICPPClassType) {
					return (IIndexScope) ((ICPPClassType) parent).getCompositeScope();
				} else if (parent instanceof ICPPUnknownBinding) {
					return (IIndexScope) ((ICPPUnknownBinding) parent).asScope();
				} else if (parent instanceof ICPPEnumeration) {
					final ICPPEnumeration enumeration = (ICPPEnumeration) parent;
					if (enumeration.isScoped()) {
						return (IIndexScope) enumeration.asScope();
					}
					parent = ((PDOMNamedNode) parent).getParentBinding();
				} else if (parent instanceof IIndexScope) {
					return (IIndexScope) parent;
				} else {
					break;
				}
			}
		} catch (DOMException e) {
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return getLinkage().getGlobalScope();
	}
}
