/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.view;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;

/**
 * This interface defines some common functionality required from all remote
 * resources for edit, irrespective of whether the remote system is an
 * OS/400, Windows, Linux or Unix operating system.
 */
public interface ISystemEditableRemoteObject
{

	public static final int NOT_OPEN = -1;
	public static final int OPEN_IN_SAME_PERSPECTIVE = 0;
	public static final int OPEN_IN_DIFFERENT_PERSPECTIVE = 1;
	
	/**
	 * Check if user has write authority to the file.
	 * @return true if user has write authority to the file, false otherwise
	 */
	public boolean isReadOnly();

	/**
	 * Indicate whether the file can be edited
	 */
	public void setReadOnly(boolean isReadOnly);

	/**
	 * Set the editor variable given an exiting editor part
	 * @param editorPart the editor
	 */
	public void setEditor(IEditorPart editorPart);

	/**
	 * Download the file.
	 * @param if the shell is null, no progress monitor will be shown
	 * @return true if successful, false if cancelled
	 */
	public boolean download(Shell shell) throws Exception;

	/**
	 * Download the file.
	 * @param the progress monitor
	 * @returns true if the operation was successful.  false if the user cancels.
	 */
	public boolean download(IProgressMonitor monitor) throws Exception;
	
	/**
	 * Saves the local file and uploads it to the host immediately, rather than, in response to a resource change
	 * event.
	 * @returns true if the operation was successful. false if the upload fails.
	 */
	public boolean doImmediateSaveAndUpload();

	/**
	 * Get the local resource
	 */
	public IFile getLocalResource();
		
	/**
	 * Is the local file open in an editor
	 */
	public int checkOpenInEditor() throws CoreException;

	/**
	 * Returns the open IEditorPart for this remote object if there is one.
	 */
	public IEditorPart getEditorPart();

	/**
	 * Returns the remote object that is editable
	 */
	public IAdaptable getRemoteObject();

	
	
	/**
	 * Open in editor
	 */
	public void open(Shell shell);
	
	/**
	 * Open in editor
	 */
	public void open(Shell shell, boolean readOnly);

	
	/**
	 * Set local resource properties
	 */
	public void setLocalResourceProperties() throws Exception;

	/**
	 * Register as listener for various events
	 */
	public void addAsListener();	

	/**
	 * Open the editor
	 */
	public void openEditor() throws PartInitException;

	/**
	 * Update the editor dirty indicator
	 */
	public void updateDirtyIndicator();
	
	/**
	 *  Check if the file is dirty
	 */
	public boolean isDirty();
	
	
	/**
	 * Return the absolute path on the remote system
	 * @return
	 */
	public String getAbsolutePath();
	
	/**
	 * Return the subsystem for the edited object
	 * @return
	 */
	public ISubSystem getSubSystem();
	
	/**
	 * Returns whether the edited object exists
	 * @return
	 */
	public boolean exists();
	
	/**
	 * Returns whether the underlying resource needs to be updated from the host
	 */
	public boolean isStale();
}