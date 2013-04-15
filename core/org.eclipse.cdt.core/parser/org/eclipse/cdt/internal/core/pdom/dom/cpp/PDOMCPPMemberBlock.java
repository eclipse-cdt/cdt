/*******************************************************************************
 * Copyright (c) 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * PDOMCPPMemberBlock stores the members of a composite type and maps every member to
 * the corresponding accessibility.
 */
public class PDOMCPPMemberBlock {
	private static final int MEMBER_COUNT = 4;
	private static final int ACCESSIBILITY_BITS = 2;
	private static final int MEMBER_POINTERS = 0;
	private static final int MEMBER_ACCESSIBILITIES = MEMBER_POINTERS + Database.PTR_SIZE
			* MEMBER_COUNT;
	private static final int NEXT_MEMBER_BLOCK = MEMBER_ACCESSIBILITIES
			+ ((MEMBER_COUNT * ACCESSIBILITY_BITS + 6) / 8);

	protected static final int RECORD_SIZE = NEXT_MEMBER_BLOCK + Database.PTR_SIZE;

	static {
		assert (MEMBER_COUNT > 0);
	}

	private final PDOMLinkage linkage;
	private final long record;

	private int nextMemberPosition = 0;

	public PDOMCPPMemberBlock(PDOMLinkage linkage, long record) throws CoreException {
		this.linkage = linkage;
		this.record = record;
		updateNextPosition();
	}

	public PDOMCPPMemberBlock(PDOMLinkage linkage) throws CoreException {
		Database db = linkage.getDB();
		this.linkage = linkage;
		this.record = db.malloc(RECORD_SIZE);
		db.clearBytes(record, RECORD_SIZE);
		updateNextPosition();
	}

	private void updateNextPosition() throws CoreException {
		while (nextMemberPosition < MEMBER_COUNT
				&& getDB().getRecPtr(getMemberRecord(nextMemberPosition)) != 0) {
			nextMemberPosition++;
		}
	}

	private Database getDB() {
		return linkage.getDB();
	}

	public long getRecord() {
		return record;
	}

	public void setNextBlock(PDOMCPPMemberBlock nextBlock) throws CoreException {
		long rec = nextBlock != null ? nextBlock.getRecord() : 0;
		getDB().putRecPtr(record + NEXT_MEMBER_BLOCK, rec);
	}

	public PDOMCPPMemberBlock getNextBlock() throws CoreException {
		long rec = getDB().getRecPtr(record + NEXT_MEMBER_BLOCK);
		return rec != 0 ? new PDOMCPPMemberBlock(linkage, rec) : null;
	}

	public void addMember(PDOMNode member) throws CoreException {
		if (nextMemberPosition == MEMBER_COUNT) {
			if (getNextBlock() == null) {
				PDOMCPPMemberBlock nextBlock = new PDOMCPPMemberBlock(linkage);
				setNextBlock(nextBlock);
			}
			getNextBlock().addMember(member);
		} else {
			long memberLocationOffset = getMemberRecord(nextMemberPosition);
			long rec = member != null ? member.getRecord() : 0;
			getDB().putRecPtr(memberLocationOffset, rec);
			nextMemberPosition++;
		}
	}

	public void accept(IPDOMVisitor visitor) throws CoreException {
		Database db = linkage.getDB();

		for (int item = 0; item < nextMemberPosition; item++) {
			final long memberRecord = db.getRecPtr(getMemberRecord(item));
			if (record == 0) {
				throw new NullPointerException();
			}

			PDOMNode node = linkage.getNode(memberRecord);
			if (visitor.visit(node) && node != null) {
				node.accept(visitor);
			}
			visitor.leave(node);
		}

		PDOMCPPMemberBlock nextBlock = getNextBlock();
		if (nextBlock != null) {
			nextBlock.accept(visitor);
		}
	}

	public void delete() throws CoreException {
		getDB().free(record);
	}

	private long getMemberRecord(int nextMemberPosition) {
		return record + MEMBER_POINTERS + Database.PTR_SIZE * nextMemberPosition;
	}

	private PDOMNode getMember(int memberIndex) throws CoreException {
		if (memberIndex < nextMemberPosition) {
			long memberLocationOffset = getMemberRecord(memberIndex);
			long recordPointer = getDB().getRecPtr(memberLocationOffset);
			if (recordPointer != 0) {
				PDOMNode node = linkage.getNode(recordPointer);
				return node;
			}
		}
		return null;
	}

	public void setAccessibility(IBinding member, int accessibility) throws CoreException {
		for (int memberIndex = 0; memberIndex < nextMemberPosition; memberIndex++) {
			PDOMNode candidate = getMember(memberIndex);
			if (candidate != null && candidate.equals(member)) {
				setAccessibility(memberIndex, accessibility);
				return;
			}
		}
		PDOMCPPMemberBlock nextBlock = getNextBlock();
		if (nextBlock != null) {
			nextBlock.setAccessibility(member, accessibility);
		}
	}

	private void setAccessibility(int memberIndex, int accessibility) throws CoreException {
		int newAccessibility = accessibility & 3;

		int targetAccessibilityBytes = memberIndex / 4;
		int accessibilityBlockOffset = memberIndex % 4;
		int newAccessibilityMask = (byte) (newAccessibility << accessibilityBlockOffset * ACCESSIBILITY_BITS);
		int resetMask = ~(3 << accessibilityBlockOffset * ACCESSIBILITY_BITS);

		long accessibilityOffset = record + MEMBER_ACCESSIBILITIES + targetAccessibilityBytes;
		int currentAccessibility = getDB().getByte(accessibilityOffset);

		currentAccessibility &= resetMask;
		currentAccessibility |= newAccessibilityMask;

		getDB().putByte(accessibilityOffset, (byte) currentAccessibility);

	}

	public int getAccessibility(IBinding member) throws CoreException {
		for (int memberIndex = 0; memberIndex < nextMemberPosition; memberIndex++) {
			PDOMNode candidate = getMember(memberIndex);
			if (candidate != null && candidate.equals(member)) {
				return getAccessibility(memberIndex);
			}
		}
		PDOMCPPMemberBlock nextBlock = getNextBlock();
		if (nextBlock != null) {
			return nextBlock.getAccessibility(member);
		}
		return ICPPClassType.a_unspecified;
	}

	private int getAccessibility(int memberIndex) throws CoreException {
		int targetAccessibilityBytes = memberIndex / 4;
		int accessibilityBlockOffset = memberIndex % 4;
		int accessibilityMask = 3 << accessibilityBlockOffset * ACCESSIBILITY_BITS;

		long accessibilityOffset = record + MEMBER_ACCESSIBILITIES + targetAccessibilityBytes;
		int currentAccessibility = getDB().getByte(accessibilityOffset);
		int maskedAccessibility = currentAccessibility & accessibilityMask;

		maskedAccessibility >>>= accessibilityBlockOffset * ACCESSIBILITY_BITS;
		return maskedAccessibility;
	}

	public Map<IBinding, Integer> getAccessibilities() throws CoreException {
		Map<IBinding, Integer> accessibilities = new HashMap<IBinding, Integer>();
		PDOMCPPMemberBlock current = this;
		do {
			for (int index = 0; index < current.nextMemberPosition; index++) {
				PDOMNode member = getMember(index);
				int accessibility = getAccessibility(index);
				if (member instanceof IBinding) {
					accessibilities.put(((IBinding) member), accessibility);
				}
			}
		} while ((current = current.getNextBlock()) != null);
		return accessibilities;
	}
}
