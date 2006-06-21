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
 ********************************************************************************/

package org.eclipse.rse.services.files.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.AbstractFileService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;


import sun.net.TelnetInputStream;
import sun.net.ftp.FtpClient;



public class FTPService extends AbstractFileService implements IFileService, IFTPService
{
	private FTPClientService _ftpClient;
	private String    _userHome;
	private IFTPDirectoryListingParser _ftpPropertiesUtil;
	
	private transient String _hostname;
	private transient String _userId;
	private transient String _password;
	private transient int _portNumber;
	private URLConnection _urlConnection;
	
	public FTPService()
	{		
	}
	
	public String getName()
	{
		return FTPServiceResources.FTP_File_Service_Name;
	}
	
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
		FtpClient ftp = getFTPClient(); 
		if (_portNumber == 0) {
			ftp.openServer(_hostname);
		} else {
			ftp.openServer(_hostname, _portNumber);
		}
		ftp.login(_userId, _password);
		
		_userHome = ftp.pwd();
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
			getFTPClient().closeServer();
			_ftpClient = null;
		}
		catch (Exception e)
		{
		//	e.printStackTrace();
			_ftpClient = null;
		}
	}
	
	public IFTPDirectoryListingParser getDirListingParser()
	{
		if (_ftpPropertiesUtil == null)
		{
			_ftpPropertiesUtil = new FTPLinuxDirectoryListingParser();
		}
		return _ftpPropertiesUtil;
	}
	
	public FTPClientService getFTPClient()
	{
		if (_ftpClient == null)
		{
			_ftpClient = new FTPClientService();
		}
		return _ftpClient;
	}
	
	public IHostFile getFile(IProgressMonitor monitor, String remoteParent, String fileName)
	{
		IHostFile[] matches = internalFetch(monitor, remoteParent, fileName, FILE_TYPE_FILES_AND_FOLDERS);
		if (matches != null && matches.length > 0)
		{
			return matches[0];
		}
		else
		{
			return new FTPHostFile(remoteParent, fileName, false, false, 0, 0, false);
		}
	}
	public boolean isConnected()
	{
		return getFTPClient().serverIsOpen();
	}
	
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
			FtpClient ftp = getFTPClient();
			try
			{
				ftp.noop();
			}
			catch (Exception e)
			{
				//e.printStackTrace();
				disconnect();
				
				// probably timed out
				reconnect();
				ftp = getFTPClient();
			}
			
			ftp.cd(parentPath);
			TelnetInputStream stream = ftp.list();		
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line = reader.readLine();
			while (line != null)
			{
				FTPHostFile node = getDirListingParser().getFTPHostFile(line, parentPath);
				if (node != null && filematcher.matches(node.getName()))
				{
					if (isRightType(fileType, node))
					{
						results.add(node);
					}
				}
				line = reader.readLine();
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
	
    private OutputStream getUploadStream(String remotePath, boolean isBinary) throws Exception 
    {
    	String typecode = isBinary ? "i" : "a";
    	remotePath = "%2F" + remotePath;
    	remotePath = remotePath.replaceAll(" ", "%20");
    	URL url = new URL("ftp://" + _userId + ":" + _password + "@" + _hostname + "/" + remotePath + ";type=" + typecode);
    	_urlConnection = url.openConnection();
    	return _urlConnection.getOutputStream();
    }
    
	public boolean upload(IProgressMonitor monitor, InputStream stream, String remoteParent, String remoteFile, boolean isBinary, String hostEncoding)
	{
		try
		{
			BufferedInputStream bis = new BufferedInputStream(stream);
			String remotePath = remoteParent + getSeparator() + remoteFile;
			OutputStream os = getUploadStream(remotePath, isBinary);
			byte[] buffer = new byte[1024];
			int readCount;
			while( (readCount = bis.read(buffer)) > 0) 
			{
			     os.write(buffer, 0, readCount);
			}
			os.close();
			bis.close();
			_urlConnection = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean download(IProgressMonitor monitor, String remoteParent, String remoteFile, File localFile, boolean isBinary, String hostEncoding)
	{
		FtpClient ftp = getFTPClient();
		try
		{
			ftp.cd(remoteParent);
			/*
			if (isBinary)
				ftp.binary();			
			else
				ftp.ascii();
				*/
			// for now only binary seems to work
			ftp.binary();
			
			InputStream is = ftp.get(remoteFile);
			BufferedInputStream bis = new BufferedInputStream(is);
			
			if (!localFile.exists())
			{
				File localParentFile = localFile.getParentFile();
				if (!localParentFile.exists())
				{
					localParentFile.mkdirs();
				}
				localFile.createNewFile();
			}
			OutputStream os = new FileOutputStream(localFile);
			BufferedOutputStream bos = new BufferedOutputStream(os);
	
			 byte[] buffer = new byte[1024];
			 int totalWrote = 0;
			 int readCount;
			 while( (readCount = bis.read(buffer)) > 0) 
			 {
			      bos.write(buffer, 0, readCount);
			      totalWrote += readCount;
			 }
			 bos.close();
	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	public IHostFile getUserHome()
	{
		int lastSlash = _userHome.lastIndexOf('/');
		String name = _userHome.substring(lastSlash + 1);	
		String parent = _userHome.substring(0, lastSlash);
		return getFile(null, parent, name);
	}

	public IHostFile[] getRoots(IProgressMonitor monitor) 
	{
		IHostFile root = new FTPHostFile("/", "/", true, true, 0, 0, true);
		return new IHostFile[] { root };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#delete(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String)
	 */
	public boolean delete(IProgressMonitor monitor, String remoteParent, String fileName) {
		boolean hasSucceeded = false;
		try {
			getFTPClient().cd(remoteParent);
			int returnedValue = getFTPClient().sendCommand("DELE " + fileName);
			hasSucceeded = (returnedValue > 0);
		} catch (IOException e) {
			// Changing folder raised an exception
			hasSucceeded = false;
		}
		return hasSucceeded;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#rename(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean rename(IProgressMonitor monitor, String remoteParent, String oldName, String newName) {
		boolean hasSucceeded = false;
		int returnedValue = getFTPClient().sendCommand("RNFR " + remoteParent + getSeparator() + oldName);
		if (returnedValue > 0) {
			returnedValue = getFTPClient().sendCommand("RNTO " + remoteParent + getSeparator() + newName);
			hasSucceeded = (returnedValue > 0);
		}
		return hasSucceeded;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#rename(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.lang.String, org.eclipse.rse.services.files.IHostFile)
	 */
	public boolean rename(IProgressMonitor monitor, String remoteParent, String oldName, String newName, IHostFile oldFile) {
		boolean hasSucceeded = false;
		int returnedValue = getFTPClient().sendCommand("RNFR " + remoteParent + getSeparator() + oldName);
		if (returnedValue > 0) {
			returnedValue = getFTPClient().sendCommand("RNTO " + remoteParent + getSeparator() + newName);
			hasSucceeded = (returnedValue > 0);
		}
		return hasSucceeded;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#move(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean move(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName) {
		boolean hasSucceeded = false;
		int returnedValue = getFTPClient().sendCommand("RNFR " + srcParent + getSeparator() + srcName);
		if (returnedValue > 0) {
			returnedValue = getFTPClient().sendCommand("RNTO " + tgtParent + getSeparator() + tgtName);
			hasSucceeded = (returnedValue > 0);
		}
		return hasSucceeded;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#createFolder(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String)
	 */
	public IHostFile createFolder(IProgressMonitor monitor, String remoteParent, String folderName) 
	{
		try
		{
			FTPClientService ftp = getFTPClient();
			ftp.cd(remoteParent);
			ftp.sendCommand("MKD " + folderName);
		}
		catch (Exception e)
		{
			e.printStackTrace();			
		}
		return getFile(monitor, remoteParent, folderName);
	}

	// TODO
	/********************************************************
	 * 
	 *    The following APIs need to be implemented
	 * 
	 ********************************************************/
	
	public IHostFile createFile(IProgressMonitor monitor, String remoteParent, String fileName) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean copy(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName) 
	{
		return false;
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