/*
 * Created on May 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.cdt.internal.ui.search.CSearchResultPage;
import org.eclipse.jface.action.Action;

public class SortAction extends Action {

		private int fSortOrder;
		private CSearchResultPage fPage;
		
		public SortAction(String label, CSearchResultPage page, int sortOrder) {
			super(label);
			fPage= page;
			fSortOrder= sortOrder;
		}

		public void run() {
			fPage.setSortOrder(fSortOrder);
		}

		public int getSortOrder() {
			return fSortOrder;
		}

}
