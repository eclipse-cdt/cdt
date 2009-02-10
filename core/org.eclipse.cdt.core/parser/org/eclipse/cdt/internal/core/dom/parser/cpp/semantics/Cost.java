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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;

/**
 * The cost of an implicit conversion sequence.
 * 
 * See [over.best.ics] 13.3.3.1.
 */
class Cost {
	//Some constants to help clarify things
	public static final int NO_USERDEFINED_CONVERSION = 0;
	public static final int AMBIGUOUS_USERDEFINED_CONVERSION = 1;
	public static final int USERDEFINED_CONVERSION = 2;
	
	public static final int NO_MATCH_RANK = -1;
	public static final int IDENTITY_RANK = 0;
	public static final int LVALUE_OR_QUALIFICATION_RANK = 0;
	public static final int PROMOTION_RANK = 1;
	public static final int CONVERSION_RANK = 2;
	public static final int DERIVED_TO_BASE_CONVERSION = 3;
	public static final int USERDEFINED_CONVERSION_RANK = 4;
	public static final int ELLIPSIS_CONVERSION = 5;
	
	public IType source;
	public IType target;
	
	public int lvalue;
	public int promotion;
	public int conversion;
	public int qualification;
	public int userDefined= NO_USERDEFINED_CONVERSION;
	public int rank = -1;
	public int detail;
	
	public Cost( IType s, IType t ){
		source = s;
		target = t;
	}

	public int compare( Cost cost ) throws DOMException{
		int result = 0;
		
		if( rank != cost.rank ){
			return cost.rank - rank;
		}
		
		if (userDefined == cost.userDefined) {
			if (userDefined == AMBIGUOUS_USERDEFINED_CONVERSION) {
				return 0;
			}
			// same or no userconversion --> rank on standard conversion sequence.
		}
		else {
			if (userDefined == NO_USERDEFINED_CONVERSION || cost.userDefined == NO_USERDEFINED_CONVERSION) {
				return cost.userDefined - userDefined;
			}
			// one ambiguous, the other needs conversion --> can't use std conversion to rank
			return 0;
		}
		
		if( promotion > 0 || cost.promotion > 0 ){
			result = cost.promotion - promotion;
		}
		if( conversion > 0 || cost.conversion > 0 ){
			if( detail == cost.detail ){
				result = cost.conversion - conversion;
			} else {
				result = cost.detail - detail;
			}
		}
		
		if( result == 0 ){
			if( cost.qualification != qualification ){
				return cost.qualification - qualification;
			} 
			if( qualification == 0 ){
				return 0;
			} 
			
			// something is wrong below:
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=226877

			IPointerType op1, op2;
			IType t1 = cost.target, t2 = target;
			int subOrSuper = 0;
			while (true) {
				op1 = null;
				op2 = null;
				while (true) {
					if (t1 instanceof ITypedef) {
						t1 = ((ITypedef) t1).getType();
					} else {
						if (t1 instanceof IPointerType)
							op1 = (IPointerType) t1;
						break;
					}
				}
				while (true) {
					if (t2 instanceof ITypedef) {
						t2 = ((ITypedef) t2).getType();
					} else {
						if (t2 instanceof IPointerType)
							op2 = (IPointerType) t2;
						break;
					}
				}
				if (op1 == null || op2 == null)
					break;

				int cmp = (op1.isConst() ? 1 : 0) + (op1.isVolatile() ? 1 : 0)
						- (op2.isConst() ? 1 : 0) + (op2.isVolatile() ? 1 : 0);

				if (cmp != 0) {
					if (subOrSuper == 0) {
						subOrSuper = cmp;
					}
					else if ((subOrSuper > 0) != (cmp > 0)) {
						return 0;
					}
				}
				t1= op1.getType();
				t2= op2.getType();
			}
			if (op1 != null) { 
				return 1;
			} else if (op2 != null) {
				return -1;
			}
			return 0;
		}
		 
		return result;
	}
}