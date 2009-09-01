/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [174945] split importexport icons from rse.ui
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [216252] MessageFormat.format -> NLS.bind
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Takuya Miyamoto - [185925] Integrate Platform/Team Synchronization
 * David McKnight   (IBM)        - [287946] Export, save in rexpfd does not work any more
 *******************************************************************************/
package org.eclipse.rse.internal.importexport.files;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.internal.importexport.IRemoteImportExportConstants;
import org.eclipse.rse.internal.importexport.RemoteImportExportPlugin;
import org.eclipse.rse.internal.importexport.RemoteImportExportProblemDialog;
import org.eclipse.rse.internal.importexport.RemoteImportExportResources;
import org.eclipse.rse.internal.synchronize.SynchronizeData;
import org.eclipse.rse.internal.synchronize.provisional.ISynchronizeOperation;
import org.eclipse.rse.internal.synchronize.provisional.SynchronizeOperation;
import org.eclipse.rse.internal.synchronize.provisional.Synchronizer;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.ui.PlatformUI;

/**
 * This class is a remote file export action.
 */
public class RemoteFileExportActionDelegate extends RemoteFileImportExportActionDelegate {
	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IFile[] descriptions = getDescriptionFiles(getSelection());
		MultiStatus mergedStatus;
		int length = descriptions.length;
		if (length < 1) {
			return;
		}
		// create read multi status
		String message;
		if (length > 1) {
			message = RemoteImportExportResources.IMPORT_EXPORT_ERROR_CREATE_FILES_FAILED;
		} else {
			message = RemoteImportExportResources.IMPORT_EXPORT_ERROR_CREATE_FILE_FAILED;
		}
		MultiStatus readStatus = new MultiStatus(RemoteImportExportPlugin.getDefault().getSymbolicName(), 0, message, null);
		RemoteFileExportData[] exportDatas = readExportDatas(descriptions, readStatus);
		if (exportDatas.length > 0) {
			IStatus status = export(exportDatas);
			if (status == null) {
				return;
			}
			if (readStatus.getSeverity() == IStatus.ERROR) {
				message = readStatus.getMessage();
			} else {
				message = status.getMessage();
			}
			// create new status because we want another message - no API to set message
			mergedStatus = new MultiStatus(RemoteImportExportPlugin.getDefault().getSymbolicName(), status.getCode(), readStatus.getChildren(), message, null);
			mergedStatus.merge(status);
		} else {
			mergedStatus = readStatus;
		}
		if (!mergedStatus.isOK()) {
			RemoteImportExportProblemDialog.open(getShell(), RemoteImportExportResources.IMPORT_EXPORT_EXPORT_ACTION_DELEGATE_TITLE, null, mergedStatus);
		}
	}

	private RemoteFileExportData[] readExportDatas(IFile[] descriptions, MultiStatus readStatus) {
		List exportDataList = new ArrayList(descriptions.length);
		for (int i = 0; i < descriptions.length; i++) {
			RemoteFileExportData exportData = readExportData(descriptions[i], readStatus);
			if (exportData != null) {
				exportDataList.add(exportData);
			}
		}
		return (RemoteFileExportData[]) exportDataList.toArray(new RemoteFileExportData[exportDataList.size()]);
	}

	/**
	 * Reads the file export data from a file.
	 */
	protected RemoteFileExportData readExportData(IFile description, MultiStatus readStatus) {
		Assert.isLegal(description.isAccessible());
		Assert.isNotNull(description.getFileExtension());
		Assert.isLegal(description.getFileExtension().equals(Utilities.EXPORT_DESCRIPTION_EXTENSION));
		RemoteFileExportData exportData = new RemoteFileExportData();
		IRemoteFileExportDescriptionReader reader = null;
		try {
			reader = exportData.createExportDescriptionReader(description.getContents());
			// read export data
			reader.read(exportData);
			// do not save settings again
			exportData.setSaveSettings(false);
		} catch (CoreException ex) {
			String message = NLS.bind(RemoteImportExportResources.IMPORT_EXPORT_ERROR_DESCRIPTION_READ, description.getFullPath(), ex.getStatus().getMessage());
			addToStatus(readStatus, message, ex);
			return null;
		} finally {
			if (reader != null) {
				readStatus.addAll(reader.getStatus());
			}
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (CoreException ex) {
				String message = NLS.bind(RemoteImportExportResources.IMPORT_EXPORT_ERROR_DESCRIPTION_CLOSE, description.getFullPath());
				addToStatus(readStatus, message, ex);
			}
		}
		return exportData;
	}

	private IStatus export(RemoteFileExportData[] exportDatas) {
		// Add re-running synchronize operation
		for (int i = 0; i < exportDatas.length; i++) {			
			RemoteFileExportData exportData = exportDatas[i];
			if (exportData.isReviewSynchronize()){
			
				SynchronizeData data = new SynchronizeData(exportData);
				data.setSynchronizeType(ISynchronizeOperation.SYNC_MODE_UI_REVIEW);
				new Synchronizer(data).run(new SynchronizeOperation());
			}
			else {
				IStatus status = null;
				RemoteFileExportOperation op = new RemoteFileExportOperation(exportDatas[i], new RemoteFileOverwriteQuery());
				try {
					PlatformUI.getWorkbench().getProgressService().run(true, true, op);
					status = op.getStatus();
				} catch (InvocationTargetException e) {
					SystemBasePlugin.logError("Error occured trying to export", e); //$NON-NLS-1$
					status = new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getBundle().getSymbolicName(), 0, "", e); //$NON-NLS-1$
				} catch (InterruptedException e) {
					SystemBasePlugin.logError("Error occured trying to export", e); //$NON-NLS-1$
					status = new Status(IStatus.OK, RemoteImportExportPlugin.getDefault().getBundle().getSymbolicName(), 0, "", e); //$NON-NLS-1$
				}
				if (!status.isOK()) {
					String msgTxt = NLS.bind(RemoteImportExportResources.FILEMSG_EXPORT_FAILED, status);
	
					SystemMessage msg = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID,
							IRemoteImportExportConstants.FILEMSG_EXPORT_FAILED,
							IStatus.ERROR, msgTxt);
	
					SystemMessageDialog dlg = new SystemMessageDialog(getShell(), msg);
					dlg.openWithDetails();
					return null;
				}
			}
		}
		return null;
	}

	protected void addToStatus(MultiStatus multiStatus, String defaultMessage, CoreException ex) {
		IStatus status = ex.getStatus();
		String message = ex.getLocalizedMessage();
		if (message == null || message.length() < 1) {
			status = new Status(status.getSeverity(), status.getPlugin(), status.getCode(), defaultMessage, ex);
		}
		multiStatus.add(status);
	}
}
