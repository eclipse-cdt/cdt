/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Ed Swartz (Nokia)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import org.eclipse.core.runtime.IPath;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;

import org.eclipse.cdt.internal.ui.util.Messages;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchListLabelProvider extends PDOMSearchLabelProvider {

	public PDOMSearchListLabelProvider(AbstractTextSearchViewPage page) {
		super(page);
	}
	
	public String getText(Object element) {
		final String text= super.getText(element);
		
		if (element instanceof PDOMSearchElement) {
			PDOMSearchElement searchElement = (PDOMSearchElement)element;
			final int count= getMatchCount(element);
			final String filename = " - " + IndexLocationFactory.getPath(searchElement.getLocation()); //$NON-NLS-1$ 
			return text + filename + " " //$NON-NLS-1$
				+ Messages.format(CSearchMessages.CSearchResultCollector_matches, new Integer(count)); 
		} 
		
		if (element instanceof IIndexFileLocation) {
			IPath path= IndexLocationFactory.getPath((IIndexFileLocation)element); 
			if(path!=null) {
				return path.toString();
			}
		}
		
		return text;
	}
}
