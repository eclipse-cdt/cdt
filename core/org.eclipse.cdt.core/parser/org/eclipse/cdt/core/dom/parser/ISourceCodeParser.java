/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParseError;

/**
 * Interface for an AST source code parser.
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISourceCodeParser {

	/**
	 * Compute an abstract syntax tree (AST).
	 *
	 * The returned AST is frozen, any attempt modify any of the nodes in
	 * the AST will result in an IllegalStateException.
	 *
	 * @return the AST, should not return <code>null</code>
	 * @throws ParseError  if parsing has been cancelled or for other reasons
	 */
	public IASTTranslationUnit parse();

	/**
	 * Cancel the parsing.
	 */
	public void cancel();

	/**
	 * Check whether there were errors.
	 * @return <code>true</code> if there were errors
	 */
	public boolean encounteredError();

	/**
	 * Compute an {@link IASTCompletionNode} for code completion.
	 * @return a completion node or <code>null</code> if none could be computed
	 *
	 * @throws ParseError  if parsing has been cancelled or for other reasons
	 */
	public IASTCompletionNode getCompletionNode();

}
