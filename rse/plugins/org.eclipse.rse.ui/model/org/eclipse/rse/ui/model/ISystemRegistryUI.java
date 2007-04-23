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
 ********************************************************************************/
package org.eclipse.rse.ui.model;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.events.ISystemResourceChangeEvent;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.ui.view.ISystemViewInputProvider;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;

/**
 * Registry or front door for all remote system connections.
 */
public interface ISystemRegistryUI 
	extends ISystemRegistry, ISystemShellProvider, ISystemViewInputProvider {

    /**
     * Returns the clipboard used for copy actions
     */
    public Clipboard getSystemClipboard();

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

	/**
	 * Notify all listeners of a change to a remote resource such as a file.
	 * This one takes the information needed and creates the event for you.
	 * @param eventType - one of the constants from {@link org.eclipse.rse.core.events.ISystemRemoteChangeEvents}
	 * @param resource - the remote resource object, or absolute name of the resource as would be given by calling getAbsoluteName on its remote adapter
	 * @param resourceParent - the remote resource's parent object, or absolute name, if that is known. If it is non-null, this will aid in refreshing occurences of that parent.
	 * @param subsystem - the subsystem which contains this remote resource. This allows the search for impacts to be 
	 *   limited to subsystems of the same parent factory, and to connections with the same hostname as the subsystem's connection.
	 * @param oldName - on a rename operation, this is the absolute name of the resource prior to the rename
	 * @param originatingViewer - optional. If set, this gives the viewer a clue that it should select the affected resource after refreshing its parent. 
	 *    This saves sending a separate event to reveal and select the new created resource on a create event, for example.
	 */
	public void fireRemoteResourceChangeEvent(int eventType, Object resource, Object resourceParent, ISubSystem subsystem, String oldName, Viewer originatingViewer);

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
