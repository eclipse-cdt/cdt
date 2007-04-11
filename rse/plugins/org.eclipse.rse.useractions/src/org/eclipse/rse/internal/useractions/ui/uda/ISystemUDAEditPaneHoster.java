/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.ui.uda;

//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.jface.viewers.StructuredSelection;
//import org.eclipse.jface.viewers.ISelection;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * The interface that must be implemented for any dialog or property page that wants to 
 * host a user action edit pane.
 */
public interface ISystemUDAEditPaneHoster extends ISystemMessageLine {
	/**
	 * Get the shell for this dialog or property page
	 */
	public Shell getShell();

	/**
	 * Identify that the page/dialog is complete
	 */
	public void setPageComplete(boolean complete);

	/**
	 * Set the help for the given control
	 */
	public void setHelp(Control c, String id);
}
