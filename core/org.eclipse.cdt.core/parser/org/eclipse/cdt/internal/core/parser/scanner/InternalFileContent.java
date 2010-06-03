/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.parser.FileContent;

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

	private final InclusionKind fKind;
	private final AbstractCharArray fSource;
	private final List<IIndexMacro> fMacroDefinitions;
	private final List<ICPPUsingDirective> fUsingDirectives;
	private final String fFileLocation;
	private boolean fHeuristic;
	private boolean fIsSource= false;
	private List<IIndexFile> fFiles;
	private IncludeSearchPathElement fFoundOnPath;
	
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
		fKind= kind;
		fFileLocation= fileLocation;
		fMacroDefinitions= null;
		fUsingDirectives= null;
		fSource= null;
	}

	/**
	 * For reading include files from disk.
	 * @throws IllegalArgumentException in case the codeReader or its location is <code>null</code>.
	 */
	public InternalFileContent(String filePath, AbstractCharArray content) throws IllegalArgumentException {
		if (content == null) {
			throw new IllegalArgumentException();
		}
		fKind= InclusionKind.USE_SOURCE;
		fFileLocation= filePath;
		fSource= content;
		fMacroDefinitions= null;
		fUsingDirectives= null;
		if (fFileLocation == null) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * For using information about an include file from the index.
	 * @param fileLocation the location of the file
	 * @param macroDefinitions a list of macro definitions
	 * @param files 
	 * @throws IllegalArgumentException in case the fileLocation or the macroDefinitions are <code>null</code>.
	 */
	public InternalFileContent(String fileLocation, List<IIndexMacro> macroDefinitions, List<ICPPUsingDirective> usingDirectives,
			List<IIndexFile> files) {
		fKind= InclusionKind.FOUND_IN_INDEX;
		fFileLocation= fileLocation;
		fSource= null;
		fUsingDirectives= usingDirectives;
		fMacroDefinitions= macroDefinitions;
		fFiles= files;
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

	/**
	 * Returns a 64-bit hash value of the file contents.
	 */
	@Override
	public long getContentsHash() {
		return fSource != null ? fSource.getContentsHash() : 0;
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

	/**
	 * Returns whether this inclusion was found by a heuristics.
	 */
	public boolean isFoundByHeuristics() {
		return fHeuristic;
	}
	
	public void setFoundByHeuristics(boolean val) {
		fHeuristic= val;
	}

	public boolean isSource() {
		return fIsSource;
	}

	public void setIsSource(boolean isSource) {
		fIsSource= isSource;
	}

	public IncludeSearchPathElement getFoundOnPath() {
		return fFoundOnPath;
	}

	public void setFoundOnPath(IncludeSearchPathElement isp) {
		fFoundOnPath= isp;
	}
}
