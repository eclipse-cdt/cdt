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
import org.eclipse.cdt.internal.core.pdom.db.StringComparator;
import org.eclipse.cdt.internal.core.pdom.db.StringVisitor;
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
	
	public static class Comparator extends StringComparator {
		
		public Comparator(Database db) {
			super(db, FILE_NAME_OFFSET);
		}
	}
	
	public abstract static class Visitor extends StringVisitor {
		
		public Visitor(Database db, String key) {
			super(db, FILE_NAME_OFFSET, key);
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
	
	public int getFirstName() throws CoreException {
		return pdom.getDB().getInt(record + FIRST_NAME_OFFSET);
	}

	public void setFirstName(int firstName) throws CoreException {
		pdom.getDB().putInt(record + FIRST_NAME_OFFSET, firstName);
	}
	
	public void free() throws CoreException {
		pdom.getDB().free(record);
	}
	
}
