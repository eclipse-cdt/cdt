/********************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - [226561] add API markup to javadoc
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 ********************************************************************************/

package org.eclipse.rse.ui.filters;

import org.eclipse.swt.widgets.Shell;

/**
 * The SystemChangeFilterPane class is used in both SystemChangeFilterDialog and
 * in SystemChangeFilterPropertyPage. The pane relies on both of these to supply
 * the edit pane (for historical reasons, so we don't break previous contracts).
 * This interface is implemented by both classes, for supplying that edit pane.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients. The
 *           standard implementations are included in the framework.
 */
public interface ISystemChangeFilterPaneEditPaneSupplier
{
	/**
	 * Return the filter string edit pane.
	 */
	public SystemFilterStringEditPane getFilterStringEditPane(Shell shell);
}