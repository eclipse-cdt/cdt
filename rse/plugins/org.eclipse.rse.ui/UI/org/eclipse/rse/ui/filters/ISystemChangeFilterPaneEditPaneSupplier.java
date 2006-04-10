/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.filters;

import org.eclipse.swt.widgets.Shell;

/**
 * The SystemChangeFilterPane class is used in both SystemChangeFilterDialog and
 *  in SystemChangeFilterPropertyPage. The pane relies on both of these to supply
 *  the edit pane (for historical reasons, so we don't break previous contracts).
 *  This interface is implemented by both classes, for supplying that edit pane.
 */
public interface ISystemChangeFilterPaneEditPaneSupplier
{
	/**
	 * Return the filter string edit pane. 
	 */
	public SystemFilterStringEditPane getFilterStringEditPane(Shell shell);
}