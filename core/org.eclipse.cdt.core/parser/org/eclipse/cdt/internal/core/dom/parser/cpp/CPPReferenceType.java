/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
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
    private IType fType = null;
    private boolean fIsRValue;
    
    public CPPReferenceType(IType type, boolean isRValue) {
    	fIsRValue= isRValue;
    	setType(type);
    }

    @Override
	public IType getType() {
        return fType;
    }
    
	@Override
	public boolean isRValueReference() {
		return fIsRValue;
	}

    @Override
	public void setType(IType t) {
    	if (t instanceof ICPPReferenceType) {
    		final ICPPReferenceType rt = (ICPPReferenceType) t;
			fIsRValue = fIsRValue && rt.isRValueReference();
    		t= rt.getType();
    	} 
    	assert t != null;
    	fType= t;
    }

    @Override
	public boolean isSameType(IType obj) {
        if (obj == this)
            return true;
        if (obj instanceof ITypedef)
            return ((ITypedef)obj).isSameType(this);
        
        if (obj instanceof ICPPReferenceType) {
            final ICPPReferenceType rhs = (ICPPReferenceType) obj;
            IType t1= getType();
            IType t2= rhs.getType();
            boolean rv1= isRValueReference();
            boolean rv2= rhs.isRValueReference();
            for(;;) {
            	if (t1 instanceof ITypedef) {
            		t1= ((ITypedef) t1).getType();
            	} else if (t1 instanceof ICPPReferenceType) {
            		rv1= rv1 && ((ICPPReferenceType) t1).isRValueReference();
            		t1= ((ICPPReferenceType) t1).getType();
            	} else {
            		break;
            	}
            }
            for(;;) {
            	if (t2 instanceof ITypedef) {
            		t2= ((ITypedef) t2).getType();
            	} else if (t2 instanceof ICPPReferenceType) {
            		rv2= rv2 && ((ICPPReferenceType) t2).isRValueReference();
            		t2= ((ICPPReferenceType) t2).getType();
            	} else {
            		break;
            	}
            }
            if (t1 == null)
            	return false;
            
			return rv1 == rv2 && t1.isSameType(t2);
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

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		int firstByte= ITypeMarshalBuffer.REFERENCE;
		if (isRValueReference()) {
			firstByte |= ITypeMarshalBuffer.FLAG1;
		}
		buffer.putByte((byte) firstByte);
		buffer.marshalType(getType());
	}
	
	public static IType unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		IType nested= buffer.unmarshalType();
		return new CPPReferenceType(nested, (firstByte & ITypeMarshalBuffer.FLAG1) != 0);
	}
}
