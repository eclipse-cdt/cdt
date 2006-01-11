/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
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
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMember;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMCPPMethod extends PDOMMember implements ICPPMethod {

	public PDOMCPPMethod(PDOMDatabase pdom, PDOMMemberOwner parent, IASTName name) throws CoreException {
		super(pdom, parent, name, PDOMCPPLinkage.CPPMETHOD);
	}

	public PDOMCPPMethod(PDOMDatabase pdom, int record) {
		super(pdom, record);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public boolean isVirtual() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isDestructor() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isMutable() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isInline() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IParameter[] getParameters() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IScope getFunctionScope() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IFunctionType getType() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isStatic() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isExtern() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isAuto() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isRegister() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean takesVarArgs() throws DOMException {
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

	public int getVisibility() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public ICPPClassType getClassOwner() throws DOMException {
		try {
			return (ICPPClassType)getMemberOwner();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

}
