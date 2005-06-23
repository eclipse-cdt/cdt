/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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
