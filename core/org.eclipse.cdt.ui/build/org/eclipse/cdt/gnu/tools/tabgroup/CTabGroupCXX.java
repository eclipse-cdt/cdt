/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.gnu.tools.tabgroup;

import org.eclipse.cdt.gnu.tools.tabs.CTabCompiler;
import org.eclipse.cdt.ui.builder.ACToolTabGroup;
import org.eclipse.cdt.ui.builder.ICBuildConfigDialog;
import org.eclipse.cdt.ui.builder.ICToolTab;

/**
 * Tab group for g++.
 */
public class CTabGroupCXX extends ACToolTabGroup {

	/**
	 * @see org.eclipse.cdt.ui.builder.ICToolTabGroup#createTabs(ICBuildConfigDialog)
	 */
	public void createTabs(ICBuildConfigDialog dialog) {
		ICToolTab[] tabs = new ICToolTab[] {
			new CTabCompiler()
		};
		setTabs(tabs);
	}

}
