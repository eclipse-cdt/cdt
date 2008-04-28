/********************************************************************************
 * Copyright (c) 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - [216858] provide ability to import and export connections
 ********************************************************************************/

package org.eclipse.rse.internal.ui.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemProfileManager;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.persistence.RSEEnvelope;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import com.ibm.icu.text.MessageFormat;

/**
 * This action is used to import a connection from a file into the selected profile.
 */
public class SystemImportConnectionAction extends SystemBaseAction {
	
	private class ImportJob extends Job {
		
		private ISystemProfile profile = null;
		private File inFile = null;
		
		public ImportJob(File inFile, ISystemProfile profile) {
			super(SystemResources.SystemImportConnectionAction_ImportJobName);
			this.inFile = inFile;
			this.profile = profile;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {
			IStatus status = Status.OK_STATUS;
			RSEEnvelope envelope = new RSEEnvelope();
			try {
				FileInputStream in = new FileInputStream(inFile);
				envelope.get(in, monitor);
				envelope.mergeWith(profile);
			} catch (FileNotFoundException e) {
				// should not happen, log as unexpected
				SystemBasePlugin.logError(SystemResources.SystemImportConnectionAction_UnexpectedException, e);
			} catch (CoreException e) {
				// log the exception and return the status code
				status = e.getStatus();
				String message = status.getMessage();
				if (message == null) {
					message = SystemResources.SystemImportConnectionAction_CoreExceptionFound;
				}
				SystemBasePlugin.logError(message, e);
			}
			return status;
		}
		
	}
	
	/**
	 * Creates a new action to import a connection into a profile.
	 */
	public SystemImportConnectionAction() {
		super(SystemResources.RESID_IMPORT_CONNECTION_ACTION_LABEL, SystemResources.RESID_IMPORT_CONNECTION_ACTION_TOOLTIP, null);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE);
		setHelp(RSEUIPlugin.HELPPREFIX + "ActionImportConnectionDefinitions"); //$NON-NLS-1$
	}

	/**
	 * The import action can be run if the selection contains profiles or hosts.
	 * @return true if the selection contains a profile or a host.
	 */
	public boolean checkObjectType(Object obj) {
		return (obj instanceof ISystemProfile || obj instanceof IHost);
	}

	/**
	 * Called when this action is selection from the popup menu.
	 */
	public void run() {
		FileDialog openDialog = new FileDialog(shell, SWT.OPEN);
		String path = openDialog.open();
		if (path != null) {
			File inFile = new File(path);
			if (inFile.exists()) {
				if (inFile.canRead()) {
					ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
					ISystemProfileManager profileManager = registry.getSystemProfileManager();
					ISystemProfile profile = profileManager.getDefaultPrivateSystemProfile();
					IStructuredSelection selection = getSelection();
					if (selection != null && selection.size() > 0) {
						Object selected = getFirstSelection();
						if (selected instanceof IHost) {
							profile = ((IHost)selected).getSystemProfile();
						} else if (selected instanceof ISystemProfile) {
							profile = (ISystemProfile) selected;
						}
					}
					ImportJob importJob = new ImportJob(inFile, profile);
					importJob.schedule();
				} else {
					String title = SystemResources.SystemImportConnectionAction_Error;
					String message = MessageFormat.format(SystemResources.SystemImportConnectionAction_FileNotReadableCondition, new String[] {path});
					MessageDialog.openError(shell, title, message);
				}
			} else {
				String title = SystemResources.SystemImportConnectionAction_Error;
				String message = MessageFormat.format(SystemResources.SystemImportConnectionAction_FileNotFoundCondition, new String[] {path});
				MessageDialog.openError(shell, title, message);
			}
		}
	}
	
}