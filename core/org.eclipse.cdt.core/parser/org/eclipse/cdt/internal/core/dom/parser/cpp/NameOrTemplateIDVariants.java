/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser.BinaryOperator;

/**
 * Tracks variants of expressions due to the ambiguity between template-id and '<' operator.
 */
public class NameOrTemplateIDVariants {
	/**
	 * A point where a '<' can be interpreted as less-than or as the angle-bracket of a template-id.
	 */
	static class BranchPoint {
		private BranchPoint fNext;
		private Variant fFirstVariant;
		private final boolean fAllowAssignment;
		private final int fConditionCount;
		private final BinaryOperator fLeftOperator;

		BranchPoint(BranchPoint next, Variant variant, BinaryOperator left, boolean allowAssignment,
				int conditionCount) {
			fNext = next;
			fFirstVariant = variant;
			fAllowAssignment = allowAssignment;
			fConditionCount = conditionCount;
			fLeftOperator = left;
			// Set owner
			while (variant != null) {
				variant.fOwner = this;
				variant = variant.getNext();
			}
		}

		public boolean isAllowAssignment() {
			return fAllowAssignment;
		}

		public int getConditionCount() {
			return fConditionCount;
		}

		public BinaryOperator getLeftOperator() {
			return fLeftOperator;
		}

		public Variant getFirstVariant() {
			return fFirstVariant;
		}

		public BranchPoint getNext() {
			return fNext;
		}

		public void reverseVariants() {
			Variant prev = null;
			Variant curr = fFirstVariant;
			while (curr != null) {
				Variant next = curr.getNext();
				curr.fNext = prev;
				prev = curr;
				curr = next;
			}
			fFirstVariant = prev;
		}
	}

	/**
	 * A variant for a branch-point is a cast-expression that can be used within a binary expression.
	 */
	static class Variant {
		private BranchPoint fOwner;
		private Variant fNext;
		private final IASTExpression fExpression;
		private BinaryOperator fTargetOperator;
		private final int fRightOffset;
		private final IASTName[] fTemplateNames;

		public Variant(Variant next, IASTExpression expr, IASTName[] templateNames, int rightOffset) {
			fNext = next;
			fExpression = expr;
			fRightOffset = rightOffset;
			fTemplateNames = templateNames;
		}

		public BranchPoint getOwner() {
			return fOwner;
		}

		public int getRightOffset() {
			return fRightOffset;
		}

		public IASTName[] getTemplateNames() {
			return fTemplateNames;
		}

		public Variant getNext() {
			return fNext;
		}

		public IASTExpression getExpression() {
			return fExpression;
		}

		public BinaryOperator getTargetOperator() {
			return fTargetOperator;
		}

		public void setTargetOperator(BinaryOperator lastOperator) {
			fTargetOperator = lastOperator;
		}
	}

	private BranchPoint fFirst;

	public boolean isEmpty() {
		return fFirst == null;
	}

	public void addBranchPoint(Variant variants, BinaryOperator left, boolean allowAssignment, int conditionCount) {
		fFirst = new BranchPoint(fFirst, variants, left, allowAssignment, conditionCount);
	}

	public void closeVariants(int offset, BinaryOperator lastOperator) {
		for (BranchPoint p = fFirst; p != null; p = p.getNext()) {
			for (Variant v = p.getFirstVariant(); v != null; v = v.getNext()) {
				if (v.getTargetOperator() == null) {
					if (offset == v.getRightOffset()) {
						v.setTargetOperator(lastOperator);
					}
				}
			}
		}
	}

	public Variant selectFallback() {
		// Search for an open variant, with a small right offset and a large left offset
		for (BranchPoint p = fFirst; p != null; p = p.getNext()) {
			Variant best = null;
			for (Variant v = p.getFirstVariant(); v != null; v = v.getNext()) {
				if (v.getTargetOperator() == null) {
					if (best == null || v.fRightOffset < best.fRightOffset) {
						best = v;
					}
				}
			}
			if (best != null) {
				remove(best);
				return best;
			}
		}
		return null;
	}

	private void remove(Variant remove) {
		final BranchPoint owner = remove.fOwner;
		final Variant next = remove.getNext();
		Variant prev = owner.getFirstVariant();
		if (remove == prev) {
			owner.fFirstVariant = next;
			if (next == null) {
				remove(owner);
			}
		} else {
			while (prev != null) {
				Variant n = prev.getNext();
				if (n == remove) {
					prev.fNext = next;
					break;
				}
				prev = n;
			}
		}
	}

	private void remove(BranchPoint remove) {
		final BranchPoint next = remove.getNext();
		if (remove == fFirst) {
			fFirst = next;
		} else {
			BranchPoint prev = fFirst;
			while (prev != null) {
				BranchPoint n = prev.getNext();
				if (n == remove) {
					prev.fNext = next;
					break;
				}
				prev = n;
			}
		}
	}

	public BranchPoint getOrderedBranchPoints() {
		BranchPoint prev = null;
		BranchPoint curr = fFirst;
		while (curr != null) {
			curr.reverseVariants();
			BranchPoint next = curr.getNext();
			curr.fNext = prev;
			prev = curr;
			curr = next;
		}
		fFirst = null;
		return prev;
	}

	public boolean hasRightBound(int opOffset) {
		// Search for an open variant, with a small right offset and a large left offset
		for (BranchPoint p = fFirst; p != null; p = p.getNext()) {
			for (Variant v = p.getFirstVariant(); v != null; v = v.getNext()) {
				if (v.fRightOffset > opOffset)
					return false;
			}
		}
		return true;
	}

	public void removeInvalid(BinaryOperator lastOperator) {
		for (BranchPoint p = fFirst; p != null; p = p.getNext()) {
			if (!isReachable(p, lastOperator)) {
				remove(p);
			} else {
				for (Variant v = p.getFirstVariant(); v != null; v = v.getNext()) {
					if (v.getTargetOperator() == null) {
						remove(v);
					}
				}
			}
		}
	}

	private boolean isReachable(BranchPoint bp, BinaryOperator endOperator) {
		BinaryOperator op = bp.getLeftOperator();
		if (op == null)
			return true;

		for (; endOperator != null; endOperator = endOperator.getNext()) {
			if (endOperator == op)
				return true;
		}
		return false;
	}
}
