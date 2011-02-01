/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Xuan Chen (IBM) - initial API and implementation
 *   - <copied code from org.eclipse.core.tests.internal.localstore/LocalStoreTest (C) IBM>
 *   - <copied code from org.eclipse.core.tests.harness/CoreTest (C) IBM>
 *   - <copied code from org.eclipse.core.tests.resources/ResourceTest (C) IBM>
 *   - <copied code from org.eclipse.core.tests.harness/CoreTest (C) IBM>
 * Martin Oberhuber (Wind River) - [195402] Add constructor with test name
 * David Dykstal (IBM) [230821] fix IRemoteFileSubSystem API to be consistent with IFileService
 * Xuan Chen (IBM) [333874] - Added more logging code to track junit failure
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
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IVirtualRemoteFile;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;

/**
 * Base class for file subsystem / file service unit tests.
 * Contains helper methods for test environment setup.
 */
public class FileServiceBaseTest extends RSEBaseConnectionTestCase {

	protected IFileServiceSubSystem fss;
	protected IFileServiceSubSystem localFss;
	protected IFileService fs;
	protected IRemoteFile tempDir;
	protected String tempDirPath;
	protected IProgressMonitor mon = new NullProgressMonitor();

	public static int TYPE_FILE = 0;
	public static int TYPE_FOLDER = 1;

	/**
	 * Constructor with specific test name.
	 * @param name test to execute
	 */
	public FileServiceBaseTest(String name) {
		super(name);
		setTargetName("local");
	}

	public void setUp() throws Exception {
		super.setUp();
		setupFileSubSystem();
		if (isTestDisabled())
			return;
		//Create a temparory directory in My Home
		try
		{
			IRemoteFile homeDirectory = fss.getRemoteFileObject(".", mon);
			String baseFolderName = "rsetest";
			String homeFolderName = homeDirectory.getAbsolutePath();
			String testFolderName = FileServiceHelper.getRandomLocation(fss, homeFolderName, baseFolderName, mon);
			tempDir = createFileOrFolder(homeFolderName, testFolderName, true);
			tempDirPath = tempDir.getAbsolutePath();
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
	}

	public void tearDown() throws Exception {
		if (fss != null) {
			try {
				fss.delete(tempDir, mon);
			} catch (SystemMessageException msg) {
				// ensure that super.tearDown() can run
				System.err.println("Exception on tearDown: " + msg.getLocalizedMessage()); //$NON-NLS-1$
			}
		}
		super.tearDown();
	}

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


	public IRemoteFile copySourceFileOrFolder(String sourceFullName, String sourceName, String targetFolderFullName) throws Exception
	{
		IRemoteFile result = null;
		IRemoteFile originalTargetArchiveFile = fss.getRemoteFileObject(sourceFullName, mon);
		IRemoteFile targetFolder = fss.getRemoteFileObject(targetFolderFullName, mon);
		fss.copy(originalTargetArchiveFile, targetFolder, sourceName, mon);
		result = fss.getRemoteFileObject(getNewAbsoluteName(targetFolder, sourceName), mon);
		//Need to call resolveFilterString of the parent to make sure the newly copied child
		//is added to the DStore map.  Otherwise, next time when query it, it will just created a
		//default filter string.  And the dstore server cannot handler it correctly.
		fss.resolveFilterString(targetFolder, null, mon);
		return result;
	}

	public IRemoteFile createFileOrFolder(String targetFolderName, String fileOrFolderName, boolean isFolder) throws Exception
	{
		return createFileOrFolder(fss, targetFolderName, fileOrFolderName, isFolder);
	}

	public IRemoteFile createFileOrFolder(IFileServiceSubSystem inputFss, String targetFolderName, String fileOrFolderName, boolean isFolder) throws Exception
	{
		IRemoteFile result = null;
		//System.out.println("createFileOrFolder: targetFolderName is " + targetFolderName);
		IRemoteFile targetFolder = inputFss.getRemoteFileObject(targetFolderName, mon);
		String fileOrFolderAbsName = getNewAbsoluteName(targetFolder, fileOrFolderName);
		IRemoteFile newFileOrFolderPath = inputFss.getRemoteFileObject(fileOrFolderAbsName, mon);
		if (isFolder)
		{
			result = inputFss.createFolder(newFileOrFolderPath, mon);
		}
		else
		{
			result = inputFss.createFile(newFileOrFolderPath, mon);
		}
		//Need to call resolveFilterString of the parent to make sure the newly created child
		//is added to the DStore map.  Otherwise, next time when query it, it will just created a
		//default filter string.  And the dstore server cannot handler it correctly.
		inputFss.resolveFilterString(targetFolder, null, mon);
		return result;
	}

	public Object getChildFromFolder(IRemoteFile folderToCheck, String childName) throws Exception
	{
		return getChildFromFolder(fss, folderToCheck, childName);
	}

	public Object getChildFromFolder(IFileServiceSubSystem inputFss, IRemoteFile folderToCheck, String childName) throws Exception
	{
		//then check the result of copy
		Object[] children = null;
		Object foundChild = null;
		children = inputFss.resolveFilterString(folderToCheck, null, mon);
		for (int i=0; i<children.length; i++)
		{
			String thisName = ((IRemoteFile)children[i]).getName();
			if (thisName.equals(childName))
			{
				foundChild = children[i];
				break;
			}
		}
		return foundChild;
	}

	public void checkFolderContents(IRemoteFile folderToCheck, String[] names, int[] types) throws Exception
	{
		checkFolderContents(fss, folderToCheck, names, types);
	}

	public void checkFolderContents(IFileServiceSubSystem inputFss, IRemoteFile folderToCheck, String[] names, int[] types) throws Exception
	{
		//the folder returned by the create API did not get the right attributes.
		//We need to call getRemoteFileObject to get its attribute updated.
		//Otherwise, will get error "directory not readable"
		folderToCheck = inputFss.getRemoteFileObject(folderToCheck.getAbsolutePath(), mon);
		System.out.println("verifying the contents for folder: " + folderToCheck.getAbsolutePath());
		Object[] children = inputFss.resolveFilterString(folderToCheck, null, mon);
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
	// <copied code from org.eclipse.core.tests.internal.localstore/LocalStoreTest (C) IBM>
	//----------------------------------------------------------------------
	protected IFileStore createDir(IFileStore store, boolean clear) throws CoreException {
		if (clear && store.fetchInfo().exists())
			store.delete(EFS.NONE, new NullProgressMonitor());
		store.mkdir(EFS.NONE, new NullProgressMonitor());
		IFileInfo info = store.fetchInfo();
		assertTrue("createDir.1", info.exists());
		assertTrue("createDir.1", info.isDirectory());
		return store;
	}

	//----------------------------------------------------------------------
	// <copied code from org.eclipse.core.tests.internal.localstore/LocalStoreTest (C) IBM>
	//----------------------------------------------------------------------
	protected IFileStore createDir(String string, boolean clear) throws CoreException {
		return createDir(EFS.getFileSystem(EFS.SCHEME_FILE).getStore(new Path(string)), clear);
	}

	/**
	 * Create a file with random content. If a resource exists in the same path,
	 * the resource is deleted.
	 * <copied code from org.eclipse.core.tests.internal.localstore/LocalStoreTest (C) IBM>
	 *
	 * @param target the file to create
	 * @param content content of the new file
	 * @throws CoreException in case of failure
	 */
	protected void createFile(IFileStore target, String content) throws CoreException {
		target.delete(EFS.NONE, null);
		InputStream input = new ByteArrayInputStream(content.getBytes());
		transferData(input, target.openOutputStream(EFS.NONE, null));
		IFileInfo info = target.fetchInfo();
		assertTrue(info.exists() && !info.isDirectory());
	}

	/**
	 * <copied code from org.eclipse.core.tests.harness/CoreTest (C) IBM>
	 * Asserts that a stream closes successfully. Null streams
	 * are ignored, but failure to close the stream is reported as
	 * an assertion failure.
	 * @param stream the input stream to close
	 */
	protected void assertClose(InputStream stream) {
		if (stream == null)
			return;
		try {
			stream.close();
		} catch (IOException e) {
			fail("Failed close in assertClose");
		}
	}

	/**
	 * <copied code from org.eclipse.core.tests.harness/CoreTest (C) IBM>
	 * Return an input stream with some the specified text to use
	 * as contents for a file resource.
	 * @param text the input text
	 * @return the input stream of the input text
	 */
	public InputStream getContents(String text) {
		return new ByteArrayInputStream(text.getBytes());
	}

	/**
	 * <copied code from org.eclipse.core.tests.resources/ResourceTest.java (C) IBM>
	 *
	 * Returns a boolean value indicating whether or not the contents
	 * of the given streams are considered to be equal. Closes both input streams.
	 * @param a input stream a
	 * @param b input stream b
	 * @return if both stream are consider to be equal
	 */
	public boolean compareContent(InputStream a, InputStream b) {
		int c, d;
		if (a == null && b == null)
			return true;
		try {
			if (a == null || b == null)
				return false;
			while ((c = a.read()) == (d = b.read()) && (c != -1 && d != -1)) {
				//body not needed
			}
			return (c == -1 && d == -1);
		} catch (IOException e) {
			return false;
		} finally {
			assertClose(a);
			assertClose(b);
		}
	}

	/**
	 * <copied code from org.eclipse.core.tests.resources/ResourceTest.java (C) IBM>
	 *
	 * Returns a boolean value indicating whether or not the contents
	 * of the given streams are considered to be equal. Closes both input streams.
	 * @param a input stream a
	 * @param b input stream b
	 * @return if both stream are consider to be equal
	 */
	public boolean compareContent1(InputStream a, InputStream b) {
		int c, d;
		if (a == null && b == null)
			return true;
		try {
			if (a == null || b == null)
			{
				fail("only one of the input stream is null");
				return false;
			}
			while ((c = a.read()) == (d = b.read()) && (c != -1 && d != -1)) {
				//body not needed
			}
			if (c == -1 && d == -1)
			{
				return true;
			}
			String msg = "difference: c is: " + c + " but d is: " + d + "  a is: " + a.toString() + " but b is: " + b.toString();
			fail(msg);
			return false;	
		} catch (IOException e) {
			fail("IOException: " + e.getMessage());
			return false;
		} finally {
			assertClose(a);
			assertClose(b);
		}
	}
	
	/**
	 * Copy the data from the input stream to the output stream.
	 * Close both streams when finished.
	 * <copied code from org.eclipse.core.tests.harness/CoreTest (C) IBM>
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
	 * <copied code from org.eclipse.core.tests.harness/CoreTest (C) IBM>
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

	/**
	 * Setup the file subsystem used for this testcase
	 */
	protected void setupFileSubSystem()
	{
		IHost localHost = getLocalSystemConnection();
		if (isTestDisabled())
			return;
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		ISubSystem[] ss = sr.getServiceSubSystems(localHost, IFileService.class);
		for (int i=0; i<ss.length; i++) {
			if (ss[i] instanceof IFileServiceSubSystem) {
				fss = (IFileServiceSubSystem)ss[i];
				fs = fss.getFileService();
			}
		}
		localFss = fss;  //Used for creating test source data.
		assertNotNull(localFss);
	}

}
