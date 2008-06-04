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
 * David McKnight   (IBM)        - [209593] [api] add support for "file permissions" and "owner" properties for unix files
 * David McKnight   (IBM)        - [231209] [api][breaking] IRemoteFile.getSystemConnection() should be changed to IRemoteFile.getHost()
 * Martin Oberhuber (Wind River) - [234726] Update IRemoteFile Javadocs
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.core.subsystems;
import java.util.Date;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IRemoteContainer;
import org.eclipse.rse.core.subsystems.IRemotePropertyHolder;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.IHostFilePermissions;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;

/**
 * This interface represents a handle to a remote file system object,
 * which is either a file or a folder. This interface is similar to
 * a java.io.File object, but with some significant differences:
 * <ul>
 *   <li>This represents a "handle" to the remote file or folder, and allows to
 *        mark special folders as "root" of the file system. For editing purposes,
 *        a local copy of the remote file can be managed. The support for this is
 *        captured in a separate interface, {@link ISystemEditableRemoteObject}.
 *   <li>This interface has no action methods, only property access methods. The action methods
 *        such as delete, rename, list or getRemoteFileObject() are found in the 
 *        {@link IRemoteFileSubSystem} interface. All remote commands/actions are routed
 *        through subsystems in this remote system framework.
 *   <li>Similarly, IRemoteFile handle objects are not constructed directly. They are obtained
 *        from factory methods in {@link IRemoteFileSubSystem#getRemoteFileObject(String, org.eclipse.core.runtime.IProgressMonitor)},
 *        and related methods, which may delegate the actual object creation to an
 *        {@link IHostFileToRemoteFileAdapter} in case of an {@link IFileServiceSubSystem}
 *        instance.
 *   <li>There are no relative names. All names are fully qualified, and include
 *        the root, the path and the file name (unless this is a folder).
 *   <li>There are additional methods for querying the root (ie, c:\) and the
 *        parent directory (unqualified name of the parent directory).
 * </ul>
 * <p>
 * Note for subsystem providers: this method does not capture the set methods that the
 * RemoteFile class defines. For that, cast to {@link RemoteFile}.
 *
 * @noimplement This interface is not intended to be implemented by clients. Clients
 *     should subclass the provided {@link RemoteFile} or one of its subclasses
 *     instead. For use with file services (IFileServiceSubSystem), the
 *     {@link AbstractRemoteFile} class must be subclassed.
 */
public interface IRemoteFile extends IRemoteContainer, IRemotePropertyHolder, ISchedulingRule
{
    public static final char CONNECTION_DELIMITER = ':';
	public static final boolean ISROOT_YES = true;
	public static final boolean ISROOT_NO  = false;

	/**
	 * Querying properties for the property sheet can be expensive on some operating systems.
	 * By default all properties are shown on the property sheet for this object, unless true
	 * is returned from this query, in which only a couple properties are shown.
	 */
	public boolean showBriefPropertySet();

	/**
	 * Get parent subsystem.
	 *
	 * @return the Subsystem holding this file.
	 */
	public IRemoteFileSubSystem getParentRemoteFileSubSystem();

    /**
     * Return the separator character for this file system: \ or /.
     * Queries it from the subsystem factory.
     *
     * @return the separator character for this file system.
     */
    public char getSeparatorChar();

    /**
     * Return the separator character for this file system, as a string: "\" or "/".
     * Queries it from the subsystem factory.
     *
     * @return the separator character for this file system as a String.
     */

    public String getSeparator();

	/**
	 * Return as a string the line separator for this file system
	 * Queries it from the subsystem factory.
	 */
	public String getLineSeparator();

    /**
     * Return the connection this remote file is from.
     * @since 3.0 renamed getSystemConnection() to getHost()
     */
    public IHost getHost();

    /**
     * Return the parent remote file object expanded to get this object,
     * or <code>null</code> if no such parent exists.
     *
     * @return the parent remote file object or <code>null</code>.
     */
    public IRemoteFile getParentRemoteFile();

    /**
     * Return the filter string resolved to get this object
     */
    public RemoteFileFilterString getFilterString();

    /**
     * If this is a folder, it is possible that it is listed as part of a multiple filter string
     *  filter. In this case, when the folder is expanded, we want to filter the file names to
     *  show all the files that match any of the filter strings that have the same parent path.
     * <p>
     * This method supports that by returning all the filter strings in the filter which have the
     *  same parent path as was used to produce this file.
     */
    public RemoteFileFilterString[] getAllFilterStrings();

    /**
     * Get fully qualified name: root plus path plus name. No connection name.
     *
     * @return the fully qualified path for uniquely addressing this file
     *     on the remote host. Never returns <code>null</code>.
     */
    public String getAbsolutePath();

    /**
     * Get fully qualified connection and file name: profile.connection\path\file.
     * Note the separator character between the profile name and the connection name is always '.'
     * Note the separator character between the connection and qualified-file is always ':'
     */
    public String getAbsolutePathPlusConnection();

    /**
     * Get the display name for this file. By default, this should be the same as the name
     * If this object represents only a root drive, this is the same as getRoot().
     */
    public String getLabel();

    /**
     * Get unqualified file name. No root and no path.
     * If this object represents only a root drive, this is the same as getRoot().
     */
    public String getName();

    /**
     * Get fully qualified path and name of folder containing this file or folder.
     * Returns the root and path. No file name, and no ending separator.
     * <p>
     * If this object represent only a root drive, this returns null;
     * <p>
     * Example: <code>c:\folder1\folder2\file1.ext</code> results in <code>c:\folder1\folder2</code>
     */
    public String getParentPath();

    /**
     * Get fully qualified path and name of folder containing this file or folder, minus the root.
     * Returns the path. No root prefix. No file name, and no ending separator.
     * <p>
     * If this object represent only a root drive, this returns null;
     * <p>
     * Example: <code>c:\folder1\folder2\file1.ext</code> results in <code>folder1\folder2</code>
     */
    public String getParentNoRoot();

    /**
     * Get the root part of the name.
     * <p>
     * <ul>
     *   <li>Example: <code>c:\folder1\folder2\file1.ext</code> results in <code>c:\</code>
     *   <li>Example: <code>/folder1/folder2/file1.ext</code> results in <code>/</code>
     * </ul>
     */
    public String getRoot();

    /**
     * Get the unqualified name of the parent directory containing this file or folder.
     * Compare this to getParent() that returns the fully qualified parent directory.
     * If this object represents only a root drive, this returns null.
     * <p>
     * Example: <code>c:\folder1\folder2\file1.ext</code> results in <code>folder2</code>
     */
    public String getParentName();

	/**
	 * Return the extension part of a file name.
	 * Eg, for abc.java, return "java"
	 */
	public String getExtension();

    /**
     * Returns true if this represents a root folder (eg: c:\\ or /).
     */
    public boolean isRoot();

    /**
     * Returns true if this represents a folder (eg: c:\\folder)
     */
    public boolean isDirectory();

    /**
     * Returns true if this represents a file, versus a root or folder
     */
    public boolean isFile();

    /**
     * Returns true if this represents an archive file, versues a non-archive file
     */
    public boolean isArchive();

    /**
     * Returns true if this is a binary file
     */
    public boolean isBinary();

    /**
     * Returns true if this is a text file
     */
    public boolean isText();

    /**
     * Returns true if this is a hidden file.
     */
    public boolean isHidden();

    /**
     * Returns true if the application can read this file.
     */
    public boolean canRead();

    /**
     * Returns true if the application can write to this file.
     */
    public boolean canWrite();

    /**
     * Returns true if this folder or file actually exists.
     */
    public boolean exists();

    /**
     * Returns the time (in milliseconds since epoch) this file was last modified.
     */
    public long getLastModified();

	/**
	 * Return the last modified time as a Date object.
	 */
	public Date getLastModifiedDate() ;

    /**
     * Returns the length, in bytes, of this file.
     */
    public long getLength();

    /**
     * Returns true if the ReadOnly Property should be shown in the property page.
     */
    public boolean showReadOnlyProperty();

    // ==================================
    // for comparator interface...
    // ==================================
    /**
     * Compare one remote file to another. This enables us to sort the files so they
     * are shown folders-first, and in alphabetical order.
     */
    public int compareTo(Object other) throws ClassCastException;

    /**
     * Get the object.
     */
    public Object getFile();

    /**
     * Note: if this remoteFile is the same as the file passed
     * as parameter, then this method returns true.
     */
    public boolean isAncestorOf(IRemoteFile file);

	/**
	 * Note: if this remoteFile is the same as the file passed
	 * as parameter, then this method returns true.
	 */
    public boolean isDescendantOf(IRemoteFile file);

	/**
	 * @return Any comments stored with the file in the file system or archive.
	 */
    public String getComment();

    /**
     * Returns the classification of this file.  If the file is
     * an executable, then "executable(...)" will be returned. If the
     * file is a symbolic link then it will appear as "symbolic link(....):resolvedPath".
     * The resolvedPath is the path that the link resolves to.
     * A symbolic link that resolves to an executable would appear as
     * "symbolic link(executable(...)):resolvedPath".
     * By default this should just return "file" or "directory".
     * @return the classification, or "unknown", or <code>null</code> if not classifiable.
     */
    public String getClassification();

    /**
     * Returns whether the file is executable or not.
     * @return <code>true</code> if the file is executable, <code>false</code> otherwise.
     */
    public boolean isExecutable();

    /**
     * Returns whether the file is a symbolic link or not.
     * @return <code>true</code> if the file is a symbolic link, <code>false</code> otherwise.
     */
    public boolean isLink();

    /**
     * Returns the canonical path of the remote file.
     * @return the resolved path if the file is a symbolic link, or
     */
    public String getCanonicalPath();

    public IHostFile getHostFile();

    /**
     * Returns the encoding of the remote file.
     * @return the encoding of the remote file.
     * @since 2.0
     */
    public String getEncoding();

    /**
     * Returns the permissions for this file if they exist
     * @return the permissions
     * @since 3.0
     */
    public IHostFilePermissions getPermissions();

}
