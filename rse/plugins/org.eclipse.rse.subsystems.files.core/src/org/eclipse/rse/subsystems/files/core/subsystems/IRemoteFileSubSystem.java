/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [183824] Forward SystemMessageException from IRemoteFileSubsystem
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * David McKnight   (IBM)        - [162195] new APIs for upload multi and download multi
 * David McKnight   (IBM)        - [209552] API changes to use multiple and getting rid of deprecated
 * Kevin Doyle		(IBM)		 - [208778] new API getOutputSteam for getting an output stream in append mode
 * David McKnight   (IBM)        - [209704] added supportsEncodingConversion()
 * Martin Oberhuber (Wind River) - [226574][api] Add ISubSystemConfiguration#supportsEncoding()
 * David Dykstal (IBM) [230821] fix IRemoteFileSubSystem API to be consistent with IFileService
 * Martin Oberhuber (Wind River) - [234026] Clarify IFileService#createFolder() Javadocs
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.core.subsystems;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.RemoteFileException;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.subsystems.files.core.ILanguageUtilityFactory;


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
public interface IRemoteFileSubSystem extends ISubSystem {

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
	public IRemoteFile[] listRoots(IProgressMonitor monitor) throws InterruptedException, SystemMessageException;


	/**
	 * Return a list of all remote folders and/or files in the given folders.  This list is not filtered.
	 *
	 * @param parents The parent folders to list folders and/or files in
	 * @param fileTypes - indicates whether to query files, folders, both or some other type.  There
	 *              should be exactly one fileType specified per parent.
	 * 				For the default list of available file types see <code>IFileServiceContants</code>
	 * @param monitor the progress monitor
	 * @since 3.0
	 */
	public IRemoteFile[] listMultiple(IRemoteFile[] parents, int[] fileTypes, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Return a list of remote folders and/or files in the given folder. Only file names are filtered
	 * by the given file name filters. It can be null for no sub-setting.
	 *
	 * @param parents The parent folders to list folders and files in
	 * @param fileNameFilters The name patterns to subset the file list by, or null to return all files.
	 *             There should be exactly one fileNameFilter per parent.
	 * @param fileTypes - indicates whether to query files, folders, both or some other type.  There
	 *              should be exactly one fileType specified per parent.
	 * 				For the default list of available file types see <code>IFileServiceContants</code>
	 * @param monitor the progress monitor
	 * @since 3.0
	 */
	public IRemoteFile[] listMultiple(IRemoteFile[] parents, String[] fileNameFilters, int[] fileTypes,  IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Return a list of all remote folders and/or files in the given folders.  This list is not filtered.
	 *
	 * @param parents The parent folders to list folders and/or files in
	 * @param fileType - indicates whether to query files, folders, both or some other type. This fileType is used for each parent query.
	 * 				For the default list of available file types see <code>IFileServiceContants</code>
	 * @param monitor the progress monitor
	 * @since 3.0
	 */
	public IRemoteFile[] listMultiple(IRemoteFile[] parents, int fileType, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Return a list of remote folders and/or files in the given folder. Only file names are filtered
	 * by the given file name filters. It can be null for no sub-setting.
	 *
	 * @param parents The parent folders to list folders and files in
	 * @param fileNameFilters The name patterns to subset the file list by, or null to return all files.
	 *             There should be exactly one fileNameFilter per parent.
	 * @param fileType - indicates whether to query files, folders, both or some other type.
	 * 		Available file types include {@link IFileService#FILE_TYPE_FILES},
	 *      {@link IFileService#FILE_TYPE_FOLDERS}, and
	 *      {@link IFileService#FILE_TYPE_FILES_AND_FOLDERS}.
	 * @param monitor the progress monitor
	 * @since 3.0
	 */
	public IRemoteFile[] listMultiple(IRemoteFile[] parents, String[] fileNameFilters, int fileType,  IProgressMonitor monitor) throws SystemMessageException;


	/**
	 * Return a list of all remote folders and/or files in the given folder. The list is not filtered.
	 *
	 * @param parent The parent folder to list folders and/or files in
	 * @param monitor the progress monitor
	 * @since 3.0
	 */
	public IRemoteFile[] list(IRemoteFile parent, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Return a list of all remote folders and/or files in the given folder. The list is not filtered.
	 *
	 * @param parent The parent folder to list folders and files in
	 * @param fileType - indicates whether to query files, folders, both or some other type.
	 * 		Available file types include {@link IFileService#FILE_TYPE_FILES},
	 *      {@link IFileService#FILE_TYPE_FOLDERS}, and
	 *      {@link IFileService#FILE_TYPE_FILES_AND_FOLDERS}.
	 * @param monitor the progress monitor
	 * @since 3.0
	 */
	public IRemoteFile[] list(IRemoteFile parent, int fileType, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Return a list of remote folders and/or files in the given folder. Only file names are filtered
	 * by the given file name filter. It can be null for no filtering.
	 *
	 * @param parent The parent folder to list folders and files in
	 * @param fileNameFilter The name pattern to subset the file list by, or null to return all files.
	 * @param fileType - indicates whether to query files, folders, both or some other type.
	 * 					For the default list of available file types see <code>IFileServiceContants</code>
	 * @param monitor the progress monitor
	 * @since 3.0
	 */
	public IRemoteFile[] list(IRemoteFile parent, String fileNameFilter, int fileType,  IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Return a list of remote folders and/or files in the given folder.  The files part of the list is filtered
	 * by the given file name filter.  It can be null for no filtering.
	 *
	 * @param parent The parent folder to list folders and files in
	 * @param fileNameFilter The name pattern to subset the file list by, or null to return all files.
	 * @param context The holder of state information
	 * - indicates whether to query files, folders, both or some other type
	 * @param fileType - indicates whether to query files, folders, both or some other type
	 * 				For the default list of available file types see <code>IFileServiceContants</code>
	 * @param monitor the progress monitor
	 * @since 3.0

	 */
	public IRemoteFile[] list(IRemoteFile parent, String fileNameFilter, IRemoteFileContext context, int fileType, IProgressMonitor monitor) throws SystemMessageException;



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
	 * @param monitor the progress monitor
	 *
	 * @return the remote file
	 */
	public IRemoteFile getParentFolder(IRemoteFile folderOrFile, IProgressMonitor monitor);

	/**
	 * Given a folder or file, return its parent folder name, fully qualified
	 * @param folderOrFile folder or file to return parent of.
	 */
	public String getParentFolderName(IRemoteFile folderOrFile);

	/**
	 * Returns the encoding of the remote system.
	 * @return the encoding of the remote system.
	 */
	public String getRemoteEncoding();

	/**
	 * Returns the encoding of the file with the remote path.
	 * @param remotePath the remote path of the file.
	 * @return the encoding of the remote file.
	 */
	// public String getEncoding(String remotePath);

	/**
	 * Given a set of fully qualified file or folder names, return an ISystemResourceSet object for it.
	 * @param folderOrFileNames Fully qualified folder or file names
	 * @param monitor the progress monitor
	 *
	 * @return the set of resources
	 * @since 3.0
	 */
	public IRemoteFile[] getRemoteFileObjects(String[] folderOrFileNames, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Given a fully qualified file or folder name, return an IRemoteFile
	 * object for it.
	 * <p>
	 * This may be a long-running operation involving remote system access
	 * if the file with the given key is not found in the internal cache.
	 * </p>
	 * @param folderOrFileName Fully qualified folder or file name.
	 * @param monitor the progress monitor
	 * @return the requested IRemoteFile object.
	 * @throws SystemMessageException in case an error occurs contacting the
	 *     remote system while retrieving the requested remote object.
	 */
	public IRemoteFile getRemoteFileObject(String folderOrFileName, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Given a un-qualified file or folder name, and its parent folder object,
	 *  return an IRemoteFile object for the file.
	 * @param parent Folder containing the folder or file
	 * @param folderOrFileName Un-qualified folder or file name
	 * @param monitor the progress monitor
	 *
	 * @return the requested IRemoteFile object
	 */
	public IRemoteFile getRemoteFileObject(IRemoteFile parent, String folderOrFileName, IProgressMonitor monitor) throws SystemMessageException;

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
	 * @see #createFolders(IRemoteFile,IProgressMonitor)
	 *
	 * @param fileToCreate The object representing the file to be created.
	 * @return The same input object returned for convenience. Will throw exception if it fails.
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 */
	public IRemoteFile createFile(IRemoteFile fileToCreate, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Create a new folder, given its IRemoteFile object (these do not have to represent existing folders)
	 * <p>
	 * <ul>
	 *    <li>The parent folder must exist for this to succeed. If the parent 
	 *        folder does not yet exist, implementations are free to create
	 *        missing parents or fail (see also {@link IFileService#createFolder(String, String, IProgressMonitor)}).</li>
	 *    <li>If this folder already exists, this is a no-op.</li>
	 *    <li>If the given object is a file, not a folder, this is a no-op.</li>
	 * </ul>
	 *
	 * @see #createFolders(IRemoteFile, IProgressMonitor)
	 *
	 * @param folderToCreate The object representing the folder to be created.
	 * @param monitor the progress monitor
	 * @return The same input object returned for convenience. Will throw exception if it fails.
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 */
	public IRemoteFile createFolder(IRemoteFile folderToCreate, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Given an IRemoteFile for a folder, this will create that folder and any missing parent folders in its path.
	 * Use getParentFolder to get the parent object of your file or folder in order to call this method.
	 * <p>
	 * <ul>
	 *    <li>If this folder already exists, this is a no-op.
	 *    <li>If the given object is a file, not a folder, this is a no-op.
	 * </ul>
	 *
	 * @see #getParentFolder(IRemoteFile, IProgressMonitor)
	 *
	 * @param folderToCreate The object representing the folder to be created, along with its parents.
	 * @param monitor the progress monitor
	 * @return The same input object returned for convenience. Will throw exception if it fails.
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 */
	public IRemoteFile createFolders(IRemoteFile folderToCreate, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Delete the given remote file or folder.
	 * <ul>
	 *   <li>If the input is a folder, that folder must be empty for this to succeed.
	 * </ul>
	 *
	 * @param folderOrFile represents the object to be deleted.
	 * @param monitor progressMonitor
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 * @since org.eclipse.rse.subsystems.files.core 3.0
	 */
	public void delete(IRemoteFile folderOrFile, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Delete the given batch of remote file or folder.
	 * <ul>
	 *   <li>If any of the inputs are a folder, those folders must be empty for this to succeed.
	 * </ul>
	 * <p>
	 * If an error occurs during the deletion of an item, this operation stops on that item and a {@link SystemMessageException} is thrown.
	 * Items deleted before that item will remain deleted. Items specified after that item will not be deleted.
	 * The item on which the error occurs will not be deleted.
	 * Without an exception thrown in such cases, views may not be refreshed correctly to account for deleted resources.
	 * @param folderOrFiles represents the objects to be deleted.
	 * @param monitor progressMonitor
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 * @since org.eclipse.rse.subsystems.files.core 3.0
	 */
	public void deleteBatch(IRemoteFile[] folderOrFiles, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Rename the given remote file or folder. This renames it in memory and, iff it exists, on disk.
	 * @param folderOrFile represents the object to be renamed.
	 * @param newName new name to give it.
	 * @param monitor the progress monitor
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 * @since org.eclipse.rse.subsystems.files.core 3.0
	 */
	public void rename(IRemoteFile folderOrFile, String newName, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Move a file or folder to a new target parent folder.
	 *
	 * @param sourceFolderOrFile The file or folder to move
	 * @param targetFolder The folder to move to. No guarantee it is on the same system, so be sure to check getSystemConnection()!
	 * @param newName The new name for the moved file or folder
	 * @param monitor progress monitor
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 * @since org.eclipse.rse.subsystems.files.core 3.0
	 */
	public void move(IRemoteFile sourceFolderOrFile, IRemoteFile targetFolder, String newName,IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Set the last modified date for the given file or folder. Like a Unix "touch" operation.
	 * Folder or file must exist on disk for this to succeed.
	 * @param folderOrFile represents the object to be renamed.
	 * @param newDate new date, in milliseconds from epoch, to assign.
	 * @param monitor the progress monitor
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 * @since org.eclipse.rse.subsystems.files.core 3.0
	 */
	public void setLastModified(IRemoteFile folderOrFile, long newDate, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Set a files read-only permissions.
	 * Folder or file must exist on disk for this to succeed.
	 * @param folderOrFile represents the object to be renamed.
	 * @param readOnly whether to set it to be read-only or not
	 * @param monitor the progress monitor
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 * @since org.eclipse.rse.subsystems.files.core 3.0
	 */
	public void setReadOnly(IRemoteFile folderOrFile, boolean readOnly, IProgressMonitor monitor) throws SystemMessageException;


	// ----------------------------
	// METHODS FOR FILE TRANSFER...
	// ----------------------------

	// Beginning of methods for downloading remote files from the server

	/**
	 * Copy a file or folder to a new target parent folder.
	 * @param sourceFolderOrFile The file or folder to copy
	 * @param targetFolder The folder to copy to. No guarantee it is on the same system, so be sure to check getSystemConnection()!
	 * @param newName The new name for the copied file or folder
	 * @param monitor progress monitor
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 * @since org.eclipse.rse.subsystems.files.core 3.0
	 */
	public void copy(IRemoteFile sourceFolderOrFile, IRemoteFile targetFolder, String newName, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Copy a set of remote files or folders to a new target parent folder. Precondition: Sources and target must all be on the same system.
	 * <p>
	 * If an error occurs during the copy of an item, this operation stops on that item and a {@link SystemMessageException} is thrown.
	 * Items copied before that item will remain copied. Items copied after that item will not be copied.
	 * The item on which the error occurs will not be copied.
	 * @param sourceFolderOrFile The file or folder to copy
	 * @param targetFolder The folder to copy to.
	 * @param monitor progress monitor
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 * @since org.eclipse.rse.subsystems.files.core 3.0
	 */
	public void copyBatch(IRemoteFile[] sourceFolderOrFile, IRemoteFile targetFolder, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Get the remote file and save it locally.
	 *
	 * The file is saved in the encoding specified, with two exceptions:
	 * <ul>
	 *   <li>If the remote file is binary, encoding does not apply.</li>
	 *   <li>If the remote file is a XML file, then it will be
	 *       copied to local in the encoding specified in the XML
	 *       declaration, or as determined from the XML specification.</li>
	 * </ul>
	 * @param source remote file that represents the file to be obtained
	 * @param destination the absolute path of the local file
	 * @param encoding the encoding of the local file
	 * @param monitor the progress monitor
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 */
	public void download(IRemoteFile source, String destination, String encoding, IProgressMonitor monitor) throws SystemMessageException;


	/**
	 * Get the remote files and save them locally.
	 *
	 * The files are saved in the encodings specified, with two exceptions:
	 * <ul>
	 *   <li>If a remote file is binary, encoding does not apply.</li>
	 *   <li>If a remote file is a XML file, then it will be
	 *       copied to local in the encoding specified in the XML
	 *       declaration, or as determined from the XML specification.</li>
	 * </ul>
	 * @param sources remote files that represent the files to be obtained
	 * @param destinations the absolute paths of the local files
	 * @param encodings the encodings of the local files
	 * @param monitor the progress monitor
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 * @since 3.0
	 */
	public void downloadMultiple(IRemoteFile[] sources, String[] destinations, String[] encodings, IProgressMonitor monitor) throws SystemMessageException;




	/**
	 * Put the local copy of the remote file back to the remote location.
	 *
	 * The file is assumed to be in the encoding specified, with
	 * two exceptions:
	 * <ul>
	 *   <li>If the local file is binary, encoding does not apply.</li>
	 *   <li>If the local file is a XML file, then it will be copied
	 *        to remote in the encoding specified in the XML declaration,
	 *        or as determined from the XML specification.</li>
	 * </ul>
	 * @param source the absolute path of the local copy
	 * @param destination remote file that represents the file on the server
	 * @param encoding the encoding of the local copy
	 * @param monitor the progress monitor
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 */
	public void upload(String source, IRemoteFile destination, String encoding, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Put the local copy of the remote file back to the remote location.
	 *
	 * The file is assumed to be in the encoding of the local operating system,
	 * with two exceptions:
	 * <ul>
	 *   <li>If the local file is binary, encoding does not apply.</li>
	 *   <li>If the local file is a XML file, then it will be copied
	 *       to remote in the encoding specified in the XML declaration,
	 *       or as determined from the XML specification.</li>
	 * </ul>
	 * @param source the absolute path of the local copy
	 * @param srcEncoding the encoding of the local copy
	 * @param remotePath remote file that represents the file on the server
	 * @param rmtEncoding the encoding of the remote file.
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 */
	public void upload(String source, String srcEncoding, String remotePath, String rmtEncoding,  IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Put the local copies of the remote files to the remote locations.
	 *
	 * The files are assumed to be in the encodings specified, with
	 * two exceptions:
	 * <ul>
	 *   <li>If a local files is binary, encoding does not apply.</li>
	 *   <li>If a local files is an XML file, then it will be copied
	 *        to remote in the encoding specified in the XML declaration,
	 *        or as determined from the XML specification.</li>
	 * </ul>
	 * @param sources the absolute paths of the local copies
	 * @param destinations remote files that represent the files on the server
	 * @param encodings the encodings of the local copies
	 * @param monitor the progress monitor
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 * @since 3.0
	 */
	public void uploadMultiple(String[] sources, IRemoteFile[] destinations, String[] encodings, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Put the local copies of the remote files to the remote locations.
	 *
	 * The files are assumed to be in the encodings of the local operating system,
	 * with two exceptions:
	 * <ul>
	 *   <li>If a local file is binary, encoding does not apply.</li>
	 *   <li>If a local file is a XML file, then it will be copied
	 *       to remote in the encoding specified in the XML declaration,
	 *       or as determined from the XML specification.</li>
	 * </ul>
	 * @param sources the absolute paths of the local copies
	 * @param srcEncodings the encodings of the local copies
	 * @param remotePaths remote files that represents the files on the server
	 * @param rmtEncodings the encodings of the remote files.
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 * @since 3.0
	 */
	public void uploadMultiple(String sources[], String[] srcEncodings, String[] remotePaths, String[] rmtEncodings,  IProgressMonitor monitor) throws SystemMessageException;

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
	public IRemoteFile[] listRoots(IRemoteFileContext context, IProgressMonitor monitor) throws InterruptedException;

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

	/**
	 * Gets the input stream to access the contents a remote file. Clients should close the input stream when done. Implementations should not return <code>null</code>.
	 * @param remoteParent the absolute path of the parent.
	 * @param remoteFile the name of the remote file.
	 * @param isBinary <code>true</code> if the file is a binary file, <code>false</code> otherwise.
	 * @return the input stream to access the contents of the remote file.
	 * @param monitor the progress monitor.
	 * @throws SystemMessageException if an error occurs.
	 * @since 2.0
	 */
	public InputStream getInputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Gets the output stream to write to a remote file. Clients should close the output stream when done. Implementations should not return <code>null</code>.
	 * @param remoteParent the absolute path of the parent.
	 * @param remoteFile the name of the remote file.
	 * @param isBinary <code>true</code> if the file is a binary file, <code>false</code> otherwise.
	 * @return the input stream to access the contents of the remote file.
	 * @param monitor the progress monitor.
	 * @throws SystemMessageException if an error occurs.
	 * @since 2.0
	 * @deprecated  Use {@link #getOutputStream(String, String, int, IProgressMonitor)} instead
	 */
	public OutputStream getOutputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Gets the output stream to write/append to a remote file. Clients should close the output stream when done. Implementations should not return <code>null</code>.
	 * @param remoteParent the absolute path of the parent.
	 * @param remoteFile the name of the remote file.
	 * @param options bit wise or of option constants.  Valid constants are {@link IFileService#APPEND}, {@link IFileService#TEXT_MODE}, and {@link IFileService#NONE}
	 * @return the input stream to access the contents of the remote file.
	 * @param monitor the progress monitor.
	 * @throws SystemMessageException if an error occurs.
	 * @since 3.0
	 */
	public OutputStream getOutputStream(String remoteParent, String remoteFile, int options, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Indicates whether this file subsystem supports code page conversion using
	 * the IFileServiceCodePageConverter mechanism.  Certain extensions, such as
	 * property pages for encoding conversion can determine whether or not to
	 * display or enable themselves based on result of this call.
	 *
	 * @return whether this service supports encoding conversion
	 * @since 3.0
	 */
	public boolean supportsEncodingConversion();
}
