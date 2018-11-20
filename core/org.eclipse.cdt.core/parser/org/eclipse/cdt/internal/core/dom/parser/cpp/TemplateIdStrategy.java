/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others.
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
 *    Nathan Ridge  - added comments and fixed bug 445177
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.BitSet;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser.ITemplateIdStrategy;

/**
 * This class is used to track alternatives for parsing segments of code that involve '<' tokens.
 *
 * The '<' token can be either a less-than operator or part of a template-id.
 * When parsing, we potentially need to consider both possibilities for each use of '<'.
 *
 * An instance of this class is used to track alternative parses in a segment of code that includes one or
 * more uses of '<' preceded by names. An alternative consists of a choice (template-id or not) for each
 * name. At a given point in time, the instance has a notion of a current alternative, and a current
 * position within that alternative.
 *
 * @see also NameOrTemplateIDVariants, which is used together with this class to deal with ambiguities
 * involving '<' when parsing in an expression context.
 */
final class TemplateIdStrategy implements ITemplateIdStrategy {
	// The current position in the current alternative.
	// The next call to shallParseAsTemplateID() will return whether in the current alternative,
	// the name at index (fCurrentBranchPoint + 1) should be parsed as a template-id.
	private int fCurrentBranchPoint;

	// The current alternative, represented as a bitset with one bit for each name.
	// A bit corresponding to a name is clear if the name should be parsed as a template-id, and set if
	// it should not.
	// For the first alternative, this bitset is null, and this is interpreted as all zeros (i.e. every name
	// is parsed as a template-id).
	private BitSet fSimpleIDs;

	// The set of names which are parsed as template-ids in the current alternative.
	private IASTName[] fTemplateNames;

	public TemplateIdStrategy() {
		fCurrentBranchPoint = -1;
		fTemplateNames = IASTName.EMPTY_NAME_ARRAY;
	}

	/**
	 * Returns whether 'name' should be parsed as a template-id according to the current alternative.
	 * For each alternative, this is expected to be called once for each name in a segment code for which
	 * this choice is ambiguous.
	 * For the first alternative, these calls are used to initially populate the list of names.
	 * For subsequent alternatives, these calls are expected for the same names in the same order.
	 */
	@Override
	public boolean shallParseAsTemplateID(IASTName name) {
		fCurrentBranchPoint++;

		// 'fSimpleIDs == null' means we're on the first alternative.
		// On the first alternative, everything is parsed as a template-id.
		boolean templateID = fSimpleIDs == null || !fSimpleIDs.get(fCurrentBranchPoint);
		if (templateID) {
			fTemplateNames = ArrayUtil.append(fTemplateNames, name);
		}
		return templateID;
	}

	/**
	 * Advance to the next alternative parse, or return false is there is none.
	 * After a call to this, the current position is reset to the beginning of the (next) alternative.
	 * @param previousAlternativeFailedToParse whether this function is being called because the previous
	 *                                         alternative failed to parse
	 */
	public boolean setNextAlternative(boolean previousAlternativeFailedToParse) {
		// No one has called shallParseAsTemplateID() for the current alternative, so there are no
		// ambiguous names. Therefore, there are no more alternatives.
		if (fCurrentBranchPoint < 0) {
			return false;
		}

		// Reset the current position, saving the old one, which should point to the last name in the
		// bitset for which parsing was attempted during the previous alternative.
		int bp = fCurrentBranchPoint;
		fCurrentBranchPoint = -1;

		// Reset the list of names that were parsed as template-ids, saving the list for the previous
		// alternative.
		IASTName[] names = getTemplateNames();
		// Note that 'names' here contains the list of names for which there is a '0' in the bitset.
		int nameLen = names.length;
		fTemplateNames = IASTName.EMPTY_NAME_ARRAY;

		// If the previous alternative was the first, the bitset is still null. Create it.
		if (fSimpleIDs == null) {
			fSimpleIDs = new BitSet();
		}

		// Advance to the next alternative by finding the right-most '0' in the bitset, and setting it to '1',
		// and bits to the right of it to '0'. In this way, successive calls to this function will iterate
		// over all possible alternatives.
		// Note that in searching for the right-most '0', we start at 'bp', not the last element of the
		// bitset. The reason is that if 'bp' is not the last element of the bitset, it means that during the
		// previous alternative, we failed the parse before getting beyond 'bp'. This means that there is a
		// problem with one of the choices up to and including 'bp', so there's no point in trying another
		// alternatives that keeps these choices the same.
		while (bp >= 0) {
			if (!fSimpleIDs.get(bp)) {
				// Optimization (bug 363609): if during the previous alternative, a name was parsed as a
				// template-id with multiple template arguments, it's not going to be parsed differently in
				// a subsequent alternative, so keep it as a template-id.
				// Of course, this optimization is only possible if the previous alternative was parsed
				// successfully (bug 445177).
				// TODO: This optimization is invalid since it triggers bug 497931.
				if (previousAlternativeFailedToParse || nameLen == 0 || !hasMultipleArgs(names[--nameLen])) {
					fSimpleIDs.clear(bp + 1, Integer.MAX_VALUE);
					fSimpleIDs.set(bp);
					return true;
				}
			}
			bp--;
		}

		// The bitset was all ones - there are no more alternatives.
		return false;
	}

	private boolean hasMultipleArgs(IASTName templateName) {
		IASTNode parent = templateName.getParent();
		if (parent instanceof ICPPASTTemplateId) {
			return ((ICPPASTTemplateId) parent).getTemplateArguments().length > 1;
		}
		return false;
	}

	public IASTName[] getTemplateNames() {
		return ArrayUtil.trim(fTemplateNames);
	}

	/**
	 * Sometimes, a BacktrackException can be thrown and handled during the processing
	 * of a single alternative (that is, the exception does not bubble up all the way
	 * to the point where setNextAlternative() would be called). In such a case, when
	 * backtracking we need to restore the branch point that was active at the point
	 * we're backing up to (otherwise, the current branch point could get out of sync
	 * with the parsing position). These methods facilitate marking and backing up to
	 * the current branch point for such situations.
	 */
	public int getCurrentBranchPoint() {
		return fCurrentBranchPoint;
	}

	public void backupToBranchPoint(int branchPoint) {
		fCurrentBranchPoint = branchPoint;
	}
}