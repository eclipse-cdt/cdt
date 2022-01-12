/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.ArithmeticConversion;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;

/**
 * The cost of an implicit conversion sequence.
 *
 * See [over.best.ics] 13.3.3.1.
 */
public class Cost {
	public enum DeferredUDC {
		NONE, COPY_INIT_OF_CLASS, INIT_BY_CONVERSION, LIST_INIT_OF_CLASS, DIRECT_LIST_INIT_OF_CLASS
	}

	public enum Rank {
		IDENTITY, PROMOTION, CONVERSION, CONVERSION_PTR_BOOL, USER_DEFINED_CONVERSION, ELLIPSIS_CONVERSION, NO_MATCH
	}

	enum ReferenceBinding {
		RVALUE_REF_BINDS_RVALUE, LVALUE_REF, OTHER_REF, NO_REF
	}

	public static final Cost NO_CONVERSION = new Cost(null, null, Rank.NO_MATCH) {
		@Override
		public void setRank(Rank rank) {
			assert false;
		}

		@Override
		public void setReferenceBinding(ReferenceBinding binding) {
			assert false;
		}

		@Override
		public void setAmbiguousUDC(boolean val) {
			assert false;
		}

		@Override
		public void setDeferredUDC(DeferredUDC val) {
			assert false;
		}

		@Override
		public void setInheritanceDistance(int inheritanceDistance) {
			assert false;
		}

		@Override
		public void setQualificationAdjustment(int adjustment) {
			assert false;
		}

		@Override
		public void setUserDefinedConversion(ICPPMethod conv) {
			assert false;
		}

		@Override
		public void setCouldNarrow() {
			assert false;
		}

		@Override
		public void setSelectedFunction(ICPPFunction function) {
			assert false;
		}
	};

	IType source;
	IType target;

	private Rank fRank;
	private Rank fSecondStandardConversionRank;
	private boolean fAmbiguousUDC;
	private DeferredUDC fDeferredUDC = DeferredUDC.NONE;
	private int fQualificationAdjustments;
	private int fInheritanceDistance;
	private boolean fImpliedObject;
	private ICPPFunction fUserDefinedConversion;
	private ReferenceBinding fReferenceBinding;
	private boolean fCouldNarrow;

	// For a list-initialization sequence, 'target' is not always the original
	// target type. Specifically, for an original target type of
	// std::initializer_list<T> or array of T, 'target' will be T, but we need
	// to know the original target as well so we store it here.
	// This will be null iff. this is not a list-initialization sequence.
	private IType fListInitializationTarget;

	private ICPPFunction fSelectedFunction; // For targeted functions

	public Cost(IType s, IType t, Rank rank) {
		source = s;
		target = t;
		fRank = rank;
		fReferenceBinding = ReferenceBinding.NO_REF;
	}

	public final Rank getRank() {
		return fRank;
	}

	public final boolean converts() {
		return fRank != Rank.NO_MATCH;
	}

	public void setRank(Rank rank) {
		fRank = rank;
	}

	public ReferenceBinding getReferenceBinding() {
		return fReferenceBinding;
	}

	public void setReferenceBinding(ReferenceBinding binding) {
		fReferenceBinding = binding;
	}

	public boolean isAmbiguousUDC() {
		return fAmbiguousUDC;
	}

	public void setAmbiguousUDC(boolean val) {
		fAmbiguousUDC = val;
	}

	public DeferredUDC isDeferredUDC() {
		return fDeferredUDC;
	}

	public void setDeferredUDC(DeferredUDC udc) {
		fDeferredUDC = udc;
	}

	public int getInheritanceDistance() {
		return fInheritanceDistance;
	}

	public void setInheritanceDistance(int inheritanceDistance) {
		fInheritanceDistance = inheritanceDistance;
	}

	public void setQualificationAdjustment(int adjustment) {
		fQualificationAdjustments = adjustment;
	}

	/**
	 * Converts the cost for the second standard conversion into the overall cost for the
	 * implicit conversion sequence.
	 */
	public void setUserDefinedConversion(ICPPMethod conv) {
		fUserDefinedConversion = conv;
		fSecondStandardConversionRank = fRank;
		fRank = Rank.USER_DEFINED_CONVERSION;
		fCouldNarrow = false;
	}

	/**
	 * Returns an integer &lt 0 if other cost is <code>null</code>, or this cost is smaller than the other cost,
	 *        0 if this cost is equal to the other cost,
	 *        an integer &gt 0 if this cost is larger than the other cost.
	 */
	public int compareTo(Cost other) {
		if (other == null)
			return -1;

		// cannot compare costs with deferred user defined conversions
		assert fDeferredUDC == DeferredUDC.NONE && other.fDeferredUDC == DeferredUDC.NONE;

		// 7.3.3.13 (using declarations in classes):
		// for overload resolution the implicit this pointer
		// is treated as if it were a pointer to the derived class
		final boolean ignoreInheritanceDist = fImpliedObject && other.fImpliedObject;
		Rank rank = fRank;
		Rank otherRank = other.fRank;
		if (ignoreInheritanceDist) {
			if (rank == Rank.CONVERSION)
				rank = Rank.IDENTITY;
			if (otherRank == Rank.CONVERSION)
				otherRank = Rank.IDENTITY;
		}

		int cmp = rank.compareTo(otherRank);
		if (cmp != 0)
			return cmp;

		// [over.ics.rank] p3.3:
		// List-initialization sequence L1 is a better conversion sequence than
		// list-initialization sequence L2 if
		if (fListInitializationTarget != null && other.fListInitializationTarget != null) {
			//   - L1 converts to std::initializer_list<X> for some X and L2 does not,
			//     or if not that,
			IType initListType = Conversions.getInitListType(fListInitializationTarget);
			IType otherInitListType = Conversions.getInitListType(other.fListInitializationTarget);
			if (initListType != null && otherInitListType == null) {
				return -1;
			} else if (initListType == null && otherInitListType != null) {
				return 1;
			}

			//   - L1 converts to type "array of N1 T", L2 converts to type "array of
			//     N2 T", and N1 is smaller than N2
			if (fListInitializationTarget instanceof IArrayType
					&& other.fListInitializationTarget instanceof IArrayType) {
				IArrayType arrayType = (IArrayType) fListInitializationTarget;
				IArrayType otherArrayType = (IArrayType) other.fListInitializationTarget;
				if (arrayType.getType().isSameType(otherArrayType.getType())) {
					Number size = arrayType.getSize().numberValue();
					Number otherSize = otherArrayType.getSize().numberValue();
					if (size != null && otherSize != null) {
						Long a = size.longValue();
						Long b = otherSize.longValue();
						return a.compareTo(b);
					}
				}
			}
		}

		// rank is equal
		if (rank == Rank.USER_DEFINED_CONVERSION) {
			// 13.3.3.1.10
			if (isAmbiguousUDC() || other.isAmbiguousUDC())
				return 0;

			if (fUserDefinedConversion != other.fUserDefinedConversion) {
				if (fUserDefinedConversion == null || !fUserDefinedConversion.equals(other.fUserDefinedConversion))
					return 0;
			}
			cmp = fSecondStandardConversionRank.compareTo(other.fSecondStandardConversionRank);
			if (cmp != 0)
				return cmp;
		}

		if (!ignoreInheritanceDist) {
			cmp = fInheritanceDistance - other.fInheritanceDistance;
			if (cmp != 0)
				return cmp;
		}

		if (fReferenceBinding == ReferenceBinding.LVALUE_REF) {
			if (other.fReferenceBinding == ReferenceBinding.RVALUE_REF_BINDS_RVALUE)
				return 1;
		} else if (fReferenceBinding == ReferenceBinding.RVALUE_REF_BINDS_RVALUE) {
			if (other.fReferenceBinding == ReferenceBinding.LVALUE_REF)
				return -1;
		}

		// Top level cv-qualifiers are compared only for reference bindings.
		int qdiff = fQualificationAdjustments ^ other.fQualificationAdjustments;
		if (fReferenceBinding == ReferenceBinding.NO_REF || other.fReferenceBinding == ReferenceBinding.NO_REF)
			qdiff &= ~7;

		if (qdiff != 0) {
			if ((fQualificationAdjustments & qdiff) == 0)
				return -1;
			if ((other.fQualificationAdjustments & qdiff) == 0)
				return 1;
		}

		return 0;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		String comma = "";
		buf.append(fRank).append('[');
		if (fQualificationAdjustments != 0) {
			buf.append(comma).append("qualification=").append(fQualificationAdjustments);
			comma = ", ";
		}
		if (fInheritanceDistance != 0) {
			buf.append(comma).append("inheritance=").append(fInheritanceDistance);
			comma = ", ";
		}
		if (fDeferredUDC != DeferredUDC.NONE) {
			buf.append(comma).append(fDeferredUDC);
			comma = ", ";
		}
		if (fAmbiguousUDC) {
			buf.append(comma).append("ambiguous UDC");
			comma = ", ";
		}
		if (fSecondStandardConversionRank != null) {
			buf.append(comma).append("2ndConvRank=").append(fSecondStandardConversionRank);
		}
		buf.append(']');
		return buf.toString();
	}

	public boolean isNarrowingConversion() {
		if (!fCouldNarrow)
			return false;

		// Determine whether this is a narrowing conversion, according to 8.5.4/7 (dcl.list.init).

		if (!(target instanceof ICPPBasicType))
			return false;
		ICPPBasicType basicTarget = (ICPPBasicType) target;

		// Deal with an enumeration source type.
		// If it has a fixed underlying type, treat it as if it were that underlying type.
		// If not, check whether the target type can represent its min and max values.
		CPPBasicType basicSource = null;
		if (source instanceof CPPBasicType) {
			basicSource = (CPPBasicType) source;
		} else if (source instanceof IEnumeration) {
			IEnumeration enumSource = (IEnumeration) source;
			if (enumSource instanceof ICPPEnumeration) {
				IType fixedType = ((ICPPEnumeration) enumSource).getFixedType();
				if (fixedType instanceof CPPBasicType) {
					basicSource = (CPPBasicType) fixedType;
				}
			}
			if (basicSource == null) { // C enumeration or no fixed type
				return !ArithmeticConversion.fitsIntoType(basicTarget, enumSource.getMinValue())
						|| !ArithmeticConversion.fitsIntoType(basicTarget, enumSource.getMaxValue());
			}
		}

		if (basicSource == null)
			return false;

		// The standard provides for an exception in some cases where, based on the types only,
		// a conversion would be narrowing, but the source expression is a constant-expression
		// and its value is exactly representable by the target type.
		boolean constantExprExceptionApplies = false;

		if (BuiltinOperators.isFloatingPoint(basicSource) && BuiltinOperators.isIntegral(basicTarget)) {
			// From a floating-point type to an integer type
			return true;
		} else if (basicSource.getKind() == Kind.eDouble && (basicTarget.getKind() == Kind.eFloat
				|| (basicTarget.getKind() == Kind.eDouble && !basicTarget.isLong() && basicSource.isLong()))) {
			// From long double to double or float, or from double to float
			constantExprExceptionApplies = true;
		} else if (BuiltinOperators.isIntegral(basicSource) && BuiltinOperators.isFloatingPoint(basicTarget)) {
			// From an integer type or unscoped enumeration type to a floating-point type
			constantExprExceptionApplies = true;
		} else if (BuiltinOperators.isIntegral(basicSource) && BuiltinOperators.isIntegral(basicTarget)
				&& !ArithmeticConversion.fitsIntoType(basicTarget, basicSource)) {
			// From an integer type or unscoped enumeration type to an integer type that
			// cannot represent all the values of the original type
			constantExprExceptionApplies = true;
		}

		if (constantExprExceptionApplies) {
			Long val = basicSource.getAssociatedNumericalValue();
			return val == null || !ArithmeticConversion.fitsIntoType(basicTarget, val.longValue());
		}

		return false;
	}

	public void setCouldNarrow() {
		fCouldNarrow = true;
	}

	public ICPPFunction getUserDefinedConversion() {
		return fUserDefinedConversion;
	}

	/**
	 * Stores a selected function. Used when resolving targeted functions.
	 */
	public void setSelectedFunction(ICPPFunction function) {
		fSelectedFunction = function;
	}

	public ICPPFunction getSelectedFunction() {
		return fSelectedFunction;
	}

	public void setImpliedObject() {
		fImpliedObject = true;
	}

	public void setListInitializationTarget(IType target) {
		fListInitializationTarget = target;
	}
}
