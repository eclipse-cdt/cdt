/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is 
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

package org.eclipse.rse.ui.propertypages;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;



/**
 * interface for a property page that can be shown in the new connection wizard
 */
public interface ISystemSubSystemPropertyPageCoreForm 
{
	/**
	 * Create the GUI contents.
	 */
    public Control createContents(Composite parent, Object inputElement, Shell shell);

    
	/**
	 * Called by parent when user presses OK
	 */
	public boolean performOk();

    /**
     * Validate the form
	 * <p>
	 * Subclasses should override to do full error checking on all
	 *  the widgets on the form.
     */
    public boolean verifyFormContents();
}