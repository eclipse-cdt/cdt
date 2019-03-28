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
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.Context;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.UDCMode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Cost.Rank;

class AggregateInitialization {
	private ICPPEvaluation[] fInitializers;
	private int fIndex = 0;

	/**
	 * Checks whether 'target' can be initialized from 'list' according to the rules for
	 * aggregate initialization ([dcl.init.aggr]).
	 */
	public static Cost check(IType target, EvalInitList list) throws DOMException {
		return new AggregateInitialization().checkImpl(target, list);
	}

	private AggregateInitialization() {
	}

	private Cost checkImpl(IType target, EvalInitList list) throws DOMException {
		fInitializers = list.getClauses();
		fIndex = 0;

		Cost worstCost = new Cost(list.getType(), target, Rank.IDENTITY);

		Cost cost = checkInitializationOfElements(target, worstCost);
		if (!cost.converts())
			return cost;

		if (fIndex < fInitializers.length)
			// p7: An initializer-list is ill-formed if the number of initializer-clauses exceeds
			// the number of members to initialize.
			return Cost.NO_CONVERSION;
		else
			return worstCost;
	}

	/**
	 * If no braces are elided, check initialization of element by taking the next clause from the EvalInitList,
	 * else recurses into the subaggregate.
	 */
	private Cost checkElement(IType type, IValue initialValue, Cost worstCost) throws DOMException {
		IType nestedType = SemanticUtil.getNestedType(type, SemanticUtil.TDEF);
		if (fIndex >= fInitializers.length)
			// TODO for arrays we could short-circuit default init instead of trying to init each element
			return checkInitializationFromDefaultMemberInitializer(nestedType, initialValue, worstCost);
		worstCost = new Cost(fInitializers[fIndex].getType(), nestedType, Rank.IDENTITY);

		if (fInitializers[fIndex].isInitializerList() || !isAggregate(nestedType)) { // no braces are elided
			// p3: The elements of the initializer list are taken as initializers for the elements
			//     of the aggregate, in order.
			ICPPEvaluation initializer = fInitializers[fIndex];
			fIndex++;
			Cost cost = Conversions.checkImplicitConversionSequence(nestedType, initializer.getType(),
					initializer.getValueCategory(), UDCMode.ALLOWED, Context.ORDINARY);
			if (!cost.converts()) {
				return cost;
			}
			// [dcl.init.aggr] If the initializer-clause is an expression and a narrowing conversion is
			// required to convert the expression, the program is ill-formed.
			if (!(initializer instanceof EvalInitList) && cost.isNarrowingConversion()) {
				return Cost.NO_CONVERSION;
			}
			if (cost.compareTo(worstCost) > 0) {
				worstCost = cost;
			}
		} else { // braces are elided: need to check on subaggregates
			Cost cost = checkInitializationOfElements(nestedType, worstCost);
			if (!cost.converts())
				return cost;
			if (cost.compareTo(worstCost) > 0) {
				worstCost = cost;
			}
		}
		return worstCost;
	}

	/**
	 * checkElement() for each element of an array or each field of a class aggregate.
	 */
	private Cost checkInitializationOfElements(IType type, Cost worstCost) throws DOMException {
		if (type instanceof ICPPClassType && TypeTraits.isAggregateClass((ICPPClassType) type)) {
			ICPPField[] fields = getFieldsForAggregateInitialization((ICPPClassType) type);
			for (ICPPField field : fields) {
				Cost cost = checkElement(field.getType(), field.getInitialValue(), worstCost);
				if (!cost.converts())
					return cost;
				if (cost.compareTo(worstCost) > 0) {
					worstCost = cost;
				}
			}
		} else if (type instanceof IArrayType) {
			IArrayType arrayType = (IArrayType) type;
			Number arraySize = arrayType.getSize().numberValue();
			if (arraySize != null)
				for (long i = 0; i < arraySize.longValue(); i++) {
					Cost cost = checkElement(arrayType.getType(), null, worstCost);
					if (!cost.converts())
						return cost;
					if (cost.compareTo(worstCost) > 0) {
						worstCost = cost;
					}
				}
		}
		return worstCost;
	}

	/**
	 * p8: If there are fewer initializer-clauses than there are elements in the
	 * aggregate, then each element not explicitly initialized shall be
	 * initialized from its default member initializer or, if there is no
	 * default member initializer, from an empty initializer list.
	 */
	private Cost checkInitializationFromDefaultMemberInitializer(IType type, IValue initialValue, Cost worstCost)
			throws DOMException {
		if (initialValue != null) {
			return worstCost; // has a default member initializer
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

	/**
	 * Get those fields of 'targetClass' which participate in aggregate initialization.
	 * These are the declared fields, excluding static fields and anonymous bit-fields
	 * ([decl.init.aggr] p6).
	 */
	private static ICPPField[] getFieldsForAggregateInitialization(ICPPClassType targetClass) {
		ICPPField[] fields = targetClass.getDeclaredFields();
		ICPPField[] result = fields;
		int j = 0;
		for (int i = 0; i < fields.length; ++i) {
			// TODO: Check for anonymous bit-fields. ICPPField doesn't currently expose whether
			//       it's a bit-field.
			if (fields[i].isStatic()) {
				if (fields == result) {
					result = new ICPPField[fields.length - 1];
					System.arraycopy(fields, 0, result, 0, i);
				}
			} else if (fields != result) {
				result[j] = fields[i];
				++j;
			}
		}
		return ArrayUtil.trim(result);
	}

}