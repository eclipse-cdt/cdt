/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.search.ui.text.Match;

/**
 * @author bgheorgh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CSearchMatch extends Match {

	private BasicSearchMatch searchMatch;
	/**
	 * @param element
	 * @param offset
	 * @param length
	 */
	public CSearchMatch(Object element, int offset, int length, IMatch match) {
		super(element, offset, length);
		if (match instanceof BasicSearchMatch)
			searchMatch = (BasicSearchMatch)match;
	}

	/**
	 * @return Returns the searchMatch.
	 */
	public BasicSearchMatch getSearchMatch() {
		return searchMatch;
	}
}
