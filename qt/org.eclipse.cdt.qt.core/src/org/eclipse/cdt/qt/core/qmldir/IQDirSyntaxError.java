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

import org.eclipse.cdt.qt.core.qmldir.QMLDirectoryLexer.Token;
import org.eclipse.cdt.qt.core.qmldir.QMLDirectoryParser.SyntaxError;

/**
 * An AST Node representing a syntax error in a qmldir file. Due to the fact that the qmldir file is so simple, a syntax error will
 * only occur at the command level while the parser jumps to the next line to recover.
 */
public interface IQDirSyntaxError extends IQDirCommand {
	/**
	 * Gets the token that caused the parser to fail. This is a helper method equivalent to
	 * <code>getSyntaxError.getOffendingToken()</code>.
	 *
	 * @return the offending token.
	 */
	public Token getOffendingToken();

	/**
	 * Gets the node that the parser was working on before it failed (if available). This is a helper method equivalent to
	 * <code>getSyntaxError.getIncompleteNode()</code>.
	 *
	 * @return the incomplete node or <code>null</code> if not available
	 */
	public IQDirASTNode getIncompleteNode();

	/**
	 * Gets the syntax error that occurred.
	 *
	 * @return the syntax error
	 */
	public SyntaxError getSyntaxError();
}
