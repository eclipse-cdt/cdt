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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.internal.persistence.RSEEnvelope;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.persistence.IRSEPersistenceManager;
import org.eclipse.rse.persistence.IRSEPersistenceProvider;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import com.ibm.icu.text.MessageFormat;

/**
 * This is the action for exporting a connection to a file.
 */
public class SystemExportConnectionAction extends SystemBaseAction {
	
	private class ExportJob extends Job {
		
		private File outFile;
		private IHost host;
		
		public ExportJob(IHost host, File outFile) {
			super(SystemResources.SystemExportConnectionAction_ExportJobName);
			this.outFile = outFile;
			this.host = host;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {
			IStatus status = Status.OK_STATUS;
			IRSEPersistenceManager manager = RSECorePlugin.getThePersistenceManager();
			IRSEPersistenceProvider persistenceProvider = manager.getPersistenceProvider("org.eclipse.rse.persistence.PropertyFileProvider"); //$NON-NLS-1$
			RSEEnvelope envelope = new RSEEnvelope();
			envelope.add(host);
			try {
				FileOutputStream out = new FileOutputStream(outFile);
				envelope.put(out, persistenceProvider, monitor);
				out.close();
			} catch (FileNotFoundException e) {
				// should not happen, log as unexpected
				SystemBasePlugin.logError(SystemResources.SystemExportConnectionAction_UnexpectedException, e);
			} catch (CoreException e) {
				// log the exception and return the status code
				SystemBasePlugin.logError(SystemResources.SystemExportConnectionAction_CoreExceptionFound, e);
				status = e.getStatus();
			} catch (IOException e) {
				// should not happend, log as unexpected
				SystemBasePlugin.logError(SystemResources.SystemExportConnectionAction_UnexpectedException, e);
			}
			return status;
		}
		
	}
	
	/**
	 * Constructor.
	 */
	public SystemExportConnectionAction() {
		super(SystemResources.RESID_EXPORT_CONNECTIONS_ACTION_LABEL, SystemResources.RESID_EXPORT_CONNECTION_ACTIONS_TOOLTIP, null);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE);
		setHelp(RSEUIPlugin.HELPPREFIX + "ActionExportConnectionDefinitions"); //$NON-NLS-1$
	}

	/**
	 * The export connection action is enabled when the selection contains only connections (hosts)
	 */
	public boolean checkObjectType(Object obj) {
		boolean result = obj instanceof IHost;
		return result;
	}
	
	/**
	 * Called when this action is selection from the popup menu.
	 */
	public void run() {
		FileDialog saveDialog = new FileDialog(shell, SWT.SAVE);
		String path = saveDialog.open();
		if (path != null) {
			File outFile = new File(path);
			boolean ok = true;
			if (outFile.exists()) {
				if (outFile.canWrite()) {
					String title = SystemResources.SystemExportConnectionAction_Warning;
					String message = MessageFormat.format(SystemResources.SystemExportConnectionAction_OverwriteFileCondition, new String[] {path});
					ok = MessageDialog.openConfirm(shell, title, message);
				} else {
					String title = SystemResources.SystemExportConnectionAction_Error;
					String message = MessageFormat.format(SystemResources.SystemExportConnectionAction_WriteProtectedFileCondition, new String[] {path});
					MessageDialog.openError(shell, title, message);
					ok = false;
				}
			}
			if (ok) {
				IStructuredSelection selection = getSelection();
				Assert.isTrue(selection.size() == 1, "selection size should be one"); //$NON-NLS-1$
				IHost host = (IHost) selection.getFirstElement();
				Job exportJob = new ExportJob(host, outFile);
				exportJob.schedule();
			}
		}
	}
	
}