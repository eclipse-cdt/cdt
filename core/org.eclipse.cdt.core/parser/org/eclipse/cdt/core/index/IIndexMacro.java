/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents a macro stored in the index.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @since 4.0
 */
public interface IIndexMacro extends IMacro {
	
	IIndexMacro[] EMPTY_INDEX_MACRO_ARRAY = new IIndexMacro[0];

	/**
	 * If available, return the file location for this macro definition
	 * otherwise return null
	 * @return
	 */
	IASTFileLocation getFileLocation();
	
	/**
	 * Returns the file this macro belongs to.
	 * @throws CoreException 
	 */
	IIndexFile getFile() throws CoreException;
	
	/**
	 * Returns the character offset of the location of the name.
	 */
	public int getNodeOffset();

	/**
	 * Returns the length of the name.
	 */
	public int getNodeLength();
}
