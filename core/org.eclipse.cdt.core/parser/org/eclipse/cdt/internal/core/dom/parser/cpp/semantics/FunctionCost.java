/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates.TypeSelection.PARAMETERS;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates.TypeSelection.RETURN_TYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates.TypeSelection;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Cost.DeferredUDC;

/**
 * Cost for the entire function call
 */
class FunctionCost {
	private final ICPPFunction fFunction;
	private final Cost[] fCosts;
	private final ValueCategory[] fValueCategories;
	private final IASTNode fPoint;
	private boolean fIsDirectCopyCtor;
	
	public FunctionCost(ICPPFunction fn, int paramCount, IASTNode point) {
		fFunction= fn;
		fCosts= new Cost[paramCount];
		fValueCategories= new ValueCategory[paramCount];
		fPoint = point;
	}
	
	public FunctionCost(ICPPFunction fn, Cost cost, IASTNode point) {
		fFunction= fn;
		fCosts= new Cost[] {cost};
		fValueCategories= null; // no udc will be performed
		fPoint = point;
	}

	public int getLength() {
		return fCosts.length;
	}
	
	public Cost getCost(int idx) {
		return fCosts[idx];
	}
	
	public void setCost(int idx, Cost cost, ValueCategory valueCat) {
		fCosts[idx]= cost;
		fValueCategories[idx]= valueCat;
	}

	public ICPPFunction getFunction() {
		return fFunction;
	}
	
	public boolean hasAmbiguousUserDefinedConversion() {
		for (Cost cost : fCosts) {
			if (cost.isAmbiguousUDC())
				return true;
		}
		return false;
	}

	public boolean hasDeferredUDC() {
		for (Cost cost : fCosts) {
			if (!cost.converts())
				return false;
			if (cost.isDeferredUDC() != DeferredUDC.NONE)
				return true;
		}
		return false;
	}
	
	public boolean performUDC(IASTNode point) throws DOMException {
		for (int i = 0; i < fCosts.length; i++) {
			Cost cost = fCosts[i];
			Cost udcCost= null;
			switch(cost.isDeferredUDC()) {
			case NONE: 
				continue;
			case COPY_INIT_OF_CLASS:
				udcCost = Conversions.copyInitializationOfClass(fValueCategories[i], cost.source,
						(ICPPClassType) cost.target, false, point);
				break;
			case INIT_BY_CONVERSION:
				IType uqSource= getNestedType(cost.source, TDEF | REF | CVTYPE);
				udcCost = Conversions.initializationByConversion(fValueCategories[i], cost.source,
						(ICPPClassType) uqSource, cost.target, false, point);
				break;
			case LIST_INIT_OF_CLASS:
				udcCost = Conversions.listInitializationOfClass(((InitializerListType) cost.source).getEvaluation(), 
						(ICPPClassType) cost.target, false, false, point); 
				break;
			case DIRECT_LIST_INIT_OF_CLASS:
				udcCost = Conversions.listInitializationOfClass(((InitializerListType) cost.source).getEvaluation(), 
						(ICPPClassType) cost.target, true, false, point); 
				break;
			default:
				return false;
			}
			fCosts[i] = udcCost;
			if (!udcCost.converts()) {
				return false;
			}
			udcCost.setReferenceBinding(cost.getReferenceBinding());
		}
		return true;
	}

	/**
	 * Compares this function call cost to another one.
	 */
	public int compareTo(IASTTranslationUnit tu, FunctionCost other) throws DOMException {
		if (other == null)
			return -1;
		
		boolean haveWorse = false;
		boolean haveBetter = false;
		// In order for this function to be better than the previous best, it must
		// have at least one parameter match that is better that the corresponding
		// match for the other function, and none that are worse.
		int idx= getLength() - 1;
		int idxOther= other.getLength() - 1;
		for (; idx >= 0 && idxOther >= 0; idx--, idxOther--) {
			Cost cost= getCost(idx);
			if (!cost.converts()) {
				haveWorse = true;
				haveBetter = false;
				break;
			}

			int cmp = cost.compareTo(other.getCost(idxOther));
			haveWorse |= (cmp > 0);
			haveBetter |= (cmp < 0);
		}
	
		final ICPPFunction f1 = getFunction();
		final ICPPFunction f2 = other.getFunction();
		if (!haveWorse && !haveBetter) {
			// If they are both template functions, we can order them that way
			ICPPFunctionTemplate asTemplate= asTemplate(f1);
			ICPPFunctionTemplate otherAsTemplate= asTemplate(f2);
			final boolean isTemplate = asTemplate != null;
			final boolean otherIsTemplate = otherAsTemplate != null;
			
			// Prefer normal functions over template functions
			if (isTemplate && !otherIsTemplate) {
				haveWorse = true;
			} else if (!isTemplate && otherIsTemplate) {
				haveBetter = true;
			} else if (isTemplate && otherIsTemplate) {
				TypeSelection ts= SemanticUtil.isConversionOperator(f1) ? RETURN_TYPE : PARAMETERS;
 				int order = CPPTemplates.orderFunctionTemplates(otherAsTemplate, asTemplate, ts, fPoint);
				if (order < 0) {
					haveBetter= true;	 				
				} else if (order > 0) {
					haveWorse= true;
				}
			} 
		}
		
		if (haveBetter == haveWorse) {
			// 7.3.3-15 Using declarations in classes can be overridden
			int cmp= overridesUsingDeclaration(f1, f2);
			if (cmp != 0)
				return cmp;
			
			// At this point prefer non-index bindings
			return -CPPSemantics.compareByRelevance(tu, f1, f2);
		}
		
		if (haveBetter) 
			return -1;
		
		return 1;
	}
	
	private int overridesUsingDeclaration(ICPPFunction f1, ICPPFunction f2) {
		if (f1.takesVarArgs() != f2.takesVarArgs())
			return 0;
		if (!(f1 instanceof ICPPMethod && f2 instanceof ICPPMethod)) 
			return 0;
		
		final ICPPMethod m1 = (ICPPMethod) f1;
		final ICPPMethod m2 = (ICPPMethod) f2;
		ICPPClassType o1= m1.getClassOwner();
		ICPPClassType o2= m2.getClassOwner();
		if (o1.isSameType(o2)) 
			return 0;
		
		final ICPPFunctionType ft1 = m1.getType();
		final ICPPFunctionType ft2 = m2.getType();
		if (ft1.isConst() != ft2.isConst() || ft2.isVolatile() != ft2.isVolatile())
			return 0;
		
		if (!parameterTypesMatch(ft1, ft2))
			return 0;
		
		int diff= SemanticUtil.calculateInheritanceDepth(o2, o1, fPoint);
		if (diff >= 0)
			return diff;
		return -SemanticUtil.calculateInheritanceDepth(o1, o2, fPoint);
	}

	private boolean parameterTypesMatch(final ICPPFunctionType ft1, final ICPPFunctionType ft2) {
		IType[] p1= ft1.getParameterTypes();
		IType[] p2= ft2.getParameterTypes();
		if (p1.length != p2.length) {
			if (p1.length == 0) 
				return p2.length == 1 && SemanticUtil.isVoidType(p2[0]);
			if (p2.length == 0) 
				return p1.length == 1 && SemanticUtil.isVoidType(p1[0]);
			return false;
		} 
		
		for (int i = 0; i < p2.length; i++) {
			if (!p1[i].isSameType(p2[i])) {
				return false;
			}
		}
		return true;
	}

	public boolean mustBeWorse(FunctionCost other) {
		if (other == null)
			return false;
		
		boolean haveWorse= false;
		int idx= getLength() - 1;
		int idxOther= other.getLength() - 1;
		for (; idx >= 0 && idxOther >= 0; idx--, idxOther--) {
			Cost cost= getCost(idx);
			if (!cost.converts()) 
				return true;
			
			Cost otherCost= other.getCost(idxOther);
			
			int cmp;
			if (cost.isDeferredUDC() != DeferredUDC.NONE) {
				cmp= cost.getRank().compareTo(otherCost.getRank());
			} else {
				cmp= cost.compareTo(otherCost);
			}
			
			if (cmp < 0)
				return false;
			if (cmp > 0)
				haveWorse= true;
		}
		
		return haveWorse;
	}

	private static ICPPFunctionTemplate asTemplate(IFunction function) {
		if (function instanceof ICPPSpecialization) {
			IBinding original= ((ICPPSpecialization) function).getSpecializedBinding();
			if (original instanceof ICPPFunctionTemplate) {
				return (ICPPFunctionTemplate) original;
			}
		}
		return null;
	}

	public void setIsDirectInitWithCopyCtor(boolean val) {
		fIsDirectCopyCtor= val;
	}
	
	public boolean isDirectInitWithCopyCtor() {
		return fIsDirectCopyCtor;
	}
}
