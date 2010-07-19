/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

/**
 * Integral c++ type.
 */
public class CPPBasicType implements ICPPBasicType, ISerializableType {
	public static int UNIQUE_TYPE_QUALIFIER= -1;
	private final Kind fKind;
	private final int fModifiers;
	private IASTExpression fExpression;

	public CPPBasicType(Kind kind, int qualifiers, IASTExpression expression) {
		if (kind == Kind.eUnspecified && qualifiers != UNIQUE_TYPE_QUALIFIER) {
			if ( (qualifiers & (IS_COMPLEX | IS_IMAGINARY)) != 0) {
				fKind= Kind.eFloat;
			} else if ( (qualifiers & (IS_LONG | IS_SHORT | IS_SIGNED | IS_UNSIGNED | IS_LONG_LONG)) != 0 ) {
				fKind= Kind.eInt;
			} else {
				fKind= Kind.eUnspecified;
			}
		} else {
			fKind= kind;
		}
		fModifiers= qualifiers;
		fExpression= expression;
	}

	public CPPBasicType(Kind kind, int qualifiers) {
		this(kind, qualifiers, null);
	}
	
	public CPPBasicType(ICPPASTSimpleDeclSpecifier sds) {
		this (getKind(sds), getModifiers(sds), null);
	}
	
	private static int getModifiers(ICPPASTSimpleDeclSpecifier sds) {
		return
			( sds.isLong()    ? IBasicType.IS_LONG  : 0 ) |
			( sds.isShort()   ? IBasicType.IS_SHORT : 0 ) |
			( sds.isSigned()  ? IBasicType.IS_SIGNED: 0 ) |
			( sds.isUnsigned()? IBasicType.IS_UNSIGNED : 0 ) |
			( sds.isLongLong()? IBasicType.IS_LONG_LONG : 0 ) |
			( sds.isComplex() ? IBasicType.IS_COMPLEX : 0 ) |
			( sds.isImaginary()?IBasicType.IS_IMAGINARY : 0 );
	}
	
	private static Kind getKind(ICPPASTSimpleDeclSpecifier sds) {
		return getKind(sds.getType());
	}

	static Kind getKind(final int simpleDeclSpecType) {
		switch(simpleDeclSpecType) {
		case IASTSimpleDeclSpecifier.t_bool:
			return Kind.eBoolean;
		case IASTSimpleDeclSpecifier.t_char:
			return Kind.eChar;
		case IASTSimpleDeclSpecifier.t_wchar_t:
			return Kind.eWChar;
		case IASTSimpleDeclSpecifier.t_char16_t:
			return Kind.eChar16;
		case IASTSimpleDeclSpecifier.t_char32_t:
			return Kind.eChar32;
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


	public boolean isSameType(IType object) {
		if (object == this)
			return true;

		if (fModifiers == UNIQUE_TYPE_QUALIFIER)
			return false;

	    if (object instanceof ITypedef)
	        return object.isSameType(this);

		if (!(object instanceof ICPPBasicType))
			return false;

		ICPPBasicType t = (ICPPBasicType) object;
		if (fKind != t.getKind())
			return false;

		if (fKind == Kind.eInt) {
			//signed int and int are equivalent
			return (fModifiers & ~IS_SIGNED) == (t.getModifiers() & ~IS_SIGNED);
		}
		return fModifiers == t.getModifiers();
	}

	public Kind getKind() {
		return fKind;
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

	public boolean isComplex() {
		return (fModifiers & IS_COMPLEX) != 0;
	}

	public boolean isImaginary() {
		return (fModifiers & IS_IMAGINARY) != 0;
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

	public void setFromExpression(IASTExpression val) {
		fExpression = val;
	}

	/**
	 * Returns the expression the type was created for, or <code>null</code>.
	 */
	public IASTExpression getCreatedFromExpression() {
		return fExpression;
	}
	
	public int getModifiers() {
		return fModifiers;
	}
	
	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
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
		return new CPPBasicType(Kind.values()[kind], modifiers);
	}


	@Deprecated
	public int getQualifierBits() {
		return getModifiers();
	}


	@Deprecated
	public int getType() {
		switch (fKind) {
		case eBoolean:
			return t_bool;
		case eChar:
		case eChar16:
		case eChar32:
			return t_char;
		case eWChar:
			return t_wchar_t;
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
    /**
     * @deprecated types don't have values
     */
	@Deprecated
	public IASTExpression getValue() {
		return fExpression;
	}
}
