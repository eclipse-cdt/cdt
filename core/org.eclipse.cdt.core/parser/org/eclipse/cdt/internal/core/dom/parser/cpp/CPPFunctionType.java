/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Dec 13, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;

/**
 * @author aniefer
 */
public class CPPFunctionType implements ICPPFunctionType {
    private IType[] parameters = null;
    private IType returnType = null;
	private boolean isConst = false;
	private boolean isVolatile = false;
    
    /**
     * @param returnType
     * @param types
     */
    public CPPFunctionType( IType returnType, IType []  types ) {
        this.returnType = returnType;
        this.parameters = types;
    }
	public CPPFunctionType( IType returnType, IType [] types, boolean isConst, boolean isVolatile ) {
        this.returnType = returnType;
        this.parameters = types;
		this.isConst = isConst;
		this.isVolatile = isVolatile;
    }

    public boolean isSameType( IType o ){
        if( o instanceof ITypedef )
            return o.isSameType( this );
        if( o instanceof ICPPFunctionType ){
            ICPPFunctionType ft = (ICPPFunctionType) o;
            IType [] fps;
            try {
                fps = ft.getParameterTypes();
            } catch ( DOMException e ) {
                return false;
            }
			try {
                //constructors & destructors have null return type
                if( ( returnType == null ) ^ ( ft.getReturnType() == null ) )
                    return false;
                else if( returnType != null && ! returnType.isSameType( ft.getReturnType() ) )
                    return false;
            } catch ( DOMException e1 ) {
                return false;
            }
			
			try {
				if( parameters.length == 1 && fps.length == 0 ){
					if( !(parameters[0] instanceof IBasicType) || ((IBasicType)parameters[0]).getType() != IBasicType.t_void )
						return false;
				} else if( fps.length == 1 && parameters.length == 0 ){
					if( !(fps[0] instanceof IBasicType) || ((IBasicType)fps[0]).getType() != IBasicType.t_void )
						return false;
				} else if( parameters.length != fps.length ){
	                return false;
	            } else {
					 for( int i = 0; i < parameters.length; i++ ){
		                if( ! parameters[i].isSameType( fps[i] ) )
		                    return false;
		            }
	            }
			} catch (DOMException e ){
				return false;
			}
           
            if( isConst != ft.isConst() || isVolatile != ft.isVolatile() )
                return false;
                
            return true;
        }
        return false;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunctionType#getReturnType()
     */
    public IType getReturnType() {
        return returnType;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunctionType#getParameterTypes()
     */
    public IType[] getParameterTypes() {
        return parameters;
    }

    public Object clone(){
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch ( CloneNotSupportedException e ) {
            //not going to happen
        }
        return t;
    }

	public boolean isConst() {
		return isConst;
	}

	public boolean isVolatile() {
		return isVolatile;
	}
}
