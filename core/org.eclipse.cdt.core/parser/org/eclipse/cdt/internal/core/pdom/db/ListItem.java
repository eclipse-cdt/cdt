/*******************************************************************************
 * Copyright (c) 2006, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.core.runtime.CoreException;

/**
 * This is a list item. It contains a next and prev pointer
 * as well as a pointer to the item.
 *
 * @author Doug Schaefer
 */
public class ListItem {
	protected final Database db;
	protected final long record;

	protected static final int NEXT = 0;
	protected static final int PREV = 4;
	protected static final int ITEM = 8;

	protected static final int RECORD_SIZE = 12;

	public ListItem(Database db, long record) {
		this.db = db;
		this.record = record;
	}

	public ListItem(Database db) throws CoreException {
		this.db = db;
		this.record = db.malloc(RECORD_SIZE);
	}

	public long getRecord() {
		return record;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof ListItem)
			return record == ((ListItem) obj).record;
		return false;
	}

	public void setItem(long item) throws CoreException {
		db.putRecPtr(record + ITEM, item);
	}

	public long getItem() throws CoreException {
		return db.getRecPtr(record + ITEM);
	}

	public void setNext(ListItem next) throws CoreException {
		db.putRecPtr(record + NEXT, next.getRecord());
	}

	public ListItem getNext() throws CoreException {
		long next = db.getRecPtr(record + NEXT);
		return next != 0 ? new ListItem(db, next) : null;
	}

	public void setPrev(ListItem prev) throws CoreException {
		db.putRecPtr(record + PREV, prev.getRecord());
	}

	public ListItem getPrev() throws CoreException {
		long prev = db.getRecPtr(record + PREV);
		return prev != 0 ? new ListItem(db, prev) : null;
	}

	public void delete() throws CoreException {
		db.free(record);
	}
}
