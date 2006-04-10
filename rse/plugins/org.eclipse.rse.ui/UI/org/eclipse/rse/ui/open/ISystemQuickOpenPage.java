/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.open;

import org.eclipse.jface.dialogs.IDialogPage;

/**
 * Defines a page inside the quick open dialog.
 * Clients can contribute their own quick open page to the
 * dialog by implementing this interface, typically as a subclass
 * of <code>DialogPage</code>.
 * <p>
 * The quick open dialog calls the <code>performAction</code> method when the Ok
 * button is pressed.
 * <p>
 *
 * @see org.eclipse.jface.dialogs.IDialogPage
 * @see org.eclipse.jface.dialogs.DialogPage
 */
public interface ISystemQuickOpenPage extends IDialogPage {

	/**
	 * Performs the action for this page.
	 * The quick open dialog calls this method when the Ok button is pressed.
	 * @return <code>true</code> if the dialog can be closed after execution.
	 */
	public boolean performAction();

	/**
	 * Sets the container of this page.
	 * The quick open dialog calls this method to initialize this page.
	 * Implementations may store the reference to the container.
	 * @param container the container for this page.
	 */
	public void setContainer(ISystemQuickOpenPageContainer container);
}