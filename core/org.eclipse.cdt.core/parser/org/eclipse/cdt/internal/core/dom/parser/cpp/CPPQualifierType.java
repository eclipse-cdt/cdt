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
    
    public boolean equals( Object o ){
	    if( o instanceof ITypedef )
	        return o.equals( this );
	    if( !( o instanceof CPPQualifierType ) ) 
	        return false;
	    
	    CPPQualifierType pt = (CPPQualifierType) o;
	    if( isConst() == pt.isConst() && isVolatile() == pt.isVolatile() )
	        return type.equals( pt.getType() );
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

}
