/*******************************************************************************
 * Copyright (c) 2007, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree.IASTInclusionNode;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.IntArray;

/**
 * Base class for all location contexts that can contain children.
 *
 * @since 5.0
 */
class LocationCtxContainer extends LocationCtx {
	/**
	 * The total length of all children in terms of sequence numbers.
	 */
	private int fChildSequenceLength;

	private ArrayList<LocationCtx> fChildren;
	private final AbstractCharArray fSource;
	private int[] fLineOffsets;

	public LocationCtxContainer(LocationCtxContainer parent, AbstractCharArray source, int parentOffset,
			int parentEndOffset, int sequenceNumber) {
		super(parent, parentOffset, parentEndOffset, sequenceNumber);
		fSource = source;
	}

	@Override
	public Collection<LocationCtx> getChildren() {
		if (fChildren == null) {
			return Collections.emptyList();
		}
		return fChildren;
	}

	public void addChild(LocationCtx locationCtx) {
		if (fChildren == null) {
			fChildren = new ArrayList<>();
		}
		fChildren.add(locationCtx);
	}

	public char[] getSource(int offset, int length) {
		if (fSource.isValidOffset(offset + length - 1)) {
			char[] result = new char[length];
			fSource.arraycopy(offset, result, 0, length);
			return result;
		}
		return CharArrayUtils.EMPTY;
	}

	@Override
	public final int getSequenceLength() {
		return fSource.getLength() + fChildSequenceLength;
	}

	@Override
	public final int getSequenceNumberForOffset(int offset, boolean checkChildren) {
		int result = fSequenceNumber + fChildSequenceLength + offset;
		if (checkChildren && fChildren != null) {
			for (int i = fChildren.size(); --i >= 0;) {
				final LocationCtx child = fChildren.get(i);
				if (child.fEndOffsetInParent > offset) { // Child was inserted behind the offset, adjust sequence number
					result -= child.getSequenceLength();
				} else {
					return result;
				}
			}
		}
		return result;
	}

	@Override
	public void addChildSequenceLength(int childLength) {
		fChildSequenceLength += childLength;
	}

	@Override
	public final LocationCtx findSurroundingContext(int sequenceNumber, int length) {
		int testEnd = length > 1 ? sequenceNumber + length - 1 : sequenceNumber;
		final LocationCtx child = findChildLessOrEqualThan(sequenceNumber, false);
		if (child != null && child.fSequenceNumber + child.getSequenceLength() > testEnd) {
			return child.findSurroundingContext(sequenceNumber, length);
		}
		return this;
	}

	@Override
	public final LocationCtxMacroExpansion findEnclosingMacroExpansion(int sequenceNumber, int length) {
		int testEnd = length > 1 ? sequenceNumber + length - 1 : sequenceNumber;
		final LocationCtx child = findChildLessOrEqualThan(sequenceNumber, true);
		if (child != null && child.fSequenceNumber + child.getSequenceLength() > testEnd) {
			return child.findEnclosingMacroExpansion(sequenceNumber, length);
		}
		return null;
	}

	@Override
	public int convertToSequenceEndNumber(int sequenceNumber) {
		// try to delegate to a child.
		final LocationCtx child = findChildLessOrEqualThan(sequenceNumber, false);
		if (child != null)
			sequenceNumber = child.convertToSequenceEndNumber(sequenceNumber);

		// if the potentially converted sequence number is the beginning of this context,
		// skip the denotation of this context in the parent.
		if (sequenceNumber == fSequenceNumber)
			return sequenceNumber - fEndOffsetInParent + fOffsetInParent;

		return sequenceNumber;
	}

	@Override
	public ASTFileLocation findMappedFileLocation(int sequenceNumber, int length) {
		// try to delegate to a child.
		int testEnd = length > 1 ? sequenceNumber + length - 1 : sequenceNumber;
		final LocationCtx child = findChildLessOrEqualThan(sequenceNumber, false);
		if (child != null && child.fSequenceNumber + child.getSequenceLength() > testEnd) {
			return child.findMappedFileLocation(sequenceNumber, length);
		}
		return super.findMappedFileLocation(sequenceNumber, length);
	}

	@Override
	public void collectLocations(int sequenceNumber, final int length, ArrayList<IASTNodeLocation> locations) {
		if (length < 1)
			return;

		final int endSequenceNumber = sequenceNumber + length;
		if (fChildren != null) {
			int childIdx = Math.max(0, findChildIdxLessOrEqualThan(sequenceNumber, false));
			for (; childIdx < fChildren.size(); childIdx++) {
				final LocationCtx child = fChildren.get(childIdx);

				// Create the location between start and the child
				if (sequenceNumber < child.fSequenceNumber) {
					// Compute offset backwards from the child's offset in this location
					final int offset = child.fEndOffsetInParent - (child.fSequenceNumber - sequenceNumber);

					// Requested range ends before the child.
					if (endSequenceNumber <= child.fSequenceNumber) {
						addFileLocation(offset, endSequenceNumber - sequenceNumber, locations);
						return;
					}

					final int gapLen = child.fOffsetInParent - offset;
					if (gapLen > 0)
						addFileLocation(offset, child.fOffsetInParent - offset, locations);

					sequenceNumber = child.fSequenceNumber;
					assert sequenceNumber < endSequenceNumber;
				}

				// Let the child create locations
				final int childEndSequenceNumber = child.fSequenceNumber + child.getSequenceLength();
				if (sequenceNumber < childEndSequenceNumber
						|| (sequenceNumber == childEndSequenceNumber && !locations.isEmpty())) {
					child.collectLocations(sequenceNumber, endSequenceNumber - sequenceNumber, locations);
					sequenceNumber = childEndSequenceNumber;
					if (sequenceNumber >= endSequenceNumber)
						return;
				}
			}
		}

		// Create the location after the last child.
		final int myEndNumber = fSequenceNumber + getSequenceLength();
		final int offset = fSource.getLength() - (myEndNumber - sequenceNumber);
		if (endSequenceNumber <= myEndNumber) {
			addFileLocation(offset, endSequenceNumber - sequenceNumber, locations);
		} else {
			addFileLocation(offset, fSource.getLength() - offset, locations);
		}
	}

	private ArrayList<IASTNodeLocation> addFileLocation(int offset, int length, ArrayList<IASTNodeLocation> sofar) {
		IASTFileLocation loc = createFileLocation(offset, length);
		if (loc != null) {
			sofar.add(loc);
		}
		return sofar;
	}

	ASTFileLocation createFileLocation(int start, int length) {
		return null;
	}

	final int findChildIdxLessOrEqualThan(int sequenceNumber, boolean beforeReplacedChars) {
		if (fChildren == null) {
			return -1;
		}
		int upper = fChildren.size();
		int lower = 0;
		while (upper > lower) {
			int middle = (upper + lower) >>> 1;
			LocationCtx child = fChildren.get(middle);
			int childSequenceNumber = child.fSequenceNumber;
			if (beforeReplacedChars) {
				childSequenceNumber -= child.fEndOffsetInParent - child.fOffsetInParent;
			}
			if (childSequenceNumber <= sequenceNumber) {
				lower = middle + 1;
			} else {
				upper = middle;
			}
		}
		return lower - 1;
	}

	final LocationCtx findChildLessOrEqualThan(final int sequenceNumber, boolean beforeReplacedChars) {
		final int idx = findChildIdxLessOrEqualThan(sequenceNumber, beforeReplacedChars);
		return idx >= 0 ? fChildren.get(idx) : null;
	}

	@Override
	public void getInclusions(ArrayList<IASTInclusionNode> result) {
		if (fChildren != null) {
			for (LocationCtx ctx : fChildren) {
				if (ctx.getInclusionStatement() != null) {
					result.add(new ASTInclusionNode(ctx));
				} else {
					ctx.getInclusions(result);
				}
			}
		}
	}

	@Override
	public int getLineNumber(int offset) {
		if (fLineOffsets == null) {
			fLineOffsets = computeLineOffsets();
		}
		int idx = Arrays.binarySearch(fLineOffsets, offset);
		if (idx < 0) {
			return -idx;
		}
		return idx + 1;
	}

	private int[] computeLineOffsets() {
		final int len = fSource.getLength();
		IntArray offsets = new IntArray(len / 10); // Assuming 10 characters per line on average.
		for (int i = 0; i < len; i++) {
			if (fSource.get(i) == '\n')
				offsets.add(i);
		}
		return offsets.toArray();
	}

	@Override
	public String toString() {
		return "<synthetic>"; //$NON-NLS-1$
	}
}
