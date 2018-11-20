/*******************************************************************************
 * Copyright (c) 2006, 2014 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.io.PrintStream;

import org.eclipse.cdt.core.IPositionConverter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * Tracks changes made to a text buffer, to afterwards recalculate positions.
 * @author markus.schorn@windriver.com
 */
public class PositionTracker implements IPositionConverter {
	private static final int MEMORY_SIZE = 48;
	private static final int NODE_MEMORY_SIZE = 32;

	private Node fAboveRoot = new Node(0, 0, 0);
	private PositionTracker fFollowedBy = null;
	private long fTimeStamp;

	/**
	 * Resets the tracker to a state reflecting no changes.
	 */
	public synchronized void clear() {
		fAboveRoot = new Node(0, 0, 0);
		fFollowedBy = null;
	}

	/**
	 * Undoes the retirement to make this the head of a tracker chain again.
	 */
	synchronized void revive() {
		fFollowedBy = null;
	}

	/**
	 * Notifies the tracker of the insertion of characters.
	 * It is assumed that character get inserted before the offset.
	 *
	 * @param offset offset of the character in front of which insertion occurs.
	 * @param count amount of characters inserted.
	 */
	public synchronized void insert(int offset, int count) {
		assert fFollowedBy == null;
		assert offset >= 0;
		if (count == 0 || offset < 0) {
			return;
		}
		fAboveRoot.addChars(offset, count, 0);
	}

	/**
	 * Notifies the tracker of the removal of characters.
	 * delete(0,1) removes the first character,
	 * for convenience delete(1,-1) does the same.
	 *
	 * @param offset offset of the first character deleted.
	 * @param count amount of characters deleted.
	 */
	public synchronized void delete(int offset, int count) {
		assert fFollowedBy == null;
		assert offset >= 0;
		if (count < 0) {
			delete(offset + count, -count);
		} else {
			if (count == 0 || offset < 0) {
				return;
			}
			fAboveRoot.removeChars(offset, count, 0, true);
		}
	}

	/**
	 * Calculates the position in the original unmodified text.
	 *
	 * @param currentOffset position in the modified text.
	 * @return position in the unmodified text.
	 */
	public synchronized int historicOffset(int currentOffset) {
		return historicOffset(currentOffset, true);
	}

	private synchronized int historicOffset(int currentOffset, boolean nextOnDelete) {
		int orig = currentOffset;
		if (fFollowedBy != null) {
			orig = fFollowedBy.historicOffset(orig, nextOnDelete);
		}
		orig = fAboveRoot.calculateOriginalOffset(orig, 0, nextOnDelete);
		return orig;
	}

	/**
	 * Calculates the position in the modified text.
	 *
	 * @param historicOffset position in the unmodified text.
	 * @return position in the modified text.
	 */
	public synchronized int currentOffset(int historicOffset) {
		return currentOffset(historicOffset, true);
	}

	private synchronized int currentOffset(int historicOffset, boolean nextOnDelete) {
		int current = fAboveRoot.calculateCurrentOffset(historicOffset, 0, nextOnDelete);
		if (fFollowedBy != null) {
			current = fFollowedBy.currentOffset(current, nextOnDelete);
		}
		return current;
	}

	/**
	 * Makes this tracker final. Future changes are tracked by the tracker
	 * supplied and will be taken into account when converting positions.
	 *
	 * @param inFavourOf tracker that tracks changes from now on.
	 */
	public synchronized void retire(PositionTracker inFavourOf) {
		assert fFollowedBy == null;
		fFollowedBy = inFavourOf;
	}

	/**
	 * For the purpose of testing.
	 */
	public synchronized void print(PrintStream out) {
		fAboveRoot.print(0, out, 0);
	}

	/**
	 * For the purpose of testing.
	 */
	public synchronized int depth() {
		return fAboveRoot.depth() - 1;
	}

	public synchronized boolean isModified() {
		return fAboveRoot.fLeft != null || fAboveRoot.fRight != null;
	}

	public synchronized long getTimeStamp() {
		return fTimeStamp;
	}

	public synchronized void setTimeStamp(long timeStamp) {
		fTimeStamp = timeStamp;
	}

	public synchronized long getRetiredTimeStamp() {
		if (fFollowedBy == null) {
			return Long.MAX_VALUE;
		}
		return fFollowedBy.getTimeStamp();
	}

	public synchronized int getMemorySize() {
		return MEMORY_SIZE + NODE_MEMORY_SIZE * countNodes();
	}

	private synchronized int countNodes() {
		return fAboveRoot.countNodes();
	}

	@Override
	public synchronized IRegion actualToHistoric(IRegion actualPosition) {
		int actual = actualPosition.getOffset();
		int len = actualPosition.getLength();

		int historic = historicOffset(actual, true);
		if (len > 0) {
			len = historicOffset(actual + len - 1, false) - historic + 1;
		}
		assert len >= 0;
		return new Region(historic, len);
	}

	@Override
	public synchronized IRegion historicToActual(IRegion historicPosition) {
		int historic = historicPosition.getOffset();
		int len = historicPosition.getLength();

		int actual = currentOffset(historic, true);
		if (len > 0) {
			len = currentOffset(historic + len - 1, false) - actual + 1;
		}
		assert len >= 0;
		return new Region(actual, len);
	}

	/**
	 * Nodes implementing a red black binary tree.
	 *
	 * @author markus.schorn@windriver.com
	 */
	private static class Node {
		private static final boolean RED = true;
		private static final boolean BLACK = false;

		private int fDeltaPos2; // Sum of this and pos2 of parent yields pos2.
		private int fPos1;
		private int fChange; // Length of text change (+ add, - remove)

		private boolean fColor;
		private Node fLeft;
		private Node fRight;
		private Node fParent;

		Node(int pos1, int deltaPos2, int change) {
			fDeltaPos2 = deltaPos2;
			fPos1 = pos1;
			fChange = change;
			fLeft = fRight = fParent = null;
			fColor = RED;
		}

		int depth() {
			if (fLeft == null) {
				if (fRight == null) {
					return 1;
				}
				return fRight.depth() + 1;
			}
			if (fRight == null) {
				return fLeft.depth() + 1;
			}
			return StrictMath.max(fLeft.depth(), fRight.depth()) + 1;
		}

		// Forward calculation.
		int calculateCurrentOffset(int value1, int parentPos2, boolean nextOnDelete) {
			int fPos2 = parentPos2 + fDeltaPos2;
			int rel1 = value1 - fPos1;

			// Is value ahead of this change?
			if (rel1 < 0) {
				if (fLeft != null) {
					return fLeft.calculateCurrentOffset(value1, fPos2, nextOnDelete);
				}

				// Value is directly ahead of this change.
				return rel1 + fPos2;
			}

			// Is value deleted by this?
			if (rel1 < -fChange) {
				return nextOnDelete ? fPos2 : fPos2 - 1;
			}

			// Value is after this change.
			if (fRight != null) {
				return fRight.calculateCurrentOffset(value1, fPos2, nextOnDelete);
			}

			// Value is directly after this change.
			return rel1 + fPos2 + fChange;
		}

		// Backward calculation.
		int calculateOriginalOffset(int value2, int parentPos2, boolean nextOnDelete) {
			int fPos2 = parentPos2 + fDeltaPos2;
			int rel2 = value2 - fPos2;

			// Is value ahead of this change?
			if (rel2 < 0) {
				if (fLeft != null) {
					return fLeft.calculateOriginalOffset(value2, fPos2, nextOnDelete);
				}

				// Value is directly ahead of this change.
				return rel2 + fPos1;
			}

			// Is value added by this?
			if (rel2 < fChange) {
				return nextOnDelete ? fPos1 : fPos1 - 1;
			}

			// Offset is behind this change.
			if (fRight != null) {
				return fRight.calculateOriginalOffset(value2, fPos2, nextOnDelete);
			}

			// Offset is directly behind this change.
			return rel2 + fPos1 - fChange;
		}

		void addChars(int value2, int add, int fPos2) {
			int rel2 = value2 - fPos2;

			if (fParent != null) {
				fParent.balance(); // This may change both the parent and fDeltaPos2;
			}

			// Added ahead of this change?
			if (rel2 < 0) {
				fDeltaPos2 += add; // Advance
				if (fLeft != null) {
					int childPos2 = fPos2 + fLeft.fDeltaPos2;
					fLeft.fDeltaPos2 -= add; // Unadvance
					fLeft.addChars(value2, add, childPos2); // Use modified parent pos
					return;
				}

				addLeft(rel2 + fPos1, rel2 - add, add); // Modify delta pos
				return;
			}

			// Added inside range of another change?
			int range2 = fChange > 0 ? fChange : 0;
			if (rel2 <= range2 && !isHolder()) {
				fChange += add;
				// Insert in a deletion at the end
				if (fChange <= 0) {
					fPos1 += add;
					fDeltaPos2 += add;
					if (fLeft != null) {
						fLeft.fDeltaPos2 -= add;
					}
				} else if (fRight != null) {
					fRight.fDeltaPos2 += add; // advance right branch
				}
				return;
			}

			// Added behind this change.
			if (fRight != null) {
				fRight.addChars(value2, add, fPos2 + fRight.fDeltaPos2);
				return;
			}

			// Added directly behind this change.
			addRight(rel2 + fPos1 - fChange, rel2, add);
		}

		boolean removeChars(int firstChar2, int remove, int fPos2, boolean mustRemove) {
			int relFirstChar2 = firstChar2 - fPos2;
			int relAfterLastChar2 = relFirstChar2 + remove;

			// No insertion - no balancing
			if (mustRemove && fParent != null) {
				fParent.balance();
			}

			// Ahead and no merge possible.
			if (relAfterLastChar2 < 0) {
				fDeltaPos2 -= remove; // Advance
				if (fLeft != null) {
					fLeft.fDeltaPos2 += remove; // Unadvance
					return fLeft.removeChars(firstChar2, remove, fPos2 - remove + fLeft.fDeltaPos2, mustRemove);
				}

				if (mustRemove) {
					addLeft(relFirstChar2 + fPos1, relFirstChar2 + remove, -remove);
					return true;
				}
				return false;
			}

			// Behind and no merge possible.
			int range2 = (fChange > 0) ? fChange : 0;
			if (relFirstChar2 > range2 || isHolder()) {
				if (fRight != null) {
					fRight.removeChars(firstChar2, remove, fPos2 + fRight.fDeltaPos2, mustRemove);
					return true;
				}

				if (mustRemove) {
					addRight(relFirstChar2 + fPos1 - fChange, relFirstChar2, -remove);
					return true;
				}
				return false;
			}

			int delAbove = 0;
			if (relFirstChar2 < 0) {
				delAbove = -relFirstChar2;
			}
			int delBelow = relAfterLastChar2 - range2;
			if (delBelow < 0) {
				delBelow = 0;
			}
			int delInside = remove - delAbove - delBelow;

			// Delegate above to left children.
			if (delAbove > 0 && fLeft != null) {
				if (fLeft.removeChars(firstChar2, delAbove, fPos2 + fLeft.fDeltaPos2, false)) {
					fDeltaPos2 -= delAbove;
					fLeft.fDeltaPos2 += delAbove;
					fPos2 -= delAbove;
					delAbove = 0;
				}
			}
			// Delegate below to right children.
			if (delBelow > 0 && fRight != null) {
				if (fRight.removeChars(fPos2 + range2, delBelow, fPos2 + fRight.fDeltaPos2, false)) {
					delBelow = 0;
				}
			}

			// Do the adjustments in this node.
			fChange -= delAbove + delInside + delBelow;
			fDeltaPos2 -= delAbove;
			fPos1 -= delAbove;
			assert fPos1 >= 0;

			if (fLeft != null) {
				fLeft.fDeltaPos2 += delAbove; // lhs is unaffected, undo
			}
			if (fRight != null) {
				fRight.fDeltaPos2 -= delInside; // rhs is additionally affected.
			}
			return true;
		}

		private void balance() {
			if (fParent == null) {
				if (fRight != null) {
					fRight.fColor = BLACK;
				}
				return;
			}
			Node grandParent = fParent.fParent;
			if (fLeft == null || fRight == null) {
				return;
			}

			if (fLeft.isRed() && fRight.isRed()) {
				fLeft.fColor = fRight.fColor = BLACK;
				if (grandParent != null) {
					fColor = RED;
					if (fParent.isRed()) {
						rotateAround(grandParent);
					}
				}
			}
		}

		private void rotateAround(Node grandParent) {
			if (grandParent.fLeft == fParent) {
				rotateRightAround(grandParent);
			} else {
				rotateLeftAround(grandParent);
			}
		}

		private void rotateRightAround(Node grandParent) {
			if (fParent.fLeft == this) {
				grandParent.rotateRight();
				fParent.fColor = BLACK;
				fParent.fRight.fColor = RED;
			} else {
				fParent.rotateLeft();
				grandParent.rotateRight();
				fColor = BLACK;
				grandParent.fColor = RED;
			}
		}

		private void rotateLeftAround(Node grandParent) {
			if (fParent.fRight == this) {
				grandParent.rotateLeft();
				fParent.fColor = BLACK;
				fParent.fLeft.fColor = RED;
			} else {
				fParent.rotateRight();
				grandParent.rotateLeft();
				fColor = BLACK;
				grandParent.fColor = RED;
			}
		}

		private void rotateRight() {
			assert fLeft != null;

			Node root = this;
			Node left = fLeft;
			Node leftRight = left.fRight;

			int rootAbove = root.fDeltaPos2;
			int aboveLeft = -root.fDeltaPos2 - left.fDeltaPos2;
			int leftRoot = left.fDeltaPos2;

			// Put under old parent.
			if (fParent.fLeft == this) {
				fParent.putLeft(left);
			} else {
				fParent.putRight(left);
			}
			left.fDeltaPos2 += rootAbove;

			// Change the right node.
			left.putRight(root);
			root.fDeltaPos2 += aboveLeft;

			// change left of right node.
			root.putLeft(leftRight);
			if (leftRight != null) {
				leftRight.fDeltaPos2 += leftRoot;
			}
		}

		private void rotateLeft() {
			assert fRight != null;

			Node root = this;
			Node right = fRight;
			Node rightLeft = right.fLeft;

			int rootAbove = root.fDeltaPos2;
			int parentRight = -root.fDeltaPos2 - right.fDeltaPos2;
			int rightRoot = right.fDeltaPos2;

			// Put under old parent.
			if (fParent.fRight == this) {
				fParent.putRight(right);
			} else {
				fParent.putLeft(right);
			}
			right.fDeltaPos2 += rootAbove;

			// Change the left node.
			right.putLeft(root);
			root.fDeltaPos2 += parentRight;

			// Change right of left node.
			root.putRight(rightLeft);
			if (rightLeft != null) {
				rightLeft.fDeltaPos2 += rightRoot;
			}
		}

		private boolean isRed() {
			return fColor == RED;
		}

		private void putLeft(Node add) {
			fLeft = add;
			if (fLeft != null) {
				fLeft.fParent = this;
			}
		}

		private void putRight(Node add) {
			fRight = add;
			if (fRight != null) {
				fRight.fParent = this;
			}
		}

		private void addLeft(int pos1, int pos2, int change) {
			fLeft = new Node(pos1, pos2, change);
			fLeft.fParent = this;
			if (isHolder()) {
				assert false;
			} else if (isRed()) {
				fLeft.rotateAround(fParent);
			}
		}

		private boolean isHolder() {
			return fParent == null;
		}

		private void addRight(int pos1, int pos2, int change) {
			fRight = new Node(pos1, pos2, change);
			fRight.fParent = this;
			if (isHolder()) {
				fRight.fColor = BLACK;
			} else if (isRed()) {
				fRight.rotateAround(fParent);
			}
		}

		public void print(int i, PrintStream out, int parentOffset) {
			parentOffset += fDeltaPos2;
			if (fRight != null) {
				fRight.print(i + 1, out, parentOffset);
			}
			for (int j = 0; j < i; j++)
				out.print("  "); //$NON-NLS-1$
			out.println(fPos1 + "<->" + parentOffset + " : " + fChange + (fColor ? " // red" : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			if (fLeft != null) {
				fLeft.print(i + 1, out, parentOffset);
			}
		}

		public int countNodes() {
			int count = 1;
			if (fLeft != null) {
				count += fLeft.countNodes();
			}
			if (fRight != null) {
				count += fRight.countNodes();
			}
			return count;
		}
	}
}
