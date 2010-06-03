/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.core.parser;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.parser.InternalParserUtil;
import org.eclipse.cdt.internal.core.parser.scanner.CharArray;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;


/**
 * Abstract class for representing the content of a file.
 * It serves as the input to the preprocessor.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @since 5.2
 */
public abstract class FileContent {

	/** 
	 * Returns the location of this file content as it will appear in {@link IASTFileLocation#getFileName()}
	 */
	public abstract String getFileLocation();

	/**
	 * Returns a 64-bit hash value of the file contents.
	 */
	public abstract long getContentsHash();

	/**
	 * Creates a file content object for a fixed buffer.
	 * @param filePath the path of the file as it will appear in {@link IASTFileLocation#getFileName()}
	 * @param contents the actual content.
	 */
	public static FileContent create(String filePath, char[] contents) {
		return new InternalFileContent(filePath, new CharArray(contents));
	}
	
	/**
	 * Creates a file content object for a translation-unit, which may be a working copy.
	 */
	public static FileContent create(ITranslationUnit tu) {
		IPath location= tu.getLocation();
		if (location == null)
			return create(tu.getElementName(), tu.getContents());
		
		if (tu.isWorkingCopy()) {
			return create(location.toOSString(), tu.getContents());
		}
		
		IResource res= tu.getResource();
		if (res instanceof IFile) {
			return create((IFile) res);
		}
		return createForExternalFileLocation(location.toOSString());
	}
	
	/**
	 * Creates a file content object for an index file location.
	 */
	public static FileContent create(IIndexFileLocation ifl) {
		return InternalParserUtil.createFileContent(ifl);
	}

	/**
	 * Creates a file content for a workspace file
	 */
	public static FileContent create(IFile file) {
		return InternalParserUtil.createWorkspaceFileContent(file);
	}

	/**
	 * Creates a file content object for a file location that is not part of the workspace
	 */
	public static FileContent createForExternalFileLocation(String fileLocation) {
		return InternalParserUtil.createExternalFileContent(fileLocation);
	}

	/**
	 * Provided to achieve backwards compatibility.
	 */
	@Deprecated
	public static FileContent adapt(CodeReader reader) {
		if (reader == null)
			return null;
		return create(reader.getPath(), reader.buffer);
	}
}
