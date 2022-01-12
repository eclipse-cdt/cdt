/*******************************************************************************
 * Copyright (c) 2013, 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * PDOMCPPMemberBlock stores the members of a composite type and maps every member to
 * the corresponding visibility.
 */
public class PDOMCPPMemberBlock {
	/*
	 * The MAX_MEMBER_COUNT was chosen empirically by comparing PDOM file sizes of a real-life
	 * project. Six members per block resulted in the most compact PDOM.
	 */
	private static final int MAX_MEMBER_COUNT = 6;
	private static final int VISIBILITY_BITS = 2;
	private static final int VISIBILITY_MASK = (1 << VISIBILITY_BITS) - 1;
	private static final int VISIBILITY_VALUES_PER_BYTE = 8 / VISIBILITY_BITS;

	private static final int MEMBER_POINTERS = 0;
	private static final int MEMBER_VISIBILITIES = MEMBER_POINTERS + Database.PTR_SIZE * MAX_MEMBER_COUNT;
	private static final int NEXT_MEMBER_BLOCK = MEMBER_VISIBILITIES
			+ (MAX_MEMBER_COUNT + VISIBILITY_VALUES_PER_BYTE - 1) / VISIBILITY_VALUES_PER_BYTE;

	protected static final int RECORD_SIZE = NEXT_MEMBER_BLOCK + Database.PTR_SIZE;

	static {
		assert (MAX_MEMBER_COUNT > 0);
	}

	private final PDOMLinkage linkage;
	private final long record;
	private int nextMemberPosition;

	public PDOMCPPMemberBlock(PDOMLinkage linkage, long record) throws CoreException {
		this.linkage = linkage;
		this.record = record;
		this.nextMemberPosition = -1; // nextMemberPosition is unknown.
	}

	public PDOMCPPMemberBlock(PDOMLinkage linkage) throws CoreException {
		Database db = linkage.getDB();
		this.linkage = linkage;
		this.record = db.malloc(RECORD_SIZE);
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

	private PDOM getPDOM() {
		return linkage.getPDOM();
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
		addMember(this, member, visibility);
	}

	private static void addMember(PDOMCPPMemberBlock block, PDOMNode member, int visibility) throws CoreException {
		assert member.getPDOM() == block.getPDOM();
		while (true) {
			int pos = block.getNextPosition();
			if (pos < MAX_MEMBER_COUNT) {
				long memberLocationOffset = block.getMemberOffset(pos);
				block.getDB().putRecPtr(memberLocationOffset, member.getRecord());
				block.setVisibility(pos, visibility);
				block.nextMemberPosition++;
				break;
			}
			PDOMCPPMemberBlock previousBlock = block;
			block = block.getNextBlock();
			if (block == null) {
				block = new PDOMCPPMemberBlock(previousBlock.linkage);
				previousBlock.setNextBlock(block);
			}
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
			PDOMNode node = PDOMNode.load(getPDOM(), memberRecord);
			if (node != null) {
				if (visitor.visit(node))
					node.accept(visitor);
				visitor.leave(node);
			}
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

	private void setVisibility(int memberIndex, int newVisibility) throws CoreException {
		newVisibility &= VISIBILITY_MASK;

		int visibilityBitOffset = memberIndex % VISIBILITY_VALUES_PER_BYTE;
		long visibilityOffset = record + MEMBER_VISIBILITIES + memberIndex / VISIBILITY_VALUES_PER_BYTE;
		int visibility = getDB().getByte(visibilityOffset);
		// Resetting the previous visibility bits of the target member.
		visibility &= ~(VISIBILITY_MASK << visibilityBitOffset * VISIBILITY_BITS);
		// Setting the new visibility bits of the target member.
		visibility |= newVisibility << visibilityBitOffset * VISIBILITY_BITS;

		getDB().putByte(visibilityOffset, (byte) visibility);
	}

	/**
	 * Returns visibility of the member, or -1 if the given binding is not a member.
	 */
	public int getVisibility(IBinding member) throws CoreException {
		IIndexFragmentBinding indexMember = getPDOM().adaptBinding(member);
		if (!(indexMember instanceof PDOMNode))
			return -1;
		return getVisibility(this, (PDOMNode) indexMember);
	}

	private static int getVisibility(PDOMCPPMemberBlock block, PDOMNode memberNode) throws CoreException {
		long memberRecord = memberNode.getRecord();

		do {
			for (int memberIndex = 0; memberIndex < MAX_MEMBER_COUNT; memberIndex++) {
				long rec = block.getMemberRecord(memberIndex);
				if (rec == 0)
					return -1;
				if (rec == memberRecord)
					return block.getVisibility(memberIndex);
			}
		} while ((block = block.getNextBlock()) != null);

		return -1;
	}

	private int getVisibility(int memberIndex) throws CoreException {
		int visibilityBitOffset = memberIndex % VISIBILITY_VALUES_PER_BYTE;
		long visibilityOffset = record + MEMBER_VISIBILITIES + memberIndex / VISIBILITY_VALUES_PER_BYTE;
		int visibility = getDB().getByte(visibilityOffset);

		visibility >>>= visibilityBitOffset * VISIBILITY_BITS;
		// Filtering the visibility bits of the target member.
		visibility &= VISIBILITY_MASK;
		return visibility;
	}
}
