/*******************************************************************************
 * Copyright (c) 2005, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents a linked list of PDOMNode records
 * @author Doug Schaefer
 */
public class PDOMNodeLinkedList {
	private long offset;
	private PDOMLinkage linkage;
	private boolean allowsNull;
	
	private static final int FIRST_MEMBER = 0;
	protected static final int RECORD_SIZE = 4;

	public PDOMNodeLinkedList(PDOMLinkage linkage, long offset, boolean allowsNulls) {
		this.offset = offset;
		this.linkage = linkage;
		this.allowsNull = allowsNulls;
	}
	
	/**
	 * Creates an object representing a linked list at the specified offset of the specified pdom.
	 * The linked list created may not hold null items
	 * @param linkage
	 * @param offset
	 */
	public PDOMNodeLinkedList(PDOMLinkage linkage, long offset) {
		this(linkage, offset, false);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public void accept(IPDOMVisitor visitor) throws CoreException {
		Database db = linkage.getDB();
		long firstItem = db.getRecPtr(offset + FIRST_MEMBER);
		if (firstItem == 0)
			return;
		
		long item = firstItem;
		do {
			PDOMNode node;
			final long record= db.getRecPtr(item + ListItem.ITEM);
			if (record == 0) {
				if (!allowsNull) {
					throw new NullPointerException();
				}
				node= null;
			} else {
				node= linkage.getNode(record);
			}
			if (visitor.visit(node) && node != null) {
				node.accept(visitor);
			}
			visitor.leave(node);
		} while ((item = db.getRecPtr(item + ListItem.NEXT)) != firstItem);
	}
	
	private ListItem getFirstMemberItem() throws CoreException {
		Database db = linkage.getDB();
		long item = db.getRecPtr(offset + FIRST_MEMBER);
		return item != 0 ? new ListItem(db, item) : null;
	}

	/**
	 * Returns node at position {@code pos}. Not recommended to be used in a loop since
	 * such a loop would be more expensive that a single {@code accept(IPDOMVisitor)} call. 
	 * @param pos A zero-based position in the list.
	 * @return The node at position {@code pos}, or {@code null} if no such node exists.
	 */
	public PDOMNode getNodeAt(int pos) throws CoreException {
		Database db = linkage.getDB();
		long firstItem = db.getRecPtr(offset + FIRST_MEMBER);
		if (firstItem == 0) {
			return null;
		}
		long item = firstItem;
		do {
			if (--pos < 0) {
				long record = db.getRecPtr(item + ListItem.ITEM);
				if (record == 0) {
					if (!allowsNull) {
						throw new NullPointerException();
					}
					return null;
				} else {
					return linkage.getNode(record);
				}
			}
		} while ((item = db.getRecPtr(item + ListItem.NEXT)) != firstItem);
		return null;
	}

	public void addMember(PDOMNode member) throws CoreException {
		addMember(allowsNull && member==null ? 0 : member.getRecord());
	}
	
	protected void addMember(long record) throws CoreException {
		Database db = linkage.getDB();
		ListItem firstMember = getFirstMemberItem();
		if (firstMember == null) {
			firstMember = new ListItem(db);
			firstMember.setItem(record);
			firstMember.setNext(firstMember);
			firstMember.setPrev(firstMember);
			db.putRecPtr(offset + FIRST_MEMBER, firstMember.getRecord());
		} else {
			ListItem newMember = new ListItem(db);
			newMember.setItem(record);
			ListItem prevMember = firstMember.getPrev();
			prevMember.setNext(newMember);
			firstMember.setPrev(newMember);
			newMember.setPrev(prevMember);
			newMember.setNext(firstMember);
		}
	}

	public void deleteListItems() throws CoreException {
		ListItem item = getFirstMemberItem();
		if (item != null) {
			long firstRec= item.record;

			do {
				ListItem nextItem= item.getNext();
				item.delete();
				item= nextItem;
			}
			while (item.record != firstRec && item.record != 0);
		}
	}
}
