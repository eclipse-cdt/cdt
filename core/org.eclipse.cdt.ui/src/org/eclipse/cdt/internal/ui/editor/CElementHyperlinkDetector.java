/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.ui.texteditor.ITextEditor;

public class CElementHyperlinkDetector implements IHyperlinkDetector{

	private ITextEditor fTextEditor;
	//TODO: Replace Keywords
	//Temp. Keywords: Once the selection parser is complete, we can use
	//it to determine if a word can be underlined	
	private  Set fgKeywords;

	public CElementHyperlinkDetector(ITextEditor editor) {
		fTextEditor= editor;
		fgKeywords = KeywordSets.getKeywords(KeywordSetKey.ALL,ParserLanguage.CPP);
	}

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || canShowMultipleHyperlinks || !(fTextEditor instanceof CEditor))
			return null;
		
		IAction openAction= fTextEditor.getAction("OpenDeclarations"); //$NON-NLS-1$
		if (openAction == null)
			return null;

		// TODO: 
		//Need some code in here to determine if the selected input should
		//be selected - the JDT does this by doing a code complete on the input -
		//if there are any elements presented it selects the word

		int offset= region.getOffset();
		IDocument document= fTextEditor.getDocumentProvider().getDocument(fTextEditor.getEditorInput());

		IRegion cregion = selectWord(document, offset);
		if (cregion != null) {
			return new IHyperlink[] {new CElementHyperlink(cregion, openAction)};
		}
		return null;
	}

	private IRegion selectWord(IDocument document, int anchor) {
		//TODO: Modify this to work with qualified name
		
		try {		
			int offset= anchor;
			char c;
			
			while (offset >= 0) {
				c= document.getChar(offset);
				if (!Character.isJavaIdentifierPart(c))
					break;
				--offset;
			}
			
			int start= offset;
			
			offset= anchor;
			int length= document.getLength();
			
			while (offset < length) {
				c= document.getChar(offset);
				if (!Character.isJavaIdentifierPart(c))
					break;
				++offset;
			}
			
			int end= offset;
			//Allow for new lines
			if (start == end)
				return new Region(start, 0);

			String selWord = null;
			String slas = document.get(start,1);
			if (slas.equals("\n") || //$NON-NLS-1$
					slas.equals("\t") || //$NON-NLS-1$
					slas.equals(" "))	 //$NON-NLS-1$
			{
				
				selWord =document.get(start+1, end - start - 1);
			}
			else{
				selWord =document.get(start, end - start);  	
			}
			//Check for keyword
			if (isKeyWord(selWord))
				return null;
			//Avoid selecting literals, includes etc.
			char charX = selWord.charAt(0);
			if (charX == '"' ||
					charX == '.' ||
					charX == '<' ||
					charX == '>')
				return null;
			
			if (selWord.equals("#include")) //$NON-NLS-1$
			{
				//get start of next identifier
				
				
				int end2 = end;
				
				while (!Character.isJavaIdentifierPart(document.getChar(end2))){
					++end2;		
				}
				
				while (end2 < length){
					c = document.getChar(end2);
					
					if (!Character.isJavaIdentifierPart(c) &&
							c != '.')
						break;
					++end2;
				}
				
				int finalEnd = end2;
				selWord =document.get(start, finalEnd - start);
				end = finalEnd + 1;
				start--;
			}
			
			return new Region(start + 1, end - start - 1);
			
		} catch (BadLocationException x) {
			return null;
		}
	}

	private boolean isKeyWord(String selWord) {
		Iterator i = fgKeywords.iterator();
		
		while (i.hasNext()){
			 String tempWord = (String) i.next();
			 if (selWord.equals(tempWord))
			 	return true;
		}
		
		return false;
	}


}
