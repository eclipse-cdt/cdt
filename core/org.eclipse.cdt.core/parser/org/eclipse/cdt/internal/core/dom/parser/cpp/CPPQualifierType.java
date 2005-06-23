/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Dec 13, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * @author aniefer
 */
public class CPPQualifierType implements IQualifierType, ITypeContainer {
    private boolean isConst = false;
    private boolean isVolatile = false;
    private IType type = null;
    
    public CPPQualifierType( IType type, boolean isConst, boolean isVolatile ){
        this.type = type;
        this.isConst = isConst;
        this.isVolatile = isVolatile;
    }
    
    public boolean isSameType( IType o ){
	    if( o instanceof ITypedef )
	        return o.isSameType( this );
	    if( !( o instanceof CPPQualifierType ) ) 
	        return false;
	    
	    CPPQualifierType pt = (CPPQualifierType) o;
	    if( isConst() == pt.isConst() && isVolatile() == pt.isVolatile() )
	        return type.isSameType( pt.getType() );
	    return false;
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IQualifierType#isConst()
     */
    public boolean isConst() {
        return isConst;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IQualifierType#isVolatile()
     */
    public boolean isVolatile() {
        return isVolatile;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IQualifierType#getType()
     */
    public IType getType() {
        return type;
    }
    
    public void setType( IType t ){
        type = t;
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
