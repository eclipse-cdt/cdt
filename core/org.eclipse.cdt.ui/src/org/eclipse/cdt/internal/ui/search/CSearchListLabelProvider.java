/*******************************************************************************
 * Copyright (c) 2006, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *     Andrey Eremchenko (LEDAS)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.cdt.internal.ui.viewsupport.ColoringLabelProvider;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.jface.viewers.ViewerCell;

/**
 * @author Doug Schaefer
 */
public class CSearchListLabelProvider extends ColoringLabelProvider {
	private final CSearchViewPage fPage;
	private final int fColumnIndex;

	public CSearchListLabelProvider(CSearchViewPage page, int columnIndex) {
		super(new CSearchLabelProvider(page));
		fPage = page;
		fColumnIndex = columnIndex;
	}

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		switch (fColumnIndex) {
		case CSearchViewPage.LOCATION_COLUMN_INDEX:
			if (element instanceof LineSearchElement) {
				LineSearchElement lineElement = (LineSearchElement) element;
				String location = IndexLocationFactory.getPath(lineElement.getLocation()).toString();
				int lineNumber = lineElement.getLineNumber();
				cell.setText(Messages.format(CSearchMessages.CSearchResultCollector_location, location, lineNumber));
				cell.setImage(CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_SEARCH_LINE));
			}
			break;
		case CSearchViewPage.DEFINITION_COLUMN_INDEX:
			if (element instanceof LineSearchElement) {
				LineSearchElement lineElement = (LineSearchElement) element;
				ICElement enclosingElement = lineElement.getMatches()[0].getEnclosingElement();
				if (fPage.isShowEnclosingDefinitions() && enclosingElement != null) {
					cell.setText(enclosingElement.getElementName());
					cell.setImage(getImage(element));
				} else {
					cell.setText(""); //$NON-NLS-1$
				}
			}
			break;
		case CSearchViewPage.MATCH_COLUMN_INDEX:
			super.update(cell);
			cell.setImage(null);
			break;
		default:
			cell.setText(""); //$NON-NLS-1$
			break;
		}
	}

}
