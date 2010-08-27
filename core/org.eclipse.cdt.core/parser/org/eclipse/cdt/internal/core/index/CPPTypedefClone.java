/*******************************************************************************
 * Copyright (c) 2007, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bryan Wilkinson (QNX) - Initial API and implementation
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
import org.eclipse.core.runtime.CoreException;

public class CPPTypedefClone implements ITypedef, ITypeContainer, IIndexType, ICPPBinding {
	protected final ITypedef delegate;
	private IType type;

	public CPPTypedefClone(ITypedef typedef) {
		this.delegate = typedef;
	}

	public IType getType() {
		if (type == null) {
			return delegate.getType();
		}
		return type;
	}

	public ILinkage getLinkage() throws CoreException {
		return delegate.getLinkage();
	}

	public String getName() {
		return delegate.getName();
	}

	public char[] getNameCharArray() {
		return delegate.getNameCharArray();
	}

	public IScope getScope() throws DOMException {
		return delegate.getScope();
	}

	public IBinding getOwner() {
		return delegate.getOwner();
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return delegate.getAdapter(adapter);
	}

	public boolean isSameType(IType type) {
		IType myrtype = getType();
		if (myrtype == null)
			return false;

		if (type instanceof ITypedef) {
			type= ((ITypedef) type).getType();
		}
		return myrtype.isSameType(type);
	}

	public void setType(IType type) {
		this.type = type;
	}

	public String[] getQualifiedName() throws DOMException {
		return ((ICPPBinding) delegate).getQualifiedName();
	}

	public char[][] getQualifiedNameCharArray() throws DOMException {
		return ((ICPPBinding) delegate).getQualifiedNameCharArray();
	}

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
