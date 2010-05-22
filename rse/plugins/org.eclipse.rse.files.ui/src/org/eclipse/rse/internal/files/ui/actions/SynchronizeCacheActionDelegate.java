/********************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 * David McKnight    (IBM)    [143503] [updating] need a synchronize cache operation
 * David McKnight     (IBM)      - [276534] Cache Conflict After Synchronization when Browsing Remote System with Case-Differentiated-Only Filenames
 ********************************************************************************/
package org.eclipse.rse.internal.files.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.core.model.ISystemResourceSet;
import org.eclipse.rse.core.model.SystemRemoteResourceSet;
import org.eclipse.rse.internal.files.ui.Activator;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 *  This action downloads remote files of a directory to the temp file cache 
 *  if the lastest versions have not yet been cached.
 *
 */
public class SynchronizeCacheActionDelegate implements IActionDelegate {

	protected IStructuredSelection fSelection;
	private IStatus errorStatus;
	private SystemMessage systemMessage;
	
	public SynchronizeCacheActionDelegate() {
	}

	public void run(IAction action) {
		errorStatus = null;
		systemMessage = null;

		IRemoteFile[] files = getRemoteFiles(fSelection);
		boolean completed = performCacheRemoteFiles(files);

        if (!completed) {
			return; // not appropriate to show errors
		}

        // If errors occurred, open an Error dialog
        if (errorStatus != null) {
            ErrorDialog.openError(getShell(), FileResources.MESSAGE_ERROR_CACHING_REMOTE_FILES, null, errorStatus);
            errorStatus = null;
        }
        else if (systemMessage != null){
        	SystemMessageDialog dlg = new SystemMessageDialog(getShell(), systemMessage);
        	dlg.open();
        	systemMessage = null;
        }
	}

	private void cacheRemoteFiles(IRemoteFile[] files, IProgressMonitor monitor) throws SystemMessageException
	{
		SystemRemoteResourceSet[] sets = getResourceSetsFor(files);
		for (int i = 0; i < sets.length; i++){
			SystemRemoteResourceSet set = sets[i];
			ISystemResourceSet resultSet = set.getAdapter().doDrag(set, monitor);
			
			systemMessage = resultSet.getMessage();
		}
	}
	
	private SystemRemoteResourceSet[] getResourceSetsFor(IRemoteFile[] files)
	{
		ISystemViewElementAdapter adapter = null;
		Map sets = new HashMap();
		for (int i = 0; i < files.length; i++){
			IRemoteFile file = files[i];		
			if (adapter == null){
				adapter = (ISystemViewElementAdapter)((IAdaptable)file).getAdapter(ISystemViewElementAdapter.class);
			}
			IRemoteFileSubSystem ss = file.getParentRemoteFileSubSystem();
			SystemRemoteResourceSet set = (SystemRemoteResourceSet)sets.get(ss);
			if (set == null){
				set = new SystemRemoteResourceSet(ss, adapter);
				sets.put(ss, set);
			}
			set.addResource(file);
		}
		Iterator iterator = sets.values().iterator();			
		List results = new ArrayList();
		while (iterator.hasNext()){
			results.add(iterator.next());
		}
		return (SystemRemoteResourceSet[])results.toArray(new SystemRemoteResourceSet[results.size()]);
	}
		
	boolean performCacheRemoteFiles(final IRemoteFile[] files) {
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					// download all files that need to be cached
			        cacheRemoteFiles(files, monitor);
				}
				catch (Exception e) {
					if (e.getCause() instanceof CoreException) {
						recordError((CoreException)e.getCause());
					} 
					else {
						Activator.getDefault().getLog().log(new Status(IStatus.ERROR,
								Activator.getDefault().getBundle().getSymbolicName(),
								-1, e.getMessage(), e));
						displayError(e.getMessage());
					}
				}
			}
		};

		try {
			//TODO make this a Job an run in foreground with option to send to background
			ProgressMonitorDialog mon = new ProgressMonitorDialog(getShell()) {
			    protected void configureShell(Shell shell) {
			        super.configureShell(shell);
					shell.setText(FileResources.MESSAGE_SYNCHRONIZING_REMOTE_FILE_CACHE);
			    }
			};
			mon.run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			displayError("Internal Error: "+e.getTargetException().getMessage()); //$NON-NLS-1$
			return false;
		}

		return true;
	}
	

	/**
	 * Opens an error dialog to display the given message.
	 * <p>
	 * Note that this method must be called from UI thread.
	 * </p>
	 * 
	 * @param message
	 *            the message
	 */
	void displayError(String message) {
		MessageDialog.openError(getShell(), FileResources.MESSAGE_ERROR_CACHING_REMOTE_FILES, message);
	}

	
	/**
	 * Records the core exception to be displayed to the user once the action is
	 * finished.
	 * 
	 * @param error
	 *            a <code>CoreException</code>
	 */
	final void recordError(CoreException error) {
		this.errorStatus = error.getStatus();
	}
	
	/**
	 * Sets the selection. The selection is only set if given a structured selection, otherwise it is set to an
	 * empty structured selection.
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		
		if (selection instanceof IStructuredSelection) {
			fSelection = (IStructuredSelection)selection;
		}
		else {
			fSelection = StructuredSelection.EMPTY;
		}
	}
	
	/**
	 * Returns the remote files in the selection.
	 * Use this method if this action allows multiple remote file selection.
	 * @return an array of remote files.
	 */
	protected IRemoteFile[] getRemoteFiles(IStructuredSelection selection) {

		IRemoteFile[] files = new IRemoteFile[selection.size()];
		Iterator iter = selection.iterator();
		
		int i = 0;
		
		while (iter.hasNext()) {
			files[i++] = (IRemoteFile)iter.next();
		}
		
		return files;
	}
	/**
	 * Returns the workbench.
	 * @return the workbench.
	 */
	protected IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}
	
	/**
	 * Returns the active shell.
	 * @return the active shell.
	 */
	protected Shell getShell() {
		return Display.getDefault().getActiveShell();
	}

	/**
	 * Returns the selection.
	 * @return the selection.
	 */
	protected IStructuredSelection getSelection() {
		return fSelection;
	}
}
