/*******************************************************************************
 * Copyright (c) 2005, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndexFragment;
import org.eclipse.cdt.internal.core.index.IWritableIndex.IncludeInformation;
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
	private static final int LINKAGE_ID= 20;
	private static final int TIME_STAMP = 24;
	private static final int SCANNER_CONFIG_HASH= 32;

	private static final int RECORD_SIZE = 36;

	public static class Comparator implements IBTreeComparator {
		private Database db;

		public Comparator(Database db) {
			this.db = db;
		}

		public int compare(int record1, int record2) throws CoreException {
			IString name1 = db.getString(db.getInt(record1 + LOCATION_REPRESENTATION));
			IString name2 = db.getString(db.getInt(record2 + LOCATION_REPRESENTATION));
			int cmp= name1.compare(name2, true);
			if (cmp == 0) {
				cmp= db.getInt(record1+LINKAGE_ID) - db.getInt(record2+LINKAGE_ID);
			}
			return cmp;
		}
	}

	public PDOMFile(PDOM pdom, int record) {
		this.pdom = pdom;
		this.record = record;
	}

	public PDOMFile(PDOM pdom, IIndexFileLocation location, int linkageID) throws CoreException {
		this.pdom = pdom;
		Database db = pdom.getDB();
		record = db.malloc(RECORD_SIZE);
		String locationString = pdom.getLocationConverter().toInternalFormat(location);
		if(locationString==null)
			throw new CoreException(CCorePlugin.createStatus(Messages.getString("PDOMFile.toInternalProblem")+location.getURI())); //$NON-NLS-1$
		IString locationDBString = db.newString(locationString);
		db.putInt(record + LOCATION_REPRESENTATION, locationDBString.getRecord());
		db.putInt(record + LINKAGE_ID, linkageID);
		db.putLong(record + TIME_STAMP, 0);
		setFirstName(null);
		setFirstInclude(null);
		setFirstIncludedBy(null);
		setTimestamp(-1);
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

	public final int hashCode() {
		return System.identityHashCode(pdom) + 41*record;
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
	
	public int getLinkageID() throws CoreException {
		Database db = pdom.getDB();
		return db.getInt(record + LINKAGE_ID);
	}

	public long getTimestamp() throws CoreException {
		Database db = pdom.getDB();
		return db.getLong(record + TIME_STAMP);
	}

	public void setTimestamp(long timestamp) throws CoreException {
		Database db= pdom.getDB();
		db.putLong(record + TIME_STAMP, timestamp);
	}

	public int getScannerConfigurationHashcode() throws CoreException {
		Database db = pdom.getDB();
		return db.getInt(record + SCANNER_CONFIG_HASH);
	}

	public void setScannerConfigurationHashcode(int hashcode) throws CoreException {
		Database db= pdom.getDB();
		db.putInt(record + SCANNER_CONFIG_HASH, hashcode);
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
	
	public IIndexInclude getParsedInContext() throws CoreException {
		return getFirstIncludedBy();
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
			PDOMMacro pdomMacro = new PDOMMacro(pdom, macro, this);
			if (lastMacro == null) {
				setFirstMacro(pdomMacro);
			}
			else {
				lastMacro.setNextMacro(pdomMacro);
			}
			lastMacro= pdomMacro;
			pdom.afterAddMacro(pdomMacro);
		}
	}

	public void addNames(IASTName[][] names) throws CoreException {
		assert getFirstName() == null;
		HashMap<IASTName, PDOMName> nameCache= new HashMap<IASTName, PDOMName>();
		PDOMName lastName= null;
		for (int i = 0; i < names.length; i++) {
			IASTName[] name = names[i];
			if (name[0] != null) {
				PDOMName caller= nameCache.get(name[1]);
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
	}

	private PDOMName createPDOMName(IASTName name, PDOMName caller) {
		if (name.getBinding() instanceof IParameter) {
			return null;
		}
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

	public void clear(Collection<IIndexFileLocation> contextsRemoved) throws CoreException {
		// Remove the includes
		PDOMInclude include = getFirstInclude();
		while (include != null) {
			PDOMInclude nextInclude = include.getNextInIncludes();
			if (contextsRemoved != null && include.getPrevInIncludedByRecord() == 0) {
				contextsRemoved.add(include.getIncludesLocation());
			}
			include.delete();
			include = nextInclude;
		}
		setFirstInclude(include);

		// Delete all the macros in this file
		PDOMMacro macro = getFirstMacro();
		while (macro != null) {
			pdom.beforeRemoveMacro(macro);
			PDOMMacro nextMacro = macro.getNextMacro();
			macro.delete();
			macro = nextMacro;
		}
		setFirstMacro(null);

		// Delete all the names in this file
		ArrayList<PDOMName> names= new ArrayList<PDOMName>();
		PDOMName name = getFirstName();
		while (name != null) {
			names.add(name);
			name.getPDOMBinding().getLinkageImpl().onDeleteName(name);
			name= name.getNextInFile();
		}
		
		for (Iterator<PDOMName> iterator = names.iterator(); iterator.hasNext();) {
			name = iterator.next();
			name.delete();
		}
		setFirstName(null);
		setTimestamp(-1);
	}

	public void addIncludesTo(IncludeInformation[] includeInfos) throws CoreException {
		assert getFirstInclude() == null;

		PDOMInclude lastInclude= null;
		for (int i = 0; i < includeInfos.length; i++) {
			final IncludeInformation info= includeInfos[i];
			final PDOMFile targetFile= (PDOMFile) info.fTargetFile;
			
			PDOMInclude pdomInclude = new PDOMInclude(pdom, info.fStatement, this, targetFile);
			if (targetFile != null) {
				assert targetFile.getIndexFragment() instanceof IWritableIndexFragment;
				targetFile.addIncludedBy(pdomInclude, info.fIsContext);
			}
			if (lastInclude == null) {
				setFirstInclude(pdomInclude);
			}
			else {
				lastInclude.setNextInIncludes(pdomInclude);
			}
			lastInclude= pdomInclude;
		}
	}

	public void addIncludedBy(PDOMInclude include, boolean isContext) throws CoreException {
		PDOMInclude firstIncludedBy = getFirstIncludedBy();
		if (firstIncludedBy != null) {
			if (isContext) {
				setFirstIncludedBy(include);
				include.setNextInIncludedBy(firstIncludedBy);
				firstIncludedBy.setPrevInIncludedBy(include);				
			}
			else {
				PDOMInclude secondIncludedBy= firstIncludedBy.getNextInIncludedBy();
				if (secondIncludedBy != null) {
					include.setNextInIncludedBy(secondIncludedBy);
					secondIncludedBy.setPrevInIncludedBy(include);
				}
				include.setPrevInIncludedBy(firstIncludedBy);
				firstIncludedBy.setNextInIncludedBy(include);
			}
		}
		else {
			setFirstIncludedBy(include);
		}
	}

	public IIndexInclude[] getIncludes() throws CoreException {
		List<PDOMInclude> result= new ArrayList<PDOMInclude>();
		PDOMInclude include = getFirstInclude();
		while (include != null) {
			result.add(include);
			include = include.getNextInIncludes();
		}
		return result.toArray(new IIndexInclude[result.size()]);
	}

	public IIndexMacro[] getMacros() throws CoreException {
		List<PDOMMacro> result= new ArrayList<PDOMMacro>();
		PDOMMacro macro = getFirstMacro();
		while (macro != null) {
			result.add(macro);
			macro = macro.getNextMacro();
		}
		return result.toArray(new IIndexMacro[result.size()]);
	}

	public IIndexFragment getIndexFragment() {
		return pdom;
	}

	public IIndexName[] findNames(int offset, int length) throws CoreException {
		ArrayList<PDOMName> result= new ArrayList<PDOMName>();
		for (PDOMName name= getFirstName(); name != null; name= name.getNextInFile()) {
			int nameOffset=  name.getNodeOffset();
			if (nameOffset >= offset) {
				if (nameOffset + name.getNodeLength() <= offset+length) {
					result.add(name);
				}
				else if (name.isReference()) { 
					// names are ordered, but callers are inserted before
					// their references
					break;
				}
			}

		}
		return result.toArray(new IIndexName[result.size()]);
	}

	public static PDOMFile findFile(PDOM pdom, BTree btree, IIndexFileLocation location, int linkageID, IIndexLocationConverter strategy)
			throws CoreException {
		String internalRepresentation= strategy.toInternalFormat(location);
		int record= 0;
		if(internalRepresentation!=null) {
			Finder finder = new Finder(pdom.getDB(), internalRepresentation, linkageID);
			btree.accept(finder);
			record= finder.getRecord();
		}
		return record != 0 ? new PDOMFile(pdom, record) : null;
	}

	public static IIndexFragmentFile[] findFiles(PDOM pdom, BTree btree, IIndexFileLocation location, IIndexLocationConverter strategy)
			throws CoreException {
		String internalRepresentation= strategy.toInternalFormat(location);
		if(internalRepresentation!=null) {
			Finder finder = new Finder(pdom.getDB(), internalRepresentation, -1);
			btree.accept(finder);
			int[] records= finder.getRecords();
			PDOMFile[] result= new PDOMFile[records.length];
			for (int i = 0; i < result.length; i++) {
				result[i]= new PDOMFile(pdom, records[i]);
			}
			return result;
		}
		return new IIndexFragmentFile[0];
	}

	private static class Finder implements IBTreeVisitor {
		private static final int[] EMPTY = {};
		private final Database db;
		private final String rawKey;
		private int record;
		private int[] records;
		private final int linkageID;

		/**
		 * Searches for a file with the given linkage id.
		 */
		public Finder(Database db, String internalRepresentation, int linkageID) {
			this.db = db;
			this.rawKey = internalRepresentation;
			this.linkageID= linkageID;
		}

		public int[] getRecords() {
			if (records == null) {
				if (record == 0) {
					return EMPTY;
				}
				return new int[] {record};
			}
			return records;
		}

		public int compare(int record) throws CoreException {
			IString name = db.getString(db.getInt(record + PDOMFile.LOCATION_REPRESENTATION));
			int cmp= name.compare(rawKey, true);
			if (cmp == 0 && linkageID >= 0) {
				cmp= db.getInt(record + PDOMFile.LINKAGE_ID) - linkageID;
			}
			return cmp;
		}

		public boolean visit(int record) throws CoreException {
			if (linkageID >= 0) {
				this.record = record;
				return false;
			}
			if (this.record == 0) {
				this.record= record;
			}
			else if (this.records == null) {
				this.records= new int[] {this.record, record};
			}
			else {
				int[] cpy= new int[this.records.length+1];
				System.arraycopy(this.records, 0, cpy, 0, this.records.length);
				cpy[cpy.length-1]= record;
				this.records= cpy;
			}
			return linkageID < 0;
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
	
	public boolean hasContent() throws CoreException {
		return getTimestamp() != -1;
	}

	public void convertIncludersToUnresolved() throws CoreException {
		// Remove the includes
		PDOMInclude include = getFirstIncludedBy();
		while (include != null) {
			PDOMInclude nextInclude = include.getNextInIncludedBy();
			include.convertToUnresolved();
			include.setNextInIncludedBy(null);
			include.setPrevInIncludedBy(null);
			include = nextInclude;
		}
		setFirstIncludedBy(null);
	}
}
