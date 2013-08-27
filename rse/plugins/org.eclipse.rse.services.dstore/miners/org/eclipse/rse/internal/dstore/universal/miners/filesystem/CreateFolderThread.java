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

public class CreateFolderThread extends SecuredThread implements ICancellableHandler {

	protected DataElement _subject;
	protected DataElement _status;
	protected UniversalFileSystemMiner _miner;
	protected String _queryType;

	protected boolean _isCancelled = false;
	protected boolean _isDone = false;
	protected SystemOperationMonitor systemOperationMonitor = new SystemOperationMonitor();

	public static final String CLASSNAME = "CreateFileThread"; //$NON-NLS-1$


	public CreateFolderThread(DataElement theElement, String queryType, UniversalFileSystemMiner miner, DataStore dataStore, DataElement status)
	{
		super(dataStore);
		this._subject = theElement;
		this._miner = miner;
		this._status = status;
		this._queryType = queryType;
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
			handleCreateFile();
		} catch (SystemMessageException e) {
			_status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
			_miner.statusDone(_status);
		}
		_isDone = true;
	}

	private DataElement handleCreateFile() throws SystemMessageException
	{
		if (_queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
			return handleCreateVirtualFolder(_subject, _status, _queryType);
		}

		File filename = null;
		if (_queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR))
		{
			if (_subject.getName().indexOf(
					ArchiveHandlerManager.VIRTUAL_SEPARATOR) > 0)
			{
				_subject.setAttribute(DE.A_TYPE,
						IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR);
				return handleCreateVirtualFolder(_subject, _status, _queryType);
			}
			else
			{
				filename = new File(_subject.getValue());
				_subject.setAttribute(DE.A_TYPE, IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR);
				_subject.setAttribute(DE.A_SOURCE, _miner.setProperties(filename));
			}
		}
		else if (_queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR))
		{
			filename = new File(_subject.getValue());
		}
		else
			UniversalServerUtilities.logError(CLASSNAME,
					"Invalid query type to handleCreateFolder", null, _dataStore); //$NON-NLS-1$

		if (filename != null)
		{
			if (filename.exists())
				_status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_EXIST);
			else
			{
		        String[] auditData = new String[] {"CREATE-FOLDER", filename.getAbsolutePath(), null, null}; //$NON-NLS-1$
		     	UniversalServerUtilities.logAudit(auditData, _dataStore);

				try {
					boolean done = filename.mkdirs();
					if (done)
					{
						_status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
						_subject.setAttribute(DE.A_SOURCE, _miner.setProperties(filename));
						_subject.setAttribute(DE.A_TYPE,IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR);
						_subject.setAttribute(DE.A_NAME, filename.getName());
						_subject.setAttribute(DE.A_VALUE, filename.getParentFile().getAbsolutePath());
					}
					else
					{
						_status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
					}

				} catch (Exception e) {
					UniversalServerUtilities.logError(CLASSNAME,
							"handleCreateFolder failed", e, _dataStore); //$NON-NLS-1$
					_status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
				}
			}
		}
		_dataStore.refresh(_subject);
		return _miner.statusDone(_status);
	}

	public DataElement handleCreateVirtualFolder(DataElement subject,
			DataElement status, String type) throws SystemMessageException {

		AbsoluteVirtualPath vpath = null;
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR)) {
			vpath = _miner.getAbsoluteVirtualPath(subject.getValue());
		} else {
			vpath = _miner.getAbsoluteVirtualPath(subject);
		}
		ISystemArchiveHandler handler = _miner.getArchiveHandlerFor(vpath
				.getContainingArchiveString());
		if (handler == null) {
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
			return _miner.statusDone(status);
		}
//		VirtualChild child = handler.getVirtualFile(vpath.getVirtualPart());
		handler.getVirtualFile(vpath.getVirtualPart(), systemOperationMonitor);
		
        String[] auditData = new String[] {"MODIFY-ARCHIVE", handler.getArchive().getAbsolutePath(), null, null}; //$NON-NLS-1$
     	UniversalServerUtilities.logAudit(auditData, _dataStore);		

		handler.createFolder(vpath.getVirtualPart(), systemOperationMonitor);

		status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR)) {
			String fullName = subject.getValue();
			String name = fullName.substring(fullName
					.lastIndexOf(File.separatorChar) + 1, fullName.length());
			String path = fullName.substring(0, fullName
					.lastIndexOf(File.separatorChar));
			subject.setAttribute(DE.A_NAME, name);
			subject.setAttribute(DE.A_VALUE, path);
			subject
					.setAttribute(DE.A_TYPE,
							IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR);
		}
		_dataStore.refresh(subject);
		return _miner.statusDone(status);
	}
}
