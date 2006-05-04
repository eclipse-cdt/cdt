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

package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 * 
 * This is a basic node in the PDOM database.
 * PDOM nodes form a multi-root tree with linkages being the roots.
 * This class managed the parent pointer.
 */
public abstract class PDOMNode implements IPDOMNode{

	private static final int PARENT = 0;
	private static final int NAME = 4;
	
	protected static final int RECORD_SIZE = 8;
	
	protected final PDOM pdom;
	protected final int record;
	
	protected PDOMNode(PDOM pdom, int record) {
		this.pdom = pdom;
		this.record = record;
	}
	
	protected PDOMNode(PDOM pdom, PDOMNode parent, char[] name) throws CoreException {
		this.pdom = pdom;
		Database db = pdom.getDB();
		
		record = db.malloc(getRecordSize());

		// name - must be before parent
		db.putInt(record + NAME, db.newString(name).getRecord());

		// parent
		if (parent != null) {
			pdom.getDB().putInt(record + PARENT, parent.getRecord());
			parent.addChild(this);
		}
		
	}

	protected abstract int getRecordSize();
	
	public PDOM getPDOM() {
		return pdom;
	}
	
	public int getRecord() {
		return record;
	}
	
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof PDOMNode) {
			PDOMNode other = (PDOMNode)obj;
			return pdom.equals(other.pdom) && record == other.record;
		}
		
		return super.equals(obj);
	}

	public void accept(IPDOMVisitor visitor) throws CoreException {
		// No children here.
	}
	
	public PDOMLinkage getLinkage() throws CoreException {
		return getLinkage(pdom, record);
	}
	
	public static PDOMLinkage getLinkage(PDOM pdom, int record) throws CoreException {
		Database db = pdom.getDB();
		int linkagerec = record;
		int parentrec = db.getInt(linkagerec + PARENT);
		while (parentrec != 0) {
			linkagerec = parentrec;
			parentrec = db.getInt(linkagerec + PARENT);
		}
		
		return pdom.getLinkage(linkagerec);
	}
	
	public IString getDBName() throws CoreException {
		Database db = pdom.getDB();
		int namerec = db.getInt(record + NAME);
		return db.getString(namerec);
	}
	
	public char[] getNameCharArray() throws CoreException {
		return getDBName().getChars(); 
	}
	
	protected void addChild(PDOMNode child) throws CoreException {
		// by defaut do nothing
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
