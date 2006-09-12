/*******************************************************************************
 * Copyright (c) 2000 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

/**
 * Double click strategy aware of C identifier syntax rules.
 */
public class CDoubleClickSelector implements ITextDoubleClickStrategy {

	protected ITextViewer fText;
	protected int fPos;
	protected int fStartPos;
	protected int fEndPos;
	private CPairMatcher fPairMatcher;


	protected static char[] fgBrackets= {'{', '}', '(', ')', '[', ']', '"', '"'};

	public CDoubleClickSelector() {
		super();
		fPairMatcher= new CPairMatcher(fgBrackets);
	}


	/**
	 * @see ITextDoubleClickStrategy#doubleClicked
	 */
	public void doubleClicked(ITextViewer text) {
		fPos= text.getSelectedRange().x;


		if (fPos < 0)
			return;


		fText= text;


		if (!selectBracketBlock())
			selectWord();
		
		fText= null;
	}


	protected boolean matchBracketsAt() {
		IRegion region= fPairMatcher.match(fText.getDocument(), fPos);
		if (region != null && region.getLength() > 0) {
			fStartPos= region.getOffset();
			fEndPos= fStartPos + region.getLength() - 1;
			return true;
		}
		return false;
	}


	protected boolean matchWord() {
		IDocument doc= fText.getDocument();
		try {
			int pos= fPos;
			char c;


			while (pos >= 0) {
				c= doc.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				--pos;
			}
			fStartPos= pos;


			pos= fPos;
			int length= doc.getLength();


			while (pos < length) {
				c= doc.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				++pos;
			}
			fEndPos= pos;


			return true;


		} catch (BadLocationException x) {
		}
		return false;
	}


	protected boolean selectBracketBlock() {
		if (matchBracketsAt()) {
			if (fStartPos == fEndPos)
				fText.setSelectedRange(fStartPos, 0);
			else
				fText.setSelectedRange(fStartPos + 1, fEndPos - fStartPos - 1);


			return true;
		}
		return false;
	}


	protected void selectWord() {
		if (matchWord()) {
			if (fStartPos == fEndPos)
				fText.setSelectedRange(fStartPos, 0);
			else
				fText.setSelectedRange(fStartPos + 1, fEndPos - fStartPos - 1);
		}
	}
}
