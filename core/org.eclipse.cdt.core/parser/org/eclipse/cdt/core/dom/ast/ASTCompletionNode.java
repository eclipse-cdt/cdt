/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
 * 
 * @author Doug Schaefer
 */
public class ASTCompletionNode implements IASTCompletionNode {

	private IToken completionToken;

	private List names = new ArrayList();
	
	private IASTTranslationUnit translationUnit;

	
	/**
	 * Only constructor.
	 * 
	 * @param completionToken the completion token
	 * @param translationUnit the translation unit for this completion
	 */
	public ASTCompletionNode(IToken completionToken, IASTTranslationUnit translationUnit) {
		this.completionToken = completionToken;
		this.translationUnit = translationUnit;
	}

	/**
	 * Add a name to node.
	 * 
	 * @param name
	 */
	public void addName(IASTName name) {
		names.add(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTCompletionNode#getPrefix()
	 */
	public String getPrefix() {
		return completionToken.getType() != IToken.tEOC ? completionToken.getImage() : ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTCompletionNode#getLength()
	 */
	public int getLength() {
		return completionToken.getLength();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTCompletionNode#getNames()
	 */
	public IASTName[] getNames() {
		return (IASTName[]) names.toArray(new IASTName[names.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTCompletionNode#getTranslationUnit()
	 */
	public IASTTranslationUnit getTranslationUnit() {
		return translationUnit;
	}
}
