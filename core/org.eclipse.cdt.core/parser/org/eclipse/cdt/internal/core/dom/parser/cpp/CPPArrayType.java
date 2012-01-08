/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.core.runtime.CoreException;

public class CPPArrayType implements IArrayType, ITypeContainer, ISerializableType {
    private IType type;
    private IASTExpression sizeExpression;
    private IValue value= Value.NOT_INITIALIZED;

    public CPPArrayType(IType type, IValue value) {
    	this.value= value;
    	setType(type);
    }

    public CPPArrayType(IType type, IASTExpression sizeExp) {
    	this.sizeExpression = sizeExp;
    	setType(type);
    }
    
    @Override
	public IType getType() {
        return type;
    }
    
    @Override
	public void setType(IType t) {
    	assert t != null;
        this.type = t;
    }
    
    @Override
	public boolean isSameType(IType obj) {
        if (obj == this)
            return true;
        if (obj instanceof ITypedef)
            return ((ITypedef) obj).isSameType(this);
        
        if (obj instanceof IArrayType) {
            final IArrayType rhs = (IArrayType) obj;
			IType objType = rhs.getType();
			if (objType != null) {
				if (objType.isSameType(type)) {
					IValue s1= getSize();
					IValue s2= rhs.getSize();
					if (s1 == s2)
						return true;
					if (s1 == null || s2 == null)
						return false;
					return CharArrayUtils.equals(s1.getSignature(), s2.getSignature());
				}
			}
        }
    	return false;
    }

    @Override
	public IValue getSize() {
    	if (value != Value.NOT_INITIALIZED)
    		return value;
    	
    	if (sizeExpression == null)
    		return value= null;

    	return value= Value.create(sizeExpression, Value.MAX_RECURSION_DEPTH);
    }
    
    @Override
	@Deprecated
    public IASTExpression getArraySizeExpression() {
        return sizeExpression;
    }

    @Override
	public Object clone() {
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch (CloneNotSupportedException e) {
            // Not going to happen
        }
        return t;
    }

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		final byte firstByte = ITypeMarshalBuffer.ARRAY;

		IValue val= getSize();
		if (val == null) {
			buffer.putByte(firstByte);
			buffer.marshalType(getType());
			return;
		} 
		
		Long num= val.numericalValue();
		if (num != null) {
			long lnum= num;
			if (lnum >= 0 && lnum <= Short.MAX_VALUE) {
				buffer.putByte((byte) (firstByte | ITypeMarshalBuffer.FLAG1));
				buffer.putShort((short) lnum);
				buffer.marshalType(getType());
				return;
			} 
		}
		buffer.putByte((byte) (firstByte | ITypeMarshalBuffer.FLAG2));
		buffer.marshalValue(val);
		buffer.marshalType(getType());
	}

	public static IType unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		IValue value= null;
		if ((firstByte & ITypeMarshalBuffer.FLAG1) != 0) {
			value = Value.create(buffer.getShort());
		} else if ((firstByte & ITypeMarshalBuffer.FLAG2) != 0) {
			value = buffer.unmarshalValue();
		}
		IType nested= buffer.unmarshalType();
		return new CPPArrayType(nested, value);
	}
}
