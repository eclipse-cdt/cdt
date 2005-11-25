/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;

/**
 * This is the interface that an AST Service uses to delegate the construction
 * of a CodeReader.
 * 
 * @author jcamelon
 */
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

	public CodeReader createCodeReaderForTranslationUnit(ITranslationUnit tu);
	
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
