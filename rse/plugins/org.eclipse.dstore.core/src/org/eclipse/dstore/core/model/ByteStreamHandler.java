/*******************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 *  David McKnight     (IBM)   [224906] [dstore] changes for getting properties and doing exit due to single-process capability
 *  David McKnight     (IBM)   [281712] [dstore] Warning message is needed when disk is full
 *  David McKnight     (IBM)   [367424] [dstore] upload mechanism should provide backups of files
 *  David McKnight     (IBM)   [380023] [dstore] remote file permissions lost after upload
 *  David McKnight     (IBM)   [385630] [dstore] backup files created during upload should be removed when upload successful
 *  David McKnight     (IBM)   [400251] [dstore] backup files cause problem when parent folder is read-only
 *  David McKnight   (IBM) - [414016] [dstore] new server audit log requirements
 *******************************************************************************/

package org.eclipse.dstore.core.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.dstore.core.server.SecuredThread;

/**
 * <p>
 * The ByteStreamHandler class is used to abstract file read and write operations
 * across the network.  By default this is used for sending and receiving files
 * on the client and the server.  The class can be extended if the default byte stream
 * implementations are not sufficient for a particular platform or use.  
 * </p>
 * <p>
 * If ByteStreamHandler is extended, you need to tell the DataStore to use the
 * extended implementation.  To do that, call <code>DataStore.setByteStreamHandler(ByteStreamHandler)</code>.
 * </p>
 * 
 */
public class ByteStreamHandler implements IByteStreamHandler
{

	protected DataStore _dataStore;
	protected DataElement _log;
	protected static final String FILEMSG_REMOTE_SAVE_FAILED = "RSEF5006"; //$NON-NLS-1$
	
	// for file backups
	private boolean _doBackups = true;
	private boolean _keepBackups = false;
	
	/**
	 * Constructor
	 * @param dataStore the DataStore instance
	 */
	public ByteStreamHandler(DataStore dataStore, DataElement log)
	{		
		_dataStore = dataStore;
		_log = log;
		
		String doBackups = System.getProperty("backupfiles"); //$NON-NLS-1$
		_doBackups = (doBackups == null || doBackups.equals("true")); //$NON-NLS-1$
		
		if (_doBackups){
			String keepBackups = System.getProperty("keepbackupfiles"); //$NON-NLS-1$
			
			// default is not NOT keep backups
			_keepBackups = (keepBackups != null && keepBackups.equals("true")); //$NON-NLS-1$
		}
	}

	public String getId()
	{
		return getClass().getName();
	}
	
	class DeleteBackupThread extends SecuredThread {
		private File _currentFile;
		private File _backupFile;
		private long _initialLength;
		public DeleteBackupThread(DataStore dataStore, File currentFile, File backupFile){
			super(dataStore);
			_currentFile = currentFile;
			_backupFile = backupFile;
			_initialLength = _currentFile.length(); // get initial length so we can see if upload is still happening
		}
		
		public void run(){
			super.run();
			boolean doneDelete = false;
						
			while (!doneDelete){
				try {
					Thread.sleep(10000); // wait 10 seconds
				}
				catch (InterruptedException e){				
				}
				
				// make sure there was no disconnect
				if (!_dataStore.isConnected()){
					// keep the backup
					doneDelete = true;
				}
				else {
					long curLength = _currentFile.length();
					if (curLength == _initialLength){ // looks like total upload is complete
						_backupFile.delete();	
						doneDelete = true;
					}		
					else {
						_initialLength = curLength;
					}
				}
			}	
		}
	}
	
	private void deleteBackupFile(File currentFile, File backupFile){
		if (backupFile != null  && !_keepBackups){ // only matters if there is a backup file
			DeleteBackupThread thread = new DeleteBackupThread(_dataStore, currentFile, backupFile);
			thread.start();
		}
	}
	
	private void backupFile(File file, File backupFile){
		
		/* this is nice but orginal file permissions not perserved
		if(!file.renameTo(backupFile) && backupFile.exists()) {
			backupFile.delete();	
			file.renameTo(backupFile);
		}
		*/
		
		/* in order to preserve original permissions for orignal file we can't rename
		 * instead we need to copy the file over		 
		 */
		FileInputStream inputStream = null;
		FileOutputStream backupFileStream = null;
		try {
			inputStream = new FileInputStream(file);
			backupFileStream = new FileOutputStream(backupFile);
			
			byte[] buffer = new byte[512000];
			long totalSize = file.length();
			int totalRead = 0;

			while (totalRead < totalSize){
				int available = inputStream.available();
				available = (available < 512000) ? available : 512000;

				int bytesRead = inputStream.read(buffer, 0, available);
				if (bytesRead == -1) {
					break;
				}
				backupFileStream.write(buffer, 0, bytesRead);				
				totalRead += bytesRead;
			}			
		} catch (FileNotFoundException e) {
		} catch (IOException e){			
		} finally {
			try {
				inputStream.close();
				backupFileStream.close();
			}
			catch (IOException e){				
			}
		}
	}
	
	
	/**
	 * Save a file in the specified location.  This method is called by the
	 * DataStore when the communication layer receives a file transfer    
	 *
	 * @param remotePath the path where to save the file
	 * @param buffer the bytes to insert in the file
	 * @param size the number of bytes to insert
	 * @param binary indicates whether to save the bytes as binary or text
	 */
	public void receiveBytes(String remotePath, byte[] buffer, int size, boolean binary)
	{
		remotePath = new String(remotePath.replace('\\', '/'));
		DataElement status = findStatusFor(remotePath);
		String fileName = _dataStore.mapToLocalPath(remotePath);
	
		if (fileName != null)
		{
			if (!_dataStore.isVirtual()){
				_dataStore.trace("Receiving Bytes for " + fileName); //$NON-NLS-1$
				String[] auditData = new String[] {"WRITE", remotePath, null, null}; //$NON-NLS-1$
				_dataStore.getClient().getLogger().logAudit(auditData);			
			}
			try
			{
				// need to create directories as well
				File file = new File(fileName);
				File backupFile = null;
				File parent = new File(file.getParent());
				if (!file.exists())
				{					
					parent.mkdirs();
				}
				else
				{
					if (!_dataStore.isVirtual()){ // only applies to server
						// backup file on upload by default
						if (_doBackups && parent.canWrite()){ 
							// backup the file first	
							String n = file.getName();			
							backupFile = new File(parent, '.' + n + '~');
							_dataStore.trace("Backing up as "+backupFile.getAbsolutePath()); //$NON-NLS-1$
							backupFile(file, backupFile);
						}
					}
				}

				File newFile = new File(fileName);				
				FileOutputStream fileStream = new FileOutputStream(newFile);

				if (binary)
				{
					fileStream.write(buffer, 0, size);
				}
				else
				{
					IByteConverter byteConverter = _dataStore.getByteConverter();
					byteConverter.setContext(file);
					byte[] convertedBytes = byteConverter.convertClientBytesToHostBytes(buffer, 0, size);
					fileStream.write(convertedBytes, 0, convertedBytes.length);
				}

				fileStream.close();
				
				deleteBackupFile(newFile, backupFile);
				
				if (!_dataStore.isVirtual()){
					String[] auditData = new String[] {"WRITE", remotePath, "0", null}; //$NON-NLS-1$ //$NON-NLS-2$
					_dataStore.getClient().getLogger().logAudit(auditData);
				}
				if (status == null)
					return;
				status.setAttribute(DE.A_SOURCE, "success"); //$NON-NLS-1$
				_dataStore.refresh(status.getParent());
			}
			catch (IOException e)
			{
				_dataStore.trace(e);
				if (status == null)
					return;
				status.setAttribute(DE.A_VALUE, FILEMSG_REMOTE_SAVE_FAILED);
				status.setAttribute(DE.A_SOURCE, "failed"); //$NON-NLS-1$
				_dataStore.refresh(status.getParent());
			}
			catch (Exception e)
			{
				_dataStore.trace(e);
				if (status == null)
					return;
				status.setAttribute(DE.A_VALUE, FILEMSG_REMOTE_SAVE_FAILED);
				status.setAttribute(DE.A_SOURCE, "failed"); //$NON-NLS-1$
				_dataStore.refresh(status.getParent());
			}
		}
	}

	/**
	 * Append a bytes to a file at a specified location. This method is called by the
	 * DataStore when the communication layer receives a file transfer append.      
	 *
	 * @param remotePath the path where to save the file
	 * @param buffer the bytes to append in the file
	 * @param size the number of bytes to append in the file
	 * @param binary indicates whether to save the bytes as binary or text
	 */
	public void receiveAppendedBytes(String remotePath, byte[] buffer, int size, boolean binary)
	{
		remotePath = new String(remotePath.replace('\\', '/'));
		DataElement status = findStatusFor(remotePath);
		String fileName = _dataStore.mapToLocalPath(remotePath);

		if (fileName != null)
		{
			if (!_dataStore.isVirtual()){
				_dataStore.trace("Receiving Appended Bytes for " + fileName); //$NON-NLS-1$
				String[] auditData = new String[] {"WRITE", remotePath, null, null}; //$NON-NLS-1$
				_dataStore.getClient().getLogger().logAudit(auditData);
			}
			try
			{
				// need to create directories as well
				File file = new File(fileName);
				File parent = new File(file.getParent());
				if (!file.exists())
				{					
					parent.mkdirs();

					File newFile = new File(fileName);
					FileOutputStream fileStream = new FileOutputStream(newFile);

					if (binary)
					{
						fileStream.write(buffer, 0, size);
					}
					else
					{
						IByteConverter byteConverter = _dataStore.getByteConverter();
						byteConverter.setContext(file);
					
						byte[] convertedBytes = byteConverter.convertClientBytesToHostBytes(buffer, 0, size);
						fileStream.write(convertedBytes, 0, convertedBytes.length);
					}

					fileStream.close();
				}
				else
				{
					FileOutputStream outStream = new FileOutputStream(fileName, true);
					
					if (binary) 
					{
						outStream.write(buffer, 0, size);
					}
					else 
					{
						IByteConverter byteConverter = _dataStore.getByteConverter();
						byteConverter.setContext(file);
						byte[] convertedBytes = byteConverter.convertClientBytesToHostBytes(buffer, 0, size);
						outStream.write(convertedBytes, 0, convertedBytes.length);
					}
					
					outStream.close();

				}
				if (!_dataStore.isVirtual()){
					String[] auditData = new String[] {"WRITE", remotePath, "0", null}; //$NON-NLS-1$ //$NON-NLS-2$
					_dataStore.getClient().getLogger().logAudit(auditData);
				}
				if (status == null)
					return;
				status.setAttribute(DE.A_SOURCE, "success"); //$NON-NLS-1$
				_dataStore.refresh(status.getParent());
			}
			catch (IOException e)
			{
				_dataStore.trace(e);
				if (status == null)
					return;
				status.setAttribute(DE.A_VALUE, FILEMSG_REMOTE_SAVE_FAILED);
				status.setAttribute(DE.A_SOURCE, "failed"); //$NON-NLS-1$
				_dataStore.refresh(status.getParent());
			}
		}
	}

	

	/**
	 * Called by <code>sendBytes</code> to either save the bytes to a local file or transmit
	 * them to a remote file.
	 * @param path the path of the file
	 * @param bytes the bytes of the file
	 * @param size the size of the file
	 * @param binary indicates whether the bytes are to be sent as binary or text
	 */
	protected void internalSendBytes(String path, byte[] bytes, int size, boolean binary)
	{
		if (_dataStore.isVirtual())
		{
			_dataStore.replaceFile(path, bytes, size, binary);
		}
		else
		{
			_dataStore.updateFile(path, bytes, size, binary);
		}
	}

	/**
	 * Called by <code>sendBytes</code> to either append the bytes to a local file or transmit
	 * them and append them to a remote file.
	 * @param path the path of the file
	 * @param bytes the bytes of the file
	 * @param size the size of the file
	 * @param binary indicates whether the bytes are to be sent as binary or text
	 */
	protected void internalSendAppendBytes(String path, byte[] bytes, int size, boolean binary)
	{
		if (_dataStore.isVirtual())
		{
			_dataStore.replaceAppendFile(path, bytes, size, binary);
		}
		else
		{
			_dataStore.updateAppendFile(path, bytes, size, binary);
		}
	}

	protected DataElement findStatusFor(String remotePath)
	{
		if (_log != null)
		{
			for (int i = 0; i < _log.getNestedSize(); i++)
			{
				DataElement child = _log.get(i);
				if (child.getName().equals(remotePath))
				{
					return child;
				}
			}
		}
		return null;
	}

}
