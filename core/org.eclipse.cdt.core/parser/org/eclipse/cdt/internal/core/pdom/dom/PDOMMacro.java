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

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents macros.
 * 
 * @author Doug Schaefer
 */
public class PDOMMacro {

	private final PDOM pdom;
	private final int record;

	private static final int NAME = 0;
	private static final int EXPANSION = 4;
	private static final int NEXT_MACRO = 8;
	
	private static final int RECORD_SIZE = 12;
	
	public PDOMMacro(PDOM pdom, int record) {
		this.pdom = pdom;
		this.record = record;
	}
	
	public PDOMMacro(PDOM pdom, IASTPreprocessorMacroDefinition macro) throws CoreException {
		this.pdom = pdom;
		
		Database db = pdom.getDB();
		this.record = db.malloc(RECORD_SIZE);
		db.putInt(record + NAME, db.newString(macro.getName().toCharArray()).getRecord());
		db.putInt(record + EXPANSION, db.newString(macro.getExpansion()).getRecord());
		setNextMacro(0);
	}
	
	public int getRecord() {
		return record;
	}
	
	public void delete() throws CoreException {
		pdom.getDB().free(record);
	}
	
	public IString getName() throws CoreException {
		Database db = pdom.getDB();
		int rec = db.getInt(record + NAME);
		return db.getString(rec);
	}
	
	public IString getExpansion() throws CoreException {
		Database db = pdom.getDB();
		int rec = db.getInt(record + EXPANSION);
		return db.getString(rec);
	}
	
	public PDOMMacro getNextMacro() throws CoreException {
		int rec = pdom.getDB().getInt(record + NEXT_MACRO);
		return rec != 0 ? new PDOMMacro(pdom, rec) : null;
	}
	
	public void setNextMacro(PDOMMacro macro) throws CoreException {
		setNextMacro(macro != null ? macro.getRecord() : 0);
	}

	public void setNextMacro(int rec) throws CoreException {
		pdom.getDB().putInt(record + NEXT_MACRO, rec);
	}
}
