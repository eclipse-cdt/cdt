/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
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
 * Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 * Martin Oberhuber (Wind River) - [199854][api] Improve error reporting for archive handlers
 * David McKnight  (IBM)  - [358301] [DSTORE] Hang during debug source look up
 *******************************************************************************/
package org.eclipse.rse.internal.dstore.universal.miners.filesystem;

import java.io.File;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.dstore.core.server.SecuredThread;
import org.eclipse.rse.dstore.universal.miners.ICancellableHandler;
import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

public class QueryThread extends SecuredThread implements ICancellableHandler {

	protected DataElement _subject;
	protected DataElement _status;

	protected boolean _isCancelled = false;
	protected boolean _isDone = false;

	public QueryThread(DataElement subject, DataElement status)
	{
		super(subject.getDataStore());
		_subject = subject;
		_status = status;
	}

	/**
	 * Complete status.
	 */
	public DataElement statusDone(DataElement status) {
		status.setAttribute(DE.A_NAME, DataStoreResources.model_done);
		_dataStore.refresh(status);
		return status;
	}


	public void cancel() {
		_isCancelled = true;
	}

	public boolean isCancelled() {
		return _isCancelled;
	}

	public boolean isDone() {
		return _isDone;
	}


	public String setProperties(File fileObj) {
		return setProperties(fileObj, false);
	}

	/**
	 * Method to obtain the properties of file or folder.
	 */
	public String setProperties(File fileObj, boolean doArchiveProperties) {
		String version = IServiceConstants.VERSION_1;
		StringBuffer buffer = new StringBuffer(500);
		long date = fileObj.lastModified();
		long size = fileObj.length();
		boolean hidden = fileObj.isHidden();
		boolean canWrite = fileObj.canWrite() ;
		boolean canRead = fileObj.canRead();

		// These extra properties here might cause problems for older clients,
		// ie: a IndexOutOfBounds in UniversalFileImpl.

		// DKM: defer this until later as it is bad for performance...
		// I think we're doing the full query on an archive by instantiating a
		// handler
		boolean isArchive = false;//ArchiveHandlerManager.getInstance().isArchive(fileObj);

		String comment;
		if (isArchive)
			try {
				comment = ArchiveHandlerManager.getInstance().getComment(fileObj);
			} catch (SystemMessageException e) {
				comment = " "; //$NON-NLS-1$
			}
		else
			comment = " "; //$NON-NLS-1$

		long compressedSize = size;
		String compressionMethod = " "; //$NON-NLS-1$
		double compressionRatio = 0;

		long expandedSize;
		if (isArchive)
			try {
				expandedSize = ArchiveHandlerManager.getInstance().getExpandedSize(fileObj);
			} catch (SystemMessageException e) {
				expandedSize = 0;
			}
		else
			expandedSize = size;

		try {
		buffer.append(version).append(IServiceConstants.TOKEN_SEPARATOR).append(date).append(
				IServiceConstants.TOKEN_SEPARATOR).append(size).append(IServiceConstants.TOKEN_SEPARATOR);
		buffer.append(hidden).append(IServiceConstants.TOKEN_SEPARATOR).append(canWrite).append(
				IServiceConstants.TOKEN_SEPARATOR).append(canRead);

		// values might not be used but we set them here just so that there are right number
		// of properties
		buffer.append(IServiceConstants.TOKEN_SEPARATOR);
		buffer.append(comment).append(IServiceConstants.TOKEN_SEPARATOR).append(compressedSize)
				.append(IServiceConstants.TOKEN_SEPARATOR).append(compressionMethod).append(
						IServiceConstants.TOKEN_SEPARATOR);
		buffer.append(compressionRatio).append(IServiceConstants.TOKEN_SEPARATOR).append(
				expandedSize);
		}
		catch (OutOfMemoryError e){
			System.exit(-1);
		}

		String buf = buffer.toString();
		return buf;
	}

	public String setProperties(VirtualChild fileObj) {
		String version = IServiceConstants.VERSION_1;
		StringBuffer buffer = new StringBuffer(500);
		long date = fileObj.getTimeStamp();
		long size = fileObj.getSize();
		boolean hidden = false;
		boolean canWrite = fileObj.getContainingArchive().canWrite();
		boolean canRead = fileObj.getContainingArchive().canRead();

		// These extra properties here might cause problems for older clients,
		// ie: a IndexOutOfBounds in UniversalFileImpl.
		String comment = fileObj.getComment();
		if (comment.equals("")) //$NON-NLS-1$
			comment = " "; // make sure this is still a //$NON-NLS-1$
		// token
		long compressedSize = fileObj.getCompressedSize();
		String compressionMethod = fileObj.getCompressionMethod();
		if (compressionMethod.equals("")) //$NON-NLS-1$
			compressionMethod = " "; //$NON-NLS-1$
		double compressionRatio = fileObj.getCompressionRatio();
		long expandedSize = size;

		buffer.append(version).append(IServiceConstants.TOKEN_SEPARATOR).append(date).append(
				IServiceConstants.TOKEN_SEPARATOR).append(size).append(IServiceConstants.TOKEN_SEPARATOR);
		buffer.append(hidden).append(IServiceConstants.TOKEN_SEPARATOR).append(canWrite).append(
				IServiceConstants.TOKEN_SEPARATOR).append(canRead);

		buffer.append(IServiceConstants.TOKEN_SEPARATOR);
		buffer.append(comment).append(IServiceConstants.TOKEN_SEPARATOR).append(compressedSize)
				.append(IServiceConstants.TOKEN_SEPARATOR).append(compressionMethod).append(
						IServiceConstants.TOKEN_SEPARATOR);
		buffer.append(compressionRatio).append(IServiceConstants.TOKEN_SEPARATOR).append(
				expandedSize);

		return buffer.toString();
	}
}
