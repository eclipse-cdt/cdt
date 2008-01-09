/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.parser.CodeReader;

/**
 * Instructs the preprocessor on how to handle a file-inclusion.
 * @since 5.0
 */
public class FileInclusionHandling {
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
		 * The file has to be scanned, a code reader is provided.
		 */
		USE_CODE_READER
	}

	private InclusionKind fKind;
	private CodeReader fCodeReader;
	private ArrayList<IIndexMacro> fMacroDefinitions;
	private String fFileLocation;
	
	public FileInclusionHandling(String fileLocation, InclusionKind kind) {
		assert kind == InclusionKind.SKIP_FILE;
		fFileLocation= fileLocation;
		fKind= kind;
	}

	public FileInclusionHandling(CodeReader codeReader) {
		assert codeReader != null;
		fKind= InclusionKind.USE_CODE_READER;
		fCodeReader= codeReader;
		if (codeReader != null) {
			fFileLocation= codeReader.getPath();
		}
	}

	public FileInclusionHandling(String fileLocation, ArrayList<IIndexMacro> macroDefinitions) {
		fKind= InclusionKind.FOUND_IN_INDEX;
		fFileLocation= fileLocation;
		fMacroDefinitions= macroDefinitions;
	}

	/**
	 * @return the kind
	 */
	public InclusionKind getKind() {
		return fKind;
	}

	/**
	 * Valid with {@link InclusionKind#USE_CODE_READER}.
	 * @return the codeReader
	 */
	public CodeReader getCodeReader() {
		return fCodeReader;
	}

	/**
	 * Valid with {@link InclusionKind#FOUND_IN_INDEX}.
	 * @return the macroDefinitions
	 */
	public List<IIndexMacro> getMacroDefinitions() {
		return fMacroDefinitions;
	}

	/**
	 * Returns the location of the file to be included.
	 */
	public String getFileLocation() {
		return fFileLocation;
	}
}
