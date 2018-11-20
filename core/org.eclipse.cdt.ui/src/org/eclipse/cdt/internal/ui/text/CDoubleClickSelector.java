/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

/**
 * Double click strategy aware of C identifier syntax rules.
 */
public class CDoubleClickSelector implements ITextDoubleClickStrategy {

	protected static char[] fgBrackets = { '{', '}', '(', ')', '[', ']', '<', '>' };
	private CPairMatcher fPairMatcher = new CPairMatcher(fgBrackets);

	public CDoubleClickSelector() {
		super();
	}

	/*
	 * @see org.eclipse.jface.text.ITextDoubleClickStrategy#doubleClicked(org.eclipse.jface.text.ITextViewer)
	 */
	@Override
	public void doubleClicked(ITextViewer textViewer) {
		int offset = textViewer.getSelectedRange().x;

		if (offset < 0)
			return;

		IDocument document = textViewer.getDocument();

		IRegion region = fPairMatcher.match(document, offset);
		if (region != null && region.getLength() >= 2) {
			textViewer.setSelectedRange(region.getOffset() + 1, region.getLength() - 2);
		} else {
			region = selectWord(document, offset);
			if (region != null && region.getLength() > 0) {
				textViewer.setSelectedRange(region.getOffset(), region.getLength());
			}
		}
	}

	protected IRegion selectWord(IDocument document, int offset) {
		return CWordFinder.findWord(document, offset);
	}

}
