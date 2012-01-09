/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	private final List<IASTName> names = new ArrayList<IASTName>();
	private final IASTTranslationUnit translationUnit;

	
	public ASTCompletionNode(IToken completionToken, IASTTranslationUnit translationUnit) {
		this.completionToken = completionToken;
		this.translationUnit = translationUnit;
	}


	public void addName(IASTName name) {
		names.add(name);
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
	public IASTName[] getNames() {
		return names.toArray(new IASTName[names.size()]);
	}


	@Override
	public IASTTranslationUnit getTranslationUnit() {
		return translationUnit;
	}
}
