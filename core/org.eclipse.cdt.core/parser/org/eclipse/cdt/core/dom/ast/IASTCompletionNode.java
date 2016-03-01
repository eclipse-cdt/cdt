/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This represents the node that would occur at the point of a context
 * completion.
 * 
 * This node may contain the prefix text of an identifier up to the point. If
 * there is no prefix, the completion occurred at the point where a new token
 * would have begun.
 * 
 * The node points to the parent node where this node, if replaced by a proper
 * node, would reside in the tree.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTCompletionNode {
	/**
	 * If the point of completion was at the end of a potential identifier, this
	 * string contains the text of that identifier.
	 * 
	 * @return the prefix text up to the point of completion
	 */
	public String getPrefix();

	/**
	 * Returns the length of the completion point.
	 */
	public int getLength();

	/**
	 * Returns a list of names that fit in this context.
	 */
	public IASTName[] getNames();

	/**
	 * Returns the translation unit for this completion.
	 */
	public IASTTranslationUnit getTranslationUnit();
}
