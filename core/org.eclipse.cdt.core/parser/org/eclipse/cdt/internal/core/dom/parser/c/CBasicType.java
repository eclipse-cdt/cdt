/*******************************************************************************
 *  Copyright (c) 2005, 2010 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICBasicType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

public class CBasicType implements ICBasicType, ISerializableType {
	private final Kind fKind;
	private int fModifiers = 0;
	private IASTExpression value = null;
	
	public CBasicType(Kind kind, int modifiers, IASTExpression value) {
		if (kind == Kind.eUnspecified) {
			if ((modifiers & (IS_COMPLEX | IS_IMAGINARY)) != 0) {
				fKind= Kind.eFloat;
			} else {
				fKind= Kind.eInt;
			}
		} else {
			fKind= kind;
		}
		fModifiers = modifiers;
		this.value = value;
	}

	public CBasicType(Kind kind, int modifiers) {
		this(kind, modifiers, null);
	}
	
	public CBasicType(ICASTSimpleDeclSpecifier sds) {
		this (getKind(sds), getQualifiers(sds), null);
	}
	
	private static int getQualifiers(ICASTSimpleDeclSpecifier sds) {
		return (sds.isLong() ? IS_LONG  : 0) |
				(sds.isShort() ? IS_SHORT : 0) |
				(sds.isSigned() ? IS_SIGNED: 0) |
				(sds.isUnsigned() ? IS_UNSIGNED : 0) |
				(sds.isLongLong() ? IS_LONG_LONG : 0) |
				(sds.isComplex() ? IS_COMPLEX : 0) |
				(sds.isImaginary() ? IS_IMAGINARY : 0);
	}
	
	private static Kind getKind(ICASTSimpleDeclSpecifier sds) {
		switch (sds.getType()) {
		case IASTSimpleDeclSpecifier.t_bool:
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
	
	public int getModifiers() {
		return fModifiers;
	}

	public boolean isSigned() {
		return (fModifiers & IS_SIGNED) != 0;
	}

	public boolean isUnsigned() {
		return (fModifiers & IS_UNSIGNED) != 0;
	}

	public boolean isShort() {
		return (fModifiers & IS_SHORT) != 0;
	}

	public boolean isLong() {
		return (fModifiers & IS_LONG) != 0;
	}

	public boolean isLongLong() {
		return (fModifiers & IS_LONG_LONG) != 0;
	}

	public boolean isSameType(IType obj) {
		if (obj == this)
			return true;
		if (obj instanceof ITypedef)
			return obj.isSameType(this);
	    
		if (!(obj instanceof ICBasicType)) return false;
		
		ICBasicType cObj = (ICBasicType)obj;
		
		if (fKind != cObj.getKind()) {
			return false;
		}
		
		if (fKind == Kind.eInt) {
			//signed int and int are equivalent
			return (fModifiers & ~IS_SIGNED) == (cObj.getModifiers() & ~IS_SIGNED);
		} else {
			return (fModifiers == cObj.getModifiers());
		}
	}
	
    @Override
	public Object clone() {
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch (CloneNotSupportedException e) {
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
		return (fModifiers & IS_COMPLEX) != 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICBasicType#isImaginary()
	 */
	public boolean isImaginary() {
		return (fModifiers & IS_IMAGINARY) != 0;
	}

	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		final int kind= getKind().ordinal();
		final int shiftedKind=  kind * ITypeMarshalBuffer.FLAG1;
		final int modifiers= getModifiers();
		if (shiftedKind < ITypeMarshalBuffer.FLAG4 && modifiers == 0) {
			buffer.putByte((byte) (ITypeMarshalBuffer.BASIC_TYPE | shiftedKind));
		} else {
			buffer.putByte((byte) (ITypeMarshalBuffer.BASIC_TYPE | ITypeMarshalBuffer.FLAG4));
			buffer.putByte((byte) kind);
			buffer.putByte((byte) modifiers);
		} 
	}
	
	public static IType unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		final boolean dense= (firstByte & ITypeMarshalBuffer.FLAG4) == 0;
		int modifiers= 0;
		int kind;
		if (dense) {
			kind= (firstByte & (ITypeMarshalBuffer.FLAG4-1))/ITypeMarshalBuffer.FLAG1;
		} else {
			kind= buffer.getByte();
			modifiers= buffer.getByte();
		} 
		return new CBasicType(Kind.values()[kind], modifiers);
	}

	@Deprecated
	public int getType() {
		switch (fKind) {
		case eBoolean:
			return t_Bool;
		case eChar:
		case eWChar:
		case eChar16:
		case eChar32:
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
	
	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}
}
