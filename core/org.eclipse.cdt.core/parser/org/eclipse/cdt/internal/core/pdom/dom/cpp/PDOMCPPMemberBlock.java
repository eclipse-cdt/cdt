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

import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * PDOMCPPMemberBlock stores the members of a composite type and maps every member to
 * the corresponding visibility.
 */
public class PDOMCPPMemberBlock {
	private static final int MAX_MEMBER_COUNT = 4;
	private static final int VISIBILITY_BITS = 2;
	private static final int VISIBILITY_MASK = (1 << VISIBILITY_BITS) - 1;
	private static final int VISIBILITY_VALUES_PER_BYTE = 8 / VISIBILITY_BITS;
	private static final int MEMBER_POINTERS = 0;
	private static final int MEMBER_VISIBILITIES =
			MEMBER_POINTERS + Database.PTR_SIZE	* MAX_MEMBER_COUNT;
	private static final int NEXT_MEMBER_BLOCK =
			MEMBER_VISIBILITIES + (MAX_MEMBER_COUNT + VISIBILITY_VALUES_PER_BYTE - 1) / VISIBILITY_VALUES_PER_BYTE;

	protected static final int RECORD_SIZE = NEXT_MEMBER_BLOCK + Database.PTR_SIZE;

	static {
		assert (MAX_MEMBER_COUNT > 0);
	}

	private final PDOMLinkage linkage;
	private final long record;
	private int nextMemberPosition = -1;

	public PDOMCPPMemberBlock(PDOMLinkage linkage, long record) throws CoreException {
		this.linkage = linkage;
		this.record = record;
	}

	public PDOMCPPMemberBlock(PDOMLinkage linkage) throws CoreException {
		Database db = linkage.getDB();
		this.linkage = linkage;
		this.record = db.malloc(RECORD_SIZE);
		db.clearBytes(record, RECORD_SIZE);
	}

	private int getNextPosition() throws CoreException {
		if (nextMemberPosition < 0) {
			nextMemberPosition = 0;
			while (nextMemberPosition < MAX_MEMBER_COUNT && getMemberRecord(nextMemberPosition) != 0) {
				nextMemberPosition++;
			}
		}
		return nextMemberPosition;
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

	public void addMember(PDOMNode member, int visibility) throws CoreException {
		if (getNextPosition() == MAX_MEMBER_COUNT) {
			PDOMCPPMemberBlock nextBlock = getNextBlock();
			if (nextBlock == null) {
				nextBlock = new PDOMCPPMemberBlock(linkage);
				setNextBlock(nextBlock);
			}
			nextBlock.addMember(member, visibility);
		} else {
			long memberLocationOffset = getMemberOffset(getNextPosition());
			long rec = member.getRecord();
			getDB().putRecPtr(memberLocationOffset, rec);
			setVisibility(getNextPosition(), visibility);
			nextMemberPosition++;
		}
	}

	public void accept(IPDOMVisitor visitor) throws CoreException {
		PDOMCPPMemberBlock current = this;
		do {
			current.visitBlock(visitor);
		} while ((current = current.getNextBlock()) != null);
	}

	private void visitBlock(IPDOMVisitor visitor) throws CoreException {
		if (record == 0) {
			throw new NullPointerException();
		}

		int item = 0;
		long memberRecord;
		while (item < MAX_MEMBER_COUNT && (memberRecord = getMemberRecord(item++)) != 0) {
			PDOMNode node = linkage.getNode(memberRecord);
			if (visitor.visit(node) && node != null) {
				node.accept(visitor);
			}
			visitor.leave(node);
		}
	}

	public void delete() throws CoreException {
		getDB().free(record);
	}

	private long getMemberRecord(int memberIndex) throws CoreException {
		return getDB().getRecPtr(getMemberOffset(memberIndex));
	}

	private long getMemberOffset(int memberIndex) {
		return record + MEMBER_POINTERS + Database.PTR_SIZE * memberIndex;
	}

	private PDOMNode getMember(int memberIndex) throws CoreException {
		if (memberIndex < getNextPosition() && memberIndex < MAX_MEMBER_COUNT) {
			long memberRecord = getMemberRecord(memberIndex);
			if (memberRecord != 0) {
				PDOMNode node = linkage.getNode(memberRecord);
				return node;
			}
		}
		return null;
	}

	private void setVisibility(int memberIndex, int newVisibility) throws CoreException {
		newVisibility &= VISIBILITY_MASK;

		int visibilityBitOffset = memberIndex % VISIBILITY_VALUES_PER_BYTE;
		long visibilityOffset = record + MEMBER_VISIBILITIES + memberIndex / VISIBILITY_VALUES_PER_BYTE;
		int visibility = getDB().getByte(visibilityOffset);
		// Resetting the previous visibility bits of the member
		visibility &= ~(VISIBILITY_MASK << visibilityBitOffset * VISIBILITY_BITS);
		// Setting the new visibility bits of the member
		visibility |= newVisibility << visibilityBitOffset * VISIBILITY_BITS;

		getDB().putByte(visibilityOffset, (byte) visibility);
	}

	public int getVisibility(IBinding member) throws CoreException {
		for (int memberIndex = 0; memberIndex < getNextPosition(); memberIndex++) {
			PDOMNode candidate = getMember(memberIndex);
			if (candidate != null && candidate.equals(member)) {
				return getVisibility(memberIndex);
			}
		}
		PDOMCPPMemberBlock nextBlock = getNextBlock();
		if (nextBlock != null) {
			return nextBlock.getVisibility(member);
		}
		throw new IllegalArgumentException(member.getName() + " is not a member"); //$NON-NLS-1$
	}

	private int getVisibility(int memberIndex) throws CoreException {
		int visibilityBitOffset = memberIndex % VISIBILITY_VALUES_PER_BYTE;
		long visibilityOffset = record + MEMBER_VISIBILITIES + memberIndex / VISIBILITY_VALUES_PER_BYTE;
		int visibility = getDB().getByte(visibilityOffset);

		// Clearing visibility bits of other members
		visibility &= VISIBILITY_MASK << visibilityBitOffset * VISIBILITY_BITS;
		visibility >>>= visibilityBitOffset * VISIBILITY_BITS;

		return visibility;
	}
}
