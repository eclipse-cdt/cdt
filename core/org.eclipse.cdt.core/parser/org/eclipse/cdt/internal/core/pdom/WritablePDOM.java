/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.internal.core.index.FileContentKey;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndex.IncludeInformation;
import org.eclipse.cdt.internal.core.index.IWritableIndexFragment;
import org.eclipse.cdt.internal.core.pdom.db.ChunkCache;
import org.eclipse.cdt.internal.core.pdom.db.DBProperties;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMacro;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMacroReferenceName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.core.runtime.CoreException;

public class WritablePDOM extends PDOM implements IWritableIndexFragment {	
	private boolean fClearedBecauseOfVersionMismatch= false;
	private boolean fCreatedFromScratch= false;
	private ASTFilePathResolver fPathResolver;
	private PDOMFile fileBeingUpdated;
	private PDOMFile uncommittedFile;
	private FileContentKey uncommittedKey;

	public WritablePDOM(File dbPath, IIndexLocationConverter locationConverter,
			Map<String, IPDOMLinkageFactory> linkageFactoryMappings) throws CoreException {
		this(dbPath, locationConverter, ChunkCache.getSharedInstance(), linkageFactoryMappings);
	}
	
	public WritablePDOM(File dbPath, IIndexLocationConverter locationConverter, ChunkCache cache,
			Map<String, IPDOMLinkageFactory> linkageFactoryMappings) throws CoreException {
		super(dbPath, locationConverter, cache, linkageFactoryMappings);
	}
	
	public void setASTFilePathResolver(ASTFilePathResolver resolver) {
		fPathResolver= resolver;
	}

	@Override
	public IIndexFragmentFile addFile(int linkageID, IIndexFileLocation location, ISignificantMacros sigMacros) throws CoreException {
		if (uncommittedKey != null && uncommittedKey.equals(new FileContentKey(linkageID, location, sigMacros)))
			return uncommittedFile;
		
		return super.addFile(linkageID, location, sigMacros);
	}

	@Override
	public IIndexFragmentFile addUncommittedFile(int linkageID, IIndexFileLocation location,
			ISignificantMacros significantMacros) throws CoreException {
		uncommittedKey = new FileContentKey(linkageID, location, significantMacros);
		fileBeingUpdated = getFile(linkageID, location, significantMacros);
		PDOMLinkage linkage= createLinkage(linkageID);
		uncommittedFile = new PDOMFile(linkage, location, linkageID, significantMacros);
		return uncommittedFile;
	}

	@Override
	public IIndexFragmentFile commitUncommittedFile() throws CoreException {
		if (uncommittedFile == null)
			return null;
		PDOMFile file;
		if (fileBeingUpdated == null) {
			// New file, insert it into the index.
			file = uncommittedFile;
			getFileIndex().insert(file.getRecord()); 
		} else {
			// Existing file.
			fileBeingUpdated.replaceContentsFrom(uncommittedFile);
			file = fileBeingUpdated;
			fileBeingUpdated = null;
		}
		fEvent.fFilesWritten.add(uncommittedKey.getLocation());
		uncommittedFile = null;
		uncommittedKey = null;
		return file;
	}

	@Override
	public void clearUncommittedFile() throws CoreException {
		if (uncommittedFile != null) {
			try {
				uncommittedFile.clear();
				uncommittedFile.delete();
			} finally {
				uncommittedFile = null;
				uncommittedKey = null;
				fileBeingUpdated = null;
			}
		}
	}

	@Override
	public void addFileContent(IIndexFragmentFile sourceFile, IncludeInformation[] includes, 
			IASTPreprocessorStatement[] macros, IASTName[][] names, ASTFilePathResolver pathResolver,
			YieldableIndexLock lock) throws CoreException, InterruptedException {
		assert sourceFile.getIndexFragment() == this;
		
		PDOMFile pdomFile = (PDOMFile) sourceFile;
		pdomFile.addMacros(macros);
		final ASTFilePathResolver origResolver= fPathResolver;
		fPathResolver= pathResolver;
		try {
			pdomFile.addNames(names, lock);
		} finally {
			fPathResolver= origResolver;
		}
		// Includes expose the temporary file in the index, we must not yield the lock beyond this point.
		pdomFile.addIncludesTo(includes);
		
		final IIndexFileLocation location = pdomFile.getLocation();
		if (location != null) {
			fEvent.fClearedFiles.remove(location);
			fEvent.fFilesWritten.add(location);
		}
	}

	@Override
	public void clearFile(IIndexFragmentFile file) throws CoreException {
		assert file.getIndexFragment() == this;
		IIndexFileLocation location = file.getLocation();
		PDOMFile pdomFile = (PDOMFile) file;
		pdomFile.clear();	

		fEvent.fClearedFiles.add(location);
	}
	
	@Override
	public void clear() throws CoreException {
		super.clear();
	}
	
	@Override
	public void flush() throws CoreException {
		super.flush();
	}
		
	@Override
	public void setProperty(String propertyName, String value) throws CoreException {
		if (IIndexFragment.PROPERTY_FRAGMENT_FORMAT_ID.equals(propertyName) 
				|| IIndexFragment.PROPERTY_FRAGMENT_FORMAT_VERSION.equals(propertyName)) {
			throw new IllegalArgumentException("Property " + value + " may not be written to"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		new DBProperties(db, PROPERTIES).setProperty(propertyName, value);
	}
	
	/**
	 * Uses the specified location converter to update each internal representation of a file
	 * location. The file index is rebuilt with the new representations. Individual PDOMFile records
	 * are unmoved so as to maintain referential integrity with other PDOM records.
	 * 
	 * <b>A write-lock must be obtained before calling this method</b>
	 * 
	 * @param newConverter the converter to use to update internal file representations
	 * @throws CoreException
	 */
	public void rewriteLocations(final IIndexLocationConverter newConverter) throws CoreException {
		final List<PDOMFile> pdomfiles = new ArrayList<PDOMFile>();
		getFileIndex().accept(new IBTreeVisitor() {
			@Override
			public int compare(long record) throws CoreException {
				return 0;
			}
			@Override
			public boolean visit(long record) throws CoreException {
				PDOMFile file = PDOMFile.recreateFile(WritablePDOM.this, record);
				pdomfiles.add(file);
				return true;
			}
		});

		clearFileIndex();
		final List<PDOMFile> notConverted = new ArrayList<PDOMFile>();
		for (PDOMFile file : pdomfiles) {
			String internalFormat = newConverter.toInternalFormat(file.getLocation());
			if (internalFormat != null) {
				file.setInternalLocation(internalFormat);
				getFileIndex().insert(file.getRecord());
			} else {
				notConverted.add(file);
			}
		}

		// remove content where converter returns null
		for (PDOMFile file : notConverted) {
			file.convertIncludersToUnresolved();
			file.clear();
		}
	}

	boolean isClearedBecauseOfVersionMismatch() {
		return fClearedBecauseOfVersionMismatch;
	}

	void setClearedBecauseOfVersionMismatch(boolean clearedBecauseOfVersionMismatch) {
		fClearedBecauseOfVersionMismatch = clearedBecauseOfVersionMismatch;
	}

	boolean isCreatedFromScratch() {
		return fCreatedFromScratch;
	}

	void setCreatedFromScratch(boolean createdFromScratch) {
		fCreatedFromScratch = createdFromScratch;
	}
	
	@Override
	protected final boolean isPermanentlyReadOnly() {
		return false;
	}

	public PDOMFile getFileForASTNode(int linkageID, IASTNode node) throws CoreException {
		if (fPathResolver != null && node != null) {
			IASTFileLocation loc= node.getFileLocation();
			if (loc != null) {
				ISignificantMacros sigMacros= getSignificantMacros(node, loc);
				if (sigMacros != null) {
					IIndexFileLocation location = fPathResolver.resolveASTPath(loc.getFileName());
					if (uncommittedKey != null && uncommittedKey.equals(new FileContentKey(linkageID, location, sigMacros)))
						return fileBeingUpdated != null ? fileBeingUpdated : uncommittedFile;
					return getFile(linkageID, location, sigMacros);
				}
			}
		}
		return null;
	}

	private ISignificantMacros getSignificantMacros(IASTNode node, IASTFileLocation loc) throws CoreException {
		IASTPreprocessorIncludeStatement owner= loc.getContextInclusionStatement();
		if (owner != null) 
			return owner.getSignificantMacros();

		IASTTranslationUnit tu = node.getTranslationUnit();
		if (tu != null)
			return tu.getSignificantMacros();
		
		return null;
	}

	@Override
	public boolean hasLastingDefinition(PDOMBinding binding) throws CoreException {
		if (fileBeingUpdated == null) {
			return binding.hasDefinition();
		}
		// Definitions in fileBeingUpdated will soon go away, so look for a definition elsewhere.
		for (PDOMName name = binding.getFirstDefinition(); name != null; name = name.getNextInBinding()) {
			if (!fileBeingUpdated.getPDOM().equals(name.getPDOM()) ||
					fileBeingUpdated.getRecord() != name.getFileRecord()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean isCommitted(PDOMName name) throws CoreException {
		return uncommittedFile == null || !uncommittedFile.getPDOM().equals(name.getPDOM()) ||
				uncommittedFile.getRecord() != name.getFileRecord();
	}

	@Override
	protected boolean isCommitted(PDOMMacro name) throws CoreException {
		return uncommittedFile == null || !uncommittedFile.getPDOM().equals(name.getPDOM()) ||
				uncommittedFile.getRecord() != name.getFileRecord();
	}

	@Override
	protected boolean isCommitted(PDOMMacroReferenceName name) throws CoreException {
		return uncommittedFile == null || !uncommittedFile.getPDOM().equals(name.getPDOM()) ||
				uncommittedFile.getRecord() != name.getFileRecord();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.IWritableIndexFragment#getDatabaseSizeBytes()
	 */
	public long getDatabaseSizeBytes() {
		return getDB().getSizeBytes();
	}
}
