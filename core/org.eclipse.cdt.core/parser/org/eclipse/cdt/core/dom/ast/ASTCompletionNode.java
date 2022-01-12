/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.parser.IToken;

/**
 * @author Doug Schaefer
 */
public class ASTCompletionNode implements IASTCompletionNode {
	private final IToken completionToken;
	private final List<CompletionNameEntry> entries = new ArrayList<>();
	private final IASTTranslationUnit translationUnit;

	public ASTCompletionNode(IToken completionToken, IASTTranslationUnit translationUnit) {
		this.completionToken = completionToken;
		this.translationUnit = translationUnit;
	}

	public void addName(IASTName name) {
		entries.add(new CompletionNameEntry(name, name.getParent()));
	}

	@Override
	public String getPrefix() {
		return completionToken.getType() == IToken.tEOC ? "" : completionToken.getImage(); //$NON-NLS-1$
	}

	@Override
	public int getLength() {
		return completionToken.getLength();
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
	public IASTName[] getNames() {
		IASTName[] names = new IASTName[entries.size()];
		for (int i = 0; i < entries.size(); ++i) {
			names[i] = entries.get(i).fName;
		}
		return names;
	}

	@Override
	public CompletionNameEntry[] getEntries() {
		return entries.toArray(new CompletionNameEntry[entries.size()]);
	}

	@Override
	public IASTTranslationUnit getTranslationUnit() {
		return translationUnit;
	}
}
