/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.Context;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.UDCMode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Cost.Rank;

class AggregateInitialization {
	private ICPPEvaluation[] fInitializers; // TODO remove
	private int iInitializer;
	private Cost fWorstCost;

	public AggregateInitialization(/*EvalInitList list*/) {
		iInitializer = 0;
	}

	/**
	 * Checks whether 'target' can be initialized from 'list' according to the rules for
	 * aggregate initialization ([dcl.init.aggr]).
	 */
	public Cost check(IType type, EvalInitList list) throws DOMException {
		fWorstCost = new Cost(list.getType(), type, Rank.IDENTITY);
		fInitializers = list.getClauses();

		Cost cost = checkInitializationOfElements(type, fWorstCost);
		if (!cost.converts())
			return cost;

		if (iInitializer < fInitializers.length)
			// p7: An initializer-list is ill-formed if the number of initializer-clauses exceeds
			// the number of members to initialize.
			return Cost.NO_CONVERSION;
		else
			return fWorstCost;
	}

	public Cost checkType(IType type) throws DOMException {
		return checkElement(type, null);
	}

	public Cost checkElement(IType type, IValue initialValue) throws DOMException {
		if (iInitializer >= fInitializers.length)
			// TODO for arrays we should probably short-circuit default init instead of initializing each element
			return checkInitializationFromDefaultMemberInitializer(type, initialValue);
		fWorstCost = new Cost(fInitializers[iInitializer].getType(), type, Rank.IDENTITY);

		if (fInitializers[iInitializer].isInitializerList() || !isAggregate(type)) { // no braces are elided
			// p3: The elements of the initializer list are taken as initializers for the elements
			//     of the aggregate, in order.
			ICPPEvaluation initializer = fInitializers[iInitializer];
			iInitializer++;
			Cost cost = Conversions.checkImplicitConversionSequence(type, initializer.getType(),
					initializer.getValueCategory(), UDCMode.ALLOWED, Context.ORDINARY);
			if (!cost.converts()) {
				return cost;
			}
			// If the initializer-clause is an expression and a narrowing conversion is
			// required to convert the expression, the program is ill-formed.
			if (!(initializer instanceof EvalInitList) && cost.isNarrowingConversion()) {
				return Cost.NO_CONVERSION;
			}
			if (cost.compareTo(fWorstCost) > 0) {
				fWorstCost = cost;
			}
		} else { // braces are elided: need to check on subaggregates
			Cost cost = checkInitializationOfElements(type, fWorstCost);
			if (!cost.converts())
				return cost;
			if (cost.compareTo(fWorstCost) > 0) {
				fWorstCost = cost;
			}
		}
		return fWorstCost;
	}

	private Cost checkInitializationOfElements(IType type, Cost worstCostdf) throws DOMException {
		if (type instanceof ICPPClassType && TypeTraits.isAggregateClass((ICPPClassType) type)) {
			ICPPField[] fields = Conversions.getFieldsForAggregateInitialization((ICPPClassType) type);
			for (ICPPField field : fields) {
				Cost cost = checkElement(field.getType(), field.getInitialValue());
				if (!cost.converts())
					return cost;
				if (cost.compareTo(fWorstCost) > 0) {
					fWorstCost = cost;
				}
			}
		} else if (type instanceof IArrayType) {
			IArrayType arrayType = (IArrayType) type;
			Number arraySize = arrayType.getSize().numberValue();
			if (arraySize != null)
				for (long i = 0; i < arraySize.longValue(); i++) {
					Cost cost = checkType(arrayType.getType());
					if (!cost.converts())
						return cost;
					if (cost.compareTo(fWorstCost) > 0) {
						fWorstCost = cost;
					}
				}
		}
		return fWorstCost;
	}

	private Cost checkInitializationFromDefaultMemberInitializer(IType type, IValue initialValue) throws DOMException {
		// p8: If there are fewer initializer-clauses than there are elements in the
		//     aggregate, then each element not explicitly initialized shall be
		//     initialized from its default member initializer or, if there is no
		//     default member initializer, from an empty initializer list.
		if (initialValue != null) {
			return fWorstCost; // has a default member initializer
		}

		// p11: If an incomplete or empty initializer-list leaves a member of
		//      reference type uninitialized, the program is ill-formed.
		IType fieldType = SemanticUtil.getNestedType(type, SemanticUtil.TDEF);
		if (fieldType instanceof ICPPReferenceType) {
			return Cost.NO_CONVERSION;
		}

		// Empty initializer list
		EvalInitList emptyInit = new EvalInitList(ICPPEvaluation.EMPTY_ARRAY, CPPSemantics.getCurrentLookupPoint());
		Cost cost = Conversions.listInitializationSequence(emptyInit, fieldType, UDCMode.ALLOWED, false);
		return cost;
	}

	private static boolean isAggregate(IType type) {
		return (type instanceof ICPPClassType && TypeTraits.isAggregateClass((ICPPClassType) type))
				|| type instanceof IArrayType;
	}
}