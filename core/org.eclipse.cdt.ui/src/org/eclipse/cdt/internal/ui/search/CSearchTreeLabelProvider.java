/*******************************************************************************
 * Copyright (c) 2006, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *     Andrey Eremchenko (LEDAS)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.jface.viewers.StyledString;

public class CSearchTreeLabelProvider extends CSearchLabelProvider {

	public CSearchTreeLabelProvider(CSearchViewPage page) {
		super(page);
	}

	@Override
	public String getText(Object element) {
		final String text = super.getText(element);
		final int count = getMatchCount(element);
		if (count <= 1) {
			return text;
		}
		return text + " " //$NON-NLS-1$
				+ Messages.format(CSearchMessages.CSearchResultCollector_matches, Integer.valueOf(count));
	}

	@Override
	public StyledString getStyledText(Object element) {
		if (element instanceof TranslationUnit) {
			StyledString styled = new StyledString(super.getText(element));
			final int count = getMatchCount(element);
			if (count > 1) {
				final String matchesCount = " " //$NON-NLS-1$
						+ Messages.format(CSearchMessages.CSearchResultCollector_matches, Integer.valueOf(count));
				styled.append(matchesCount, StyledString.COUNTER_STYLER);
				return styled;
			}
		}
		if (!(element instanceof LineSearchElement))
			return new StyledString(getText(element));
		LineSearchElement lineElement = (LineSearchElement) element;
		String enclosingName = ""; //$NON-NLS-1$
		ICElement enclosingElement = lineElement.getMatches()[0].getEnclosingElement();
		if (fPage.isShowEnclosingDefinitions() && enclosingElement != null) {
			enclosingName = getElementDescription(enclosingElement) + ", "; //$NON-NLS-1$
		}
		Integer lineNumber = lineElement.getLineNumber();
		String prefix = Messages.format(CSearchMessages.CSearchResultCollector_line, enclosingName, lineNumber);
		prefix += ":  "; //$NON-NLS-1$
		StyledString location = new StyledString(prefix, StyledString.QUALIFIER_STYLER);
		return location.append(super.getStyledText(element));
	}

	private String getElementDescription(ICElement element) {
		ICElement parent = element.getParent();
		if (parent instanceof IStructure) {
			return parent.getElementName() + "::" + element.getElementName(); //$NON-NLS-1$
		}
		return element.getElementName();
	}
}
