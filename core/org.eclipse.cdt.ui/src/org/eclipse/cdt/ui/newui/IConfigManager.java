/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.core.resources.IProject;

/*
 * Implementors are intended to
 * override "Manage Configurations" dialog
 *
 * @see ConfigManager extension point.
 */
public interface IConfigManager {
	/**
	 * Checks whether objects are applicable to the manager
	 *
	 * @param obs - selected projects
	 * @return true if Configuration Management
	 *         is possible for these objects
	 */
	public boolean canManage(IProject[] obs);

	/**
	 * Displays "Manage Configurations" dialog
	 *
	 * @param obs - selected projects
	 * @param doOk - whether data saving is required
	 * @return true if user pressed OK in dialog
	 */
	public boolean manage(IProject[] obs, boolean doOk);
}
