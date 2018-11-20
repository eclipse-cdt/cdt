/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.parser.ISignificantMacros;

/**
 * This interface represents a preprocessor #include statement.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTPreprocessorIncludeStatement extends IASTPreprocessorStatement, IFileNomination {
	/**
	 * {@code INCLUDE_NAME} describes the relationship between an include directive and its name.
	 */
	public static final ASTNodeProperty INCLUDE_NAME = new ASTNodeProperty(
			"IASTPreprocessorMacroDefinition.INCLUDE_NAME - Include Name"); //$NON-NLS-1$

	/**
	 * Returns the absolute location of the file found through #include, or an empty string if
	 * include was not resolved.
	 */
	public String getPath();

	/**
	 * Returns the name of the file as specified in the directive. Does not include quotes or
	 * angle brackets.
	 * @since 4.0
	 */
	public IASTName getName();

	/**
	 * Returns whether this is a system include (one specified with angle brackets).
	 * @since 4.0
	 */
	public boolean isSystemInclude();

	/**
	 * Returns whether this include directive was actually taken.
	 * @since 4.0
	 */
	@Override
	public boolean isActive();

	/**
	 * Returns whether this include file was successfully resolved.
	 * @since 4.0
	 */
	public boolean isResolved();

	/**
	 * Returns whether the inclusion was resolved using a heuristics.
	 * @since 5.1
	 */
	public boolean isResolvedByHeuristics();

	/**
	 * Returns the list of versions of the target file, each of which is
	 * identified by its significant macros, that had been included
	 * in this translation-unit prior to this statement.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public ISignificantMacros[] getLoadedVersions();

	/**
	 * Returns the modification time of the included file, or -1 if the file was not read.
	 * @since 5.4
	 */
	public long getIncludedFileTimestamp();

	/**
	 * Returns the size of the included file, or -1 if the file was not read.
	 * @since 5.4
	 */
	public long getIncludedFileSize();

	/**
	 * Returns a hash-code for the contents of the file included, or <code>0</code>
	 * if the content has not been parsed.
	 * @since 5.4
	 */
	public long getIncludedFileContentsHash();

	/**
	 * Returns time when the included file was read. Corresponds to the start of reading.
	 * @return time before reading started in milliseconds since epoch
	 * @since 5.4
	 */
	public long getIncludedFileReadTime();

	/**
	 * Returns <code>true</code> if I/O errors were encountered while reading the included file.
	 * @since 5.4
	 */
	public boolean isErrorInIncludedFile();

	/**
	 * Returns {@code true} if the included file is exported by the including header.
	 *
	 * @see "https://github.com/include-what-you-use/include-what-you-use/blob/master/docs/IWYUPragmas.md"
	 * @since 5.5
	 */
	public boolean isIncludedFileExported();

	/**
	 * Returns {@code true}, if an attempt will be or has been made to create AST for the target
	 * of this inclusion.
	 * @since 5.4
	 */
	public boolean createsAST();

	/**
	 * Returns the file from the index that this include statement has pulled in, or {@code null}
	 * if the include creates AST or is unresolved or skipped.
	 * @since 5.4
	 */
	public IIndexFile getImportedIndexFile();
}
