/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.core.runtime.CoreException;

/**
 * This is a list item. It contains a next and prev pointer
 * as well as a pointer to the item.
 * block.
 * 
 * @author Doug Schaefer
 */
public class ListItem {
	
	protected final Database db;
	protected final int record;
	
	protected static final int NEXT = 0;
	protected static final int PREV = 4;
	protected static final int ITEM = 8;

	protected static final int RECORD_SIZE = 12;
	
	public ListItem(Database db, int record) {
		this.db = db;
		this.record = record;
	}
	
	public ListItem(Database db) throws CoreException {
		this.db = db;
		this.record = db.malloc(RECORD_SIZE);
	}
	
	public int getRecord() {
		return record;
	}
	
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		else if (obj instanceof ListItem)
			return record == ((ListItem)obj).record;
		else
			return false;
	}
	
	public void setItem(int item) throws CoreException {
		db.putInt(record + ITEM, item);
	}
	
	public int getItem() throws CoreException {
		return db.getInt(record + ITEM);
	}
	
	public void setNext(ListItem next) throws CoreException {
		db.putInt(record + NEXT, next.getRecord());
	}
	
	public ListItem getNext() throws CoreException {
		int next = db.getInt(record + NEXT);
		return next != 0 ? new ListItem(db, next) : null;
	}
	
	public void setPrev(ListItem prev) throws CoreException {
		db.putInt(record + PREV, prev.getRecord());
	}
	
	public ListItem getPrev() throws CoreException {
		int prev = db.getInt(record + PREV);
		return prev != 0 ? new ListItem(db, prev) : null;
	}
	
}
