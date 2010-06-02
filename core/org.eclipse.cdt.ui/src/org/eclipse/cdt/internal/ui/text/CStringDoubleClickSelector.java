/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.ui.editor.CDocumentSetupParticipant;

/**
 * Double click strategy aware of string and character syntax rules.
 *
 * @since 4.0
 */
public class CStringDoubleClickSelector extends CDoubleClickSelector {

	private String fPartitioning;
	private ITextDoubleClickStrategy fFallbackStrategy;

	/**
	 * Creates a new string double click selector for the given document partitioning.
	 *
	 * @param partitioning the document partitioning
	 */
	public CStringDoubleClickSelector(String partitioning) {
		this(partitioning, null);
	}

	/**
	 * Creates a new string double click selector for the given document partitioning.
	 *
	 * @param partitioning the document partitioning
	 * @param doubleClickStrategy  the fallback double click strategy
	 */
	public CStringDoubleClickSelector(String partitioning,
			ITextDoubleClickStrategy doubleClickStrategy) {
		fPartitioning= partitioning;
		fFallbackStrategy= doubleClickStrategy;
	}

	/*
	 * @see ITextDoubleClickStrategy#doubleClicked(ITextViewer)
	 */
	@Override
	public void doubleClicked(ITextViewer textViewer) {
		int offset= textViewer.getSelectedRange().x;

		if (offset < 0)
			return;

		IDocument document= textViewer.getDocument();

		IRegion region= matchString(document, offset);
		if (region != null) {
			if (region.getLength() >= 2) {
				textViewer.setSelectedRange(region.getOffset() + 1, region.getLength() - 2);
			}
		} else if (fFallbackStrategy != null) {
			fFallbackStrategy.doubleClicked(textViewer);
		} else {
			region= selectWord(document, offset);
			if (region != null) {
				textViewer.setSelectedRange(region.getOffset(), region.getLength());
			}
		}
	}

	private IRegion matchString(IDocument document, int offset) {
		try {
			if ((document.getChar(offset) == '"') || (document.getChar(offset) == '\'') ||
				(document.getChar(offset - 1) == '"') || (document.getChar(offset - 1) == '\''))
			{
				ITypedRegion region= TextUtilities.getPartition(document, fPartitioning, offset, true);
				// little hack: in case this strategy is used in preprocessor partitions, the string
				// partition inside the preprocessor partition must be computed in an extra step
				if (ICPartitions.C_PREPROCESSOR.equals(region.getType())) {
					String ppDirective= document.get(region.getOffset(), region.getLength());
					int hashIdx= ppDirective.indexOf('#');
					document= new Document(ppDirective.substring(hashIdx+1));
					new CDocumentSetupParticipant().setup(document);
					int delta= region.getOffset() + hashIdx + 1;
					region= TextUtilities.getPartition(document, fPartitioning, offset - delta, true);
					return new Region(region.getOffset() + delta, region.getLength());
				}
				return region;
			}
		} catch (BadLocationException e) {
		}

		return null;
	}
}
