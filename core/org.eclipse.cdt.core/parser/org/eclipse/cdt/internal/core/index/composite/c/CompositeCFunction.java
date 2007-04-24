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
package org.eclipse.cdt.internal.core.index.composite.c;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCFunction extends CompositeCBinding implements IIndexBinding, IFunction {

	public CompositeCFunction(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		super(cf, rbinding);
	}

	public IScope getFunctionScope() throws DOMException {
		IScope scope= ((IFunction)rbinding).getFunctionScope();
		return cf.getCompositeScope((IIndexScope)scope);
	}
	
	public IParameter[] getParameters() throws DOMException {
		IParameter[] preResult = ((IFunction)rbinding).getParameters();
		IParameter[] result = new IParameter[preResult.length];
		for(int i=0; i<preResult.length; i++) {
			result[i] = (IParameter) cf.getCompositeBinding((IIndexFragmentBinding) preResult[i]);
		}
		return result;
	}
	
	public IFunctionType getType() throws DOMException {
		IType rtype = ((IFunction)rbinding).getType();
		return (IFunctionType) cf.getCompositeType((IIndexType)rtype);
	}

	public boolean isAuto() throws DOMException {
		return ((IFunction)rbinding).isAuto();
	}

	public boolean isExtern() throws DOMException {
		return ((IFunction)rbinding).isExtern();
	}

	public boolean isInline() throws DOMException {
		return ((IFunction)rbinding).isInline();
	}

	public boolean isRegister() throws DOMException {
		return ((IFunction)rbinding).isRegister();
	}

	public boolean isStatic() throws DOMException {
		return ((IFunction)rbinding).isStatic();
	}

	public boolean takesVarArgs() throws DOMException {
		return ((IFunction)rbinding).takesVarArgs();
	}
}
