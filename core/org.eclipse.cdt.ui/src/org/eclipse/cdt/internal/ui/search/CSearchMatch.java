/*
 * Created on Jun 27, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
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
