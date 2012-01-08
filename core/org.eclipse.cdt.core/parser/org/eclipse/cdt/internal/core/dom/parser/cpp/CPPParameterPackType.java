/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.core.runtime.CoreException;

public class CPPParameterPackType implements ICPPParameterPackType, ITypeContainer, ISerializableType {
    private IType fType = null;
    
    public CPPParameterPackType(IType type) {
    	setType(type);
    }

    @Override
	public IType getType() {
        return fType;
    }
    
    @Override
	public void setType(IType t) {
    	assert t != null;
    	fType= t;
    }

    @Override
	public boolean isSameType(IType obj) {
        if (obj == this)
            return true;
        if (obj instanceof ITypedef)
            return ((ITypedef)obj).isSameType(this);
        
        if (obj instanceof ICPPParameterPackType) {
            final ICPPParameterPackType rhs = (ICPPParameterPackType) obj;
            IType t1= getType();
            IType t2= rhs.getType();
            return t1 != null && t1.isSameType(t2);
        }
    	return false;
    }
    
    @Override
	public Object clone() {
   		try {
   			return super.clone();
        } catch (CloneNotSupportedException e) {
            // not going to happen
        	return null;
        }
    }
    
	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		int firstByte= ITypeMarshalBuffer.PACK_EXPANSION;
		buffer.putByte((byte) firstByte);
		buffer.marshalType(getType());
	}
	
	public static IType unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		IType nested= buffer.unmarshalType();
		if (nested == null)
			return new ProblemBinding(null, IProblemBinding.SEMANTIC_INVALID_TYPE);
		
		return new CPPParameterPackType(nested);
	}
}
