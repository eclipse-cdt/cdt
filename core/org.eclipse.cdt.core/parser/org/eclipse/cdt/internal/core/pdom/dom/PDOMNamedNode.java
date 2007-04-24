/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * IBM Corporation
 * Andrew Ferguson (Symbian)
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
public abstract class PDOMNamedNode extends PDOMNode {
	/**
	 * Offset of pointer to node name (relative to the beginning of the record).
	 */
	private static final int NAME = PDOMNode.RECORD_SIZE + 0;

	/**
	 * The size in bytes of a PDOMNamedNode record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMNode.RECORD_SIZE + 4;
	
	public PDOMNamedNode(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMNamedNode(PDOM pdom, PDOMNode parent, char[] name) throws CoreException {
		super(pdom, parent);

		Database db = pdom.getDB();
		db.putInt(record + NAME,
				name != null ? db.newString(name).getRecord() : 0);
	}

	abstract protected int getRecordSize();

	public IString getDBName() throws CoreException {
		Database db = pdom.getDB();
		int namerec = db.getInt(record + NAME);
		return db.getString(namerec);
	}
	
	public static IString getDBName(PDOM pdom, int record) throws CoreException {
		Database db = pdom.getDB();
		int namerec = db.getInt(record + NAME);
		return db.getString(namerec);
	}
	
	public char[] getNameCharArray() throws CoreException {
		return getDBName().getChars(); 
	}
	
	public boolean hasName(char[] name) throws CoreException {
		return getDBName().equals(name);
	}
}
