/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Devin Steffler (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

public class CFunctionType implements IFunctionType, ISerializableType {
    private final IType[] parameters;
    private final IType returnType;
    
    public CFunctionType( IType returnType, IType []  types ) {
        this.returnType = returnType;
        this.parameters = types;
    }

    @Override
	public boolean isSameType( IType o ){
        if( o == this )
            return true;
        if( o instanceof ITypedef )
            return o.isSameType( this );
        if( o instanceof IFunctionType ){
            IFunctionType ft = (IFunctionType) o;
            IType [] fps;
            fps = ft.getParameterTypes();
            if( fps.length != parameters.length )
                return false;
            if( ! returnType.isSameType( ft.getReturnType() ) )
			    return false;
            for( int i = 0; i < parameters.length; i++ )
                if( ! parameters[i].isSameType( fps[i] ) )
                    return false;
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
	public Object clone(){
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch ( CloneNotSupportedException e ) {
            //not going to happen
        }
        return t;
    }

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.FUNCTION_TYPE;

		int len= parameters.length & 0xffff;
		int codedLen= len * ITypeMarshalBuffer.FIRST_FLAG;
		if (codedLen < ITypeMarshalBuffer.LAST_FLAG) {
			firstBytes |= codedLen;
			buffer.putShort(firstBytes);
		} else {
			firstBytes |= ITypeMarshalBuffer.LAST_FLAG;
			buffer.putShort(firstBytes);
			buffer.putInt(len);
		}
		
		buffer.marshalType(returnType);
		for (int i = 0; i < len; i++) {
			buffer.marshalType(parameters[i]);
		}
	}
	
	public static IType unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		int len;
		if (((firstBytes & ITypeMarshalBuffer.LAST_FLAG) != 0)) {
			len= buffer.getInt();
		} else {
			len= (firstBytes & (ITypeMarshalBuffer.LAST_FLAG-1))/ITypeMarshalBuffer.FIRST_FLAG;
		}
		IType rt= buffer.unmarshalType();
		IType[] pars= new IType[len];
		for (int i = 0; i < pars.length; i++) {
			pars[i]= buffer.unmarshalType();
		}
		return new CFunctionType(rt, pars);
	}
}
