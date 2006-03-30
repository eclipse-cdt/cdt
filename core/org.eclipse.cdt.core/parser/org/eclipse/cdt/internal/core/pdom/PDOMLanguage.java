/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom;

import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMLanguage {

	private PDOM pdom;
	private int record;
	
	private static final int NEXT = 0;	// int
	private static final int ID = 4;	// char
	private static final int NAME = 6;	// string
	
	public PDOMLanguage(PDOM pdom, int record) {
		this.pdom = pdom;
		this.record = record;
	}

	public PDOMLanguage(PDOM pdom, String name, int id, int next) throws CoreException {
		this.pdom = pdom;
		Database db = pdom.getDB();
		record = db.malloc(6 + (name.length() + 1) * 2);
		db.putInt(record + NEXT, next);
		db.putChar(record + ID, (char)id);
		db.putString(record + NAME, name);
	}
	
	public int getRecord() {
		return record;
	}
	
	public int getId() throws CoreException {
		return pdom.getDB().getChar(record + ID);
	}
	
	public String getName() throws CoreException {
		return pdom.getDB().getString(record + NAME);
	}
	
	public PDOMLanguage getNext() throws CoreException {
		int nextrec = pdom.getDB().getInt(record + NEXT);
		return nextrec != 0 ? new PDOMLanguage(pdom, nextrec) : null;
	}
	
	public boolean equals(String id) throws CoreException {
		return pdom.getDB().stringCompare(record + NAME, id) == 0;
	}
}
