/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDelegateCreator;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPVariable extends CompositeCPPBinding implements ICPPVariable, ICPPDelegateCreator {

	public CompositeCPPVariable(ICompositesFactory cf, ICPPVariable delegate) {
		super(cf, delegate);
	}
	
	public boolean isMutable() throws DOMException {
		return ((ICPPVariable)rbinding).isMutable();
	}

	public IType getType() throws DOMException {
		IType rtype = ((ICPPVariable)rbinding).getType();
		return cf.getCompositeType((IIndexType)rtype);
	}

	public boolean isAuto() throws DOMException {
		return ((ICPPVariable)rbinding).isAuto();
	}

	public boolean isExtern() throws DOMException {
		return ((ICPPVariable)rbinding).isExtern();
	}

	public boolean isRegister() throws DOMException {
		return ((ICPPVariable)rbinding).isRegister();
	}

	public boolean isStatic() throws DOMException {
		return ((ICPPVariable)rbinding).isStatic();
	}
	
	public ICPPDelegate createDelegate(IASTName name) {
		return new CPPVariable.CPPVariableDelegate(name, this);
	}
}
