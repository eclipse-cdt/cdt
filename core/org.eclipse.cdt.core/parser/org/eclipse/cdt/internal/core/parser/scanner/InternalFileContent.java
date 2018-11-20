/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.ISignificantMacros;

/**
 * Instructs the preprocessor on how to handle a file-inclusion.
 */
public class InternalFileContent extends FileContent {
	public enum InclusionKind {
		/**
		 * Instruct the preprocessor to skip this inclusion.
		 */
		SKIP_FILE,
		/**
		 * The file and its dependents are indexed, required information is read
		 * from there.
		 */
		FOUND_IN_INDEX,
		/**
		 * The file has to be scanned, source is provided.
		 */
		USE_SOURCE
	}

	public static class FileVersion {
		public final String fPath;
		public final ISignificantMacros fSigMacros;

		public FileVersion(String path, ISignificantMacros sig) {
			fPath = path;
			fSigMacros = sig;
		}
	}

	private final InclusionKind fKind;
	private final AbstractCharArray fSource;
	private final List<IIndexMacro> fMacroDefinitions;
	private final List<ICPPUsingDirective> fUsingDirectives;
	private final String fFileLocation;
	private final List<FileVersion> fNonPragmaOnceFiles;
	private boolean fHeuristic;
	private boolean fIsSource;
	private ITranslationUnit fTranslationUnit;
	private List<IIndexFile> fFiles;
	private IncludeSearchPathElement fFoundOnPath;
	private final long fTimestamp;
	private final long fFileSize;
	private final long fReadTime;

	/**
	 * For skipping include files.
	 * @param fileLocation the location of the file.
	 * @param kind must be {@link InclusionKind#SKIP_FILE}.
	 * @throws IllegalArgumentException if fileLocation is <code>null</code> or the kind value is illegal for
	 * this constructor.
	 */
	public InternalFileContent(String fileLocation, InclusionKind kind) throws IllegalArgumentException {
		if (fileLocation == null || kind != InclusionKind.SKIP_FILE) {
			throw new IllegalArgumentException();
		}
		fKind = kind;
		fFileLocation = fileLocation;
		fMacroDefinitions = null;
		fUsingDirectives = null;
		fSource = null;
		fNonPragmaOnceFiles = null;
		fTimestamp = NULL_TIMESTAMP;
		fFileSize = NULL_FILE_SIZE;
		fReadTime = 0;
	}

	/**
	 * For reading include files from disk.
	 * @throws IllegalArgumentException in case the codeReader or its location is <code>null</code>.
	 */
	public InternalFileContent(String filePath, AbstractCharArray content, long timestamp, long fileSize,
			long fileReadTime) throws IllegalArgumentException {
		if (content == null) {
			throw new IllegalArgumentException();
		}
		fKind = InclusionKind.USE_SOURCE;
		fFileLocation = filePath;
		fSource = content;
		fMacroDefinitions = null;
		fUsingDirectives = null;
		fNonPragmaOnceFiles = null;
		if (fFileLocation == null) {
			throw new IllegalArgumentException();
		}
		fTimestamp = timestamp;
		fFileSize = fileSize;
		fReadTime = fileReadTime;
	}

	/**
	 * For reading in-memory buffers.
	 * @throws IllegalArgumentException in case the codeReader or its location is <code>null</code>.
	 */
	public InternalFileContent(String filePath, CharArray content) throws IllegalArgumentException {
		if (content == null) {
			throw new IllegalArgumentException();
		}
		fKind = InclusionKind.USE_SOURCE;
		fFileLocation = filePath;
		fSource = content;
		fMacroDefinitions = null;
		fUsingDirectives = null;
		fNonPragmaOnceFiles = null;
		if (fFileLocation == null) {
			throw new IllegalArgumentException();
		}
		fTimestamp = NULL_TIMESTAMP;
		fFileSize = NULL_FILE_SIZE;
		fReadTime = 0;
	}

	/**
	 * For using information about an include file from the index.
	 * @param fileLocation the location of the file
	 * @param macroDefinitions a list of macro definitions
	 * @param files
	 * @throws IllegalArgumentException in case the fileLocation or the macroDefinitions are <code>null</code>.
	 */
	public InternalFileContent(String fileLocation, List<IIndexMacro> macroDefinitions,
			List<ICPPUsingDirective> usingDirectives, List<IIndexFile> files, List<FileVersion> nonPragmaOnceVersions) {
		fKind = InclusionKind.FOUND_IN_INDEX;
		fFileLocation = fileLocation;
		fSource = null;
		fUsingDirectives = usingDirectives;
		fMacroDefinitions = macroDefinitions;
		fFiles = files;
		fNonPragmaOnceFiles = nonPragmaOnceVersions;
		fTimestamp = NULL_TIMESTAMP;
		fFileSize = NULL_FILE_SIZE;
		fReadTime = 0;
	}

	/**
	 * @return the kind
	 */
	public InclusionKind getKind() {
		return fKind;
	}

	/**
	 * Returns the location of the file to be included.
	 */
	@Override
	public String getFileLocation() {
		return fFileLocation;
	}

	@Override
	public long getTimestamp() {
		return fTimestamp;
	}

	@Override
	public long getReadTime() {
		return fReadTime;
	}

	@Override
	public long getFileSize() {
		return fFileSize;
	}

	@Override
	public long getContentsHash() {
		return fSource != null ? fSource.getContentsHash() : 0;
	}

	@Override
	public boolean hasError() {
		return fSource != null && fSource.hasError();
	}

	/**
	 * Valid with {@link InclusionKind#USE_SOURCE}.
	 * @return the codeReader or <code>null</code> if kind is different to {@link InclusionKind#USE_SOURCE}.
	 */
	public AbstractCharArray getSource() {
		return fSource;
	}

	/**
	 * Valid with {@link InclusionKind#FOUND_IN_INDEX}.
	 * @return the macroDefinitions or <code>null</code> if kind is different to {@link InclusionKind#FOUND_IN_INDEX}.
	 */
	public List<IIndexMacro> getMacroDefinitions() {
		return fMacroDefinitions;
	}

	/**
	 * Valid with {@link InclusionKind#FOUND_IN_INDEX}.
	 * @return the usingDirectives or <code>null</code> if kind is different to {@link InclusionKind#FOUND_IN_INDEX}.
	 */
	public List<ICPPUsingDirective> getUsingDirectives() {
		return fUsingDirectives;
	}

	/**
	 * Valid with {@link InclusionKind#FOUND_IN_INDEX}.
	 * @return the files included or <code>null</code> if kind is different to {@link InclusionKind#FOUND_IN_INDEX}.
	 */
	public List<IIndexFile> getFilesIncluded() {
		return fFiles;
	}

	public List<FileVersion> getNonPragmaOnceVersions() {
		return fNonPragmaOnceFiles;
	}

	/**
	 * Returns whether this inclusion was found by a heuristics.
	 */
	public boolean isFoundByHeuristics() {
		return fHeuristic;
	}

	public void setFoundByHeuristics(boolean val) {
		fHeuristic = val;
	}

	public boolean isSource() {
		return fIsSource;
	}

	public void setIsSource(boolean isSource) {
		fIsSource = isSource;
	}

	public ITranslationUnit getTranslationUnit() {
		return fTranslationUnit;
	}

	public void setTranslationUnit(ITranslationUnit tu) {
		fTranslationUnit = tu;
	}

	public IncludeSearchPathElement getFoundOnPath() {
		return fFoundOnPath;
	}

	public void setFoundOnPath(IncludeSearchPathElement isp) {
		fFoundOnPath = isp;
	}

	/**
	 * This method is slow. Use only for debugging.
	 */
	@Override
	public String toString() {
		return getSource().toString();
	}
}
