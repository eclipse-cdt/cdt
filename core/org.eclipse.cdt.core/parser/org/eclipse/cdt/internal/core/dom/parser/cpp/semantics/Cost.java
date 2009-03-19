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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

/**
 * The cost of an implicit conversion sequence.
 * 
 * See [over.best.ics] 13.3.3.1.
 */
final class Cost {
	enum Rank {
		IDENTITY, LVALUE_TRANSFORMATION, PROMOTION, CONVERSION, CONVERSION_PTR_BOOL, 
		USER_DEFINED_CONVERSION, ELLIPSIS_CONVERSION, NO_MATCH
	}

	IType source;
	IType target;

	private Rank fRank;
	private Rank fSecondStandardConversionRank;
	private boolean fAmbiguousUserdefinedConversion;
	private int fQualificationAdjustments;
	private int fInheritanceDistance;
	private ICPPFunction fUserDefinedConversion;
	
	public Cost(IType s, IType t, Rank rank) {
		source = s;
		target = t;
		fRank= rank;
	}

	public Rank getRank() {
		return fRank;
	}

	public void setRank(Rank rank) {
		fRank= rank;
	}
	
	public boolean isAmbiguousUserdefinedConversion() {
		return fAmbiguousUserdefinedConversion;
	}

	public void setAmbiguousUserdefinedConversion(boolean val) {
		fAmbiguousUserdefinedConversion= val;
	}

	public int getInheritanceDistance() {
		return fInheritanceDistance;
	}

	public void setInheritanceDistance(int inheritanceDistance) {
		fInheritanceDistance = inheritanceDistance;
	}

	public void setQualificationAdjustment(int adjustment) {
		fQualificationAdjustments= adjustment;
		if (adjustment != 0 && fRank == Rank.IDENTITY) { 
			fRank= Rank.LVALUE_TRANSFORMATION;
		}
	}

	/**
	 * Converts the cost for the second standard conversion into the overall cost for the
	 * implicit conversion sequence.
	 */
	public void setUserDefinedConversion(ICPPMethod conv) {
		fUserDefinedConversion= conv;
		if (conv != null) {
			fSecondStandardConversionRank= fRank;
			fRank= Rank.USER_DEFINED_CONVERSION;
		}
	}

	/**
	 * Returns an integer &lt 0 if other cost is <code>null</code>, or this cost is smaller than the other cost,
	 *        0 if this cost is equal to the other cost,
	 *        an integer &gt 0 if this cost is larger than the other cost.
	 */
	public int compare(Cost other) throws DOMException {
		if (other == null)
			return -1;
		
		int cmp= fRank.compareTo(other.fRank);
		if (cmp != 0) 
			return cmp;
		
		// rank is equal
		if (fRank == Rank.USER_DEFINED_CONVERSION) {
			// 13.3.3.1.10
			if (isAmbiguousUserdefinedConversion() || other.isAmbiguousUserdefinedConversion())
				return 0;
			
			if (!fUserDefinedConversion.equals(other.fUserDefinedConversion))
				return 0;
			
			cmp= fSecondStandardConversionRank.compareTo(other.fSecondStandardConversionRank);
			if (cmp != 0)
				return cmp;
		}
		
		cmp= fInheritanceDistance - other.fInheritanceDistance;
		if (cmp != 0)
			return cmp;

		int qdiff= fQualificationAdjustments ^ other.fQualificationAdjustments;
		if (qdiff != 0) {
			if ((fQualificationAdjustments & qdiff) == 0)
				return -1;
			if ((other.fQualificationAdjustments & qdiff) == 0)
				return 1;
		}
		return 0;
	}
}