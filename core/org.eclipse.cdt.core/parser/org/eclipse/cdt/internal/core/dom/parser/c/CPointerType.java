/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Devin Steffler (IBM Rational Software) - Initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICPointerType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

public class CPointerType implements ICPointerType, ITypeContainer, ISerializableType {
	static public final int IS_CONST    = 1;
	static public final int IS_RESTRICT = 1 << 1;
	static public final int IS_VOLATILE = 1 << 2;
	
	IType nextType = null;
	private int qualifiers = 0;
	
	public CPointerType() {}
	
	public CPointerType(IType next, int qualifiers) {
		this.nextType = next;
		this.qualifiers = qualifiers;
	}
	
	@Override
	public boolean isSameType(IType obj) {
	    if (obj == this)
	        return true;
	    if (obj instanceof ITypedef)
	        return obj.isSameType(this);

	    if (obj instanceof ICPointerType) {
	        ICPointerType pt = (ICPointerType) obj;
            if (isConst() != pt.isConst()) return false;
			if (isRestrict() != pt.isRestrict()) return false;
			if (isVolatile() != pt.isVolatile()) return false;
         
			return pt.getType().isSameType(nextType);
        }
    	return false;
	}
	
	@Override
	public boolean isRestrict() {
		return (qualifiers & IS_RESTRICT) != 0;
	}

	@Override
	public IType getType() {
		return nextType;
	}
	
	@Override
	public void setType(IType type) {
		nextType = type;
	}

	@Override
	public boolean isConst() {
		return (qualifiers & IS_CONST) != 0;
	}

	@Override
	public boolean isVolatile() {
		return (qualifiers & IS_VOLATILE) != 0;
	}
		
    @Override
	public Object clone() {
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch (CloneNotSupportedException e) {
            // Not going to happen.
        }
        return t;
    }

	public void setQualifiers(int qualifiers) {
		this.qualifiers = qualifiers;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		int firstByte= ITypeMarshalBuffer.POINTER;
		if (isConst()) firstByte |= ITypeMarshalBuffer.FLAG1;
		if (isVolatile()) firstByte |= ITypeMarshalBuffer.FLAG2;
		if (isRestrict()) firstByte |= ITypeMarshalBuffer.FLAG3;
		buffer.putByte((byte) firstByte);
		buffer.marshalType(getType());
	}
	
	public static IType unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		IType nested= buffer.unmarshalType();
		return new CPointerType(nested, firstByte/ITypeMarshalBuffer.FLAG1);
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}
}
