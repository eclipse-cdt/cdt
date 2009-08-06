/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [183824] Forward SystemMessageException from IRemoteFileSubsystem
 * Martin Oberhuber (Wind River) - [199548] Avoid touching files on setReadOnly() if unnecessary
 * Martin Oberhuber (Wind River) - [204710] Update Javadoc to mention that getUserHome() may return null
 * David McKnight (IBM) - [207178] changing list APIs for file service and subsystems
 * David McKnight (IBM) - [162195] new APIs for upload multi and download multi
 * David McKnight (IBM) - [209552] API changes to use multiple and getting rid of deprecated
 * David McKnight (IBM) - [210109] store constants in IFileService rather than IFileServiceConstants
 * Kevin Doyle (IBM) - [208778] new API getOutputSteam for getting an output stream in append mode
 * David McKnight (IBM) - [209704] added supportsEncodingConversion()
 * Martin Oberhuber (Wind River) - [cleanup] Fix API since tags
 * David Dykstal (IBM) - [221211] clarifying javadoc on batch operations
 * David Dykstal (IBM) - [221211] fix IFileService API for batch operations
 * Radoslav Gerganov (ProSyst) - [230919] IFileService.delete() should not return a boolean
 * Martin Oberhuber (Wind River) - [234026] Clarify IFileService#createFolder() Javadocs
 * Martin Oberhuber (Wind River) - [274568] Dont use SftpMonitor for Streams transfer
 *******************************************************************************/

package org.eclipse.rse.services.files;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

/**
 * A IFileService is an abstraction of a file service that runs over some sort
 * of connection. It can be shared among multiple instances of a subsystem. At
 * some point this file service layer may become official API but for now it is
 * experimental. Each subsystem is currently responsible for layering an
 * abstraction over whatever it wants to construct as a service.
 * <p>
 * This is a very bare bones definition. A real definition would probably have
 * changed terminology, use URI's rather than Strings, and have much more robust
 * error handling.
 * </p>
 * <p>
 * Implementers of this interface will have to either be instantiated,
 * initialized, or somehow derive a connection as part of its state.
 * </p>
 * @noimplement This interface is not intended to be implemented by clients.
 *              File service implementations must subclass
 *              {@link AbstractFileService} rather than implementing this
 *              interface directly.
 */
public interface IFileService extends IService
{
	/**
	 * Query constant (bit mask value 1) which indicates that a query should be
	 * on files. The filter(s) passed into the list methods will produce a
	 * subset of files matching the filter(s).
	 *
	 * This constant is passed into the IFileService list calls. Implementors of
	 * IFileService make use of this to determine what to query and what to
	 * return from the query.
	 *
	 * @see IFileService#list(String,String,int,IProgressMonitor)
	 * @see IFileService#listMultiple(String[],String[],int,List,IProgressMonitor)
	 * @see IFileService#listMultiple(String[],String[],int[],List,IProgressMonitor)
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public static final int FILE_TYPE_FILES =  0x1;

	/**
	 * Query constant (bit mask value 2) which indicates that a query should be
	 * on folders. The filter(s) passed into the list methods will produce a
	 * subset of folders matching the filter(s).
	 *
	 * This constant is passed into the IFileService list calls. Implementors of
	 * IFileService make use of this to determine what to query and what to
	 * return from the query.
	 *
	 * @see IFileService#list(String,String,int,IProgressMonitor)
	 * @see IFileService#listMultiple(String[],String[],int,List,IProgressMonitor)
	 * @see IFileService#listMultiple(String[],String[],int[],List,IProgressMonitor)
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public static final int FILE_TYPE_FOLDERS =  0x2;

	/**
	 * Query constant (bit mask value 0) which indicates that a query should
	 * produce folders and files. The filter(s) passed into the list methods
	 * will produce a subset of files matching the filter(s) and all the
	 * folders. Note that only files are filtered and all folders are returned
	 * when this is used.
	 *
	 * This constant is passed into the IFileService list calls. Implementors of
	 * IFileService make use of this to determine what to query and what to
	 * return from the query.
	 *
	 * @see IFileService#list(String,String,int,IProgressMonitor)
	 * @see IFileService#listMultiple(String[],String[],int,List,IProgressMonitor)
	 * @see IFileService#listMultiple(String[],String[],int[],List,IProgressMonitor)
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public static final int FILE_TYPE_FILES_AND_FOLDERS = 0x0;

	/**
	 * Options constant (value 1 &lt;&lt;0) for specifying a stream that will
	 * append data to a file.
	 *
	 * @see IFileService#getOutputStream(String, String, int, IProgressMonitor)
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public static final int APPEND = 1 << 0;

	/**
	 * Options constant (value 2 &lt;&lt;0) for specifying that a file is Text
	 * instead of the default Binary.
	 *
	 * In Text mode, encoding conversions and line end conversions can be
	 * performed on the stream.
	 *
	 * @see IFileService#getOutputStream(String, String, int, IProgressMonitor)
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public static final int TEXT_MODE = 2 << 0;

	/**
	 * Options constant (value 0) to indicate that no bit options are set.
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public static final int NONE = 0;

	/**
	 * Copy a file to the remote file system.  The remote target is denoted by a
	 * string representing the parent and a string representing the file.
	 * @param stream input stream to transfer
	 * @param remoteParent - a string designating the parent folder of the target for this file.
	 * @param remoteFile - a string designating the name of the file to be written on the remote system.
	 * @param isBinary - indicates whether the file is text or binary
	 * @param hostEncoding - the tgt encoding of the file (if text)
	 * @param monitor the monitor for this potentially long running operation
	 * @throws SystemMessageException if an error occurs.
	 * Typically this would be one of those in the RemoteFileException family.
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public void upload(InputStream stream, String remoteParent, String remoteFile, boolean isBinary, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException;

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
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public void upload(File localFile, String remoteParent, String remoteFile, boolean isBinary, String srcEncoding, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException;


	/**
	 * Copy files to the remote file system.  The remote target is denoted by
	 * strings representing the parents and strings representing the files.
	 * <p>
	 * If an error occurs during the upload of a file, this operation stops on that file and a {@link SystemMessageException} is thrown.
	 * Files uploaded before that file will remain uploaded. Files in the list after that file will not be uploaded.
	 * The file on which the error occurs will not be uploaded.
	 * @param localFiles - real files in the local file system.
	 * @param remoteParents - strings designating the parent folders of the target for the files.
	 * @param remoteFiles - strings designating the names of the files to be written on the remote system.
	 * @param isBinary - indicates whether the files are text or binary
	 * @param srcEncodings - the src encodings of the files (if text)
	 * @param hostEncodings - the tgt encodings of the files (if text)
	 * @param monitor the monitor for this potentially long running operation
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public void uploadMultiple(File[] localFiles, String[] remoteParents, String[] remoteFiles, boolean[] isBinary, String[] srcEncodings, String[] hostEncodings, IProgressMonitor monitor) throws SystemMessageException;


	/**
	 * Copy a file from the remote file system to the local system.
	 * @param remoteParent - a String designating the remote parent.
	 * @param remoteFile - a String designating the remote file residing in the parents.
	 * @param localFile - The file that is to be written.  If the file exists it is
	 * overwritten.
	 * @param isBinary - indicates whether the file is text on binary
	 * @param hostEncoding - the encoding on the host (if text)
	 * @param monitor the monitor for this potentially long running operation
	 * @throws SystemMessageException if an error occurs.
	 *     Typically this would be one of those in the
	 *     {@link RemoteFileException} family.
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public void download(String remoteParent, String remoteFile, File localFile, boolean isBinary, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Copy files from the remote file system to the local system.
	 * <p>
	 * If an error occurs during the download of a file, this operation stops on that file and a {@link SystemMessageException} is thrown.
	 * Files downloaded before that file will remain downloaded. Files in the list after that file will not be downloaded.
	 * The file on which the error occurs will not be downloaded.
	 *
	 * @param remoteParents - string designating the remote parents.
	 * @param remoteFiles - Strings designating the remote files residing in the
	 *            parents.
	 * @param localFiles - The files that are to be written. If the files exists
	 *            they are overwritten.
	 * @param isBinary - indicates whether the files are text on binary
	 * @param hostEncodings - the encodings on the host (if text)
	 * @param monitor the monitor for this potentially long running operation
	 * @throws SystemMessageException if an error occurs. Typically this would
	 *             be one of those in the {@link RemoteFileException} family.
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public void downloadMultiple(String[] remoteParents, String[] remoteFiles, File[] localFiles, boolean[] isBinary, String[] hostEncodings, IProgressMonitor monitor) throws SystemMessageException;



	/**
	 * Get an abstract remote file handle for a specified path.
	 *
	 * @param remoteParent the name of the parent directory on the remote file
	 *            system from which to retrieve the file.
	 * @param name the name of the file to be retrieved.
	 * @param monitor the monitor for this potentially long running operation
	 * @return the host file given the parent path and file name. Must not
	 *         return <code>null</code>, non-existing files should be
	 *         reported with an IHostFile object where
	 *         {@link IHostFile#exists()} returns <code>false</code>.
	 * @throws SystemMessageException if an error occurs. Typically this would
	 *             be one of those in the RemoteFileException family.
	 */
	public IHostFile getFile(String remoteParent, String name, IProgressMonitor monitor) throws SystemMessageException;


	/**
	 * List the contents of a remote folder.
	 *
	 * @param remoteParent - the name of the parent directory on the remote file
	 *            system from which to retrieve the child list.
	 * @param fileFilter - a string that can be used to filter the children.
	 *            Only those files matching the filter make it into the list.
	 *            The interface does not dictate where the filtering occurs.
	 * @param fileType - indicates whether to query files, folders, both or some
	 *            other type
	 * @param monitor the monitor for this potentially long running operation
	 * @return the list of host files.
	 * @throws SystemMessageException if an error occurs. Typically this would
	 *             be one of those in the RemoteFileException family.
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public IHostFile[] list(String remoteParent, String fileFilter, int fileType, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Get multiple abstract remote file handles for an array of specified
	 * paths.
	 * <p>
	 * If an error occurs during the retrieval an item, this operation stops on that item and a {@link SystemMessageException} is thrown.
	 * Items retrieved before that item will be returned. Items to be retrieved after that item will not be retrieved.
	 * The items on which the error occurs will not be retrieved.
	 *
	 * @param remoteParents - the list of remote parents
	 * @param names - the list of file names
	 * @param hostFiles a list to which the retrieved {@link IHostFile} objects will be appended
	 * @param monitor the monitor for this potentially long running operation
	 * @throws SystemMessageException if an error occurs. Typically this would
	 *             be one of those in the RemoteFileException family.
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public void getFileMultiple(String remoteParents[], String names[], List hostFiles, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * List the contents of multiple remote folders.
	 * <p>
	 * If an error occurs during the retrieval of the contents of a folder, this operation stops on that folder and a {@link SystemMessageException} is thrown.
	 * Items retrieved before that folder will be returned. Items in folders after that folder will not be retrieved.
	 * The items in the folder on which the error occurs will not be returned.
	 *
	 * @param remoteParents - the names of the parent directories on the remote
	 *            file system from which to retrieve the collective child list.
	 * @param fileFilters - a set of strings that can be used to filter the
	 *            children. Only those files matching the filter corresponding
	 *            to it's remoteParent make it into the list. The interface does
	 *            not dictate where the filtering occurs. For each remoteParent,
	 *            there must be a corresponding fileFilter.
	 * @param fileTypes - indicates whether to query files, folders, both or
	 *            some other type. For each remoteParent, there must be a
	 *            corresponding fileType. For the default list of available file
	 *            types see <code>IFileServiceContants</code>
	 * @param hostFiles a list to which the found {@link IHostFile} objects will be appended
	 * @param monitor the monitor for this potentially long running operation
	 * @throws SystemMessageException if an error occurs. Typically this would
	 *             be one of those in the RemoteFileException family.
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public void listMultiple(String[] remoteParents, String[] fileFilters, int[] fileTypes, List hostFiles, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * List the contents of multiple remote folders.
	 * <p>
	 * If an error occurs during the retrieval of the contents of a folder, this operation stops on that folder and a {@link SystemMessageException} is thrown.
	 * Items retrieved before that folder will be returned. Items in folders after that folder will not be retrieved.
	 * The items in the folder on which the error occurs will not be returned.
	 *
	 * @param remoteParents - the names of the parent directories on the remote
	 *            file system from which to retrieve the collective child list.
	 * @param fileFilters - a set of strings that can be used to filter the
	 *            children. Only those files matching the filter corresponding
	 *            to it's remoteParent make it into the list. The interface does
	 *            not dictate where the filtering occurs. For each remoteParent,
	 *            there must be a corresponding fileFilter.
	 * @param fileType - indicates whether to query files, folders, both or some
	 *            other type. All results will be of the specified type. For the
	 *            default list of available file types see
	 *            <code>IFileServiceContants</code>
	 * @param hostFiles a list to which the found {@link IHostFile} objects will be appended
	 * @param monitor the monitor for this potentially long running operation
	 * @throws SystemMessageException if an error occurs. Typically this would
	 *             be one of those in the RemoteFileException family.
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public void listMultiple(String[] remoteParents, String[] fileFilters, int fileType, List hostFiles, IProgressMonitor monitor) throws SystemMessageException;


	/**
	 * Get abstract remote file handles for the known remote file system roots.
	 *
	 * @param monitor the monitor for this potentially long running operation
	 *            Return the list of roots for this system
	 * @return the list of host files.
	 * @throws SystemMessageException if an error occurs. Typically this would
	 *             be one of those in the RemoteFileException family.
	 */
	public IHostFile[] getRoots(IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Return the user's home directory on this connection.
	 *
	 * The resulting IHostFile object is just a handle, so there is no guarantee
	 * that it refers to an existing file.
	 *
	 * This method may also return <code>null</code> if the home directory could
	 * not be determined (for instance, because the connection is not yet connected).
	 * In this case, clients are encouraged to query the home directory again once
	 * the connection is connected.
	 *
	 * @return A handle to the current user's home directory, or <code>null</code>
	 *     if the home directory could not be determined.
	 */
	public IHostFile getUserHome();

	/**
	 * Create a file on the host.
	 *
	 * @param remoteParent the parent directory
	 * @param fileName the name of the new file
	 * @param monitor the monitor for this potentially long running operation
	 * @return the newly created file
	 * @throws SystemMessageException if an error occurs. Typically this would
	 *             be one of those in the RemoteFileException family.
	 */
	public IHostFile createFile(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Create a folder on the host.
	 *
	 * Implementations are free to create missing parent folders or fail with a
	 * SystemMessageException if the parent folder does not yet exist. In
	 * general, creating missing parent folders is recommended if it doesn't
	 * require additional client-server round trips. Therefore the "Local" and
	 * "DStore" services do create missing parent folders.
	 *
	 * @param remoteParent the parent directory
	 * @param folderName the name of the new folder
	 * @param monitor the progress monitor
	 * @return the newly created folder
	 * @throws SystemMessageException if an error occurs. Typically this would
	 *             be one of those in the RemoteFileException family.
	 */
	public IHostFile createFolder(String remoteParent, String folderName, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Delete a file or folder on the host.
	 *
	 * @param remoteParent the folder containing the file to delete
	 * @param fileName the name of the file or folder to delete
	 * @param monitor the progress monitor
	 * @throws SystemMessageException if an error occurs or the user canceled
	 * 		the operation. SystemElementNotFoundException is thrown if the remote
	 * 		file doesn't exist.
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public void delete(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Delete a set of files or folders on the host. Should throw an exception
	 * if some files and folders were deleted and others were not due to an
	 * exception during the operation. Without an exception thrown in such
	 * cases, views may not be refreshed correctly to account for deleted
	 * resources.
	 * <p>
	 * If an error occurs during the deletion of an item, this operation stops on that item and a {@link SystemMessageException} is thrown.
	 * Items deleted before that item will remain deleted. Items specified after that item will not be deleted.
	 * The item on which the error occurs will not be deleted.
	 *
	 * @param remoteParents the array of folders containing the files to delete
	 * @param fileNames the names of the files or folders to delete
	 * @param monitor the progress monitor
	 * @throws SystemMessageException if an error occurs. Typically this would
	 *             be one of those in the RemoteFileException family.
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public void deleteBatch(String[] remoteParents, String[] fileNames, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Rename a file or folder on the host.
	 *
	 * @param remoteParent the folder containing the file to rename
	 * @param oldName the old name of the file or folder to rename
	 * @param newName the new name for the file
	 * @param monitor the progress monitor
	 * @throws SystemMessageException if an error occurs. Typically this would
	 *             be one of those in the RemoteFileException family.
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public void rename(String remoteParent, String oldName, String newName, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Rename a file or folder on the host.
	 *
	 * @param remoteParent the folder containing the file to rename
	 * @param oldName the old name of the file or folder to rename
	 * @param newName the new name for the file
	 * @param oldFile the file to update with the change
	 * @param monitor the progress monitor
	 * @throws SystemMessageException if an error occurs. Typically this would
	 *             be one of those in the RemoteFileException family.
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public void rename(String remoteParent, String oldName, String newName, IHostFile oldFile, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Move the file or folder specified to a different remote path.
	 *
	 * @param srcParent the folder containing the file or folder to move
	 * @param srcName the new of the file or folder to move
	 * @param tgtParent the destination folder for the move
	 * @param tgtName the name of the moved file or folder
	 * @param monitor the progress monitor
	 * @throws SystemMessageException if an error occurs. Typically this would
	 *             be one of those in the RemoteFileException family.
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public void move(String srcParent, String srcName, String tgtParent, String tgtName, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Copy the file or folder to the specified destination.
	 *
	 * @param srcParent the folder containing the file or folder to copy
	 * @param srcName the new of the file or folder to copy
	 * @param tgtParent the destination folder for the copy
	 * @param tgtName the name of the copied file or folder
	 * @param monitor the progress monitor
	 * @throws SystemMessageException if an error occurs. Typically this would
	 *             be one of those in the RemoteFileException family.
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public void copy(String srcParent, String srcName, String tgtParent, String tgtName, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Copy a set of files or folders to the specified destination.
	 * <p>
	 * If an error occurs during the copy of an item, this operation stops on that item and a {@link SystemMessageException} is thrown.
	 * Items copied before that item will remain copied. Items copied after that item will not be copied.
	 * The item on which the error occurs will not be copied.
	 *
	 * @param srcParents the folders containing each file or folder to copy
	 * @param srcNames the names of the files or folders to copy
	 * @param tgtParent the destination folder for the copy
	 * @param monitor the progress monitor
	 * @throws SystemMessageException if an error occurs. Typically this would
	 *             be one of those in the RemoteFileException family.
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public void copyBatch(String[] srcParents, String[] srcNames, String tgtParent, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Indicates whether the file system is case sensitive.
	 *
	 * @return true if the file system has case sensitive file names
	 */
	public boolean isCaseSensitive();

	/**
	 * Set the last modified stamp of the file or folder with the specified
	 * timestamp.
	 *
	 * Note that the precision to which the underlying file system supports last
	 * modified times may vary. Therefore, even if this method successfully sets
	 * the timestamp, there is no guarantee that the
	 * {@link IHostFile#getModifiedDate()} method after a following
	 * {@link #getFile(String, String, IProgressMonitor)} call returns exactly
	 * the same timestamp.
	 *
	 * @param parent the parent path of the file to set
	 * @param name the name of the file to set
	 * @param timestamp the new timestamp in milliseconds from January 1, 1970,
	 *            00:00:00 UTC.
	 * @param monitor the progress monitor
	 * @see IHostFile#getModifiedDate()
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public void setLastModified(String parent, String name, long timestamp, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Set the read-only permission of the specified file or folder.
	 *
	 * @param parent the parent path of the file to set
	 * @param name the name of the file to set
	 * @param readOnly indicates whether to make the file read-only or
	 *            read-write
	 * @param monitor the progress monitor
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public void setReadOnly(String parent, String name, boolean readOnly, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Gets the remote encoding.
	 * @param monitor the progress monitor.
	 * @return the encoding.
	 * @throws SystemMessageException if an error occurs.
	 * @since 2.0
	 */
	public String getEncoding(IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Get the input stream to access the contents a remote file. Clients should
	 * close the input stream when done.
	 *
	 * @param remoteParent the absolute path of the parent.
	 * @param remoteFile the name of the remote file.
	 * @param isBinary <code>true</code> if the file is a binary file,
	 *            <code>false</code> otherwise.
	 * @param monitor the progress monitor. Only used for the process of opening
	 *            the Stream. Implementations are not expected to use or update
	 *            the monitor for actual Stream transfer operations.
	 * @return the input stream to access the contents of the remote file.
	 * @throws SystemMessageException if an error occurs.
	 * @since org.eclipse.rse.services 2.0
	 */
	public InputStream getInputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Get the output stream to write to a remote file. Clients should close the
	 * output stream when done.
	 *
	 * @param remoteParent the absolute path of the parent.
	 * @param remoteFile the name of the remote file.
	 * @param isBinary <code>true</code> if the file is a binary file,
	 *            <code>false</code> otherwise.
	 * @param monitor the progress monitor. Only used for the process of opening
	 *            the Stream. Implementations are not expected to use or update
	 *            the monitor for actual Stream transfer operations.
	 * @return the input stream to access the contents of the remote file.
	 * @throws SystemMessageException if an error occurs.
	 * @since org.eclipse.rse.services 2.0
	 * @deprecated Use
	 *             {@link #getOutputStream(String, String, int, IProgressMonitor)}
	 *             instead
	 */
	public OutputStream getOutputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Get the output stream to write/append to a remote file. Clients should
	 * close the output stream when done.
	 *
	 * @param remoteParent the absolute path of the parent.
	 * @param remoteFile the name of the remote file.
	 * @param options bit wise or of option constants. Valid constants are
	 *            {@link IFileService#APPEND}, {@link IFileService#TEXT_MODE},
	 *            and {@link IFileService#NONE}
	 * @param monitor the progress monitor. Only used for the process of opening
	 *            the Stream. Implementations are not expected to use or update
	 *            the monitor for actual Stream transfer operations.
	 * @return the input stream to access the contents of the remote file.
	 * @throws SystemMessageException if an error occurs.
	 * @since org.eclipse.rse.services 3.0
	 */
	public OutputStream getOutputStream(String remoteParent, String remoteFile, int options, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Indicates whether this file service supports code page conversion using
	 * the IFileServiceCodePageConverter mechanism.  Certain extensions, such as
	 * property pages for encoding conversion can determine whether or not to
	 * display or enable themselves based on result of this call.
	 *
	 * @return whether this service supports encoding conversion
	 *
	 * @since org.eclipse.rse.services 3.0
	 */
	public boolean supportsEncodingConversion();
}
