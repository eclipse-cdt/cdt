/*******************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *    IBM Rational Software - Initial API and implementation 
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICBasicType;
import org.eclipse.cdt.internal.core.index.IIndexType;

/**
 * @author dsteffle
 */
public class CBasicType implements ICBasicType {
	static public final int IS_LONG = 1;
	static public final int IS_LONGLONG = 1 << 1;
	static public final int IS_SHORT    = 1 << 2;
	static public final int IS_SIGNED   = 1 << 3;
	static public final int IS_UNSIGNED = 1 << 4;
	static public final int IS_COMPLEX  = 1 << 5;
	static public final int IS_IMAGINARY= 1 << 6;
	
	private int type = 0;
	private int qualifiers = 0;
	private IASTExpression value = null;
	
	/**
	 * keep a reference to the declaration specifier so that duplicate information isn't generated.
	 * 
	 * @param sds the simple declaration specifier
	 */
	public CBasicType(ICASTSimpleDeclSpecifier sds) {
		this.type = sds.getType();
		this.qualifiers = ( sds.isLong()    ? CBasicType.IS_LONG  : 0 ) |
		   				  ( sds.isShort()   ? CBasicType.IS_SHORT : 0 ) |
		   				  ( sds.isSigned()  ? CBasicType.IS_SIGNED: 0 ) |
		   				  ( sds.isUnsigned()? CBasicType.IS_UNSIGNED : 0 ) |
						  ( sds.isLongLong()? CBasicType.IS_LONGLONG : 0 ) |
						  ( sds.isComplex() ? CBasicType.IS_COMPLEX : 0 ) |
						  ( sds.isImaginary()?CBasicType.IS_IMAGINARY : 0 );
		
		if( type == IBasicType.t_unspecified ){
			if( (qualifiers & ( IS_COMPLEX | IS_IMAGINARY )) != 0 )
				type = IBasicType.t_float;
			else {
				type = IBasicType.t_int;
			}
		}
	}
	
	public CBasicType( int type, int qualifiers ){
		this.type = type;
		this.qualifiers = qualifiers;
		
		if( type == IBasicType.t_unspecified ){
			if( (qualifiers & ( IS_COMPLEX | IS_IMAGINARY )) != 0 )
				type = IBasicType.t_float;
			else {
				type = IBasicType.t_int;
			}
		}
	}
	
	public CBasicType( int type, int qualifiers, IASTExpression value ){
		this.type = type;
		this.qualifiers = qualifiers;
		this.value = value;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBasicType#getType()
	 */
	public int getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBasicType#isSigned()
	 */
	public boolean isSigned() {
		return ( qualifiers & IS_SIGNED) != 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBasicType#isUnsigned()
	 */
	public boolean isUnsigned() {
		return ( qualifiers & IS_UNSIGNED) != 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBasicType#isShort()
	 */
	public boolean isShort() {
		return ( qualifiers & IS_SHORT) != 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBasicType#isLong()
	 */
	public boolean isLong() {
		return ( qualifiers & IS_LONG) != 0;
	}

	public boolean isLongLong() {
		return ( qualifiers & IS_LONGLONG) != 0;
	}

	public boolean isSameType(IType obj) {
	    if( obj == this )
	        return true;
	    if( obj instanceof ITypedef || obj instanceof IIndexType)
	        return obj.isSameType( this );
	    
		if (!(obj instanceof CBasicType)) return false;
		
		CBasicType cObj = (CBasicType)obj;
		
		if (type != cObj.type) {
			return false;
		}
		
		if (type == IBasicType.t_int) {
			//signed int and int are equivalent
			return (qualifiers & ~IS_SIGNED) == (cObj.qualifiers & ~IS_SIGNED);
		} else {
			return (qualifiers == cObj.qualifiers);
		}
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

    @Deprecated
	public IASTExpression getValue() {
		return value;
	}
	
	public void setValue( IASTExpression expression ){
		this.value = expression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICBasicType#isComplex()
	 */
	public boolean isComplex() {
		return ( qualifiers & IS_COMPLEX) != 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICBasicType#isImaginary()
	 */
	public boolean isImaginary() {
		return ( qualifiers & IS_IMAGINARY) != 0;
	}
}
