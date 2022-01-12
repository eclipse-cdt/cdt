/*******************************************************************************
 * Copyright (c) 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;

/**
 * A container for symbols that should be added to a linkage in the persisted index.  Contributors
 * of the org.eclipse.cdt.core.PDOMASTProcessor extension-point are provided the opportunity
 * to process the parsed AST translation units.  Elements of interest are added to an
 * implementation of this interface.
 *
 * @since 5.6
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IIndexSymbols {
	/**
	 * Return true if the map contains no symbols and false otherwise.
	 */
	public boolean isEmpty();

	/**
	 * Adds an IASTName along with an optional referencing IASTName to the map.  The names
	 * will be associated with the optional owning include statement.
	 *
	 * @param owner  The optional include statement that provides context for the given
	 *               names.  Can be null.
	 * @param name   The name that is being added to the map, cannot be null.
	 * @param caller The optional name that references the name being added.  Can be null.
	 */
	public void add(IASTPreprocessorIncludeStatement owner, IASTName name, IASTName caller);

	/**
	 * Creates an association from the caller include statement to the owning context.
	 */
	public void add(IASTPreprocessorIncludeStatement owner, IASTPreprocessorIncludeStatement caller);

	/**
	 * Creates an association from the caller preprocessor statement to the owning context.
	 */
	public void add(IASTPreprocessorIncludeStatement owner, IASTPreprocessorStatement caller);
}
