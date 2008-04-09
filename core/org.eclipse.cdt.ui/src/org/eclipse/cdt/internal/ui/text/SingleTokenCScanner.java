/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import org.eclipse.cdt.ui.text.ITokenStoreFactory;

/**
 * 
 */
public final class SingleTokenCScanner extends AbstractCScanner {
	private String fProperty;
	private int position, end;
	private int size;
	protected IToken fDefaultReturnToken;
	
	public SingleTokenCScanner(ITokenStoreFactory factory, String property) {
		super(factory.createTokenStore(new String[] {property}), 20);
		fProperty= property;
		setRules(createRules());
	}

	protected List<IRule> createRules() {
		fDefaultReturnToken= getToken(fProperty);
		setDefaultReturnToken(fDefaultReturnToken);
		return null;
	}
	
	/**
	 * setRange -- sets the range to be scanned
	 */
	@Override
	public void setRange(IDocument document, int offset, int length) {
		
		super.setRange(document, offset, length);
		position = offset;
		size = length;
		end = offset + length;
	}
	
	/**
	 * Returns the next token in the document.
	 *
	 * @return the next token in the document
	 */
	@Override
	public IToken nextToken() {
		fTokenOffset = position;
		
		if(position < end) {
			size = end - position;
			position = end;
			return fDefaultReturnToken;
		}
		return Token.EOF;
	}
	
	@Override
	public int getTokenLength() {
		return size;
	}
}



