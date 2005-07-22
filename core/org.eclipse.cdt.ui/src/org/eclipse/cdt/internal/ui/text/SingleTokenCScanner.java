/*******************************************************************************
 * Copyright (c) 2000 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.List;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;


/**
 * 
 */
public final class SingleTokenCScanner extends AbstractCScanner{
	
	protected IToken fDefaultReturnToken;
	private String[] fProperty;
	
	public SingleTokenCScanner(IColorManager manager, IPreferenceStore store, String property) {
		super(manager, store, 20);
		fProperty= new String[] { property };
		initialize();
	}

	/*
	 * @see AbstractCScanner#getTokenProperties()
	 */
	protected String[] getTokenProperties() {
		return fProperty;
	}

	/*
	 * @see AbstractCScanner#createRules()
	 */
	protected List createRules() {
		fDefaultReturnToken= getToken(fProperty[0]);
		setDefaultReturnToken(fDefaultReturnToken);
		return null;
	}
	
	/**
	 * setRange -- sets the range to be scanned
	 */
	
	private int position, end;
	private int size;
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
	public IToken nextToken() {
		
		fTokenOffset = position;
		
		if(position < end) {
			size = end - position;
			position = end;
			return fDefaultReturnToken;
		}
		return Token.EOF;
	}
	
	public int getTokenLength() {
		return size;
	}
	
	//public int getTokenOffset() {
	//	return position;
	//}
		/* while (true) {
			
			fTokenOffset= fOffset;
			fColumn= UNDEFINED;
			
			if (fRules != null) {
				for (int i= 0; i < fRules.length; i++) {
					token= (fRules[i].evaluate(this));
					if (!token.isUndefined())
						return token;
				}
			}
			
			if (read() == EOF)
				return Token.EOF;
			else
				return fDefaultReturnToken;
		} 
	} */

}



