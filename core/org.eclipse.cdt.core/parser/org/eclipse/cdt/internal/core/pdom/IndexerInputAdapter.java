/*******************************************************************************
 * Copyright (c) 2007, 2014 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.internal.core.pdom;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IScannerInfo;

/**
 * Abstract class to obtain information about the input to the indexer. For the
 * project based indexers the input are translation units, for the stand-alone
 * indexer they are file-paths represented as strings.
 * @since 5.0
 */
public abstract class IndexerInputAdapter extends ASTFilePathResolver {
	/**
	 * Returns an object representing an input file for the given index location,
	 * or <code>null</code>, if it does not exist.
	 */
	public abstract Object getInputFile(IIndexFileLocation location);

	/**
	 * Return the last modification date for the file denoted by the index location.
	 */
	public abstract long getLastModified(IIndexFileLocation location);

	/**
	 * Returns the size of the file in bytes, or 0 if the file does not exist.
	 */
	public abstract long getFileSize(IIndexFileLocation location);

	/**
	 * Returns the encoding for the file.
	 */
	public abstract String getEncoding(IIndexFileLocation location);

	/**
	 * Create an index location for the given input file.
	 */
	public abstract IIndexFileLocation resolveFile(Object tu);

	/**
	 * Tests whether the input file is a source unit.
	 */
	public abstract boolean isSourceUnit(Object tu);

	/**
	 * Tests whether the input file is part of the build.
	 */
	public abstract boolean isFileBuildConfigured(Object tu);

	/**
	 * Returns whether the given translation unit is not indexed unless it gets included.
	 * This applies to files that are outside of a source root.
	 */
	public abstract boolean isIndexedOnlyIfIncluded(Object tu);

	/**
	 * Checks whether the given file should be indexed unconditionally.
	 *
	 * @param location the location of the file
	 * @return {@code true} if the file should be indexed unconditionally
	 */
	public abstract boolean isIndexedUnconditionally(IIndexFileLocation location);

	/**
	 * Returns the priority of indexing a file. The priority is a nonnegative number. A larger
	 * number means a higher priority causing the file to be indexed ahead of files with lower
	 * priorities.
	 *
	 * @param location the location of the file
	 * @return {@code true} the indexing priority
	 */
	public abstract int getIndexingPriority(IIndexFileLocation location);

	/**
	 * Tests whether the file in the index is allowed to be part of an SDK. If not
	 * it will be indexed.
	 */
	public abstract boolean canBePartOfSDK(IIndexFileLocation ifl);

	/**
	 * Obtains the languages the input file should be parsed with.
	 */
	public abstract AbstractLanguage[] getLanguages(Object tu, AbstractIndexerTask.UnusedHeaderStrategy strat);

	/**
	 * Obtains the scanner configuration for the input file.
	 */
	public abstract IScannerInfo getBuildConfiguration(int linkageID, Object tu);

	/**
	 * Returns a code reader for the given input file.
	 */
	public abstract FileContent getCodeReader(Object tu);
}
