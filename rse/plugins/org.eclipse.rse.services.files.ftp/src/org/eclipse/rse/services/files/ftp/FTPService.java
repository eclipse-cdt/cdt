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
 * David Dykstal (IBM) - Fixing 162511 - FTP file service does not process filter strings correctly
 * Javier Montalvo Orus (Symbian) - Fixing 162511 - FTP file service does not process filter strings correctly
 * Javier Montalvo Orus (Symbian) - Fixing 162782 - File filter does not display correct result in RC3
 * Javier Montalvo Orus (Symbian) - Fixing 162878 - New file and new folder dialogs don't work in FTP in a folder with subfolders
 * Javier Montalvo Orus (Symbian) - Fixing 162585 - [FTP] fetch children cannot be canceled
 * Javier Montalvo Orus (Symbian) - Fixing 161209 - Need a Log of ftp commands
 * Javier Montalvo Orus (Symbian) - Fixing 163264 - FTP Only can not delete first subfolder
 ********************************************************************************/

package org.eclipse.rse.services.files.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.services.clientserver.FileTypeMatcher;
import org.eclipse.rse.services.clientserver.IMatcher;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.AbstractFileService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.RemoteFileIOException;
import org.eclipse.rse.services.files.RemoteFolderNotEmptyException;


public class FTPService extends AbstractFileService implements IFileService, IFTPService
{
	private FTPClient _ftpClient;
	private FTPFile[] _ftpFiles;
	
	private String    _userHome;
	private transient String _hostName;
	private transient String _userId;
	private transient String _password;
	private transient int _portNumber;
	
	private OutputStream _ftpLoggingOutputStream;
	
	
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
		_hostName = hostname;
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
	
	public void setLoggingStream(OutputStream  ftpLoggingOutputStream)
	{
		 _ftpLoggingOutputStream =  ftpLoggingOutputStream;
	}

	public void connect() throws Exception
	{
		if (_ftpClient == null)
		{
			_ftpClient = new FTPClient();
			
		}

		if(_ftpLoggingOutputStream!=null)
		{
			_ftpClient.registerSpyStream(_ftpLoggingOutputStream);
		}
		
		if (_portNumber == 0) {
			_ftpClient.connect(_hostName);
		} else {
			_ftpClient.connect(_hostName, _portNumber);
		}
		_ftpClient.login(_userId, _password);
		
		_userHome = _ftpClient.printWorkingDirectory();
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
		
		if(_hostName!=null)
		{
			try{
				_ftpClient.sendNoOp();
			}catch (IOException e){
				try {
					connect();
				} catch (Exception e1) {}
			}
		}
			
		
		return _ftpClient; 
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#getFile(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String)
	 */
	public IHostFile getFile(IProgressMonitor monitor, String remoteParent, String fileName) throws SystemMessageException 
	{
		if (monitor!=null){
			if (monitor.isCanceled()) {
				return null;
			}	
		}
		
		FTPHostFile file = null;
		
		try{
		
			//try to retrieve the file
			_ftpClient = getFTPClient();
			
			if(!_ftpClient.changeWorkingDirectory(remoteParent))
			{
				return null;
			}
			
			String systemName = _ftpClient.getSystemName();
			
			if(!listFiles(monitor))
			{
				return null;
			}
			
			for (int i = 0; i < _ftpFiles.length; i++) 
			{
				if(_ftpFiles[i].getName().equalsIgnoreCase(fileName))
				{
					file = new FTPHostFile(remoteParent,_ftpFiles[i],systemName);
					break;
				}
			}
			
			// if not found, create new object with non-existing flag
			if(file == null)
			{
				file = new FTPHostFile(remoteParent,fileName, false, false, 0, 0, false);
			}
		
		
		}catch (Exception e){
			throw new RemoteFileIOException(e);
		}
		
		return file;
	}
	
	public boolean isConnected()
	{
		boolean isConnected = false;
		
		if(_ftpClient!=null) {
			isConnected =  _ftpClient.isConnected();
		}
		
		return isConnected;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.AbstractFileService#internalFetch(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, int)
	 */
	protected IHostFile[] internalFetch(IProgressMonitor monitor, String parentPath, String fileFilter, int fileType) throws SystemMessageException
	{
		if (monitor!=null){
			if (monitor.isCanceled()) {
				return null;
			}	
		}
		
		
		if (fileFilter == null)
		{
			fileFilter = "*"; //$NON-NLS-1$
		}
		IMatcher filematcher = null;
		if (fileFilter.endsWith(",")) {  //$NON-NLS-1$
			String[] types = fileFilter.split(",");  //$NON-NLS-1$
			filematcher = new FileTypeMatcher(types, true);
		} else {
			filematcher = new NamePatternMatcher(fileFilter, true, true);
		}
		List results = new ArrayList();
		
		try
		{
			_ftpClient = getFTPClient();
			
			if(!_ftpClient.changeWorkingDirectory(parentPath))
			{
				return null;
			}
			
			if(!listFiles(monitor))
			{
				return null;
			}
			
			String systemName = _ftpClient.getSystemName();
			
			for(int i=0; i<_ftpFiles.length; i++)
			{
				FTPFile f = _ftpFiles[i];
				if(filematcher.matches(f.getName()) || f.isDirectory())
				{
					results.add(new FTPHostFile(parentPath,_ftpFiles[i],systemName));
				}
			}
		}
		catch (Exception e)
		{			
			throw new RemoteFileIOException(e);
		}
		
		return (IHostFile[])results.toArray(new IHostFile[results.size()]);
	}
	
	
	public String getSeparator()
	{
		return "/";  //$NON-NLS-1$
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#upload(org.eclipse.core.runtime.IProgressMonitor, java.io.File, java.lang.String, java.lang.String, boolean, java.lang.String, java.lang.String)
	 */
	public boolean upload(IProgressMonitor monitor, File localFile, String remoteParent, String remoteFile, boolean isBinary, String srcEncoding, String hostEncoding) throws SystemMessageException
	{ 
		boolean retValue = true;
		
		if (monitor!=null){
			if (monitor.isCanceled()) {
				return false;
			}	
		}
		
		FTPClient ftpClient = getFTPClient();
		
		MyProgressMonitor progressMonitor = new MyProgressMonitor(monitor);
		
		try
		{
			
			ftpClient.changeWorkingDirectory(remoteParent);
				
			if (isBinary)
				ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);			
			else
				ftpClient.setFileTransferMode(FTP.ASCII_FILE_TYPE);
			
			FileInputStream input =  new FileInputStream(localFile);
			OutputStream output = ftpClient.storeFileStream(remoteFile);
			
			progressMonitor.init(0, localFile.getName(), remoteFile, localFile.length());
			
			byte[] buffer = new byte[4096];
			
			int readCount;
			while((readCount = input.read(buffer)) > 0)
			{
				output.write(buffer, 0, readCount);
				progressMonitor.count(readCount);
				if (monitor!=null){
					if (monitor.isCanceled()) {
						retValue = false;
						break;
					}	
				}
			}
			
			input.close();
			output.flush();
			output.close();
			
			ftpClient.completePendingCommand();
			
			if(retValue==false)	{
				ftpClient.deleteFile(remoteFile);
			}
			
			progressMonitor.end();
		}
		catch (Exception e)
		{
			throw new RemoteFileIOException(e);
		}
		
		return retValue;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#upload(org.eclipse.core.runtime.IProgressMonitor, java.io.InputStream, java.lang.String, java.lang.String, boolean, java.lang.String)
	 */
	public boolean upload(IProgressMonitor monitor, InputStream stream, String remoteParent, String remoteFile, boolean isBinary, String hostEncoding) throws SystemMessageException
	{
		boolean retValue = true;
		
		try
		{
			BufferedInputStream bis = new BufferedInputStream(stream);
			File tempFile = File.createTempFile("ftpup", "temp"); //$NON-NLS-1$ //$NON-NLS-2$
			FileOutputStream os = new FileOutputStream(tempFile);
			BufferedOutputStream bos = new BufferedOutputStream(os);
	
			 byte[] buffer = new byte[4096];
			 int readCount;
			 while( (readCount = bis.read(buffer)) > 0) 
			 {
			      bos.write(buffer, 0, readCount);
			      if (monitor!=null){
					if (monitor.isCanceled()) {
						retValue = false;
						break;
					}	
				}
			 }
			 bos.close();
			 
			 if(retValue == true){
				 retValue = upload(monitor, tempFile, remoteParent, remoteFile, isBinary, "", hostEncoding); //$NON-NLS-1$
			 }
			 
		}
		catch (Exception e) {
			throw new RemoteFileIOException(e);
		}
		return retValue;
	
		}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#download(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.io.File, boolean, java.lang.String)
	 */
	public boolean download(IProgressMonitor monitor, String remoteParent, String remoteFile, File localFile, boolean isBinary, String hostEncoding) throws SystemMessageException
	{
		
		if (monitor!=null){
			if (monitor.isCanceled()) {
				return false;
			}	
		}
		
		MyProgressMonitor progressMonitor = new MyProgressMonitor(monitor);
		
		IHostFile remoteHostFile = getFile(null,remoteParent,remoteFile);
		
		boolean retValue = false;

		FTPClient ftpClient = getFTPClient();
		
		try
		{
			
			ftpClient.changeWorkingDirectory(remoteParent);
			
			if (isBinary)
				ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);			
			else
				ftpClient.setFileTransferMode(FTP.ASCII_FILE_TYPE);
			
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
			InputStream input = ftpClient.retrieveFileStream(remoteFile);
			
			progressMonitor.init(0, remoteFile, localFile.getName(), remoteHostFile.getSize());
			
			byte[] buffer = new byte[4096];
			
			int readCount;
			while((readCount = input.read(buffer)) > 0)
			{
				output.write(buffer, 0, readCount);
				progressMonitor.count(readCount);
				if (monitor!=null){
					if (monitor.isCanceled()) {
						retValue = false;
						break;
					}	
				}
			}
			
			progressMonitor.end();
			
			output.flush();
			input.close();
			output.close();
			
			ftpClient.completePendingCommand();
		}
		catch (Exception e)
		{			
			throw new RemoteFileIOException(e);
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
		if(parent.equals(""))  //$NON-NLS-1$
		{
			parent = "/";  //$NON-NLS-1$
		}
		
		return new FTPHostFile(parent,name,true,true,0,0,true);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#getRoots(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IHostFile[] getRoots(IProgressMonitor monitor) 
	{	
		return new IHostFile[]{new FTPHostFile(null, "/", true, true, 0, 0, true)};  //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#delete(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String)
	 */
	public boolean delete(IProgressMonitor monitor, String remoteParent, String fileName) throws SystemMessageException {
		boolean hasSucceeded = false;
		
		FTPClient ftpClient = getFTPClient();
		
		MyProgressMonitor progressMonitor = new MyProgressMonitor(monitor);
		
		progressMonitor.init(FTPServiceResources.FTP_File_Service_Deleting_Task+fileName, 1);  
		
		boolean isFile = getFile(null,remoteParent,fileName).isFile();
		
		try {
			hasSucceeded = FTPReply.isPositiveCompletion(ftpClient.cwd(remoteParent));
			
			if(hasSucceeded)
			{
				if(isFile)
				{
					hasSucceeded = ftpClient.deleteFile(fileName);
				}
				else
				{
					hasSucceeded = ftpClient.removeDirectory(fileName);
				}
			}
			
			if(!hasSucceeded){
				throw new Exception(ftpClient.getReplyString()+" ("+fileName+")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else
			{
				progressMonitor.worked(1);
			}
			
		}
		catch (Exception e) {
			if(isFile){
				throw new RemoteFileIOException(e);
			}
			else{
				throw new RemoteFolderNotEmptyException(e);
			}
				
				
		}

		return hasSucceeded;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#rename(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean rename(IProgressMonitor monitor, String remoteParent, String oldName, String newName) throws SystemMessageException {

		FTPClient ftpClient = getFTPClient(); 
		
		try {
			
			if(newName.startsWith("/"))  //$NON-NLS-1$
			{
				ftpClient.rename(remoteParent + getSeparator() + oldName, newName);
			}
			else
			{
				ftpClient.rename(remoteParent + getSeparator() + oldName, remoteParent + getSeparator() + newName);
			}
			
		
		} catch (Exception e) {
			throw new RemoteFileIOException(e);
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
	public boolean move(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName) throws SystemMessageException{
		
		boolean success = false;

		FTPClient ftpClient = getFTPClient(); 
		
		try {
						
			int returnedValue = getFTPClient().sendCommand("RNFR " + srcParent + getSeparator() + srcName);  //$NON-NLS-1$
			
			//350: origin file exits, ready for destination
			if (FTPReply.CODE_350 == returnedValue) {
				returnedValue = getFTPClient().sendCommand("RNTO " + tgtParent + getSeparator() + tgtName);  //$NON-NLS-1$
			}
		
			success = FTPReply.isPositiveCompletion(returnedValue);

			if(!success)
			{
				throw new Exception(getFTPClient().getReplyString());
			}
		
		}catch (Exception e) {
			throw new RemoteFileIOException(e);
		}
		
		return success;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#createFolder(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String)
	 */
	public IHostFile createFolder(IProgressMonitor monitor, String remoteParent, String folderName) throws SystemMessageException
	{
		
		FTPClient ftpClient = getFTPClient(); 
		
		try
		{
			
			if(!ftpClient.changeWorkingDirectory(remoteParent))
			{
				throw new Exception(ftpClient.getReplyString()+" ("+remoteParent+")");  //$NON-NLS-1$  //$NON-NLS-2$
			}
			
			if(!ftpClient.makeDirectory(folderName))
			{
				throw new Exception(ftpClient.getReplyString()+" ("+folderName+")");  //$NON-NLS-1$  //$NON-NLS-2$
			}
						
		}
		catch (Exception e)	{
			throw new RemoteFileIOException(e);
		}

		return getFile(monitor, remoteParent, folderName);
	}

    /* (non-Javadoc)
     * @see org.eclipse.rse.services.files.IFileService#createFile(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String)
     */
    public IHostFile createFile(IProgressMonitor monitor, String remoteParent, String fileName) throws SystemMessageException{

    	try {
			File tempFile = File.createTempFile("ftp", "temp");  //$NON-NLS-1$  //$NON-NLS-2$
			tempFile.deleteOnExit();
			boolean success = upload(monitor, tempFile, remoteParent, fileName, true, null, null);
			
			if(!success)
			{
				throw new Exception(getFTPClient().getReplyString());
			}
		}
		catch (Exception e) {			
			throw new RemoteFileIOException(e);
		}

		return getFile(monitor, remoteParent, fileName);
	}
    	
    public boolean copy(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName) throws SystemMessageException  
	{
    	throw new RemoteFileIOException(new Exception(FTPServiceResources.FTP_File_Service_Copy_Not_Supported)); 
    }
	
	public boolean copyBatch(IProgressMonitor monitor, String[] srcParents, String[] srcNames, String tgtParent) throws SystemMessageException 
	{
		boolean hasSucceeded = false;
		
		for(int i=0; i<srcNames.length; i++)
		{
			hasSucceeded = copy(monitor, srcParents[i], srcNames[i], tgtParent, srcNames[i]);
			if(!hasSucceeded)
			{
				break;
			}
		}
		
		return hasSucceeded;
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
	
	
	private boolean listFiles(IProgressMonitor monitor) throws Exception
	{
		boolean result = true;
		
		Job fetchJob = new Job(FTPServiceResources.FTP_File_Service_Listing_Job){ 
			protected IStatus run(IProgressMonitor monitor) {
				try {
					_ftpFiles = _ftpClient.listFiles();
				} catch (IOException e) {
					return new Status(IStatus.ERROR, "org.eclipse.rse.services.files.ftp",IStatus.ERROR,e.getMessage(),e);  //$NON-NLS-1$
				}
				return new Status(IStatus.OK,"org.eclipse.rse.services.files.ftp",IStatus.OK,FTPServiceResources.FTP_File_Service_Listing_Job_Success,null);  //$NON-NLS-1$ 
			}};
		
		IStatus fetchResult = null;	
		
		if(monitor!=null)
		{
			if(!monitor.isCanceled())
				fetchJob.schedule();
			else
				return false;
		}
		else
		{
			fetchJob.schedule();	
		}
		
		if(monitor!=null)
		{
			while(!monitor.isCanceled() && (fetchResult = fetchJob.getResult())==null)		
			{
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}
			}
			
			if(monitor.isCanceled() && fetchJob.getState()!=Job.NONE)
			{
				fetchJob.cancel();
				_ftpClient.completePendingCommand();
				result = false;
			}
		}
		else
		{
			fetchJob.join();
			fetchResult = fetchJob.getResult();
		}
		
		if(fetchResult==null)
		{
			result = false;
		}
		else
		{
			if(fetchResult.getSeverity()==IStatus.ERROR)
			{
				throw new RemoteFileIOException(new Exception(fetchResult.getException()));
			}
		}
		
		return result;
	}
	
	
	
	private class MyProgressMonitor
	{
		  private IProgressMonitor fMonitor;
		  private double fWorkPercentFactor;
		  private Long fMaxWorkKB;
		  private long fWorkToDate;
		  
		  public MyProgressMonitor(IProgressMonitor monitor) {
			  fMonitor = monitor;
		  }
		  
		  public void init(int op, String src, String dest, long max){
			  fWorkPercentFactor = 1.0 / max;
			  fMaxWorkKB = new Long(max / 1024L);
			  fWorkToDate = 0;
			  String srcFile = new Path(src).lastSegment();
			  String desc = srcFile;
			  fMonitor.beginTask(desc, (int)max);
		  }
		  
		  public void init(String label, int max){
			  fMonitor.beginTask(label, max);
		  }
		  		  
		  public boolean count(long count){
			  fWorkToDate += count;
			  Long workToDateKB = new Long(fWorkToDate / 1024L);
			  Double workPercent = new Double(fWorkPercentFactor * fWorkToDate);
			  String subDesc = MessageFormat.format(
					 FTPServiceResources.FTP_File_Service_Monitor_Format,  
					  new Object[] {
						workToDateKB, fMaxWorkKB, workPercent	  
					  });
			  fMonitor.subTask(subDesc);
		      fMonitor.worked((int)count);
		      return !(fMonitor.isCanceled());
		  }
		  
		  public void worked(int work){
			  fMonitor.worked(work);
		  }
		  
		  public void end(){
			  fMonitor.done();
		  }
	}
	
	
}
