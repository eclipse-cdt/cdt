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

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICBasicType;

/**
 * @author dsteffle
 */
public class CBasicType implements ICBasicType {
	
	private ICASTSimpleDeclSpecifier sds = null;
	
	/**
	 * keep a reference to the declaration specifier so that duplicate information isn't generated.
	 * 
	 * @param sds the simple declaration specifier
	 */
	public CBasicType(ICASTSimpleDeclSpecifier sds) {
		this.sds = sds;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBasicType#getType()
	 */
	public int getType() {
		return sds.getType();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBasicType#isSigned()
	 */
	public boolean isSigned() {
		return sds.isSigned();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBasicType#isUnsigned()
	 */
	public boolean isUnsigned() {
		return sds.isUnsigned();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBasicType#isShort()
	 */
	public boolean isShort() {
		return sds.isShort();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBasicType#isLong()
	 */
	public boolean isLong() {
		return sds.isLong();
	}

	public boolean isLongLong() {
		return sds.isLongLong();
	}

	public boolean isSameType(IType obj) {
	    if( obj == this )
	        return true;
	    if( obj instanceof ITypedef )
	        return obj.isSameType( this );
	    
		if (!(obj instanceof CBasicType)) return false;
		
		CBasicType cObj = (CBasicType)obj;
		
		return (cObj.getType() == this.getType()
				&& cObj.isLong() == this.isLong() 
				&& cObj.isShort() == this.isShort() 
				&& cObj.isSigned() == this.isSigned() 
				&& cObj.isUnsigned() == this.isUnsigned()
				&& cObj.isLongLong() == this.isLongLong());
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBasicType#getValue()
	 */
	public IASTExpression getValue() {
		// TODO Auto-generated method stub
		return null;
	}
}
