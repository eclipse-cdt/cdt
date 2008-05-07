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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.dstore.universal.miners.UniversalFileSystemMiner;
import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.SystemOperationMonitor;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

public class CopyBatchThread extends CopyThread {


	public CopyBatchThread(DataElement targetFolder, DataElement theElement, UniversalFileSystemMiner miner, boolean isWindows, DataElement status)
	{
		super(targetFolder, theElement, miner, isWindows, status);
	}

	public void run()
	{
		super.run();
		try {
			handleCopyBatch();
		} catch (SystemMessageException e) {
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
			miner.statusDone(status);
		}
		_isDone = true;
	}

	private DataElement handleCopyBatch() throws SystemMessageException
	{
		String targetType = targetFolder.getType();
		File tgtFolder = getFileFor(targetFolder);
		int numOfSources = theElement.getNestedSize() - 2;
		systemOperationMonitor = new SystemOperationMonitor();

		if (targetType.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR) || targetType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR))
		{
		    // if target is virtual or an archive, insert into an archive
			AbsoluteVirtualPath vpath = miner.getAbsoluteVirtualPath(targetFolder);
			ISystemArchiveHandler handler = miner.getArchiveHandlerFor(vpath.getContainingArchiveString());
			boolean result = true;

			List nonDirectoryArrayList = new ArrayList();
			List nonDirectoryNamesArrayList = new ArrayList();

			String virtualContainer = ""; //$NON-NLS-1$

			if (targetType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR))
			{
				virtualContainer = vpath.getVirtualPart();
			}

			for (int i = 0; i < numOfSources; i++)
			{
				if (isCancelled())
				{
					return miner.statusCancelled(status);
				}
				DataElement sourceFile = miner.getCommandArgument(theElement, i+1);
				String srcType = sourceFile.getType();
				String srcName = sourceFile.getName();
				File srcFile;

				if (srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR) || srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR)
					|| srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR))
				{
					srcFile = getFileFor(sourceFile);
				}
				else if (srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) || srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR))
				{
					AbsoluteVirtualPath svpath = miner.getAbsoluteVirtualPath(sourceFile);
					ISystemArchiveHandler shandler = miner.getArchiveHandlerFor(svpath.getContainingArchiveString());

					if (shandler == null)
					{
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						return miner.statusDone(status);
					}

					VirtualChild child = shandler.getVirtualFile(svpath.getVirtualPart(), systemOperationMonitor);
					srcFile = child.getExtractedFile();
				}
				else {
					//invalid source type
					status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
					return miner.statusDone(status);
				}

				//If this source file object is directory, we will call ISystemArchiveHandler#add(File ...) method to
				//it and all its descendants into the archive file.
				//If this source file object is not a directory, we will add it into a list, and then
				//call ISystemArchiveHandler#add(File[] ...) to add them in batch.
				if (srcFile.isDirectory())
				{
					try {
						handler.add(srcFile, virtualContainer, srcName, systemOperationMonitor);
					} catch (SystemMessageException e) {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						if (isCancelled())
						{
							return miner.statusCancelled(status);
						}
						else
						{
							return miner.statusDone(status);
						}

					}
				}
				else
				{
					nonDirectoryArrayList.add(srcFile);
					nonDirectoryNamesArrayList.add(srcName);
				}
			}

			if (nonDirectoryArrayList.size() > 0)
			{
				File[] resultFiles = (File[])nonDirectoryArrayList.toArray(new File[nonDirectoryArrayList.size()]);
				String[] resultNames = (String[])nonDirectoryNamesArrayList.toArray(new String[nonDirectoryNamesArrayList.size()]);
				//we need to add those files into the archive file as well.
				try {
					handler.add(resultFiles, virtualContainer, resultNames, systemOperationMonitor);
				} catch (SystemMessageException e) {
					result = false;
				}
			}

			if (result)
			{
				status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
			}
			else
			{
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
			}
			if (isCancelled())
			{
				return miner.statusCancelled(status);
			}
			else
			{
				return miner.statusDone(status);
			}
		}
		else // target is a regular folder
		{
			boolean folderCopy = false;
			String source = ""; //$NON-NLS-1$
			String tgt = enQuote(tgtFolder.getAbsolutePath());

			int numOfNonVirtualSources = 0;
			for (int i = 0; i < numOfSources; i++)
			{
				if (isCancelled())
				{
					return miner.statusCancelled(status);
				}
				DataElement sourceFile = miner.getCommandArgument(theElement, i+1);
				String srcType = sourceFile.getType();

				if (srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) || srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR))
				{
					// extract from an archive to folder
					try {
						AbsoluteVirtualPath svpath = miner.getAbsoluteVirtualPath(sourceFile);
						ISystemArchiveHandler shandler = miner.getArchiveHandlerFor(svpath.getContainingArchiveString());
						VirtualChild child = shandler.getVirtualFile(svpath.getVirtualPart(), systemOperationMonitor);

						File parentDir = getFileFor(targetFolder);
						File destination = new File(parentDir, sourceFile.getName());

						if (child.isDirectory) {
							shandler.extractVirtualDirectory(svpath.getVirtualPart(), parentDir, destination, systemOperationMonitor);
						} else {
							shandler.extractVirtualFile(svpath.getVirtualPart(), destination, systemOperationMonitor);
						}

					} catch (SystemMessageException e) {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						return miner.statusDone(status);
					}
				}
				else // source is regular file or folder
				{
					File srcFile = getFileFor(sourceFile);
					folderCopy = folderCopy || srcFile.isDirectory();
					String src = srcFile.getAbsolutePath();

					// handle special characters in source and target strings
					src = enQuote(src);

					// handle window case separately, since xcopy command could not handler
					// multiple source names
					if (isWindows)
					{
						tgt = tgtFolder.getAbsolutePath() + File.separatorChar + srcFile.getName();
						// Both unix and windows need src quoted, so it's already done
						doCopyCommand(src, enQuote(tgt), folderCopy, status);
						if (status.getAttribute(DE.A_SOURCE) == IServiceConstants.FAILED)
						{
							break;
						}
						continue;
					}
					if (numOfNonVirtualSources == 0)
					{
						source += src;
					}
					else
					{
						source = source + " " + src; //$NON-NLS-1$
					}
					numOfNonVirtualSources++;
				}
			} // end for loop iterating through sources

			if (numOfNonVirtualSources > 0)
			{
				doCopyCommand(source, tgt, folderCopy, status);
			}
		} // end if/then/else (target is regular folder)
		if (isCancelled())
		{
			return miner.statusCancelled(status);
		}
		else
		{
			return miner.statusDone(status);
		}
	}



}
