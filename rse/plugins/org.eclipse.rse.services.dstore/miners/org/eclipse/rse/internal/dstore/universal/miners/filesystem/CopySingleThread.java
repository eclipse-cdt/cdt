/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.rse.internal.dstore.universal.miners.filesystem;

import java.io.File;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.dstore.universal.miners.UniversalFileSystemMiner;
import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.SystemOperationMonitor;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

public class CopySingleThread extends CopyThread {

	private DataElement nameObj;


	public CopySingleThread(DataElement targetFolder, DataElement theElement, DataElement nameObj, UniversalFileSystemMiner miner, boolean isWindows, DataElement status)
	{
		super(targetFolder, theElement, miner, isWindows, status);
		this.nameObj = nameObj;
	}

	public void run()
	{
		super.run();
		try {
			handleCopy();
		} catch (SystemMessageException e) {
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
			miner.statusDone(status);
		}
		_isDone = true;
	}

	private DataElement handleCopy() throws SystemMessageException
	{
		DataElement sourceFile = theElement;
		String newName = nameObj.getName();
		String targetType = targetFolder.getType();
		String srcType = sourceFile.getType();
		//In the case of super transfer, the source file is a virtual file/folder inside the temporary zip file, and its type information is set to
		//default UNIVERSAL_FILTER_DESCRIPTOR since its information never been cached before.
		//We need to find out its real type first before going to different if statement.
		File srcFile = null;
		VirtualChild child = null;
		systemOperationMonitor = new SystemOperationMonitor();
		if (IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR == srcType)
		{
			if (ArchiveHandlerManager.isVirtual(sourceFile.getValue()))
			{
				String goodFullName = ArchiveHandlerManager.cleanUpVirtualPath(sourceFile.getValue());
				child = ArchiveHandlerManager.getInstance().getVirtualObject(goodFullName);
				if (child.exists())
				{
					if (child.isDirectory)
					{
						srcType = IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR;
					} else
					{
						srcType = IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR;
					}
				}
			}
		}

		if (targetType.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR) || targetType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {

		    // insert into an archive
			AbsoluteVirtualPath vpath = miner.getAbsoluteVirtualPath(targetFolder);
			ISystemArchiveHandler handler = miner.getArchiveHandlerFor(vpath.getContainingArchiveString());

			if (handler == null) {
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
				return miner.statusDone(status);
			}

			if (srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR) || srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR)
					|| srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)) {

			    srcFile = getFileFor(sourceFile);
			}
			else if (srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) || srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
				ISystemArchiveHandler shandler = null;
				if (null == child)
				{
					AbsoluteVirtualPath svpath = miner.getAbsoluteVirtualPath(sourceFile);
					shandler = miner.getArchiveHandlerFor(svpath.getContainingArchiveString());

					if (shandler == null) {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						return miner.statusDone(status);
					}
					child = shandler.getVirtualFile(svpath.getVirtualPart(), systemOperationMonitor);
				}
				else
				{
					//If child is not null, it means the sourceFile is a type of UNIVERSAL_FILTER_DESCRIPTOR, and has already been handled
					shandler = child.getHandler();
				}
				srcFile = child.getExtractedFile();
			}

			String virtualContainer = ""; //$NON-NLS-1$

			if (targetType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
				virtualContainer = vpath.getVirtualPart();
			}

			handler.add(srcFile, virtualContainer, newName, systemOperationMonitor);
		}
		else if (srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) || srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
			ISystemArchiveHandler shandler = null;
			AbsoluteVirtualPath svpath = null;
			if (null == child)
			{
				svpath = miner.getAbsoluteVirtualPath(sourceFile);
				shandler = miner.getArchiveHandlerFor(svpath.getContainingArchiveString());

				if (shandler == null) {
					status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
					return miner.statusDone(status);
				}
				child = shandler.getVirtualFile(svpath.getVirtualPart(), systemOperationMonitor);
			}
			else
			{
				//If child is not null, it means the sourceFile is a type of UNIVERSAL_FILTER_DESCRIPTOR, and has already been handled
				shandler = child.getHandler();
				svpath = miner.getAbsoluteVirtualPath(sourceFile.getValue());
			}

			File parentDir = getFileFor(targetFolder);
			File destination = new File(parentDir, newName);

			if (child.isDirectory) {
				shandler.extractVirtualDirectory(svpath.getVirtualPart(), parentDir, destination, systemOperationMonitor);
			}
			else {
				shandler.extractVirtualFile(svpath.getVirtualPart(), destination, systemOperationMonitor);
			}
		}
		else {
			File tgtFolder = getFileFor(targetFolder);
			srcFile = getFileFor(sourceFile);

			// regular copy
			boolean folderCopy = srcFile.isDirectory();
			String src = srcFile.getAbsolutePath();
			String tgt = tgtFolder.getAbsolutePath() + File.separatorChar + newName;
			File tgtFile = new File(tgt);

			if (tgtFile.exists() && tgtFile.isDirectory())
			{
				//For Windows, we need to use xcopy command, which require the new directory
				//name be part of the target.
				if (newName.equals(srcFile.getName()) && !isWindows)
				{
					tgt =  tgtFolder.getAbsolutePath();
				}
			}

			doCopyCommand(enQuote(src), enQuote(tgt), folderCopy, status);
		}

		return miner.statusDone(status);
	}



}
