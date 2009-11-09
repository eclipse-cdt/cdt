/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

public class CPPReferenceType implements ICPPReferenceType, ITypeContainer, ISerializableType {
    IType type = null;
    
    public CPPReferenceType(IType type) {
        this.type = type;
    }

    public IType getType() {
        return type;
    }
    
    public void setType(IType t) {
        type = t;
    }

    public boolean isSameType(IType obj) {
        if (obj == this)
            return true;
        if (obj instanceof ITypedef)
            return ((ITypedef)obj).isSameType(this);
        
        if (type == null)
            return false;
        
        if (obj instanceof ICPPReferenceType) {
            return type.isSameType(((ICPPReferenceType) obj).getType());
        }
    	return false;
    }
    
    @Override
	public Object clone() {
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch (CloneNotSupportedException e) {
            // not going to happen
        }
        return t;
    }
    
	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}

	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		int firstByte= ITypeMarshalBuffer.REFERENCE;
		buffer.putByte((byte) firstByte);
		buffer.marshalType(getType());
	}
	
	public static IType unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		IType nested= buffer.unmarshalType();
		return new CPPReferenceType(nested);
	}
}
