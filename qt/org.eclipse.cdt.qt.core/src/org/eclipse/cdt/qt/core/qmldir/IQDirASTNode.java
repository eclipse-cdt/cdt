/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.qmldir;

import org.eclipse.cdt.qt.core.location.ISourceLocation;

/**
 * The base type for all qmldir AST nodes. Contains methods for retrieving a node's positional information.
 */
public interface IQDirASTNode {
	/**
	 * Gets a more detailed description of this node's location than {@link IQDirASTNode#getStart()} and
	 * {@link IQDirASTNode#getStart()}. This method allows the retrieval of line and column information in order to make output for
	 * syntax errors and the like more human-readable.
	 *
	 * @return the {@link ISourceLocation} representing this node's location in the source
	 */
	public ISourceLocation getLocation();

	/**
	 * Gets the zero-indexed offset indicating the start of this node in the source.
	 *
	 * @return the node's start offset
	 */
	public int getStart();

	/**
	 * Gets the zero-indexed offset indicating the end of this node in the source.
	 *
	 * @return the node's end offset
	 */
	public int getEnd();
}
