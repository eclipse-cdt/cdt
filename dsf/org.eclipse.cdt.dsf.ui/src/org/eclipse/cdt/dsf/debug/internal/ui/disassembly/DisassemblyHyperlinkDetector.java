/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;

/**
 * A hyperlink detector detecting words and numbers to support navigation
 * to a symbolic address.
 */
@SuppressWarnings("restriction")
class DisassemblyHyperlinkDetector extends AbstractHyperlinkDetector {

	public class DisassemblyHyperlink implements IHyperlink {

		private String fSymbol;
		private IRegion fRegion;

		/**
		 * @param symbol
		 * @param region
		 */
		public DisassemblyHyperlink(String symbol, IRegion region) {
			fSymbol= symbol;
			fRegion= region;
		}

		/*
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkRegion()
		 */
		@Override
		public IRegion getHyperlinkRegion() {
			return fRegion;
		}

		/*
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkText()
		 */
		@Override
		public String getHyperlinkText() {
			return null;
		}

		/*
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getTypeLabel()
		 */
		@Override
		public String getTypeLabel() {
			return null;
		}

		/*
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#open()
		 */
		@Override
		public void open() {
			if (fPart != null) {
 				fPart.gotoSymbol(fSymbol);
			}
		}

	}

	private DisassemblyPart fPart;

	public DisassemblyHyperlinkDetector(DisassemblyPart part) {
		fPart= part;
	}

	/*
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion, boolean)
	 */
	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		IDocument document= textViewer.getDocument();
		if (document == null) {
			return null;
		}
		IRegion wordRegion = CWordFinder.findWord(document, region.getOffset());
		if (wordRegion != null && wordRegion.getLength() != 0) {
			String word;
			try {
				word= document.get(wordRegion.getOffset(), wordRegion.getLength());
				return new IHyperlink[] { new DisassemblyHyperlink(word, wordRegion) };
			} catch (BadLocationException e) {
			}
		}
		return null;
	}

}
