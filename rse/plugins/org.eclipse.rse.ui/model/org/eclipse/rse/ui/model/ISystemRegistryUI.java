/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 ********************************************************************************/
package org.eclipse.rse.ui.model;

import java.util.List;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.rse.core.events.ISystemResourceChangeEvent;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.ui.view.ISystemViewInputProvider;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;

/**
 * Registry or front door for all remote system connections.
 */
public interface ISystemRegistryUI 
	extends ISystemShellProvider, ISystemViewInputProvider {

    /**
     * Returns the clipboard used for copy actions
     */
    public Clipboard getSystemClipboard();

	/**
	 * Returns the list of objects on the system clipboard
	 * @param srcType the transfer type
	 * @return the list of clipboard objects
	 */
	public List getSystemClipboardObjects(int srcType);

	/**
	 * Notify all listeners of a change to a system resource such as a connection.
	 * You would not normally call this as the methods in this class call it when appropriate.
	 * <p>
	 * This version calls fireEvent at the next reasonable opportunity, leveraging SWT's 
	 * Display.asyncExec() method.
	 */
	public void postEvent(ISystemResourceChangeEvent event);
	
	/**
	 * Notify a specific listener of a change to a system resource such as a connection.
	 * <p>
	 * This version calls fireEvent at the next reasonable opportunity, leveraging SWT's 
	 * Display.asyncExec() method.
	 */
	public void postEvent(ISystemResourceChangeListener listener, ISystemResourceChangeEvent event);

	// ----------------------------------
	// ACTIVE PROGRESS MONITOR METHODS...
	// ----------------------------------

	/**
	 * Set the current active runnable context to be used for a progress monitor
	 *  by the subsystem methods that go to the host. Called by wizards and dialogs
	 *  that have a built-in progress monitor and hence removes the need to popup
	 *  an intrusive pm dialog.
	 * <p><b>You must call clearRunnableContext when your dialog/wizard is disposed!</b>
	 * @param shell The shell of the wizard/dialog. This is recorded so it can be tested if
	 *  it is disposed before attempting to use the context
	 * @param context The dialog/wizard/view that implements IRunnableContext
	 */
	public void setRunnableContext(Shell shell, IRunnableContext context);

	/**
	 * Clear the current active runnable context to be used for a progress monitor. 
	 * Be sure to call this from you dispose method.
	 */
	public void clearRunnableContext();

	/**
	 * Return the current registered runnable context, or null if none registered. Use this
	 *  for long running operations instead of an intrusive progress monitor dialog as it is
	 *  more user friendly. Many dialogs/wizards have these built in so it behooves us to use it.
	 */
	public IRunnableContext getRunnableContext();

}
