/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.internal.ui.IndexLabelProvider;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.swt.graphics.Image;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchTreeLabelProvider extends IndexLabelProvider {

	private final AbstractTextSearchViewPage page;
	
	public PDOMSearchTreeLabelProvider(AbstractTextSearchViewPage page) {
		this.page = page;
	}
	
	public Image getImage(Object element) {
		if (element instanceof PDOMSearchElement)
			return getImage(((PDOMSearchElement)element).getBinding());
		else
			return super.getImage(element);
	}

	public String getText(Object element) {
		if (element instanceof PDOMSearchElement) {
			int count = page.getInput().getMatchCount(element);
			return getText(((PDOMSearchElement)element).getBinding()) + " " //$NON-NLS-1$
				+ CSearchMessages.getFormattedString("CSearchResultCollector.matches", new Integer(count)); //$NON-NLS-1$
		} else
			return super.getText(element);
	}

}
