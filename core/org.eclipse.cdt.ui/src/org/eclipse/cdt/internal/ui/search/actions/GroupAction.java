/*
 * Created on May 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.cdt.internal.ui.search.CSearchResultPage;
import org.eclipse.jface.action.Action;

public class GroupAction extends Action {
	private int _grouping;
	private CSearchResultPage _page;
	
	public GroupAction(String label, String tooltip, CSearchResultPage page, int grouping) {
		super(label);
		setToolTipText(tooltip);
		_page= page;
		_grouping= grouping;
	}

	public void run() {
		_page.setGrouping(_grouping);
	}

	public int getGrouping() {
		return _grouping;
	}
}
