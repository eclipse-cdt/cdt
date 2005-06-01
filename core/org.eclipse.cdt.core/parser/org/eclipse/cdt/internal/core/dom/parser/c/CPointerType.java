/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICPointerType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * @author dsteffle
 */
public class CPointerType implements ICPointerType, ITypeContainer {
	static public final int IS_CONST       = 1;
	static public final int IS_RESTRICT    = 1 << 1;
	static public final int IS_VOLATILE    = 1 << 2;
	
	IType nextType = null;
	private int qualifiers = 0;
	
	public CPointerType() {}
	
	public CPointerType(IType next, int qualifiers) {
		this.nextType = next;
		this.qualifiers = qualifiers;
	}
	
	public boolean isSameType( IType obj ){
	    if( obj == this )
	        return true;
	    if( obj instanceof ITypedef )
	        return obj.isSameType( this );
	    
	    if( obj instanceof ICPointerType ){
	        ICPointerType pt = (ICPointerType) obj;
            try {
		        if( isConst() != pt.isConst() ) return false;
		        if( isRestrict() != pt.isRestrict() ) return false;
		        if( isVolatile() != pt.isVolatile() ) return false;
            
                return pt.getType().isSameType( nextType );
            } catch ( DOMException e ) {
                return false;
            }
        }
    	return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICPointerType#isRestrict()
	 */
	public boolean isRestrict() {
		return (qualifiers & IS_RESTRICT) != 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IPointerType#getType()
	 */
	public IType getType() {
		return nextType;
	}
	
	public void setType(IType type) {
		nextType = type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IPointerType#isConst()
	 */
	public boolean isConst() {
		return (qualifiers & IS_CONST) != 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IPointerType#isVolatile()
	 */
	public boolean isVolatile() {
		return (qualifiers & IS_VOLATILE) != 0;
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

	public void setQualifiers(int qualifiers) {
		this.qualifiers = qualifiers;
	}
}
