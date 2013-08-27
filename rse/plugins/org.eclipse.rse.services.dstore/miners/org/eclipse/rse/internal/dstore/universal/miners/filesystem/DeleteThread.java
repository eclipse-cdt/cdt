/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * Xuan Chen (IBM) - [209827] Update DStore command implementation to enable cancelation of archive operations
 * Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 * Martin Oberhuber (Wind River) - [199854][api] Improve error reporting for archive handlers
 * David McKnight   (IBM)        - [264607] Unable to delete a broken symlink
 * David McKnight   (IBM)        - [321026][dstore] Broken symbolic link can't be removed
 * David McKnight   (IBM)        - [342450][dstore] Real files should not be deleted when deleting a symbolic link
 * David McKnight   (IBM)        - [392012] [dstore] make server safer for delete operations
 * David McKnight   (IBM) - [414016] [dstore] new server audit log requirements
 *******************************************************************************/
package org.eclipse.rse.internal.dstore.universal.miners.filesystem;

import java.io.File;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.server.SecuredThread;
import org.eclipse.rse.dstore.universal.miners.ICancellableHandler;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.dstore.universal.miners.UniversalFileSystemMiner;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;
import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.SystemOperationMonitor;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

public class DeleteThread extends SecuredThread implements ICancellableHandler {

	protected DataElement _theElement;
	protected DataElement _status;
	protected UniversalFileSystemMiner _miner;
	protected boolean _batch;

	protected boolean _isCancelled = false;
	protected boolean _isDone = false;
	protected SystemOperationMonitor systemOperationMonitor = new SystemOperationMonitor();

	public static final String CLASSNAME = "DeleteThread"; //$NON-NLS-1$


	public DeleteThread(DataElement theElement, UniversalFileSystemMiner miner, DataStore dataStore, boolean batch, DataElement status)
	{
		super(dataStore);
		this._theElement = theElement;
		this._miner = miner;
		this._status = status;
		this._batch = batch;
	}




	public void cancel() {
		_isCancelled = true;
		if (null != systemOperationMonitor)
		{
			systemOperationMonitor.setCancelled(true);
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
		super.run();
		try {
			if (_batch) {
				handleDeleteBatch();
			} else {
				handleDelete(_theElement, _status);
			}
		} catch (SystemMessageException e) {
			_status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
			_miner.statusDone(_status);
		}
		_isDone = true;
	}

	private DataElement handleDeleteBatch() throws SystemMessageException
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
	private DataElement handleDelete(DataElement subject, DataElement thisStatus) throws SystemMessageException
	{
		String type = subject.getType();
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)
				|| type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
			return handleDeleteFromArchive(subject, thisStatus);
		}
		else if (IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR.equals(type) ||
				IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR.equals(type) ||
				IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR.equals(type) ||
				IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR.equals(type)){			
			
			String path = subject.getAttribute(DE.A_VALUE)+ File.separatorChar + subject.getName();			
			if (path.equals(""+File.separatorChar)){ // no path provided //$NON-NLS-1$
				return _miner.statusCancelled(_status);
			}
			File deleteObj = new File(path);
			DataElement deObj = null;
	
			String attributes = subject.getSource();
			String classification = "file"; //$NON-NLS-1$
			String[] str = attributes.split("\\"+IServiceConstants.TOKEN_SEPARATOR); //$NON-NLS-1$
			if (str.length > 11){ // 11 is classification index
				classification = str[11];
			}
			boolean exists = deleteObj.exists();
			if (!exists){
				// special case for broken symbolic link
				if (classification.startsWith("broken symbolic link")){ //$NON-NLS-1$
					exists = true;
				}
			}
			
			if (!exists) {
				thisStatus.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_DOES_NOT_EXIST + "|" + deleteObj.getAbsolutePath()); //$NON-NLS-1$
				UniversalServerUtilities.logError(CLASSNAME,
						"The object to delete does not exist", null, _dataStore); //$NON-NLS-1$
			} else {
				try {
			        String[] auditData = new String[] {"DELETE", deleteObj.getAbsolutePath(), null, null};
			     	UniversalServerUtilities.logAudit(auditData, _dataStore);

					if (classification != null && classification.startsWith("symbolic link")){ //$NON-NLS-1$
						// only delete the link - no the actual file or folder contents
						deleteObj.delete();
					}
					else if (deleteObj.isFile()) {
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
									"Deletion of dir fialed", null, _dataStore); //$NON-NLS-1$
						} else {
							_dataStore.deleteObjects(subject);
							DataElement parent = subject.getParent();
							_dataStore.deleteObject(parent, subject);
							_dataStore.refresh(parent);
							thisStatus.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS + "|" + deleteObj.getAbsolutePath()); //$NON-NLS-1$
						}
					} else {
						// try to treat this as a file
						if (deleteObj.delete() == false) {
							thisStatus.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED + "|" + deleteObj.getAbsolutePath()); //$NON-NLS-1$												
							UniversalServerUtilities
									.logError(
											CLASSNAME,
											"The object to delete is neither a File or Folder! in handleDelete", //$NON-NLS-1$
											null, _dataStore);
						} else {
							// delete was successful and delete the object from the
							// datastore
							deObj = _dataStore.find(subject, DE.A_NAME, subject
									.getName(), 1);
							_dataStore.deleteObject(subject, deObj);
							thisStatus.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS + "|" + deleteObj.getAbsolutePath()); //$NON-NLS-1$
						}
						_dataStore.refresh(subject);
	
					}
				} catch (Exception e) {
					thisStatus.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_EXCEPTION + "|" + deleteObj.getAbsolutePath()); //$NON-NLS-1$
					thisStatus.setAttribute(DE.A_VALUE, e.getLocalizedMessage());
					UniversalServerUtilities.logError(CLASSNAME,
							"Delete of the object failed", e, _dataStore); //$NON-NLS-1$
				}
			}
			_dataStore.refresh(subject);
		}
		else {
			_dataStore.trace("attempt to delete "+subject + " prevented");  //$NON-NLS-1$//$NON-NLS-2$
		}
		return _miner.statusDone(_status);

	}

	public DataElement handleDeleteFromArchive(DataElement subject,
			DataElement status) throws SystemMessageException {
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
			for (int i = 0; i < list.length && !_isCancelled; ++i) {
				if (list[i].isFile()) {
					if (!(list[i].delete())) {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						UniversalServerUtilities.logWarning(CLASSNAME,
								"Deletion of dir failed", _dataStore); //$NON-NLS-1$
					}
				} else {
					deleteDir(list[i], status);
					if (!(list[i].delete())) {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						UniversalServerUtilities.logWarning(CLASSNAME,
								"Deletion of dir failed", _dataStore); //$NON-NLS-1$
					}
				}
			}
		} catch (Exception e) {
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_EXCEPTION);
			status.setAttribute(DE.A_VALUE, e.getLocalizedMessage());
			UniversalServerUtilities.logError(CLASSNAME,
					"Deletion of dir failed", e, _dataStore); //$NON-NLS-1$
		}
	}

}
