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
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

public class CPPQualifierType implements IQualifierType, ITypeContainer, ISerializableType {
    private final boolean isConst;
    private final boolean isVolatile;
    private IType type;
    
    public CPPQualifierType(IType type, boolean isConst, boolean isVolatile) {
        this.isConst = isConst;
        this.isVolatile = isVolatile;
        setType(type);
    }
    
    @Override
	public boolean isSameType(IType o) {
		if (o instanceof ITypedef)
			return o.isSameType(this);
		if (!(o instanceof IQualifierType))
			return false;

		IQualifierType pt = (IQualifierType) o;
		if (isConst() == pt.isConst() && isVolatile() == pt.isVolatile() && type != null)
			return type.isSameType(pt.getType());
		return false;
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IQualifierType#isConst()
     */
    @Override
	public boolean isConst() {
        return isConst;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IQualifierType#isVolatile()
     */
    @Override
	public boolean isVolatile() {
        return isVolatile;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IQualifierType#getType()
     */
    @Override
	public IType getType() {
        return type;
    }
    
    @Override
	public void setType(IType t) {
    	assert t != null;
        type = t;
    }
    
    @Override
	public Object clone() {
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch (CloneNotSupportedException e) {
            //not going to happen
        }
        return t;
    }

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		int firstByte= ITypeMarshalBuffer.CVQUALIFIER;
		if (isConst()) firstByte |= ITypeMarshalBuffer.FLAG1;
		if (isVolatile()) firstByte |= ITypeMarshalBuffer.FLAG2;
		buffer.putByte((byte) firstByte);
		buffer.marshalType(getType());
	}
	
	public static IType unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		IType nested= buffer.unmarshalType();
		return new CPPQualifierType(nested, (firstByte & ITypeMarshalBuffer.FLAG1) != 0,
				(firstByte & ITypeMarshalBuffer.FLAG2) != 0);
	}
}
