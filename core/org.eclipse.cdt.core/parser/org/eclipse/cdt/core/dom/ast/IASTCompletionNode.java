/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation
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
	 * Represents a name that fits in this context, and its parent.
	 * The parent is stored separately because two entries can have
	 * the same name but different parents. (This is due to the
	 * parser sometimes re-using nodes between alternatives in an
	 * ambiguous node.)
	 *
	 * @since 6.4
	 */
	public class CompletionNameEntry {
		public CompletionNameEntry(IASTName name, IASTNode parent) {
			fName = name;
			fParent = parent;
		}

		public IASTName fName;
		public IASTNode fParent;
	}

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
	 * Returns true if this completion node contains a {@link CompletionNameEntry}
	 * with the given name.
	 *
	 * @since 6.4
	 */
	public boolean containsName(IASTName name);

	/**
	 * Returns a list of names that fit in this context.
	 * If doing computations based on the name's parent, prefer calling getEntries() instead
	 * and obtaining the parent from there.
	 */
	public IASTName[] getNames();

	/**
	 * Returns a list of names that fir in this context, along with their parents.
	 * See {@link CompletionNameEntry} for more details.
	 *
	 * @since 6.4
	 */
	public CompletionNameEntry[] getEntries();

	/**
	 * Returns the translation unit for this completion.
	 */
	public IASTTranslationUnit getTranslationUnit();
}
