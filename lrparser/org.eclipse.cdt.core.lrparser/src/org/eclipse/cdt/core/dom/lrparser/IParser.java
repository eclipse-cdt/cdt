/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Represents a parser that can be used by BaseExtensibleLanguage.
 *
 * @author Mike Kucera
 */
public interface IParser<N extends IASTNode> {

	/**
	 * Performs the actual parse.
	 *
	 * If there were any errors during the parse these will be represented in the
	 * AST as problem nodes.
	 *
	 * If the parser encounters a completion token then a completion node
	 * will be available via the getCompletionNode() method.
	 */
	public N parse();

	/**
	 * Returns the completion node if a completion token was encountered
	 * during the parse, null otherwise.
	 */
	public IASTCompletionNode getCompletionNode();

}
