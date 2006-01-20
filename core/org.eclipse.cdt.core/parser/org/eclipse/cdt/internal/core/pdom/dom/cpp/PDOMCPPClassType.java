/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMember;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMCPPClassType extends PDOMMemberOwner implements ICPPClassType, ICPPClassScope {

	protected static final int RECORD_SIZE = PDOMMemberOwner.RECORD_SIZE + 0;
	
	public PDOMCPPClassType(PDOMDatabase pdom, PDOMNode parent, IASTName name) throws CoreException {
		super(pdom, parent, name, PDOMCPPLinkage.CPPCLASSTYPE);
	}

	public PDOMCPPClassType(PDOMDatabase pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public boolean isSameType(IType type) {
		if (type instanceof PDOMBinding)
			return record == ((PDOMBinding)type).getRecord();
		else
			// TODO - should we check for real?
			return false;
	}

	public Object clone() {
		throw new PDOMNotImplementedError();
	}

	public IField findField(String name) throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public ICPPBase[] getBases() throws DOMException {
		// TODO
		return new ICPPBase[0];
	}

	public ICPPConstructor[] getConstructors() throws DOMException {
		// TODO
		return new ICPPConstructor[0];
	}

	public ICPPField[] getDeclaredFields() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public ICPPMethod[] getDeclaredMethods() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IField[] getFields() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IBinding[] getFriends() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public ICPPMethod[] getMethods() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public ICPPClassType[] getNestedClasses() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IScope getCompositeScope() throws DOMException {
		return this;
	}

	public int getKey() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public String[] getQualifiedName() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public char[][] getQualifiedNameCharArray() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isGloballyQualified() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public ICPPClassType getClassType() {
		return null;
		// TODO - do we need the real type?
		//throw new PDOMNotImplementedError();
	}

	public ICPPMethod[] getImplicitMethods() {
		throw new PDOMNotImplementedError();
	}

	public void addBinding(IBinding binding) throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public void addName(IASTName name) throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IBinding[] find(String name) throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public void flushCache() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IBinding getBinding(IASTName name, boolean resolve) throws DOMException {
		try {
			PDOMMember[] matches = findMembers(name.toCharArray());
			// TODO - need to check for overloads
			return matches.length > 0 ? matches[0] : null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public IScope getParent() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IASTNode getPhysicalNode() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IASTName getScopeName() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isFullyCached() throws DOMException {
		return true;
	}

	public void removeBinding(IBinding binding) throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public void setFullyCached(boolean b) throws DOMException {
		throw new PDOMNotImplementedError();
	}
	
}
