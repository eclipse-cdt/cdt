/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;

/**
 * This is the interface that an AST Service uses to delegate the construction
 * of a CodeReader.
 *
 * @author jcamelon
 * @deprecated replaced by {@link IncludeFileContentProvider}
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface ICodeReaderFactory {
	/**
	 * @return unique identifier as int
	 */
	public int getUniqueIdentifier();

	/**
	 * Create CodeReader for translation unit
	 *
	 * @param path
	 *            Canonical Path representing path location for file to be
	 *            opened
	 * @return CodeReader for contents at that path.
	 */
	public CodeReader createCodeReaderForTranslationUnit(String path);

	/**
	 * Create CodeReader for inclusion.
	 *
	 * @param path
	 * @return CodeReader for contents at that path.
	 */
	public CodeReader createCodeReaderForInclusion(String path);

	/**
	 * Returns the ICodeReaderCache used for this ICodeReaderFacotry.
	 * @return the ICodeReaderCache used for this ICodeReaderFacotry
	 */
	public ICodeReaderCache getCodeReaderCache();
}
