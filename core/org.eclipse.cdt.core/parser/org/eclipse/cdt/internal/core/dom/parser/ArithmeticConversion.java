/*******************************************************************************
 * Copyright (c) 2009, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Nathan Ridge
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.internal.core.dom.parser.SizeofCalculator.SizeAndAlignment;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Arithmetic conversions as required to compute the type of unary or binary expressions.
 */
public abstract class ArithmeticConversion {
	private static final int DOMAIN_FLAGS = IBasicType.IS_IMAGINARY | IBasicType.IS_COMPLEX;
	
	private enum Domain {
		eReal(0), 
		eImaginary(IBasicType.IS_IMAGINARY), 
		eComplex(IBasicType.IS_COMPLEX);
	
		private final int fModifier;

		private Domain(int modifier) {
			fModifier= modifier;
		}
		
		int getModifier() {
			return fModifier;
		}
	}
	private enum Rank {eInt, eLong, eLongLong}
	
	protected abstract IBasicType createBasicType(IBasicType.Kind kind, int modifiers);
	
	/**
	 * Performs an arithmetic conversion as described in section 6.3.1.8 of the C99 standard,
	 * or 5.0.9 of C++ standard
	 */
	public final IType convertOperandTypes(int operator, IType op1, IType op2) {
		op1 = SemanticUtil.getNestedType(op1, TDEF);
		op2 = SemanticUtil.getNestedType(op2, TDEF);
		if (!isArithmeticOrUnscopedEnum(op1) || !isArithmeticOrUnscopedEnum(op2)) {
			return null;
		}
		switch (operator) {
		// Multiplicative operators
		case IASTBinaryExpression.op_divide:
		case IASTBinaryExpression.op_modulo:
		case IASTBinaryExpression.op_multiply:
		// Additive operators
		case IASTBinaryExpression.op_minus:
		case IASTBinaryExpression.op_plus:
		// Bitwise operators
		case IASTBinaryExpression.op_binaryAnd:
		case IASTBinaryExpression.op_binaryOr:
		case IASTBinaryExpression.op_binaryXor:
		// Gcc's minimum/maximum operators
		case IASTBinaryExpression.op_max:
		case IASTBinaryExpression.op_min:
			return convert(op1, op2);

		case IASTBinaryExpression.op_shiftLeft:
		case IASTBinaryExpression.op_shiftRight:
			return promote(op1, getDomain(op1));
			
		default:
			return null;
		}
	}
	
	public final IType promoteType(IType type) {
		if (!isIntegralOrUnscopedEnum(type))
			return null;
		
		return promote(type, getDomain(type));
	}
	
	private boolean isArithmeticOrUnscopedEnum(IType op1) {
		if (op1 instanceof IBasicType)  {
			final Kind kind = ((IBasicType) op1).getKind();
			switch (kind) {
			case eUnspecified:
			case eVoid:
			case eNullPtr:
				return false;
			default:
				return true;
			}
		}
		if (op1 instanceof IEnumeration) {
			if (op1 instanceof ICPPEnumeration && ((ICPPEnumeration) op1).isScoped())
				return false;
			return true;
		}
		return false;
	}

	private boolean isIntegralOrUnscopedEnum(IType op1) {
		if (op1 instanceof IEnumeration)
			return true;
		
		if (op1 instanceof IBasicType) {
			Kind kind= ((IBasicType) op1).getKind();
			switch(kind) {
			case eBoolean:
			case eChar:
			case eChar16:
			case eChar32:
			case eInt:
			case eInt128:
			case eWChar:
				return true;
				
			case eDouble:
			case eFloat:
			case eFloat128:
			case eDecimal32:
			case eDecimal64:
			case eDecimal128:
			case eUnspecified:
			case eVoid:
			case eNullPtr:
				return false;
			}
		}
		return false;
	}

	private final IType convert(IType type1, IType type2) {
		Domain domain= getDomain(type1, type2);
		
		// If either type is a long double, return that type
		if (isLongDouble(type1)) {
			return adjustDomain((IBasicType) type1, domain);
		}
		if (isLongDouble(type2)) {
			return adjustDomain((IBasicType) type2, domain);
		}
		
		// Else if either type is a double return that type
		if (isDouble(type1)) {
			return adjustDomain((IBasicType) type1, domain);
		}
		if (isDouble(type2)) {
			return adjustDomain((IBasicType) type2, domain);
		}
		
		// Else if either type is a float return that type
		if (isFloat(type1)) {
			return adjustDomain((IBasicType) type1, domain);
		}
		if (isFloat(type2)) {
			return adjustDomain((IBasicType) type2, domain);
		}
		
		// We're dealing with integer types so perform integer promotion
		IBasicType btype1 = promote(type1, domain);
		IBasicType btype2 = promote(type2, domain);
		
		if (btype1.isSameType(btype2)) {
			return btype1;
		}
    	
		if (btype1.isUnsigned() == btype2.isUnsigned()) {
			return getIntegerRank(btype1).ordinal() >= getIntegerRank(btype2).ordinal() ? btype1 : btype2;
		} 
		
		IBasicType unsignedType, signedType;
		if (btype1.isUnsigned()) {
			unsignedType= btype1;
			signedType= btype2;
		} else {
			unsignedType= btype2;
			signedType= btype1;
		}

		final Rank signedRank= getIntegerRank(signedType);
		final Rank unsignedRank= getIntegerRank(unsignedType);
		
		// same rank -> use unsigned
		if (unsignedRank.ordinal() >= signedRank.ordinal()) {
			return unsignedType;
		}
		
		// The signed has the higher rank.
		if (signedRank.ordinal() > unsignedRank.ordinal()) {
			return signedType;
		}
		
		return createBasicType(signedType.getKind(),
				changeModifier(signedType.getModifiers(), IBasicType.IS_SIGNED, IBasicType.IS_UNSIGNED));
	}
	
	private IBasicType promote(IType type, Domain domain) {
		if (type instanceof IEnumeration) {
			IType fixedType= null;
			if (type instanceof ICPPEnumeration) {
				fixedType= ((ICPPEnumeration) type).getFixedType();
			}
			if (fixedType == null) 
				return createBasicType(Kind.eInt, domain.getModifier() | getEnumIntTypeModifiers((IEnumeration) type));
			type= fixedType;
		} 
		
		if (type instanceof IBasicType) {
			final IBasicType bt = (IBasicType) type;
			final Kind kind = bt.getKind();
			switch (kind) {
			case eBoolean:
			case eChar:
			case eWChar:
			case eChar16:
				return createBasicType(Kind.eInt, domain.getModifier());

			case eChar32:
				// Assuming 32 bits.
				return createBasicType(Kind.eInt, domain.getModifier() | IBasicType.IS_UNSIGNED);

			case eInt:
				if (bt.isShort())
					return createBasicType(Kind.eInt, domain.getModifier());
				return adjustDomain(bt, domain);

			case eInt128:
				return createBasicType(Kind.eInt128, domain.getModifier() | IBasicType.IS_UNSIGNED);

			case eVoid:
			case eUnspecified:
			case eDouble:
			case eFloat:
			case eFloat128:
			case eDecimal32:
			case eDecimal64:
			case eDecimal128:
			case eNullPtr:
				assert false;
			}
		}		
		return createBasicType(Kind.eInt, domain.getModifier());
	}

	private Domain getDomain(IType type1, IType type2) {
		Domain d1= getDomain(type1);
		Domain d2= getDomain(type2);
		if (d1 == d2)
			return d1;
		return Domain.eComplex;
	}

	private Domain getDomain(IType type) {
		if (type instanceof IBasicType) {
			IBasicType bt= (IBasicType) type;
			if (bt.isComplex())
				return Domain.eComplex;
			if (bt.isImaginary())
				return Domain.eImaginary;
		}
		return Domain.eReal;
	}

	private IBasicType adjustDomain(IBasicType t, Domain d) {
		Domain myDomain= getDomain(t);
		if (myDomain == d)
			return t;
		
		return createBasicType(t.getKind(), changeModifier(t.getModifiers(), DOMAIN_FLAGS, d.getModifier()));
	}
	
	private int changeModifier(int modifiers, int remove, int add) {
		return (modifiers & ~remove) | add;
	}

	private Rank getIntegerRank(IBasicType type) {
		if (type.getKind() == Kind.eInt128)
			return Rank.eLongLong;
		assert type.getKind() == Kind.eInt;
		if (type.isLongLong())
			return Rank.eLongLong;
		if (type.isLong())
			return Rank.eLong;
		return Rank.eInt;
	}

	private boolean isLongDouble(IType type) {
		if (type instanceof IBasicType) {
			final IBasicType bt= (IBasicType) type;
			return bt.isLong() && bt.getKind() == Kind.eDouble || bt.getKind() == Kind.eFloat128 ||
					bt.getKind() == Kind.eDecimal128;
		}
		return false;
	}

	private static boolean isDouble(IType type) {
		if (type instanceof IBasicType) {
			final IBasicType bt= (IBasicType) type;
			return bt.getKind() == Kind.eDouble || bt.getKind() == Kind.eDecimal64;
		}
		return false;
	}

	private static boolean isFloat(IType type) {
		if (type instanceof IBasicType) {
			final IBasicType bt= (IBasicType) type;
			return bt.getKind() == Kind.eFloat || bt.getKind() == Kind.eDecimal32;
		}
		return false;
	}
	
	public static int getEnumIntTypeModifiers(IEnumeration enumeration) {
		final long minValue = enumeration.getMinValue();
		final long maxValue = enumeration.getMaxValue();
		// TODO(sprigogin): Use values of __INT_MAX__ and __LONG_MAX__ macros
		if (minValue >= Integer.MIN_VALUE && maxValue <= Integer.MAX_VALUE) {
			return 0;
		} else if (minValue >= 0 && maxValue <= 0xFFFFFFFFL) {
			return IBasicType.IS_UNSIGNED;
		} else if (minValue >= Long.MIN_VALUE && maxValue <= Long.MAX_VALUE) {
			return IBasicType.IS_LONG;
		} else {
			// This branch is unreachable due to limitations of Java long type. 
			return IBasicType.IS_UNSIGNED | IBasicType.IS_LONG;
		}
	}

	public static boolean fitsIntoType(IBasicType basicTarget, long n) {
		final Kind kind = basicTarget.getKind();
		switch (kind) {
		case eInt:
			if (!basicTarget.isUnsigned()) {
				if (basicTarget.isShort()) {
					return Short.MIN_VALUE <= n && n <= Short.MAX_VALUE;
				}
				// Can't represent long longs with Java longs.
				if (basicTarget.isLong() || basicTarget.isLongLong()) {
					return true;
				}
				return Integer.MIN_VALUE <= n && n <= Integer.MAX_VALUE;
			}
			if (n < 0)
				return false;
			
			if (basicTarget.isShort()) {
				return n < (Short.MAX_VALUE + 1L) * 2;
			}
			// Can't represent long longs with Java longs.
			if (basicTarget.isLong() || basicTarget.isLongLong()) {
				return true;
			}
			return n < (Integer.MAX_VALUE + 1L) * 2;

		case eFloat:
			float f= n;
			return (long) f == n;
		
		case eDouble:
			double d= n;
			return (long) d == n;
			
		default:
			return false;
		}
	}

	/**
	 * Makes a best-effort guess at the sizeof() of an integral type.  
	 */
	private static long getApproximateSize(IBasicType type) {
		switch (type.getKind()) {
			case eChar: return 1;
			case eWChar: return 2;
			case eInt: 
				// Note: we return 6 for long so that both long -> int 
				//       and long long -> long conversions are reported 
				//       as narrowing, to be on the safe side.
				return type.isShort() ? 2 
				     : type.isLong() ? 6
				     : type.isLongLong() ? 8 
				     : 4;
			case eBoolean: return 1;
			case eChar16: return 2;
			case eChar32: return 4;
			case eInt128: return 16;
			default: return 0;  // shouldn't happen
		}
	}

	/**
	 * Checks whether a target integral type can represent all values of a source integral type.
	 *
	 * @param target the target integral type
	 * @param source the source integral type
	 * @param point point for sizeof lookup
	 * @return whether the target integral type can represent all values of the source integral type
	 */
	public static boolean fitsIntoType(IBasicType target, IBasicType source, IASTNode point) {
		// A boolean cannot represent any other type.
		if (target.getKind() == Kind.eBoolean && source.getKind() != Kind.eBoolean)
			return false;
		// A boolean can be represented by any other integral type.
		if (source.getKind() == Kind.eBoolean)
			return true;

		// If the source is signed, it might be negative, so an unsigned target cannot represent it.
		if (!source.isUnsigned() && target.isUnsigned())
			return false;

		// Otherwise, go by the size and signedness of the type.
		SizeAndAlignment sourceSizeAndAlignment = SizeofCalculator.getSizeAndAlignment(source, point); 
		SizeAndAlignment targetSizeAndAlignment = SizeofCalculator.getSizeAndAlignment(target, point);
		long sizeofSource = sourceSizeAndAlignment == null ? getApproximateSize(source) : sourceSizeAndAlignment.size;
		long sizeofTarget = targetSizeAndAlignment == null ? getApproximateSize(target) : targetSizeAndAlignment.size;

		if (sizeofSource == sizeofTarget) {
			return target.isUnsigned() == source.isUnsigned();
		} else {
			return sizeofSource < sizeofTarget;
		}
	}
}
