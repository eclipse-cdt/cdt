/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Cost.Rank;

/**
 * Cost for the entire function call
 */
class FunctionCost {
	private final IFunction fFunction;
	private final Cost[] fCosts;
	
	public FunctionCost(IFunction fn, int paramCount) {
		fFunction= fn;
		fCosts= new Cost[paramCount];
	}
	
	public int getLength() {
		return fCosts.length;
	}
	
	public Cost getCost(int idx) {
		return fCosts[idx];
	}
	
	public void setCost(int idx, Cost cost) {
		fCosts[idx]= cost;
	}

	public IFunction getFunction() {
		return fFunction;
	}
	
	public boolean hasAmbiguousUserDefinedConversion() {
		for (Cost cost : fCosts) {
			if (cost.isAmbiguousUserdefinedConversion())
				return true;
		}
		return false;
	}

	/**
	 * Compares this function call cost to another one.
	 */
	public int compareTo(LookupData data, FunctionCost other) throws DOMException {
		if (other == null)
			return -1;
		
		boolean haveWorse = false;
		boolean haveBetter = false;
		// In order for this function to be better than the previous best, it must
		// have at least one parameter match that is better that the corresponding
		// match for the other function, and none that are worse.
		int idx= getLength()-1;
		int idxOther= other.getLength()-1;
		for (; idx>=0 && idxOther>=0; idx--,idxOther--) {
			Cost cost= getCost(idx);
			if (cost.getRank() == Rank.NO_MATCH) {
				haveWorse = true;
				haveBetter = false;
				break;
			}

			int cmp = cost.compare(other.getCost(idxOther));
			haveWorse |= (cmp > 0);
			haveBetter |= (cmp < 0);
		}
	
		if (!haveWorse && !haveBetter) {
			// If they are both template functions, we can order them that way
			ICPPFunctionTemplate asTemplate= asTemplate(getFunction());
			ICPPFunctionTemplate otherAsTemplate= asTemplate(other.getFunction());
			final boolean isTemplate = asTemplate != null;
			final boolean otherIsTemplate = otherAsTemplate != null;
			
			// Prefer normal functions over template functions
			if (isTemplate && !otherIsTemplate) {
				haveWorse = true;
			} else if (!isTemplate && otherIsTemplate) {
				haveBetter = true;
			} else if (isTemplate && otherIsTemplate) {
				int order = CPPTemplates.orderTemplateFunctions(otherAsTemplate, asTemplate);
				if (order < 0) {
					haveBetter= true;	 				
				} else if (order > 0) {
					haveWorse= true;
				}
			} 
		}
		
		// if we are ambiguous at this point prefer non-index bindings
		if (haveBetter == haveWorse) {
			return -CPPSemantics.compareByRelevance(data, getFunction(), other.getFunction());
		}
		
		if (haveBetter) 
			return -1;
		
		return 1;
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
}
