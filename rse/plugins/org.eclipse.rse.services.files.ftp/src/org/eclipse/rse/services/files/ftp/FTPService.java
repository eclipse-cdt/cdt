/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Michael Berger (IBM) - Fixing 140408 - FTP upload does not work
 * Javier Montalvo Orus (Symbian) - Fixing 140323 - provided implementation for 
 *    delete, move and rename.
 * Javier Montalvo Orus (Symbian) - Bug 140348 - FTP did not use port number
 * Michael Berger (IBM) - Fixing 140404 - FTP new file creation does not work
 * Javier Montalvo Orus (Symbian) - Migrate to jakarta commons net FTP client
 * Javier Montalvo Orus (Symbian) - Fixing 161211 - Cannot expand /pub folder as 
 *    anonymous on ftp.wacom.com
 * Javier Montalvo Orus (Symbian) - Fixing 161238 - [ftp] expand "My Home" node on 
 *    ftp.ibiblio.org as anonymous fails
 * Javier Montalvo Orus (Symbian) - Fixing 160922 - create folder/file fails for FTP service
 ********************************************************************************/

package org.eclipse.rse.services.files.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.AbstractFileService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;




public class FTPService extends AbstractFileService implements IFileService, IFTPService
{
	private FTPClient _ftpClient;
	
	private String    _userHome;
	
	private transient String _hostname;
	private transient String _userId;
	private transient String _password;
	private transient int _portNumber;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.IService#getName()
	 */
	public String getName()
	{
		return FTPServiceResources.FTP_File_Service_Name;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.IService#getDescription()
	 */
	public String getDescription()
	{
		return FTPServiceResources.FTP_File_Service_Description;
	}
	
	public void setHostName(String hostname)
	{
		_hostname = hostname;
	}
	
	public void setPortNumber(int portNumber) {
		_portNumber = portNumber;
	}
	
	public void setUserId(String userId)
	{
		_userId = userId;
	}
	
	public void setPassword(String password)
	{
		_password = password;
	}

	public void connect() throws Exception
	{
		FTPClient ftp = getFTPClient(); 
		if (_portNumber == 0) {
			ftp.connect(_hostname);
		} else {
			ftp.connect(_hostname, _portNumber);
		}
		ftp.login(_userId, _password);
		
		_userHome = ftp.printWorkingDirectory();
	}
	
	protected void reconnect()
	{
		try
		{
			connect();
		}
		catch (Exception e)
		{			
		}
	}
	
	public void disconnect()
	{
		try
		{
			getFTPClient().logout();
			_ftpClient = null;
		}
		catch (Exception e)
		{
			_ftpClient = null;
		}
	}
	
	public FTPClient getFTPClient()
	{
		if (_ftpClient == null)
		{
			_ftpClient = new FTPClient();
		}
		return _ftpClient; 
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#getFile(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String)
	 */
	public IHostFile getFile(IProgressMonitor monitor, String remoteParent, String fileName)
	{
		IHostFile[] matches = internalFetch(monitor, remoteParent, fileName, FILE_TYPE_FILES_AND_FOLDERS);
		
		if (matches != null && matches.length > 0)
		{
			return matches[0];
		}
		else
		{
			return new FTPHostFile(remoteParent,fileName, false, false, 0, 0, false);
		}
	}
	
	public boolean isConnected()
	{
		return getFTPClient().isConnected();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.AbstractFileService#internalFetch(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, int)
	 */
	protected IHostFile[] internalFetch(IProgressMonitor monitor, String parentPath, String fileFilter, int fileType)
	{
		
		if (fileFilter == null)
		{
			fileFilter = "*";
		}
		
		NamePatternMatcher filematcher = new NamePatternMatcher(fileFilter, true, true);
		List results = new ArrayList();
		
		try
		{
			FTPClient ftp = getFTPClient();
			try
			{
				ftp.noop();
			}
			catch (Exception e)
			{
				disconnect();
				
				reconnect();
				ftp = getFTPClient();
			}
			
			if(!ftp.changeWorkingDirectory(parentPath))
			{
				return null;
			}
			
			String systemName = ftp.getSystemName();
			
			FTPFile[] ftpFiles = ftp.listFiles();
			
			for(int i=0; i<ftpFiles.length; i++)
			{
				if(filematcher.matches(ftpFiles[i].getName()))
				{
					results.add(new FTPHostFile(parentPath,ftpFiles[i],systemName));
				}
			}
		}
		catch (Exception e)
		{			
			e.printStackTrace();
		}
		
		return (IHostFile[])results.toArray(new IHostFile[results.size()]);
	}
	
	
	public String getSeparator()
	{
		return "/";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#upload(org.eclipse.core.runtime.IProgressMonitor, java.io.File, java.lang.String, java.lang.String, boolean, java.lang.String, java.lang.String)
	 */
	public boolean upload(IProgressMonitor monitor, File localFile, String remoteParent, String remoteFile, boolean isBinary, String srcEncoding, String hostEncoding)
	{
		try
		{
			FileInputStream fis =  new FileInputStream(localFile);
			return upload(monitor, fis, remoteParent, remoteFile, isBinary, hostEncoding);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#upload(org.eclipse.core.runtime.IProgressMonitor, java.io.InputStream, java.lang.String, java.lang.String, boolean, java.lang.String)
	 */
	public boolean upload(IProgressMonitor monitor, InputStream stream, String remoteParent, String remoteFile, boolean isBinary, String hostEncoding)
	{
		boolean retValue = false;

		try
		{
			FTPClient ftp = getFTPClient();
			ftp.changeWorkingDirectory(remoteParent);
			
			if (isBinary)
				ftp.setFileTransferMode(FTP.BINARY_FILE_TYPE);			
			else
				ftp.setFileTransferMode(FTP.ASCII_FILE_TYPE);
			
			retValue = ftp.storeFile(remoteFile, stream);
			
			stream.close();

		}
		catch (Exception e)
		{			
			e.printStackTrace();
		}

		return retValue;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#download(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.io.File, boolean, java.lang.String)
	 */
	public boolean download(IProgressMonitor monitor, String remoteParent, String remoteFile, File localFile, boolean isBinary, String hostEncoding)
	{
		boolean retValue = false;

		try
		{
			FTPClient ftp = getFTPClient();
			ftp.changeWorkingDirectory(remoteParent);
			
			if (isBinary)
				ftp.setFileTransferMode(FTP.BINARY_FILE_TYPE);			
			else
				ftp.setFileTransferMode(FTP.ASCII_FILE_TYPE);
			
			if (!localFile.exists())
			{
				File localParentFile = localFile.getParentFile();
				if (!localParentFile.exists())
				{
					localParentFile.mkdirs();
				}
				localFile.createNewFile();
			}
			
			OutputStream output = new FileOutputStream(localFile);
			
			retValue = ftp.retrieveFile(remoteFile, output);
			
			output.flush();
			output.close();
		}
		catch (Exception e)
		{			
			e.printStackTrace();
		}

		return retValue;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#getUserHome()
	 */
	public IHostFile getUserHome()
	{
		
		int lastSlash = _userHome.lastIndexOf('/');
		String name = _userHome.substring(lastSlash + 1);	
		String parent = _userHome.substring(0, lastSlash);
		
		// if home is root
		if(parent.equals(""))
		{
			parent = "/";
		}
		
		return new FTPHostFile(parent,name,true,true,0,0,true);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#getRoots(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IHostFile[] getRoots(IProgressMonitor monitor) 
	{	
		return new IHostFile[]{new FTPHostFile(null, "/", true, true, 0, 0, true)};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#delete(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String)
	 */
	public boolean delete(IProgressMonitor monitor, String remoteParent, String fileName) {
		boolean hasSucceeded = false;

		try {
			getFTPClient().cwd(remoteParent);

			//attempt to remove an empty folder folder
			hasSucceeded = getFTPClient().removeDirectory(fileName);
			
			if(!hasSucceeded)
			{
				//attempt to remove a file
				hasSucceeded = getFTPClient().deleteFile(fileName);
			}
		}
		catch (IOException e) {			
			// Changing folder raised an exception
			hasSucceeded = false;
		}

		return hasSucceeded;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#rename(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean rename(IProgressMonitor monitor, String remoteParent, String oldName, String newName) {

		try {
			
			if(newName.startsWith("/"))
			{
				getFTPClient().rename(remoteParent + getSeparator() + oldName, newName);
			}
			else
			{
				getFTPClient().rename(remoteParent + getSeparator() + oldName, remoteParent + getSeparator() + newName);
			}
			
		
		} catch (IOException e) {
			return false;
		}

		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#rename(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.lang.String, org.eclipse.rse.services.files.IHostFile)
	 */
	public boolean rename(IProgressMonitor monitor, String remoteParent, String oldName, String newName, IHostFile oldFile) {
		boolean hasSucceeded = false;
				
		oldFile.renameTo(newName);

		return hasSucceeded;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#move(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean move(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName) {
		boolean hasSucceeded = false;

		try {
				
			int returnedValue;
			returnedValue = getFTPClient().sendCommand("RNFR " + srcParent + getSeparator() + srcName);
			
			if (returnedValue > 0) {
				returnedValue = getFTPClient().sendCommand("RNTO " + tgtParent + getSeparator() + tgtName);
				hasSucceeded = (returnedValue > 0);
			}
			
		}catch (IOException e) {}

		return hasSucceeded;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#createFolder(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String)
	 */
	public IHostFile createFolder(IProgressMonitor monitor, String remoteParent, String folderName) 
	{
		try
		{
			FTPClient ftp = getFTPClient();
			ftp.makeDirectory(folderName);
		}
		catch (Exception e)	{}

		return getFile(monitor, remoteParent, folderName);
	}

    /* (non-Javadoc)
     * @see org.eclipse.rse.services.files.IFileService#createFile(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String)
     */
    public IHostFile createFile(IProgressMonitor monitor, String remoteParent, String fileName) {

		try {
			File tempFile = File.createTempFile("ftp", "temp");
			tempFile.deleteOnExit();
			upload(monitor, tempFile, remoteParent, fileName, true, null, null);
		}
		catch (Exception e) {			
			e.printStackTrace();
		}

		return getFile(monitor, remoteParent, fileName);
	}
    
	// TODO
	/********************************************************
	 * 
	 *    The following APIs need to be implemented
	 * 
	 ********************************************************/
	
    public boolean copy(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName) 
	{
		return move(monitor, srcParent, srcName, tgtParent, tgtName);
	}
	
	public boolean copyBatch(IProgressMonitor monitor, String[] srcParents, String[] srcNames, String tgtParent) throws SystemMessageException 
	{
		boolean ok = true;
		for (int i = 0; i < srcParents.length; i++)
		{
			ok = ok && copy(monitor, srcParents[i], srcNames[i], tgtParent, srcNames[i]);
		}
		return ok;
	}

	public void initService(IProgressMonitor monitor)
	{
	}
	
	public void uninitService(IProgressMonitor monitor)
	{
	}

	public boolean isCaseSensitive()
	{
		return true;
	}
	
}