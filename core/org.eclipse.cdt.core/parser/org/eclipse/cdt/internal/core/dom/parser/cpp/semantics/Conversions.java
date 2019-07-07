/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.valueCategoryFromReturnType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.ALLCVQ;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.COND_TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.addQualifiers;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.calculateInheritanceDepth;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getCVQualifier;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.isVoidType;

import java.util.Collections;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ArithmeticConversion;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Cost.DeferredUDC;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Cost.Rank;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Cost.ReferenceBinding;

/**
 * Routines for calculating the cost of conversions.
 */
public class Conversions {
	public enum UDCMode {
		ALLOWED, FORBIDDEN, DEFER
	}

	public enum Context {
		ORDINARY, IMPLICIT_OBJECT_FOR_METHOD_WITHOUT_REF_QUALIFIER, IMPLICIT_OBJECT_FOR_METHOD_WITH_REF_QUALIFIER,
		FIRST_PARAM_OF_DIRECT_COPY_CTOR, REQUIRE_DIRECT_BINDING
	}

	private static final char[] INITIALIZER_LIST_NAME = "initializer_list".toCharArray(); //$NON-NLS-1$
	private static final char[] STD_NAME = "std".toCharArray(); //$NON-NLS-1$

	/**
	 * Computes the cost of an implicit conversion sequence [over.best.ics] 13.3.3.1.
	 * The semantics of the initialization is explained in 8.5-16.
	 *
	 * @param target the target (parameter) type
	 * @param exprType the source (argument) type
	 * @param valueCat value category of the expression
	 * @return the cost of converting from source to target
	 * @throws DOMException
	 */
	public static Cost checkImplicitConversionSequence(IType target, IType exprType, ValueCategory valueCat,
			UDCMode udc, Context ctx) throws DOMException {
		final boolean isImpliedObject = ctx == Context.IMPLICIT_OBJECT_FOR_METHOD_WITHOUT_REF_QUALIFIER
				|| ctx == Context.IMPLICIT_OBJECT_FOR_METHOD_WITH_REF_QUALIFIER;
		if (isImpliedObject)
			udc = UDCMode.FORBIDDEN;

		target = getNestedType(target, TDEF);
		exprType = getNestedType(exprType, TDEF | REF);
		final IType cv1T1 = getNestedType(target, TDEF | REF);
		final IType T1 = getNestedType(cv1T1, TDEF | REF | ALLCVQ);

		if (target instanceof ICPPReferenceType) {
			ReferenceBinding refBindingType = ReferenceBinding.OTHER_REF;
			// [8.5.3-5] initialization of a reference
			final boolean isLValueRef = !((ICPPReferenceType) target).isRValueReference();
			final IType cv2T2 = exprType;
			final IType T2 = getNestedType(cv2T2, TDEF | REF | ALLCVQ);

			refBindingType = isLValueRef ? ReferenceBinding.LVALUE_REF : ReferenceBinding.RVALUE_REF_BINDS_RVALUE;

			if (exprType instanceof InitializerListType) {
				if (isLValueRef && getCVQualifier(cv1T1) != CVQualifier.CONST)
					return Cost.NO_CONVERSION;

				Cost cost = listInitializationSequence(((InitializerListType) exprType).getEvaluation(), T1, udc,
						false);
				if (cost.converts()) {
					cost.setReferenceBinding(refBindingType);
				}
				return cost;
			}

			// If the reference is an lvalue reference and ...
			if (isLValueRef) {
				// ... the initializer expression is an lvalue (but is not a bit field)
				// [for overload resolution bit-fields are treated the same, error if selected as best match]
				if (valueCat == LVALUE || ctx == Context.IMPLICIT_OBJECT_FOR_METHOD_WITHOUT_REF_QUALIFIER) {
					// 13.3.3.5: For non-static member functions declared without a ref-qualifier,
					// an additional rule applies:
					//   — even if the implicit object parameter is not const-qualified, an rvalue can be
					//     bound to the parameter as long as in all other respects the argument can be
					//     converted to the type of the implicit object parameter.
					//     [Note: The fact that such an argument is an rvalue does not affect the ranking of
					//     implicit conversion sequences (13.3.3.2). — end note]
					if (valueCat != LVALUE)
						refBindingType = ReferenceBinding.RVALUE_REF_BINDS_RVALUE;
					// ... and "cv1 T1" is reference-compatible with "cv2 T2"
					Cost cost = isReferenceCompatible(cv1T1, cv2T2, isImpliedObject);
					if (cost != null) {
						cost.setReferenceBinding(refBindingType);
						return cost;
					}
				}
				// ... or has a class type (i.e., T2 is a class type), where T1 is not reference-related to T2, and can be
				// implicitly converted to an lvalue of type 'cv3 T3', where 'cv1 T1' is reference-compatible with
				// 'cv3 T3' (this conversion is selected by enumerating the applicable conversion functions (13.3.1.6)
				// and choosing the best one through overload resolution (13.3)),
				if (T2 instanceof ICPPClassType && udc != UDCMode.FORBIDDEN && isReferenceRelated(T1, T2) < 0) {
					Cost cost = initializationByConversionForDirectReference(cv1T1, cv2T2, (ICPPClassType) T2, true,
							false, ctx);
					if (cost != null) {
						cost.setReferenceBinding(refBindingType);
						return cost;
					}
				}
			}

			// Otherwise, the reference shall be an lvalue reference to a non-volatile const type (i.e., cv1
			// shall be const), or the reference shall be an rvalue reference.
			if (isLValueRef && getCVQualifier(cv1T1) != CVQualifier.CONST) {
				return Cost.NO_CONVERSION;
			}

			// If the initializer expression is an xvalue, class prvalue, array prvalue, or function lvalue
			// and 'cv1 T1' is reference-compatible with 'cv2 T2', then the reference is bound to the value
			// of the initializer expression (or the appropriate base class subobject).
			if (valueCat == ValueCategory.XVALUE
					|| (valueCat == ValueCategory.PRVALUE && (T2 instanceof ICPPClassType || T2 instanceof IArrayType))
					|| (valueCat == ValueCategory.LVALUE && T2 instanceof ICPPFunctionType)) {
				Cost cost = isReferenceCompatible(cv1T1, cv2T2, isImpliedObject);
				if (cost != null) {
					cost.setReferenceBinding(refBindingType);
					return cost;
				}
			}

			// If the initializer expression has class type (i.e. T2 is a class type), where T1 is not
			// reference-related to T2, and can be implicitly converted to an xvalue, class prvalue,
			// or function lvalue of type 'cv3 T3', where 'cv1 T1' is reference-compatible with 'cv3 T3',
			// then the reference is bound to the result of the conversion (or the appropriate base class
			// subobject). If the reference is an rvalue reference and the second standard conversion
			// sequence of the user-defined conversion sequence includes an lvalue-to-rvalue
			// conversion, the program is ill-formed [this is why we pass illFormedIfLValue = true].
			if (T2 instanceof ICPPClassType) {
				if (udc != UDCMode.FORBIDDEN && isReferenceRelated(T1, T2) < 0) {
					Cost cost = initializationByConversionForDirectReference(cv1T1, cv2T2, (ICPPClassType) T2, false,
							true, ctx);
					if (cost != null) {
						if (cost != Cost.NO_CONVERSION) {
							cost.setReferenceBinding(refBindingType);
						}
						return cost;
					}
				}
			}

			// Otherwise, a temporary of type 'cv1 T1' is created and initialized from the initializer
			// expression using the rules for a non-reference copy initialization (8.5). The reference is then
			// bound to the temporary.

			// 13.3.3.1.7 no temporary object when converting the implicit object parameter
			if (!isImpliedObject && ctx != Context.REQUIRE_DIRECT_BINDING) {
				Cost cost = nonReferenceConversion(valueCat, cv2T2, T1, udc);
				if (cost.converts()) {
					cost.setReferenceBinding(refBindingType);
				}
				boolean referenceRelated = isReferenceRelated(T1, T2) >= 0;
				// If T1 is reference-related to T2, cv1 shall be the same cv-qualification as,
				// or greater cv-qualification than, cv2.
				if (referenceRelated && compareQualifications(cv1T1, cv2T2) < 0) {
					return Cost.NO_CONVERSION;
				}
				// if T1 is reference-related to T2 and the reference is an rvalue reference,
				// the initializer expression shall not be an lvalue.
				if (referenceRelated && !isLValueRef && valueCat == ValueCategory.LVALUE) {
					return Cost.NO_CONVERSION;
				}
				return cost;
			}
			return Cost.NO_CONVERSION;
		}

		// Non-reference binding
		return nonReferenceConversion(valueCat, exprType, T1, udc);
	}

	/**
	 * C++0x: 13.3.1.6 Initialization by conversion function for direct reference binding
	 * @param needLValue don't consider conversion functions that return rvalue references
	 * @param illFormedIfLValue make the conversion ill-formed (by returning Cost.NO_CONVERSION)
	 *                          if the best match is a conversion function that returns an
	 *                          lvalue reference
	 * Note that there's a difference between returning null and returning Cost.NO_CONVERSION:
	 * in the former case, the caller will continue trying other conversion methods.
	 */
	private static Cost initializationByConversionForDirectReference(final IType cv1T1, final IType cv2T2,
			final ICPPClassType T2, boolean needLValue, boolean illFormedIfLValue, Context ctx) throws DOMException {
		ICPPMethod[] fcns = SemanticUtil.getConversionOperators(T2);
		Cost operatorCost = null;
		FunctionCost bestUdcCost = null;
		boolean ambiguousConversionOperator = false;
		if (fcns.length > 0 && !(fcns[0] instanceof IProblemBinding)) {
			for (final ICPPMethod op : fcns) {
				// Note: the special case of initializing a temporary to be bound to the first parameter
				// of a copy constructor called with a single argument in the context of direct-initialization
				// is (more naturally) handled here rather than in copyInitializationOfClass().
				if (op.isExplicit() && ctx != Context.FIRST_PARAM_OF_DIRECT_COPY_CTOR)
					continue;
				final ICPPFunctionType ft = op.getType();
				IType t = getNestedType(ft.getReturnType(), TDEF);
				final boolean isLValueRef = t instanceof ICPPReferenceType
						&& !((ICPPReferenceType) t).isRValueReference();
				if (needLValue && !isLValueRef) {
					continue;
				}
				IType implicitParameterType = CPPSemantics.getImplicitParameterType(op);
				Cost udcCost = isReferenceCompatible(getNestedType(implicitParameterType, TDEF | REF), cv2T2, true); // expression type to implicit object type
				if (udcCost != null) {
					// Make sure top-level cv-qualifiers are compared
					udcCost.setReferenceBinding(ReferenceBinding.LVALUE_REF);
					FunctionCost udcFuncCost = new FunctionCost(op, udcCost);
					int cmp = udcFuncCost.compareTo(null, bestUdcCost, 1);
					if (cmp <= 0) {
						Cost cost = isReferenceCompatible(cv1T1, getNestedType(t, TDEF | REF), false); // converted to target
						if (cost != null) {
							bestUdcCost = udcFuncCost;
							ambiguousConversionOperator = cmp == 0;
							operatorCost = cost;
							operatorCost.setUserDefinedConversion(op);

							if (illFormedIfLValue && isLValueRef) {
								operatorCost = Cost.NO_CONVERSION;
							}
						}
					}
				}
			}
		}

		if (operatorCost != null && !ambiguousConversionOperator) {
			return operatorCost;
		}
		return null;
	}

	/**
	 * 8.5-16
	 */
	private static Cost nonReferenceConversion(ValueCategory valueCat, IType source, IType target, UDCMode udc)
			throws DOMException {
		if (source instanceof InitializerListType) {
			return listInitializationSequence(((InitializerListType) source).getEvaluation(), target, udc, false);
		}

		IType uqTarget = SemanticUtil.getNestedType(target, TDEF | REF | CVTYPE);
		IType uqSource = SemanticUtil.getNestedType(source, TDEF | REF | CVTYPE);
		if (uqTarget instanceof ICPPClassType) {
			if (uqSource instanceof ICPPClassType) {
				// 13.3.3.1-6 Conceptual derived to base conversion
				int depth = calculateInheritanceDepth(uqSource, uqTarget);
				if (depth >= 0) {
					if (depth == 0) {
						return new Cost(source, target, Rank.IDENTITY);
					}
					Cost cost = new Cost(source, target, Rank.CONVERSION);
					cost.setInheritanceDistance(depth);
					return cost;
				}
			}
			if (udc == UDCMode.FORBIDDEN)
				return Cost.NO_CONVERSION;

			return copyInitializationOfClass(valueCat, source, (ICPPClassType) uqTarget, udc == UDCMode.DEFER);
		}

		if (uqSource instanceof ICPPClassType) {
			if (udc == UDCMode.FORBIDDEN)
				return Cost.NO_CONVERSION;

			return initializationByConversion(valueCat, source, (ICPPClassType) uqSource, target, udc == UDCMode.DEFER,
					false);
		}

		return checkStandardConversionSequence(uqSource, target);
	}

	/**
	 * 13.3.3.1.5 List-initialization sequence [over.ics.list]
	 */
	static Cost listInitializationSequence(EvalInitList arg, IType target, UDCMode udc, boolean isDirect)
			throws DOMException {
		Cost result = listInitializationSequenceHelper(arg, target, udc, isDirect);
		result.setListInitializationTarget(target);
		return result;
	}

	static Cost listInitializationSequenceHelper(EvalInitList arg, IType target, UDCMode udc, boolean isDirect)
			throws DOMException {
		IType listType = getInitListType(target);
		if (listType != null) {
			Cost worstCost = new Cost(arg.getType(), target, Rank.IDENTITY);
			for (ICPPEvaluation clause : arg.getClauses()) {
				Cost cost = checkImplicitConversionSequence(listType, clause.getType(), clause.getValueCategory(),
						UDCMode.ALLOWED, Context.ORDINARY);
				if (!cost.converts())
					return cost;
				if (cost.isNarrowingConversion()) {
					cost.setRank(Rank.NO_MATCH);
					return cost;
				}
				if (cost.compareTo(worstCost) > 0) {
					worstCost = cost;
				}
			}
			return worstCost;
		} else if (target instanceof IArrayType) {
			return AggregateInitialization.check(target, arg);
		}

		IType noCVTarget = getNestedType(target, CVTYPE | TDEF);
		if (noCVTarget instanceof ICPPClassType) {
			if (udc == UDCMode.FORBIDDEN)
				return Cost.NO_CONVERSION;

			ICPPClassType classTarget = (ICPPClassType) noCVTarget;
			if (TypeTraits.isAggregateClass(classTarget)) {
				Cost cost = AggregateInitialization.check(classTarget, arg);
				if (cost.converts()) {
					cost.setUserDefinedConversion(null);
					return cost;
				}
			}
			return listInitializationOfClass(arg, classTarget, isDirect, udc == UDCMode.DEFER);
		}

		ICPPEvaluation[] args = arg.getClauses();
		if (args.length == 1) {
			final ICPPEvaluation firstArg = args[0];
			if (!firstArg.isInitializerList()) {
				Cost cost = checkImplicitConversionSequence(target, firstArg.getType(), firstArg.getValueCategory(),
						udc, Context.ORDINARY);
				if (cost.isNarrowingConversion()) {
					return Cost.NO_CONVERSION;
				}
				return cost;
			}
		} else if (args.length == 0) {
			return new Cost(arg.getType(), target, Rank.IDENTITY);
		}

		return Cost.NO_CONVERSION;
	}

	static IType getInitListType(IType target) {
		target = getNestedType(target, REF | TDEF | CVTYPE);
		if (target instanceof ICPPClassType && target instanceof ICPPTemplateInstance) {
			ICPPTemplateInstance inst = (ICPPTemplateInstance) target;
			if (CharArrayUtils.equals(INITIALIZER_LIST_NAME, inst.getNameCharArray())) {
				IBinding owner = inst.getOwner();
				if (owner instanceof ICPPNamespace && CharArrayUtils.equals(STD_NAME, owner.getNameCharArray())
						&& owner.getOwner() == null) {
					ICPPTemplateArgument[] args = inst.getTemplateArguments();
					if (args.length == 1) {
						ICPPTemplateArgument arg = args[0];
						if (arg.isTypeValue()) {
							return arg.getTypeValue();
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * [3.9.3-4] Implements cv-ness (partial) comparison. There is a (partial)
	 * ordering on cv-qualifiers, so that a type can be said to be more
	 * cv-qualified than another.
	 * @return <ul>
	 * <li>3 if cv1 == const volatile cv2
	 * <li>2 if cv1 == volatile cv2
	 * <li>1 if cv1 == const cv2
	 * <li>EQ 0 if cv1 == cv2
	 * <li>LT -1 if cv1 is less qualified than cv2 or not comparable
	 * </ul>
	 */
	private static final int compareQualifications(IType t1, IType t2) {
		return getCVQualifier(t1).partialComparison(getCVQualifier(t2));
	}

	/**
	 * [8.5.3] "cv1 T1" is reference-related to "cv2 T2" if T1 is the same type as T2,
	 * or T1 is a base class of T2.
	 * Note this is not a symmetric relation.
	 * @return inheritance distance, or -1, if <code>cv1t1</code> is not reference-related to <code>cv2t2</code>
	 */
	private static final int isReferenceRelated(IType cv1Target, IType cv2Source) {
		IType t = SemanticUtil.getNestedType(cv1Target, TDEF | REF);
		IType s = SemanticUtil.getNestedType(cv2Source, TDEF | REF);

		// The way cv-qualification is currently modeled means
		// we must cope with IPointerType objects separately.
		if (t instanceof IPointerType) {
			if (s instanceof IPointerType) {
				t = SemanticUtil.getNestedType(((IPointerType) t).getType(), TDEF | REF);
				s = SemanticUtil.getNestedType(((IPointerType) s).getType(), TDEF | REF);
			} else {
				return -1;
			}
		} else if (t instanceof IArrayType) {
			if (s instanceof IArrayType) {
				final IArrayType at = (IArrayType) t;
				final IArrayType st = (IArrayType) s;
				final IValue av = at.getSize();
				final IValue sv = st.getSize();
				if (av == sv || (av != null && av.equals(sv))) {
					t = SemanticUtil.getNestedType(at.getType(), TDEF | REF | ALLCVQ);
					s = SemanticUtil.getNestedType(st.getType(), TDEF | REF | ALLCVQ);
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} else {
			if (t instanceof IQualifierType)
				t = SemanticUtil.getNestedType(((IQualifierType) t).getType(), TDEF | REF);
			if (s instanceof IQualifierType)
				s = SemanticUtil.getNestedType(((IQualifierType) s).getType(), TDEF | REF);

			if (t instanceof ICPPClassType && s instanceof ICPPClassType) {
				return SemanticUtil.calculateInheritanceDepth(s, t);
			}
		}
		if (t == s || (t != null && s != null && t.isSameType(s))) {
			return 0;
		}
		return -1;
	}

	/**
	 * [8.5.3] "cv1 T1" is reference-compatible with "cv2 T2" if T1 is reference-related
	 * to T2 and cv1 is the same cv-qualification as, or greater cv-qualification than, cv2.
	 * Note this is not a symmetric relation.
	 * @return The cost for converting or <code>null</code> if <code>cv1t1</code> is not
	 * reference-compatible with <code>cv2t2</code>
	 */
	private static final Cost isReferenceCompatible(IType cv1Target, IType cv2Source, boolean isImpliedObject) {
		int inheritanceDist = isReferenceRelated(cv1Target, cv2Source);
		if (inheritanceDist < 0)
			return null;
		final int cmp = compareQualifications(cv1Target, cv2Source);
		if (cmp < 0)
			return null;

		Cost cost = new Cost(cv2Source, cv1Target, Rank.IDENTITY);
		cost.setQualificationAdjustment(cmp);
		if (inheritanceDist > 0) {
			cost.setInheritanceDistance(inheritanceDist);
			cost.setRank(Rank.CONVERSION);
		}

		if (isImpliedObject) {
			cost.setImpliedObject();
		}
		return cost;
	}

	/**
	 * [4] Standard Conversions
	 * Computes the cost of using the standard conversion sequence from source to target.
	 */
	private static final Cost checkStandardConversionSequence(IType source, IType target) {
		final Cost cost = new Cost(source, target, Rank.IDENTITY);
		if (lvalue_to_rvalue(cost))
			return cost;

		if (promotion(cost))
			return cost;

		if (conversion(cost))
			return cost;

		if (qualificationConversion(cost))
			return cost;

		// If we can't convert the qualifications, then we can't do anything
		cost.setRank(Rank.NO_MATCH);
		return cost;
	}

	// [over.match.list] Initialization by list-initialization
	static Cost listInitializationOfClass(EvalInitList arg, ICPPClassType t, boolean isDirect, boolean deferUDC)
			throws DOMException {
		if (deferUDC) {
			Cost c = new Cost(arg.getType(), t, Rank.USER_DEFINED_CONVERSION);
			c.setDeferredUDC(isDirect ? DeferredUDC.DIRECT_LIST_INIT_OF_CLASS : DeferredUDC.LIST_INIT_OF_CLASS);
			return c;
		}

		// p1: When objects of non-aggregate class type are list-initialized,
		// [...] overload resoution selects the constructor in two phases:

		//   - Initially, the candidate functions are the initializer-
		//     list constructors of the class T and the argument list
		//     consists of the initializer list as a single argument.

		ICPPConstructor usedCtor = null;
		Cost bestCost = null;
		final ICPPConstructor[] constructors = t.getConstructors();
		ICPPConstructor[] ctors = constructors;
		for (ICPPConstructor ctor : ctors) {
			final int minArgCount = ctor.getRequiredArgumentCount();
			if (minArgCount == 0) {
				if (arg.getClauses().length == 0) {
					Cost c = new Cost(arg.getType(), t, Rank.IDENTITY);
					c.setUserDefinedConversion(ctor);
					return c;
				}
			} else if (minArgCount <= 1) {
				IType[] parTypes = ctor.getType().getParameterTypes();
				if (parTypes.length > 0) {
					final IType target = parTypes[0];
					if (getInitListType(target) != null) {
						Cost cost = listInitializationSequence(arg, target, UDCMode.FORBIDDEN, isDirect);
						if (cost.converts()) {
							int cmp = cost.compareTo(bestCost);
							if (bestCost == null || cmp < 0) {
								usedCtor = ctor;
								cost.setUserDefinedConversion(ctor);
								bestCost = cost;
							} else if (cmp == 0) {
								bestCost.setAmbiguousUDC(true);
							}
						}
					}
				}
			}
		}
		if (bestCost != null) {
			if (!bestCost.isAmbiguousUDC() && !isDirect) {
				if (usedCtor != null && usedCtor.isExplicit()) {
					bestCost.setRank(Rank.NO_MATCH);
				}
			}
			// This cost came from listInitializationSequence() with an std::initializer_list
			// type as the list initialization target. From the point of view of the caller,
			// however, the target is the class type, not std::initializer_list, so update it
			// accordingly.
			bestCost.setListInitializationTarget(t);
			return bestCost;
		}

		//   - If no viable initializer-list constructor is found,
		//     overload resolution is performed again, where the
		//     candidate functions are all the constructors of the
		//     class T and the argument list consists of the elements
		//     of the initializer list.

		LookupData data = new LookupData(t.getNameCharArray(), null, CPPSemantics.getCurrentLookupPoint());
		final ICPPEvaluation[] expandedArgs = arg.getClauses();
		data.setFunctionArguments(false, expandedArgs);
		data.fNoNarrowing = true;

		// 13.3.3.1.4
		ICPPConstructor[] filteredConstructors = constructors;
		if (expandedArgs.length == 1) {
			filteredConstructors = new ICPPConstructor[constructors.length];
			int j = 0;
			for (ICPPConstructor ctor : constructors) {
				if (ctor.getRequiredArgumentCount() < 2) {
					IType[] ptypes = ctor.getType().getParameterTypes();
					if (ptypes.length > 0) {
						IType ptype = getNestedType(ptypes[0], TDEF | REF | CVTYPE);
						if (!t.isSameType(ptype)) {
							filteredConstructors[j++] = ctor;
						}
					}
				}
			}
		}

		final IBinding result = CPPSemantics.resolveFunction(data, filteredConstructors, true, false);
		final Cost c;
		if (result instanceof ICPPMethod) {
			if (!isDirect && ((ICPPMethod) result).isExplicit()) {
				c = Cost.NO_CONVERSION;
			} else {
				c = new Cost(arg.getType(), t, Rank.IDENTITY);
				c.setUserDefinedConversion((ICPPMethod) result);
			}
		} else if (result instanceof IProblemBinding
				&& ((IProblemBinding) result).getID() == IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP) {
			c = new Cost(arg.getType(), t, Rank.USER_DEFINED_CONVERSION);
			c.setAmbiguousUDC(true);
		} else {
			c = Cost.NO_CONVERSION;
		}
		// This cost came from listInitializationSequence() with an std::initializer_list
		// type as the list initialization target. From the point of view of the caller,
		// however, the target is the class type, not std::initializer_list, so update it
		// accordingly.
		c.setListInitializationTarget(t);
		return c;
	}

	/**
	 * 13.3.1.4 Copy-initialization of class by user-defined conversion [over.match.copy]
	 */
	static final Cost copyInitializationOfClass(ValueCategory valueCat, IType source, ICPPClassType t, boolean deferUDC)
			throws DOMException {
		if (deferUDC) {
			Cost c = new Cost(source, t, Rank.USER_DEFINED_CONVERSION);
			c.setDeferredUDC(DeferredUDC.COPY_INIT_OF_CLASS);
			return c;
		}

		FunctionCost cost1 = null;
		Cost cost2 = null;
		ICPPFunction[] ctors = t.getConstructors();
		ctors = CPPTemplates.instantiateForFunctionCall(ctors, null, Collections.singletonList(source),
				Collections.singletonList(valueCat), false);

		for (ICPPFunction f : ctors) {
			if (!(f instanceof ICPPConstructor) || f instanceof IProblemBinding)
				continue;

			ICPPConstructor ctor = (ICPPConstructor) f;
			// Note: the special case of initializing a temporary to be bound to the first parameter
			// of a copy constructor called with a single argument in the context of direct-initialization
			// is (more naturally) handled in initializationByConversionForDirectReference.
			if (!ctor.isExplicit()) {
				final ICPPFunctionType ft = ctor.getType();
				final IType[] ptypes = ft.getParameterTypes();
				FunctionCost c1;
				if (ptypes.length == 0) {
					if (ctor.takesVarArgs()) {
						c1 = new FunctionCost(ctor, new Cost(source, null, Rank.ELLIPSIS_CONVERSION));
					} else {
						continue;
					}
				} else {
					IType ptype = SemanticUtil.getNestedType(ptypes[0], TDEF);
					// We don't need to check the implicit conversion sequence if the type is void
					if (SemanticUtil.isVoidType(ptype))
						continue;
					if (ctor.getRequiredArgumentCount() > 1)
						continue;

					c1 = new FunctionCost(ctor, checkImplicitConversionSequence(ptype, source, valueCat,
							UDCMode.FORBIDDEN, Context.ORDINARY));
				}
				int cmp = c1.compareTo(null, cost1, 1);
				if (cmp <= 0) {
					cost1 = c1;
					cost2 = new Cost(t, t, Rank.IDENTITY);
					cost2.setUserDefinedConversion(ctor);
					if (cmp == 0) {
						cost2.setAmbiguousUDC(true);
					}
				}
			}
		}

		final IType uqSource = getNestedType(source, TDEF | REF | CVTYPE);
		if (uqSource instanceof ICPPClassType) {
			ICPPFunction[] ops = SemanticUtil.getConversionOperators((ICPPClassType) uqSource);
			ops = CPPTemplates.instantiateConversionTemplates(ops, t);
			for (final ICPPFunction f : ops) {
				if (f instanceof ICPPMethod && !(f instanceof IProblemBinding)) {
					ICPPMethod op = (ICPPMethod) f;
					if (op.isExplicit())
						continue;
					final IType returnType = op.getType().getReturnType();
					final IType uqReturnType = getNestedType(returnType, REF | TDEF | CVTYPE);
					final int dist = SemanticUtil.calculateInheritanceDepth(uqReturnType, t);
					if (dist >= 0) {
						IType implicitType = CPPSemantics.getImplicitParameterType(op);
						final Cost udcCost = isReferenceCompatible(getNestedType(implicitType, TDEF | REF), source,
								true);
						if (udcCost != null) {
							// Make sure top-level cv-qualifiers are compared
							udcCost.setReferenceBinding(ReferenceBinding.LVALUE_REF);
							FunctionCost c1 = new FunctionCost(op, udcCost);
							int cmp = c1.compareTo(null, cost1, 1);
							if (cmp <= 0) {
								cost1 = c1;
								cost2 = new Cost(t, t, Rank.IDENTITY);
								if (dist > 0) {
									cost2.setInheritanceDistance(dist);
									cost2.setRank(Rank.CONVERSION);
								}
								cost2.setUserDefinedConversion(op);
								if (cmp == 0) {
									cost2.setAmbiguousUDC(true);
								}
							}
						}
					}
				}
			}
		}
		if (cost1 == null || !cost1.getCost(0).converts())
			return Cost.NO_CONVERSION;

		return cost2;
	}

	/**
	 * 13.3.1.5 Initialization by conversion function [over.match.conv]
	 */
	static Cost initializationByConversion(ValueCategory valueCat, IType source, ICPPClassType uqSource, IType target,
			boolean deferUDC, boolean allowExplicitConversion) throws DOMException {
		if (deferUDC) {
			Cost c = new Cost(source, target, Rank.USER_DEFINED_CONVERSION);
			c.setDeferredUDC(DeferredUDC.INIT_BY_CONVERSION);
			return c;
		}
		ICPPFunction[] ops = SemanticUtil.getConversionOperators(uqSource);
		ops = CPPTemplates.instantiateConversionTemplates(ops, target);
		FunctionCost cost1 = null;
		Cost cost2 = null;
		for (final ICPPFunction f : ops) {
			if (f instanceof ICPPMethod && !(f instanceof IProblemBinding)) {
				ICPPMethod op = (ICPPMethod) f;
				final boolean isExplicitConversion = op.isExplicit() && !allowExplicitConversion;
				if (isExplicitConversion /** && !direct **/
				)
					continue;

				ICPPFunctionType functionType = op.getType();
				final IType returnType = functionType.getReturnType();
				IType uqReturnType = getNestedType(returnType, TDEF | ALLCVQ);
				Cost c2 = checkImplicitConversionSequence(target, uqReturnType,
						valueCategoryFromReturnType(uqReturnType), UDCMode.FORBIDDEN, Context.ORDINARY);
				if (c2.converts()) {
					if (isExplicitConversion && c2.getRank() != Rank.IDENTITY)
						continue;
					IType implicitType = CPPSemantics.getImplicitParameterType(op);
					final Cost udcCost = isReferenceCompatible(getNestedType(implicitType, TDEF | REF), source, true);
					if (udcCost != null) {
						// Make sure top-level cv-qualifiers are compared
						if (functionType.hasRefQualifier() && functionType.isRValueReference()) {
							udcCost.setReferenceBinding(ReferenceBinding.RVALUE_REF_BINDS_RVALUE);
						} else {
							udcCost.setReferenceBinding(ReferenceBinding.LVALUE_REF);
						}
						FunctionCost c1 = new FunctionCost(op, udcCost);
						int cmp = c1.compareTo(null, cost1, 1);
						if (cmp <= 0) {
							cost1 = c1;
							cost2 = c2;
							cost2.setUserDefinedConversion(op);
							if (cmp == 0) {
								cost2.setAmbiguousUDC(true);
							}
						}
					}
				}
			}
		}
		if (cost1 == null || !cost1.getCost(0).converts())
			return Cost.NO_CONVERSION;

		return cost2;
	}

	/**
	 * Attempts the conversions below and returns whether this completely converts the source to
	 * the target type.
	 * [4.1] Lvalue-to-rvalue conversion
	 * [4.2] array-to-ptr
	 * [4.3] function-to-ptr
	 */
	private static final boolean lvalue_to_rvalue(final Cost cost) {
		IType target = getNestedType(cost.target, REF | TDEF | ALLCVQ);
		IType source = getNestedType(cost.source, REF | TDEF);

		// 4.2 array to pointer conversion
		if (source instanceof IArrayType) {
			if (target instanceof IPointerType) {
				// 4.2-2 a string literal can be converted to pointer to char
				source = unqualifyStringLiteral(source, (IPointerType) target, cost);
			}
			if (!(source instanceof IPointerType)) {
				source = new CPPPointerType(getNestedType(((IArrayType) source).getType(), TDEF));
			}
		} else if (source instanceof IFunctionType) {
			// 4.3 function to pointer conversion
			source = new CPPPointerType(source);
		} else {
			if (source instanceof IPointerType) {
				// A string literal may have been converted to a pointer when
				// computing the type of a conditional expression.
				if (target instanceof IPointerType) {
					// 4.2-2 a string literal can be converted to pointer to char
					source = unqualifyStringLiteral(source, (IPointerType) target, cost);
				}
			}
			source = getNestedType(source, TDEF | REF | ALLCVQ);
		}

		if (source == null || target == null) {
			cost.setRank(Rank.NO_MATCH);
			return true;
		}
		cost.source = source;
		cost.target = target;
		return source.isSameType(target);
	}

	private static IType unqualifyStringLiteral(IType source, final IPointerType target, final Cost cost) {
		if (target instanceof ICPPPointerToMemberType)
			return source;

		final IType targetPtrTgt = getNestedType((target).getType(), TDEF);
		if (targetPtrTgt instanceof IQualifierType && ((IQualifierType) targetPtrTgt).isConst())
			return source;

		IType srcTarget = ((ITypeContainer) source).getType();
		if (!(srcTarget instanceof IQualifierType))
			return source;

		final IQualifierType srcQTarget = (IQualifierType) srcTarget;
		if (srcQTarget.isConst() && !srcQTarget.isVolatile()) {
			srcTarget = srcQTarget.getType();
			if (srcTarget instanceof CPPBasicType) {
				if (((CPPBasicType) srcTarget).isFromStringLiteral()) {
					source = new CPPPointerType(srcTarget, false, false, false);
					CVQualifier cvqTarget = getCVQualifier(targetPtrTgt).add(CVQualifier.CONST);
					cost.setQualificationAdjustment(cvqTarget.partialComparison(CVQualifier.NONE) << 3);
				}
			}
		}
		return source;
	}

	/**
	 * [4.4] Qualifications
	 * @param cost
	 */
	private static final boolean qualificationConversion(Cost cost) {
		IType s = cost.source;
		IType t = cost.target;
		boolean constInEveryCV2k = true;
		boolean firstPointer = true;
		int adjustments = 0;
		int shift = 0;
		while (true) {
			s = getNestedType(s, TDEF | REF);
			t = getNestedType(t, TDEF | REF);
			if (s instanceof IPointerType && t instanceof IPointerType) {
				final int cmp = compareQualifications(t, s); // is t more qualified than s?
				if (cmp < 0 || (cmp > 0 && !constInEveryCV2k)) {
					return false;
				}
				final IPointerType tPtr = (IPointerType) t;
				final IPointerType sPtr = (IPointerType) s;
				if (haveMemberPtrConflict(sPtr, tPtr))
					return false;

				constInEveryCV2k &= (firstPointer || tPtr.isConst());
				s = sPtr.getType();
				t = tPtr.getType();
				firstPointer = false;
				adjustments |= (cmp << shift);
				shift += 3;
			} else {
				break;
			}
		}

		int cmp = compareQualifications(t, s); // is t more qualified than s?
		if (cmp < 0 || (cmp > 0 && !constInEveryCV2k)) {
			return false;
		}

		adjustments |= (cmp << shift);
		s = getNestedType(s, ALLCVQ | TDEF | REF);
		t = getNestedType(t, ALLCVQ | TDEF | REF);

		if (adjustments > 0) {
			cost.setQualificationAdjustment(adjustments);
		}
		return s != null && t != null && s.isSameType(t);
	}

	private static boolean haveMemberPtrConflict(IPointerType s, IPointerType t) {
		final boolean sIsPtrToMember = s instanceof ICPPPointerToMemberType;
		final boolean tIsPtrToMember = t instanceof ICPPPointerToMemberType;
		if (sIsPtrToMember != tIsPtrToMember) {
			return true;
		}
		if (sIsPtrToMember) {
			final IType sMemberOf = ((ICPPPointerToMemberType) s).getMemberOfClass();
			final IType tMemberOf = ((ICPPPointerToMemberType) t).getMemberOfClass();
			if (sMemberOf == null || tMemberOf == null || !sMemberOf.isSameType(tMemberOf)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Attempts promotions and returns whether the promotion converted the type.
	 *
	 * [4.5] [4.6] Promotion
	 *
	 * 4.5-1 char, signed char, unsigned char, short int or unsigned short int
	 * can be converted to int if int can represent all the values of the source
	 * type, otherwise they can be converted to unsigned int.
	 * 4.5-2 wchar_t or an enumeration can be converted to the first of the
	 * following that can hold it: int, unsigned int, long unsigned long.
	 * 4.5-4 bool can be promoted to int
	 * 4.6 float can be promoted to double
	 */
	private static final boolean promotion(Cost cost) {
		IType src = cost.source;
		IType trg = cost.target;

		boolean canPromote = false;
		if (trg instanceof IBasicType) {
			IBasicType basicTgt = (IBasicType) trg;
			final Kind tKind = basicTgt.getKind();

			if (src instanceof ICPPEnumeration) {
				final ICPPEnumeration enumType = (ICPPEnumeration) src;
				if (enumType.isScoped()) {
					return false;
				}
				IType fixedType = enumType.getFixedType();
				if (fixedType == null) {
					if (tKind == Kind.eInt || tKind == Kind.eUnspecified) {
						if (trg instanceof ICPPBasicType) {
							int qualifiers = ArithmeticConversion.getEnumIntTypeModifiers((IEnumeration) src);
							int targetModifiers = ((ICPPBasicType) trg).getModifiers();
							if (qualifiers == (targetModifiers & (IBasicType.IS_LONG | IBasicType.IS_LONG_LONG
									| IBasicType.IS_SHORT | IBasicType.IS_UNSIGNED))) {
								canPromote = true;
							}
						} else {
							canPromote = true;
						}
					}
				} else {
					if (fixedType.isSameType(trg))
						canPromote = true;
					// Allow to further promote the fixed type
					src = fixedType;
				}
			}
			if (src instanceof IBasicType) {
				final IBasicType basicSrc = (IBasicType) src;
				Kind sKind = basicSrc.getKind();
				if (tKind == Kind.eInt) {
					if (!basicTgt.isLong() && !basicTgt.isLongLong() && !basicTgt.isShort()) {
						switch (sKind) {
						case eInt: // short, and unsigned short
							if (basicSrc.isShort() && !basicTgt.isUnsigned()) {
								canPromote = true;
							}
							break;
						case eChar:
						case eBoolean:
						case eWChar:
						case eChar16:
						case eUnspecified: // treat unspecified as int
							if (!basicTgt.isUnsigned()) {
								canPromote = true;
							}
							break;

						case eChar32:
							if (basicTgt.isUnsigned()) {
								canPromote = true;
							}
							break;
						default:
							break;
						}
					}
				} else if (tKind == Kind.eDouble && sKind == Kind.eFloat) {
					canPromote = true;
				}
			}
		}
		if (canPromote) {
			cost.setRank(Rank.PROMOTION);
			return true;
		}
		return false;
	}

	/**
	 * Attempts conversions and returns whether the conversion succeeded.
	 * [4.7]  Integral conversions
	 * [4.8]  Floating point conversions
	 * [4.9]  Floating-integral conversions
	 * [4.10] Pointer conversions
	 * [4.11] Pointer to member conversions
	 */
	private static final boolean conversion(Cost cost) {
		final IType s = cost.source;
		final IType t = cost.target;

		if (t instanceof IBasicType) {
			// 4.7 integral conversion
			// 4.8 floating point conversion
			// 4.9 floating-integral conversion
			final Kind tgtKind = ((IBasicType) t).getKind();
			if (s instanceof IBasicType) {
				final Kind srcKind = ((IBasicType) s).getKind();
				if (srcKind == Kind.eVoid)
					return false;
				// 4.12 std::nullptr_t can be converted to bool
				if (srcKind == Kind.eNullPtr && tgtKind != Kind.eBoolean)
					return false;
				// 4.10-1 a null pointer constant can be converted to std::nullptr_t
				if (tgtKind == Kind.eNullPtr && !isNullPointerConstant(s))
					return false;

				cost.setRank(Rank.CONVERSION);
				if (srcKind != Kind.eNullPtr && tgtKind != Kind.eNullPtr) {
					cost.setCouldNarrow();
				}
				return true;
			}
			if (s instanceof ICPPEnumeration && !((ICPPEnumeration) s).isScoped()) {
				// 4.7 An rvalue of an enumeration type can be converted to an rvalue of an integer type.
				cost.setRank(Rank.CONVERSION);
				cost.setCouldNarrow();
				return true;
			}
			// 4.12 pointer or pointer to member type can be converted to an rvalue of type bool
			if (tgtKind == Kind.eBoolean && s instanceof IPointerType) {
				cost.setRank(Rank.CONVERSION_PTR_BOOL);
				return true;
			}
		}

		if (t instanceof IPointerType) {
			IPointerType tgtPtr = (IPointerType) t;
			// 4.10-1 an integral constant expression of integer type that evaluates to 0 can
			// be converted to a pointer type
			// 4.11-1 same for pointer to member
			if (s instanceof IBasicType) {
				if (isNullPointerConstant(s)) {
					cost.setRank(Rank.CONVERSION);
					return true;
				}
				return false;
			}
			if (s instanceof IPointerType) {
				IPointerType srcPtr = (IPointerType) s;
				// 4.10-2 an rvalue of type "pointer to cv T", where T is an object type can be
				// converted to an rvalue of type "pointer to cv void"
				IType tgtPtrTgt = getNestedType(tgtPtr.getType(), TDEF | CVTYPE | REF);
				if (SemanticUtil.isVoidType(tgtPtrTgt)) {
					cost.setRank(Rank.CONVERSION);
					cost.setInheritanceDistance(Short.MAX_VALUE);
					CVQualifier cv = getCVQualifier(srcPtr.getType());
					cost.source = new CPPPointerType(
							addQualifiers(CPPSemantics.VOID_TYPE, cv.isConst(), cv.isVolatile(), cv.isRestrict()));
					return false;
				}

				final boolean tIsPtrToMember = t instanceof ICPPPointerToMemberType;
				final boolean sIsPtrToMember = s instanceof ICPPPointerToMemberType;
				if (!tIsPtrToMember && !sIsPtrToMember) {
					// 4.10-3 An rvalue of type "pointer to cv D", where D is a class type can be converted
					// to an rvalue of type "pointer to cv B", where B is a base class of D.
					IType srcPtrTgt = getNestedType(srcPtr.getType(), TDEF | CVTYPE | REF);
					if (tgtPtrTgt instanceof ICPPClassType && srcPtrTgt instanceof ICPPClassType) {
						int depth = SemanticUtil.calculateInheritanceDepth(srcPtrTgt, tgtPtrTgt);
						if (depth == -1) {
							cost.setRank(Rank.NO_MATCH);
							return true;
						}
						if (depth > 0) {
							cost.setRank(Rank.CONVERSION);
							cost.setInheritanceDistance(depth);
							CVQualifier cv = getCVQualifier(srcPtr.getType());
							cost.source = new CPPPointerType(
									addQualifiers(tgtPtrTgt, cv.isConst(), cv.isVolatile(), cv.isRestrict()));
						}
						return false;
					}
				} else if (tIsPtrToMember && sIsPtrToMember) {
					// 4.11-2 An rvalue of type "pointer to member of B of type cv T", where B is a class type,
					// can be converted to an rvalue of type "pointer to member of D of type cv T" where D is a
					// derived class of B
					ICPPPointerToMemberType spm = (ICPPPointerToMemberType) s;
					ICPPPointerToMemberType tpm = (ICPPPointerToMemberType) t;
					IType st = spm.getType();
					IType tt = tpm.getType();
					if (st != null && tt != null && st.isSameType(tt)) {
						int depth = SemanticUtil.calculateInheritanceDepth(tpm.getMemberOfClass(),
								spm.getMemberOfClass());
						if (depth == -1) {
							cost.setRank(Rank.NO_MATCH);
							return true;
						}
						if (depth > 0) {
							cost.setRank(Rank.CONVERSION);
							cost.setInheritanceDistance(depth);
							cost.source = new CPPPointerToMemberType(spm.getType(), tpm.getMemberOfClass(),
									spm.isConst(), spm.isVolatile(), spm.isRestrict());
						}
						return false;
					}
				}
			}
		}
		return false;
	}

	public static boolean isNullPointerConstant(IType s) {
		if (s instanceof CPPBasicType) {
			final CPPBasicType basicType = (CPPBasicType) s;
			if (basicType.getKind() == Kind.eNullPtr)
				return true;

			Long val = basicType.getAssociatedNumericalValue();
			if (val != null && val == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 4.1, 4.2, 4.3
	 */
	public static IType lvalue_to_rvalue(IType type, boolean resolveTypedefs) {
		IType t = SemanticUtil.getNestedType(type, TDEF | REF);
		if (t instanceof IArrayType) {
			return new CPPPointerType(((IArrayType) t).getType());
		}
		if (t instanceof IFunctionType) {
			return new CPPPointerType(t);
		}
		IType uqType = SemanticUtil.getNestedType(t, TDEF | REF | ALLCVQ);
		if (uqType instanceof ICPPClassType) {
			return resolveTypedefs ? t : SemanticUtil.getNestedType(type, COND_TDEF | REF);
		}
		return resolveTypedefs ? uqType : SemanticUtil.getNestedType(type, COND_TDEF | REF | ALLCVQ);
	}

	/**
	 * Composite pointer type computed as described in 5.9-2 except that if the conversion to
	 * the pointer is not possible, the method returns {@code null}.
	 */
	public static IType compositePointerType(IType t1, IType t2) {
		t1 = SemanticUtil.getNestedType(t1, TDEF);
		t2 = SemanticUtil.getNestedType(t2, TDEF);
		final boolean isPtr1 = t1 instanceof IPointerType;
		if (isPtr1 || isNullPtr(t1)) {
			if (isNullPointerConstant(t2)) {
				return t1;
			}
		}
		final boolean isPtr2 = t2 instanceof IPointerType;
		if (isPtr2 || isNullPtr(t2)) {
			if (isNullPointerConstant(t1)) {
				return t2;
			}
		}
		if (!isPtr1 || !isPtr2)
			return null;

		final IPointerType p1 = (IPointerType) t1;
		final IPointerType p2 = (IPointerType) t2;
		if (haveMemberPtrConflict(p1, p2))
			return null;

		final IType target1 = p1.getType();
		if (isVoidType(target1)) {
			return addQualifiers(p1, p2.isConst(), p2.isVolatile(), p2.isRestrict());
		}
		final IType target2 = p2.getType();
		if (isVoidType(target2)) {
			return addQualifiers(p2, p1.isConst(), p1.isVolatile(), p1.isRestrict());
		}

		IType t = mergePointers(target1, target2, true, true);
		if (t == null)
			return null;
		if (t == target1)
			return p1;
		if (t == target2)
			return p2;
		return copyPointer(p1, t, false, false);
	}

	private static IType mergePointers(IType t1, IType t2, boolean allcq, boolean allowInheritance) {
		t1 = getNestedType(t1, TDEF | REF);
		t2 = getNestedType(t2, TDEF | REF);
		if (t1 instanceof IPointerType && t2 instanceof IPointerType) {
			final IPointerType p1 = (IPointerType) t1;
			final IPointerType p2 = (IPointerType) t2;
			final CVQualifier cv1 = getCVQualifier(t1);
			final CVQualifier cv2 = getCVQualifier(t2);
			if (haveMemberPtrConflict(p1, p2))
				return null;
			if (!allcq && cv1 != cv2)
				return null;

			final IType p1target = p1.getType();
			IType merged = mergePointers(p1target, p2.getType(), allcq && (cv1.isConst() || cv2.isConst()), false);
			if (merged == null)
				return null;
			if (p1target == merged && cv1.isAtLeastAsQualifiedAs(cv2))
				return p1;
			if (p2.getType() == merged && cv2.isAtLeastAsQualifiedAs(cv1))
				return p2;

			return copyPointer(p1, merged, cv1.isConst() || cv2.isConst(), cv1.isVolatile() || cv2.isVolatile());
		}

		final IType uq1 = getNestedType(t1, TDEF | REF | CVTYPE);
		final IType uq2 = getNestedType(t2, TDEF | REF | CVTYPE);
		if (uq1 == null) {
			return null;
		}

		if (uq1.isSameType(uq2)) {
			if (uq1 == t1 && uq2 == t2)
				return t1;

			CVQualifier cv1 = getCVQualifier(t1);
			CVQualifier cv2 = getCVQualifier(t2);
			if (cv1 == cv2)
				return t1;

			if (!allcq)
				return null;

			if (cv1.isAtLeastAsQualifiedAs(cv2))
				return t1;
			if (cv2.isAtLeastAsQualifiedAs(cv1))
				return t2;

			// One type is const the other is volatile.
			return new CPPQualifierType(uq1, true, true);
		} else if (allowInheritance) {
			// Allow for conversion from pointer-to-derived to pointer-to-base as per [conv.ptr] p3.
			IType base;
			if (SemanticUtil.calculateInheritanceDepth(uq1, uq2) > 0) {
				base = uq2;
			} else if (SemanticUtil.calculateInheritanceDepth(uq2, uq1) > 0) {
				base = uq1;
			} else {
				return null;
			}
			CVQualifier cv1 = getCVQualifier(t1);
			CVQualifier cv2 = getCVQualifier(t2);
			if (cv1 == CVQualifier.NONE && cv2 == CVQualifier.NONE) {
				return base;
			}
			return new CPPQualifierType(base, cv1.isConst() || cv2.isConst(), cv1.isVolatile() || cv2.isVolatile());
		}
		return null;
	}

	public static IType copyPointer(final IPointerType p1, IType target, final boolean isConst,
			final boolean isVolatile) {
		if (p1 instanceof ICPPPointerToMemberType) {
			ICPPPointerToMemberType ptm = (ICPPPointerToMemberType) p1;
			return new CPPPointerToMemberType(target, ptm.getMemberOfClass(), isConst, isVolatile, false);
		}
		return new CPPPointerType(target, isConst, isVolatile, false);
	}

	private static boolean isNullPtr(IType t1) {
		return t1 instanceof IBasicType && ((IBasicType) t1).getKind() == Kind.eNullPtr;
	}
}
