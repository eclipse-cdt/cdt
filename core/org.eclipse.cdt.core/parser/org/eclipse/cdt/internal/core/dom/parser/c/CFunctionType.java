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
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * @author dsteffle
 */
public class CFunctionType implements IFunctionType {
    IType[] parameters = null;
    IType returnType = null;
    
    /**
     * @param returnType
     * @param types
     */
    public CFunctionType( IType returnType, IType []  types ) {
        this.returnType = returnType;
        this.parameters = types;
    }

    public boolean equals( Object o ){
        if( o instanceof IFunctionType ){
            IFunctionType ft = (IFunctionType) o;
            IType [] fps;
            try {
                fps = ft.getParameterTypes();
            } catch ( DOMException e ) {
                return false;
            }
            if( fps.length != parameters.length )
                return false;
            try {
                if( ! returnType.equals( ft.getReturnType() ) )
                    return false;
            } catch ( DOMException e1 ) {
                return false;
            }
            for( int i = 0; i < parameters.length; i++ )
                if( ! parameters[i].equals( fps[i] ) )
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
}
