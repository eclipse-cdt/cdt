/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Ed Swartz (Nokia)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;

import org.eclipse.cdt.internal.core.model.TranslationUnit;

import org.eclipse.cdt.internal.ui.util.Messages;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchTreeLabelProvider extends PDOMSearchLabelProvider {
	
	public PDOMSearchTreeLabelProvider(AbstractTextSearchViewPage page) {
		super(page);
	}
	
	@Override
	public String getText(Object element) {
		final String text= super.getText(element);
		final int count= getMatchCount(element);
		if (count <= 1) {
			return text;
		}
		return text + " " //$NON-NLS-1$
				+ Messages.format(CSearchMessages.CSearchResultCollector_matches, new Integer(count)); 
	}

	@Override
	public StyledString getStyledText(Object element) {
		if (element instanceof TranslationUnit) {
			StyledString styled = new StyledString(super.getText(element));
			final int count= getMatchCount(element);
			if (count > 1) {
				final String matchesCount = " " //$NON-NLS-1$
					+ Messages.format(CSearchMessages.CSearchResultCollector_matches, new Integer(count));
				styled.append(matchesCount, StyledString.COUNTER_STYLER);
				return styled;
			}
		}
		if (element instanceof LineSearchElement) {
			LineSearchElement lineElement = (LineSearchElement) element;
			int lineNumber = lineElement.getLineNumber();
			String lineNumberString = Messages.format("{0}: ", Integer.valueOf(lineNumber)); //$NON-NLS-1$
			StyledString styled = new StyledString(lineNumberString, StyledString.QUALIFIER_STYLER);
			return styled.append(super.getStyledText(element));
		}
		return new StyledString(getText(element));
	}
}
