/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Bryan Wilkinson (QNX)
 *    Andrew Ferguson (Symbian)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.*;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Cost.Rank;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * Routines for calculating the cost of conversions.
 */
public class Conversions {
	enum UDCMode {allowUDC, noUDC, deferUDC}
	/**
	 * Computes the cost of an implicit conversion sequence
	 * [over.best.ics] 13.3.3.1
	 * @param sourceExp the expression behind the source type
	 * @param source the source (argument) type
	 * @param target the target (parameter) type
	 * @param isImpliedObject
	 * 
	 * @return the cost of converting from source to target
	 * @throws DOMException
	 */
	public static Cost checkImplicitConversionSequence(IASTExpression sourceExp, IType source,
			IType target, UDCMode udc, boolean isImpliedObject) throws DOMException {
		if (isImpliedObject) {
			udc= UDCMode.noUDC;
		}
		
		target= getNestedType(target, TDEF);
		source= getNestedType(source, TDEF);
		
		if (target instanceof ICPPReferenceType) {
			// [8.5.3-5] initialization of a reference 
			IType cv1T1= getNestedType(target, TDEF | REF);
			
			boolean lvalue= sourceExp == null || !CPPVisitor.isRValue(sourceExp);	
			if (source instanceof ICPPReferenceType) 
				source= getNestedType(source, TDEF | REF);
			
			IType T2= getNestedType(source, TDEF | REF | CVQ | PTR_CVQ);

		    // [8.5.3-5] Is an lvalue (but is not a bit-field), and "cv1 T1" is reference-compatible with "cv2 T2," 
			if (lvalue) {
				Cost cost= isReferenceCompatible(cv1T1, source, isImpliedObject);
				if (cost != null) {
					// [8.5.3-5] this is a direct reference binding
					// [13.3.3.1.4-1] direct binding has either identity or conversion rank.					
					if (cost.getInheritanceDistance() > 0) {
						cost.setRank(Rank.CONVERSION);
					} 
					return cost;
				} 
			}
			
			if (T2 instanceof ICPPClassType && udc != UDCMode.noUDC) {
				// Or has a class type (i.e., T2 is a class type) and can be implicitly converted to
				// an lvalue of type "cv3 T3," where "cv1 T1" is reference-compatible with "cv3 T3" 92)
				// (this conversion is selected by enumerating the applicable conversion functions
				// (13.3.1.6) and choosing the best one through overload resolution (13.3)).
				ICPPMethod[] fcns= SemanticUtil.getConversionOperators((ICPPClassType) T2);
				Cost operatorCost= null;
				boolean ambiguousConversionOperator= false;
				if (fcns.length > 0 && !(fcns[0] instanceof IProblemBinding)) {
					for (final ICPPMethod op : fcns) {
						IType newSource= op.getType().getReturnType();
						if (newSource instanceof ICPPReferenceType) { // require an lvalue
							IType cvT2= getNestedType(newSource, TDEF | REF);
							Cost cost2= isReferenceCompatible(cv1T1, cvT2, false); 
							if (cost2 != null) {
								int cmp= cost2.compareTo(operatorCost);
								if (cmp <= 0) {
									ambiguousConversionOperator= cmp == 0;
									operatorCost= cost2;
									operatorCost.setUserDefinedConversion(op);
								}
							}
						}
					}
				}

				if (operatorCost != null && !ambiguousConversionOperator) {
					if (isImpliedObject) {
						operatorCost.setInheritanceDistance(0);
					}
					return operatorCost;
				}
			}

			// [8.5.3-5] Direct binding failed  - Otherwise
			boolean cv1isConst= getCVQualifier(cv1T1) == 1;
			if (cv1isConst)  {
				if (!lvalue && T2 instanceof ICPPClassType) {
					Cost cost= isReferenceCompatible(cv1T1, source, isImpliedObject);
					if (cost != null)
						return cost;
				}
			
				// 5 - Otherwise
				// Otherwise, a temporary of type "cv1 T1" is created and initialized from
				// the initializer expression using the rules for a non-reference copy
				// initialization (8.5). The reference is then bound to the temporary.

				// If T1 is reference-related to T2, cv1 must be the same cv-qualification as,
				// or greater cv-qualification than, cv2; otherwise, the program is ill-formed.
				
				// 13.3.3.1.7 no temporary object when converting the implicit object parameter
				if (!isImpliedObject) {
					IType T1= getNestedType(cv1T1, TDEF | REF | CVQ | PTR_CVQ);
					boolean illformed= isReferenceRelated(T1, T2) >= 0 && compareQualifications(cv1T1, source) < 0;

					// We must do a non-reference initialization
					if (!illformed) {
						return nonReferenceConversion(source, cv1T1, udc, isImpliedObject);
					}
				}
			}
			return new Cost(source, cv1T1, Rank.NO_MATCH);
		} 
		
		// Non-reference binding
		return nonReferenceConversion(source, target, udc, isImpliedObject);
	}

	private static Cost nonReferenceConversion(IType source, IType target, UDCMode udc, boolean isImpliedObject) throws DOMException {
		Cost cost= checkStandardConversionSequence(source, target, isImpliedObject);
		if (cost.getRank() != Rank.NO_MATCH || udc == UDCMode.noUDC) 
			return cost;
		
		Cost temp = checkUserDefinedConversionSequence(source, target, udc == UDCMode.deferUDC);
		if (temp != null) {
			cost = temp;
		}
		return cost;
	}

	/**
	 * [3.9.3-4] Implements cv-ness (partial) comparison. There is a (partial)
	 * ordering on cv-qualifiers, so that a type can be said to be more
	 * cv-qualified than another.
	 * @return <ul>
	 * <li>GT 1 if cv1 is more qualified than cv2
	 * <li>EQ 0 if cv1 and cv2 are equally qualified
	 * <li>LT -1 if cv1 is less qualified than cv2 or not comparable
	 * </ul>
	 * @throws DOMException
	 */
	private static final int compareQualifications(IType t1, IType t2) throws DOMException {
		int cv1= getCVQualifier(t1);
		int cv2= getCVQualifier(t2);
		
		// same qualifications
		if (cv1 == cv2)
			return 0;

		// both are different but not comparable
		final int diffs= cv1 ^ cv2;
		if (diffs == 3 && cv1 != 3 && cv2 != 3) {
			return -1;
		}

		return cv1-cv2;
	}

	/** 
	 * Returns 0 for no qualifier, 1 for const, 2 for volatile and 3 for const volatile.
	 */
	private static int getCVQualifier(IType t) {
		if (t instanceof IQualifierType) {
			int result= 0;
			IQualifierType qt= (IQualifierType) t;
			if (qt.isConst()) 
				result= 1;
			if (qt.isVolatile())
				result |= 2;
			return result;
		} 
		if (t instanceof IPointerType) {
			IPointerType pt= (IPointerType) t;
			int result= 0;
			if (pt.isConst()) 
				result= 1;
			if (pt.isVolatile())
				result |= 2;
			return result;
		}
		return 0;
	}
	
	/**
	 * [8.5.3] "cv1 T1" is reference-related to "cv2 T2" if T1 is the same type as T2,
	 * or T1 is a base class of T2.
	 * Note this is not a symmetric relation.
	 * @return inheritance distance, or -1, if <code>cv1t1</code> is not reference-related to <code>cv2t2</code>
	 */
	private static final int isReferenceRelated(IType cv1Target, IType cv2Source) throws DOMException {
		IType t= SemanticUtil.getNestedType(cv1Target, TDEF | REF);
		IType s= SemanticUtil.getNestedType(cv2Source, TDEF | REF);
		
		// The way cv-qualification is currently modeled means
		// we must cope with IPointerType objects separately.
		if (t instanceof IPointerType && s instanceof IPointerType) {
			t= SemanticUtil.getNestedType(((IPointerType) t).getType(), TDEF | REF);
			s= SemanticUtil.getNestedType(((IPointerType) s).getType(), TDEF | REF);
		} else {
			if (t instanceof IQualifierType)
				t= SemanticUtil.getNestedType(((IQualifierType) t).getType(), TDEF | REF);
			if (s instanceof IQualifierType)
				s= SemanticUtil.getNestedType(((IQualifierType) s).getType(), TDEF | REF);

			if (t instanceof ICPPClassType && s instanceof ICPPClassType) {
				return calculateInheritanceDepth(CPPSemantics.MAX_INHERITANCE_DEPTH, s, t);
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
	private static final Cost isReferenceCompatible(IType cv1Target, IType cv2Source, boolean isImpliedObject) throws DOMException {
		int inheritanceDist= isReferenceRelated(cv1Target, cv2Source);
		if (inheritanceDist < 0)
			return null;
		final int cmp= compareQualifications(cv1Target, cv2Source);
		if (cmp < 0)
			return null;
		
		// 7.3.3.13 for overload resolution the implicit this pointer is treated as if 
		// it were a pointer to the derived class
		if (isImpliedObject) 
			inheritanceDist= 0;

		Cost cost= new Cost(cv2Source, cv1Target, Rank.IDENTITY);
		cost.setQualificationAdjustment(cmp);
		cost.setInheritanceDistance(inheritanceDist);
		return cost;
	}
	
	/**
	 * [4] Standard Conversions
	 * Computes the cost of using the standard conversion sequence from source to target.
	 * @param isImplicitThis handles the special case when members of different
	 *    classes are nominated via using-declarations. In such a situation the derived to
	 *    base conversion does not cause any costs.
	 * @throws DOMException
	 */
	protected static final Cost checkStandardConversionSequence(IType source, IType target,
			boolean isImplicitThis) throws DOMException {
		final Cost cost= new Cost(source, target, Rank.IDENTITY);
		if (lvalue_to_rvalue(cost))
			return cost;

		if (promotion(cost))
			return cost;
		
		if (conversion(cost, isImplicitThis)) 
			return cost;

		if (qualificationConversion(cost))
			return cost;

		// If we can't convert the qualifications, then we can't do anything
		cost.setRank(Rank.NO_MATCH);
		return cost;
	}

	/**
	 * [13.3.3.1.2] User-defined conversions
	 * @param source
	 * @param target
	 * @return
	 * @throws DOMException
	 */
	static final Cost checkUserDefinedConversionSequence(IType source, IType target, boolean deferUDC) throws DOMException {
		Cost constructorCost= null;
		Cost operatorCost= null;

		IType s= getNestedType(source, TDEF | CVQ | REF);
		IType t= getNestedType(target, TDEF | CVQ | REF);

		if (!(s instanceof ICPPClassType || t instanceof ICPPClassType)) {
			return null;
		}
		
		if (deferUDC) {
			Cost c= new Cost(s, t, Rank.USER_DEFINED_CONVERSION);
			c.setDeferredUDC(true);
			return c;
		}
		
		//constructors
		if (t instanceof ICPPClassType) {
			ICPPConstructor[] ctors= ((ICPPClassType) t).getConstructors();
			// select converting constructors
			int j= 0;
			ICPPConstructor[] convertingCtors= new ICPPConstructor[ctors.length];
			for (int i = 0; i < ctors.length; i++) {
				ICPPConstructor ctor= ctors[i];
				if (!(ctor instanceof IProblemBinding) && !ctor.isExplicit())
					convertingCtors[j++]= ctor;
			}
			if (j > 0) {
				LookupData data= new LookupData();
				data.setFunctionArgumentTypes(new IType [] { source });
				IBinding binding = CPPSemantics.resolveFunction(data, convertingCtors, false);
				if (binding instanceof ICPPConstructor && !(binding instanceof IProblemBinding)) {
					constructorCost = checkStandardConversionSequence(t, target, false);
					if (constructorCost.getRank() == Rank.NO_MATCH) {
						constructorCost= null;
					} else {
						constructorCost.setUserDefinedConversion((ICPPConstructor)binding);
					}
				}
			}
		}
		
		//conversion operators
		boolean ambiguousConversionOperator= false;
		if (s instanceof ICPPClassType) {
			ICPPMethod [] ops = SemanticUtil.getConversionOperators((ICPPClassType) s); 
			if (ops.length > 0 && !(ops[0] instanceof IProblemBinding)) {
				for (final ICPPMethod op : ops) {
					Cost cost= checkStandardConversionSequence(op.getType().getReturnType(), target, false);
					if (cost.getRank() != Rank.NO_MATCH) {
						int cmp= cost.compareTo(operatorCost);
						if (cmp <= 0) {
							cost.setUserDefinedConversion(op);
							operatorCost= cost;
							ambiguousConversionOperator= cmp == 0;
						}
					}
				}
			}
		}

		if (constructorCost != null) {
			if (operatorCost != null && !ambiguousConversionOperator) {
				// If both are valid, then the conversion is ambiguous
				constructorCost.setAmbiguousUDC(true);
			} 
			return constructorCost;
		} 
		
		if (operatorCost != null) {
			operatorCost.setAmbiguousUDC(ambiguousConversionOperator);
			return operatorCost;
		}

		return null;
	}

	/**
	 * Calculates the number of edges in the inheritance path of <code>clazz</code> to
	 * <code>ancestorToFind</code>, returning -1 if no inheritance relationship is found.
	 * @param clazz the class to search upwards from
	 * @param ancestorToFind the class to find in the inheritance graph
	 * @return the number of edges in the inheritance graph, or -1 if the specified classes have
	 * no inheritance relation
	 * @throws DOMException
	 */
	private static final int calculateInheritanceDepth(int maxdepth, IType type, IType ancestorToFind)
			throws DOMException {
		if (type == ancestorToFind || type.isSameType(ancestorToFind)) {
			return 0;
		}

		if (maxdepth > 0 && type instanceof ICPPClassType && ancestorToFind instanceof ICPPClassType) {
			ICPPClassType clazz = (ICPPClassType) type;
			if (clazz instanceof ICPPDeferredClassInstance) {
				clazz= (ICPPClassType) ((ICPPDeferredClassInstance) clazz).getSpecializedBinding();
			}
			
			for (ICPPBase cppBase : clazz.getBases()) {
				IBinding base= cppBase.getBaseClass();
				if (base instanceof IType) {
					IType tbase= (IType) base;
					if (tbase.isSameType(ancestorToFind) || 
							(ancestorToFind instanceof ICPPSpecialization &&  // allow some flexibility with templates 
							((IType)((ICPPSpecialization) ancestorToFind).getSpecializedBinding()).isSameType(tbase))) {
						return 1;
					}

					tbase= getNestedType(tbase, TDEF);
					if (tbase instanceof ICPPClassType) {
						int n= calculateInheritanceDepth(maxdepth - 1, tbase, ancestorToFind);
						if (n > 0)
							return n + 1;
					}
				}
			}
		}

		return -1;
	}

	/**
	 * Attempts the conversions below and returns whether this completely converts the source to
	 * the target type.
	 * [4.1] Lvalue-to-rvalue conversion
	 * [4.2] array-to-ptr
	 * [4.3] function-to-ptr
	 */
	private static final boolean lvalue_to_rvalue(final Cost cost) throws DOMException {
		// target should not be a reference here.
		boolean isConverted= false;
		IType target = getNestedType(cost.target, REF | TDEF);
		IType source= getNestedType(cost.source, TDEF);

		
		// 4.1 lvalue to rvalue
		IType srcRValue= getNestedType(source, REF | TDEF);
		if (source instanceof ICPPReferenceType) {
			// 4.1 lvalue of non-function and non-array
			if (!(srcRValue instanceof IFunctionType) && !(srcRValue instanceof IArrayType)) {
				// 4.1 if T is a non-class type, the type of the rvalue is the cv-unqualified version of T
				IType unqualifiedSrcRValue= getNestedType(srcRValue, CVQ | PTR_CVQ | TDEF | REF);
				if (unqualifiedSrcRValue instanceof ICPPClassType) {
					if (isCompleteType(unqualifiedSrcRValue)) {
						source= srcRValue;
					} else {
						// ill-formed
						cost.setRank(Rank.NO_MATCH);
						return true;
					}
				} else {
					source= unqualifiedSrcRValue;
				}
				cost.setRank(Rank.LVALUE_TRANSFORMATION);
				isConverted= true;
			}
		}
		
		// 4.2 array to pointer conversion
		if (!isConverted && srcRValue instanceof IArrayType) {
			final IArrayType arrayType= (IArrayType) srcRValue;
			
			if (target instanceof IPointerType) {
				final IType targetPtrTgt= getNestedType(((IPointerType) target).getType(), TDEF);
				
				// 4.2-2 a string literal can be converted to pointer to char
				if (!(targetPtrTgt instanceof IQualifierType) || !((IQualifierType) targetPtrTgt).isConst()) {
					IType tmp= arrayType.getType();
					if (tmp instanceof IQualifierType && ((IQualifierType) tmp).isConst()) {
						tmp= ((IQualifierType) tmp).getType();
						if (tmp instanceof CPPBasicType) {
							IASTExpression val = ((CPPBasicType) tmp).getCreatedFromExpression();
							if (val instanceof IASTLiteralExpression) {
								IASTLiteralExpression lit= (IASTLiteralExpression) val;
								if (lit.getKind() == IASTLiteralExpression.lk_string_literal) {
									source= new CPPPointerType(tmp, false, false);
									cost.setQualificationAdjustment(getCVQualifier(targetPtrTgt) | 1);
									cost.setRank(Rank.LVALUE_TRANSFORMATION);
									isConverted= true;
								}
							}
						}
					}
				}
			}
			if (!isConverted && (target instanceof IPointerType || target instanceof IBasicType)) {
				source = new CPPPointerType(getNestedType(arrayType.getType(), TDEF));
				cost.setRank(Rank.LVALUE_TRANSFORMATION);
				isConverted= true;
			}
		}

		// 4.3 function to pointer conversion
		if (!isConverted && target instanceof IPointerType) {
			final IType targetPtrTgt= getNestedType(((IPointerType) target).getType(), TDEF);
			if (targetPtrTgt instanceof IFunctionType && srcRValue instanceof IFunctionType) {
				source = new CPPPointerType(source);
				cost.setRank(Rank.LVALUE_TRANSFORMATION);
				isConverted= true;
			} 
		}

		// this should actually be done in 'checkImplicitConversionSequence', see 13.3.3.1-6 and 8.5.14
		// 8.5.14 cv-qualifiers can be ignored for non-class types
		IType unqualifiedTarget= getNestedType(target, CVQ | PTR_CVQ | TDEF | REF);
		if (!(unqualifiedTarget instanceof ICPPClassType)) {
			IType unqualifiedSource= getNestedType(source, CVQ | PTR_CVQ | TDEF | REF);
			if (!(unqualifiedSource instanceof ICPPClassType)) {
				source= unqualifiedSource;
				target= unqualifiedTarget;
			}
		}		

		if (source == null || target == null) {
			cost.setRank(Rank.NO_MATCH);
			return true;
		} 
		cost.source= source;
		cost.target= target;
		if (source.isSameType(target)) 
			return true;
		
		return false;
	}
	
	/**
	 * [4.4] Qualifications 
	 * @param cost
	 * @throws DOMException
	 */
	private static final boolean qualificationConversion(Cost cost) throws DOMException{
		IType s = cost.source;
		IType t = cost.target;
		boolean constInEveryCV2k = true;
		boolean firstPointer= true;
		int adjustments= 0;
		while (true) {
			s= getNestedType(s, TDEF | REF);
			t= getNestedType(t, TDEF | REF);
			if (s instanceof IPointerType && t instanceof IPointerType) {
				adjustments <<= 2;
				final int cmp= compareQualifications(t, s);  // is t more qualified than s?
				if (cmp < 0 || (cmp > 0 && !constInEveryCV2k)) {
					return false;
				} else {
					final boolean sIsPtrToMember = s instanceof ICPPPointerToMemberType;
					final boolean tIsPtrToMember = t instanceof ICPPPointerToMemberType;
					if (sIsPtrToMember != tIsPtrToMember) {
						return false;
					} else if (sIsPtrToMember) {
						final IType sMemberOf = ((ICPPPointerToMemberType) s).getMemberOfClass();
						final IType tMemberOf = ((ICPPPointerToMemberType) t).getMemberOfClass();
						if (sMemberOf == null || tMemberOf == null || !sMemberOf.isSameType(tMemberOf)) {
							return false;
						}
					}
				}

				final IPointerType tPtr = (IPointerType) t;
				final IPointerType sPtr = (IPointerType) s;
				constInEveryCV2k &= (firstPointer || tPtr.isConst());
				s= sPtr.getType();
				t= tPtr.getType();
				firstPointer= false;
				adjustments |= cmp;
			} else {
				break;
			}
		}

		if (s instanceof IQualifierType || t instanceof IQualifierType) {
			adjustments <<= 2;
			int cmp= compareQualifications(t, s);  // is t more qualified than s?
			if (cmp < 0 || (cmp > 0 && !constInEveryCV2k)) {
				return false;
			} 

			adjustments |= cmp;
			s= getNestedType(s, CVQ | TDEF | REF);
			t= getNestedType(t, CVQ | TDEF | REF);
		} 
		
		if (adjustments > 0) {
			cost.setQualificationAdjustment(adjustments);
		}
		return s != null && t != null && s.isSameType(t);
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
	 * @throws DOMException
	 */
	private static final boolean promotion(Cost cost) throws DOMException{
		IType src = cost.source;
		IType trg = cost.target;

		boolean canPromote= false;
		if (trg instanceof IBasicType) {
			IBasicType basicTgt = (IBasicType) trg;
			final int tType = basicTgt.getType();

			if (src instanceof IBasicType) {
				final IBasicType basicSrc = (IBasicType) src;
				int sType = basicSrc.getType();
				if (tType == IBasicType.t_int) {
					switch (sType) {
					case IBasicType.t_int: // short, and unsigned short
						if (basicSrc.isShort()) {
							canPromote= true;
						}
						break;
					case IBasicType.t_char:
					case ICPPBasicType.t_bool:
					case ICPPBasicType.t_wchar_t:
					case IBasicType.t_unspecified: // treat unspecified as int
						canPromote= true;
						break;
					}
				} else if (tType == IBasicType.t_double && sType == IBasicType.t_float) {
					canPromote= true;
				}
			} else if (src instanceof IEnumeration) {
				if (tType == IBasicType.t_int || tType == IBasicType.t_unspecified) {
					canPromote= true;
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
	private static final boolean conversion(Cost cost, boolean forImplicitThis) throws DOMException{
		final IType s = cost.source;
		final IType t = cost.target;

		if (t instanceof IBasicType) {
			// 4.7 integral conversion
			// 4.8 floating point conversion
			// 4.9 floating-integral conversion
			if (s instanceof IBasicType || s instanceof IEnumeration) {
				// 4.7 An rvalue of an integer type can be converted to an rvalue of another integer type.  
				// An rvalue of an enumeration type can be converted to an rvalue of an integer type.
				cost.setRank(Rank.CONVERSION);
				return true;
			} 
			// 4.12 pointer or pointer to member type can be converted to an rvalue of type bool
			final int tgtType = ((IBasicType) t).getType();
			if (tgtType == ICPPBasicType.t_bool && s instanceof IPointerType) {
				cost.setRank(Rank.CONVERSION_PTR_BOOL);
				return true;
			} 
		}
		
		if (t instanceof IPointerType) {
			IPointerType tgtPtr= (IPointerType) t;
			if (s instanceof CPPBasicType) {
				// 4.10-1 an integral constant expression of integer type that evaluates to 0 can
				// be converted to a pointer type
				// 4.11-1 same for pointer to member
				IASTExpression exp = ((CPPBasicType) s).getCreatedFromExpression();
				if (exp != null) {
					Long val= Value.create(exp, Value.MAX_RECURSION_DEPTH).numericalValue();
					if (val != null && val == 0) {
						cost.setRank(Rank.CONVERSION);
						return true;
					}
				}
				return false;
			}
			if (s instanceof IPointerType) {
				IPointerType srcPtr= (IPointerType) s;
				// 4.10-2 an rvalue of type "pointer to cv T", where T is an object type can be
				// converted to an rvalue of type "pointer to cv void"
				IType tgtPtrTgt= getNestedType(tgtPtr.getType(), TDEF | CVQ | REF);
				if (tgtPtrTgt instanceof IBasicType && ((IBasicType) tgtPtrTgt).getType() == IBasicType.t_void) {
					cost.setRank(Rank.CONVERSION);
					cost.setInheritanceDistance(Short.MAX_VALUE); // mstodo add distance to last base class
					int cv= getCVQualifier(srcPtr.getType());
					cost.source= new CPPPointerType(addQualifiers(CPPSemantics.VOID_TYPE, (cv&1) != 0, (cv&2) != 0));
					return false; 
				}
				
				final boolean tIsPtrToMember = t instanceof ICPPPointerToMemberType;
				final boolean sIsPtrToMember = s instanceof ICPPPointerToMemberType;
				if (!tIsPtrToMember && !sIsPtrToMember) {
					// 4.10-3 An rvalue of type "pointer to cv D", where D is a class type can be converted
					// to an rvalue of type "pointer to cv B", where B is a base class of D.
					IType srcPtrTgt= getNestedType(srcPtr.getType(), TDEF | CVQ | REF);
					if (tgtPtrTgt instanceof ICPPClassType && srcPtrTgt instanceof ICPPClassType) {
						int depth= calculateInheritanceDepth(CPPSemantics.MAX_INHERITANCE_DEPTH, srcPtrTgt, tgtPtrTgt);
						if (depth == -1) {
							cost.setRank(Rank.NO_MATCH);
							return true;
						}
						if (depth > 0) {
							if (!forImplicitThis) {
								cost.setRank(Rank.CONVERSION);
								cost.setInheritanceDistance(depth);
							}
							int cv= getCVQualifier(srcPtr.getType());
							cost.source= new CPPPointerType(addQualifiers(tgtPtrTgt, (cv&1) != 0, (cv&2) != 0));
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
						int depth= calculateInheritanceDepth(CPPSemantics.MAX_INHERITANCE_DEPTH,
								tpm.getMemberOfClass(), spm.getMemberOfClass());
						if (depth == -1) {
							cost.setRank(Rank.NO_MATCH);
							return true;
						}
						if (depth > 0) {
							cost.setRank(Rank.CONVERSION);
							cost.setInheritanceDistance(depth);
							cost.source = new CPPPointerToMemberType(spm.getType(),
									tpm.getMemberOfClass(), spm.isConst(), spm.isVolatile());
						}
						return false;
					}
				}
			}
		}
		return false;
	}
		
	/**
	 * @param type
	 * @return whether the specified type has an associated definition
	 */
	private static final boolean isCompleteType(IType type) {
		type= getUltimateType(type, false);
		if (type instanceof ICPPTemplateInstance)
			return true;
		if (type instanceof ICPPClassType) {
			if (type instanceof IIndexFragmentBinding) {
				try {
					return ((IIndexFragmentBinding) type).hasDefinition();
				} catch(CoreException ce) {
					CCorePlugin.log(ce);
				}
			}
			try {
				return ((ICPPClassType) type).getCompositeScope() != null;
			} catch (DOMException e) {
				return false;
			}
		}
		
		return true;
	}

//  mstodo must be part of implicit conversion
//	/**
//	 * [13.3.3.1-6] Derived to base conversion
//	 * @param cost
//	 * @throws DOMException
//	 */
//	private static final void derivedToBaseConversion(Cost cost) throws DOMException {
//		IType s = getUltimateType(cost.source, true);
//		IType t = getUltimateType(cost.target, true);
//
//		if (s instanceof ICPPClassType && t instanceof ICPPClassType) {
//			int depth= calculateInheritanceDepth(CPPSemantics.MAX_INHERITANCE_DEPTH, s, t);
//			if (depth > -1) {
//				cost.rank = Cost.DERIVED_TO_BASE_CONVERSION;
//				cost.conversion = depth;
//			}
//		}
//	}
}
