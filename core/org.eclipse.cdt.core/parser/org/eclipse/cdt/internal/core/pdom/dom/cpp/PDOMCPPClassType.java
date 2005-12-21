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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMCPPClassType extends PDOMMemberOwner implements ICPPClassType {

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
		throw new PDOMNotImplementedError();
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
		throw new PDOMNotImplementedError();
	}

	public ICPPConstructor[] getConstructors() throws DOMException {
		throw new PDOMNotImplementedError();
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
		throw new PDOMNotImplementedError();
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

}
