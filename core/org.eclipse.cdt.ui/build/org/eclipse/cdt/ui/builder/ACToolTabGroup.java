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

package org.eclipse.cdt.ui.builder;
import org.eclipse.cdt.core.builder.model.ICBuildConfig;
import org.eclipse.cdt.core.builder.model.ICBuildConfigWorkingCopy;

/**
 * Base tool tab group implementation.
 */
public abstract class ACToolTabGroup implements ICToolTabGroup {

	/**
	 * The tabs in this tab group, or <code>null</code> if not yet instantiated.
	 */
	protected ICToolTab[] fTabs = null;
	
	/**
	 * @see org.eclipse.cdt.ui.builder.ICToolTabGroup#getTabs()
	 */
	public ICToolTab[] getTabs() {
		return fTabs;
	}

	/**
	 * Sets the tabs in this group
	 * 
	 * @param tabs the tabs in this group
	 */
	protected void setTabs(ICToolTab[] tabs) {
		fTabs = tabs;
	}

	/**
	 * By default, dispose all the tabs in this group.
	 * 
	 * @see org.eclipse.cdt.ui.builder.ICToolTabGroup#dispose()
	 */
	public void dispose() {
		ICToolTab[] tabs = getTabs();
		for (int i = 0; i < tabs.length; i++) {
			tabs[i].dispose();
		}
	}

	/**
	 * By default, delegate to all of the tabs in this group.
	 * 
	 * @see org.eclipse.cdt.ui.builder.ICToolTabGroup#setDefaults(ICBuildConfigWorkingCopy)
	 */
	public void setDefaults(ICBuildConfigWorkingCopy configuration) {
		ICToolTab[] tabs = getTabs();
		for (int i = 0; i < tabs.length; i++) {
			tabs[i].setDefaults(configuration);
		}		
	}

	/**
	 * By default, delegate to all of the tabs in this group.
	 * 
	 * @see org.eclipse.cdt.ui.builder.ICToolTabGroup#initializeFrom(ICBuildConfig)
	 */
	public void initializeFrom(ICBuildConfig configuration) {
		ICToolTab[] tabs = getTabs();
		for (int i = 0; i < tabs.length; i++) {
			tabs[i].initializeFrom(configuration);
		}		
	}

	/**
	 * By default, delegate to all of the tabs in this group.
	 * 
	 * @see org.eclipse.cdt.ui.builder.ICToolTabGroup#performApply(ICBuildConfigWorkingCopy)
	 */
	public void performApply(ICBuildConfigWorkingCopy configuration) {
		ICToolTab[] tabs = getTabs();
		for (int i = 0; i < tabs.length; i++) {
			tabs[i].performApply(configuration);
		}		
	}

}
