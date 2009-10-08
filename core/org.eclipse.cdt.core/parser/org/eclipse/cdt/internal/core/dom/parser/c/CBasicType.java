/*******************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *    Devin Steffler (IBM Rational Software) - Initial API and implementation 
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICBasicType;
import org.eclipse.cdt.internal.core.index.IIndexType;

public class CBasicType implements ICBasicType {
	public final static int IS_LONG = 1;
	public final static int IS_LONGLONG = 1 << 1;
	public final static int IS_SHORT    = 1 << 2;
	public final static int IS_SIGNED   = 1 << 3;
	public final static int IS_UNSIGNED = 1 << 4;
	public final static int IS_COMPLEX  = 1 << 5;
	public final static int IS_IMAGINARY= 1 << 6;
	
	private final Kind fKind;
	private int qualifiers = 0;
	private IASTExpression value = null;
	
	public CBasicType(Kind kind, int qualifiers, IASTExpression value ){
		if (kind == Kind.eUnspecified) {
			if ( (qualifiers & (IS_COMPLEX | IS_IMAGINARY)) != 0) {
				fKind= Kind.eFloat;
			} else {
				fKind= Kind.eInt;
			}
		} else {
			fKind= kind;
		}
		this.qualifiers = qualifiers;
		this.value = value;
	}

	public CBasicType(Kind kind, int qualifiers) {
		this(kind, qualifiers, null);
	}
	
	public CBasicType(ICASTSimpleDeclSpecifier sds) {
		this (getKind(sds), getQualifiers(sds), null);
	}
	
	private static int getQualifiers(ICASTSimpleDeclSpecifier sds) {
		return ( sds.isLong()    ? CBasicType.IS_LONG  : 0 ) |
		( sds.isShort()   ? CBasicType.IS_SHORT : 0 ) |
		( sds.isSigned()  ? CBasicType.IS_SIGNED: 0 ) |
		( sds.isUnsigned()? CBasicType.IS_UNSIGNED : 0 ) |
		( sds.isLongLong()? CBasicType.IS_LONGLONG : 0 ) |
		( sds.isComplex() ? CBasicType.IS_COMPLEX : 0 ) |
		( sds.isImaginary()?CBasicType.IS_IMAGINARY : 0 );
	}
	
	private static Kind getKind(ICASTSimpleDeclSpecifier sds) {
		switch(sds.getType()) {
		case ICASTSimpleDeclSpecifier.t_Bool:
			return Kind.eBoolean;
		case IASTSimpleDeclSpecifier.t_char:
			return Kind.eChar;
		case IASTSimpleDeclSpecifier.t_double:
			return Kind.eDouble;
		case IASTSimpleDeclSpecifier.t_float:
			return Kind.eFloat;
		case IASTSimpleDeclSpecifier.t_int:
			return Kind.eInt;
		case IASTSimpleDeclSpecifier.t_void:
			return Kind.eVoid;
		default:
			return Kind.eUnspecified;
		}
	}

	public Kind getKind() {
		return fKind;
	}

	public boolean isSigned() {
		return (qualifiers & IS_SIGNED) != 0;
	}

	public boolean isUnsigned() {
		return (qualifiers & IS_UNSIGNED) != 0;
	}

	public boolean isShort() {
		return (qualifiers & IS_SHORT) != 0;
	}

	public boolean isLong() {
		return (qualifiers & IS_LONG) != 0;
	}

	public boolean isLongLong() {
		return (qualifiers & IS_LONGLONG) != 0;
	}

	public boolean isSameType(IType obj) {
	    if( obj == this )
	        return true;
	    if( obj instanceof ITypedef || obj instanceof IIndexType)
	        return obj.isSameType( this );
	    
		if (!(obj instanceof CBasicType)) return false;
		
		CBasicType cObj = (CBasicType)obj;
		
		if (fKind != cObj.fKind) {
			return false;
		}
		
		if (fKind == Kind.eInt) {
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

	@Deprecated
	public int getType() {
		switch (fKind) {
		case eBoolean:
			return t_Bool;
		case eChar:
		case eWChar:
			return t_char;
		case eDouble:
			return t_double;
		case eFloat:
			return t_float;
		case eInt:
			return t_int;
		case eVoid:
			return t_void;
		case eUnspecified:
			return t_unspecified;
		}
		return t_unspecified;
	}
}
