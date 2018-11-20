/*******************************************************************************
 * Copyright (c) 2009, 2013 Wind River Systems, Inc. and others.
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
 *     Chris Recoskie (IBM Corporation)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.io.File;

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
	/** @since 5.4 */
	public static final long NULL_TIMESTAMP = -1;
	/** @since 5.4 */
	public static final long NULL_FILE_SIZE = -1;

	/**
	 * Returns the location of this file content as it will appear in {@link IASTFileLocation#getFileName()}
	 */
	public abstract String getFileLocation();

	/**
	 * Returns the modification time of the file containing the content, or NULL_TIMESTAMP if
	 * the content does not originate from a file. A zero value may be returned if there was
	 * an I/O error.
	 * @since 5.4
	 */
	public abstract long getTimestamp();

	/**
	 * Returns time when the file was read. Corresponds to the start of reading.
	 * @return time before reading started in milliseconds since epoch
	 * @since 5.4
	 */
	public abstract long getReadTime();

	/**
	 * Returns the size of the file, or NULL_FILE_SIZE if the content does not originate from
	 * a file. A zero value may be returned if there was an I/O error.
	 * @since 5.4
	 */
	public abstract long getFileSize();

	/**
	 * Returns {@code true} if there were I/O errors while retrieving contents of this file.
	 * @since 5.4
	 */
	public abstract boolean hasError();

	/**
	 * Returns a 64-bit hash value of the file contents.
	 */
	public abstract long getContentsHash();

	/**
	 * Creates a file content object for a fixed buffer.
	 *
	 * @param filePath the path of the file as it will appear in {@link IASTFileLocation#getFileName()}
	 * @param contents the actual content.
	 */
	public static FileContent create(String filePath, char[] contents) {
		return new InternalFileContent(filePath, new CharArray(contents));
	}

	/**
	 * Creates a file content object for a fixed buffer.
	 *
	 * @param filePath the path of the file as it will appear in {@link IASTFileLocation#getFileName()}
	 * @param contents the actual content.
	 * @since 6.3
	 */
	public static FileContent create(String filePath, boolean isSource, char[] contents) {
		InternalFileContent fileContent = new InternalFileContent(filePath, new CharArray(contents));
		fileContent.setIsSource(isSource);
		return fileContent;
	}

	/**
	 * Creates a file content object for a translation-unit, which may be a working copy.
	 */
	public static FileContent create(ITranslationUnit tu) {
		InternalFileContent fileContent;

		IPath location = tu.getLocation();
		if (location == null) {
			fileContent = new InternalFileContent(tu.getElementName(), new CharArray(tu.getContents()));
		} else if (tu.isWorkingCopy()) {
			fileContent = new InternalFileContent(location.toOSString(), new CharArray(tu.getContents()));
		} else {
			IResource res = tu.getResource();
			if (res instanceof IFile) {
				fileContent = InternalParserUtil.createWorkspaceFileContent((IFile) res);
			} else {
				fileContent = InternalParserUtil.createExternalFileContent(location.toOSString(),
						InternalParserUtil.SYSTEM_DEFAULT_ENCODING);
			}
		}

		if (fileContent != null) {
			fileContent.setTranslationUnit(tu);
			fileContent.setIsSource(tu.isSourceUnit());
		}
		return fileContent;
	}

	/**
	 * Creates a file content object for an index file location.
	 */
	public static FileContent create(IIndexFileLocation ifl) {
		return InternalParserUtil.createFileContent(ifl);
	}

	/**
	 * Creates a file content for a workspace header file.
	 */
	public static FileContent create(IFile file) {
		return InternalParserUtil.createWorkspaceFileContent(file);
	}

	/**
	 * Creates a file content object for a header file that is not part of the workspace.
	 */
	public static FileContent createForExternalFileLocation(String fileLocation) {
		return createForExternalFileLocation(fileLocation, InternalParserUtil.SYSTEM_DEFAULT_ENCODING);
	}

	/**
	 * Creates a file content object for a header file that is not part of the workspace.
	 * @since 5.3
	 */
	public static FileContent createForExternalFileLocation(String fileLocation, String encoding) {
		return createForExternalFileLocation(fileLocation, false, encoding);
	}

	/**
	 * Creates a file content object for a header or a source file that is not part of the workspace.
	 * @since 6.3
	 */
	public static FileContent createForExternalFileLocation(String fileLocation, boolean isSource, String encoding) {
		InternalFileContent fileContent = InternalParserUtil.createExternalFileContent(fileLocation, encoding);
		if (fileContent != null)
			fileContent.setIsSource(isSource);
		return fileContent;
	}

	/**
	 * Provided to achieve backwards compatibility.
	 */
	@Deprecated
	public static FileContent adapt(CodeReader reader) {
		if (reader == null)
			return null;

		long fileReadTime = System.currentTimeMillis();
		CharArray chars = new CharArray(reader.buffer);
		String filePath = reader.getPath();
		File file = new File(filePath);
		return new InternalFileContent(filePath, chars, file.lastModified(), file.length(), fileReadTime);
	}
}
