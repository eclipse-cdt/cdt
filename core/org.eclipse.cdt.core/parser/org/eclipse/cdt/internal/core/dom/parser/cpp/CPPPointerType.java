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
 * Created on Dec 10, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * @author aniefer
 */
public class CPPPointerType implements IPointerType, ITypeContainer {
	protected IASTPointer operator = null;
	protected IType type = null;
	/**
	 * @param type
	 * @param operator
	 */
	public CPPPointerType(IType type, IASTPointer operator) {
		this.type = type;
		this.operator = operator;
	}

	/**
	 * @param type2
	 */
	public CPPPointerType(IType type) {
		this.type = type;
	}

	public boolean equals( Object o ){
	    if( o instanceof ITypedef )
	        return o.equals( this );
	    if( !( o instanceof CPPPointerType ) ) 
	        return false;
	    
	    if( type == null )
	        return false;
	    
	    CPPPointerType pt = (CPPPointerType) o;
	    if( isConst() == pt.isConst() && isVolatile() == pt.isVolatile() )
	        return type.equals( pt.getType() );
	    return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IPointerType#getType()
	 */
	public IType getType() {
		return type;
	}
	
	public void setType( IType t ){
	    type = t;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IPointerType#isConst()
	 */
	public boolean isConst() {
		return ( operator != null ) ? operator.isConst() : false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IPointerType#isVolatile()
	 */
	public boolean isVolatile() {
		return ( operator != null ) ? operator.isVolatile() : false;
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
