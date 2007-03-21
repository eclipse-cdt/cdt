/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.core.settings.model.ICProjectDescription;

/**
 * Represents class which is able to display
 * "New configuration" dialog instead of standard one
 * and, if user pressed OK, create new configuration
 * on a basis of its internal data
 * 
 * used by extension point:
 * "org.eclipse.cdt.ui.newCfgDialog" 
 */
public interface INewCfgDialog {
	// Project to work with (set before open() !)
	void setProject(ICProjectDescription prj);
	// Title of dialog box (set before open() !)
	void setTitle(String title);
	// Shell to create dialog (set before open() !)
	void setShell(Shell shell);
	// Opens dialog and (after user presses OK)
	// creates new configuration. 
	// Returns Windows.OK on success. 
	int open();
}
