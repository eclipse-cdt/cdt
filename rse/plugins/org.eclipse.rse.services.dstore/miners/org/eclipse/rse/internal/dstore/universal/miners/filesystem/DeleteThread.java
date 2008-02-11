/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
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
 * Xuan Chen (IBM) - [209827] Update DStore command implementation to enable cancelation of archive operations
 ********************************************************************************/
package org.eclipse.rse.internal.dstore.universal.miners.filesystem;

import java.io.File;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.rse.dstore.universal.miners.ICancellableHandler;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.dstore.universal.miners.UniversalFileSystemMiner;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;
import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.SystemOperationMonitor;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler;

public class DeleteThread extends Thread implements ICancellableHandler {

	protected DataElement _theElement;
	protected DataElement _status;
	private DataStore _dataStore;
	protected UniversalFileSystemMiner _miner;
	protected boolean _batch;
	
	protected boolean _isCancelled = false;
	protected boolean _isDone = false;
	protected SystemOperationMonitor systemOperationMonitor = new SystemOperationMonitor();
	
	public static final String CLASSNAME = "DeleteThread"; //$NON-NLS-1$
	
	
	public DeleteThread(DataElement theElement, UniversalFileSystemMiner miner, DataStore dataStore, boolean batch, DataElement status)
	{
		this._theElement = theElement;
		this._miner = miner;
		this._dataStore = dataStore;
		this._status = status;
		this._batch = batch;
	}
	
	
	

	public void cancel() {
		_isCancelled = true;
		if (null != systemOperationMonitor)
		{
			systemOperationMonitor.setCanceled(true);
		}
	}

	public boolean isCancelled() {
		return _isCancelled;
	}

	public boolean isDone() {
		return _isDone;
	}
	
	public void run()
	{
		if (_batch)
		{
			handleDeleteBatch();
		}
		else
		{
			handleDelete(_theElement, _status);
		}
		_isDone = true;
	}
	
	private DataElement handleDeleteBatch()
	{
		DataElement substatus = _dataStore.createObject(null, "status", "substatus"); //$NON-NLS-1$ //$NON-NLS-2$
		int numOfSources = _theElement.getNestedSize() - 2;
		for (int i = 0; i < numOfSources; i++)
		{
			if (isCancelled())
			{
				return _miner.statusCancelled(_status);
			}
			DataElement subject = _miner.getCommandArgument(_theElement, i+1);
			handleDelete(subject, substatus);
			/*
			if (!substatus.getSource().startsWith(IServiceConstants.SUCCESS)) 
			{
				status.setAttribute(DE.A_SOURCE, substatus.getSource());
				return statusDone(status);
			}
			*/
		}
		_status.setAttribute(DE.A_SOURCE, substatus.getSource());
		return _miner.statusDone(_status);
	}
	private DataElement handleDelete(DataElement subject, DataElement thisStatus)
	{

		String type = subject.getType();
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)
				|| type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
			return handleDeleteFromArchive(subject, thisStatus);
		}

		File deleteObj = new File(subject.getAttribute(DE.A_VALUE)
				+ File.separatorChar + subject.getName());
		DataElement deObj = null;
		if (!deleteObj.exists()) {
			thisStatus.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_DOES_NOT_EXIST + "|" + deleteObj.getAbsolutePath()); //$NON-NLS-1$
			UniversalServerUtilities.logError(CLASSNAME,
					"The object to delete does not exist", null); //$NON-NLS-1$
		} else {
			try {
				if (deleteObj.isFile()) {
					if (deleteObj.delete() == false) {
						thisStatus.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED + "|" + deleteObj.getAbsolutePath()); //$NON-NLS-1$
					} else {
						// delete was successful and delete the object from the
						// datastore
						deObj = _dataStore.find(subject, DE.A_NAME, subject
								.getName(), 1);
						_dataStore.deleteObject(subject, deObj);
						thisStatus.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS + "|" + deleteObj.getAbsolutePath()); //$NON-NLS-1$
					}
					_dataStore.refresh(subject);
				} else if (deleteObj.isDirectory()) { // it is directory and
													  // need to delete the
													  // entire directory +
					// children
					deleteDir(deleteObj, thisStatus);
					if (deleteObj.delete() == false) {
						thisStatus.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED + "|" + deleteObj.getAbsolutePath()); //$NON-NLS-1$
						UniversalServerUtilities.logError(CLASSNAME,
								"Deletion of dir fialed", null); //$NON-NLS-1$
					} else {
						_dataStore.deleteObjects(subject);
						DataElement parent = subject.getParent();
						_dataStore.deleteObject(parent, subject);
						_dataStore.refresh(parent);
						thisStatus.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS + "|" + deleteObj.getAbsolutePath()); //$NON-NLS-1$
					}
				} else {
					UniversalServerUtilities
							.logError(
									CLASSNAME,
									"The object to delete is neither a File or Folder! in handleDelete", //$NON-NLS-1$
									null);
				}
			} catch (Exception e) {
				thisStatus.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_EXCEPTION + "|" + deleteObj.getAbsolutePath()); //$NON-NLS-1$
				thisStatus.setAttribute(DE.A_VALUE, e.getLocalizedMessage());
				UniversalServerUtilities.logError(CLASSNAME,
						"Delete of the object failed", e); //$NON-NLS-1$
			}
		}
		_dataStore.refresh(subject);
		return _miner.statusDone(_status);
	
	}
	
	public DataElement handleDeleteFromArchive(DataElement subject,
			DataElement status) {
		String type = subject.getType();
		DataElement deObj = null;

		AbsoluteVirtualPath vpath = _miner.getAbsoluteVirtualPath(subject);
		if (vpath != null) {
			ArchiveHandlerManager archiveHandlerManager = ArchiveHandlerManager.getInstance();
			ISystemArchiveHandler handler = archiveHandlerManager.getRegisteredHandler(new File(vpath.getContainingArchiveString()));
			if (handler == null || !handler.delete(vpath.getVirtualPart(), systemOperationMonitor)) {
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED + "|" + vpath.toString()); //$NON-NLS-1$
				_dataStore.refresh(subject);
				return _miner.statusDone(status);
			}

			if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)) {
				deObj = _dataStore.find(subject, DE.A_NAME, subject.getName(),
						1);
				_dataStore.deleteObject(subject, deObj);
				status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
			} else if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
				_dataStore.deleteObjects(subject);
				DataElement parent = subject.getParent();
				_dataStore.deleteObject(parent, subject);
				_dataStore.refresh(parent);
				status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
			}
		}

		_dataStore.refresh(subject);
		return _miner.statusDone(status);
	}
	
	/**
	 * Delete directory and its children.
	 *  
	 */
	public void deleteDir(File fileObj, DataElement status) {
		try {
			File list[] = fileObj.listFiles();
			for (int i = 0; i < list.length; ++i) {
				if (list[i].isFile()) {
					if (!(list[i].delete())) {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						UniversalServerUtilities.logWarning(CLASSNAME,
								"Deletion of dir failed"); //$NON-NLS-1$
					}
				} else {
					deleteDir(list[i], status);
					if (!(list[i].delete())) {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						UniversalServerUtilities.logWarning(CLASSNAME,
								"Deletion of dir failed"); //$NON-NLS-1$
					}
				}
			}
		} catch (Exception e) {
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_EXCEPTION);
			status.setAttribute(DE.A_VALUE, e.getLocalizedMessage());
			UniversalServerUtilities.logError(CLASSNAME,
					"Deletion of dir failed", e); //$NON-NLS-1$
		}
	}
	
}
