/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
