/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndexFragment;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
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
public class PDOMFile implements IIndexFragmentFile {

	private final PDOM pdom;
	private final int record;

	private static final int FIRST_NAME = 0;
	private static final int FIRST_INCLUDE = 4;
	private static final int FIRST_INCLUDED_BY = 8;
	private static final int FIRST_MACRO = 12;
	private static final int LOCATION_REPRESENTATION = 16;
	private static final int TIME_STAMP = 20;

	private static final int RECORD_SIZE = 28;

	public static class Comparator implements IBTreeComparator {
		private Database db;

		public Comparator(Database db) {
			this.db = db;
		}

		public int compare(int record1, int record2) throws CoreException {
			IString name1 = db.getString(db.getInt(record1 + LOCATION_REPRESENTATION));
			IString name2 = db.getString(db.getInt(record2 + LOCATION_REPRESENTATION));
			return name1.compare(name2, true);
		}
	}

	public PDOMFile(PDOM pdom, int record) {
		this.pdom = pdom;
		this.record = record;
	}

	public PDOMFile(PDOM pdom, IIndexFileLocation location) throws CoreException {
		this.pdom = pdom;
		Database db = pdom.getDB();
		record = db.malloc(RECORD_SIZE);
		String locationString = pdom.getLocationConverter().toInternalFormat(location);
		if(locationString==null)
			throw new CoreException(CCorePlugin.createStatus(Messages.getString("PDOMFile.toInternalProblem")+location.getURI())); //$NON-NLS-1$
		IString locationDBString = db.newString(locationString);
		db.putInt(record + LOCATION_REPRESENTATION, locationDBString.getRecord());
		db.putLong(record + TIME_STAMP, 0);
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

	/**
	 * Directly changes this record's internal location string. The format
	 * of this string is unspecified in general and is determined by the 
	 * associated IIndexLocationConverter
	 * @param newName
	 * @throws CoreException
	 */
	public void setInternalLocation(String internalLocation) throws CoreException {
		Database db = pdom.getDB();
		int oldRecord = db.getInt(record + LOCATION_REPRESENTATION);
		db.free(oldRecord);
		db.putInt(record + LOCATION_REPRESENTATION, db.newString(internalLocation).getRecord());
	}

	public long getTimestamp() throws CoreException {
		Database db = pdom.getDB();
		return db.getLong(record + TIME_STAMP);
	}

	public void setTimestamp(long timestamp) throws CoreException {
		Database db= pdom.getDB();
		db.putLong(record + TIME_STAMP, timestamp);
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

	public PDOMMacro getFirstMacro() throws CoreException {
		int rec = pdom.getDB().getInt(record + FIRST_MACRO);
		return rec != 0 ? new PDOMMacro(pdom, rec) : null;
	}

	public void setFirstMacro(PDOMMacro macro) throws CoreException {
		int rec = macro != null ? macro.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_MACRO, rec);
	}

	public void addMacros(IASTPreprocessorMacroDefinition[] macros) throws CoreException {
		assert getFirstMacro() == null;

		PDOMMacro lastMacro= null;
		for (int i = 0; i < macros.length; i++) {
			IASTPreprocessorMacroDefinition macro = macros[i];
			PDOMMacro pdomMacro = new PDOMMacro(pdom, macro);
			if (lastMacro == null) {
				setFirstMacro(pdomMacro);
			}
			else {
				lastMacro.setNextMacro(pdomMacro);
			}
			lastMacro= pdomMacro;
		}
	}

	public void addNames(IASTName[][] names) throws CoreException {
		assert getFirstName() == null;
		HashMap nameCache= new HashMap();
		PDOMName lastName= null;
		for (int i = 0; i < names.length; i++) {
			IASTName[] name = names[i];
			PDOMName caller= (PDOMName) nameCache.get(name[1]);
			PDOMName pdomName = createPDOMName(name[0], caller);
			if (pdomName != null) {
				nameCache.put(name[0], pdomName);
				if (lastName == null) {
					setFirstName(pdomName);
				}
				else {
					lastName.setNextInFile(pdomName);
				}
				lastName= pdomName;
			}
		}
	}

	private PDOMName createPDOMName(IASTName name, PDOMName caller) {
		PDOMName result= null;
		try {
			PDOMBinding binding = ((WritablePDOM) pdom).addBinding(name);
			if (binding != null) {
				result= new PDOMName(pdom, name, this, binding, caller);
				binding.getLinkageImpl().onCreateName(result, name);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return result;
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
		ArrayList names= new ArrayList();
		PDOMName name = getFirstName();
		while (name != null) {
			names.add(name);
			name.getPDOMBinding().getLinkageImpl().onDeleteName(name);
			name= name.getNextInFile();
		}
		
		for (Iterator iterator = names.iterator(); iterator.hasNext();) {
			name = (PDOMName) iterator.next();
			name.delete();
		}
		setFirstName(null);
	}

	public void addIncludesTo(IIndexFragmentFile[] files, IASTPreprocessorIncludeStatement[] includes) throws CoreException {
		assert files.length == includes.length;
		assert getFirstInclude() == null;

		PDOMInclude lastInclude= null;
		for (int i = 0; i < includes.length; i++) {
			IASTPreprocessorIncludeStatement statement = includes[i];
			PDOMFile thisIncludes= (PDOMFile) files[i];
			assert thisIncludes.getIndexFragment() instanceof IWritableIndexFragment;

			PDOMInclude pdomInclude = new PDOMInclude(pdom, statement);
			pdomInclude.setIncludedBy(this);
			pdomInclude.setIncludes(thisIncludes);

			thisIncludes.addIncludedBy(pdomInclude);
			if (lastInclude == null) {
				setFirstInclude(pdomInclude);
			}
			else {
				lastInclude.setNextInIncludes(pdomInclude);
			}
			lastInclude= pdomInclude;
		}
	}

	public void addIncludedBy(PDOMInclude include) throws CoreException {
		PDOMInclude firstIncludedBy = getFirstIncludedBy();
		if (firstIncludedBy != null) {
			include.setNextInIncludedBy(firstIncludedBy);
			firstIncludedBy.setPrevInIncludedBy(include);
		}
		setFirstIncludedBy(include);
	}



	public IIndexInclude[] getIncludes() throws CoreException {
		List result= new ArrayList();
		PDOMInclude include = getFirstInclude();
		while (include != null) {
			result.add(include);
			include = include.getNextInIncludes();
		}
		return (IIndexInclude[]) result.toArray(new IIndexInclude[result.size()]);
	}

	public IIndexMacro[] getMacros() throws CoreException {
		List result= new ArrayList();
		PDOMMacro macro = getFirstMacro();
		while (macro != null) {
			result.add(macro);
			macro = macro.getNextMacro();
		}
		return (IIndexMacro[]) result.toArray(new IIndexMacro[result.size()]);
	}

	public IIndexFragment getIndexFragment() {
		return pdom;
	}

	public IIndexName[] findNames(int offset, int length) throws CoreException {
		ArrayList result= new ArrayList();
		for (PDOMName name= getFirstName(); name != null; name= name.getNextInFile()) {
			int nameOffset=  name.getNodeOffset();
			if (nameOffset >= offset) {
				if (nameOffset == offset) {
					if (name.getNodeLength() == length) {
						result.add(name);
					}
				}
				else if (name.isReference()) { 
					// names are ordered, but callers are inserted before
					// their references
					break;
				}
			}

		}
		return (IIndexName[]) result.toArray(new IIndexName[result.size()]);
	}

	public static IIndexFragmentFile findFile(PDOM pdom, BTree btree, IIndexFileLocation location, IIndexLocationConverter strategy)
			throws CoreException {
		Finder finder = new Finder(pdom.getDB(), location, strategy);
		btree.accept(finder);
		int record = finder.getRecord();
		return record != 0 ? new PDOMFile(pdom, record) : null;
	}
	private static class Finder implements IBTreeVisitor {
		private final Database db;
		private final String rawKey;
		private int record;

		public Finder(Database db, IIndexFileLocation location, IIndexLocationConverter strategy)
			throws CoreException
		{
			this.db = db;
			this.rawKey = strategy.toInternalFormat(location);
		}

		public int compare(int record) throws CoreException {
			IString name = db.getString(db.getInt(record + PDOMFile.LOCATION_REPRESENTATION));
			return name.compare(rawKey, true);
		}

		public boolean visit(int record) throws CoreException {
			this.record = record;
			return false;
		}

		public int getRecord() {
			return record;
		}
	}

	public IIndexFileLocation getLocation() throws CoreException {
		Database db = pdom.getDB();
		String raw = db.getString(db.getInt(record + LOCATION_REPRESENTATION)).getString();
		IIndexFileLocation result = pdom.getLocationConverter().fromInternalFormat(raw);
		if(result==null)
			throw new CoreException(CCorePlugin.createStatus(Messages.getString("PDOMFile.toExternalProblem")+raw)); //$NON-NLS-1$
		return result;
	}
	
	public boolean hasNames() throws CoreException {
		return getFirstName()!=null;
	}
}
