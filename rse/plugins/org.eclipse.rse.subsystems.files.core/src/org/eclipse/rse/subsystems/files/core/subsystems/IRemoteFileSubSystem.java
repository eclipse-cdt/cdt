/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.subsystems.files.core.subsystems;
import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.subsystems.files.core.ILanguageUtilityFactory;
import org.eclipse.rse.model.SystemRemoteResourceSet;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;


//
/**
 * Specialized interface for remote file subsystems.
 * <p>
 * These are unix/windows like file systems, versus native OS/400 or OS/390
 * file subsystems. Rather, it is more like the IFS and HFS systems on
 * these operating systems.
 * <p>
 * The idea is to encourage a common API and GUI layer that works
 * with any remote unix-like file system on any remote operating system.
 */
/**
 * @lastgen interface RemoteFileSubSystem extends SubSystem {}
 */

public interface IRemoteFileSubSystem extends ISubSystem{

    // ----------------------
    // HELPER METHODS...
    // ----------------------
    
	/**
	 * Return parent subsystem factory, cast to a RemoteFileSubSystemConfiguration
	 */
	public IRemoteFileSubSystemConfiguration getParentRemoteFileSubSystemConfiguration();		
	/**
	 * Return true if file names are case-sensitive. Used when doing name or type filtering
	 */
	public boolean isCaseSensitive();

	// --------------------------------
	// FILE SYSTEM ATTRIBUTE METHODS...
	// --------------------------------   	
	/**
	 * Return in string format the character used to separate folders. Eg, "\" or "/".
	 * <br>
	 * Shortcut to {@link #getParentRemoteFileSubSystemConfiguration()}.getSeparator()
	 */
	public String getSeparator();

	/**
	 * Return in character format the character used to separate folders. Eg, "\" or "/"
	 * <br>
	 * Shortcut to {@link #getParentRemoteFileSubSystemConfiguration()}.getSeparatorChar()
	 */    
	public char getSeparatorChar();

	/**
	 * Return in string format the character used to separate paths. Eg, ";" or ":"
	 * <br>
	 * Shortcut to {@link #getParentRemoteFileSubSystemConfiguration()}.getPathSeparator()
	 */    
	public String getPathSeparator();

	/**
	 * Return in char format the character used to separate paths. Eg, ";" or ":"
	 * <br>
	 * Shortcut to {@link #getParentRemoteFileSubSystemConfiguration()}.getPathSeparatorChar()
	 */    
	public char getPathSeparatorChar();

	/**
	 * Return as a string the line separator.
	 * <br>
	 * Shortcut to {@link #getParentRemoteFileSubSystemConfiguration()}.getLineSeparator()
	 */
	public String getLineSeparator();
	
    // ----------------------
    // FILE SYSTEM METHODS...
    // ----------------------

	/**
	 * Return a list of roots/drives on the remote system.
	 * This version is called directly by users.
	 */
	public IRemoteFile[] listRoots() throws InterruptedException;
	/**
	 * Return a list of all remote folders in the given parent folder on the remote system
	 * @param parent The parent folder to list folders in
	 */
	public IRemoteFile[] listFolders(IRemoteFile parent);

	/**
	 * Return a full list of remote folders in the given parent folder on the remote system.
	 * @param parent The parent folder to list folders in
	 * @param fileNameFilter The name pattern for subsetting the file list when this folder is subsequently expanded
	 */
	public IRemoteFile[] listFolders(IRemoteFile parent, String fileNameFilter);
	
	/**
	 * Return a list of all remote files in the given parent folder on the remote system
	 * @param parent The parent folder to list files in
	 */
	public IRemoteFile[] listFiles(IRemoteFile parent);
		
	/**
	 * Return a list of remote files in the given folder, which match the given name pattern.
	 * @param parent The parent folder to list files in
	 * @param fileNameFilter The name pattern to subset the list by, or null to return all files.
	 */
	public IRemoteFile[] listFiles(IRemoteFile parent, String fileNameFilter);
	
	/**
	 * Return a list of all remote folders and files in the given folder. The list is not subsetted.
	 * @param parent The parent folder to list folders and files in
	 */
	public IRemoteFile[] listFoldersAndFiles(IRemoteFile parent);		
	
	/**
	 * Return a list of remote folders and files in the given folder. Only file names are subsettable
	 * by the given file name filter. It can be null for no subsetting.
	 * @param parent The parent folder to list folders and files in
	 * @param fileNameFilter The name pattern to subset the file list by, or null to return all files.
	 */
	public IRemoteFile[] listFoldersAndFiles(IRemoteFile parent, String fileNameFilter);

	/**
	 * Return a list of remote folders and files in the given folder. 
	 * <p>
	 * The files part of the list is subsetted by the given file name filter. 
	 * It can be null for no subsetting.
	 * This version is called by RemoteFileSubSystemImpl's resolveFilterString(s).
	 * @param parent The parent folder to list folders and files in
	 * @param fileNameFilter The name pattern to subset the file list by, or null to return all files.
	 * @param context The holder of state information
	 */
	public IRemoteFile[] listFoldersAndFiles(IRemoteFile parent, String fileNameFilter, IRemoteFileContext context);

	/**
	 * Return a subsetted list of remote folders in the given parent folder on the remote system.
	 * This version is called by RemoteFileSubSystemImpl's resolveFilterString(s)
	 * <b>note</b>This method should be abstract but MOF doesn't allow abstract impl classes at this point
	 * @param parent The parent folder to list folders in
	 * @param fileNameFilter The name pattern for subsetting the file list when this folder is subsequently expanded
	 * @param context The holder of state information
	 */
	public IRemoteFile[] listFolders(IRemoteFile parent, String fileNameFilter, IRemoteFileContext context);

	/**
	 * Return a list of remote files in the given folder, which match the given name pattern.
	 * This version is called by RemoteFileSubSystemImpl's resolveFilterString(s)
	 * <b>note</b>This method should be abstract but MOF doesn't allow abstract impl classes at this point
	 * @param parent The parent folder to list files in
	 * @param fileNameFilter The name pattern to subset the list by, or null to return all files.
	 * @param context The holder of state information
	 */
	public IRemoteFile[] listFiles(IRemoteFile parent, String fileNameFilter, IRemoteFileContext context);
	/**
	 * Given a search configuration, searches for its results.
	 * @param searchConfig a search configuration.
	 */
	public void search(IHostSearchResultConfiguration searchConfig);

	/**
	 * Given a search configuration, cancel the search.
  	 * @param searchConfig a search configuration.
	 */
	public void cancelSearch(IHostSearchResultConfiguration searchConfig);


	/**
	 * Given a folder or file, return its parent folder object.
	 * @param folderOrFile folder or file to return parent of.
	 */
	public IRemoteFile getParentFolder(IRemoteFile folderOrFile);

	/**
	 * Given a folder or file, return its parent folder name, fully qualified
	 * @param folderOrFile folder or file to return parent of.
	 */
	public String getParentFolderName(IRemoteFile folderOrFile);	

	/**
	 * Get the default encoding of the target system
	 * @return the encoding
	 */
	public String getRemoteEncoding();
	
	/**
	 * Given a set of fully qualified file or folder names, return an ISystemResourceSet object for it.
	 * @param folderOrFileNames Fully qualified folder or file names
	 */
	public SystemRemoteResourceSet getRemoteFileObjects(List folderOrFileNames) throws SystemMessageException;
	
	/**
	 * Given a fully qualified file or folder name, return an IRemoteFile object for it.
	 * @param folderOrFileName Fully qualified folder or file name
	 */
	public IRemoteFile getRemoteFileObject(String folderOrFileName) throws SystemMessageException;

	/**
	 * Given a un-qualified file or folder name, and its parent folder object, 
	 *  return an IRemoteFile object for the file.
	 * @param parent Folder containing the folder or file
	 * @param folderOrFileName Un-qualified folder or file name
	 */
	public IRemoteFile getRemoteFileObject(IRemoteFile parent, String folderOrFileName) throws SystemMessageException;
	
	/**
	 * Given a key, returns a search result object for it. For the key, see <
	 * @param key the key that uniquely identifies a search result.
	 */
	public IRemoteSearchResult getRemoteSearchResultObject(String key) throws SystemMessageException;
	
	/**
	 * Create a new file, given its IRemoteFile object (these do not have to represent existing files).
	 * <p>
	 * <ul>
	 *    <li>The parent folder must exist for this to succeed.
	 *    <li>If this file already exists, this is a no-op.
	 *    <li>If the given object is a folder, not a file, this is a no-op.
	 * </ul>
	 * 
	 * @see #createFolders(IRemoteFile)
	 * 
	 * @param fileToCreate The object representing the file to be created.
	 * @return The same input object returned for convenience. Will throw exception if it fails.
	 */
	public IRemoteFile createFile(IRemoteFile fileToCreate) throws RemoteFileSecurityException, RemoteFileIOException;

	/**
	 * Create a new folder, given its IRemoteFile object (these do not have to represent existing folders)
	 * <p>
	 * <ul>
	 *    <li>The parent folder must exist for this to succeed.
	 *    <li>If this folder already exists, this is a no-op.
	 *    <li>If the given object is a file, not a folder, this is a no-op.
	 * </ul>
	 * 
	 * @see #createFolders(IRemoteFile)
	 * 
	 * @param folderToCreate The object representing the folder to be created.
	 * @return The same input object returned for convenience. Will throw exception if it fails.
	 */
	public IRemoteFile createFolder(IRemoteFile folderToCreate) throws RemoteFileSecurityException, RemoteFileIOException;	
	
	/**
	 * Given an IRemoteFile for a folder, this will create that folder and any missing parent folders in its path.
	 * Use getParentFolder to get the parent object of your file or folder in order to call this method.
	 * <p>
	 * <ul>
	 *    <li>If this folder already exists, this is a no-op.
	 *    <li>If the given object is a file, not a folder, this is a no-op.
	 * </ul>
	 * 
	 * @see #getParentFolder(IRemoteFile)
	 * 
	 * @param folderToCreate The object representing the folder to be created, along with its parents.
	 * @return The same input object returned for convenience. Will throw exception if it fails.
	 */
	public IRemoteFile createFolders(IRemoteFile folderToCreate) throws RemoteFileSecurityException, RemoteFileIOException;		
	
	/**
	 * Delete the given remote file or folder. 
	 * <ul>
	 *   <li>If the input is a folder, that folder must be empty for this to succeed.
	 * </ul>
	 * 
	 * @param folderOrFile represents the object to be deleted.
	 * @return false if the given folder/file didn't exist to begin with, else true. Throws an exception if anything fails.
	 * 
	 * @deprecated use the delete that takes a monitor now
	 */
	public boolean delete(IRemoteFile folderOrFile) throws RemoteFolderNotEmptyException, RemoteFileSecurityException, RemoteFileIOException;
	
	
	/**
	 * Delete the given remote file or folder. 
	 * <ul>
	 *   <li>If the input is a folder, that folder must be empty for this to succeed.
	 * </ul>
	 * 
	 * @param folderOrFile represents the object to be deleted.
	 * @param monitor progressMonitor
	 * @return false if the given folder/file didn't exist to begin with, else true. Throws an exception if anything fails.
	 */
	public boolean delete(IRemoteFile folderOrFile, IProgressMonitor monitor) throws RemoteFolderNotEmptyException, RemoteFileSecurityException, RemoteFileIOException;

	/**
	 * Delete the given batch of remote file or folder. 
	 * <ul>
	 *   <li>If any of the inputs are a folder, those folders must be empty for this to succeed.
	 * </ul>
	 * 
	 * @param folderOrFiles represents the objects to be deleted.
	 * @param monitor progressMonitor
	 * @return false if any of the given folder/file dont exist to begin with, else true. Throws an exception if anything fails.
	 */
	public boolean deleteBatch(IRemoteFile[] folderOrFiles, IProgressMonitor monitor) throws RemoteFolderNotEmptyException, RemoteFileSecurityException, RemoteFileIOException;

	
	/**
	 * Rename the given remote file or folder. This renames it in memory and, iff it exists, on disk.
	 * @param folderOrFile represents the object to be renamed.
	 * @param newName new name to give it.
	 * @return false if the given folder/file didn't exist on disk (still renamed in memory), else true. Throws an exception if anything fails.
	 */
	public boolean rename(IRemoteFile folderOrFile, String newName) throws RemoteFileSecurityException, RemoteFileIOException;	

	/**
	 * Move a file or folder to a new target parent folder.
	 * 
	 * @param sourceFolderOrFile The file or folder to move
	 * @param targetFolder The folder to move to. No guarantee it is on the same system, so be sure to check getSystemConnection()!
	 * @param newName The new name for the moved file or folder
	 * @return false true iff the move succeeded
	 */
	public boolean move(IRemoteFile sourceFolderOrFile, IRemoteFile targetFolder, String newName) throws RemoteFileSecurityException, RemoteFileIOException;
	
	
	/**
	 * Move a file or folder to a new target parent folder.
	 * 
	 * @param sourceFolderOrFile The file or folder to move
	 * @param targetFolder The folder to move to. No guarantee it is on the same system, so be sure to check getSystemConnection()!
	 * @param newName The new name for the moved file or folder
	 * @param monitor progress monitor
	 * @return false true iff the move succeeded
	 */
	public boolean move(IRemoteFile sourceFolderOrFile, IRemoteFile targetFolder, String newName,IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;
	
	/**
	 * Set the last modified date for the given file or folder. Like a Unix "touch" operation.
	 * Folder or file must exist on disk for this to succeed.
	 * 
	 * @param folderOrFile represents the object to be renamed.
	 * @param newDate new date, in milliseconds from epoch, to assign.
	 * @return false if the given folder/file didn't exist on disk (operation fails), else true. Throws an exception if anything fails.
	 */
	public boolean setLastModified(IRemoteFile folderOrFile, long newDate) throws RemoteFileSecurityException, RemoteFileIOException;

	/**
	 * Set a file to readonly.
	 * Folder or file must exist on disk for this to succeed.
	 * 
	 * @param folderOrFile represents the object to be renamed.
	 * @return false if the given folder/file didn't exist on disk (operation fails), else true. Throws an exception if anything fails.
	 */
	public boolean setReadOnly(IRemoteFile folderOrFile) throws RemoteFileSecurityException, RemoteFileIOException;

	
	// ----------------------------
	// METHODS FOR FILE TRANSFER...
	// ----------------------------
	
	// Beginning of methods for downloading remote files from the server
	
	/**
		 * Copy a file or folder to a new target parent folder.
		 * 
		 * @param sourceFolderOrFile The file or folder to copy
		 * @param targetFolder The folder to copy to. No guarantee it is on the same system, so be sure to check getSystemConnection()!
		 * @param newName The new name for the copied file or folder
		 * @return false true iff the copy succeeded
		 */
	public boolean copy(IRemoteFile sourceFolderOrFile, IRemoteFile targetFolder, String newName, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;

	/**
	 * Copy a set of remote files or folders to a new target parent folder. Precondition: Sources and target must all be on the same system!
	 * 
	 * @param sourceFolderOrFiles The file or folder to copy
	 * @param targetFolder The folder to copy to. 
	 * @return false true iff all copies succeeded
	 */
	public boolean copyBatch(IRemoteFile[] sourceFolderOrFile, IRemoteFile targetFolder, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;


	/**
	 * Get the remote file and save it locally. The file is saved in the encoding
	 * of the operating system. Two exceptions: if the remote file is binary, encoding does not apply.
	 * If the remote file is a XML file, then it will be copied to local in the encoding
	 * specified in the XML declaration, or as determined from the XML specification. 
	 * @param source remote file that represents the file to be obtained
	 * @param destination the absolute path of the local file
	 * @param monitor the progress monitor
	 */
	public void download(IRemoteFile source, String destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;
	
	
	/**
	 * Get the remote file and save it locally. The file is saved in the encoding
	 * specified. Two exceptions: if the remote file is binary, encoding does not apply.
	 * If the remote file is a XML file, then it will be copied to local in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * @param source remote file that represents the file to be obtained
	 * @param destination the absolute path of the local file
	 * @param encoding the encoding of the local file
	 * @param monitor the progress monitor
	 */
	public void download(IRemoteFile source, String destination, String encoding,  IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;


	/**
	 * Get the remote file and save it locally. The file is saved in UTF-8 encoding.
	 * Two exceptions: if the remote file is binary, encoding does not apply.
	 * If the remote file is a XML file, then it will be copied to local in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * This is a recommended method to use for file transfer.
	 * @param source remote file that represents the file to be obtained
	 * @param destination the absolute path of the local file
	 * @param monitor the progress monitor
	 */
	public void downloadUTF8(IRemoteFile source, String destination,  IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;
	
	
	/**
	 * Get the remote file and save it locally. The file is saved in the encoding
	 * of the operating system. Two exceptions: if the remote file is binary, encoding does not apply.
	 * If the remote file is a XML file, then it will be copied to local in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * @param source remote file that represents the file to be obtained
	 * @param destination the local file
	 * @param monitor the progress monitor
	 */
	public void download(IRemoteFile source, File destination,  IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;
	
	
	/**
	 * Get the remote file and save it locally. The file is saved in the encoding
	 * specified. Two exceptions: if the remote file is binary, encoding does not apply.
	 * If the remote file is a XML file, then it will be copied to local in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * @param source remote file that represents the file to be obtained
	 * @param destination the local file
	 * @param encoding the encoding of the local file
	 * @param monitor the progress monitor
	 */
	public void download(IRemoteFile source, File destination, String encoding, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;


	/**
	 * Get the remote file and save it locally. The file is saved in UTF-8 encoding.
	 * Two exceptions: if the remote file is binary, encoding does not apply.
	 * If the remote file is a XML file, then it will be copied to local in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * This is a recommended method to use for file transfer.
	 * @param source remote file that represents the file to be obtained
	 * @param destination the local file
	 * @param monitor the progress monitor
	 */
	public void downloadUTF8(IRemoteFile source, File destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;
	
	
	/**
	 * Get the remote file and save it locally. The file is saved in the encoding
	 * of the operating system. Two exceptions: if the remote file is binary, encoding does not apply.
	 * If the remote file is a XML file, then it will be copied to local in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * @param source remote file that represents the file to be obtained
	 * @param destination the local file
	 * @param monitor the progress monitor
	 */
	public void download(IRemoteFile source, IFile destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;
	
	
	/**
	 * Get the remote file and save it locally. The file is saved in the encoding
	 * specified. Two exceptions: if the remote file is binary, encoding does not apply.
	 * If the remote file is a XML file, then it will be copied to local in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * @param source remote file that represents the file to be obtained
	 * @param destination the local file
	 * @param encoding the encoding of the local file
	 * @param monitor the progress monitor
	 */
	public void download(IRemoteFile source, IFile destination, String encoding,  IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;


	/**
	 * Get the remote file and save it locally. The file is saved in UTF-8 encoding.
	 * Two exceptions: if the remote file is binary, encoding does not apply.
	 * If the remote file is a XML file, then it will be copied to local in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * This is a recommended method to use for file transfer.
	 * @param source remote file that represents the file to be obtained
	 * @param destination the local file
	 * @param monitor the progress monitor
	 */
	public void downloadUTF8(IRemoteFile source, IFile destination,  IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;
	
	// End of methods to download remote files from the server
	
	
	// Beginning of methods to upload local files to the server
	
	/**
	 * Put the local copy of the remote file back to the remote location. The file
	 * is assumed to be in the encoding of the local operating system.
	 * Two exceptions: if the local file is binary, encoding does not apply.
	 * If the local file is a XML file, then it will be copied to remote in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * @param source the absolute path of the local copy
	 * @param destination remote file that represents the file on the server
	 * @param monitor the progress monitor
	 */
	public void upload(String source, IRemoteFile destination,  IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;
	
	
	/**
	 * Put the local copy of the remote file back to the remote location. The file
	 * is assumed to be in the encoding specified.
	 * Two exceptions: if the local file is binary, encoding does not apply.
	 * If the local file is a XML file, then it will be copied to remote in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * @param source the absolute path of the local copy
	 * @param destination remote file that represents the file on the server
	 * @param encoding the encoding of the local copy
	 * @param monitor the progress monitor
	 */
	public void upload(String source, IRemoteFile destination, String encoding, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;
	
	/**
	 * Put local data to a remote location. The local data is assumed to be in the encoding specified.
	 * @param stream the input stream containing the local data.
	 * @param totalBytes the total number of bytes in the stream, or -1 if unknown. If -1 is specified, then the progress monitor must be <code>null</code>.
	 * @param destination remote file that represents the file on the server. 
	 * @param encoding the encoding of the local data, or <code>null</code> to specify binary.
	 * @param monitor the progress monitor.
	 * @throws RemoteFileSecurityException
	 * @throws RemoteFileIOException
	 */
	public void upload(InputStream stream, long totalBytes, IRemoteFile destination, String encoding, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException; 

	/**
	 * Put the local copy of the remote file back to the remote location. The file
	 * is assumed to be in the encoding of the local operating system.
	 * Two exceptions: if the local file is binary, encoding does not apply.
	 * If the local file is a XML file, then it will be copied to remote in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * @param source the absolute path of the local copy
	 * @param destination remote file that represents the file on the server
	 * @param monitor the progress monitor
	 */
	public void upload(String source, String destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;

	/**
	 * Put the local copy of the remote file back to the remote location. The file
	 * is assumed to be in the encoding of the local operating system.
	 * Two exceptions: if the local file is binary, encoding does not apply.
	 * If the local file is a XML file, then it will be copied to remote in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * @param source the absolute path of the local copy
	 * @param srcEncoding the encoding of the local copy
	 * @param remotePath remote file that represents the file on the server
	 * @param rmtEncoding the encoding of the remote file.
	 */
	public void upload(String source, String srcEncoding, String remotePath, String rmtEncoding,  IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;
	
	/**
	 * Put the local copy of the remote file back to the remote location. The local file
	 * must be in UTF-8 encoding.
	 * Two exceptions: if the local file is binary, encoding does not apply.
	 * If the local file is a XML file, then it will be copied to remote in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * @param source the absolute path of the local copy
	 * @param destination remote file that represents the file on the server
	 * @param monitor the progress monitor
	 */
	public void uploadUTF8(String source, IRemoteFile destination,  IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;
	
	
	/**
	 * Put the local copy of the remote file back to the remote location. The file
	 * is assumed to be in the encoding of the local operating system.
	 * Two exceptions: if the local file is binary, encoding does not apply.
	 * If the local file is a XML file, then it will be copied to remote in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * @param source the local copy
	 * @param destination remote file that represents the file on the server
	 * @param monitor the progress monitor
	 */
	public void upload(File source, IRemoteFile destination,  IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;
	
	
	/**
	 * Put the local copy of the remote file back to the remote location. The file
	 * is assumed to be in the encoding specified.
	 * Two exceptions: if the local file is binary, encoding does not apply.
	 * If the local file is a XML file, then it will be copied to remote in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * @param source the local copy
	 * @param destination remote file that represents the file on the server
	 * @param encoding the encoding of the local copy
	 * @param monitor the progress monitor
	 */
	public void upload(File source, IRemoteFile destination, String encoding,  IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;
	
	
	/**
	 * Put the local copy of the remote file back to the remote location. The local file
	 * must be in UTF-8 encoding.
	 * Two exceptions: if the local file is binary, encoding does not apply.
	 * If the local file is a XML file, then it will be copied to remote in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * @param source the local copy
	 * @param destination remote file that represents the file on the server
	 * @param monitor the progress monitor
	 */
	public void uploadUTF8(File source, IRemoteFile destination, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;
	
	
	/**
	 * Put the local copy of the remote file back to the remote location. The file
	 * is assumed to be in the encoding of the local operating system.
	 * Two exceptions: if the local file is binary, encoding does not apply.
	 * If the local file is a XML file, then it will be copied to remote in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * @param source the local copy
	 * @param destination remote file that represents the file on the server
	 * @param monitor the progress monitor
	 */
	public void upload(IFile source, IRemoteFile destination,  IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;
	
	
	/**
	 * Put the local copy of the remote file back to the remote location. The file
	 * is assumed to be in the encoding specified
	 * @param source the local copy
	 * @param destination remote file that represents the file on the server
	 * @param encoding the encoding of the local copy
	 * @param monitor the progress monitor
	 */
	public void upload(IFile source, IRemoteFile destination, String encoding,  IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;
	
	
	/**
	 * Put the local copy of the remote file back to the remote location. The local file
	 * must be in UTF-8 encoding.
	 * Two exceptions: if the local file is binary, encoding does not apply.
	 * If the local file is a XML file, then it will be copied to remote in the encoding
	 * specified in the XML declaration, or as determined from the XML specification.
	 * @param source the local copy
	 * @param destination remote file that represents the file on the server
	 * @param monitor the progress monitor
	 */
	public void uploadUTF8(IFile source, IRemoteFile destination,  IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException;
	
	
	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the HomeFolder attribute
	 */
	String getHomeFolder();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the HomeFolder attribute
	 */
	void setHomeFolder(String value);
	
	/**
	 * Returns a language utility factory associated with this subsystem.
	 * @return the language utility factory associated with this subsystem.
	 */
	public ILanguageUtilityFactory getLanguageUtilityFactory();
	
	/**
	 * Returns an unused port number on the remote host that could be used
	 * by any tool on the host.
	 * @return an unused port number on the host, or -1 if none could be found.
	 */
	public int getUnusedPort();
	
	
	/**
	 * Return a list of roots/drives on the remote system.
	 * This version is called by RemoteFileSubSystemImpl's resolveFilterString(s)
	 * <b>note</b>This method should be abstract but MOF doesn't allow abstract impl classes at this point
	 */
	public IRemoteFile[] listRoots(IRemoteFileContext context) throws InterruptedException;
	
	/**
	 * Returns the TCP/IP address for the local system that is accessible from 
	 * the remote server. If the local system has multiple IP addresses (because of multiple
	 * network cards or VPN), then this will return the address that the remote system
	 * can use to "call back" to the PC. There must be a network connection between the local
	 * system and the remote system for this method to work. If no network connection exists,
	 * then this method returns <code>null</code>.
	 * 
	 * @return the local TCP/IP address accessible from the remote system, or <code>null</code> if
	 *          no address can be resolved.
	 */
	public InetAddress getLocalAddress();
}