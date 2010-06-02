/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *     Andrey Eremchenko (LEDAS)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.jface.viewers.ViewerCell;

import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICElement;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.cdt.internal.ui.viewsupport.ColoringLabelProvider;

/**
 * @author Doug Schaefer
 */
public class PDOMSearchListLabelProvider extends ColoringLabelProvider {
	private final PDOMSearchViewPage fPage;
	private final int fColumnIndex;
	
	public PDOMSearchListLabelProvider(PDOMSearchViewPage page, int columnIndex) {
		super(new PDOMSearchLabelProvider(page));
		fPage = page;
		fColumnIndex = columnIndex;
	}
	
	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		switch (fColumnIndex) {
		case PDOMSearchViewPage.LOCATION_COLUMN_INDEX:
			if (element instanceof LineSearchElement) {
				LineSearchElement lineElement = (LineSearchElement) element;
				String location = IndexLocationFactory.getPath(lineElement.getLocation()).toString();
				int lineNumber = lineElement.getLineNumber();
				cell.setText(Messages.format(CSearchMessages.CSearchResultCollector_location, location, lineNumber));
				cell.setImage(CPluginImages.get(CPluginImages.IMG_OBJS_SEARCH_LINE));
			}
			break;
		case PDOMSearchViewPage.DEFINITION_COLUMN_INDEX:
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
		case PDOMSearchViewPage.MATCH_COLUMN_INDEX:
			super.update(cell);
			cell.setImage(null);
			break;
		default:
			cell.setText(""); //$NON-NLS-1$
			break;
		}
	}

}
