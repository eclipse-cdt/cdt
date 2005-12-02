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

import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents a file containing names.
 * 
 * @author Doug Schaefer
 *
 */
public class PDOMFile {

	private PDOMDatabase pdom;
	private int record;
	
	private static final int FIRST_NAME_OFFSET = 0;
	private static final int FILE_NAME_OFFSET = Database.INT_SIZE;
	
	public static class Comparator implements IBTreeComparator {
		private Database db;
		
		public Comparator(Database db) {
			this.db = db;
		}
		
		public int compare(int record1, int record2) throws CoreException {
			return db.stringCompare(record1 + FILE_NAME_OFFSET, record2 + FILE_NAME_OFFSET);
		}
	}
	
	public abstract static class Visitor implements IBTreeVisitor {
		private Database db;
		private String key;
		
		public Visitor(Database db, String key) {
			this.db = db;
			this.key = key;
		}
		
		public int compare(int record) throws CoreException {
			return db.stringCompare(record + FILE_NAME_OFFSET, key);
		}
	}

	public static class FindVisitor extends Visitor {

		private int record;
		
		public FindVisitor(Database db, String key) {
			super(db, key);
		}
		
		public boolean visit(int record) throws CoreException {
			this.record = record;
			return false;
		}
		
		public int findIn(BTree btree) throws CoreException {
			btree.visit(this);
			return record;
		}
		
	}
	
	public static PDOMFile insert(PDOMDatabase pdom, String filename) throws CoreException {
		BTree index = pdom.getFileIndex();
		PDOMFile pdomFile = find(pdom, filename);
		if (pdomFile == null) {
			Database db = pdom.getDB();
			int record = db.malloc(FILE_NAME_OFFSET + (filename.length() + 1) * Database.CHAR_SIZE);
			db.putInt(record + FIRST_NAME_OFFSET, 0);
			db.putString(record + FILE_NAME_OFFSET, filename);
			index.insert(record, new Comparator(db));
			pdomFile = new PDOMFile(pdom, record);
		}
		return pdomFile;
	}

	public static PDOMFile find(PDOMDatabase pdom, String filename) throws CoreException {
		BTree index = pdom.getFileIndex();
		int record = new FindVisitor(pdom.getDB(), filename).findIn(index);
		return (record != 0) ? new PDOMFile(pdom, record) : null;
	}
	
	public PDOMFile(PDOMDatabase pdom, int record) {
		this.pdom = pdom;
		this.record = record;
	}
	
	public int getRecord() {
		return record;
	}
	
	public String getFileName() throws CoreException {
		return pdom.getDB().getString(record + FILE_NAME_OFFSET);
	}
	
	public PDOMName getFirstName() throws CoreException {
		int namerec = pdom.getDB().getInt(record + FIRST_NAME_OFFSET);
		return namerec != 0 ? new PDOMName(pdom, namerec) : null;
	}

	public void setFirstName(PDOMName firstName) throws CoreException {
		int namerec = firstName != null ? firstName.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_NAME_OFFSET, namerec);
	}
	
	public void clear() throws CoreException {
		// Delete all the names in this file
		PDOMName name = getFirstName();
		while (name != null) {
			PDOMName nextName = name.getNextInFile();
			name.delete();
			name = nextName;
		}
		
		setFirstName(null);
	}
	
}
