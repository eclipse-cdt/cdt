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
import org.eclipse.rse.dstore.universal.miners.UniversalFileSystemMiner;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;
import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.SystemOperationMonitor;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

public class RenameThread extends SecuredThread implements ICancellableHandler {

	protected DataElement _subject;
	protected DataElement _status;
	protected UniversalFileSystemMiner _miner;

	protected boolean _isCancelled = false;
	protected boolean _isDone = false;
	protected SystemOperationMonitor systemOperationMonitor = new SystemOperationMonitor();

	public static final String CLASSNAME = "RenameThread"; //$NON-NLS-1$


	public RenameThread(DataElement theElement, UniversalFileSystemMiner miner, DataStore dataStore, DataElement status)
	{
		super(dataStore);
		this._subject = theElement;
		this._miner = miner;
		this._status = status;
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
			handleRename();
		} catch (SystemMessageException e) {
			_status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
			_miner.statusDone(_status);
		}
		_isDone = true;
	}

	private DataElement handleRename() throws SystemMessageException
	{
		File fileoldname = new File(_subject.getAttribute(DE.A_VALUE)
				+ File.separatorChar + _subject.getName());
		File filerename = new File(_subject.getAttribute(DE.A_SOURCE));

		if (ArchiveHandlerManager.isVirtual(fileoldname.getAbsolutePath())) {
			AbsoluteVirtualPath oldAbsPath = new AbsoluteVirtualPath(
					fileoldname.getAbsolutePath());
			AbsoluteVirtualPath newAbsPath = new AbsoluteVirtualPath(filerename
					.getAbsolutePath());
			ArchiveHandlerManager archiveHandlerManager = ArchiveHandlerManager.getInstance();
			ISystemArchiveHandler handler = archiveHandlerManager
					.getRegisteredHandler(new File(oldAbsPath
							.getContainingArchiveString()));
			if (handler != null) {
				handler.fullRename(oldAbsPath.getVirtualPart(), newAbsPath.getVirtualPart(), systemOperationMonitor);
				_subject.setAttribute(DE.A_NAME, filerename.getName());
				_subject.setAttribute(DE.A_SOURCE, _miner.setProperties(handler
						.getVirtualFile(newAbsPath.getVirtualPart(), systemOperationMonitor)));
				_status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
				_dataStore.update(_subject);
			}
			else if (systemOperationMonitor.isCancelled())
			{
				_subject.setAttribute(DE.A_SOURCE, _miner.setProperties(handler
						.getVirtualFile(oldAbsPath.getVirtualPart(), systemOperationMonitor)));
			}
			else {
				_status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
			}
			_dataStore.refresh(_subject);
			return _miner.statusDone(_status);
		}
		if (filerename.exists())
			_status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_EXIST);
		else {
			try {
		        String[] auditData = new String[] {"RENAME", fileoldname.getAbsolutePath(), filerename.getAbsolutePath(), null}; //$NON-NLS-1$
		     	UniversalServerUtilities.logAudit(auditData, _dataStore);

				boolean done = fileoldname.renameTo(filerename);
				if (done) {
					_subject.setAttribute(DE.A_NAME, filerename.getName());
					_subject
							.setAttribute(DE.A_SOURCE,
									_miner.setProperties(filerename));
					_status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);

					if (filerename.isDirectory()) {
						// update children's properties
						updateChildProperties(_subject, filerename);
					}
					_dataStore.update(_subject);
				} else
					_status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
			} catch (Exception e) {
				_status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
				UniversalServerUtilities.logError(CLASSNAME,
						"handleRename failed", e, _dataStore); //$NON-NLS-1$
			}
		}
		_dataStore.refresh(_subject);
		return _miner.statusDone(_status);
	}

	// DKM: during folder rename we need to recursively update all the parent
	// paths
	private void updateChildProperties(DataElement subject, File filerename) {

		int nestedSize = subject.getNestedSize();
		for (int i = 0; i < nestedSize; i++) {
			DataElement child = subject.get(i);
			child.setAttribute(DE.A_VALUE, filerename.getAbsolutePath());

			if (child.getNestedSize() > 0) {
				File childFile = new File(filerename, child.getName());
				updateChildProperties(child, childFile);
			}
		}
	}
}
