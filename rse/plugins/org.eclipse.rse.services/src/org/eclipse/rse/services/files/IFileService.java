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

package org.eclipse.rse.services.files;

import java.io.File;
import java.io.InputStream;

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
	 * @return true if the file was uploaded
	 */
	public boolean upload(IProgressMonitor monitor, InputStream stream, String remoteParent, String remoteFile, boolean isBinary, String hostEncoding) throws SystemMessageException;
	
	/**
	 * Copy a file to the remote file system.  The remote target is denoted by a
	 * string representing the parent and a string representing the file.
	 * @param localFile - a real file in the local file system.
	 * @param remoteParent - a string designating the parent folder of the target for this file.
	 * @param remoteFile - a string designating the name of the file to be written on the remote system.
	 * @param isBinary - indicates whether the file is text or binary
	 * @param srcEncoding - the src encoding of the file (if text)
	 * @param hostEncoding - the tgt encoding of the file (if text)
	 * @return true if the file was uploaded
	 */
	public boolean upload(IProgressMonitor monitor, File localFile, String remoteParent, String remoteFile, boolean isBinary, String srcEncoding, String hostEncoding) throws SystemMessageException;

	/**
	 * Copy a file from the remote file system to the local system.
	 * @param remoteParent - a String designating the remote parent.
	 * @param remoteFile - a String designating the remote file residing in the parent.
	 * @param localFile - The file that is to be written.  If the file exists it is 
	 * overwritten.
	 * @param isBinary - indicates whether the file is text on binary
	 * @param hostEncoding - the encoding on the host (if text)
	 * @return true if the file was copied from the remote system.
	 * @throws SystemMessageException 
	 */
	public boolean download(IProgressMonitor monitor, String remoteParent, String remoteFile, File localFile, boolean isBinary, String hostEncoding) throws SystemMessageException;

	/**
	 * Return the host file given the parent path and file name
	 * @param remoteParent
	 * @param name
	 * @return
	 */
	public IHostFile getFile(IProgressMonitor monitor, String remoteParent, String name) throws SystemMessageException;
	
	/**
	 * @param remoteParent - the name of the parent directory on the remote file 
	 * system from which to retrieve the child list.
	 * @param fileFilter - a string that can be used to filter the children.  Only
	 * those files matching the filter make it into the list.  The interface 
	 * does not dictate where the filtering occurs.
	 * @return the list of host files. 
	 */
	public IHostFile[] getFilesAndFolders(IProgressMonitor monitor, String remoteParent, String fileFilter) throws SystemMessageException;
  
	/**
	 * @param remoteParent - the name of the parent directory on the remote file 
	 * system from which to retrieve the child list.
	 * @param fileFilter - a string that can be used to filter the children.  Only
	 * those files matching the filter make it into the list.  The interface 
	 * does not dictate where the filtering occurs.
	 * @return the list of host files. 
	 */
	public IHostFile[] getFiles(IProgressMonitor monitor, String remoteParent, String fileFilter) throws SystemMessageException;
  
	/**
	 * @param remoteParent - the name of the parent directory on the remote file 
	 * system from which to retrieve the child list.
	 * @param fileFilter - a string that can be used to filter the children.  Only
	 * those files matching the filter make it into the list.  The interface 
	 * does not dictate where the filtering occurs.
	 * @return the list of host files. 
	 */
	public IHostFile[] getFolders(IProgressMonitor monitor, String remoteParent, String fileFilter) throws SystemMessageException;
  
	/**
	 * Return the list of roots for this system
	 * @return the list of host files. 
	 */
	public IHostFile[] getRoots(IProgressMonitor monitor) throws SystemMessageException;
	
	/**
	 * @return the String containing the name of the user's home directory on this
	 * connection that would be contained in implementations of this service.
	 */
	public IHostFile getUserHome();

	/**
	 * Create a file on the host
	 * @param monitor the progress monitor
	 * @param remoteParent the parent directory
	 * @param fileName the name of the new file
	 * @return the newly created file
	 */
	public IHostFile createFile(IProgressMonitor monitor, String remoteParent, String fileName) throws SystemMessageException;
	
	/**
	 * Create a folder on the host
	 * @param monitor the progress monitor
	 * @param remoteParent the parent directory
	 * @param folderName the name of the new folder
	 * @return the newly created folder
	 */
	public IHostFile createFolder(IProgressMonitor monitor, String remoteParent, String folderName) throws SystemMessageException;
	
	/**
	 * Deletes a file or folder on the host
	 * @param monitor the progress monitor
	 * @param remoteParent the folder containing the file to delete
	 * @param fileName the name of the file or folder to delete
	 * @return true if successful
	 */
	public boolean delete(IProgressMonitor monitor, String remoteParent, String fileName) throws SystemMessageException;

	/**
	 * Deletes a set of files or folders on the host
	 * @param monitor the progress monitor
	 * @param remoteParents the array of folders containing the files to delete
	 * @param fileNames the names of the files or folders to delete
	 * @return true iff all deletes are successful
	 */
	public boolean deleteBatch(IProgressMonitor monitor, String[] remoteParents, String[] fileNames) throws SystemMessageException;

	/**
	 * Renames a file or folder on the host
	 * @param monitor the progress monitor
	 * @param remoteParent the folder containing the file to rename
	 * @param oldName the old name of the file or folder to rename
	 * @param newName the new name for the file
	 * @return true if successful
	 */
	public boolean rename(IProgressMonitor monitor, String remoteParent, String oldName, String newName) throws SystemMessageException;
	
	/**
	 * Renames a file or folder on the host
	 * @param monitor the progress monitor
	 * @param remoteParent the folder containing the file to rename
	 * @param oldName the old name of the file or folder to rename
	 * @param newName the new name for the file
	 * @param oldFile the file to update with the change
	 * @return true if successful
	 */
	public boolean rename(IProgressMonitor monitor, String remoteParent, String oldName, String newName, IHostFile oldFile) throws SystemMessageException;
	
	/**
	 * Move the file or folder specified
	 * @param monitor the progress monitor
	 * @param srcParent the folder containing the file or folder to move
	 * @param srcName the new of the file or folder to move
	 * @param tgtParent the destination folder for the move
	 * @param tgtName the name of the moved file or folder
	 * @return
	 */
	public boolean move(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName) throws SystemMessageException;

	/**
	 * Copy the file or folder to the specified destination
	 * @param monitor the progress monitor
	 * @param srcParent the folder containing the file or folder to copy
	 * @param srcName the new of the file or folder to copy
	 * @param tgtParent the destination folder for the copy
	 * @param tgtName the name of the copied file or folder
	 * @return
	 */
	public boolean copy(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName) throws SystemMessageException;

	/**
	 * Copy a set of files or folders to the specified destination
	 * @param monitor the progress monitor
	 * @param srcParents the folders containing each file or folder to copy
	 * @param srcNames the names of the files or folders to copy
	 * @param tgtParent the destination folder for the copy
	 * @return
	 */
	public boolean copyBatch(IProgressMonitor monitor, String[] srcParents, String[] srcNames, String tgtParent) throws SystemMessageException;

	/**
	 * Indicates whether the file system is case sensitive
	 * @return
	 */
	public boolean isCaseSensitive();
}