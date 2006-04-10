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

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ISelection;

public interface ISystemQuickOpenPageContainer {

	/**
	 * Returns the selection with which this container was opened.
	 *
	 * @return the selection passed to this container when it was opened
	 */
	public ISelection getSelection(); 

	/**
	 * Returns the context for the search operation.
	 * This context allows progress to be shown inside the search dialog.
	 *
	 * @return	the <code>IRunnableContext</code> for the search operation
	 */
	public IRunnableContext getRunnableContext();

	/**
	 * Sets the enable state of the perform action button
	 * of this container.
	 *
	 * @param	state	<code>true</code> to enable the button which performs the action
	 */
	 public void setPerformActionEnabled(boolean state);
}