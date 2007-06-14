/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents a linked list of PDOMNode records
 * @author Doug Schaefer
 *
 */
public class PDOMNodeLinkedList {
	private PDOM pdom;
	private int offset;
	private PDOMLinkage linkage;
	private boolean allowsNull;
	
	private static final int FIRST_MEMBER = 0;
	protected static final int RECORD_SIZE = 4;

	public PDOMNodeLinkedList(PDOM pdom, int offset, PDOMLinkage linkage, boolean allowsNulls) {
		this.pdom = pdom;
		this.offset = offset;
		this.linkage = linkage;
		this.allowsNull = allowsNulls;
	}
	
	/**
	 * Creates an object representing a linked list at the specified offset of the specified pdom.
	 * The linked list created may not hold null items
	 * @param pdom
	 * @param offset
	 * @param linkage
	 */
	public PDOMNodeLinkedList(PDOM pdom, int offset, PDOMLinkage linkage) {
		this(pdom, offset, linkage, false);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public void accept(IPDOMVisitor visitor) throws CoreException {
		ListItem firstItem = getFirstMemberItem();
		if (firstItem == null)
			return;
		
		ListItem item = firstItem;
		do {
			PDOMNode node;
			int record= item.getItem();
			if(record==0) {
				if(!allowsNull) {
					throw new NullPointerException();
				}
				node= null;
			} else {
				node= linkage.getNode(item.getItem());
			}
			if (visitor.visit(node) && node!=null)
				node.accept(visitor);
			visitor.leave(node);
			item = item.getNext();
		} while (!item.equals(firstItem));
	}
	
	private ListItem getFirstMemberItem() throws CoreException {
		Database db = pdom.getDB();
		int item = db.getInt(offset + FIRST_MEMBER);
		return item != 0 ? new ListItem(db, item) : null;
	}
	
	public void addMember(PDOMNode member) throws CoreException {
		addMember(allowsNull && member==null ? 0 : member.getRecord());
	}
	
	protected void addMember(int record) throws CoreException {
		Database db = pdom.getDB();
		ListItem firstMember = getFirstMemberItem();
		if (firstMember == null) {
			firstMember = new ListItem(db);
			firstMember.setItem(record);
			firstMember.setNext(firstMember);
			firstMember.setPrev(firstMember);
			db.putInt(offset + FIRST_MEMBER, firstMember.getRecord());
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
}
