/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 */
public class CPPTypedefClone implements ITypedef, ITypeContainer, IIndexType, ICPPBinding {
	protected final ITypedef delegate;
	private IType type = null;

	public CPPTypedefClone(ITypedef typedef) {
		this.delegate = typedef;
	}
	public IType getType() throws DOMException {
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
	public Object getAdapter(Class adapter) {
		return delegate.getAdapter(adapter);
	}
	public boolean isSameType(IType type) {
		try {
			IType myrtype = getType();
			if (myrtype == null)
				return false;

			if (type instanceof ITypedef) {
				type= ((ITypedef)type).getType();
			}
			return myrtype.isSameType(type);
		} catch (DOMException e) {
		}
		return false;
	}
	public void setType(IType type) {
		this.type = type;
	}
	public String[] getQualifiedName() throws DOMException {
		return ((ICPPBinding)delegate).getQualifiedName();
	}
	public char[][] getQualifiedNameCharArray() throws DOMException {
		return ((ICPPBinding)delegate).getQualifiedNameCharArray();
	}
	public boolean isGloballyQualified() throws DOMException {
		return ((ICPPBinding)delegate).isGloballyQualified();
	}
	public Object clone() {
		return new CPPTypedefClone(this);
	}
}
