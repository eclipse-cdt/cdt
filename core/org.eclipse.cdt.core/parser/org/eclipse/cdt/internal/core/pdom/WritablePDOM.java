/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.internal.core.index.FileContentKey;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndex.IncludeInformation;
import org.eclipse.cdt.internal.core.index.IWritableIndexFragment;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
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
	private boolean fClearedBecauseOfVersionMismatch = false;
	private boolean fCreatedFromScratch = false;
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
		fPathResolver = resolver;
	}

	@Override
	public IIndexFragmentFile addFile(int linkageID, IIndexFileLocation location, ISignificantMacros sigMacros)
			throws CoreException {
		if (uncommittedKey != null && uncommittedKey.equals(new FileContentKey(linkageID, location, sigMacros)))
			return uncommittedFile;

		return super.addFile(linkageID, location, sigMacros);
	}

	@Override
	public IIndexFragmentFile addUncommittedFile(int linkageID, IIndexFileLocation location,
			ISignificantMacros significantMacros) throws CoreException {
		uncommittedKey = new FileContentKey(linkageID, location, significantMacros);
		fileBeingUpdated = getFile(linkageID, location, significantMacros);
		PDOMLinkage linkage = createLinkage(linkageID);
		uncommittedFile = new PDOMFile(linkage, location, linkageID, significantMacros);
		return uncommittedFile;
	}

	@Override
	public IIndexFragmentFile commitUncommittedFile() throws CoreException {
		if (uncommittedFile == null)
			return null;

		int defectiveStateChange = uncommittedFile.getTimestamp() == 0 ? 1 : 0;
		int unresolvedIncludeStateChange = uncommittedFile.hasUnresolvedInclude() ? 1 : 0;

		PDOMFile file;
		if (fileBeingUpdated == null) {
			// New file, insert it into the index.
			file = uncommittedFile;
			getFileIndex().insert(file.getRecord());
		} else {
			// Existing file.
			if (fileBeingUpdated.getTimestamp() == 0)
				defectiveStateChange -= 1;
			if (fileBeingUpdated.hasUnresolvedInclude())
				unresolvedIncludeStateChange -= 1;
			fileBeingUpdated.replaceContentsFrom(uncommittedFile);
			file = fileBeingUpdated;
			fileBeingUpdated = null;
		}
		if (defectiveStateChange > 0) {
			getIndexOfDefectiveFiles().insert(file.getRecord());
		} else if (defectiveStateChange < 0) {
			getIndexOfDefectiveFiles().delete(file.getRecord());
		}
		if (unresolvedIncludeStateChange > 0) {
			getIndexOfFilesWithUnresolvedIncludes().insert(file.getRecord());
		} else if (unresolvedIncludeStateChange < 0) {
			getIndexOfFilesWithUnresolvedIncludes().delete(file.getRecord());
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
		final ASTFilePathResolver origResolver = fPathResolver;
		fPathResolver = pathResolver;
		try {
			pdomFile.addNames(names, lock);
		} finally {
			fPathResolver = origResolver;
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
		IIndexInclude include = pdomFile.getParsedInContext();
		if (include != null) {
			PDOMFile includedBy = (PDOMFile) include.getIncludedBy();
			if (includedBy.getTimestamp() > 0)
				getIndexOfFilesWithUnresolvedIncludes().insert(includedBy.getRecord());
		}

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
		final List<PDOMFile> pdomfiles = new ArrayList<>();
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
		final List<PDOMFile> notConverted = new ArrayList<>();
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

	/**
	 * Returns the best file for the given location, linkage, and translation unit.
	 * May return {@code null}, if no such file exists.
	 *
	 * The "best" file (variant) is the one with the most content, as measured
	 * by the total number of macros defined in the file. The rationale is that
	 * often one of the variants will contain most of the code and the others
	 * just small pieces, and we are usually interested in the one with most of
	 * the code. As a tiebreaker, a variant that was parsed in the context of a
	 * source file is preferred, since a header parsed outside of the context of
	 * a code file may not represent code that a compiler actually sees.
	 *
	 * @param linkageID the id of the linkage in which the file has been parsed.
	 * @param location the IIndexFileLocation representing the location of the file
	 * @param tu the translation unit from which 'location' originates
	 * @return the best file for the location, or {@code null} if the file is not
	 *     present in the index
	 * @throws CoreException
	 */
	private PDOMFile getBestFile(int linkageID, IIndexFileLocation location, ITranslationUnit tu) throws CoreException {
		IIndexFile[] files = getFiles(linkageID, location);
		IIndexFile best = null;
		int bestScore = -1;
		for (IIndexFile file : files) {
			int score = file.getMacros().length * 2;
			if (tu != null && TranslationUnit.isSourceFile(TranslationUnit.getParsedInContext(file),
					tu.getCProject().getProject()))
				score++;
			if (score > bestScore) {
				bestScore = score;
				best = file;
			}
		}
		return (PDOMFile) best;
	}

	public PDOMFile getFileForASTNode(int linkageID, IASTNode node) throws CoreException {
		if (fPathResolver != null && node != null) {
			IASTFileLocation loc = node.getFileLocation();
			if (loc != null) {
				IASTPreprocessorIncludeStatement owner = loc.getContextInclusionStatement();
				ISignificantMacros sigMacros = owner != null ? owner.getSignificantMacros() : ISignificantMacros.NONE;
				if (sigMacros != null) {
					IIndexFileLocation location = fPathResolver.resolveASTPath(loc.getFileName());
					if (uncommittedKey != null
							&& uncommittedKey.equals(new FileContentKey(linkageID, location, sigMacros)))
						return fileBeingUpdated != null ? fileBeingUpdated : uncommittedFile;
					return getBestFile(linkageID, location, node.getTranslationUnit().getOriginatingTranslationUnit());
				}
			}
		}
		return null;
	}

	@Override
	public boolean hasLastingDefinition(PDOMBinding binding) throws CoreException {
		if (fileBeingUpdated == null) {
			return binding.hasDefinition();
		}
		// Definitions in fileBeingUpdated will be removed when the file is committed, so look for
		// a definition elsewhere.
		for (PDOMName name = binding.getFirstDefinition(); name != null; name = name.getNextInBinding()) {
			if (!fileBeingUpdated.getPDOM().equals(name.getPDOM())
					|| fileBeingUpdated.getRecord() != name.getFileRecord()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean isCommitted(PDOMName name) throws CoreException {
		return uncommittedFile == null || !uncommittedFile.getPDOM().equals(name.getPDOM())
				|| uncommittedFile.getRecord() != name.getFileRecord();
	}

	@Override
	protected boolean isCommitted(PDOMMacro name) throws CoreException {
		return uncommittedFile == null || !uncommittedFile.getPDOM().equals(name.getPDOM())
				|| uncommittedFile.getRecord() != name.getFileRecord();
	}

	@Override
	protected boolean isCommitted(PDOMMacroReferenceName name) throws CoreException {
		return uncommittedFile == null || !uncommittedFile.getPDOM().equals(name.getPDOM())
				|| uncommittedFile.getRecord() != name.getFileRecord();
	}

	@Override
	public long getDatabaseSizeBytes() {
		return getDB().getSizeBytes();
	}
}
