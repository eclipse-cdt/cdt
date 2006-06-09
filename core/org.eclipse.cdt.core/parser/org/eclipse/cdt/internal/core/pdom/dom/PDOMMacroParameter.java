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

package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMMacroParameter {

	private final PDOM pdom;
	private final int record;

	private static final int NEXT = 0;
	private static final int NAME = 4;
	
	private static final int RECORD_SIZE = 8;
	
	public PDOMMacroParameter(PDOM pdom, int record) {
		this.pdom = pdom;
		this.record = record;
	}
	
	public PDOMMacroParameter(PDOM pdom, String name) throws CoreException {
		Database db = pdom.getDB();
		
		this.pdom = pdom;
		this.record = db.malloc(RECORD_SIZE);
		
		db.putInt(record + NEXT, 0);
		db.putInt(record + NAME, db.newString(name).getRecord());
	}

	public int getRecord() {
		return record;
	}
	
	public void delete() throws CoreException {
		PDOMMacroParameter next = getNextParameter();
		if (next != null)
			next.delete();
		getName().delete();
		pdom.getDB().free(record);
	}
	
	public void setNextParameter(PDOMMacroParameter next) throws CoreException {
		int rec = next != null ? next.getRecord() : 0;
		pdom.getDB().putInt(record + NEXT, rec);
	}
	
	public PDOMMacroParameter getNextParameter() throws CoreException {
		int rec = pdom.getDB().getInt(record + NEXT);
		return rec != 0 ? new PDOMMacroParameter(pdom, rec) : null;
	}
	
	public IString getName() throws CoreException {
		Database db = pdom.getDB();
		return db.getString(db.getInt(record + NAME));
	}
	
}
