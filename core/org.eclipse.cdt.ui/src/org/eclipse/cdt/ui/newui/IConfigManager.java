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
	 * @param obs - selected objects 
	 * @return true if Configuration Management 
	 *         is possible for these objects
	 */
	public boolean canManage(Object[] obs);
	
	/**
	 * Displays "Manage Configurations" dialog
	 * 
	 * @param obs - selected objects
	 * @param doOk - whether data saving is required
	 * @return true if user pressed OK in dialog 
	 */
	public boolean manage(Object[] obs, boolean doOk);
}
