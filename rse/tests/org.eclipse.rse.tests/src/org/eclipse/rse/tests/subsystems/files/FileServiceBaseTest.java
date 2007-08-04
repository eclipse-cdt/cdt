/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Xuan Chen (IBM)               - initial API and implementation
 *   - <copied code from org.eclipse.core.tests.internal.localstore/LocalStoreTest (Copyright IBM)>
 *   - <copied code from org.eclipse.core.tests.harness/CoreTest (Copyright IBM)>
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.files;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IVirtualRemoteFile;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;

public class FileServiceBaseTest extends RSEBaseConnectionTestCase {

	protected IFileServiceSubSystem fss;
	protected IFileServiceSubSystem localFss;
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
			fss.resolveFilterString(targetFolder, null, mon);
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
			fss.resolveFilterString(targetFolder, null, mon);
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
			System.out.println("verifying the contents for folder: " + folderToCheck.getAbsolutePath());
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
				assertTrue("Could not find " + names[i], found != null);
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
               	
       	if ((parentFolderPath.length()==1) && (parentFolderPath.charAt(0)=='/') &&
            (parentFolderPath.charAt(0)==sep))
       	  newAbsName = sep + newName; 
        else
	      newAbsName = parentFolderPath + sep + newName; 
	    return newAbsName;
	}
	
	//----------------------------------------------------------------------
	// <copied code from org.eclipse.core.tests.internal.localstore/LocalStoreTest (Copyright IBM)>
	//----------------------------------------------------------------------
	protected IFileStore createDir(IFileStore store, boolean clear) throws CoreException {
		if (clear && store.fetchInfo().exists())
			store.delete(EFS.NONE, null);
		store.mkdir(EFS.NONE, null);
		IFileInfo info = store.fetchInfo();
		assertTrue("createDir.1", info.exists());
		assertTrue("createDir.1", info.isDirectory());
		return store;
	}

	//----------------------------------------------------------------------
	// <copied code from org.eclipse.core.tests.internal.localstore/LocalStoreTest (Copyright IBM)>
	//----------------------------------------------------------------------
	protected IFileStore createDir(String string, boolean clear) throws CoreException {
		return createDir(EFS.getFileSystem(EFS.SCHEME_FILE).getStore(new Path(string)), clear);
	}
	
	/**
	 * Create a file with random content. If a resource exists in the same path,
	 * the resource is deleted.
	 * <copied code from org.eclipse.core.tests.internal.localstore/LocalStoreTest (Copyright IBM)>
	 * 
	 * @param target the file to create
	 * @param content content of the new file
	 * @throws CoreException
	 */
	protected void createFile(IFileStore target, String content) throws CoreException {
		target.delete(EFS.NONE, null);
		InputStream input = new ByteArrayInputStream(content.getBytes());
		transferData(input, target.openOutputStream(EFS.NONE, null));
		IFileInfo info = target.fetchInfo();
		assertTrue(info.exists() && !info.isDirectory());
	}
	
	/**
	 * Copy the data from the input stream to the output stream.
	 * Close both streams when finished.
	 * <copied code from org.eclipse.core.tests.harness/CoreTest (Copyright IBM)>
	 * 
	 * @param input input stream
	 * @param output output stream
	 */
	protected void transferData(InputStream input, OutputStream output) {
		try {
			try {
				int c = 0;
				while ((c = input.read()) != -1)
					output.write(c);
			} finally {
				input.close();
				output.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(e.toString(), false);
		}
	}
	
	/**
	 * Return String with some random text to use
	 * as contents for a file resource.
	 * <copied code from org.eclipse.core.tests.harness/CoreTest (Copyright IBM)>
	 * 
	 * @return the result random string
	 */
	protected String getRandomString() {
		switch ((int) Math.round(Math.random() * 10)) {
			case 0 :
				return "este e' o meu conteudo (portuguese)";
			case 1 :
				return "ho ho ho";
			case 2 :
				return "I'll be back";
			case 3 :
				return "don't worry, be happy";
			case 4 :
				return "there is no imagination for more sentences";
			case 5 :
				return "customize yours";
			case 6 :
				return "foo";
			case 7 :
				return "bar";
			case 8 :
				return "foobar";
			case 9 :
				return "case 9";
			default :
				return "these are my contents";
		}
	}

}
