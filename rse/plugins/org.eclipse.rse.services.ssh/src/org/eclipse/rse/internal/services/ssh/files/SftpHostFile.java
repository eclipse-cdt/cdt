/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - Adapted from FTPHostFile.
 * David McKnight   (IBM)        - [209593] [api] add support for "file permissions" and "owner" properties for unix files
 * Martin Oberhuber (Wind River) - [235360][ftp][ssh][local] Return proper "Root" IHostFile
 * Martin Oberhuber (Wind River) - [235472] [ssh] RSE doesn't show correct properties of the file system root ("/")
 *******************************************************************************/

package org.eclipse.rse.internal.services.ssh.files;

import java.io.File;

import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.IHostFilePermissions;
import org.eclipse.rse.services.files.IHostFilePermissionsContainer;

public class SftpHostFile implements IHostFile, IHostFilePermissionsContainer {

	private String fName;
	private String fParentPath;
	private boolean fIsDirectory = false;
	private boolean fIsRoot = false;
	private boolean fIsArchive = false;
	private boolean fIsReadable = true;
	private boolean fIsWritable = true;
	private boolean fIsExecutable = false;
	private boolean fExists = true;
	private long fLastModified = 0;
	private long fSize = 0;
	private boolean fIsLink = false;
	private String fLinkTarget;
	private String fCanonicalPath;
	private String[] fExtended = null;

	private IHostFilePermissions _permissions = null;

	//TODO just re-use or extend FTPHostFile instead of copying here?
	public SftpHostFile(String parentPath, String name, boolean isDirectory, boolean isRoot, boolean isLink, long lastModified, long size) {
		fParentPath = parentPath;
		fName = name;
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException();
		} else if (parentPath == null || isRoot) {
			//Root files must be consistent
			if (parentPath !=null || !isRoot /* || !isDirectory */) {
				throw new IllegalArgumentException();
			}
		} else if (name.indexOf('/')>=0) {
			//Non-root files must not have a relative path as name, or it would break parent/child relationships
			throw new IllegalArgumentException();
		}
		fIsDirectory = isDirectory;
		fIsRoot = isRoot;
		fLastModified = lastModified;
		fSize = size;
		fIsLink = isLink;
		fIsArchive = internalIsArchive();
	}

	public String getName() {
		return fName;
	}

	public boolean isHidden() {
		String name = getName();
		return name.charAt(0) == '.';
	}

	public String getParentPath() {
		return fParentPath;
	}

	public boolean isDirectory() {
		return fIsDirectory;
	}

	public boolean isFile() {
		return !(fIsDirectory || fIsRoot);
	}

	public boolean isRoot() {
		return fIsRoot;
	}

	public void setExists(boolean b) {
		fExists = b;
	}

	public boolean exists() {
		return fExists;
	}

	public String getAbsolutePath() {
		if (isRoot()) {
			return getName();
		} else {
			StringBuffer path = new StringBuffer(getParentPath());
			if (!fParentPath.endsWith("/")) //$NON-NLS-1$
			{
				path.append('/');
			}
			path.append(getName());
			return path.toString();
		}
	}

	public long getSize() {
		return fSize;
	}

	public long getModifiedDate() {
		return fLastModified;
	}

	public void renameTo(String newAbsolutePath) {
		int i = newAbsolutePath.lastIndexOf("/"); //$NON-NLS-1$
		if (i == -1) {
			//Rename inside the same parent folder.
			//FIXME is this really what was desired here? Or would we rename Roots?
			//Renaming Roots isn't possible, I'd think.
			fName = newAbsolutePath;
		}
		else if (i == 0) {
			// Renaming a root folder
			if (newAbsolutePath.length()==1) {
				//rename to root "/" -- should this work?
				fParentPath = null;
				fIsRoot = true;
				fName = newAbsolutePath;
			} else {
				fParentPath = "/"; //$NON-NLS-1$
				fName = newAbsolutePath.substring(i + 1);
			}
			fParentPath = "/"; //$NON-NLS-1$
			fName = newAbsolutePath.substring(i + 1);
		} else {
			fParentPath = newAbsolutePath.substring(0, i);
			fName = newAbsolutePath.substring(i+1);
		}
		fIsArchive = internalIsArchive();
	}

	protected boolean internalIsArchive() {
		return ArchiveHandlerManager.getInstance().isArchive(new File(getAbsolutePath()))
		&& !ArchiveHandlerManager.isVirtual(getAbsolutePath());
	}

	public boolean isArchive() {
		return fIsArchive;
	}

	public boolean isLink() {
		return fIsLink;
	}

	public void setLinkTarget(String linkTarget) {
		fLinkTarget = linkTarget;
	}

	public String getLinkTarget() {
		return fLinkTarget;
	}

	public void setCanonicalPath(String canonicalPath) {
		fCanonicalPath = canonicalPath;
	}

	public String getCanonicalPath() {
		if (fCanonicalPath==null) {
			return getAbsolutePath();
		} else {
			return fCanonicalPath;
		}
	}

	/**
	 * Set Extended data as key,value pairs.
	 *
	 * The data is maintained as a String array, where every element
	 * with an even index refers to a key, and the next element
	 * refers to its value. Example
	 *   extended[0] = "acl"
	 *   extended[1] = "joe,tim"
	 *   extended[2] = "version"
	 *   extended[3] = "/main/3"
	 *
	 * @param extended String[] array of key,value pairs
	 */
	public void setExtendedData(String[] extended) {
		fExtended = extended;
	}

	/**
	 * Return extended data as name,value pairs.
	 * @see #setExtendedData(String[])
	 *
	 * @return String[] array of key,value pairs
	 */
	public String[] getExtendedData() {
		return fExtended;
	}

	public String getClassification() {
		//TODO: isExecutable(), shellscript vs. binary
		String result;
		if (isLink()) {
			result = "symbolic link"; //$NON-NLS-1$
			if (fLinkTarget!=null) {
				if (fLinkTarget.startsWith(":dangling link")) { //$NON-NLS-1$
					String linkTarget = (fLinkTarget.length()<=15) ? "unknown" : fLinkTarget.substring(15); //$NON-NLS-1$
					result = "broken symbolic link to `" + linkTarget + "'"; //$NON-NLS-1$ //$NON-NLS-2$
				} else if(isDirectory()) {
					result += "(directory):" + fLinkTarget; //$NON-NLS-1$
				} else if(canExecute()) {
					result += "(executable):" +  fLinkTarget; //$NON-NLS-1$
				} else {
					result += "(file):" +  fLinkTarget; //$NON-NLS-1$
				}
			}
		} else if (isFile()) {
			if (canExecute()) {
				result = "executable"; //$NON-NLS-1$
			} else {
				result = "file"; //$NON-NLS-1$
			}
		} else if (isDirectory()) {
			result = "directory"; //$NON-NLS-1$
		} else {
			result = "unknown"; //default-fallback //$NON-NLS-1$
		}
		return result;
	}

	public void setReadable(boolean b) {
		fIsReadable=b;
	}
	public void setWritable(boolean b) {
		fIsWritable=b;
	}
	public void setExecutable(boolean b) {
		fIsExecutable=b;
	}

	public boolean canRead() {
		return fIsReadable;
	}
	public boolean canWrite() {
		return fIsWritable;
	}
	public boolean canExecute() {
		return fIsExecutable;
	}

	public IHostFilePermissions getPermissions() {
		return _permissions;
	}

	public void setPermissions(IHostFilePermissions permissions) {
		_permissions = permissions;
	}
}
