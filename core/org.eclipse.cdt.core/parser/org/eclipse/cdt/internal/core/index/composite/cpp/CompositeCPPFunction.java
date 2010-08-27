/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPFunction extends CompositeCPPBinding implements ICPPFunction {

	public CompositeCPPFunction(ICompositesFactory cf, ICPPFunction rbinding) {
		super(cf, rbinding);
	}

	public boolean isExternC() {
		return ((ICPPFunction)rbinding).isExternC();
	}

	public boolean isInline() {
		return ((ICPPFunction)rbinding).isInline();
	}

	public boolean isMutable() {
		return ((ICPPFunction)rbinding).isMutable();
	}

	public IScope getFunctionScope() throws DOMException {
		fail(); return null;
	}

	public ICPPParameter[] getParameters() throws DOMException {
		ICPPParameter[] result = ((ICPPFunction)rbinding).getParameters();
		for(int i=0; i<result.length; i++) {
			result[i] = (ICPPParameter) cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
		}
		return result;
	}

	public ICPPFunctionType getType() throws DOMException {
		IType rtype = ((ICPPFunction)rbinding).getType();
		return (ICPPFunctionType) cf.getCompositeType(rtype);
	}

	public boolean isDeleted() {
		return ((ICPPFunction)rbinding).isDeleted();
	}

	public boolean isAuto() {
		return ((ICPPFunction)rbinding).isAuto();
	}

	public boolean isExtern() {
		return ((ICPPFunction)rbinding).isExtern();
	}

	public boolean isRegister() {
		return ((ICPPFunction)rbinding).isRegister();
	}

	public boolean isStatic() {
		return ((ICPPFunction)rbinding).isStatic();
	}

	public boolean takesVarArgs() {
		return ((ICPPFunction)rbinding).takesVarArgs();
	}
	
	public int getRequiredArgumentCount() throws DOMException {
		return ((ICPPFunction)rbinding).getRequiredArgumentCount();
	}

	public boolean hasParameterPack() {
		return ((ICPPFunction)rbinding).hasParameterPack();
	}

	@Override
	public Object clone() {
		fail(); return null;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		try {
			result.append(getName()+" "+ASTTypeUtil.getParameterTypeString(getType())); //$NON-NLS-1$
		} catch(DOMException de) {
			result.append(de);
		}
		return result.toString();
	}

	public IType[] getExceptionSpecification() {
		IType[] es= ((ICPPFunction)rbinding).getExceptionSpecification();
		if (es == null || es.length == 0)
			return es;
		
		IType[] result= new IType[es.length];
		for (int i = 0; i < result.length; i++) {
			result[i]= cf.getCompositeType(es[i]);
		}
		return result;
	}
}
