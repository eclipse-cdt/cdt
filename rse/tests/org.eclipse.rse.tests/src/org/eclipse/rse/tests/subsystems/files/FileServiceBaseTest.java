/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Xuan Chen (IBM)               - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.files;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IVirtualRemoteFile;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;

public class FileServiceBaseTest extends RSEBaseConnectionTestCase {

	protected IFileServiceSubSystem fss;
	protected IFileService fs;
	protected IRemoteFile tempDir;
	protected String tempDirPath;
	protected IProgressMonitor mon = new NullProgressMonitor();
	protected static boolean classBeenRunBefore = false;
	
	public static int TYPE_FILE = 0;
	public static int TYPE_FOLDER = 1;
	
	
	public boolean isWindows() {
		return fss.getHost().getSystemType().isWindows(); 
	}
	
	public String getTestFileName() {
		//Return a filename for testing that exposes all characters valid on the file system
		if (!isWindows()) {
			//UNIX TODO: test embedded newlines
			return "a !@#${a}\"\' fi\tle\b\\%^&*()?_ =[]~+-'`;:,.|<>"; //$NON-NLS-1$
		}
		//Fallback: Windows TODO: test unicode
		return "a !@#${a}'` file%^&()_ =[]~+-;,."; //$NON-NLS-1$
	}
	
	public IRemoteFile copySourceFileOrFolder(String sourceFullName, String sourceName, String targetFolderFullName)
	{
		boolean ok = false;
		IRemoteFile result = null;
		try
		{
			IRemoteFile originalTargetArchiveFile = fss.getRemoteFileObject(sourceFullName, mon); 
			IRemoteFile targetFolder = fss.getRemoteFileObject(targetFolderFullName, mon);
			ok = fss.copy(originalTargetArchiveFile, targetFolder, sourceName, mon);
			if (ok)
			{
				//copy is successful
				result = fss.getRemoteFileObject(getNewAbsoluteName(targetFolder, sourceName), mon);
			}
			//Need to call resolveFilterString of the parent to make sure the newly copied child
			//is added to the DStore map.  Otherwise, next time when query it, it will just created a 
			//default filter string.  And the dstore server cannot handler it correctly.
			Object[] children = fss.resolveFilterString(targetFolder, null, mon);
		}
		catch(Exception e)
		{
			return null;
		}
		return result;
	}
	
	public IRemoteFile createFileOrFolder(String targetFolderName, String fileOrFolderName, boolean isFolder)
	{
		IRemoteFile result = null;
		try
		{
			System.out.println("targetFolderName is " + targetFolderName);
			if (fss == null)
			{
				System.out.println("fss is null ");
			}
			IRemoteFile targetFolder = fss.getRemoteFileObject(targetFolderName, mon);
			//fss.resolveFilterString(targetFolder, null, mon);
			String fileOrFolderAbsName = getNewAbsoluteName(targetFolder, fileOrFolderName);
			IRemoteFile newFileOrFolderPath = fss.getRemoteFileObject(fileOrFolderAbsName, mon); 
			if (isFolder)
			{
				result = fss.createFolder(newFileOrFolderPath, mon);
			}
			else
			{
				result = fss.createFile(newFileOrFolderPath, mon);
			}
			//Need to call resolveFilterString of the parent to make sure the newly created child
			//is added to the DStore map.  Otherwise, next time when query it, it will just created a 
			//default filter string.  And the dstore server cannot handler it correctly.
			Object[] children = fss.resolveFilterString(targetFolder, null, mon);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return result;
	}
	
	public Object getChildFromFolder(IRemoteFile folderToCheck, String childName)
	{
		//then check the result of copy
		Object[] children = null;
		Object foundChild = null;
		try
		{
			children = fss.resolveFilterString(folderToCheck, null, mon);
			for (int i=0; i<children.length; i++)
			{
				String thisName = ((IRemoteFile)children[i]).getName();
				if (thisName.equals(childName))
				{
					foundChild = children[i];
				}
			}
		}
		catch (Exception e)
		{
			foundChild = null;
		}
		return foundChild;
	}
	
	public void checkFolderContents(IRemoteFile folderToCheck, String[] names, int[] types)
	{
		try
		{
			//the folder returned by the create API did not get the right attributes.
			//We need to call getRemoteFileObject to get its attribute updated.
			//Otherwise, will get error "directory not readable"
			folderToCheck = fss.getRemoteFileObject(folderToCheck.getAbsolutePath(), mon);
			Object[] children = fss.resolveFilterString(folderToCheck, null, mon);
			//Make sure the children array includes the copied folder.
			HashMap childrenMap = new HashMap();
		    //Add children name into the map
			for (int i=0; i<children.length; i++)
			{
				String thisName = ((IRemoteFile)children[i]).getName();
				childrenMap.put(thisName, children[i]);
			}
			//Check contents are in the array list
			for (int i=0; i<names.length; i++)
			{
				IRemoteFile found = (IRemoteFile)(childrenMap.get(names[i]));
				assertTrue(found != null);
				assertTrue(found.exists());
				if (types != null && types.length != 0)
				{
					//If input array of types, we also need to check if the type is correct.
					if (types[i] == TYPE_FILE)
					{
						assertTrue(found.isFile());
					}
					else if (types[i] == TYPE_FOLDER)
					{
						assertTrue(found.isDirectory());
					}
				}
			}
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
	}
	
	protected static String getNewAbsoluteName(IRemoteFile parentFolder, String newName)
	{
		String newAbsName = null;
        char sep = parentFolder.getSeparatorChar();		
        String parentFolderPath = parentFolder.getAbsolutePath();
        
        // hack by Mike to allow virtual files and folders.
        if (parentFolder instanceof IVirtualRemoteFile)
        {
        	sep = '/';             	
        }
        else if (parentFolder.isArchive())
        {
        	sep = '/';
        	parentFolderPath = parentFolderPath + ArchiveHandlerManager.VIRTUAL_SEPARATOR;
        }
        
        // hack by Phil to fix bug when trying to create file inside root "/"... it
        //  tried to create "//file.ext".           	
       	if ((parentFolderPath.length()==1) && (parentFolderPath.charAt(0)=='/') &&
            (parentFolderPath.charAt(0)==sep))
       	  newAbsName = sep + newName; 
        else
	      newAbsName = parentFolderPath + sep + newName; 
	    return newAbsName;
	}

}
