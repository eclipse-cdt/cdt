/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents c++ function types. Note that we keep typedefs as part of the function type.
 * For safe usage in index bindings, all fields need to be final.
 */
public class CPPFunctionType implements ICPPFunctionType, ISerializableType {
    private final IType[] parameters;
    private final IType returnType;
    private final boolean isConst;
    private final boolean isVolatile;
    private final boolean takesVarargs;
    
    public CPPFunctionType(IType returnType, IType[] types) {
    	this(returnType, types, false, false, false);
    }

	public CPPFunctionType(IType returnType, IType[] types, boolean isConst, boolean isVolatile,
			boolean takesVarargs) {
        this.returnType = returnType;
        this.parameters = types;
        this.isConst = isConst;
        this.isVolatile= isVolatile;
        this.takesVarargs= takesVarargs;
    }

    @Override
	public boolean isSameType(IType o) {
        if (o instanceof ITypedef)
            return o.isSameType(this);
        if (o instanceof ICPPFunctionType) {
            ICPPFunctionType ft = (ICPPFunctionType) o;
            if (isConst() != ft.isConst() || isVolatile() != ft.isVolatile() || takesVarArgs() != ft.takesVarArgs()) {
                return false;
            }

            IType[] fps;
            fps = ft.getParameterTypes();
			//constructors & destructors have null return type
			if ((returnType == null) ^ (ft.getReturnType() == null))
			    return false;
			
			if (returnType != null && ! returnType.isSameType(ft.getReturnType()))
			    return false;
			
			if (parameters.length == fps.length) {
				for (int i = 0; i < parameters.length; i++) {
			        if (parameters[i] == null || !parameters[i].isSameType(fps[i]))
			            return false;
			    }
			} else {
				if (!SemanticUtil.isEmptyParameterList(parameters) 
					|| !SemanticUtil.isEmptyParameterList(fps)) {
					return false;
				}
			} 
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunctionType#getReturnType()
     */
    @Override
	public IType getReturnType() {
        return returnType;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunctionType#getParameterTypes()
     */
    @Override
	public IType[] getParameterTypes() {
        return parameters;
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
	@Deprecated
    public IPointerType getThisType() {
        return null;
    }

	@Override
	public final boolean isConst() {
		return isConst;
	}

	@Override
	public final boolean isVolatile() {
		return isVolatile;
	}

	@Override
	public boolean takesVarArgs() {
		return takesVarargs;
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		int firstByte= ITypeMarshalBuffer.FUNCTION_TYPE;
		if (isConst()) firstByte |= ITypeMarshalBuffer.FLAG1;
		if (isVolatile()) firstByte |= ITypeMarshalBuffer.FLAG2;
		if (takesVarArgs()) firstByte |= ITypeMarshalBuffer.FLAG3;
		
		int len= (parameters.length & 0xffff);
		if (len > 0xff) {
			firstByte |= ITypeMarshalBuffer.FLAG4;
			buffer.putByte((byte) firstByte);
			buffer.putShort((short) len);
		} else {
			buffer.putByte((byte) firstByte);
			buffer.putByte((byte) len);
		}
		
		buffer.marshalType(returnType);
		for (int i = 0; i < len; i++) {
			buffer.marshalType(parameters[i]);
		}
	}
	
	public static IType unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		int len;
		if (((firstByte & ITypeMarshalBuffer.FLAG4) != 0)) {
			len= buffer.getShort();
		} else {
			len= buffer.getByte();
		}
		IType rt= buffer.unmarshalType();
		IType[] pars= new IType[len];
		for (int i = 0; i < pars.length; i++) {
			pars[i]= buffer.unmarshalType();
		}
		return new CPPFunctionType(rt, pars, (firstByte & ITypeMarshalBuffer.FLAG1) != 0,
				(firstByte & ITypeMarshalBuffer.FLAG2) != 0, (firstByte & ITypeMarshalBuffer.FLAG3) != 0);
	}
}
