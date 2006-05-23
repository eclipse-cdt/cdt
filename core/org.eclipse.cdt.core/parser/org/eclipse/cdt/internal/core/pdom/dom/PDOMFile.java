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

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IString;
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
	private static final int FIRST_MACRO = 12;
	private static final int FILE_NAME = 16;
	
	private static final int RECORD_SIZE = 20;
	
	public static class Comparator implements IBTreeComparator {
		private Database db;
		
		public Comparator(Database db) {
			this.db = db;
		}
		
		public int compare(int record1, int record2) throws CoreException {
			IString name1 = db.getString(db.getInt(record1 + FILE_NAME));
			IString name2 = db.getString(db.getInt(record2 + FILE_NAME));
			return name1.compare(name2);
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
			IString name = db.getString(db.getInt(record + FILE_NAME));
			return name.compare(key);
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
		record = db.malloc(RECORD_SIZE);
		db.putInt(record + FILE_NAME, db.newString(filename).getRecord());
		setFirstName(null);
		setFirstInclude(null);
		setFirstIncludedBy(null);
	}
	
	public int getRecord() {
		return record;
	}
	
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof PDOMFile) {
			PDOMFile other = (PDOMFile)obj;
			return pdom.equals(other.pdom) && record == other.record;
		}
		return false;
	}
	
	public IString getFileName() throws CoreException {
		Database db = pdom.getDB();
		return db.getString(db.getInt(record + FILE_NAME));
	}
	
	public PDOMName getFirstName() throws CoreException {
		int namerec = pdom.getDB().getInt(record + FIRST_NAME);
		return namerec != 0 ? new PDOMName(pdom, namerec) : null;
	}

	public void setFirstName(PDOMName firstName) throws CoreException {
		int namerec = firstName != null ? firstName.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_NAME, namerec);
	}
	
	public void addName(PDOMName name) throws CoreException {
		PDOMName firstName = getFirstName();
		if (firstName != null) {
			name.setNextInFile(firstName);
			firstName.setPrevInFile(name);
		}
		setFirstName(name);
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
	
	public PDOMMacro getFirstMacro() throws CoreException {
		int rec = pdom.getDB().getInt(record + FIRST_MACRO);
		return rec != 0 ? new PDOMMacro(pdom, rec) : null;
	}

	public void setFirstMacro(PDOMMacro macro) throws CoreException {
		int rec = macro != null ? macro.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_MACRO, rec);
	}
	
	public void addMacro(IASTPreprocessorMacroDefinition macro) throws CoreException {
		PDOMMacro firstMacro = getFirstMacro();
		
		// Make sure we don't already have one
		char[] name = macro.getName().toCharArray();
		PDOMMacro pdomMacro = firstMacro;
		while (pdomMacro != null) {
			if (pdomMacro.getName().equals(name))
				return;
			pdomMacro = pdomMacro.getNextMacro();
		}
		
		// Nope, add it in
		pdomMacro = new PDOMMacro(pdom, macro);
		pdomMacro.setNextMacro(getFirstMacro());
		setFirstMacro(pdomMacro);
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
		
		// Delete all the macros in this file
		PDOMMacro macro = getFirstMacro();
		while (macro != null) {
			PDOMMacro nextMacro = macro.getNextMacro();
			macro.delete();
			macro = nextMacro;
		}
		setFirstMacro(null);
		
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
		include.setIncludedBy(this);
		include.setIncludes(file);
		
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
		IString myFileName = getFileName();
		files.put(myFileName, this);
		
		todo.addLast(this);
		while (todo.size() > 0) {
			PDOMFile file = (PDOMFile)todo.removeFirst();
			PDOMInclude includedBy = file.getFirstIncludedBy();
			while (includedBy != null) {
				PDOMFile incFile = includedBy.getIncludedBy();
				IString incFileName = incFile.getFileName();
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
