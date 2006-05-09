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
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public abstract class PDOMNamedNode extends PDOMNode {

	private static final int NAME = PDOMNode.RECORD_SIZE + 0;

	protected static final int RECORD_SIZE = PDOMNode.RECORD_SIZE + 4;
	
	public PDOMNamedNode(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMNamedNode(PDOM pdom, PDOMNode parent, char[] name) throws CoreException {
		super(pdom, parent);

		Database db = pdom.getDB();
		db.putInt(record + NAME,
				name != null ? db.newString(name).getRecord() : 0);
		
		if (parent != null)
			parent.addChild(this);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public IString getDBName() throws CoreException {
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

	public IBTreeComparator getIndexComparator() {
		return new IBTreeComparator() {
			public int compare(int record1, int record2) throws CoreException {
				Database db = pdom.getDB();
				int string1 = db.getInt(record1 + NAME);
				int string2 = db.getInt(record2 + NAME);
				return db.getString(string1).compare(db.getString(string2));
			};
		};
	}
	
	public abstract static class NodeFinder implements IBTreeVisitor {
		protected final PDOM pdom;
		protected final char[] name;
		protected NodeFinder(PDOM pdom, char [] name) {
			this.pdom = pdom;
			this.name = name;
		}
		public int compare(int record) throws CoreException {
			Database db = pdom.getDB();
			int namerec = db.getInt(record + NAME);
			return db.getString(namerec).compare(name);
		}
	}

}
