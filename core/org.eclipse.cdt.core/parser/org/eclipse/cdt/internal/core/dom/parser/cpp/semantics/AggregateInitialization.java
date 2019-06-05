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
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
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
		assert !CPPTemplates.isDependentType(type);
		IType nestedType = SemanticUtil.getNestedType(type, SemanticUtil.TDEF);
		if (fIndex >= fInitializers.length)
			// TODO for arrays we could short-circuit default init instead of trying to init each element
			return checkInitializationFromDefaultMemberInitializer(nestedType, initialValue, worstCost);
		worstCost = new Cost(fInitializers[fIndex].getType(), nestedType, Rank.IDENTITY);

		ICPPEvaluation initializer = fInitializers[fIndex];
		if (initFromStringLiteral(nestedType, initializer)) {
			// [dcl.init.string]
			fIndex++;
			// nestedType is guaranteed to be an IArrayType if initFromStringLiteral() returns true
			Number sizeOfCharArrayNumber = getArraySize((IArrayType) nestedType);
			long sizeofCharArray = 0; // will error in case we cannot determine the size
			if (sizeOfCharArrayNumber != null) {
				sizeofCharArray = sizeOfCharArrayNumber.longValue();
			}
			// so is initializer.getType()
			Number sizeofStringLiteralNumber = getArraySize((IArrayType) initializer.getType());
			long sizeofStringLiteral = Long.MAX_VALUE; // will error in case we cannot determine the size
			if (sizeofStringLiteralNumber != null) {
				sizeofStringLiteral = sizeofStringLiteralNumber.longValue();
			}
			IType targetCharType = getBasicTypeFromArray(nestedType);
			IType literalCharType = getBasicTypeFromArray(initializer.getType());
			Cost cost;
			if (sizeofCharArray >= sizeofStringLiteral && targetCharType.isSameType(literalCharType)) {
				cost = new Cost(initializer.getType(), nestedType, Rank.CONVERSION);
			} else {
				cost = Cost.NO_CONVERSION;
			}
			return cost;
		}

		Cost costWithoutElision = Conversions.checkImplicitConversionSequence(nestedType, initializer.getType(),
				initializer.getValueCategory(), UDCMode.ALLOWED, Context.ORDINARY);
		if (costWithoutElision.converts()) {
			// p3: The elements of the initializer list are taken as initializers for the elements
			//     of the aggregate, in order.
			fIndex++;
			// [dcl.init.aggr] If the initializer-clause is an expression and a narrowing conversion is
			// required to convert the expression, the program is ill-formed.
			if (!initializer.isConstantExpression()) {
				if (!(initializer instanceof EvalInitList) && costWithoutElision.isNarrowingConversion()) {
					return Cost.NO_CONVERSION;
				}
			}
			if (costWithoutElision.compareTo(worstCost) > 0) {
				worstCost = costWithoutElision;
			}
		} else if (fInitializers[fIndex].isInitializerList() || !isAggregate(nestedType)) { // cannot elide braces
			return costWithoutElision; // doesn't convert
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
		if (type instanceof ICPPClassType && isAggregate(type)) {
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
			IValue sizeVal = arrayType.getSize();
			if (sizeVal != null) {
				Number arraySize = sizeVal.numberValue();
				if (arraySize != null) {
					for (long i = 0; i < arraySize.longValue(); i++) {
						Cost cost = checkElement(arrayType.getType(), null, worstCost);
						if (!cost.converts())
							return cost;
						if (cost.compareTo(worstCost) > 0) {
							worstCost = cost;
						}
					}
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
		return (type instanceof ICPPClassType && TypeTraits.isAggregateClass((ICPPClassType) type)
				&& !isSelfAggregate((ICPPClassType) type, null)) || type instanceof IArrayType;
	}

	// Check if a class is (illegally) aggregating itself
	private static boolean isSelfAggregate(ICPPClassType type, ICPPClassType subType) {
		if (type.isSameType(subType))
			return true;

		if (subType == null)
			subType = type;
		for (ICPPField field : subType.getDeclaredFields()) {
			IType fieldType = field.getType();
			if (fieldType instanceof ICPPClassType && TypeTraits.isAggregateClass((ICPPClassType) fieldType)) {
				if (isSelfAggregate(type, (ICPPClassType) fieldType))
					return true;
			}
		}
		return false;
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

	/**
	 * @param type
	 * @return CPPBasicType of element; null if type is not IArrayType or element is not CPPBasicType
	 */
	private static ICPPBasicType getBasicTypeFromArray(IType type) {
		if (type instanceof IArrayType) {
			IType nested = SemanticUtil.getNestedType(((IArrayType) type).getType(),
					SemanticUtil.ALLCVQ | SemanticUtil.TDEF);
			if (nested instanceof ICPPBasicType) {
				return (ICPPBasicType) nested;
			}
		}
		return null;
	}

	private static boolean isCharArray(IType target) {
		ICPPBasicType t = getBasicTypeFromArray(target);
		if (t != null) {
			Kind k = t.getKind();
			return k == Kind.eChar || k == Kind.eChar16 || k == Kind.eChar32 || k == Kind.eWChar;
		}
		return false;
	}

	private static boolean fromStringLiteral(ICPPEvaluation initializer) {
		ICPPBasicType t = getBasicTypeFromArray(initializer.getType());
		if (t != null && t instanceof CPPBasicType) {
			return ((CPPBasicType) t).isFromStringLiteral();
		}
		return false;
	}

	private static boolean initFromStringLiteral(IType target, ICPPEvaluation initializer) {
		return isCharArray(target) && fromStringLiteral(initializer);
	}

	private static Number getArraySize(IArrayType type) {
		IValue size = type.getSize();
		if (size != null) {
			return size.numberValue();
		}
		return null;
	}
}