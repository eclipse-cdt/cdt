/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.rse.services.files;

/**
 * Objects that conform to this interface are delivered by the file services to their clients.
 * These objects are meant to represent objects that are files or file-like objects on a 
 * remote system. These would include files, folders, directories, archives and the like.
 * <p>
 * These objects are typically "handle" objects and can be created even though their corresponding
 * remote resources do not exist - in this case, the {@link #exists()} method will return 
 * <code>false</code>.
 * <p>
 * @see IFileService
 */
public interface IHostFile {
	/**
	 * Gets the simple name of the file object on the remote system.
	 * 
	 * @return The name of the file object on the remote system devoid of any qualifying path
	 * information.
	 */
	public String getName();

	/**
	 * Gets the absolute path name of the parent object of this object on the remote file system.
	 * 
	 * @return The fully qualified path of any parent object for this file. This would typically be the
	 * string representation of the absolute path as interpreted by the remote file system. Returns 
	 * <code>null</code> if {@link #isRoot()} is true.
	 */
	public String getParentPath();

	/**
	 * Gets the fully qualified path to this object in the remote file system.
	 * 
	 * The name is constructed as it would be used on the remote file system. 
	 * This string can be interpreted and used by its file service to locate 
	 * this object on the remote file system beginning at the file system root.
	 *    
	 * @return a String representing the path name. Never returns <code>null</code>. 
	 */
	public String getAbsolutePath();

	/**
	 * Determines if the file system object is hidden on the remote file system.
	 * 
	 * @return true if and only if the file on the remote system has a "hidden" attribute or a naming
	 * convention that would normal indicate that it was hidden when listing the contents of its parent
	 * on that file system. It is up to the file services to conform to the correct notion of "hidden" for
	 * the remote systems they support.
	 */
	public boolean isHidden();

	/**
	 * Determines if the file system object is a directory on the remote file system.
	 * 
	 * @return true if and only if the object on the remote system is a directory. That is, it contains 
	 * entries that can be interpreted as other IHostFile objects. A return value of true does not
	 * necessarily imply that isFile() returns false.
	 */
	public boolean isDirectory();

	/**
	 * Determines if the file system object is a "root" directory on the remote file system.
	 * 
	 * @return true if and only if the object on the remote system is a directory whose simple name and
	 * absolute path name are the same.
	 */
	public boolean isRoot();

	/**
	 * Determines if the file system object is a file on the remote file system.
	 * 
	 * @return true if and only if the object on the remote system can be considered to have "contents" that
	 * have the potential to be read and written as a byte or character stream. A return value of true 
	 * does not necessarily imply that {@link #isDirectory()} returns false.
	 */
	public boolean isFile();

	/**
	 * Determines if the file system object is "writeable" on the remote file system.
	 * 
	 * @return true if and only if the object on the remote system is a file that can be written. This could
	 * mean that there is write permission granted to this user or perhaps a "writable" attribute is set for the 
	 * file. It is up to the file services to conform to the correct notion of "writable" for the remote
	 * systems they support. For directory objects this should return true if the child objects may be added
	 * to or removed from the directory.
	 */
	public boolean canWrite();

	/**
	 * Determines if the file system object is "readable" on the remote file system.
	 * 
	 * @return true if and only if the object on the remote system is a file that can be read. This could
	 * mean that there is read permission granted to this user or perhaps a "readable" attribute is set for the 
	 * file. It is up to the file services to conform to the correct notion of "readable" for the remote
	 * systems they support. For directory objects this should return true if the user can determine the children
	 * of the directory.
	 */
	public boolean canRead();

	/**
	 * Determines if the file system object exists on the remote file system.
	 * 
	 * @return true if and only if the remote object represented by this object exists
	 * in the remote file system. Symbolic links on a UNIX file system exist even if
	 * the target they point to does not exist.
	 */
	public boolean exists();

	/**
	 * Determines if the file system object represents an archive on the remote file system.
	 * 
	 * @return true if and only if the remote object is a file that can be "extracted" to contain other files.
	 * Examples would be tar and zip files. It is up to the file services to conform to the correct notion of 
	 * "archive" for the remote systems they support. If a file service creates an object with
	 * this attribute as true then the file service must be able to extract the contents of the archive.
	 */
	public boolean isArchive();

	/**
	 * Gets the size of the file system object on the remote file system in bytes if isFile() is true.
	 * If the storage unit on the remote system is not bytes then the file service creating this must
	 * convert the remote value to bytes.
	 *  
	 * @return the size in bytes of the file if {@link #isFile()} is true, 0L otherwise.
	 */
	public long getSize();

	/**
	 * Gets a timestamp representing the date and time of last modification to the file.
	 * 
	 * @return the timestamp as obtained from the remote file system.
	 * The timestamp represents the time the file was modified in milliseconds from January 1, 1970, 00:00:00 UTC.
	 * Note that even so, comparison of timestamps between systems should be avoided since clock resolution and 
	 * accuracy vary widely from system to system.
	 * It may be necessary to convert from the timestamp of a remote file system to this format.
	 */
	public long getModifiedDate();

	/**
	 * Renames this abstract file handle.
	 * 
     * This does not physically rename the corresponding file on the 
     * remote system, it merely updates internal bookkeeping for a 
     * rename operation that needs to be performed separately through
     * an instance of @see IFileService. 
     *  
     * Therefore, this method cannot fail and no return value is given.
     * 
	 * @param newAbsolutePath The new path on the remote file system that
	 * this file will be renamed to.
	 */
	public void renameTo(String newAbsolutePath);
	
}
