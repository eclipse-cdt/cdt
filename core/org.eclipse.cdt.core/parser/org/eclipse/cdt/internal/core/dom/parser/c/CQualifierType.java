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
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * @author dsteffle
 */
public class CQualifierType implements ICQualifierType, ITypeContainer {

	private boolean isConst;
	private boolean isVolatile;
	private boolean isRestrict;
	private IType type = null;

	/**
	 * CQualifierType has an IBasicType to keep track of the basic type information.
	 * 
	 * @param type the CQualifierType's IBasicType
	 */
	public CQualifierType(ICASTDeclSpecifier declSpec) {
		this.type = resolveType( declSpec );
		this.isConst = declSpec.isConst();
		this.isVolatile = declSpec.isVolatile();
		this.isRestrict = declSpec.isRestrict();
	}
	
	public CQualifierType( IType type, boolean isConst, boolean isVolatile, boolean isRestrict ){
		this.type = type;
		this.isConst = isConst;
		this.isVolatile = isVolatile;
		this.isRestrict = isRestrict;
	}
	
	public boolean isSameType( IType obj ){
	    if( obj == this )
	        return true;
	    if( obj instanceof ITypedef )
	        return obj.isSameType( this );
	    
	    if( obj instanceof ICQualifierType ){
	        ICQualifierType qt = (ICQualifierType) obj;
            try {
		        if( isConst() != qt.isConst() ) return false;
		        if( isRestrict() != qt.isRestrict() ) return false;
		        if( isVolatile() != qt.isVolatile() ) return false;
            
		        if( type == null )
		        	return false;
                return type.isSameType( qt.getType() );
            } catch ( DOMException e ) {
                return false;
            }
        }
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
	 * @see org.eclipse.cdt.core.dom.ast.c.ICQualifierType#isRestrict()
	 */
	public boolean isRestrict() {
		return isRestrict; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IQualifierType#getType()
	 */
	private IType resolveType( ICASTDeclSpecifier declSpec ) {
		IType t = null;
		if( declSpec instanceof ICASTTypedefNameSpecifier ){
			ICASTTypedefNameSpecifier nameSpec = (ICASTTypedefNameSpecifier) declSpec;
			t = (IType) nameSpec.getName().resolveBinding();			
		} else if( declSpec instanceof IASTElaboratedTypeSpecifier ){
			IASTElaboratedTypeSpecifier elabTypeSpec = (IASTElaboratedTypeSpecifier) declSpec;
			t = (IType) elabTypeSpec.getName().resolveBinding();
		} else if( declSpec instanceof IASTCompositeTypeSpecifier ){
			IASTCompositeTypeSpecifier compTypeSpec = (IASTCompositeTypeSpecifier) declSpec;
			t = (IType) compTypeSpec.getName().resolveBinding();
		} else {
		    t = new CBasicType((ICASTSimpleDeclSpecifier)declSpec);
		}
		
		return t;
	}
	
	public IType getType(){
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
