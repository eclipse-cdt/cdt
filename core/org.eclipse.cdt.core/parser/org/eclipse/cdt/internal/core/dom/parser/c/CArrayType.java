/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Devin Steffler (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICArrayType;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.core.runtime.CoreException;

public class CArrayType implements ICArrayType, ITypeContainer, ISerializableType {
	IType type;
    private IASTExpression sizeExpression;
    private IValue value= Value.NOT_INITIALIZED;
    private boolean isConst;
    private boolean isVolatile;
    private boolean isRestrict;
    private boolean isStatic;
	private boolean isVariableSized;
	
	public CArrayType(IType type) {
		this.type = type;
	}

	public CArrayType(IType type, boolean isConst, boolean isVolatile, boolean isRestrict, IValue size) {
		this.type= type;
		this.isConst= isConst;
		this.isVolatile= isVolatile;
		this.isRestrict= isRestrict;
		this.value= size;
	}
	
	public void setIsStatic(boolean val) {
		isStatic= val;
	}
	public void setIsVariableLength(boolean val) {
		isVariableSized= val;
	}

    public boolean isSameType(IType obj) {
        if (obj == this)
            return true;
        if (obj instanceof ITypedef)
            return obj.isSameType(this);
        if (obj instanceof ICArrayType) {
        	ICArrayType at = (ICArrayType) obj;
        	if (isConst() != at.isConst()) return false;
			if (isRestrict() != at.isRestrict()) return false;
			if (isStatic() != at.isStatic()) return false;
			if (isVolatile() != at.isVolatile()) return false;
			if (isVariableLength() != at.isVariableLength()) return false;

			return at.getType().isSameType(type) && hasSameSize(at);
        }
    	return false;
    }
    
	private boolean hasSameSize(IArrayType rhs) {
		IValue s1 = getSize();
		IValue s2 = rhs.getSize();
		if (s1 == s2)
			return true;
		if (s1 == null || s2 == null)
			return false;
		return CharArrayUtils.equals(s1.getSignature(), s2.getSignature());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IArrayType#getType()
	 */
	public IType getType() {
		return type;
	}
	
	public void setType(IType t) {
	    this.type = t;
	}
	
	public void setModifier(ICASTArrayModifier mod) {
		isConst= mod.isConst();
		isVolatile= mod.isVolatile();
		isRestrict= mod.isRestrict();
		isStatic= mod.isStatic();
		isVariableSized= mod.isVariableSized();
		sizeExpression= mod.getConstantExpression();
	}

	public boolean isConst() {
		return isConst;
	}

	public boolean isRestrict() {
		return isRestrict;
	}

	public boolean isVolatile() {
		return isVolatile;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public boolean isVariableLength() {
		return isVariableSized;
	}

    public IValue getSize() {
    	if (value != Value.NOT_INITIALIZED)
    		return value;
    	
    	if (sizeExpression == null)
    		return value= null;

    	return value= Value.create(sizeExpression, Value.MAX_RECURSION_DEPTH);
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

	
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		int firstByte= ITypeMarshalBuffer.ARRAY;
		int flags= 0;
		short nval= -1;
		IValue val= null;

		if (isConst()) flags |= 0x01;
		if (isVolatile()) flags |= 0x02;
		if (isRestrict()) flags |= 0x04;
		if (isStatic()) flags |= 0x08;
		if (isVariableLength()) flags |= 0x10;
		if (flags != 0) {
			firstByte |= ITypeMarshalBuffer.FLAG1;
		}


		val= getSize();
		if (val != null) {
			firstByte |= ITypeMarshalBuffer.FLAG2;
			Long num= val.numericalValue();
			if (num != null) {
				long l= num;
				if (l>=0 && l <= Short.MAX_VALUE) {
					nval= (short) l;
					firstByte |= ITypeMarshalBuffer.FLAG3;
				} 
			}
		}
		buffer.putByte((byte) firstByte);
		if (flags != 0) {
			buffer.putByte((byte) flags);
		}
		if (nval >= 0) {
			buffer.putShort(nval);
		} else if (val != null) {
			buffer.marshalValue(val);
		}
		buffer.marshalType(getType());
	}

	public static IType unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		int flags= 0;
		IValue value= null;
		if ( (firstByte & ITypeMarshalBuffer.FLAG1) != 0) {
			flags= buffer.getByte();
		}
		if ((firstByte & ITypeMarshalBuffer.FLAG3) != 0) {
			value = Value.create(buffer.getShort());
		} else if ((firstByte & ITypeMarshalBuffer.FLAG2) != 0) {
			value = buffer.unmarshalValue();
		}
		IType nested= buffer.unmarshalType();		
		CArrayType result= new CArrayType(nested, (flags & 0x01) != 0, (flags & 0x02) != 0, (flags & 0x04) != 0, value);
		result.setIsStatic((flags & 0x08) != 0);
		result.setIsVariableLength((flags & 0x10) != 0);
		return result;
	}
	
	@Deprecated
    public IASTExpression getArraySizeExpression() {
        if (sizeExpression != null)
            return sizeExpression;
        return null;
    }
}
