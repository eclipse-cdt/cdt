/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents a macro stored in the index.
 *
 * @since 4.0
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IIndexMacro extends IMacroBinding, IIndexBinding {
	IIndexMacro[] EMPTY_INDEX_MACRO_ARRAY = {};

	/**
	 * If available, return the file location for the macro definition of this macro,
	 * otherwise return <code>null</code>.
	 */
	IASTFileLocation getFileLocation() throws CoreException;

	/**
	 * Returns the file in which this macro is defined and belongs to.
	 * @throws CoreException
	 */
	IIndexFile getFile() throws CoreException;

	/**
	 * Returns the name of the definition of this macro, or <code>null</code> if not available.
	 * @throws CoreException
	 * @since 5.0
	 */
	IIndexName getDefinition() throws CoreException;
}
