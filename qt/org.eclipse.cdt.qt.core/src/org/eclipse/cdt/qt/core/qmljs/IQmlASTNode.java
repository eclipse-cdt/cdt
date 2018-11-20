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
package org.eclipse.cdt.qt.core.qmljs;

import org.eclipse.cdt.qt.core.location.ISourceLocation;
import org.eclipse.cdt.qt.core.qmldir.IQDirASTNode;
import org.eclipse.cdt.qt.core.tern.ITernScope;

/**
 * The base node interface for all QML and JavaScript Abstract Syntax Tree elements. Conforms to the ESTree Specification as well as
 * the extra features added by Acorn.
 *
 * @see <a href="https://github.com/estree/estree/blob/master/spec.md#node-objects">ESTree Node Objects</a>
 */
public interface IQmlASTNode {

	/**
	 * Gets the String representation of the type of AST node that this node represents. This is a bit redundant in Java with access
	 * to <code>instanceof</code>, but is provided for the sake of conforming to the ESTree Specification for node objects.
	 *
	 * @return the String representation of this node
	 */
	public String getType();

	/**
	 * Gets a more detailed description of this node's location than {@link IQDirASTNode#getStart()} and
	 * {@link IQDirASTNode#getStart()}. This method allows the retrieval of line and column information in order to make output for
	 * syntax errors and the like more human-readable.<br>
	 * <br>
	 * <b>Note</b>: It is necessary to set the 'locations' option to <code>true</code> when parsing with acorn in order to use this
	 * method.
	 *
	 * @return the {@link ISourceLocation} representing this node's location in the source or <code>null</code> if not available
	 */
	public ISourceLocation getLocation();

	/**
	 * Gets the range of this node if available. A range is an array of two integers containing the start and end offset of this
	 * node in that order. Like {@link IQmlASTNode#getStart()} and {@link IQmlASTNode#getEnd()}, this method returns zero-indexed
	 * offsets relative to the beginning of the source.<br>
	 * <br>
	 * <b>Note</b>: It is necessary to set the 'ranges' option to <code>true</code> when parsing with acorn in order to use this
	 * method.
	 *
	 * @return the range of this node or <code>null</code> if not available
	 */
	public int[] getRange();

	/**
	 * Gets the zero-indexed offset indicating the start of this node relative to the beginning of the source.
	 *
	 * @return the node's start offset
	 */
	public int getStart();

	/**
	 * Gets the zero-indexed offset indicating the end of this node relative to the beginning of the source.
	 *
	 * @return the node's end offset
	 */
	public int getEnd();

	/**
	 * Gets the {@link ITernScope} attached to this node if one exists. This method will only return a non-null value if the AST was
	 * already processed by Tern. For example, if the AST was retrieved from Tern using the 'parseFile' query, then at least one of
	 * the AST nodes will contain a scope object. However, if the 'parseString' query was used, no static analysis will be performed
	 * on the parsed AST and there will be no scope objects attached to any of its nodes.
	 *
	 * @return the Tern scope or <code>null</code> if not available
	 */
	public ITernScope getScope();
}
