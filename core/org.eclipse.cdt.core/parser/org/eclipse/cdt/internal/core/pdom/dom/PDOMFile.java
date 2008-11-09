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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorUndefStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.cdt.internal.core.index.IWritableIndexFragment;
import org.eclipse.cdt.internal.core.index.IndexFileLocation;
import org.eclipse.cdt.internal.core.index.IWritableIndex.IncludeInformation;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Represents a file containing names.
 * 
 * @author Doug Schaefer
 */
public class PDOMFile implements IIndexFragmentFile {
	private final PDOM pdom;
	private final int record;
	private IIndexFileLocation location;
	private PDOMLinkage fLinkage;

	private static final int FIRST_NAME = 0;
	private static final int FIRST_INCLUDE = 4;
	private static final int FIRST_INCLUDED_BY = 8;
	private static final int FIRST_MACRO = 12;
	private static final int LOCATION_REPRESENTATION = 16;
	private static final int LINKAGE_ID= 20;
	private static final int TIME_STAMP = 24;
	private static final int SCANNER_CONFIG_HASH= 32;
	private static final int FIRST_USING_DIRECTIVE= 36;
	private static final int FIRST_MACRO_REFERENCE= 40;

	private static final int RECORD_SIZE= 44;

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
				cmp= db.getInt(record1 + LINKAGE_ID) - db.getInt(record2 + LINKAGE_ID);
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
		this.location= location;
		Database db = pdom.getDB();
		record = db.malloc(RECORD_SIZE);
		String locationString = pdom.getLocationConverter().toInternalFormat(location);
		if (locationString==null)
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

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof PDOMFile) {
			PDOMFile other = (PDOMFile)obj;
			return pdom.equals(other.pdom) && record == other.record;
		}
		return false;
	}

	@Override
	public final int hashCode() {
		return System.identityHashCode(pdom) + 41*record;
	}
	
	/**
	 * Directly changes this record's internal location string. The format
	 * of this string is unspecified in general and is determined by the 
	 * associated IIndexLocationConverter
	 * @param internalLocation
	 * @throws CoreException
	 */
	public void setInternalLocation(String internalLocation) throws CoreException {
		Database db = pdom.getDB();
		int oldRecord = db.getInt(record + LOCATION_REPRESENTATION);
		db.free(oldRecord);
		db.putInt(record + LOCATION_REPRESENTATION, db.newString(internalLocation).getRecord());
		location= null;
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

	private PDOMName getFirstName() throws CoreException {
		int namerec = pdom.getDB().getInt(record + FIRST_NAME);
		return namerec != 0 ? new PDOMName(pdom, namerec) : null;
	}

	private void setFirstName(PDOMName firstName) throws CoreException {
		int namerec = firstName != null ? firstName.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_NAME, namerec);
	}

	private PDOMMacroReferenceName getFirstMacroReference() throws CoreException {
		int namerec = pdom.getDB().getInt(record + FIRST_MACRO_REFERENCE);
		return namerec != 0 ? new PDOMMacroReferenceName(pdom, namerec) : null;
	}

	private void setFirstMacroReference(PDOMMacroReferenceName firstName) throws CoreException {
		int namerec = firstName != null ? firstName.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_MACRO_REFERENCE, namerec);
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

	public void addMacros(IASTPreprocessorStatement[] macros) throws CoreException {
		assert getFirstMacro() == null;

		PDOMMacro lastMacro= null;
		final PDOMLinkage linkage = getLinkage();
		for (IASTPreprocessorStatement stmt : macros) {
			PDOMMacro pdomMacro= null;
			if (stmt instanceof IASTPreprocessorMacroDefinition) {
				IASTPreprocessorMacroDefinition macro= (IASTPreprocessorMacroDefinition) stmt;
				PDOMMacroContainer container= linkage.getMacroContainer(macro.getName().toCharArray());
				pdomMacro = new PDOMMacro(pdom, container, macro, this);
			} else if (stmt instanceof IASTPreprocessorUndefStatement) {
				IASTPreprocessorUndefStatement undef= (IASTPreprocessorUndefStatement) stmt;
				PDOMMacroContainer container= linkage.getMacroContainer(undef.getMacroName().toCharArray());
				pdomMacro = new PDOMMacro(pdom, container, undef, this);
			}
			if (pdomMacro != null) {
				if (lastMacro == null) {
					setFirstMacro(pdomMacro);
				} else {
					lastMacro.setNextMacro(pdomMacro);
				}
				lastMacro= pdomMacro;
			}
		}
	}

	PDOMLinkage getLinkage() throws CoreException {
		if (fLinkage == null) {
			final String linkageName = Linkage.getLinkageName(getLinkageID());
			fLinkage= pdom.createLinkage(linkageName);
			if (fLinkage == null) {
				throw new CoreException(createStatus("Unsupported linkage: " + linkageName)); //$NON-NLS-1$
			}
		}
		return fLinkage;
	}

	public void addNames(IASTName[][] names) throws CoreException {
		assert getFirstName() == null;
		assert getFirstMacroReference() == null;
		final PDOMLinkage linkage= getLinkage();
		HashMap<IASTName, PDOMName> nameCache= new HashMap<IASTName, PDOMName>();
		PDOMName lastName= null;
		PDOMMacroReferenceName lastMacroName= null;
		for (IASTName[] name : names) {
			if (name[0] != null) {
				PDOMName caller= nameCache.get(name[1]);
				IIndexFragmentName fname= createPDOMName(linkage, name[0], caller);
				if (fname instanceof PDOMName) {
					PDOMName pdomName = (PDOMName) fname;
					nameCache.put(name[0], pdomName);
					if (lastName == null) {
						setFirstName(pdomName);
					} else {
						lastName.setNextInFile(pdomName);
					}
					lastName= pdomName;
				} else if (fname instanceof PDOMMacroReferenceName) {
					PDOMMacroReferenceName macroName = (PDOMMacroReferenceName) fname;
					if (lastMacroName == null) {
						setFirstMacroReference(macroName);
					} else {
						lastMacroName.setNextInFile(macroName);
					}
					lastMacroName= macroName;
				}
			}
		}
	}

	private IIndexFragmentName createPDOMName(PDOMLinkage linkage, IASTName name, PDOMName caller) {
		final IBinding binding = name.getBinding();
		if (binding instanceof IParameter) {
			return null;
		}
		try {
			if (binding instanceof IMacroBinding
					|| (binding == null && name.getPropertyInParent() == IASTPreprocessorStatement.MACRO_NAME)) {
				return createPDOMMacroReferenceName(linkage, name);
			}
			PDOMBinding pdomBinding = linkage.addBinding(name);
			if (pdomBinding != null) {
				final PDOMName result= new PDOMName(pdom, name, this, pdomBinding, caller);
				linkage.onCreateName(this, name, result);
				return result;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	private IIndexFragmentName createPDOMMacroReferenceName(PDOMLinkage linkage, IASTName name) throws CoreException {
		PDOMMacroContainer cont= linkage.getMacroContainer(name.toCharArray());
		return new PDOMMacroReferenceName(pdom, name, this, cont);
	}

	public void clear(Collection<IIndexFileLocation> contextsRemoved) throws CoreException {
		ICPPUsingDirective[] directives= getUsingDirectives();
		for (ICPPUsingDirective ud : directives) {
			if (ud instanceof IPDOMNode) {
				((IPDOMNode) ud).delete(null);
			}
		}
		setFirstUsingDirectiveRec(0);

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
		PDOMLinkage linkage= getLinkage();
		PDOMMacro macro = getFirstMacro();
		while (macro != null) {
			PDOMMacro nextMacro = macro.getNextMacro();
			macro.delete(linkage);
			macro = nextMacro;
		}
		setFirstMacro(null);

		// Delete all the names in this file
		ArrayList<PDOMName> names= new ArrayList<PDOMName>();
		PDOMName name = getFirstName();
		while (name != null) {
			names.add(name);
			linkage.onDeleteName(name);
			name= name.getNextInFile();
		}
		for (Iterator<PDOMName> iterator = names.iterator(); iterator.hasNext();) {
			name = iterator.next();
			name.delete();
		}
		setFirstName(null);

		// Delete all macro references
		ArrayList<PDOMMacroReferenceName> mrefs= new ArrayList<PDOMMacroReferenceName>();
		PDOMMacroReferenceName mref = getFirstMacroReference();
		while (mref != null) {
			mrefs.add(mref);
			mref= mref.getNextInFile();
		}
		for (PDOMMacroReferenceName m : mrefs) {
			m.delete();
		}
		setFirstMacroReference(null);

		setTimestamp(-1);
	}

	public void addIncludesTo(IncludeInformation[] includeInfos) throws CoreException {
		assert getFirstInclude() == null;

		PDOMInclude lastInclude= null;
		for (final IncludeInformation info : includeInfos) {
			final PDOMFile targetFile= (PDOMFile) info.fTargetFile;
			
			PDOMInclude pdomInclude = new PDOMInclude(pdom, info.fStatement, this, targetFile);
			if (targetFile != null) {
				assert targetFile.getIndexFragment() instanceof IWritableIndexFragment;
				targetFile.addIncludedBy(pdomInclude, info.fIsContext);
			}
			if (lastInclude == null) {
				setFirstInclude(pdomInclude);
			} else {
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
			} else {
				PDOMInclude secondIncludedBy= firstIncludedBy.getNextInIncludedBy();
				if (secondIncludedBy != null) {
					include.setNextInIncludedBy(secondIncludedBy);
					secondIncludedBy.setPrevInIncludedBy(include);
				}
				include.setPrevInIncludedBy(firstIncludedBy);
				firstIncludedBy.setNextInIncludedBy(include);
			}
		} else {
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
		ArrayList<IIndexName> result= new ArrayList<IIndexName>();
		for (PDOMName name= getFirstName(); name != null; name= name.getNextInFile()) {
			int nameOffset=  name.getNodeOffset();
			if (nameOffset >= offset) {
				if (nameOffset + name.getNodeLength() <= offset+length) {
					result.add(name);
				} else if (name.isReference()) { 
					// names are ordered, but callers are inserted before
					// their references
					break;
				}
			}

		}
		for (PDOMMacro macro= getFirstMacro(); macro != null; macro= macro.getNextMacro()) {
			int nameOffset=  macro.getNodeOffset();
			if (nameOffset >= offset) {
				if (nameOffset + macro.getNodeLength() <= offset+length) {
					IIndexFragmentName name= macro.getDefinition();
					if (name != null) {
						result.add(name);
					}
				} else { 
					break;
				}
			}
		}
		for (PDOMMacroReferenceName name= getFirstMacroReference(); name != null; name= name.getNextInFile()) {
			int nameOffset=  name.getNodeOffset();
			if (nameOffset >= offset) {
				if (nameOffset + name.getNodeLength() <= offset+length) {
					result.add(name);
				} else { 
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
		if (internalRepresentation != null) {
			Finder finder = new Finder(pdom.getDB(), internalRepresentation, linkageID);
			btree.accept(finder);
			record= finder.getRecord();
		}
		return record != 0 ? new PDOMFile(pdom, record) : null;
	}

	public static IIndexFragmentFile[] findFiles(PDOM pdom, BTree btree, IIndexFileLocation location, IIndexLocationConverter strategy)
			throws CoreException {
		String internalRepresentation= strategy.toInternalFormat(location);
		if (internalRepresentation != null) {
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
				return new int[] { record };
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
			} else if (this.records == null) {
				this.records= new int[] {this.record, record};
			} else {
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
		if (location == null) {
			Database db = pdom.getDB();
			String raw = db.getString(db.getInt(record + LOCATION_REPRESENTATION)).getString();
			location= pdom.getLocationConverter().fromInternalFormat(raw);
			if (location == null) {
				URI uri;
				try {
					int idx= raw.lastIndexOf('>');
					uri= new URI("file", null, raw.substring(idx+1), null); //$NON-NLS-1$
				} catch (URISyntaxException e) {
					uri= URI.create("file:/unknown-location"); //$NON-NLS-1$
				}
				location= new IndexFileLocation(uri, null);
			}
		}
		return location;
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

	public int getFirstUsingDirectiveRec() throws CoreException {
		return pdom.getDB().getInt(record + FIRST_USING_DIRECTIVE);
	}

	public void setFirstUsingDirectiveRec(int rec) throws CoreException {
		pdom.getDB().putInt(record + FIRST_USING_DIRECTIVE, rec);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexFile#getUsingDirectives()
	 */
	public ICPPUsingDirective[] getUsingDirectives() throws CoreException {
		PDOMLinkage linkage= pdom.getLinkage(ILinkage.CPP_LINKAGE_NAME);
		if (linkage != null) {
			return linkage.getUsingDirectives(this);
		}
		return ICPPUsingDirective.EMPTY_ARRAY;
	}
	
	// required because we cannot reference CCorePlugin in order for StandaloneIndexer to work
	private IStatus createStatus(String msg) {
		return new Status(IStatus.ERROR, "org.eclipse.cdt.core", IStatus.ERROR, msg, null); //$NON-NLS-1$
	}
}
