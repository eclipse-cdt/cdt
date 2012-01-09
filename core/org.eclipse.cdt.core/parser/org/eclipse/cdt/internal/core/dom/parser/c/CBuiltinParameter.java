/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.core.runtime.PlatformObject;

public class CBuiltinParameter extends PlatformObject implements IParameter {
	
	public static IParameter[] createParameterList(IFunctionType ft) {
		if (ft == null) {
			return IParameter.EMPTY_PARAMETER_ARRAY;
		}
		assert !(ft instanceof ICPPFunctionType);
		IType[] ptypes= ft.getParameterTypes();
		IParameter[] result= new IParameter[ptypes.length];
		for (int i = 0; i < result.length; i++) {
			result[i]= new CBuiltinParameter(ptypes[i]);
		}
		return result;
	}

	private IType type= null;
	
	public CBuiltinParameter(IType type) {
		this.type = type;
	}
	
	@Override
	public IType getType() {
		return type;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public boolean isExtern() {
		return false;
	}

	@Override
	public boolean isAuto() {
		return false;
	}

	@Override
	public boolean isRegister() {
		return false;
	}

	@Override
	public String getName() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public char[] getNameCharArray() {
		return CharArrayUtils.EMPTY;
	}

	@Override
	public IScope getScope() {
		return null;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}
	
	@Override
	public IBinding getOwner() {
		return null;
	}

	@Override
	public IValue getInitialValue() {
		return null;
	}
}