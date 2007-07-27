/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [183824] Forward SystemMessageException from IRemoteFileSubsystem
 ********************************************************************************/

package org.eclipse.rse.services.files;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

/**
 * A IFileService is an abstraction of a file service that runs over some sort of connection.
 * It can be shared among multiple instances of a subsystem.  At some point this file
 * service layer may become official API but for now it is experimental.  Each
 * subsystem is currently responsible for layering an abstraction over whatever it 
 * wants to construct as a service.
 * <p>
 * This is a very bare bones definition.  A real definition would probably have changed
 * terminology, use URI's rather than Strings, and have much more robust error handling.
 * <p>
 * Implementers of this interface will have to either be instantiated, initialized, or
 * somehow derive a connection as part of its state.
 */
public interface IFileService extends IService
{	

	/**
	 * Copy a file to the remote file system.  The remote target is denoted by a
	 * string representing the parent and a string representing the file.
	 * @param stream input stream to transfer
	 * @param remoteParent - a string designating the parent folder of the target for this file.
	 * @param remoteFile - a string designating the name of the file to be written on the remote system.
	 * @param isBinary - indicates whether the file is text or binary
	 * @param hostEncoding - the tgt encoding of the file (if text)
	 * @param monitor the monitor for this potentially long running operation
	 * @return true if the file was uploaded
	 * @throws SystemMessageException if an error occurs. 
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public boolean upload(InputStream stream, String remoteParent, String remoteFile, boolean isBinary, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException;
	
	/**
	 * Copy a file to the remote file system.  The remote target is denoted by a
	 * string representing the parent and a string representing the file.
	 * @param localFile - a real file in the local file system.
	 * @param remoteParent - a string designating the parent folder of the target for this file.
	 * @param remoteFile - a string designating the name of the file to be written on the remote system.
	 * @param isBinary - indicates whether the file is text or binary
	 * @param srcEncoding - the src encoding of the file (if text)
	 * @param hostEncoding - the tgt encoding of the file (if text)
	 * @param monitor the monitor for this potentially long running operation
	 * @return true if the file was uploaded
	 * @throws SystemMessageException if an error occurs. 
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 */
	public boolean upload(File localFile, String remoteParent, String remoteFile, boolean isBinary, String srcEncoding, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Copy a file from the remote file system to the local system.
	 * @param remoteParent - a String designating the remote parent.
	 * @param remoteFile - a String designating the remote file residing in the parent.
	 * @param localFile - The file that is to be written.  If the file exists it is 
	 * overwritten.
	 * @param isBinary - indicates whether the file is text on binary
	 * @param hostEncoding - the encoding on the host (if text)
	 * @param monitor the monitor for this potentially long running operation
	 * @return true if the file was copied from the remote system.
	 * @throws SystemMessageException if an error occurs. 
	 *     Typically this would be one of those in the 
	 *     {@link RemoteFileException} family.
	 */
	public boolean download(String remoteParent, String remoteFile, File localFile, boolean isBinary, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * @param remoteParent
	 * @param name
	 * @param monitor the monitor for this potentially long running operation
	 * @return the host file given the parent path and file name.
	 *     Must not return <code>null</code>, non-existing files should be
	 *     reported with an IHostFile object where {@link IHostFile#exists()}
	 *     returns <code>false</code>.
	 * @throws SystemMessageException if an error occurs. 
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public IHostFile getFile(String remoteParent, String name, IProgressMonitor monitor) throws SystemMessageException;
	
	/**
	 * @param remoteParent - the name of the parent directory on the remote file 
	 * system from which to retrieve the child list.
	 * @param fileFilter - a string that can be used to filter the children.  Only
	 * those files matching the filter make it into the list.  The interface 
	 * does not dictate where the filtering occurs.
	 * @param monitor the monitor for this potentially long running operation
	 * @return the list of host files. 
	 * @throws SystemMessageException if an error occurs. 
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public IHostFile[] getFilesAndFolders(String remoteParent, String fileFilter, IProgressMonitor monitor) throws SystemMessageException;
  
	/**
	 * @param remoteParent - the name of the parent directory on the remote file 
	 * system from which to retrieve the child list.
	 * @param fileFilter - a string that can be used to filter the children.  Only
	 * those files matching the filter make it into the list.  The interface 
	 * does not dictate where the filtering occurs.
	 * @param monitor the monitor for this potentially long running operation
	 * @return the list of host files. 
	 * @throws SystemMessageException if an error occurs. 
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public IHostFile[] getFiles(String remoteParent, String fileFilter, IProgressMonitor monitor) throws SystemMessageException;
  
	/**
	 * @param remoteParent - the name of the parent directory on the remote file 
	 * system from which to retrieve the child list.
	 * @param fileFilter - a string that can be used to filter the children.  Only
	 * those files matching the filter make it into the list.  The interface 
	 * does not dictate where the filtering occurs.
	 * @param monitor the monitor for this potentially long running operation
	 * @return the list of host files. 
	 * @throws SystemMessageException if an error occurs. 
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public IHostFile[] getFolders(String remoteParent, String fileFilter, IProgressMonitor monitor) throws SystemMessageException;
  
	/**
	 * @param monitor the monitor for this potentially long running operation
	 * Return the list of roots for this system
	 * @return the list of host files. 
	 * @throws SystemMessageException if an error occurs. 
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public IHostFile[] getRoots(IProgressMonitor monitor) throws SystemMessageException;
	
	/**
	 * @return the String containing the name of the user's home directory on this
	 * connection that would be contained in implementations of this service.
	 */
	public IHostFile getUserHome();

	/**
	 * Create a file on the host
	 * @param remoteParent the parent directory
	 * @param fileName the name of the new file
	 * @param monitor the monitor for this potentially long running operation
	 * @return the newly created file
	 * @throws SystemMessageException if an error occurs. 
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public IHostFile createFile(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException;
	
	/**
	 * Create a folder on the host
	 * @param remoteParent the parent directory
	 * @param folderName the name of the new folder
	 * @param monitor the progress monitor
	 * @return the newly created folder
	 * @throws SystemMessageException if an error occurs. 
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public IHostFile createFolder(String remoteParent, String folderName, IProgressMonitor monitor) throws SystemMessageException;
	
	/**
	 * Deletes a file or folder on the host
	 * @param remoteParent the folder containing the file to delete
	 * @param fileName the name of the file or folder to delete
	 * @param monitor the progress monitor
	 * @return true if successful
	 * @throws SystemMessageException if an error occurs. 
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public boolean delete(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Deletes a set of files or folders on the host. Should throw an exception if some files and folders were deleted and others were not
	 * due to an exception during the operation. Without an exception thrown in such cases, views may not be refreshed correctly to account
	 * for deleted resources.
	 * @param remoteParents the array of folders containing the files to delete
	 * @param fileNames the names of the files or folders to delete
	 * @param monitor the progress monitor
	 * @return true iff all deletes are successful
	 * @throws SystemMessageException if an error occurs. 
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public boolean deleteBatch(String[] remoteParents, String[] fileNames, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Renames a file or folder on the host
	 * @param remoteParent the folder containing the file to rename
	 * @param oldName the old name of the file or folder to rename
	 * @param newName the new name for the file
	 * @param monitor the progress monitor
	 * @return true if successful
	 * @throws SystemMessageException if an error occurs. 
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public boolean rename(String remoteParent, String oldName, String newName, IProgressMonitor monitor) throws SystemMessageException;
	
	/**
	 * Renames a file or folder on the host
	 * @param remoteParent the folder containing the file to rename
	 * @param oldName the old name of the file or folder to rename
	 * @param newName the new name for the file
	 * @param oldFile the file to update with the change
	 * @param monitor the progress monitor
	 * @return true if successful
	 * @throws SystemMessageException if an error occurs. 
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public boolean rename(String remoteParent, String oldName, String newName, IHostFile oldFile, IProgressMonitor monitor) throws SystemMessageException;
	
	/**
	 * Move the file or folder specified
	 * @param srcParent the folder containing the file or folder to move
	 * @param srcName the new of the file or folder to move
	 * @param tgtParent the destination folder for the move
	 * @param tgtName the name of the moved file or folder
	 * @param monitor the progress monitor
	 * @return true if the file was moved
	 * @throws SystemMessageException if an error occurs. 
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public boolean move(String srcParent, String srcName, String tgtParent, String tgtName, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Copy the file or folder to the specified destination
	 * @param srcParent the folder containing the file or folder to copy
	 * @param srcName the new of the file or folder to copy
	 * @param tgtParent the destination folder for the copy
	 * @param tgtName the name of the copied file or folder
	 * @param monitor the progress monitor
	 * @return true if the file was copied successfully
	 * @throws SystemMessageException if an error occurs. 
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public boolean copy(String srcParent, String srcName, String tgtParent, String tgtName, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Copy a set of files or folders to the specified destination
	 * @param srcParents the folders containing each file or folder to copy
	 * @param srcNames the names of the files or folders to copy
	 * @param tgtParent the destination folder for the copy
	 * @param monitor the progress monitor
	 * @return true if all files were copied
	 * @throws SystemMessageException if an error occurs. 
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public boolean copyBatch(String[] srcParents, String[] srcNames, String tgtParent, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Indicates whether the file system is case sensitive
	 * @return true if the file system has case sensitive file names
	 */
	public boolean isCaseSensitive();
	
	/**
	 * Sets the last modified stamp of the file or folder with the specified timestamp
	 * @param parent the parent path of the file to set
	 * @param name the name of the file to set
	 * @param timestamp the new timestamp  
	 * @param monitor the progress monitor
	 * @return true if the file timestamp was changed successfully
	 */
	public boolean setLastModified(String parent, String name, long timestamp, IProgressMonitor monitor) throws SystemMessageException;
	
	/**
	 * Sets the readonly permission of the file or folder
	 * @param parent the parent path of the file to set
	 * @param name the name of the file to set
	 * @param readOnly indicates whether to make the file readonly or read-write
	 * @param monitor the progress monitor
	 * @return true if the readonly permission was changed successfully
	 */
	public boolean setReadOnly(String parent, String name, boolean readOnly, IProgressMonitor monitor) throws SystemMessageException;
	
	/**
	 * Gets the remote encoding.
	 * @param monitor the progress monitor.
	 * @return the encoding.
	 * @throws SystemMessageException if an error occurs.
	 * @since 2.0
	 */
	public String getEncoding(IProgressMonitor monitor) throws SystemMessageException;
	
	/**
	 * Gets the input stream to access the contents a remote file. Clients should close the input stream when done.
	 * @param remoteParent the absolute path of the parent.
	 * @param remoteFile the name of the remote file.
	 * @param isBinary <code>true</code> if the file is a binary file, <code>false</code> otherwise.
	 * @param monitor the progress monitor.
	 * @return the input stream to access the contents of the remote file.
	 * @throws SystemMessageException if an error occurs. 
	 * @since 2.0
	 */
	public InputStream getInputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException;
	
	/**
	 * Gets the output stream to write to a remote file. Clients should close the output stream when done.
	 * @param remoteParent the absolute path of the parent.
	 * @param remoteFile the name of the remote file.
	 * @param isBinary <code>true</code> if the file is a binary file, <code>false</code> otherwise.
	 * @param monitor the progress monitor.
	 * @return the input stream to access the contents of the remote file.
	 * @throws SystemMessageException if an error occurs.
	 * @since 2.0
	 */
	public OutputStream getOutputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException;
}