/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeFileContent.InclusionKind;

/** 
 * The index based code-reader factory fakes the inclusion of files that are already indexed.
 * When trying to figure out whether a specific header has been included or not, the factory
 * has to be consulted.
 * @since 4.0.1
 */
public interface IIndexBasedCodeReaderFactory extends ICodeReaderFactory {
	/**
	 * Returns whether or not the file has been included.
	 */
	boolean hasFileBeenIncludedInCurrentTranslationUnit(String path);

	/**
	 * Reports the path of the translation unit, such that it is known as included.
	 */
	void reportTranslationUnitFile(String path);

	/**
	 * Create an InclusionContent object for the given location.
     * return an inclusion content or <code>null</code> if the location does not exist.
	 * @see IncludeFileContent
	 */
	public IncludeFileContent getContentForInclusion(String fileLocation);

	/**
	 * Check whether the specified inclusion exists.
	 * @since 5.0
	 */
	boolean getInclusionExists(String finalPath);

	/**
	 * Returns a file-content object of kind {@link InclusionKind#FOUND_IN_INDEX}, representing
	 * the content from the context of the given file up to where the file actually gets included.
	 * @since 5.0
	 */
	IncludeFileContent getContentForContextToHeaderGap(String fileLocation);
}
