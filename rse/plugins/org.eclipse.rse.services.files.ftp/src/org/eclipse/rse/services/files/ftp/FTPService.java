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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.services.files.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
		ftp.openServer(_hostname);
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
		return null;
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
		FtpClient ftp = getFTPClient();
		try
		{
			ftp.cd(remoteParent);
			if (isBinary) 
				ftp.binary();
			else 
				ftp.ascii();
			ftp.put(localFile.getAbsolutePath());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean upload(IProgressMonitor monitor, InputStream stream, String remoteParent, String remoteFile, boolean isBinary, String hostEncoding)
	{
		// hack for now
		try
		{
			BufferedInputStream bis = new BufferedInputStream(stream);
			File tempFile = File.createTempFile("ftp", "temp");
			FileOutputStream os = new FileOutputStream(tempFile);
			BufferedOutputStream bos = new BufferedOutputStream(os);
	
			 byte[] buffer = new byte[1024];
			 int readCount;
			 while( (readCount = bis.read(buffer)) > 0) 
			 {
			      bos.write(buffer, 0, readCount);
			 }
			 bos.close();
					
			FtpClient ftp = getFTPClient();
			try
			{				
				ftp.cd(remoteParent);				
				if (isBinary) 
					ftp.binary();
				else 
					ftp.ascii();
				ftp.put(tempFile.getAbsolutePath());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
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
		IHostFile root = new FTPHostFile("/", "/", true, true, 0, 0);
		return new IHostFile[] { root };
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

	public IHostFile createFolder(IProgressMonitor monitor, String remoteParent, String folderName) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean delete(IProgressMonitor monitor, String remoteParent, String fileName) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean rename(IProgressMonitor monitor, String remoteParent, String oldName, String newName) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean rename(IProgressMonitor monitor, String remoteParent, String oldName, String newName, IHostFile oldFile) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean move(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean copy(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName) {
		// TODO Auto-generated method stub
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