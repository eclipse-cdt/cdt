/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/package org.eclipse.cdt.managedbuilder.ui.newui;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.newui.CLocationOutputTab;

/**
 * The same as CLocationOutputTab - but can be hidden 
 * in case of managed project 
 */
public class CBuildLocationOutputTab extends CLocationOutputTab {

	public boolean canBeVisible() {
		if (!page.isForProject()) return false;
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(getResDesc().getConfiguration());
		return !cfg.getBuilder().isManagedBuildOn();
	}
}
