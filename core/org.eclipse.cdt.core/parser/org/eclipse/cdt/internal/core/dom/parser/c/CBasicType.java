/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Devin Steffler (IBM Rational Software) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
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
	public static final CBasicType VOID = new CBasicType(Kind.eVoid, 0, null);
	public static final CBasicType INT = new CBasicType(Kind.eInt, 0, null);

	private final Kind fKind;
	private int fModifiers;
	private IASTExpression value;

	public CBasicType(Kind kind, int modifiers, IASTExpression value) {
		if (kind == Kind.eUnspecified) {
			if ((modifiers & (IS_COMPLEX | IS_IMAGINARY)) != 0) {
				fKind = Kind.eFloat;
			} else {
				fKind = Kind.eInt;
			}
		} else {
			fKind = kind;
		}
		fModifiers = modifiers;
		this.value = value;
	}

	public CBasicType(Kind kind, int modifiers) {
		this(kind, modifiers, null);
	}

	public CBasicType(ICASTSimpleDeclSpecifier sds) {
		this(getKind(sds), getQualifiers(sds), null);
	}

	private static int getQualifiers(ICASTSimpleDeclSpecifier sds) {
		return (sds.isLong() ? IS_LONG : 0) | (sds.isShort() ? IS_SHORT : 0) | (sds.isSigned() ? IS_SIGNED : 0)
				| (sds.isUnsigned() ? IS_UNSIGNED : 0) | (sds.isLongLong() ? IS_LONG_LONG : 0)
				| (sds.isComplex() ? IS_COMPLEX : 0) | (sds.isImaginary() ? IS_IMAGINARY : 0);
	}

	private static Kind getKind(ICASTSimpleDeclSpecifier sds) {
		// Note: when adding a new kind, marshal() and unnmarshal() may need to be revised.
		switch (sds.getType()) {
		case IASTSimpleDeclSpecifier.t_bool:
			return Kind.eBoolean;
		case IASTSimpleDeclSpecifier.t_char:
			return Kind.eChar;
		case IASTSimpleDeclSpecifier.t_double:
			return Kind.eDouble;
		case IASTSimpleDeclSpecifier.t_float:
			return Kind.eFloat;
		case IASTSimpleDeclSpecifier.t_float128:
			return Kind.eFloat128;
		case IASTSimpleDeclSpecifier.t_decimal32:
			return Kind.eDecimal32;
		case IASTSimpleDeclSpecifier.t_decimal64:
			return Kind.eDecimal64;
		case IASTSimpleDeclSpecifier.t_decimal128:
			return Kind.eDecimal128;
		case IASTSimpleDeclSpecifier.t_int:
			return Kind.eInt;
		case IASTSimpleDeclSpecifier.t_int128:
			return Kind.eInt128;
		case IASTSimpleDeclSpecifier.t_void:
			return Kind.eVoid;
		default:
			return Kind.eUnspecified;
		}
	}

	@Override
	public Kind getKind() {
		return fKind;
	}

	@Override
	public int getModifiers() {
		return fModifiers;
	}

	@Override
	public boolean isSigned() {
		return (fModifiers & IS_SIGNED) != 0;
	}

	@Override
	public boolean isUnsigned() {
		return (fModifiers & IS_UNSIGNED) != 0;
	}

	@Override
	public boolean isShort() {
		return (fModifiers & IS_SHORT) != 0;
	}

	@Override
	public boolean isLong() {
		return (fModifiers & IS_LONG) != 0;
	}

	@Override
	public boolean isLongLong() {
		return (fModifiers & IS_LONG_LONG) != 0;
	}

	@Override
	public boolean isSameType(IType obj) {
		if (obj == this)
			return true;
		if (obj instanceof ITypedef)
			return obj.isSameType(this);

		if (!(obj instanceof ICBasicType))
			return false;

		ICBasicType cObj = (ICBasicType) obj;

		if (fKind != cObj.getKind())
			return false;

		if (fKind == Kind.eInt) {
			// Signed int and int are equivalent
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
			// Not going to happen
		}
		return t;
	}

	@Override
	@Deprecated
	public IASTExpression getValue() {
		return value;
	}

	@Override
	public boolean isComplex() {
		return (fModifiers & IS_COMPLEX) != 0;
	}

	@Override
	public boolean isImaginary() {
		return (fModifiers & IS_IMAGINARY) != 0;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		final int kind = getKind().ordinal();
		final int shiftedKind = kind * ITypeMarshalBuffer.FIRST_FLAG;
		final int modifiers = getModifiers();
		if (modifiers == 0) {
			buffer.putShort((short) (ITypeMarshalBuffer.BASIC_TYPE | shiftedKind));
		} else {
			buffer.putShort((short) (ITypeMarshalBuffer.BASIC_TYPE | shiftedKind | ITypeMarshalBuffer.LAST_FLAG));
			buffer.putByte((byte) modifiers);
		}
	}

	public static IType unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		final boolean haveModifiers = (firstBytes & ITypeMarshalBuffer.LAST_FLAG) != 0;
		int modifiers = 0;
		int kind = (firstBytes & (ITypeMarshalBuffer.LAST_FLAG - 1)) / ITypeMarshalBuffer.FIRST_FLAG;
		if (haveModifiers) {
			modifiers = buffer.getByte();
		}
		return new CBasicType(Kind.values()[kind], modifiers);
	}

	@Override
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
		case eNullPtr:
		case eInt128:
		case eFloat128:
		case eDecimal32:
		case eDecimal64:
		case eDecimal128:
			// Null pointer type cannot be expressed wit ha simple decl specifier.
			break;
		}
		return t_unspecified;
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}
}
