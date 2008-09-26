/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getUltimateType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getUltimateTypeViaTypedefs;

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
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * Routines for calculating the cost of conversions.
 */
public class Conversions {
	/**
	 * Computes the cost of an implicit conversion sequence
	 * [over.best.ics] 13.3.3.1
	 * 
	 * @param allowUDC whether a user-defined conversion is allowed during the sequence
	 * @param sourceExp the expression behind the source type
	 * @param source the source (argument) type
	 * @param target the target (parameter) type
	 * @param isImpliedObject
	 * @return the cost of converting from source to target
	 * @throws DOMException
	 */
	public static Cost checkImplicitConversionSequence(boolean allowUDC, IASTExpression sourceExp, IType source, IType target, boolean isImpliedObject) throws DOMException {
		Cost cost;
		
		allowUDC &= !isImpliedObject;
		target= getUltimateTypeViaTypedefs(target);
		source= getUltimateTypeViaTypedefs(source);
		
		if (target instanceof ICPPReferenceType) {
			// [13.3.3.3.1] Reference binding 
			IType cv1T1= getUltimateTypeViaTypedefs(((ICPPReferenceType)target).getType());
			cost= new Cost(source, cv1T1);
			cost.targetHadReference= true;
			
			boolean lvalue= sourceExp == null || !CPPVisitor.isRValue(sourceExp);			
			IType T2= source instanceof IQualifierType ? getUltimateTypeViaTypedefs(((IQualifierType)source).getType()) : source;

			if (lvalue && isReferenceCompatible(cv1T1, source)) {
				/* Direct reference binding */
				// [13.3.3.1.4]
				
				if (cost.source.isSameType(cost.target) || 
						// 7.3.3.13 for overload resolution the implicit this pointer is treated as if 
						// it were a pointer to the derived class
						(isImpliedObject && cost.source instanceof ICPPClassType && cost.target instanceof ICPPClassType)) {
					cost.rank = Cost.IDENTITY_RANK;
					return cost;
				}
				
				/*
				 * is an lvalue (but is not a bit-field), and "cv1 T1" is reference-compatible with "cv2 T2," 
				 */
				// [13.3.3.1.4-1] direct binding
				// [8.5.3-5]
				qualificationConversion(cost);

				derivedToBaseConversion(cost);
			} else if (T2 instanceof ICPPClassType) {
				if(allowUDC) {
					/*
					 * or has a class type (i.e., T2 is a class type) and can be implicitly converted to
					 * an lvalue of type "cv3 T3," where "cv1 T1" is reference-compatible with "cv3 T3" 92)
					 * (this conversion is selected by enumerating the applicable conversion functions (13.3.1.6)
					 * and choosing the best one through overload resolution (13.3)).
					 */
					ICPPMethod[] fcns= SemanticUtil.getConversionOperators((ICPPClassType)T2);
					Cost operatorCost= null;
					ICPPMethod conv= null;
					boolean ambiguousConversionOperator= false;
					if (fcns.length > 0 && fcns[0] instanceof IProblemBinding == false) {
						for (final ICPPMethod op : fcns) {
							Cost cost2 = checkStandardConversionSequence(op.getType().getReturnType(), target, false);
							if (cost2.rank != Cost.NO_MATCH_RANK) {
								if (operatorCost == null) {
									operatorCost= cost2;
									conv= op;
								} else {
									int cmp= operatorCost.compare(cost2);
									if (cmp >= 0) {
										ambiguousConversionOperator= cmp == 0;
										operatorCost= cost2;
										conv= op;
									}
								}
							}
						}
					}

					if (conv!= null && !ambiguousConversionOperator) {
						IType newSource= conv.getType().getReturnType();
						boolean isNewSourceLValue= newSource instanceof ICPPReferenceType;
						if (isNewSourceLValue && isReferenceCompatible(cv1T1, newSource)) {
							cost= new Cost(cv1T1, newSource);
							qualificationConversion(cost);
							derivedToBaseConversion(cost);
						}
					}
				}
			}

			/* Direct binding failed */

			if (cost.rank == Cost.NO_MATCH_RANK) {
				// 8.5.3-5 - Otherwise
				
				boolean cv1isConst= false;
				if (cv1T1 instanceof IQualifierType) {
					cv1isConst= ((IQualifierType)cv1T1).isConst() && !((IQualifierType)cv1T1).isVolatile();
				} else if (cv1T1 instanceof IPointerType) {
					cv1isConst= ((IPointerType)cv1T1).isConst() && !((IPointerType)cv1T1).isVolatile();
				}

				if (cv1isConst) {
					if (!lvalue && source instanceof ICPPClassType) {
						cost= new Cost(source, target);
						cost.rank= Cost.IDENTITY_RANK;
					} else {
						// 5 - Otherwise
						// Otherwise, a temporary of type "cv1 T1" is created and initialized from the initializer expression
						// using the rules for a non-reference copy initialization (8.5). The reference is then bound to the temporary.

						// If T1 is reference-related to T2, cv1 must be the same cv-qualification as, or greater cvqualification
						// than, cv2; otherwise, the program is ill-formed. [Example
						boolean illformed= false;
						if (isReferenceRelated(cv1T1, source)) {
							Integer cmp= compareQualifications(cv1T1, source); 
							if (cmp == null || cmp < 0) {
								illformed= true;
							}
						}

						// we must do a non-reference initialization
						if (!illformed) {
							cost= checkStandardConversionSequence(source, cv1T1, isImpliedObject);
							// 12.3-4 At most one user-defined conversion is implicitly applied to
							// a single value.  (also prevents infinite loop)				
							if (allowUDC && (cost.rank == Cost.NO_MATCH_RANK || 
									cost.rank == Cost.FUZZY_TEMPLATE_PARAMETERS)) { 
								Cost temp = checkUserDefinedConversionSequence(source, cv1T1);
								if (temp != null) {
									cost = temp;
								}
							}
						}
					}
				}
			}
		} else {
			// Non-reference binding
			
			cost= checkStandardConversionSequence(source, target, isImpliedObject);
			if (allowUDC && (cost.rank == Cost.NO_MATCH_RANK || 
					cost.rank == Cost.FUZZY_TEMPLATE_PARAMETERS)) { 
				Cost temp = checkUserDefinedConversionSequence(source, target);
				if (temp != null) {
					cost = temp;
				}
			}
		}
		
		return cost;
	}

	/**
	 * [3.9.3-4] Implements cv-ness (partial) comparison. There is a (partial)
	 * ordering on cv-qualifiers, so that a type can be said to be more
	 * cv-qualified than another.
	 * @param cv1
	 * @param cv2
	 * @return <ul>
	 * <li>GT 1 if cv1 is more qualified than cv2
	 * <li>EQ 0 if cv1 and cv2 are equally qualified
	 * <li>LT -1 if cv1 is less qualified than cv2
	 * <li>NC null if cv1 and cv2 are not comparable
	 * </ul>
	 * @throws DOMException
	 */
	private static final Integer compareQualifications(IType cv1, IType cv2) throws DOMException {
		boolean cv1Const= false, cv2Const= false, cv1Volatile= false, cv2Volatile= false;
		if (cv1 instanceof IQualifierType) {
			IQualifierType qt1= (IQualifierType) cv1;
			cv1Const= qt1.isConst();
			cv1Volatile= qt1.isVolatile();
		} else if (cv1 instanceof IPointerType) {
			IPointerType pt1= (IPointerType) cv1;
			cv1Const= pt1.isConst();
			cv1Volatile= pt1.isVolatile();
		}
		if (cv2 instanceof IQualifierType) {
			IQualifierType qt2= (IQualifierType) cv2;
			cv2Const= qt2.isConst();
			cv2Volatile= qt2.isVolatile();
		} else if (cv2 instanceof IPointerType) {
			IPointerType pt2= (IPointerType) cv2;
			cv1Const= pt2.isConst();
			cv1Volatile= pt2.isVolatile();
		}
		int cmpConst=  cv1Const ? (cv2Const ? 0 : 1) : (!cv2Const ? 0 : -1); 
		int cmpVolatile= cv1Volatile ? (cv2Volatile ? 0 : 1) : (!cv2Volatile ? 0 : -1);

		if (cmpConst == cmpVolatile) {
			return cmpConst;
		} else if (cmpConst != 0 && cmpVolatile == 0) {
			return cmpConst;
		} else if (cmpConst == 0 && cmpVolatile != 0) {
			return cmpVolatile;
		}

		return null;
	}

	/**
	 * [8.5.3] "cv1 T1" is reference-related to "cv2 T2" if T1 is the same type as T2, or T1 is a base class of T2.
	 * Note this is not a symmetric relation.
	 * @param cv1t1
	 * @param cv2t2
	 * @return whether <code>cv1t1</code> is reference-related to <code>cv2t2</code>
	 * @throws DOMException
	 */
	private static final boolean isReferenceRelated(IType cv1t1, IType cv2t2) throws DOMException {
		// I've not found anything in the spec to justify unrolling cv1t1 or cv1t2 so far
		IType t1= SemanticUtil.getUltimateTypeUptoPointers(cv1t1);
		IType t2= SemanticUtil.getUltimateTypeUptoPointers(cv2t2);
		
		// The way cv-qualification is currently modeled means
		// we must cope with IPointerType objects separately.
		if (t1 instanceof IPointerType && t2 instanceof IPointerType) {
			IType ptt1= ((IPointerType)t1).getType();
			IType ptt2= ((IPointerType)t2).getType();
			return ptt1 != null && ptt2 != null ? ptt1.isSameType(ptt2) : ptt1 == ptt2;
		}
		
		t1= t1 instanceof IQualifierType ? ((IQualifierType)t1).getType() : t1;
		t2= t2 instanceof IQualifierType ? ((IQualifierType)t2).getType() : t2;
		
		if (t1 instanceof ICPPClassType && t2 instanceof ICPPClassType) {
			return calculateInheritanceDepth(CPPSemantics.MAX_INHERITANCE_DEPTH, t2, t1) >= 0;
		}
		
		return t1 != null && t2 != null ? t1.isSameType(t2) : t1 == t2; 
	}

	/**
	 * [8.5.3] "cv1 T1" is reference-compatible with "cv2 T2" if T1 is reference-related
	 * to T2 and cv1 is the same cv-qualification as, or greater cv-qualification than, cv2.
	 * Note this is not a symmetric relation.
	 * @param cv1t1
	 * @param cv2t2
	 * @return whether <code>cv1t1</code> is reference-compatible with <code>cv2t2</code>
	 * @throws DOMException
	 */
	private static final boolean isReferenceCompatible(IType cv1t1, IType cv2t2) throws DOMException {
		if (isReferenceRelated(cv1t1, cv2t2)) {
			Integer cmp= compareQualifications(cv1t1, cv2t2);
			return cmp != null && cmp >= 0;
		}
		return false;
	}
	
	/**
	 * [4] Standard Conversions
	 * Computes the cost of using the standard conversion sequence from source to target.
	 * @param isImplicitThis handles the special case when members of different
	 *    classes are nominated via using-declarations. In such a situation the derived to
	 *    base conversion does not cause any costs.
	 * @throws DOMException
	 */
	protected static final Cost checkStandardConversionSequence(IType source, IType target, boolean isImplicitThis) throws DOMException {
		Cost cost = lvalue_to_rvalue(source, target);

		if (cost.source == null || cost.target == null) {
			return cost;
		}

		if (cost.source.isSameType(cost.target) || 
				// 7.3.3.13 for overload resolution the implicit this pointer is treated as if 
				// it were a pointer to the derived class
				(isImplicitThis && cost.source instanceof ICPPClassType && cost.target instanceof ICPPClassType)) {
			cost.rank = Cost.IDENTITY_RANK;
			return cost;
		}

		qualificationConversion(cost);

		//if we can't convert the qualifications, then we can't do anything
		if (cost.qualification == Cost.NO_MATCH_RANK) {
			return cost;
		}

		//was the qualification conversion enough?
		IType s = getUltimateType(cost.source, true);
		IType t = getUltimateType(cost.target, true);

		if (s == null || t == null) {
			cost.rank = Cost.NO_MATCH_RANK;
			return cost;
		}

		if (s.isSameType(t) || 
				// 7.3.3.13 for overload resolution the implicit this pointer is treated as if 
				// it were a pointer to the derived class
				(isImplicitThis && s instanceof ICPPClassType && t instanceof ICPPClassType)) {
			return cost;
		}

		promotion(cost);
		if (cost.promotion > 0 || cost.rank > -1) {
			return cost;
		}

		conversion(cost);

		if (cost.rank > -1)
			return cost;

		derivedToBaseConversion(cost);

		if (cost.rank == -1) {
			relaxTemplateParameters(cost);
		}
		return cost;	
	}

	/**
	 * [13.3.3.1.2] User-defined conversions
	 * @param source
	 * @param target
	 * @return
	 * @throws DOMException
	 */
	private static final Cost checkUserDefinedConversionSequence(IType source, IType target) throws DOMException {
		Cost constructorCost= null;
		Cost operatorCost= null;

		IType s= getUltimateType(source, true);
		IType t= getUltimateType(target, true);

		//constructors
		if (t instanceof ICPPClassType) {
			ICPPConstructor [] constructors= ((ICPPClassType)t).getConstructors();
			if (constructors.length > 0 && constructors[0] instanceof IProblemBinding == false) {
				LookupData data= new LookupData();
				data.forUserDefinedConversion= true;
				data.functionParameters= new IType [] { source };
				IBinding binding = CPPSemantics.resolveFunction(data, constructors);
				if (binding instanceof ICPPConstructor) {
					ICPPConstructor constructor= (ICPPConstructor) binding;
					if (!constructor.isExplicit()) {
						constructorCost = checkStandardConversionSequence(t, target, false);
						if (constructorCost.rank == Cost.NO_MATCH_RANK) {
							constructorCost= null;
						}
					}
				}
			}
		}
		
		//conversion operators
		boolean ambiguousConversionOperator= false;
		if (s instanceof ICPPClassType) {
			ICPPMethod [] ops = SemanticUtil.getConversionOperators((ICPPClassType)s); 
			if (ops.length > 0 && ops[0] instanceof IProblemBinding == false) {
				for (final ICPPMethod op : ops) {
					Cost cost= checkStandardConversionSequence(op.getType().getReturnType(), target, false);
					if (cost.rank != Cost.NO_MATCH_RANK) {
						if (operatorCost == null) {
							operatorCost= cost;
						}
						else {
							int cmp= operatorCost.compare(cost);
							if (cmp >= 0) {
								ambiguousConversionOperator= cmp == 0;
								operatorCost= cost;
							}
						}
					}
				}
			}
		}

		if (constructorCost != null) {
			if (operatorCost == null || ambiguousConversionOperator) {
				constructorCost.userDefined = Cost.USERDEFINED_CONVERSION;
				constructorCost.rank = Cost.USERDEFINED_CONVERSION_RANK;
			}
			else {
				//if both are valid, then the conversion is ambiguous
				constructorCost.userDefined = Cost.AMBIGUOUS_USERDEFINED_CONVERSION;	
				constructorCost.rank = Cost.USERDEFINED_CONVERSION_RANK;
			}
			return constructorCost;
		} 
		if (operatorCost != null) {
			operatorCost.rank = Cost.USERDEFINED_CONVERSION_RANK;
			if (ambiguousConversionOperator) {
				operatorCost.userDefined = Cost.AMBIGUOUS_USERDEFINED_CONVERSION;
			}
			else {
				operatorCost.userDefined = Cost.USERDEFINED_CONVERSION;
			} 			
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
	private static final int calculateInheritanceDepth(int maxdepth, IType type, IType ancestorToFind) throws DOMException {
		if (type == ancestorToFind || type.isSameType(ancestorToFind)) {
			return 0;
		}

		if (maxdepth > 0 && type instanceof ICPPClassType && ancestorToFind instanceof ICPPClassType) {
			ICPPClassType clazz = (ICPPClassType) type;
			if(clazz instanceof ICPPDeferredClassInstance) {
				clazz= (ICPPClassType) ((ICPPDeferredClassInstance)clazz).getSpecializedBinding();
			}
			
			for (ICPPBase cppBase : clazz.getBases()) {
				IBinding base= cppBase.getBaseClass();
				if (base instanceof IType) {
					IType tbase= (IType) base;
					if (tbase.isSameType(ancestorToFind) || 
							(ancestorToFind instanceof ICPPSpecialization &&  /*allow some flexibility with templates*/ 
							((IType)((ICPPSpecialization)ancestorToFind).getSpecializedBinding()).isSameType(tbase))) {
						return 1;
					}

					tbase= getUltimateTypeViaTypedefs(tbase);
					if (tbase instanceof ICPPClassType) {
						int n= calculateInheritanceDepth(maxdepth-1, tbase, ancestorToFind);
						if (n > 0)
							return n + 1;
					}
				}
			}
		}

		return -1;
	}

	/**
	 * [4.1] Lvalue-to-rvalue conversion
	 * [4.2] array-to-ptr
	 * [4.3] function-to-ptr
	 * 
	 * @param source
	 * @param target
	 * @return
	 * @throws DOMException
	 */
	private static final Cost lvalue_to_rvalue(IType source, IType target) throws DOMException {
		Cost cost = new Cost(source, target);

		if (!isCompleteType(source)) {
			cost.rank= Cost.NO_MATCH_RANK;
			return cost;
		}

		if (source instanceof ICPPReferenceType) {
			source= ((ICPPReferenceType) source).getType();
		}
		if (target instanceof ICPPReferenceType) {
			target= ((ICPPReferenceType) target).getType();
			cost.targetHadReference = true;
		}

		//4.3 function to pointer conversion
		if (target instanceof IPointerType && ((IPointerType)target).getType() instanceof IFunctionType &&
				source instanceof IFunctionType) {
			source = new CPPPointerType(source);
		} else if (target instanceof IPointerType && source instanceof IArrayType) {
			//4.2 Array-To-Pointer conversion
			source = new CPPPointerType(((IArrayType)source).getType());
		}

		//4.1 if T is a non-class type, the type of the rvalue is the cv-unqualified version of T
		if (source instanceof IQualifierType) {
			IType t = ((IQualifierType)source).getType();
			while (t instanceof ITypedef)
				t = ((ITypedef)t).getType();
			if (!(t instanceof ICPPClassType)) {
				source = t;
			}
		} else if (source instanceof IPointerType && 
				(((IPointerType)source).isConst() || ((IPointerType)source).isVolatile())) {
			IType t= ((IPointerType) source).getType();
			while (t instanceof ITypedef)
				t= ((ITypedef) t).getType();
			if (!(t instanceof ICPPClassType)) {
				source= new CPPPointerType(t);
			}
		}

		cost.source = source;
		cost.target = target;

		return cost;
	}
	
	/**
	 * [4.4] Qualifications 
	 * @param cost
	 * @throws DOMException
	 */
	private static final void qualificationConversion(Cost cost) throws DOMException{
		boolean canConvert = true;
		int requiredConversion = Cost.IDENTITY_RANK;  

		IType s = cost.source, t = cost.target;
		boolean constInEveryCV2k = true;
		boolean firstPointer= true;
		while (true) {
			s= getUltimateTypeViaTypedefs(s);
			t= getUltimateTypeViaTypedefs(t);
			final boolean sourceIsPointer= s instanceof IPointerType;
			final boolean targetIsPointer= t instanceof IPointerType;

			if (!targetIsPointer) {
				if (!sourceIsPointer) 
					break;
				if (t instanceof ICPPBasicType) {
					if (((ICPPBasicType)t).getType() == ICPPBasicType.t_bool) {
						canConvert= true;
						requiredConversion = Cost.CONVERSION_RANK;
						break;
					}
				}
				canConvert = false; 
				break;
			} else if (!sourceIsPointer) {
				canConvert = false; 
				break;
			} else if (s instanceof ICPPPointerToMemberType ^ t instanceof ICPPPointerToMemberType) {
				canConvert = false;
				break;
			} 
			
			// both are pointers
			IPointerType op1= (IPointerType) s;
			IPointerType op2= (IPointerType) t;

			//if const is in cv1,j then const is in cv2,j.  Similary for volatile
			if ((op1.isConst() && !op2.isConst()) || (op1.isVolatile() && !op2.isVolatile())) {
				canConvert = false;
				requiredConversion = Cost.NO_MATCH_RANK;
				break;
			}
			//if cv1,j and cv2,j are different then const is in every cv2,k for 0<k<j
			if (!constInEveryCV2k && (op1.isConst() != op2.isConst() ||
					op1.isVolatile() != op2.isVolatile())) {
				canConvert = false;
				requiredConversion = Cost.NO_MATCH_RANK;
				break; 
			}
			constInEveryCV2k &= (firstPointer || op2.isConst());
			s = op1.getType();
			t = op2.getType();
			firstPointer= false;
		}

		if (cost.targetHadReference && s instanceof ICPPReferenceType) {
			s = ((ICPPReferenceType) s).getType();
		}
		if (s instanceof IQualifierType ^ t instanceof IQualifierType) {
			if (t instanceof IQualifierType) {
				if (!constInEveryCV2k) {
					canConvert= false;
					requiredConversion= Cost.NO_MATCH_RANK;
				} else {
					canConvert = true;
					requiredConversion = Cost.CONVERSION_RANK;
				}
			} else {
				//4.2-2 a string literal can be converted to pointer to char
				if (t instanceof IBasicType && ((IBasicType)t).getType() == IBasicType.t_char &&
						s instanceof IQualifierType) {
					IType qt = ((IQualifierType) s).getType();
					if (qt instanceof IBasicType) {
						IASTExpression val = ((IBasicType) qt).getValue();
						canConvert = (val != null && 
								val instanceof IASTLiteralExpression && 
								((IASTLiteralExpression)val).getKind() == IASTLiteralExpression.lk_string_literal);
					} else {
						canConvert = false;
						requiredConversion = Cost.NO_MATCH_RANK;
					}
				} else {
					canConvert = false;
					requiredConversion = Cost.NO_MATCH_RANK;
				}
			}
		} else if (s instanceof IQualifierType && t instanceof IQualifierType) {
			IQualifierType qs = (IQualifierType) s;
			IQualifierType qt = (IQualifierType) t;
			if (qs.isConst() == qt.isConst() && qs.isVolatile() == qt.isVolatile()) {
				requiredConversion = Cost.IDENTITY_RANK;
			} else if ((qs.isConst() && !qt.isConst()) || (qs.isVolatile() && !qt.isVolatile()) || !constInEveryCV2k) {
				requiredConversion = Cost.NO_MATCH_RANK;
				canConvert= false;
			} else {
				requiredConversion = Cost.CONVERSION_RANK;
			}
		} else if (constInEveryCV2k && !canConvert) {
			canConvert = true;
			requiredConversion = Cost.CONVERSION_RANK;
			int i = 1;
			for (IType type = s; canConvert && i == 1; type = t, i++) {
				while (type instanceof ITypeContainer) {
					if (type instanceof IQualifierType) {
						canConvert = false;
				    } else if (type instanceof IPointerType) {
						canConvert = !((IPointerType) type).isConst() && !((IPointerType) type).isVolatile();
					}
					if (!canConvert) {
						requiredConversion = Cost.NO_MATCH_RANK;
						break;
					}
					type = ((ITypeContainer) type).getType();
				}
			}
		}

		cost.qualification = requiredConversion;
		if (canConvert == true) {
			cost.rank = Cost.LVALUE_OR_QUALIFICATION_RANK;
		}
	}

	/**
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
	private static final void promotion(Cost cost) throws DOMException{
		IType src = cost.source;
		IType trg = cost.target;

		if (src.isSameType(trg))
			return;

		if (src instanceof IBasicType && trg instanceof IBasicType) {
			int sType = ((IBasicType)src).getType();
			int tType = ((IBasicType)trg).getType();
			if ((tType == IBasicType.t_int && (sType == IBasicType.t_int ||   //short, long , unsigned etc
					sType == IBasicType.t_char    || 
					sType == ICPPBasicType.t_bool || 
					sType == ICPPBasicType.t_wchar_t ||
					sType == IBasicType.t_unspecified)) || //treat unspecified as int
					(tType == IBasicType.t_double && sType == IBasicType.t_float)) {
				cost.promotion = 1; 
			}
		} else if (src instanceof IEnumeration && trg instanceof IBasicType &&
				(((IBasicType)trg).getType() == IBasicType.t_int || 
				((IBasicType)trg).getType() == IBasicType.t_unspecified)) {
			cost.promotion = 1; 
		}

		cost.rank = (cost.promotion > 0) ? Cost.PROMOTION_RANK : Cost.NO_MATCH_RANK;
	}
	
	/**
	 * [4.7]  Integral conversions
	 * [4.8]  Floating point conversions
	 * [4.9]  Floating-integral conversions
	 * [4.10] Pointer conversions
	 * [4.11] Pointer to member conversions
	 * @param cost
	 * @throws DOMException
	 */
	private static final void conversion(Cost cost) throws DOMException{
		final IType src = cost.source;
		final IType trg = cost.target;

		cost.conversion = 0;
		cost.detail = 0;

		IType[] sHolder= new IType[1], tHolder= new IType[1];
		IType s = getUltimateType(src, sHolder, true);
		IType t = getUltimateType(trg, tHolder, true);
		IType sPrev= sHolder[0], tPrev= tHolder[0];

		if (src instanceof IBasicType && trg instanceof IPointerType) {
			//4.10-1 an integral constant expression of integer type that evaluates to 0 can be converted to a pointer type
			IASTExpression exp = ((IBasicType)src).getValue();
			if (exp instanceof IASTLiteralExpression && 
					((IASTLiteralExpression)exp).getKind() == IASTLiteralExpression.lk_integer_constant) {
				try { 
					String val = exp.toString().toLowerCase().replace('u', '0');
					val.replace('l', '0');
					if (Integer.decode(val).intValue() == 0) {
						cost.rank = Cost.CONVERSION_RANK;
						cost.conversion = 1;
					}
				} catch(NumberFormatException e) {
				}
			}
		} else if (sPrev instanceof IPointerType) {
			//4.10-2 an rvalue of type "pointer to cv T", where T is an object type can be
			//converted to an rvalue of type "pointer to cv void"
			if (tPrev instanceof IPointerType && t instanceof IBasicType && ((IBasicType)t).getType() == IBasicType.t_void) {
				cost.rank = Cost.CONVERSION_RANK;
				cost.conversion = 1;
				cost.detail = 2;
				return;
			}
			//4.10-3 An rvalue of type "pointer to cv D", where D is a class type can be converted
			//to an rvalue of type "pointer to cv B", where B is a base class of D.
			else if (s instanceof ICPPClassType && tPrev instanceof IPointerType && t instanceof ICPPClassType) {
				int depth= calculateInheritanceDepth(CPPSemantics.MAX_INHERITANCE_DEPTH, s, t);
				cost.rank= (depth > -1) ? Cost.CONVERSION_RANK : Cost.NO_MATCH_RANK;
				cost.conversion= (depth > -1) ? depth : 0;
				cost.detail= 1;
				return;
			}
			// 4.12 if the target is a bool, we can still convert
			else if (!(trg instanceof IBasicType && ((IBasicType)trg).getType() == ICPPBasicType.t_bool)) {
				return;
			}
		}

		if (t instanceof IBasicType && s instanceof IBasicType || s instanceof IEnumeration) {
			//4.7 An rvalue of an integer type can be converted to an rvalue of another integer type.  
			//An rvalue of an enumeration type can be converted to an rvalue of an integer type.
			cost.rank = Cost.CONVERSION_RANK;
			cost.conversion = 1;	
		} else if (trg instanceof IBasicType && ((IBasicType)trg).getType() == ICPPBasicType.t_bool && s instanceof IPointerType) {
			//4.12 pointer or pointer to member type can be converted to an rvalue of type bool
			cost.rank = Cost.CONVERSION_RANK;
			cost.conversion = 1;
		} else if (s instanceof ICPPPointerToMemberType && t instanceof ICPPPointerToMemberType) {
			//4.11-2 An rvalue of type "pointer to member of B of type cv T", where B is a class type, 
			//can be converted to an rvalue of type "pointer to member of D of type cv T" where D is a
			//derived class of B
			ICPPPointerToMemberType spm = (ICPPPointerToMemberType) s;
			ICPPPointerToMemberType tpm = (ICPPPointerToMemberType) t;
			IType st = spm.getType();
			IType tt = tpm.getType();
			if (st != null && tt != null && st.isSameType(tt)) {
				int depth= calculateInheritanceDepth(CPPSemantics.MAX_INHERITANCE_DEPTH, tpm.getMemberOfClass(), spm.getMemberOfClass());
				cost.rank= (depth > -1) ? Cost.CONVERSION_RANK : Cost.NO_MATCH_RANK;
				cost.conversion= (depth > -1) ? depth : 0;
				cost.detail= 1;
			}
		}
	}
	
	/**
	 * [13.3.3.1-6] Derived to base conversion
	 * @param cost
	 * @throws DOMException
	 */
	private static final void derivedToBaseConversion(Cost cost) throws DOMException {
		IType s = getUltimateType(cost.source, true);
		IType t = getUltimateType(cost.target, true);

		if (cost.targetHadReference && s instanceof ICPPClassType && t instanceof ICPPClassType) {
			int depth= calculateInheritanceDepth(CPPSemantics.MAX_INHERITANCE_DEPTH, s, t);
			if (depth > -1) {
				cost.rank = Cost.DERIVED_TO_BASE_CONVERSION;
				cost.conversion = depth;
			}
		}
	}
	
	/**
	 * @param type
	 * @return whether the specified type has an associated definition
	 */
	private static final boolean isCompleteType(IType type) {
		type= getUltimateType(type, false);
		if (type instanceof ICPPClassType) {
			if (type instanceof ICPPInternalBinding) {
				return (((ICPPInternalBinding)type).getDefinition() != null);
			}
			if (type instanceof IIndexFragmentBinding) {
				try {
					return ((IIndexFragmentBinding)type).hasDefinition();
				} catch(CoreException ce) {
					CCorePlugin.log(ce);
				}
			}
		}
		
		return true;
	}

	/**
	 * Allows any very loose matching between template parameters.
	 * @param cost
	 */
	private static final void relaxTemplateParameters(Cost cost) {
		IType s = getUltimateType(cost.source, false);
		IType t = getUltimateType(cost.target, false);

		if ((s instanceof ICPPTemplateTypeParameter && t instanceof ICPPTemplateTypeParameter) ||
				(s instanceof ICPPTemplateTemplateParameter && t instanceof ICPPTemplateTemplateParameter)) {
			cost.rank = Cost.FUZZY_TEMPLATE_PARAMETERS;
		}
	}
}
