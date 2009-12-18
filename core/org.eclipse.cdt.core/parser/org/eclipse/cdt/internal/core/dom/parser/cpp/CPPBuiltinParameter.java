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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.core.runtime.PlatformObject;

public class CPPBuiltinParameter extends PlatformObject implements ICPPParameter {
	public static ICPPParameter[] createParameterList(ICPPFunctionType ft) {
		if (ft == null) {
			return ICPPParameter.EMPTY_CPPPARAMETER_ARRAY;
		}
		IType[] ptypes= ft.getParameterTypes();
		ICPPParameter[] result= new ICPPParameter[ptypes.length];
		for (int i = 0; i < result.length; i++) {
			result[i]= new CPPBuiltinParameter(ptypes[i]);
		}
		return result;
	}

    private IType type= null;

    public CPPBuiltinParameter(IType type) {
        this.type = type;
    }

    public IType getType() {
        return type;
    }

    public boolean isStatic() {
        return false;
    }

    public boolean isExtern() {
        return false;
    }

	public boolean isExternC() {
		return false;
	}

    public boolean isAuto() {
        return false;
    }

    public boolean isRegister() {
        return false;
    }

    public String getName() {
        return ""; //$NON-NLS-1$
    }

    public char[] getNameCharArray() {
    	return CharArrayUtils.EMPTY;
    }

    public IScope getScope() {
        return null;
    }

    public boolean hasDefaultValue() {
        return false;
    }

    public boolean isMutable() {
        return false;
    }

    public String[] getQualifiedName() {
        return new String[0];
    }

    public char[][] getQualifiedNameCharArray() {
        return new char[0][];
    }

    public boolean isGloballyQualified() {
        return false;
    }

	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	public IBinding getOwner() {
		return null;
	}

	public IValue getInitialValue() {
		return null;
	}

	public boolean isParameterPack() {
		return false;
	}
}