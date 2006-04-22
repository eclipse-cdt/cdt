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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.cdt.internal.core.pdom.PDOM;
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

	private final PDOM pdom;
	private final int record;
	
	private static final int FIRST_NAME = 0;
	private static final int FIRST_INCLUDE = 4;
	private static final int FIRST_INCLUDED_BY = 8;
	private static final int FILE_NAME = 12;
	
	private static final int RECORD_SIZE = 12; // + length of string
	
	public static class Comparator implements IBTreeComparator {
		private Database db;
		
		public Comparator(Database db) {
			this.db = db;
		}
		
		public int compare(int record1, int record2) throws CoreException {
			return db.stringCompare(record1 + FILE_NAME, record2 + FILE_NAME);
		}
	}
	
	public static class Finder implements IBTreeVisitor {
		private final Database db;
		private final String key;
		private int record;
		
		public Finder(Database db, String key) {
			this.db = db;
			this.key = key;
		}
		
		public int compare(int record) throws CoreException {
			return db.stringCompare(record + FILE_NAME, key);
		}

		public boolean visit(int record) throws CoreException {
			this.record = record;
			return false;
		}
		
		public int getRecord() {
			return record;
		}
	}
	
	public PDOMFile(PDOM pdom, int record) {
		this.pdom = pdom;
		this.record = record;
	}
	
	public PDOMFile(PDOM pdom, String filename) throws CoreException {
		this.pdom = pdom;
		Database db = pdom.getDB();
		record = db.malloc(RECORD_SIZE + (filename.length() + 1) * 2);
		db.putString(record + FILE_NAME, filename);
		setFirstName(null);
		setFirstInclude(null);
		setFirstIncludedBy(null);
	}
	
	public int getRecord() {
		return record;
	}
	
	public String getFileName() throws CoreException {
		return pdom.getDB().getString(record + FILE_NAME);
	}
	
	public PDOMName getFirstName() throws CoreException {
		int namerec = pdom.getDB().getInt(record + FIRST_NAME);
		return namerec != 0 ? new PDOMName(pdom, namerec) : null;
	}

	public void setFirstName(PDOMName firstName) throws CoreException {
		int namerec = firstName != null ? firstName.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_NAME, namerec);
	}
	
	public PDOMInclude getFirstInclude() throws CoreException {
		int increc = pdom.getDB().getInt(record + FIRST_INCLUDE);
		return increc != 0 ? new PDOMInclude(pdom, increc) : null;
	}
	
	public void setFirstInclude(PDOMInclude include) throws CoreException {
		int rec = include != null ? include.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_INCLUDE, rec);
	}
	
	public PDOMInclude getFirstIncludedBy() throws CoreException {
		int rec = pdom.getDB().getInt(record + FIRST_INCLUDED_BY);
		return rec != 0 ? new PDOMInclude(pdom, rec) : null;
	}
	
	public void setFirstIncludedBy(PDOMInclude includedBy) throws CoreException {
		int rec = includedBy != null ? includedBy.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_INCLUDED_BY, rec);
	}
	
	public void clear() throws CoreException {
		// Remove the includes
		PDOMInclude include = getFirstInclude();
		while (include != null) {
			PDOMInclude nextInclude = include.getNextInIncludes();
			include.delete();
			include = nextInclude;
		}
		setFirstInclude(include);
		
		// Delete all the names in this file
		PDOMName name = getFirstName();
		while (name != null) {
			PDOMName nextName = name.getNextInFile();
			name.delete();
			name = nextName;
		}
		
		setFirstName(null);
	}
	
	public PDOMInclude addIncludeTo(PDOMFile file) throws CoreException {
		PDOMInclude include = new PDOMInclude(pdom);
		
		PDOMInclude firstInclude = getFirstInclude();
		if (firstInclude != null) {
			include.setNextInIncludes(firstInclude);
		}
		setFirstInclude(include);
		
		file.addIncludedBy(include);
		return include;
	}
	
	public void addIncludedBy(PDOMInclude include) throws CoreException {
		PDOMInclude firstIncludedBy = getFirstIncludedBy();
		if (firstIncludedBy != null) {
			include.setNextInIncludedBy(firstIncludedBy);
			firstIncludedBy.setPrevInIncludedBy(include);
		}
		setFirstIncludedBy(include);
	}
	
	public PDOMFile[] getAllIncludedBy() throws CoreException {
		Map files = new HashMap();
		LinkedList todo = new LinkedList();
		
		// Add me in to make sure we don't get caught in a circular include
		String myFileName = getFileName();
		files.put(myFileName, this);
		
		todo.addLast(this);
		while (todo.size() > 0) {
			PDOMFile file = (PDOMFile)todo.removeFirst();
			PDOMInclude includedBy = getFirstIncludedBy();
			while (includedBy != null) {
				PDOMFile incFile = includedBy.getIncludedBy();
				String incFileName = incFile.getFileName();
				if (files.get(incFileName) == null) {
					files.put(incFileName, incFile);
					todo.addLast(incFile);
				}
				includedBy = includedBy.getNextInIncludedBy();
			}
		}
		
		// Now remove me
		files.remove(myFileName);
		
		Collection values = files.values(); 
		return (PDOMFile[])values.toArray(new PDOMFile[values.size()]);
	}
	
}
