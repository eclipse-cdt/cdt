/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.core.settings.model.ICMultiItemsHolder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.newui.CLocationOutputTab;

/**
 * The same as CLocationOutputTab - but can be hidden
 * in case of managed project
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CBuildLocationOutputTab extends CLocationOutputTab {

	@Override
	public boolean canBeVisible() {
		if (!page.isForProject())
			return false; // for project only
		if (getResDesc() instanceof ICMultiItemsHolder)
			return false; // multi cfgs not supported for now

		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(getResDesc().getConfiguration());
		return !cfg.getBuilder().isManagedBuildOn();
	}
}
