/*******************************************************************************
 * Copyright (c) 2007, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bryan Wilkinson (QNX) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * Delegating clone implementation for index classes implementing {@link ITypedef} interface.
 */
public class CPPTypedefClone implements ITypedef, ITypeContainer, IIndexType, ICPPBinding {
	protected final ITypedef delegate;
	private IType type;

	public CPPTypedefClone(ITypedef typedef) {
		this.delegate = typedef;
	}

	@Override
	public IType getType() {
		if (type == null) {
			return delegate.getType();
		}
		return type;
	}

	@Override
	public ILinkage getLinkage() {
		return delegate.getLinkage();
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public char[] getNameCharArray() {
		return delegate.getNameCharArray();
	}

	@Override
	public IScope getScope() throws DOMException {
		return delegate.getScope();
	}

	@Override
	public IBinding getOwner() {
		return delegate.getOwner();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return delegate.getAdapter(adapter);
	}

	@Override
	public boolean isSameType(IType type) {
		IType myrtype = getType();
		if (myrtype == null)
			return false;

		if (type instanceof ITypedef) {
			type = ((ITypedef) type).getType();
		}
		return myrtype.isSameType(type);
	}

	@Override
	public void setType(IType type) {
		this.type = type;
	}

	@Override
	public String[] getQualifiedName() throws DOMException {
		return ((ICPPBinding) delegate).getQualifiedName();
	}

	@Override
	public char[][] getQualifiedNameCharArray() throws DOMException {
		return ((ICPPBinding) delegate).getQualifiedNameCharArray();
	}

	@Override
	public boolean isGloballyQualified() throws DOMException {
		return ((ICPPBinding) delegate).isGloballyQualified();
	}

	@Override
	public Object clone() {
		return new CPPTypedefClone(this);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
