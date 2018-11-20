/*******************************************************************************
 *  Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/**
 * An AST node that represents the location of content assist
 * in the source file.
 *
 * This node may contain the prefix text of an identifier up to the point. If
 * there is no prefix, the completion occurred at the point where a new token
 * would have begun.
 *
 * Contains a list of name nodes, each name represents an identifier
 * at the point where content assist was invoked. There is usually
 * a single name node, however if an ambiguity is detected then that section
 * of the source may be interpreted in more than one way (for example, as an expression then as a declaration).
 * This results in an ambiguity node in the tree and one name node for each of the ways it can be interpreted.
 *
 * The full AST may be accessed via getTranslationUnit() or by following
 * the parent pointers of the name nodes.
 *
 * @author Mike Kucera
 */
public class ASTCompletionNode implements IASTCompletionNode {

	private final List<CompletionNameEntry> entries = new ArrayList<>();

	private final String prefix;
	private IASTTranslationUnit tu;

	/**
	 * Creates a completion node.
	 * @param prefix The completion prefix, set to null if the empty string is passed.
	 */
	public ASTCompletionNode(String prefix, IASTTranslationUnit tu) {
		if ("".equals(prefix)) //$NON-NLS-1$
			prefix = null;

		this.prefix = prefix;
		this.tu = tu;
	}

	public ASTCompletionNode(String prefix) {
		this(prefix, null);
	}

	public void addName(IASTName name) {
		entries.add(new CompletionNameEntry(name, name.getParent()));
	}

	/**
	 * Returns the length of the prefix.
	 */
	@Override
	public int getLength() {
		return prefix == null ? 0 : prefix.length();
	}

	@Override
	public IASTName[] getNames() {
		IASTName[] names = new IASTName[entries.size()];
		for (int i = 0; i < entries.size(); ++i) {
			names[i] = entries.get(i).fName;
		}
		return names;
	}

	/**
	 * If the point of completion was at the end of a potential identifier, this
	 * string contains the text of that identifier.
	 *
	 * @returns a string of length >= 1 or null
	 */
	@Override
	public String getPrefix() {
		return prefix;
	}

	public void setTranslationUnit(IASTTranslationUnit tu) {
		this.tu = tu;
	}

	@Override
	public IASTTranslationUnit getTranslationUnit() {
		return tu;
	}

	@Override
	public boolean containsName(IASTName name) {
		for (CompletionNameEntry entry : entries) {
			if (entry.fName == name) {
				return true;
			}
		}
		return false;
	}

	@Override
	public CompletionNameEntry[] getEntries() {
		return entries.toArray(new CompletionNameEntry[entries.size()]);
	}
}
