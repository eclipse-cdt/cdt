/*******************************************************************************
 * Copyright (c) 2005, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
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
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.cdt.internal.core.index.IWritableIndex.IncludeInformation;
import org.eclipse.cdt.internal.core.index.IWritableIndexFragment;
import org.eclipse.cdt.internal.core.index.IndexFileLocation;
import org.eclipse.cdt.internal.core.parser.scanner.SignificantMacros;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.YieldableIndexLock;
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
	private final PDOMLinkage fLinkage;
	private final long record;
	private IIndexFileLocation location;  // No need to make volatile, all fields of IndexFileLocation are final.
	private ISignificantMacros sigMacros;  // No need to make volatile, all fields of SignificantMacros are either final or atomically updated.

	private static final int FIRST_NAME = 0;
	private static final int FIRST_INCLUDE = FIRST_NAME + Database.PTR_SIZE;
	private static final int FIRST_INCLUDED_BY = FIRST_INCLUDE + Database.PTR_SIZE;
	private static final int FIRST_MACRO = FIRST_INCLUDED_BY + Database.PTR_SIZE;
	private static final int LOCATION_REPRESENTATION = FIRST_MACRO + Database.PTR_SIZE;
	private static final int LINKAGE_ID= LOCATION_REPRESENTATION + Database.PTR_SIZE;  // size 3
	private static final int FLAGS= LINKAGE_ID + 3;  // size 1
	private static final int TIME_STAMP = FLAGS + 1; // long
	private static final int CONTENT_HASH= TIME_STAMP + 8;  // long
	private static final int ENCODING_HASH= CONTENT_HASH + 8;
	private static final int LAST_USING_DIRECTIVE= ENCODING_HASH + 4;
	private static final int FIRST_MACRO_REFERENCE= LAST_USING_DIRECTIVE + Database.PTR_SIZE;
	private static final int SIGNIFICANT_MACROS= FIRST_MACRO_REFERENCE + Database.PTR_SIZE;
	private static final int RECORD_SIZE= SIGNIFICANT_MACROS + Database.PTR_SIZE;   // 8*PTR_SIZE + 3+1+8+8+4 = 56

	private static final int FLAG_PRAGMA_ONCE_SEMANTICS	= 0x01;

	public static class Comparator implements IBTreeComparator {
		private Database db;

		public Comparator(Database db) {
			this.db = db;
		}

		@Override
		public int compare(long record1, long record2) throws CoreException {
			IString name1 = db.getString(db.getRecPtr(record1 + LOCATION_REPRESENTATION));
			IString name2 = db.getString(db.getRecPtr(record2 + LOCATION_REPRESENTATION));
			int cmp= name1.compare(name2, true);
			if (cmp == 0) {
				cmp= db.get3ByteUnsignedInt(record1 + LINKAGE_ID) - db.get3ByteUnsignedInt(record2 + LINKAGE_ID);
				if (cmp == 0) {
					IString sm1= getString(record1 + SIGNIFICANT_MACROS);
					IString sm2= getString(record2 + SIGNIFICANT_MACROS);
					if (sm1 == null) {
						cmp= sm2 == null ? 0 : -1;
					} else if (sm2 == null) {
						cmp= 1;
					} else {
						cmp= sm1.compare(sm2, true);
					}
				}
			}
			return cmp;
		}
		
		private IString getString(long offset) throws CoreException {
			long rec = db.getRecPtr(offset);
			return rec != 0 ? db.getString(rec) : null;
		}
	}

	public PDOMFile(PDOMLinkage linkage, long record) {
		fLinkage = linkage;
		this.record = record;
	}

	public PDOMFile(PDOMLinkage linkage, IIndexFileLocation location, int linkageID, ISignificantMacros macros) throws CoreException {
		fLinkage = linkage;
		this.location= location;
		Database db = fLinkage.getDB();
		record = db.malloc(RECORD_SIZE);
		String locationString = fLinkage.getPDOM().getLocationConverter().toInternalFormat(location);
		if (locationString == null)
			throw new CoreException(CCorePlugin.createStatus(Messages.getString("PDOMFile.toInternalProblem") + location.getURI())); //$NON-NLS-1$
		IString locationDBString = db.newString(locationString);
		db.putRecPtr(record + LOCATION_REPRESENTATION, locationDBString.getRecord());
		db.put3ByteUnsignedInt(record + LINKAGE_ID, linkageID);
		db.putRecPtr(record + SIGNIFICANT_MACROS, db.newString(macros.encode()).getRecord());
		setTimestamp(-1);
	}

	public long getRecord() {
		return record;
	}

	public PDOM getPDOM() {
		return fLinkage.getPDOM();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof PDOMFile) {
			PDOMFile other = (PDOMFile)obj;
			return fLinkage.getPDOM().equals(other.getLinkage().getPDOM()) && record == other.record;
		}
		return false;
	}

	@Override
	public final int hashCode() {
		return System.identityHashCode(fLinkage.getPDOM()) + (int) (41 * record);
	}

	/**
	 * Transfers names, macros and includes from another file to this one and deletes the other file.
	 * @param sourceFile the file to transfer the local bindings from.
	 * @throws CoreException
	 */
	public void replaceContentsFrom(PDOMFile sourceFile) throws CoreException {
		// Delete current content
		clear();

		// Link in the using directives
		setLastUsingDirective(sourceFile.getLastUsingDirectiveRec());

		// Link in the includes, replace the owner.
		PDOMInclude include = sourceFile.getFirstInclude();
		setFirstInclude(include);
		for (; include != null; include= include.getNextInIncludes()) {
			include.setIncludedBy(this);
		}

		// In the unexpected case that there is an included by relation, append it.
		transferIncluders(sourceFile);

		// Link in the macros.
		PDOMMacro macro = sourceFile.getFirstMacro();
		setFirstMacro(macro);
		for (; macro != null; macro = macro.getNextMacro()) {
			macro.setFile(this);
		}

		// Link in macro references
		PDOMMacroReferenceName mref = sourceFile.getFirstMacroReference();
		setFirstMacroReference(mref);
		for (; mref != null; mref = mref.getNextInFile()) {
			mref.setFile(this);
		}

		// Replace all the names in this file
		PDOMName name = sourceFile.getFirstName();
		setFirstName(name);
		for (; name != null; name= name.getNextInFile()) {
			name.setFile(this);
		}

		setTimestamp(sourceFile.getTimestamp());
		setEncodingHashcode(sourceFile.getEncodingHashcode());
		setContentsHash(sourceFile.getContentsHash());

		// Transfer the flags. 
		Database db= fLinkage.getDB();
		db.putByte(record + FLAGS, db.getByte(sourceFile.record + FLAGS));
		
		// Delete the source file
		sourceFile.delete();
	}

	@Override
	public void transferIncluders(IIndexFragmentFile sourceFile) throws CoreException {
		PDOMFile source= (PDOMFile) sourceFile;
		PDOMInclude include = source.getFirstIncludedBy();
		if (include != null) {
			// Detach the includes
			source.setFirstIncludedBy(null);
			// Adjust the includes
			for (PDOMInclude i= include; i != null; i= i.getNextInIncludedBy()) {
				i.setIncludes(this);
			}
			// Append the includes
			PDOMInclude last= getFirstIncludedBy();
			if (last == null) {
				setFirstIncludedBy(include);
			} else {
				for (PDOMInclude i= last; i != null; i= i.getNextInIncludedBy()) {
					last= i;
				}
				last.setNextInIncludedBy(include);
				include.setPrevInIncludedBy(last);
			}
		}
	}

	@Override
	public void transferContext(IIndexFragmentFile sourceFile) throws CoreException {
		PDOMFile source= (PDOMFile) sourceFile;
		PDOMInclude include = source.getFirstIncludedBy();
		if (include != null) {
			// Detach the include
			final PDOMInclude next = include.getNextInIncludedBy();
			include.setNextInIncludedBy(null);
			source.setFirstIncludedBy(next);
			if (next != null)
				next.setPrevInIncludedBy(null);
			
			// Adjust the include
			include.setIncludes(this);

			// Insert the include
			addIncludedBy(include, false);
		}
	}

	/**
	 * This method should not be called on PDOMFile objects that are referenced by the file index.
	 * @param location a new location
	 * @throws CoreException
	 */
	public void setLocation(IIndexFileLocation location) throws CoreException {
		String locationString = fLinkage.getPDOM().getLocationConverter().toInternalFormat(location);
		if (locationString == null) {
			throw new CoreException(CCorePlugin.createStatus(Messages.getString("PDOMFile.toInternalProblem") + //$NON-NLS-1$
					location.getURI()));
		}
		setInternalLocation(locationString);
	}

	/**
	 * Directly changes this record's internal location string. The format of this string is unspecified
	 * in general and is determined by the associated IIndexLocationConverter.
	 * This method should not be called on PDOMFile objects that are referenced by the file index.
	 * @param internalLocation
	 * @throws CoreException
	 */
	public void setInternalLocation(String internalLocation) throws CoreException {
		Database db = fLinkage.getDB();
		long oldRecord = db.getRecPtr(record + LOCATION_REPRESENTATION);
		if (oldRecord != 0)
			db.getString(oldRecord).delete();
		db.putRecPtr(record + LOCATION_REPRESENTATION, db.newString(internalLocation).getRecord());
		location= null;
	}

	@Override
	public int getLinkageID() throws CoreException {
		Database db = fLinkage.getDB();
		return db.get3ByteUnsignedInt(record + LINKAGE_ID);
	}

	@Override
	public long getTimestamp() throws CoreException {
		Database db = fLinkage.getDB();
		return db.getLong(record + TIME_STAMP);
	}

	@Override
	public void setTimestamp(long timestamp) throws CoreException {
		Database db= fLinkage.getDB();
		db.putLong(record + TIME_STAMP, timestamp);
	}

	@Override
	public long getContentsHash() throws CoreException {
		Database db = fLinkage.getDB();
		return db.getLong(record + CONTENT_HASH);
	}

	@Override
	public void setContentsHash(long hash) throws CoreException {
		Database db= fLinkage.getDB();
		db.putLong(record + CONTENT_HASH, hash);
	}

	@Override
	public int getScannerConfigurationHashcode() throws CoreException {
		return 0;
	}

	@Override
	public int getEncodingHashcode() throws CoreException {
		Database db = fLinkage.getDB();
		return db.getInt(record + ENCODING_HASH);
	}

	@Override
	public void setEncodingHashcode(int hashcode) throws CoreException {
		Database db= fLinkage.getDB();
		db.putInt(record + ENCODING_HASH, hashcode);
	}

	@Override
	public boolean hasPragmaOnceSemantics() throws CoreException {
		return (fLinkage.getDB().getByte(record + FLAGS) & FLAG_PRAGMA_ONCE_SEMANTICS) != 0;
	}

	@Override
	public void setPragmaOnceSemantics(boolean value) throws CoreException {
		Database db = fLinkage.getDB();
		byte flags = db.getByte(record + FLAGS);
		if (value) {
			flags |= FLAG_PRAGMA_ONCE_SEMANTICS;
		} else {
			flags &= ~FLAG_PRAGMA_ONCE_SEMANTICS;
		}
		db.putByte(record + FLAGS, flags);
	}

	private PDOMName getFirstName() throws CoreException {
		long namerec = fLinkage.getDB().getRecPtr(record + FIRST_NAME);
		return namerec != 0 ? new PDOMName(fLinkage, namerec) : null;
	}

	private void setFirstName(PDOMName firstName) throws CoreException {
		long namerec = firstName != null ? firstName.getRecord() : 0;
		fLinkage.getDB().putRecPtr(record + FIRST_NAME, namerec);
	}

	private PDOMMacroReferenceName getFirstMacroReference() throws CoreException {
		long namerec = fLinkage.getDB().getRecPtr(record + FIRST_MACRO_REFERENCE);
		return namerec != 0 ? new PDOMMacroReferenceName(fLinkage, namerec) : null;
	}

	private void setFirstMacroReference(PDOMMacroReferenceName firstName) throws CoreException {
		long namerec = firstName != null ? firstName.getRecord() : 0;
		fLinkage.getDB().putRecPtr(record + FIRST_MACRO_REFERENCE, namerec);
	}

	public PDOMInclude getFirstInclude() throws CoreException {
		long increc = fLinkage.getDB().getRecPtr(record + FIRST_INCLUDE);
		return increc != 0 ? new PDOMInclude(fLinkage, increc) : null;
	}

	public void setFirstInclude(PDOMInclude include) throws CoreException {
		long rec = include != null ? include.getRecord() : 0;
		fLinkage.getDB().putRecPtr(record + FIRST_INCLUDE, rec);
	}

	public PDOMInclude getFirstIncludedBy() throws CoreException {
		long rec = fLinkage.getDB().getRecPtr(record + FIRST_INCLUDED_BY);
		return rec != 0 ? new PDOMInclude(fLinkage, rec) : null;
	}

	@Override
	public IIndexInclude getParsedInContext() throws CoreException {
		return getFirstIncludedBy();
	}

	public void setFirstIncludedBy(PDOMInclude includedBy) throws CoreException {
		long rec = includedBy != null ? includedBy.getRecord() : 0;
		fLinkage.getDB().putRecPtr(record + FIRST_INCLUDED_BY, rec);
	}

	public PDOMMacro getFirstMacro() throws CoreException {
		long rec = fLinkage.getDB().getRecPtr(record + FIRST_MACRO);
		return rec != 0 ? new PDOMMacro(fLinkage, rec) : null;
	}

	public void setFirstMacro(PDOMMacro macro) throws CoreException {
		long rec = macro != null ? macro.getRecord() : 0;
		fLinkage.getDB().putRecPtr(record + FIRST_MACRO, rec);
	}

	public void addMacros(IASTPreprocessorStatement[] macros) throws CoreException {
		assert getFirstMacro() == null;

		PDOMMacro lastMacro= null;
		final PDOMLinkage linkage = getLinkage();
		for (IASTPreprocessorStatement stmt : macros) {
			PDOMMacro pdomMacro= null;
			if (stmt instanceof IASTPreprocessorMacroDefinition) {
				IASTPreprocessorMacroDefinition macro= (IASTPreprocessorMacroDefinition) stmt;
				PDOMMacroContainer container= linkage.getMacroContainer(macro.getName().getSimpleID());
				pdomMacro = new PDOMMacro(fLinkage, container, macro, this);
			} else if (stmt instanceof IASTPreprocessorUndefStatement) {
				IASTPreprocessorUndefStatement undef= (IASTPreprocessorUndefStatement) stmt;
				PDOMMacroContainer container= linkage.getMacroContainer(undef.getMacroName().getSimpleID());
				pdomMacro = new PDOMMacro(fLinkage, container, undef, this);
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

	final PDOMLinkage getLinkage() {
		return fLinkage;
	}

	public void addNames(IASTName[][] names, YieldableIndexLock lock) throws CoreException, InterruptedException {
		assert getFirstName() == null;
		assert getFirstMacroReference() == null;
		final PDOMLinkage linkage= getLinkage();
		HashMap<IASTName, PDOMName> nameCache= new HashMap<IASTName, PDOMName>();
		PDOMName lastName= null;
		PDOMMacroReferenceName lastMacroName= null;
		for (IASTName[] name : names) {
			if (name[0] != null) {
				if (lock != null) {
					lock.yield();
				}
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

	private IIndexFragmentName createPDOMName(PDOMLinkage linkage, IASTName name, PDOMName caller) throws CoreException {
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
				final PDOMName result= new PDOMName(fLinkage, name, this, pdomBinding, caller);
				linkage.onCreateName(this, name, result);
				return result;
			}
		} catch (CoreException e) {
			final IStatus status = e.getStatus();
			if (status != null && status.getCode() == CCorePlugin.STATUS_PDOM_TOO_LARGE) {
				if (CCorePlugin.PLUGIN_ID.equals(status.getPlugin()))
					throw e;
			}
			CCorePlugin.log(e);
		}
		return null;
	}

	private IIndexFragmentName createPDOMMacroReferenceName(PDOMLinkage linkage, IASTName name) throws CoreException {
		PDOMMacroContainer cont= linkage.getMacroContainer(name.getSimpleID());
		return new PDOMMacroReferenceName(fLinkage, name, this, cont);
	}

	public void clear() throws CoreException {
		ICPPUsingDirective[] directives= getUsingDirectives();
		for (ICPPUsingDirective ud : directives) {
			if (ud instanceof IPDOMNode) {
				((IPDOMNode) ud).delete(null);
			}
		}
		setLastUsingDirective(0);

		// Remove the includes
		PDOMInclude include = getFirstInclude();
		while (include != null) {
			PDOMInclude nextInclude = include.getNextInIncludes();
//			if (contextsRemoved != null && include.getPrevInIncludedByRecord() == 0) {
//				contextsRemoved.add(include.getIncludesLocation());
//			}
			include.delete();
			include = nextInclude;
		}
		setFirstInclude(null);

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

	/**
	 * Deletes this file from PDOM. Only uncommitted files can be safely deleted.
	 *
	 * @throws CoreException
	 */
	public void delete() throws CoreException {
		Database db = fLinkage.getDB();
		long locRecord = db.getRecPtr(record + LOCATION_REPRESENTATION);
		if (locRecord != 0)
			db.getString(locRecord).delete();
		locRecord = db.getRecPtr(record + SIGNIFICANT_MACROS);
		if (locRecord != 0)
			db.getString(locRecord).delete();

		db.free(record);
	}

	public void addIncludesTo(IncludeInformation[] includeInfos) throws CoreException {
		assert getFirstInclude() == null;

		PDOMInclude lastInclude= null;
		for (final IncludeInformation info : includeInfos) {
			final PDOMFile targetFile= (PDOMFile) info.fTargetFile;

			PDOMInclude pdomInclude = new PDOMInclude(fLinkage, info.fStatement, this, targetFile);
			assert targetFile == null || targetFile.getIndexFragment() instanceof IWritableIndexFragment;
			if (targetFile != null) {
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

	@Override
	public IIndexInclude[] getIncludes() throws CoreException {
		List<PDOMInclude> result= new ArrayList<PDOMInclude>();
		PDOMInclude include = getFirstInclude();
		while (include != null) {
			result.add(include);
			include = include.getNextInIncludes();
		}
		return result.toArray(new IIndexInclude[result.size()]);
	}

	@Override
	public IIndexMacro[] getMacros() throws CoreException {
		List<PDOMMacro> result= new ArrayList<PDOMMacro>();
		PDOMMacro macro = getFirstMacro();
		while (macro != null) {
			result.add(macro);
			macro = macro.getNextMacro();
		}
		return result.toArray(new IIndexMacro[result.size()]);
	}

	@Override
	public IIndexFragment getIndexFragment() {
		return fLinkage.getPDOM();
	}

	@Override
	public IIndexName[] findNames(int offset, int length) throws CoreException {
		ArrayList<IIndexName> result= new ArrayList<IIndexName>();
		for (PDOMName name= getFirstName(); name != null; name= name.getNextInFile()) {
			int nameOffset=  name.getNodeOffset();
			if (nameOffset >= offset) {
				if (nameOffset + name.getNodeLength() <= offset + length) {
					result.add(name);
				} else if (name.isReference()) {
					// Names are ordered, but callers are inserted before
					// their references.
					break;
				}
			}
		}
		for (PDOMMacro macro= getFirstMacro(); macro != null; macro= macro.getNextMacro()) {
			int nameOffset=  macro.getNodeOffset();
			if (nameOffset >= offset) {
				if (nameOffset + macro.getNodeLength() <= offset + length) {
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
				if (nameOffset + name.getNodeLength() <= offset + length) {
					result.add(name);
				} else {
					break;
				}
			}
		}
		return result.toArray(new IIndexName[result.size()]);
	}

	public static IIndexFragmentFile[] findFiles(PDOMLinkage linkage, BTree btree, IIndexFileLocation location,
			IIndexLocationConverter strategy) throws CoreException {
		String internalRepresentation= strategy.toInternalFormat(location);
		if (internalRepresentation != null) {
			Finder finder = new Finder(linkage.getDB(), internalRepresentation, linkage.getLinkageID(), null);
			btree.accept(finder);
			long[] records= finder.getRecords();
			IIndexFragmentFile[] result= new IIndexFragmentFile[records.length];
			for (int i = 0; i < result.length; i++) {
				result[i]= new PDOMFile(linkage, records[i]);
			}
			return result;
		}
		return IIndexFragmentFile.EMPTY_ARRAY;
	}

	/**
	 * When a header file is stored in the index in multiple variants for different sets of macro
	 * definitions this method will return an arbitrary one of these variants.
	 *
	 * @deprecated Use
	 *     {@link #findFile(PDOMLinkage, BTree, IIndexFileLocation, IIndexLocationConverter, ISignificantMacros)}
	 *     or {@link #findFiles(PDOMLinkage, BTree, IIndexFileLocation, IIndexLocationConverter)}
	 */
	@Deprecated
	public static PDOMFile findFile(PDOMLinkage linkage, BTree btree, IIndexFileLocation location,
			IIndexLocationConverter strategy) throws CoreException {
		return findFile(linkage, btree, location, strategy, null);
	}

	/**
	 * Finds the file in index.
	 *
	 * @param linkage The linkage of the file.
	 * @param btree The file index.
	 * @param location The location of the file.
	 * @param strategy The index location converter.
	 * @param macroDictionary The names and definitions of the macros used to disambiguate between
	 *     variants of the file contents corresponding to different inclusion points.
	 * @return The found file, or <code>null</code> if the matching file was not found.
	 */
	public static PDOMFile findFile(PDOMLinkage linkage, BTree btree, IIndexFileLocation location,
			IIndexLocationConverter strategy, ISignificantMacros macroDictionary) throws CoreException {
		String internalRepresentation= strategy.toInternalFormat(location);
		if (internalRepresentation != null) {
			Finder finder = new Finder(linkage.getDB(), internalRepresentation, linkage.getLinkageID(),
					macroDictionary);
			btree.accept(finder);
			long record= finder.getRecord();
			if (record != 0) {
				return new PDOMFile(linkage, record);
			}
		}
		return null;
	}

	public static IIndexFragmentFile[] findFiles(PDOM pdom, BTree btree, IIndexFileLocation location,
			IIndexLocationConverter strategy) throws CoreException {
		String internalRepresentation= strategy.toInternalFormat(location);
		if (internalRepresentation != null) {
			Finder finder = new Finder(pdom.getDB(), internalRepresentation, -1, null);
			btree.accept(finder);
			long[] records= finder.getRecords();
			PDOMFile[] result= new PDOMFile[records.length];
			for (int i = 0; i < result.length; i++) {
				result[i] = recreateFile(pdom, records[i]);
			}
			return result;
		}
		return new IIndexFragmentFile[0];
	}

	public static PDOMFile recreateFile(PDOM pdom, final long record) throws CoreException {
		final Database db= pdom.getDB();
		final int linkageID= db.get3ByteUnsignedInt(record + LINKAGE_ID);
		PDOMLinkage linkage= pdom.getLinkage(linkageID);
		if (linkage == null)
			throw new CoreException(createStatus("Invalid linkage ID in database")); //$NON-NLS-1$
		return new PDOMFile(linkage, record);
	}

	private static class Finder implements IBTreeVisitor {
		private static final long[] EMPTY = {};
		private final Database db;
		private final String rawKey;
		private long record;
		private long[] records;
		private final int linkageID;
		private char[] rawSignificantMacros;

		/**
		 * Searches for a file with the given linkage id.
		 */
		public Finder(Database db, String internalRepresentation, int linkageID, ISignificantMacros sigMacros) {
			this.db = db;
			this.rawKey = internalRepresentation;
			this.linkageID= linkageID;
			this.rawSignificantMacros = sigMacros == null ? null : sigMacros.encode();
			assert linkageID >= 0 || rawSignificantMacros == null;
		}

		public long[] getRecords() {
			if (records == null) {
				if (record == 0) {
					return EMPTY;
				}
				return new long[] { record };
			}
			return records;
		}

		@Override
		public int compare(long record) throws CoreException {
			IString name = db.getString(db.getRecPtr(record + PDOMFile.LOCATION_REPRESENTATION));
			int cmp= name.compare(rawKey, true);
			if (cmp == 0 && linkageID >= 0) {
				cmp= db.get3ByteUnsignedInt(record + PDOMFile.LINKAGE_ID) - linkageID;
				if (cmp == 0 && rawSignificantMacros != null) {
					IString significantMacrosStr = getString(record + SIGNIFICANT_MACROS);
					if (significantMacrosStr != null) {
						cmp = significantMacrosStr.compare(rawSignificantMacros, true);
					} else {
						cmp = rawSignificantMacros.length > 0 ? -1 : 0;
					}
				}
			}
			return cmp;
		}

		private IString getString(long offset) throws CoreException {
			long rec = db.getRecPtr(offset);
			return rec != 0 ? db.getString(rec) : null;
		}

		@Override
		public boolean visit(long record) throws CoreException {
			if (rawSignificantMacros != null) {
				this.record = record;
				return false; 
				// Stop searching.
			}
			
			if (this.record == 0) {
				this.record= record;
			} else if (this.records == null) {
				this.records= new long[] { this.record, record };
			} else {
				long[] cpy= new long[this.records.length + 1];
				System.arraycopy(this.records, 0, cpy, 0, this.records.length);
				cpy[cpy.length - 1]= record;
				this.records= cpy;
			}
			// Continue search.
			return true;
		}

		public long getRecord() {
			return record;
		}
	}

	@Override
	public IIndexFileLocation getLocation() throws CoreException {
		if (location == null) {
			Database db = fLinkage.getDB();
			String raw = db.getString(db.getRecPtr(record + LOCATION_REPRESENTATION)).getString();
			location= fLinkage.getPDOM().getLocationConverter().fromInternalFormat(raw);
			if (location == null) {
				URI uri;
				try {
					int idx= raw.lastIndexOf('>');
					uri= new URI("file", null, raw.substring(idx + 1), null); //$NON-NLS-1$
				} catch (URISyntaxException e) {
					uri= URI.create("file:/unknown-location"); //$NON-NLS-1$
				}
				location= new IndexFileLocation(uri, null);
			}
		}
		return location;
	}

	
	@Override
	public ISignificantMacros getSignificantMacros() throws CoreException {
		if (sigMacros == null) {
			Database db= fLinkage.getDB();
			final IString encoded = db.getString(db.getRecPtr(record + SIGNIFICANT_MACROS));
			sigMacros= encoded == null ? ISignificantMacros.NONE : new SignificantMacros(encoded.getChars());
		}
		return sigMacros;
	}

	@Override
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

	public long getLastUsingDirectiveRec() throws CoreException {
		return fLinkage.getDB().getRecPtr(record + LAST_USING_DIRECTIVE);
	}

	public void setLastUsingDirective(long rec) throws CoreException {
		fLinkage.getDB().putRecPtr(record + LAST_USING_DIRECTIVE, rec);
	}

	@Override
	public ICPPUsingDirective[] getUsingDirectives() throws CoreException {
		return fLinkage.getUsingDirectives(this);
	}

	// Required because we cannot reference CCorePlugin in order for StandaloneIndexer to work
	private static IStatus createStatus(String msg) {
		return new Status(IStatus.ERROR, "org.eclipse.cdt.core", msg, null); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		IIndexFileLocation loc = null;
		try {
			loc = getLocation();
		} catch (CoreException e) {
		}
		return loc != null ? loc.toString() : super.toString();
	}
}
